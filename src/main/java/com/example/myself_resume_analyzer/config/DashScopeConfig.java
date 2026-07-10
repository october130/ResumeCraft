package com.example.myself_resume_analyzer.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data//lombok注解，生成getter和setter方法
@Component
@ConfigurationProperties(prefix ="aliyun.dashscope")//将配置文件以aliyun.dashscope开头的属性值注入到当前类中
public class DashScopeConfig {
    private String apiKey;
}
