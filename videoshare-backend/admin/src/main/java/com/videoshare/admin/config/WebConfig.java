// 路径: admin/src/main/java/com/videoshare/admin/config/WebConfig.java
package com.videoshare.admin.config;

import com.videoshare.admin.interceptor.AdminInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.Resource;

/**
 * Spring MVC 全局配置类
 *
 * 注册拦截器（哪些路径需要验证 token，哪些放行）
 * CORS 已迁移至 Gateway 统一处理
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Resource
    private AdminInterceptor adminInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry
            .addInterceptor(adminInterceptor)
            .addPathPatterns("/admin/**")
            .excludePathPatterns(
                "/admin/account/login",
                "/admin/account/checkCode"
            );
    }
}
