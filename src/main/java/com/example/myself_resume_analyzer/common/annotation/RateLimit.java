package com.example.myself_resume_analyzer.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)// 注解作用在方法上
@Retention(RetentionPolicy.RUNTIME)// 注解在运行时保留
public @interface RateLimit {

    String key();// 缓存的 key,用于限流标识
    int limit() default 5;// 限制的次数,即就是最大访问次数
    int time() default 1;// 限制的时间,即就是限制的时长

    String message() default "访问次数过多,请稍后再试";
}
