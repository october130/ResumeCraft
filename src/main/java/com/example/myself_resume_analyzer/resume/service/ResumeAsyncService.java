package com.example.myself_resume_analyzer.resume.service;

import org.springframework.scheduling.annotation.Async;

public interface ResumeAsyncService {
    @Async("resumeParseExecutor")
    void resumeParseAsync(Long resumeId, byte[] resumeBytes, String fileName);

    void reanalyzeAsync(Long resumeId, Long userId);
}
