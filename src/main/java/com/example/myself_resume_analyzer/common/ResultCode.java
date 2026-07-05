package com.example.myself_resume_analyzer.common;

import lombok.Getter;

@Getter
public enum ResultCode {

    SUCCESS(200, "成功"),
    PARAM_ERROR(400, "参数错误"),
    UNAUTHORIZED(401, "未登录或token已过期"),
    FORBIDDEN(403, "无权限"),
    NOT_FOUND(404, "资源不存在"),
    PHONE_EXIST(405, "手机号已存在"),
    PASSWORD_ERROR(406, "手机号或者密码错误"),
    USER_NOT_EXIST(407, "用户不存在"),
    RANDOM_NOT_EXIST(408, "手机验证码输入错误"),
    SERVER_ERROR(500, "服务器内部错误");



    private final int code;
    private final String message;

    ResultCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
