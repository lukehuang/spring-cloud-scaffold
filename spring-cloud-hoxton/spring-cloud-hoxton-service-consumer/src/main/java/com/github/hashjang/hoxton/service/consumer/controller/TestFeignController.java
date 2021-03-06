package com.github.hashjang.hoxton.service.consumer.controller;

import com.github.hashjang.hoxton.service.consumer.feign.ServiceProvider2TestExceptionThrownFeignCleint;
import com.github.hashjang.hoxton.service.consumer.feign.ServiceProvider2TestFeignCleint;
import com.github.hashjang.hoxton.service.consumer.feign.ServiceProvider2TestReadTimeoutFeignCleint;
import com.github.hashjang.hoxton.service.consumer.feign.ServiceProviderTestExceptionThrownFeignCleint;
import com.github.hashjang.hoxton.service.consumer.feign.ServiceProviderTestFeignCleint;
import com.github.hashjang.hoxton.service.consumer.feign.ServiceProviderTestReadTimeoutFeignCleint;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Log4j2
@RestController
public class TestFeignController {
    @Autowired
    private ServiceProviderTestExceptionThrownFeignCleint serviceProviderTestExceptionThrownFeignCleint;
    @Autowired
    private ServiceProviderTestFeignCleint serviceProviderTestFeignCleint;
    @Autowired
    private ServiceProviderTestReadTimeoutFeignCleint serviceProviderTestReadTimeoutFeignCleint;
    @Autowired
    private ServiceProvider2TestExceptionThrownFeignCleint serviceProvider2TestExceptionThrownFeignCleint;
    @Autowired
    private ServiceProvider2TestFeignCleint serviceProvider2TestFeignCleint;
    @Autowired
    private ServiceProvider2TestReadTimeoutFeignCleint serviceProvider2TestReadTimeoutFeignCleint;

