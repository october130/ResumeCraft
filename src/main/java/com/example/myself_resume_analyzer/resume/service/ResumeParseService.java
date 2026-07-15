package com.example.myself_resume_analyzer.resume.service;

public interface ResumeParseService {


    String parseResume(byte[] resumeBytes, String fileName);
}
