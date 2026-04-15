// 路径: admin/src/main/java/com/videoshare/admin/config/WebConfig.java
package com.videoshare.admin.config;

import com.videoshare.admin.interceptor.AdminInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.Resource;

/**
 * Spring MVC 全局配置类
 *
 * 两件事：
 * 1. 注册拦截器（哪些路径需要验证 token，哪些放行）
 * 2. 配置跨域（admin 前端运行在 3001，后端在 8081，端口不同 = 跨域）
 *
 * 什么是跨域（CORS）？
 * 浏览器安全策略：A网站的页面不能直接调用B网站的接口
 * 例：前端 localhost:3001 调用 localhost:8081 的接口 → 跨域
 * 配置 CORS 就是"告诉浏览器：这个跨域请求我允许"
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Resource
    private AdminInterceptor adminInterceptor;

    // 
    // 注册拦截器
    // 
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry
            .addInterceptor(adminInterceptor)   // 使用我们写的 AdminInterceptor
            .addPathPatterns("/admin/**")        // 拦截所有 /admin/ 开头的请求
            .excludePathPatterns(               // 以下路径不需要验证 token（白名单）
                "/admin/account/login",          // 登录接口（没 token 才来登录）
                "/admin/account/checkCode"       // 验证码接口
            );
    }

    // 
    // 配置跨域（开发环境允许所有来源，生产环境应限制具体域名）
    // 
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry
            .addMapping("/**")                   // 所有路径都允许跨域
            .allowedOriginPatterns("*")          // 允许所有来源（生产改为具体域名）
            .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
            .allowedHeaders("*")                 // 允许所有请求头（包括 Authorization）
            .allowCredentials(true)              // 允许携带 Cookie
            .maxAge(3600);                       // 预检请求缓存1小时（减少 OPTIONS 请求）
    }
}
