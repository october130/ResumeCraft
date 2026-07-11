package com.example.myself_resume_analyzer.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 简历评估 VO（扁平结构，和数据库字段对应）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResumeEvaluationVO {

    private Long evaluationId;

    private Integer overallScore;      // 总分（0-100）

    private Integer contentScore;      // 内容完整性（0-25）

    private Integer structureScore;    // 结构清晰度（0-20）

    private Integer skillMatchScore;   // 技能匹配度（0-25）

    private Integer expressionScore;   // 表达专业性（0-15）

    private Integer projectScore;      // 项目经验（0-15）

    private String summary;            // 简历摘要

    private List<String> strengths;    // 优点列表

    private List<SuggestionVO> suggestions;  // 改进建议
@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime analyzedAt;  // 分析时间

    /**
     * 改进建议 VO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SuggestionVO {
        private String category;        // 类别（内容/技能/排版等）
        private String priority;        // 优先级（高/中/低）
        private String issue;           // 问题描述
        private String recommendation;  // 改进建议
    }
}
