package com.example.myself_resume_analyzer.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResumeVO {

    private Long resumeId;           // 简历 ID

    private String fileName;         // 原始文件名

    private String fileUrl;          // OSS 文件 URL

    private Long fileSize;           // 文件大小（字节）

    private String fileType;         // 文件类型（pdf/docx）

    private Integer parseStatus;     // 解析状态（0-待解析 1-已完成 2-失败）

    private LocalDateTime uploadTime; // 上传时间

    private  String resumeText;//提取的简历文本
    private String parseResult;//AI简历分析后的结果
}
