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
