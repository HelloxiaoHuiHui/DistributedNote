package com.sce.controller;

import com.sce.feignService.RemoteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/hello")
public class HelloController {

    @Autowired
    private RemoteService remoteService;

    /**
    * 示例方法
    * */
    @GetMapping
    public String sayHello(){
        return "Hello,This is Biz-A Service.";
    }

    /**
     * 示例方法
     * */
    @GetMapping("call2b")
    public String sayHello2B(){
        return remoteService.sayHello();
    }
}
