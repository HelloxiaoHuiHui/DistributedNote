package com.sce.feignService;


import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;

@Service
@FeignClient(name = "spring-cloud-example-biz-b") //指定服务名称
public interface RemoteService {

    @GetMapping("/hello")
    String sayHello();
}
