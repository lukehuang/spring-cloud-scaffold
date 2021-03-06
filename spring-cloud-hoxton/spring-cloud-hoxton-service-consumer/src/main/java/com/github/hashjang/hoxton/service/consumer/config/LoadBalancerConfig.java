package com.github.hashjang.hoxton.service.consumer.config;

import brave.Span;
import brave.Tracer;
import feign.Request;
import feign.Response;
import io.github.resilience4j.bulkhead.BulkheadRegistry;
import io.github.resilience4j.bulkhead.ThreadPoolBulkhead;
import io.github.resilience4j.bulkhead.ThreadPoolBulkheadRegistry;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.core.ConfigurationNotFoundException;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.github.resilience4j.retry.RetryRegistry;
import io.vavr.control.Try;
import lombok.extern.log4j.Log4j2;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultConnectionKeepAliveStrategy;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.springframework.cloud.loadbalancer.annotation.LoadBalancerClients;
import org.springframework.cloud.loadbalancer.blocking.client.BlockingLoadBalancerClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.openfeign.loadbalancer.FeignBlockingLoadBalancerClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

@Configuration
@LoadBalancerClients(defaultConfiguration = {CommonLoadBalancerConfig.class})
@EnableFeignClients(basePackages = "com.github.hashjang.hoxton")
public class LoadBalancerConfig {
    @Bean
    public HttpClient getHttpClient() {
        // 长连接保持30秒
        PoolingHttpClientConnectionManager pollingConnectionManager = new PoolingHttpClientConnectionManager(5, TimeUnit.MINUTES);
        // 总连接数
        pollingConnectionManager.setMaxTotal(1000);
        // 同路由的并发数
        pollingConnectionManager.setDefaultMaxPerRoute(1000);

        HttpClientBuilder httpClientBuilder = HttpClients.custom();
        httpClientBuilder.setConnectionManager(pollingConnectionManager);
        // 保持长连接配置，需要在头添加Keep-Alive
        httpClientBuilder.setKeepAliveStrategy(new DefaultConnectionKeepAliveStrategy());
        return httpClientBuilder.build();
    }

    @Bean
    public FeignBlockingLoadBalancerClient feignBlockingLoadBalancerCircuitBreakableClient(HttpClient httpClient, BlockingLoadBalancerClient loadBalancerClient, BulkheadRegistry bulkheadRegistry, ThreadPoolBulkheadRegistry threadPoolBulkheadRegistry, CircuitBreakerRegistry circuitBreakerRegistry, RateLimiterRegistry rateLimiterRegistry, RetryRegistry retryRegistry, Tracer tracer) {
        return new FeignBlockingLoadBalancerClient(new CircuitBreakableClient(
                httpClient,
                bulkheadRegistry,
                threadPoolBulkheadRegistry,
                circuitBreakerRegistry,
                rateLimiterRegistry,
                retryRegistry,
                tracer),
                loadBalancerClient);
    }

    @Log4j2
    public static class CircuitBreakableClient extends feign.httpclient.ApacheHttpClient {
        private final BulkheadRegistry bulkheadRegistry;
        private final ThreadPoolBulkheadRegistry threadPoolBulkheadRegistry;
        private final CircuitBreakerRegistry circuitBreakerRegistry;
        private final RateLimiterRegistry rateLimiterRegistry;
        private final RetryRegistry retryRegistry;
        private final Tracer tracer;

        public CircuitBreakableClient(HttpClient httpClient, BulkheadRegistry bulkheadRegistry, ThreadPoolBulkheadRegistry threadPoolBulkheadRegistry, CircuitBreakerRegistry circuitBreakerRegistry, RateLimiterRegistry rateLimiterRegistry, RetryRegistry retryRegistry, Tracer tracer) {
            super(httpClient);
            this.bulkheadRegistry = bulkheadRegistry;
            this.threadPoolBulkheadRegistry = threadPoolBulkheadRegistry;
            this.circuitBreakerRegistry = circuitBreakerRegistry;
            this.rateLimiterRegistry = rateLimiterRegistry;
            this.retryRegistry = retryRegistry;
            this.tracer = tracer;
        }

        @Override
        public Response execute(Request request, Request.Options options) throws IOException {
            String serviceName = request.requestTemplate().feignTarget().name();
            String serviceInstanceId = getServiceInstanceId(request);
            String serviceInstanceMethodId = getServiceInstanceMethodId(request);
            ThreadPoolBulkhead threadPoolBulkhead;
            CircuitBreaker circuitBreaker;
            try {
                //每个实例一个线程池
                threadPoolBulkhead = threadPoolBulkheadRegistry.bulkhead(serviceInstanceId, serviceName);
            } catch (ConfigurationNotFoundException e) {
                threadPoolBulkhead = threadPoolBulkheadRegistry.bulkhead(serviceInstanceId);
            }
            try {
                //每个服务实例具体方法一个resilience4j熔断记录器，在服务实例具体方法维度做熔断，所有这个服务的实例具体方法共享这个服务的resilience4j熔断配置
                circuitBreaker = circuitBreakerRegistry.circuitBreaker(serviceInstanceMethodId, serviceName);
            } catch (ConfigurationNotFoundException e) {
                circuitBreaker = circuitBreakerRegistry.circuitBreaker(serviceInstanceMethodId);
            }
            //保持traceId
            Span span = tracer.currentSpan();
            Supplier<CompletionStage<Response>> completionStageSupplier = ThreadPoolBulkhead.decorateSupplier(threadPoolBulkhead,
                    CircuitBreaker.decorateSupplier(circuitBreaker, () -> {
                        try (Tracer.SpanInScope cleared = tracer.withSpanInScope(span)) {
                            log.info("call url: {} -> {}", request.httpMethod(), request.url());
                            Response execute = super.execute(request, options);
                            //对于非返回200的接口，抛出异常
                            if (execute.status() != HttpStatus.OK.value()) {
                                execute.close();
                                throw new ResponseWrapperException(execute.toString(), execute);
                            }
                            return execute;
                        } catch (Exception e) {
                            throw new ResponseWrapperException(e.getMessage(), e);
                        }
                    })
            );

            try {
                return Try.ofSupplier(completionStageSupplier).get().toCompletableFuture().join();
            } catch (CompletionException e) {
                Throwable cause = e.getCause();
                if (cause instanceof ResponseWrapperException) {
                    ResponseWrapperException responseWrapperException = (ResponseWrapperException) cause;
                    if (responseWrapperException.getResponse() != null) {
                        return (Response) responseWrapperException.getResponse();
                    }
                }
                throw new ResponseWrapperException(cause.getMessage(), cause);
            }
        }

        private String getServiceInstanceId(Request request) throws MalformedURLException {
            String serviceName = request.requestTemplate().feignTarget().name();
            URL url = new URL(request.url());
            return serviceName + ":" + url.getHost() + ":" + url.getPort();
        }

        private String getServiceInstanceMethodId(Request request) throws MalformedURLException {
            String serviceName = request.requestTemplate().feignTarget().name();
            URL url = new URL(request.url());
            //通过微服务名称 + 实例 + 方法的方式，获取唯一id
            String methodName = request.requestTemplate().methodMetadata().method().toGenericString();
            return serviceName + ":" + url.getHost() + ":" + url.getPort() + ":" + methodName;
        }
    }
}
