package com.example.myself_resume_analyzer.resume.service.impl;

import com.example.myself_resume_analyzer.common.service.KnowledgeBaseService;
import com.example.myself_resume_analyzer.resume.service.ResumeGradingService;
import com.example.myself_resume_analyzer.resume.vo.ResumeEvaluationVO;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 简历评分服务实现（Spring AI + RAG 版本）
 */
@Service
@Slf4j
public class ResumeGradingServiceImpl implements ResumeGradingService {

    @Resource
    private ChatModel chatModel;

    @Resource
    private ObjectMapper objectMapper;

    @Resource
    private KnowledgeBaseService knowledgeBaseService;

    @Override
    public ResumeEvaluationVO analyzeResume(String resumeText) {
        log.info("开始分析简历，文本长度: {} 字符", resumeText.length());

        try {
            // 1. 调用 AI 获取评分 JSON（带 RAG 检索）
            String json = callAI(resumeText);
            log.trace("AI 返回结果: {}", json);

            // 2. 解析 JSON，转换为 ResumeEvaluationVO
            ResumeEvaluationVO vo = parseAIResponse(json);
            vo.setAnalyzedAt(LocalDateTime.now());

            log.info("简历分析完成，总分: {}", vo.getOverallScore());
            return vo;

        } catch (Exception e) {
            log.error("简历分析失败: {}", e.getMessage(), e);
            throw new RuntimeException("简历分析失败: " + e.getMessage(), e);
        }
    }

    /**
     * 调用 AI 进行简历评分（Spring AI + RAG 版本）
     */
    private String callAI(String resumeText) throws Exception {
        // 1. RAG 检索：根据简历内容检索相关 JD
        List<Document> jdDocs = knowledgeBaseService.searchJdBySkill("java-backend", 2);
        StringBuilder jdContext = new StringBuilder();
        for (Document doc : jdDocs) {
            jdContext.append(doc.getText()).append("\n\n");
        }
        log.info("RAG 检索到 {} 个 JD 文档", jdDocs.size());

        // 2. 读取 system prompt
        String systemPrompt = new String(
                getClass().getResourceAsStream("/prompts/resume_grading_system.txt").readAllBytes(),
                StandardCharsets.UTF_8
        );

        // 3. 读取 user prompt 模板，替换占位符
        String userPromptTemplate = new String(
                getClass().getResourceAsStream("/prompts/resume_grading_user.txt").readAllBytes(),
                StandardCharsets.UTF_8
        );
        String userPrompt = userPromptTemplate
                .replace("{resumeText}", resumeText)
                .replace("{jdContext}", jdContext.toString());

        // 4. 调用 AI（Spring AI 方式）
        ChatClient chatClient = ChatClient.create(chatModel);
        String aiAnswer = chatClient.prompt()
                .system(systemPrompt)
                .user(userPrompt)
                .call()
                .content();

        if (aiAnswer == null) {
            throw new RuntimeException("AI评分失败");
        }

        log.info("AI评分调用成功（含 RAG 检索）");
        return aiAnswer;
    }

    /**
     * 解析 AI 返回的 JSON
     */
    private ResumeEvaluationVO parseAIResponse(String json) throws Exception {
        // 清理可能的 markdown 代码块标记
        json = json.trim();
        if (json.startsWith("```json")) {
            json = json.substring(7);
        }
        if (json.startsWith("```")) {
            json = json.substring(3);
        }
        if (json.endsWith("```")) {
            json = json.substring(0, json.length() - 3);
        }
        json = json.trim();

        JsonNode root = objectMapper.readTree(json);

        // 解析评分详情
        JsonNode scoreDetailNode = root.get("scoreDetail");

        // 解析优点列表
        List<String> strengths = new ArrayList<>();
        JsonNode strengthsNode = root.get("strengths");
        if (strengthsNode != null && strengthsNode.isArray()) {
            for (JsonNode node : strengthsNode) {
                strengths.add(node.asText());
            }
        }

        // 解析建议列表
        List<ResumeEvaluationVO.SuggestionVO> suggestions = new ArrayList<>();
        JsonNode suggestionsNode = root.get("suggestions");
        if (suggestionsNode != null && suggestionsNode.isArray()) {
            for (JsonNode node : suggestionsNode) {
                ResumeEvaluationVO.SuggestionVO suggestion = ResumeEvaluationVO.SuggestionVO.builder()
                        .category(node.get("category").asText())
                        .priority(node.get("priority").asText())
                        .issue(node.get("issue").asText())
                        .recommendation(node.get("recommendation").asText())
                        .build();
                suggestions.add(suggestion);
            }
        }

        // 构建返回对象
        return ResumeEvaluationVO.builder()
                .overallScore(root.get("overallScore").asInt())
                .contentScore(scoreDetailNode.get("contentScore").asInt())
                .structureScore(scoreDetailNode.get("structureScore").asInt())
                .skillMatchScore(scoreDetailNode.get("skillMatchScore").asInt())
                .expressionScore(scoreDetailNode.get("expressionScore").asInt())
                .projectScore(scoreDetailNode.get("projectScore").asInt())
                .summary(root.get("summary").asText())
                .strengths(strengths)
                .suggestions(suggestions)
                .build();
    }
}
