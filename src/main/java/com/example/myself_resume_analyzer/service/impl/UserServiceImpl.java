package com.example.myself_resume_analyzer.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.myself_resume_analyzer.common.Result;
import com.example.myself_resume_analyzer.common.ResultCode;
import com.example.myself_resume_analyzer.dto.LoginRequestDTO;
import com.example.myself_resume_analyzer.dto.RegisterRequestDTO;
import com.example.myself_resume_analyzer.entity.User;
import com.example.myself_resume_analyzer.mapper.UserMapper;
import com.example.myself_resume_analyzer.service.UserService;
import com.example.myself_resume_analyzer.utils.JwTUtils;
import com.example.myself_resume_analyzer.vo.LoginVO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class UserServiceImpl implements UserService {
    @Resource
    private UserMapper userMapper;
    @Resource
    private RedisTemplate redisTemplate;
    @Resource
    private JwTUtils jwTUtils;

    BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @Override
    public Result<LoginVO> register(RegisterRequestDTO registerRequest) {
        if (registerRequest == null) {
            return Result.error(ResultCode.PARAM_ERROR);
        }
        QueryWrapper<User> query = new QueryWrapper<>();
        query.eq("phone", registerRequest.getPhone());
        User user = userMapper.selectOne(query);
        if (user != null) {
            return Result.error(ResultCode.PHONE_EXIST);
        }
        String  code = (String) redisTemplate.opsForValue().get("sms:code:" + registerRequest.getPhone());//获取redis中的验证码
        log.info("获取的验证码为：{}",code);
        if (code== null|| !code.equals(registerRequest.getCode())){
            return Result.error(ResultCode.RANDOM_NOT_EXIST);//检验验证码存在性
        }
log.info("用户密码明文:{}",  registerRequest.getPassword());
        String password = encoder.encode(registerRequest.getPassword());//这个是密码加密后的密码，将密码加密后存储到数据库中
        user = new User();//创建一个用户对象
        user.setUsername(registerRequest.getUsername());
        user.setPassword(password);
        user.setPhone(registerRequest.getPhone());
        user.setEmail(registerRequest.getEmail());
        userMapper.insert(user);//将封装好的用户对象插入到数据库中
        log.info("用户注册成功");
        return Result.success(LoginVO.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .build());
    }

    @Override
    public Result<LoginVO> login(LoginRequestDTO loginRequest) {
        QueryWrapper<User> query = new QueryWrapper<>();
        query.eq("phone", loginRequest.getPhone());
        User user = userMapper.selectOne(query);
        if (user == null) {
            return Result.error(ResultCode.USER_NOT_EXIST);
        }
        boolean matches = encoder.matches(loginRequest.getPassword(), user.getPassword());//匹配一下用户密码信息和输入的密码
        log.info("数据库密码密文: {}", user.getPassword());
        if (!matches) {
            return Result.error(ResultCode.PASSWORD_ERROR);
        }
        String token = jwTUtils.generateToken(user.getId(), user.getUsername());//生成token
        log.info("用户登录成功");

        // userId作为key，token作为value
        redisTemplate.opsForValue().set(
                "user:token:" + user.getId(),
                token,
                24,
                TimeUnit.HOURS
        );
log.info("已经成功缓存到Redis里面了");
        return Result.success(LoginVO.builder()
                .token(token)
                .userId(user.getId())
                .username(user.getUsername())
                .build());
    }
}
