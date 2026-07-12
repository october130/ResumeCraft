package com.example.myself_resume_analyzer.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 简历评估表（参考原项目 ResumeAnalysisEntity）
 * 存储评分和改进建议
 */
@Data
@TableName("resume_evaluation")
public class ResumeEvaluation {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 关联简历ID
     */
    private Long resumeId;

    /**
     * 用户ID
     */
    private Long userId;

    // ===== 评分字段（参考原项目5维度）=====

    /**
     * 总分（0-100）
     */
    private Integer overallScore;

    /**
     * 内容完整性（0-25）
     */
    private Integer contentScore;

    /**
     * 结构清晰度（0-20）
     */
    private Integer structureScore;

    /**
     * 技能匹配度（0-25）
     */
    private Integer skillMatchScore;

    /**
     * 表达专业性（0-15）
     */
    private Integer expressionScore;

    /**
     * 项目经验（0-15）
     */
    private Integer projectScore;

    // ===== 摘要和建议 =====

    /**
     * 简历摘要
     */
    private String summary;

    /**
     * 优点列表（JSON数组字符串）
     */
    private String strengthsJson;

    /**
     * 改进建议（JSON数组字符串）
     */
    private String suggestionsJson;

    // ===== 时间字段 =====

    /**
     * 分析时间
     */
    private LocalDateTime analyzedAt;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
