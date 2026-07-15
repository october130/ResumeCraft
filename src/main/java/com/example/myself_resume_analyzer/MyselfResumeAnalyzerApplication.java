package com.example.myself_resume_analyzer;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@MapperScan({"com.example.myself_resume_analyzer.resume.mapper", "com.example.myself_resume_analyzer.interview.mapper"})
@EnableAsync//启用异步任务，开启异步支持
public class MyselfResumeAnalyzerApplication {

    public static void main(String[] args) {
        SpringApplication.run(MyselfResumeAnalyzerApplication.class, args);
    }

}
