package com.example.myself_resume_analyzer.interview.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/**
 * 提交答案请求 DTO
 * 前端用户作答后提交
 */
@Data
@Schema(description = "提交答案请求")
public class SubmitAnswerRequestDTO implements Serializable {

    @NotBlank(message = "会话ID不能为空")
    @Schema(description = "会话UUID", example = "a1b2c3d4e5f6")
    private String sessionId;

    @NotNull(message = "题号不能为空")
    @Min(value = 0, message = "题号无效")
    @Schema(description = "题号（从0开始）", example = "0")
    private Integer questionIndex;

    @NotBlank(message = "答案不能为空")
    @Schema(description = "用户回答内容", example = "HashMap底层基于数组+链表+红黑树...")
    private String answer;
}
