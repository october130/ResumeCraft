package com.example.myself_resume_analyzer.resume.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.io.Serializable;

@Data
@Schema(description = "注册请求参数")
public class RegisterRequestDTO implements Serializable {

    @NotBlank(message = "用户名不能为空")
    @Schema(description = "用户名", example = "张三")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Schema(description = "密码")
    private String password;

    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    @Schema(description = "手机号", example = "13800138000")
    private String phone;

    @Schema(description = "邮箱（选填）", example = "zhangsan@example.com")
    private String email;

    @NotBlank(message = "验证码不能为空")
    @Schema(description = "短信验证码", example = "1234")
    private String code;

}
