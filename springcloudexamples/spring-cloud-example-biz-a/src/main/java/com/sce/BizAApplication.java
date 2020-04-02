package com.sce;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

//@ComponentScan(basePackages = "com.sce")
@EnableFeignClients(basePackages = "com.sce.feignService")
@SpringBootApplication
public class BizAApplication {

    public static void main(String[] args) {
        SpringApplication.run(BizAApplication.class, args);
    }
}
