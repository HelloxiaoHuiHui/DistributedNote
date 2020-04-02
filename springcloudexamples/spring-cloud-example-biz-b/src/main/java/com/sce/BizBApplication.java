package com.sce;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients(basePackages = "com.sce.feignService")
@SpringBootApplication
public class BizBApplication {

    public static void main(String[] args) {
        SpringApplication.run(BizBApplication.class, args);
    }
}
