package com.example.myself_resume_analyzer.exception;

import com.example.myself_resume_analyzer.common.Result;
import com.example.myself_resume_analyzer.constant.ErrorMessages;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public Result handleBusinessException(BusinessException e) {
        return Result.error(e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public Result handleException(Exception e) {
        log.error("服务器内部错误: ", e);
        return Result.error(e.getMessage());
    }
}
