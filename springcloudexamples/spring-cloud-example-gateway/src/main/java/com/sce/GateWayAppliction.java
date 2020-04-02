package com.sce;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;

@EnableZuulProxy
@SpringBootApplication
public class GateWayAppliction {
    public static void main(String[] args) {
        SpringApplication.run(GateWayAppliction.class,args);
    }
}
