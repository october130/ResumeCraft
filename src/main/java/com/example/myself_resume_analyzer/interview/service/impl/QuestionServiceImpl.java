package com.example.myself_resume_analyzer.interview.service.impl;

import com.example.myself_resume_analyzer.common.Result.Result;
import com.example.myself_resume_analyzer.common.service.KnowledgeBaseService;
import com.example.myself_resume_analyzer.interview.dto.CreateInterviewRequestDTO;
import com.example.myself_resume_analyzer.interview.service.QuestionService;
import com.example.myself_resume_analyzer.interview.vo.QuestionVO;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * 面试题目生成服务实现（Spring AI + RAG 版本）
 */
@Service
@Slf4j
public class QuestionServiceImpl implements QuestionService {

    @Resource
    private ChatModel chatModel;

    @Resource
    private ObjectMapper objectMapper;

    @Resource
    private KnowledgeBaseService knowledgeBaseService;

    @Override
    public Result<List<QuestionVO>> createQuestion(CreateInterviewRequestDTO request) throws IOException {
        log.info("用户点击了创建面试题目,开始创建面试题目");

        try {
            String json = aiGenerateQuestion(request);
            log.info("AI出题调用成功");
            List<QuestionVO> list = parseAIQuestion(json);
            log.info("用户点击了创建面试题目,创建面试题目成功，返回VO数据成功");
            return Result.success(list);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String aiGenerateQuestion(CreateInterviewRequestDTO request) throws IOException {
        // 1. RAG 检索：根据技能方向和难度检索面试题题库
        List<Document> questionDocs = knowledgeBaseService.searchQuestionsBySkillAndDifficulty(
                request.getSkillId(), request.getDifficulty(), 3);
        StringBuilder questionsContext = new StringBuilder();
        for (Document doc : questionDocs) {
            questionsContext.append(doc.getText()).append("\n\n");
        }
        log.info("RAG 检索到 {} 个面试题文档", questionDocs.size());

        // 2. 读取 system prompt
        String systemPrompt = new String(
                getClass().getResourceAsStream("/prompts/interview_question_system.txt").readAllBytes(),
                StandardCharsets.UTF_8
        );

        // 3. 读取 user prompt 模板，替换占位符
        String userPromptTemplate = new String(
                getClass().getResourceAsStream("/prompts/interview_question_user.txt").readAllBytes(),
                StandardCharsets.UTF_8
        );
        String userPrompt = userPromptTemplate.replace("{skillId}", request.getSkillId())
                .replace("{difficulty}", request.getDifficulty())
                .replace("{questionCount}", String.valueOf(request.getQuestionCount()))
                .replace("{resumeText}", request.getResumeText() != null ? request.getResumeText() : "无简历，通用面试模式")
                .replace("{questionsContext}", questionsContext.toString());

        // 4. 调用 AI（Spring AI 方式）
        ChatClient chatClient = ChatClient.create(chatModel);
        String aiAnswer = chatClient.prompt()
                .system(systemPrompt)
                .user(userPrompt)
                .call()
                .content();

        if (aiAnswer == null) {
            throw new RuntimeException("AI出题时发生错误，请重试");
        }

        log.info("AI出题结果已经生成（含 RAG 检索）");
        return aiAnswer;
    }

    private List<QuestionVO> parseAIQuestion(String json) throws Exception {
        // 1. 先清理 markdown 代码块标记
        json = json.trim();
        if (json.startsWith("```json")) { json = json.substring(7); }
        if (json.startsWith("```")) { json = json.substring(3); }
        if (json.endsWith("```")) { json = json.substring(0, json.length() - 3); }
        json = json.trim();

        // 2. 解析
        JsonNode root = objectMapper.readTree(json);
        JsonNode questionsNode = root.get("questions");

        List<QuestionVO> list = new ArrayList<>();
        for (int questionIndex = 0; questionIndex < questionsNode.size(); questionIndex++) {
            JsonNode q = questionsNode.get(questionIndex);
            list.add(QuestionVO.builder()
                    .questionIndex(questionIndex)
                    .question(q.get("question").asText())
                    .category(q.get("category").asText())
                    .build());
        }
        return list;
    }
}
