package com.example.myself_resume_analyzer.service;

public interface ResumeParseService {


    String parseResume(byte[] resumeBytes, String fileName);
}
