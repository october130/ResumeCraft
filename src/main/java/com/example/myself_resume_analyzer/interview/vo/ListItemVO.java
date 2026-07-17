package com.example.myself_resume_analyzer.interview.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 面试记录列表项 VO
 * 面试列表页每一行记录，字段精简不包含题目内容
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ListItemVO {

    /**
     * 会话UUID
     */
    private String sessionId;

    /**
     * 技能方向（如 java-backend、frontend）
     */
    private String skillId;

     /**
     * 简历ID
     */
    private Long resumeId;


    /**
     * 难度级别：junior / mid / senior
     */
    private String difficulty;

    /**
     * 总题数
     */
    private Integer totalQuestions;

    /**
     * 面试状态：CREATED / IN_PROGRESS / COMPLETED / EVALUATED
     */
    private String status;


    /**
     * 评估进度
     */
    private String evaluationStatus;

    /**
     * 总分（0-100，评估后才有值）
     */
    private Integer overallScore;

    /**
     * 评估错误信息
     */
    private String evaluateError;
    /**
     * 完成时间
     */
    private LocalDateTime completedAt;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}
