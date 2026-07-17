package com.example.myself_resume_analyzer.interview.controller;

import com.example.myself_resume_analyzer.common.Result.Result;
import com.example.myself_resume_analyzer.common.annotation.RateLimit;
import com.example.myself_resume_analyzer.interview.dto.CreateInterviewRequestDTO;
import com.example.myself_resume_analyzer.interview.dto.SubmitAnswerRequestDTO;
import com.example.myself_resume_analyzer.interview.service.SessionService;
import com.example.myself_resume_analyzer.interview.vo.InterviewDetailVO;
import com.example.myself_resume_analyzer.interview.vo.ListItemVO;
import com.example.myself_resume_analyzer.interview.vo.SessionVO;
import com.example.myself_resume_analyzer.interview.vo.SubmitAnswerResponseVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

/**
 * 面试模块控制器
 * 负责面试会话的创建、问答交互、报告生成等HTTP接口
 */
@RestController
@RequestMapping("/api/interview")
@Slf4j
@Tag(name = "AI面试接口")
public class InterviewController {

    @Resource
   private SessionService sessionService;
    @PostMapping("/sessions")
    @Operation(summary = "创建面试题目")
    @RateLimit(key = "createSession",limit = 5,time = 1)
    public Result<SessionVO> createSession(
            @RequestBody @Valid CreateInterviewRequestDTO request,//valid用来触发前端传入的参数校验
            HttpServletRequest httpServletRequest
    ) throws IOException {
          Long userId =(Long) httpServletRequest.getAttribute("userId");
          log.info("用户:{}创建面试题目",userId);
        Result<SessionVO> session = sessionService.createSession(request,userId);
        return session;

    }

    @PostMapping("/{sessionId}/answers")
    @Operation(summary = "提交面试答案")
    @RateLimit(key = "submitAnswer",limit = 5,time = 1)
    public Result<SubmitAnswerResponseVO> submitAnswer(
            @PathVariable String sessionId,
            @RequestBody @Valid SubmitAnswerRequestDTO submitAnswerRequest) {
        log.info("用户提交面试答案");
        return sessionService.submitAnswer(submitAnswerRequest);
    }
    @GetMapping("/sessions")
    @Operation(summary = "获取面试列表")
    public Result<List<ListItemVO >> listSessions(HttpServletRequest httpServletRequest) {
        Long userId =(Long) httpServletRequest.getAttribute("userId");
        log.info("用户:{}获取面试列表",userId);
        return sessionService.listSessions(userId);
    }
    @GetMapping("/sessions/{sessionId}/details")
    @Operation(summary = "获取面试详情")
    public Result<InterviewDetailVO> getInterviewDetail(
            @PathVariable String sessionId
    ){
        log.info("获取面试详情");
        return sessionService.getInterviewDetail(sessionId);
    }

    @DeleteMapping("/sessions/{sessionId}")
    @Operation(summary = "删除面试记录")
    public Result  deleteSession(
            @PathVariable String sessionId
    ){
        log.info("删除面试记录");
        return sessionService.deleteSession(sessionId);
    }
}
