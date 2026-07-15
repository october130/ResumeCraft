package com.example.myself_resume_analyzer.resume.service;

import com.example.myself_resume_analyzer.common.Result.Result;
import com.example.myself_resume_analyzer.resume.dto.LoginRequestDTO;
import com.example.myself_resume_analyzer.resume.dto.RegisterRequestDTO;
import com.example.myself_resume_analyzer.resume.vo.LoginVO;

public interface UserService {
    Result<LoginVO> register(RegisterRequestDTO registerRequest);

    Result<LoginVO> login(LoginRequestDTO loginRequest);


}
