package com.example.myself_resume_analyzer.interview.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 面试答案实体（从表）
 * 参考原项目 InterviewAnswerEntity
 * 唯一约束：(session_id, question_index)
 */
@Data
@TableName("interview_answer")
public class Answer {

    /**
     * 自增主键
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 关联会话ID
     */
    private String sessionId;

    /**
     * 题号（从0开始）
     */
    private Integer questionIndex;

    /**
     * 题目内容
     */
    private String question;

    /**
     * 题目类别：基础 / 框架 / 项目 / 算法
     */
    private String category;

    /**
     * 用户回答
     */
    private String userAnswer;

    /**
     * AI 评分（0-100）
     */
    private Integer score;

    /**
     * AI 反馈
     */
    private String feedback;

    /**
     * AI 参考答案
     */
    private String referenceAnswer;

    /**
     * 关键点 JSON 数组
     */
    private String keyPointsJson;

    /**
     * 答题时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime answeredAt;
}
