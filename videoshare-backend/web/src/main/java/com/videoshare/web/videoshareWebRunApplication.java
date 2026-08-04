package com.videoshare.web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = ("com.videoshare"))
@EnableScheduling
@EnableFeignClients
public class videoshareWebRunApplication {
    public static void main(String[] args) {
        SpringApplication.run(videoshareWebRunApplication.class,args);
    }
}
