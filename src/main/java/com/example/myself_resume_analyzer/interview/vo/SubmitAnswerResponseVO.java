package com.example.myself_resume_analyzer.interview.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubmitAnswerResponseVO {
    /*
    判断是否有下一题
     */
    private boolean hasNextQuestion;

    /*
   下一题内容
     */
    private  QuestionVO nextQuestion;

    private Integer currentIndex;
    private Integer totalQuestions;

}
