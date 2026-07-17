package com.example.myself_resume_analyzer.interview.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 面试会话 VO
 * 创建面试或获取会话信息时返回，包含完整的题目列表
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SessionVO {

    /**
     * 会话UUID（对外标识）
     */
    private String sessionId;

    /**
     * 题目总数
     */
    private Integer totalQuestions;

    /**
     * 当前题号（从0开始，指向下一道未答的题）
     */
    private Integer currentQuestionIndex;

    /**
     * 题目列表
     */
    private List<QuestionVO> questions;

    /**
     * 面试状态：CREATED / IN_PROGRESS / COMPLETED / EVALUATED
     */
    private String status;
}
