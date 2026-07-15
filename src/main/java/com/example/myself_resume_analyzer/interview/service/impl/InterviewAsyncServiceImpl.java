package com.example.myself_resume_analyzer.interview.service.impl;

import com.alibaba.dashscope.aigc.generation.Generation;
import com.alibaba.dashscope.aigc.generation.GenerationParam;
import com.alibaba.dashscope.aigc.generation.GenerationResult;
import com.alibaba.dashscope.common.Message;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.myself_resume_analyzer.common.config.DashScopeConfig;
import com.example.myself_resume_analyzer.interview.entity.Answer;
import com.example.myself_resume_analyzer.interview.entity.Session;
import com.example.myself_resume_analyzer.interview.mapper.AnswerMapper;
import com.example.myself_resume_analyzer.interview.mapper.SessionMapper;
import com.example.myself_resume_analyzer.interview.service.InterviewAsyncService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Slf4j
@Service
public class InterviewAsyncServiceImpl implements InterviewAsyncService {

    @Resource
    private SessionMapper interviewSessionMapper;
    @Resource
    private AnswerMapper answerMapper;
    @Resource
    private DashScopeConfig dashScopeConfig;
    @Resource
    private ObjectMapper objectMapper;

    @Override
    public void evaluateAsync(String sessionId) {
        log.info("开始异步评估面试, sessionId={}", sessionId);

        // 1. 查 session
        Session session = interviewSessionMapper.selectOne(
                new QueryWrapper<Session>().eq("session_id", sessionId)
        );
        if (session == null) {
            log.warn("session不存在, sessionId={}", sessionId);
            return;
        }

        // 标记评估中
        session.setEvaluateStatus("RUNNING");
        interviewSessionMapper.updateById(session);

        try {
            // 2. 查所有 answer
            List<Answer> answerList = answerMapper.selectList(
                    new QueryWrapper<Answer>()
                            .eq("session_id", sessionId)
                            .orderByAsc("question_index")
            );

            // 3. 拼接题目+回答文本
            StringBuilder sb = new StringBuilder();
            for (Answer answer : answerList) {
                sb.append("## 第").append(answer.getQuestionIndex() + 1).append("题：")
                  .append(answer.getQuestion()).append("\n");
                sb.append("候选人回答：").append(answer.getUserAnswer()).append("\n\n");
            }

            // 4. 读 prompt，替换占位符
            String systemPrompt = new String(
                    getClass().getResourceAsStream("/prompts/interview_evaluation_system.txt").readAllBytes(),
                    StandardCharsets.UTF_8
            );
            String userPrompt = new String(
                    getClass().getResourceAsStream("/prompts/interview_evaluation_user.txt").readAllBytes(),
                    StandardCharsets.UTF_8
            );
            userPrompt = userPrompt.replace("{skillId}", session.getSkillId())
                    .replace("{difficulty}", session.getDifficulty())
                    .replace("{questionsWithAnswers}", sb.toString());

            // 5. 调 AI
            Message systemMsg = Message.builder().role("system").content(systemPrompt).build();
            Message userMsg = Message.builder().role("user").content(userPrompt).build();

            Generation gen = new Generation();
            GenerationResult result = gen.call(
                    GenerationParam.builder()
                            .apiKey(dashScopeConfig.getApiKey())
                            .model("qwen-plus")
                            .messages(List.of(systemMsg, userMsg))
                            .resultFormat(GenerationParam.ResultFormat.MESSAGE)
                            .build()
            );
            String aiAnswer = result.getOutput().getChoices().get(0).getMessage().getContent();

            if (aiAnswer == null) {
                throw new RuntimeException("AI评估返回为空");
            }

            // 6. 解析 JSON，清理 markdown 代码块
            aiAnswer = aiAnswer.trim();
            if (aiAnswer.startsWith("```json")) { aiAnswer = aiAnswer.substring(7); }
            if (aiAnswer.startsWith("```")) { aiAnswer = aiAnswer.substring(3); }
            if (aiAnswer.endsWith("```")) { aiAnswer = aiAnswer.substring(0, aiAnswer.length() - 3); }
            aiAnswer = aiAnswer.trim();

            JsonNode root = objectMapper.readTree(aiAnswer);

            // 7. 更新每道题的评分
            JsonNode scoresNode = root.get("scores");
            for (int i = 0; i < scoresNode.size(); i++) {
                JsonNode scoreNode = scoresNode.get(i);
                Answer answer = answerList.get(i);
                answer.setScore(scoreNode.get("score").asInt());
                answer.setFeedback(scoreNode.get("feedback").asText());
                answer.setReferenceAnswer(scoreNode.get("referenceAnswer").asText());
                answerMapper.updateById(answer);
            }

            // 8. 更新 session 总评
            session.setOverallScore(root.get("overallScore").asInt());
            session.setOverallFeedback(root.get("overallFeedback").asText());
            session.setStrengthsJson(objectMapper.writeValueAsString(root.get("strengths")));
            session.setImprovementsJson(objectMapper.writeValueAsString(root.get("improvements")));
            session.setStatus("EVALUATED");
            session.setEvaluateStatus("COMPLETED");
            interviewSessionMapper.updateById(session);

            log.info("面试评估完成, sessionId={}, overallScore={}", sessionId, session.getOverallScore());

        } catch (Exception e) {
            log.error("面试评估失败, sessionId={}", sessionId, e);
            session.setEvaluateStatus("FAILED");
            session.setEvaluateError(e.getMessage());
            interviewSessionMapper.updateById(session);
        }
    }
}
