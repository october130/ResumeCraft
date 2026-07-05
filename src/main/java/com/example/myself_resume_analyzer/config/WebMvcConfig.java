package com.example.myself_resume_analyzer.config;

import com.example.myself_resume_analyzer.interceptor.AuthInterceptor;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    @Resource
    private AuthInterceptor authInterceptor;
    @Override
    public void addInterceptors(org.springframework.web.servlet.config.annotation.InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/api/**")//拦截以api开头的所有请求
                .excludePathPatterns(//不拦截的请求
                        "/api/user/login",
                        "/api/user/register",
                        "/swagger-ui/**",
                        "/api/user/sms/send",
                        "/v3/api-docs/**"
                        );
    }
}
