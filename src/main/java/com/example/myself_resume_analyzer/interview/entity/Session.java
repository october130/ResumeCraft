package com.example.myself_resume_analyzer.interview.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 面试会话实体（主表）
 * 参考原项目 InterviewSessionEntity
 */
@Data
@TableName("interview_session")
public class Session {

    /**
     * 自增主键
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 会话UUID（对外标识）
     */
    private String sessionId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 关联简历ID（可为空，支持无简历面试）
     */
    private Long resumeId;

    /**
     * 面试技能方向（如 java-backend、frontend）
     */
    private String skillId;

    /**
     * 难度级别：junior / mid / senior
     */
    private String difficulty;

    /**
     * 题目总数
     */
    private Integer totalQuestions;

    /**
     * 当前题号（从0开始）
     */
    private Integer currentIndex;

    /**
     * 面试状态：CREATED → IN_PROGRESS → COMPLETED → EVALUATED
     */
    private String status;

    /**
     * 题目列表 JSON
     */
    private String questionsJson;

    /**
     * 总分（0-100，评估后填充）
     */
    private Integer overallScore;

    /**
     * 总体评价
     */
    private String overallFeedback;

    /**
     * 优势 JSON 数组
     */
    private String strengthsJson;

    /**
     * 改进建议 JSON 数组
     */
    private String improvementsJson;

    /**
     * 评估状态：PENDING / RUNNING / COMPLETED / FAILED
     */
    private String evaluateStatus;

    /**
     * 评估失败原因
     */
    private String evaluateError;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /**
     * 完成时间
     */
    private LocalDateTime completedAt;
}
