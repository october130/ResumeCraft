package com.example.myself_resume_analyzer.resume.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 简历详情 VO（嵌套结构，参考原项目 ResumeDetailDTO）
 * 包含简历基础信息 + 评分历史
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResumeDetailVO {

    /**
     * 简历基础信息（复用 ResumeVO）
     */
    private ResumeVO resumeInfo;

    /**
     * 评分历史列表（来自 resume_evaluation 表，1:N）
     */
    private List<ResumeEvaluationVO> evaluations;
}

