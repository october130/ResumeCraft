package com.example.myself_resume_analyzer.controller;

import com.example.myself_resume_analyzer.common.Result;
import com.example.myself_resume_analyzer.dto.LoginRequestDTO;
import com.example.myself_resume_analyzer.dto.RegisterRequestDTO;
import com.example.myself_resume_analyzer.service.UserService;
import com.example.myself_resume_analyzer.utils.JwTUtils;
import com.example.myself_resume_analyzer.vo.LoginVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.Random;
import java.util.concurrent.TimeUnit;

@RestController
@Slf4j
@RequestMapping("/api/user")
@Tag(name = "用户模块")
public class UserController {
    @Resource
    private UserService userService;
    @Resource
    private RedisTemplate redisTemplate;
    @Resource
    private JwTUtils jwTUtils;

    @PostMapping("/register")
    @Operation(summary = "用户注册")
    public Result<LoginVO> register(@RequestBody RegisterRequestDTO registerRequest) {
        return userService.register(registerRequest);
    }

    @PostMapping("/login")
    @Operation(summary = "用户登录")
    public Result<LoginVO> login(@RequestBody LoginRequestDTO loginRequest) {
        return userService.login(loginRequest);
    }

    @PostMapping("/logout")
    @Operation(summary = "用户登出")
    public Result<String> logout(HttpServletRequest request) {
        String authorization = request.getHeader("Authorization");
        String token = authorization.substring(7);

        // 从 token 解析 userId，删除 Redis 中的 token
        Long userId = jwTUtils.getUserIdFromToken(token);
        redisTemplate.delete("user:token:" + userId);

        return Result.success("登出成功", null);
    }
    @PostMapping("sms/send")
    @Operation(summary = "发送短信验证码")
    public  Result<String> sendSms(@RequestParam String phone) {
        Random random = new Random();
        String code = String.format("%04d", random.nextInt(10000));
        redisTemplate.opsForValue().set(
                "sms:code:" + phone,
                code,
                5,
                TimeUnit.MINUTES
        );
        log.info("短信验证码发送成功：{}",code );
        return Result.success("验证码发送成功", null);
    }
}