    @RequestMapping("/testFeign")
    public void testFeign() {
        Map<String, String> map = Map.of("time", System.currentTimeMillis() + "");
        try {
            log.info("testGet {}", serviceProviderTestFeignCleint.testGet(map));
            log.info("testGet {}", serviceProviderTestFeignCleint.testGet(map));
//            TimeUnit.SECONDS.sleep(2);
        } catch (Exception e) {
            log.error("testGet error: {}", e.getMessage());
        }
        try {
            log.info("testPost {}", serviceProviderTestFeignCleint.testPost(map));
            log.info("testPost {}", serviceProviderTestFeignCleint.testPost(map));
//            TimeUnit.SECONDS.sleep(2);
        } catch (Exception e) {
            log.error("testPost error: {}", e.getMessage());
        }
        try {
            log.info("testPut {}", serviceProviderTestFeignCleint.testPut(map));
            log.info("testPut {}", serviceProviderTestFeignCleint.testPut(map));
//            TimeUnit.SECONDS.sleep(2);
        } catch (Exception e) {
            log.error("testPut error: {}", e.getMessage());
        }
        try {
            log.info("testDelete {}", serviceProviderTestFeignCleint.testDelete(map));
            log.info("testDelete {}", serviceProviderTestFeignCleint.testDelete(map));
//            TimeUnit.SECONDS.sleep(2);
        } catch (Exception e) {
            log.error("testDelete error: {}", e.getMessage());
        }
        try {
            log.info("testTimeoutGet {}", serviceProviderTestReadTimeoutFeignCleint.testTimeoutGet());
            log.info("testTimeoutGet {}", serviceProviderTestReadTimeoutFeignCleint.testTimeoutGet());
//            TimeUnit.SECONDS.sleep(2);
        } catch (Exception e) {
            log.error("testTimeoutGet error: {}", e.getMessage());
        }
        try {
            log.info("testTimeoutPost {}", serviceProviderTestReadTimeoutFeignCleint.testTimeoutPost());
            log.info("testTimeoutPost {}", serviceProviderTestReadTimeoutFeignCleint.testTimeoutPost());
//            TimeUnit.SECONDS.sleep(2);
        } catch (Exception e) {
            log.error("testTimeoutPost error: {}", e.getMessage());
        }
        try {
            log.info("testTimeoutPut {}", serviceProviderTestReadTimeoutFeignCleint.testTimeoutPut());
            log.info("testTimeoutPut {}", serviceProviderTestReadTimeoutFeignCleint.testTimeoutPut());
//            TimeUnit.SECONDS.sleep(2);
        } catch (Exception e) {
            log.error("testTimeoutPut error: {}", e.getMessage());
        }
        try {
            log.info("testTimeoutDelete {}", serviceProviderTestReadTimeoutFeignCleint.testTimeoutDelete());
            log.info("testTimeoutDelete {}", serviceProviderTestReadTimeoutFeignCleint.testTimeoutDelete());
//            TimeUnit.SECONDS.sleep(2);
        } catch (Exception e) {
            log.error("testTimeoutDelete error: {}", e.getMessage());
        }
        try {
            log.info("testExceptionThrownGet {}", serviceProviderTestExceptionThrownFeignCleint.testExceptionThrownGet());
            log.info("testExceptionThrownGet {}", serviceProviderTestExceptionThrownFeignCleint.testExceptionThrownGet());
//            TimeUnit.SECONDS.sleep(2);
        } catch (Exception e) {
            log.error("testExceptionThrownGet error: {}", e.getMessage());
        }
        try {
            log.info("testExceptionThrownPost {}", serviceProviderTestExceptionThrownFeignCleint.testExceptionThrownPost());
            log.info("testExceptionThrownPost {}", serviceProviderTestExceptionThrownFeignCleint.testExceptionThrownPost());
//            TimeUnit.SECONDS.sleep(2);
        } catch (Exception e) {
            log.error("testExceptionThrownPost error: {}", e.getMessage());
        }
        try {
            log.info("testExceptionThrownPut {}", serviceProviderTestExceptionThrownFeignCleint.testExceptionThrownPut());
            log.info("testExceptionThrownPut {}", serviceProviderTestExceptionThrownFeignCleint.testExceptionThrownPut());
//            TimeUnit.SECONDS.sleep(2);
        } catch (Exception e) {
            log.error("testExceptionThrownPut error: {}", e.getMessage());
        }
        try {
            log.info("testExceptionThrownDelete {}", serviceProviderTestExceptionThrownFeignCleint.testExceptionThrownDelete());
            log.info("testExceptionThrownDelete {}", serviceProviderTestExceptionThrownFeignCleint.testExceptionThrownDelete());
//            TimeUnit.SECONDS.sleep(2);
        } catch (Exception e) {
            log.error("testExceptionThrownDelete error: {}", e.getMessage());
        }
    }

    @RequestMapping("/testVariousServiceFeign")
    public void testVariousServiceFeign() {
        try {
            log.info("service-provider testTimeoutGet {}", serviceProviderTestReadTimeoutFeignCleint.testTimeoutGet());
        } catch (Exception e) {
            log.error("service-provider testTimeoutGet error: {}", e.getMessage());
        }
        try {
            log.info("service-provider2 testTimeoutGet {}", serviceProvider2TestReadTimeoutFeignCleint.testTimeoutGet());
        } catch (Exception e) {
            log.error("service-provider2 testTimeoutGet error: {}", e.getMessage());
        }
        try {
            log.info("service-provider testExceptionThrownDelete {}", serviceProviderTestExceptionThrownFeignCleint.testExceptionThrownGet());
        } catch (Exception e) {
            log.error("service-provider testExceptionThrownDelete error: {}", e.getMessage());
        }
        try {
            log.info("service-provider2 testExceptionThrownDelete {}", serviceProvider2TestExceptionThrownFeignCleint.testExceptionThrownGet());
        } catch (Exception e) {
            log.error("service-provider2 testExceptionThrownDelete error: {}", e.getMessage());
        }
    }
}
