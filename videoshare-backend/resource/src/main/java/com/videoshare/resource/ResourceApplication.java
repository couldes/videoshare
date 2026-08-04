package com.videoshare.resource;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 资源服务：只做文件 I/O（上传存储、静态资源、FFmpeg 转码），不连接 MySQL
 * scanBasePackages 额外包含 common.utils，以引入 SnowflakeIdGenerator 组件
 */
@SpringBootApplication(exclude = DataSourceAutoConfiguration.class,
        scanBasePackages = {"com.videoshare.resource", "com.videoshare.common.utils"})
@EnableFeignClients
public class ResourceApplication {
    public static void main(String[] args) {
        SpringApplication.run(ResourceApplication.class, args);
    }
}
