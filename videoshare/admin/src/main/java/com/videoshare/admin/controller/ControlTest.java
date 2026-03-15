package com.videoshare.admin.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController//告诉 Spring Boot：这个类里包含处理 HTTP 请求的方法，需要注册到 Spring 容器,被 Spring 扫描到之后，这个类就成为 Spring Bean
public class ControlTest {
    @RequestMapping("/test")//这个方法会 处理访问 /test 的 HTTP 请求
    public String test(){
        return "启动成功";
    }
}
