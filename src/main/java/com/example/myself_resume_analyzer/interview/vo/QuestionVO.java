package com.example.myself_resume_analyzer.interview.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 单道面试题目 VO
 * 后端返回给前端渲染一道面试题
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionVO {

    /**
     * 题号（从0开始）
     */
    private Integer questionIndex;//前端显示第几题

    /**
     * 题目内容
     */
    private String question;//显示题目内容

    /**
     * 题目类别：基础 / 框架 / 项目 / 算法
     */
    private String category;//显示题目分类标签
    private String type;//技能分类按键
    private String userAnswer;//用户回答问题后回填显示
    private Integer score;//显示得分
    private  String feedback;//AI点评
    private String referenceAnswer;//AI参考答案，评分后回填

}
