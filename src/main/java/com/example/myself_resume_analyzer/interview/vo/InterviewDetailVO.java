package com.example.myself_resume_analyzer.interview.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 面试详情 VO
 * 用于详情页展示：面试基本信息 + 逐题答题记录
 * 对应原项目 InterviewDetailDTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InterviewDetailVO {

    /** 会话UUID */
    private String sessionId;

    /** 技能方向（如 java-backend） */
    private String skillId;

    /** 难度级别：junior / mid / senior */
    private String difficulty;

    /** 总题数 */
    private Integer totalQuestions;

    /** 面试状态：CREATED / IN_PROGRESS / COMPLETED / EVALUATED */
    private String status;

    /** 评估状态：PENDING / RUNNING / COMPLETED / FAILED */
    private String evaluateStatus;

    /** 总分（0-100），评估完成后才有值 */
    private Integer overallScore;

    /** AI 总体评价 */
    private String overallFeedback;

    /** 优势列表，由 AI 生成 */
    private List<String> strengths;

    /** 改进建议列表，由 AI 生成 */
    private List<String> improvements;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    /** 完成时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime completedAt;

    /** 逐题答题详情（含评分和反馈） */
    private List<QuestionVO> answers;
}
