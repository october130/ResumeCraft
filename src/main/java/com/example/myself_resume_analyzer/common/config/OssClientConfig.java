package com.example.myself_resume_analyzer.common.config;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OssClientConfig {
    @Resource
    private OssConfig ossConfig;

    @Bean
    public OSS ossClient() {
        return new OSSClientBuilder().
                build(
                        ossConfig.getEndpoint(),
                        ossConfig.getAccessKeyId(),
                        ossConfig.getAccessKeySecret()
                );
    }
}
