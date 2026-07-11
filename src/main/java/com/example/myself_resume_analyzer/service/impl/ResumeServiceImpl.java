package com.example.myself_resume_analyzer.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.myself_resume_analyzer.common.Result;
import com.example.myself_resume_analyzer.constant.ErrorMessages;
import com.example.myself_resume_analyzer.entity.ResumeEvaluation;
import com.example.myself_resume_analyzer.entity.ResumeRecord;
import com.example.myself_resume_analyzer.mapper.ResumeEvaluationMapper;
import com.example.myself_resume_analyzer.mapper.ResumeRecordMapper;
import com.example.myself_resume_analyzer.service.FileStorageService;
import com.example.myself_resume_analyzer.service.ResumeAsyncService;
import com.example.myself_resume_analyzer.service.ResumeService;
import com.example.myself_resume_analyzer.utils.Md5Utils;
import com.example.myself_resume_analyzer.vo.ResumeDetailVO;
import com.example.myself_resume_analyzer.vo.ResumeEvaluationVO;
import com.example.myself_resume_analyzer.vo.ResumeVO;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class ResumeServiceImpl implements ResumeService {
    @Resource
   private FileStorageService fileService;
    @Resource
    private ResumeRecordMapper resumeRecordMapper;
    @Resource
    private ResumeAsyncService resumeParseAsyncService;
    @Resource
    private RedisTemplate redisTemplate;
    @Resource
    private ResumeEvaluationMapper resumeEvaluationMapper;
    @Resource
    private ObjectMapper objectMapper;
    @Override
    public Result<ResumeVO> upload(MultipartFile file, Long userId) throws IOException {
        // 1. 校验文件
        if (file.isEmpty()) {
            return Result.error("文件不能为空");
        }
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename.substring(originalFilename.lastIndexOf("."));//获取文件后缀
        // 校验类型
        if (!extension.equalsIgnoreCase(".pdf") &&
                !extension.equalsIgnoreCase(".docx")) {
            return Result.error("只支持 PDF 和 Word 文件");
        }
        // 校验大小（10MB）
        if (file.getSize() > 10 * 1024 * 1024) {
            return Result.error("文件大小不能超过 10MB");
        }
        String fileMd5 = Md5Utils.calculateMd5( file.getBytes());
        log.info("文件md5去重检验: {}", fileMd5);
        QueryWrapper<ResumeRecord> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId).eq("file_md5", fileMd5);
        ResumeRecord recordcurrent = resumeRecordMapper.selectOne(wrapper);//查询当前用户是否已经上传过此简历
         if ( recordcurrent != null){
             log.info("用户已经上传过此简历:{}", fileMd5);
             return Result.success("已经上传过此简历", ResumeVO.builder()
                     .resumeId(recordcurrent.getId())
                     .fileName(recordcurrent.getFileName())
                     .fileUrl(recordcurrent.getFileUrl())
                     .fileSize(recordcurrent.getFileSize())
                     .fileType(recordcurrent.getFileType())
                     .parseStatus(recordcurrent.getParseStatus())
                     .uploadTime(recordcurrent.getCreateTime())
                     .build());
         }


        log.info("开始上传文件: {}", originalFilename);
        String fileUrl= fileService.upload(file, "resume");//引用FileService里面关于文件上传OSS

        // 3. 创建简历记录，存数据库
        ResumeRecord record = new ResumeRecord();
        record.setUserId(userId);
        record.setFileMd5(fileMd5);
        record.setFileUrl(fileUrl);
        record.setFileName(originalFilename);
        record.setFileSize(file.getSize());
        record.setFileType(extension.substring(1).toLowerCase());  // "pdf" 或 "docx"
        record.setParseStatus(1);  // 解析中
log.info("文件没有问题,将文件插入到数据库里");
        resumeRecordMapper.insert(record);

        // 4. 返回 ResumeVO
        ResumeVO vo = new ResumeVO();
        vo.setResumeId(record.getId());
        vo.setFileName(record.getFileName());
        vo.setFileUrl(record.getFileUrl());
        vo.setFileSize(record.getFileSize());

        vo.setParseStatus(record.getParseStatus());
log.info("数据库里已经有用户的信息了，包括OSS");
       resumeParseAsyncService.resumeParseAsync(
                record.getId(),
                file.getBytes(),
                originalFilename
        );//触发异步解析，在简历上传后开始进行简历解析

        return Result.success("上传成功", vo);
    }

    @Override
    public Result<IPage<ResumeVO>> list(Integer pageNum, Integer pageSize, Long userId) {
        Page<ResumeRecord> page = new Page<>(pageNum, pageSize);
        QueryWrapper< ResumeRecord> wrapper = new QueryWrapper<>();
        //查询当前的用户简历，查的是resume_record表
        wrapper.eq("user_id", userId);
        wrapper.orderByDesc(true, "create_time");//根据创建时间来进行降序排序
        IPage<ResumeRecord> recordPage = resumeRecordMapper.selectPage(page, wrapper);
        IPage<ResumeVO> voPage = recordPage.convert(record -> ResumeVO.builder()//这里convert就是转换，将ResumeRecord转换成ResumeVO
                .resumeId(record.getId())
                .fileName(record.getFileName())
                .fileUrl(record.getFileUrl())
                .fileSize(record.getFileSize())
                .fileType(record.getFileType())
                .parseStatus(record.getParseStatus())
                .uploadTime(record.getCreateTime())
                .latestScore(getLatestScore(record.getId()))
                .build());//将分页查询得来的数据通过封装后的ResumeVO类进行返回

                return Result.success(voPage);
    }
    private Integer getLatestScore(Long resumeId) {//获取最新评分
        QueryWrapper<ResumeEvaluation> wrapper = new QueryWrapper<>();
        wrapper.eq("resume_id", resumeId).orderByDesc("analyzed_at").last("LIMIT 1");
        ResumeEvaluation eval = resumeEvaluationMapper.selectOne(wrapper);
        return eval != null ? eval.getOverallScore() : null;
    }


    @Override
    public Result<String> delete(Long id, Long userId) {
        log.info("删除请求: resumeId={}, userId={}", id, userId);
        ResumeRecord record = resumeRecordMapper.selectById(id);
        if (record == null) {
            log.info("简历不存在: id={}", id);
            return Result.error(ErrorMessages.RESUME_NOT_EXIST);
        }
        log.info("查到的简历: recordUserId={}, 当前userId={}", record.getUserId(), userId);
        if (!record.getUserId().equals(userId)) {
            log.info("无权删除");
            return Result.error("无权删除");
        }
        fileService.delete(record.getFileUrl());
        resumeRecordMapper.deleteById(id);
        log.info("删除成功: {}", id);
        String key = "resume:detail:" + userId + ":" + id;
        redisTemplate.delete(key);
        log.info("删除缓存: {}", key);
        return Result.success("删除成功");
    }

    @Override
    public Result<ResumeDetailVO> getDetail(Long id, Long userId) {
        String key = "resume:detail:" + userId + ":" + id;
        ResumeDetailVO cached = (ResumeDetailVO) redisTemplate.opsForValue().get(key);
        if (cached != null) {
            log.info("缓存命中: {}", key);
            return Result.success(cached);
        }

        // 1. 查询简历基础信息
        ResumeRecord record = resumeRecordMapper.selectById(id);
        if (record == null) {
            return Result.error(ErrorMessages.RESUME_NOT_EXIST);
        }
        if (!record.getUserId().equals(userId)) {
            return Result.error("无权查看");
        }

        // 2. 查询评分列表
        QueryWrapper<ResumeEvaluation> evalWrapper = new QueryWrapper<>();
        evalWrapper.eq("resume_id", id).orderByDesc("analyzed_at");
        List<ResumeEvaluation> evaluations = resumeEvaluationMapper.selectList(evalWrapper);

        // 3. 转换为 ResumeVO
        ResumeVO resumeVO = ResumeVO.builder()
                .resumeId(record.getId())
                .fileName(record.getFileName())
                .fileUrl(record.getFileUrl())
                .fileSize(record.getFileSize())
                .fileType(record.getFileType())
                .parseStatus(record.getParseStatus())
                .uploadTime(record.getCreateTime())
                .resumeText(record.getResumeText())
                .build();

        // 4. 转换评分列表为 VO（扁平结构，直接映射）
        List<ResumeEvaluationVO> evalVOList = evaluations.stream()
                .map(e -> ResumeEvaluationVO.builder()
                        .evaluationId(e.getId())
                        .overallScore(e.getOverallScore())
                        .contentScore(e.getContentScore())
                        .structureScore(e.getStructureScore())
                        .skillMatchScore(e.getSkillMatchScore())
                        .expressionScore(e.getExpressionScore())
                        .projectScore(e.getProjectScore())
                        .summary(e.getSummary())
                        .strengths(parseJsonToList(e.getStrengthsJson()))
                        .suggestions(parseJsonToSuggestionList(e.getSuggestionsJson()))
                        .analyzedAt(e.getAnalyzedAt())
                        .build())
                .toList();

        // 5. 组合返回
        ResumeDetailVO detailVO = ResumeDetailVO.builder()
                .resumeInfo(resumeVO)
                .evaluations(evalVOList)
                .build();

        redisTemplate.opsForValue().set(key, detailVO, 24, TimeUnit.HOURS);
        log.info("缓存已存入: {}", key);

        return Result.success(detailVO);
    }

    // 解析 JSON 为 List<String>
    private List<String> parseJsonToList(String json) {
        if (json == null || json.isEmpty()) return List.of();
        try {
            return objectMapper.readValue(json, objectMapper.getTypeFactory()
                    .constructCollectionType(List.class, String.class));
        } catch (Exception e) {
            log.error("解析 JSON 失败: {}", json, e);
            return List.of();
        }
    }

    // 解析 JSON 为 List<SuggestionVO>
    private List<ResumeEvaluationVO.SuggestionVO> parseJsonToSuggestionList(String json) {
        if (json == null || json.isEmpty()) return List.of();
        try {
            return objectMapper.readValue(json, objectMapper.getTypeFactory()
                    .constructCollectionType(List.class, ResumeEvaluationVO.SuggestionVO.class));
        } catch (Exception e) {
            log.error("解析 JSON 失败: {}", json, e);
            return List.of();
        }
    }


}

