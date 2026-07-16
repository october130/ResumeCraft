package com.example.myself_resume_analyzer.common.aspect;

import com.example.myself_resume_analyzer.common.annotation.RateLimit;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.concurrent.TimeUnit;

@Aspect
@Component
@Slf4j
public class RateLimitAspect {
    @Resource
    private RedisTemplate<String, Object> redisTemplate;
    @Around("@annotation(rateLimit)")//意思是拦截所有使用了RateLimit注解的方法
    public Object around(ProceedingJoinPoint joinPoint, RateLimit rateLimit) throws Throwable {
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        Long userId = (Long) requestAttributes.getRequest().getAttribute("userId");

        String redisKey = "rateLimit:" + rateLimit.key() + ":" + userId;//缓存的key
        Long count =   redisTemplate.opsForValue().increment(redisKey);//采用Redis的increment方法，原子自增，实现计数
        //其中increment方法原理是：如果key不存在，则设置key的值为1，并返回1；如果key存在，则将key的值+1，并返回+1后的值
        if (count==null||count==1){
          redisTemplate.expire(redisKey,rateLimit.time(), TimeUnit.SECONDS);//设置缓存的过期时间
        }
        if (count>rateLimit.limit()){
            log.info("用户{}访问{}方法太频繁了",userId,joinPoint.getSignature().getName());
             throw  new RuntimeException(rateLimit.message());
        }

        return joinPoint.proceed();//继续执行方法，

    }
}
