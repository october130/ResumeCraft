package com.example.myself_resume_analyzer.interview.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.myself_resume_analyzer.interview.entity.Session;
import org.apache.ibatis.annotations.Mapper;

/**
 * 面试会话 Mapper 接口
 */
@Mapper
public interface SessionMapper extends BaseMapper<Session> {
}
