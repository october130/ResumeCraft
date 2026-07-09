package com.example.myself_resume_analyzer.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("resume_record")
public class ResumeRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String fileUrl;

    private String fileName;

    // 新增字段
    private Long fileSize;      // 文件大小（字节）

    private String fileType;    // 文件类型（pdf/docx）

    private Integer parseStatus;  // 解析状态（0-待解析，1-解析中，2-已完成，3-解析失败）

    private String resumeText;

    private Integer scoreContent;

    private Integer scoreMatch;

    private Integer scoreSkill;

    private Integer scoreLayout;

    private Integer scoreHighlight;

    private String suggestion;

    private String targetJd;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
