package com.example.myself_resume_analyzer.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

@Data
@Schema(description = "登录请求参数")
public class LoginRequestDTO implements Serializable {

    @NotBlank(message = "手机号不能为空")
    @Schema(description = "手机号", example = "13800138000")
    private String phone;

    @NotBlank(message = "密码不能为空")
    @Schema(description = "密码")
    private String password;
}
