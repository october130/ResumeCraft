package com.example.myself_resume_analyzer.service.impl;

import com.example.myself_resume_analyzer.entity.ResumeRecord;
import com.example.myself_resume_analyzer.mapper.ResumeRecordMapper;
import com.example.myself_resume_analyzer.service.ResumeAsyncService;
import com.example.myself_resume_analyzer.service.ResumeParseService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ResumeAsyncServiceImpl implements ResumeAsyncService {
    @Resource
     private ResumeParseService resumeParseService;
    @Resource
    private ResumeRecordMapper resumeRecordMapper;
    @Async("resumeParseExecutor")
    @Override
    public void resumeParseAsync(Long resumeId, byte[] resumeBytes, String fileName){
        //关于这个异步方法新开一个service，原因就在于是异步，不能写到同类方法中
        try {
            String result = resumeParseService.parseResume(resumeBytes, fileName);
            ResumeRecord record = new ResumeRecord();
            record.setId(resumeId);
            record.setResumeText("解析已完成");
            record.setParseResult(result);
            record.setParseStatus(2);
            resumeRecordMapper.updateById(record);
            log.info("解析成功: {}", resumeId);
        } catch (Exception e) {
            // 解析失败，更新状态为 3
            ResumeRecord record = new ResumeRecord();
            record.setId(resumeId);
            record.setParseStatus(3);  // 3 = 失败
            resumeRecordMapper.updateById(record);

            log.error("简历解析失败: resumeId={}", resumeId, e);
        }
    }
}
