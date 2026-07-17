package com.example.myself_resume_analyzer.interview.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

/**
 * 创建面试会话请求 DTO
 * 前端选择简历、技能、难度、题数后提交
 */
@Data
@Schema(description = "创建面试会话请求")
public class CreateInterviewRequestDTO implements Serializable {

    @Schema(description = "简历ID（可空，无简历时为通用面试）", example = "5")
    private Long resumeId;

    @Schema(description = "简历文本内容（可空，有简历时作为AI出题依据）")
    private String resumeText;

    @NotBlank(message = "技能方向不能为空")
    @Schema(description = "技能方向", example = "java-backend")
    private String skillId;

    @Schema(description = "难度级别", example = "mid")
    private String difficulty;

    @Min(value = 3, message = "题目数量最少3题")
    @Max(value = 20, message = "题目数量最多20题")
    @Schema(description = "题目数量（3-20）", example = "5")
    private int questionCount;
}
