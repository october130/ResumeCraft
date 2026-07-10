package com.example.myself_resume_analyzer.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

@Configuration
public class AsyncConfig {
    @Bean
   public Executor resumeParseExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();//创建一个线程池
        executor.setCorePoolSize(2);//平时保持2个线程
        executor.setMaxPoolSize(5);//最多允许5个线程
        executor.setQueueCapacity(20);//最多需要排队20个任务
        executor.setThreadNamePrefix("resume-parse-");//线程名前缀

        executor.setRejectedExecutionHandler(
                new ThreadPoolExecutor.CallerRunsPolicy()
        );//拒绝策略，当线程池达到最大线程数时，会调用这个策略，这里选择使用CallerRunsPolicy，即由调用者执行任务由tomcat线程池执行
        executor.initialize();
        return executor;

    }

}
