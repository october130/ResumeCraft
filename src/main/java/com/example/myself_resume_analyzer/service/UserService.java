package com.example.myself_resume_analyzer.service;

import com.example.myself_resume_analyzer.common.Result;
import com.example.myself_resume_analyzer.dto.LoginRequestDTO;
import com.example.myself_resume_analyzer.dto.RegisterRequestDTO;
import com.example.myself_resume_analyzer.vo.LoginVO;

public interface UserService {
    Result<LoginVO> register(RegisterRequestDTO registerRequest);

    Result<LoginVO> login(LoginRequestDTO loginRequest);


}
