package com.example.myself_resume_analyzer.exception;

import com.example.myself_resume_analyzer.common.Result;
import com.example.myself_resume_analyzer.constant.ErrorMessages;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public Result handleBusinessException(BusinessException e) {
        return Result.error(e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public Result handleException(Exception e) {
        return Result.error(ErrorMessages.SYSTEM_ERROR);
    }
}
