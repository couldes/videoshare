package com.videoshare.web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication(scanBasePackages = ("com.videoshare"))
public class videoshareWebRunApplication {
    public static void main(String[] args) {
        SpringApplication.run(videoshareWebRunApplication.class,args);
    }
}
