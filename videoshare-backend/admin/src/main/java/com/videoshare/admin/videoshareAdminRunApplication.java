package com.videoshare.admin;//包声明，表示该类属于：com.videoshare.admin

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;//Spring Boot 应用启动的核心类
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication(scanBasePackages = ("com.videoshare"))
//启动应用时，scanBasePackages 决定 Spring 去哪里找 “组件（Bean），只扫描 启动类所在包 + 子包，把它们注册为 Spring 的 Bean
// 禁用 Spring Boot 自动配置数据库
public class videoshareAdminRunApplication {
    public static void main(String[] args) {
        SpringApplication.run(videoshareAdminRunApplication.class,args);
    }
}
