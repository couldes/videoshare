package com.videoshare.web.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${project.folder:d:/webser/videoshare/}")
    private String projectFolder;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String uploadDir = projectFolder.endsWith("/") || projectFolder.endsWith("\\")
                ? projectFolder + "videos/"
                : projectFolder + "/videos/";
        registry.addResourceHandler("/video/resource/**")
                .addResourceLocations("file:" + uploadDir);
    }
}
