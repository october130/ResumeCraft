package com.example.myself_resume_analyzer.interview.service;

import com.example.myself_resume_analyzer.common.Result.Result;
import com.example.myself_resume_analyzer.interview.dto.CreateInterviewRequestDTO;
import com.example.myself_resume_analyzer.interview.dto.SubmitAnswerRequestDTO;
import com.example.myself_resume_analyzer.interview.vo.InterviewDetailVO;
import com.example.myself_resume_analyzer.interview.vo.ListItemVO;
import com.example.myself_resume_analyzer.interview.vo.SessionVO;
import com.example.myself_resume_analyzer.interview.vo.SubmitAnswerResponseVO;

import java.util.List;

/**
 * 面试会话生命周期管理
 * 负责会话的创建、查询、状态流转
 */
public interface SessionService {
    Result<SessionVO> createSession(CreateInterviewRequestDTO request, Long userId);

    Result<SubmitAnswerResponseVO> submitAnswer(SubmitAnswerRequestDTO submitAnswerRequest);

    Result<List<ListItemVO>> listSessions(Long userId);

    Result<InterviewDetailVO> getInterviewDetail(String sessionId);

    Result deleteSession(String sessionId);
}
