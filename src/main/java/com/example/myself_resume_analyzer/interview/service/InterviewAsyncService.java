package com.example.myself_resume_analyzer.interview.service;

import org.springframework.scheduling.annotation.Async;

public interface InterviewAsyncService {
    @Async("interviewExecutor")
    void evaluateAsync(String sessionId);
}
