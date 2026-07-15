package com.example.myself_resume_analyzer.resume.service;

import com.example.myself_resume_analyzer.resume.vo.ResumeEvaluationVO;

/**
 * 简历评分服务接口
 */
public interface ResumeGradingService {

    /**
     * 分析简历并返回评分和建议
     *
     * @param resumeText 简历文本内容
     * @return 评分结果
     */
    ResumeEvaluationVO analyzeResume(String resumeText);
}
