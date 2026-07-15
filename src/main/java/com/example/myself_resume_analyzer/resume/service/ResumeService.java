package com.example.myself_resume_analyzer.resume.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.myself_resume_analyzer.common.Result.Result;
import com.example.myself_resume_analyzer.resume.vo.ResumeDetailVO;
import com.example.myself_resume_analyzer.resume.vo.ResumeVO;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface ResumeService {
    Result<ResumeVO> upload(MultipartFile file,Long userId) throws IOException;

    Result<IPage<ResumeVO>> list(Integer pageNum, Integer pageSize, Long userId);

    Result<String> delete(Long id, Long userId);

    Result<ResumeDetailVO> getDetail(Long id, Long userId);
    Result<String> reanalyze(Long resumeId, Long userId);

    byte[] exportPdf(Long resumeId, Long userId);
}
