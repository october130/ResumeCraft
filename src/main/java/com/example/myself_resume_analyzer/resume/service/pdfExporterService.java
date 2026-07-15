package com.example.myself_resume_analyzer.resume.service;

import com.example.myself_resume_analyzer.resume.entity.ResumeEvaluation;
import com.example.myself_resume_analyzer.resume.entity.ResumeRecord;

public interface pdfExporterService {
    byte[] exportPdf(Long resumeId, ResumeRecord record, ResumeEvaluation evaluation);
}
