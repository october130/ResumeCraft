package com.example.myself_resume_analyzer.interview.service;

import com.example.myself_resume_analyzer.common.Result.Result;
import com.example.myself_resume_analyzer.interview.dto.CreateInterviewRequestDTO;
import com.example.myself_resume_analyzer.interview.vo.QuestionVO;

import java.io.IOException;
import java.util.List;

/**
 * 面试题目生成服务
 * 负责调用 AI（DashScope）生成面试题目
 */
public interface QuestionService {
    Result<List<QuestionVO>> createQuestion(CreateInterviewRequestDTO request) throws IOException;
}
