package com.example.myself_resume_analyzer.resume.service.impl;

import com.example.myself_resume_analyzer.resume.entity.ResumeEvaluation;
import com.example.myself_resume_analyzer.resume.entity.ResumeRecord;
import com.example.myself_resume_analyzer.resume.mapper.ResumeEvaluationMapper;
import com.example.myself_resume_analyzer.resume.mapper.ResumeRecordMapper;
import com.example.myself_resume_analyzer.resume.service.FileStorageService;
import com.example.myself_resume_analyzer.resume.service.ResumeAsyncService;
import com.example.myself_resume_analyzer.resume.service.ResumeGradingService;
import com.example.myself_resume_analyzer.resume.service.ResumeParseService;
import com.example.myself_resume_analyzer.resume.vo.ResumeEvaluationVO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ResumeAsyncServiceImpl implements ResumeAsyncService {
    @Resource
     private ResumeParseService resumeParseService;
    @Resource
    private ResumeRecordMapper resumeRecordMapper;
    @Resource
    private ResumeGradingService resumeGradingService;
    @Resource
    private ResumeEvaluationMapper resumeEvaluationMapper;
    @Resource
    private ObjectMapper objectMapper;
    @Resource
    private FileStorageService fileStorageService;
    @Resource
    private RedisTemplate redisTemplate;
    @Async("resumeParseExecutor")
    @Override
    public void resumeParseAsync(Long resumeId, byte[] resumeBytes, String fileName){
        //关于这个异步方法新开一个service，原因就在于是异步，不能写到同类方法中
        log.info(">>> 开始异步解析: resumeId={}, fileName={}, bytes长度={}", resumeId, fileName, resumeBytes.length);
        try {
            log.info(">>> 开始调用 parseResume...");
            String result = resumeParseService.parseResume(resumeBytes, fileName);
            log.info(">>> 文本提取完成: resumeId={}, 文本长度={}", resumeId, result != null ? result.length() : "null");
            log.info("文本提取成功: {}", resumeId);

            ResumeRecord exitRecord = resumeRecordMapper.selectById(resumeId);


            ResumeEvaluationVO evaluationVO = resumeGradingService.analyzeResume(result);//评分
            log.info("评分成功:resumeId = {},score ={}", resumeId, evaluationVO.getOverallScore());

            resumeEvaluationResult(resumeId, exitRecord, evaluationVO);
            //这一步是进行实体类插入数据库，将VO转换成json字符串保存在数据库中
            ResumeRecord record = new ResumeRecord();
            record.setId(resumeId);
            record.setResumeText(result);
            record.setParseStatus(2);
            resumeRecordMapper.updateById(record);//更新状态为 2
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

    @Async("resumeParseExecutor")
    @Override
    public void reanalyzeAsync(Long resumeId, Long userId) {
        log.info(">>> 开始重新分析: resumeId={}, userId={}", resumeId, userId);
        try {
            // 1. 从 OSS 下载文件，重新解析
            ResumeRecord record = resumeRecordMapper.selectById(resumeId);
            byte[] ossFile = fileStorageService.download(record.getFileUrl());
            String parseResume = resumeParseService.parseResume(ossFile, record.getFileName());
            log.info("重新解析文本完成: resumeId={}, 文本长度={}", resumeId, parseResume != null ? parseResume.length() : "null");

            // 2. AI 评分
            ResumeEvaluationVO evaluationVO = resumeGradingService.analyzeResume(parseResume);
            log.info("重新评分完成: resumeId={}, score={}", resumeId, evaluationVO.getOverallScore());

            // 3. 保存评分结果
    resumeEvaluationResult(resumeId, record, evaluationVO);
            // 4. 更新简历状态为完成，保存解析文本
            record.setResumeText(parseResume);
            record.setParseStatus(2);
            resumeRecordMapper.updateById(record);

            // 5. 清除 Redis 缓存
            redisTemplate.delete("resume:detail:" + userId + ":" + resumeId);
            log.info("重新分析成功: resumeId={}", resumeId);

        } catch (Exception e) {
            ResumeRecord record = new ResumeRecord();
            record.setId(resumeId);
            record.setParseStatus(3);
            resumeRecordMapper.updateById(record);
            log.error("重新分析失败: resumeId={}", resumeId, e);
        }
    }

    private void resumeEvaluationResult(Long resumeId, ResumeRecord exitRecord, ResumeEvaluationVO evaluationVO) throws JsonProcessingException {
        ResumeEvaluation evaluation = new ResumeEvaluation();//创建一个ResumeEvaluation对象
        evaluation.setResumeId(exitRecord.getId());
        evaluation.setUserId(exitRecord.getUserId());
        evaluation.setOverallScore(evaluationVO.getOverallScore());
        evaluation.setContentScore(evaluationVO.getContentScore());
        evaluation.setStructureScore(evaluationVO.getStructureScore());
        evaluation.setSkillMatchScore(evaluationVO.getSkillMatchScore());
        evaluation.setExpressionScore(evaluationVO.getExpressionScore());
        evaluation.setProjectScore(evaluationVO.getProjectScore());
        evaluation.setSummary(evaluationVO.getSummary());
        evaluation.setSuggestionsJson(objectMapper.writeValueAsString(evaluationVO.getSuggestions()));
        evaluation.setStrengthsJson(objectMapper.writeValueAsString(evaluationVO.getStrengths()));
        evaluation.setAnalyzedAt(evaluationVO.getAnalyzedAt());
        resumeEvaluationMapper.insert(evaluation);
        log.info("保存成功,评分结果已经由VO封装成实体类并插入数据库中: {}", resumeId);
    }

}
