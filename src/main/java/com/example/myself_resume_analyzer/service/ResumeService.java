package com.example.myself_resume_analyzer.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.myself_resume_analyzer.common.Result;
import com.example.myself_resume_analyzer.vo.ResumeVO;
import org.springframework.web.multipart.MultipartFile;

public interface ResumeService {
    Result<ResumeVO> upload(MultipartFile file,Long userId);

    Result<IPage<ResumeVO>> list(Integer pageNum, Integer pageSize, Long userId);

    Result<String> delete(Long id, Long userId);
}
