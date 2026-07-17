package com.example.myself_resume_analyzer.interview.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.myself_resume_analyzer.interview.entity.Answer;
import org.apache.ibatis.annotations.Mapper;

/**
 * 面试答案 Mapper 接口
 */
@Mapper
public interface AnswerMapper extends BaseMapper<Answer> {
}
