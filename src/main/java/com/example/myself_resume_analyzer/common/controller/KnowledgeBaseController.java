package com.example.myself_resume_analyzer.common.controller;

import com.example.myself_resume_analyzer.common.Result.Result;
import com.example.myself_resume_analyzer.common.service.KnowledgeBaseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * 知识库控制器
 * 提供知识库文档列表查看和语义搜索接口
 */
@RestController
@RequestMapping("/api/knowledge-base")
@Slf4j
@Tag(name = "知识库接口")
public class KnowledgeBaseController {

    @Resource
    private KnowledgeBaseService knowledgeBaseService;

    /**
     * 知识库文档元信息（静态配置）
     */
    private static final List<Map<String, Object>> KB_DOCS = List.of(
            Map.of("id", "jd-java-backend", "type", "jd", "skill", "java-backend",
                    "name", "Java 后端开发工程师 JD", "path", "knowledge-base/jd/java-backend.md"),
            Map.of("id", "jd-frontend", "type", "jd", "skill", "frontend",
                    "name", "前端开发工程师 JD", "path", "knowledge-base/jd/frontend.md"),
            Map.of("id", "jd-python", "type", "jd", "skill", "python",
                    "name", "Python 开发工程师 JD", "path", "knowledge-base/jd/python.md"),
            Map.of("id", "q-java-junior", "type", "questions", "skill", "java-backend", "difficulty", "junior",
                    "name", "Java 初级面试题库", "path", "knowledge-base/questions/java-junior.md"),
            Map.of("id", "q-java-mid", "type", "questions", "skill", "java-backend", "difficulty", "mid",
                    "name", "Java 中级面试题库", "path", "knowledge-base/questions/java-mid.md"),
            Map.of("id", "q-java-senior", "type", "questions", "skill", "java-backend", "difficulty", "senior",
                    "name", "Java 高级面试题库", "path", "knowledge-base/questions/java-senior.md")
    );

    @GetMapping("/list")
    @Operation(summary = "获取知识库文档列表")
    public Result<List<Map<String, Object>>> listDocuments() {
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map<String, Object> doc : KB_DOCS) {
            Map<String, Object> item = new LinkedHashMap<>(doc);
            // 读取文件内容用于预览
            try {
                ClassPathResource resource = new ClassPathResource((String) doc.get("path"));
                String content = resource.getContentAsString(StandardCharsets.UTF_8);
                item.put("charCount", content.length());
                item.put("preview", content.substring(0, Math.min(200, content.length())));
            } catch (IOException e) {
                item.put("charCount", 0);
                item.put("preview", "文件读取失败");
            }
            result.add(item);
        }
        return Result.success(result);
    }

    @GetMapping("/doc/{docId}")
    @Operation(summary = "获取知识库文档详情")
    public Result<Map<String, Object>> getDocument(@PathVariable String docId) {
        Map<String, Object> doc = KB_DOCS.stream()
                .filter(d -> d.get("id").equals(docId))
                .findFirst()
                .orElse(null);
        if (doc == null) {
            return Result.error("文档不存在");
        }
        Map<String, Object> result = new LinkedHashMap<>(doc);
        try {
            ClassPathResource resource = new ClassPathResource((String) doc.get("path"));
            result.put("content", resource.getContentAsString(StandardCharsets.UTF_8));
        } catch (IOException e) {
            result.put("content", "文件读取失败");
        }
        return Result.success(result);
    }

    @GetMapping("/search")
    @Operation(summary = "语义搜索知识库")
    public Result<List<Map<String, Object>>> search(
            @RequestParam(defaultValue = "java-backend") String skillId,
            @RequestParam(defaultValue = "jd") String type,
            @RequestParam(defaultValue = "3") int topK) {

        log.info("知识库搜索: skillId={}, type={}, topK={}", skillId, type, topK);

        List<Document> docs;
        if ("questions".equals(type)) {
            docs = knowledgeBaseService.searchQuestionsBySkillAndDifficulty(skillId, "junior", topK);
        } else {
            docs = knowledgeBaseService.searchJdBySkill(skillId, topK);
        }

        List<Map<String, Object>> results = new ArrayList<>();
        for (Document doc : docs) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("content", doc.getText());
            item.put("score", doc.getScore() != null ? doc.getScore() : 0.0);
            item.put("metadata", doc.getMetadata());
            results.add(item);
        }
        return Result.success(results);
    }
}
