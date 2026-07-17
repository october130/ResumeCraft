package com.example.myself_resume_analyzer;

import com.example.myself_resume_analyzer.common.Result.Result;
import com.example.myself_resume_analyzer.resume.dto.RegisterRequestDTO;
import com.example.myself_resume_analyzer.resume.service.UserService;
import com.example.myself_resume_analyzer.resume.vo.LoginVO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.concurrent.TimeUnit;

@SpringBootTest
class MyselfResumeAnalyzerApplicationTests {
@Autowired
private UserService  userService;
@Autowired
private RedisTemplate redisTemplate;
    @Test
    void contextLoads() {
        RegisterRequestDTO registerRequest = new RegisterRequestDTO();
        registerRequest.setUsername("张三封");
        registerRequest.setPassword("12345");
        registerRequest.setPhone("13100138000");
        registerRequest.setEmail("zhangsanf@example.com");
        registerRequest.setCode("1234");
        // 先存一个验证码到 Redis
        redisTemplate.opsForValue().set("sms:code:13100138000", "1234", 5, TimeUnit.MINUTES);
        Result<LoginVO> result = userService.register(registerRequest);
        assert result.getCode() == 200;
        assert result.getData() != null;
    }
}
