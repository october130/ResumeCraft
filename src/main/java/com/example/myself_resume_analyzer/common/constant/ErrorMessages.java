package com.example.myself_resume_analyzer.common.constant;

/**
 * 错误信息常量类
 * 统一管理业务异常的错误信息
 */
public class ErrorMessages {

    // 用户相关
    public static final String USER_NOT_EXIST = "用户不存在";
    public static final String PHONE_EXIST = "手机号已存在";
    public static final String PASSWORD_ERROR = "手机号或密码错误";
    public static final String USERNAME_EXIST = "用户名已存在";

    // 简历相关
    public static final String RESUME_NOT_EXIST = "简历不存在";
    public static final String ANALYSIS_FAILED = "分析失败，请重试";

    // 系统相关
    public static final String SYSTEM_ERROR = "服务器内部错误";
    public static final String PARAM_ERROR = "参数错误";
}
