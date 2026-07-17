package com.example.myself_resume_analyzer.common.service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.reader.TextReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 知识库服务（RAG）
 * 负责加载知识库文档到向量存储
 */
@Service
@Slf4j
public class KnowledgeBaseService {

    @Resource
    private VectorStore vectorStore;

    @Resource
    private EmbeddingModel embeddingModel;

    /**
     * 应用启动时加载知识库
     */
    @PostConstruct
    public void initKnowledgeBase() {
        log.info("开始加载知识库...");

        try {
            // 1. 读取 JD 文档
            TextReader javaReader = new TextReader("classpath:knowledge-base/jd/java-backend.md");
            TextReader frontendReader = new TextReader("classpath:knowledge-base/jd/frontend.md");
            TextReader pythonReader = new TextReader("classpath:knowledge-base/jd/python.md");

            javaReader.getCustomMetadata().put("type", "jd");
            javaReader.getCustomMetadata().put("skill", "java-backend");

            frontendReader.getCustomMetadata().put("type", "jd");
            frontendReader.getCustomMetadata().put("skill", "frontend");

            pythonReader.getCustomMetadata().put("type", "jd");
            pythonReader.getCustomMetadata().put("skill", "python");

            // 2. 读取面试题题库
            TextReader javaJuniorReader = new TextReader("classpath:knowledge-base/questions/java-junior.md");
            TextReader javaMidReader = new TextReader("classpath:knowledge-base/questions/java-mid.md");
            TextReader javaSeniorReader = new TextReader("classpath:knowledge-base/questions/java-senior.md");

            javaJuniorReader.getCustomMetadata().put("type", "questions");
            javaJuniorReader.getCustomMetadata().put("skill", "java-backend");
            javaJuniorReader.getCustomMetadata().put("difficulty", "junior");

            javaMidReader.getCustomMetadata().put("type", "questions");
            javaMidReader.getCustomMetadata().put("skill", "java-backend");
            javaMidReader.getCustomMetadata().put("difficulty", "mid");

            javaSeniorReader.getCustomMetadata().put("type", "questions");
            javaSeniorReader.getCustomMetadata().put("skill", "java-backend");
            javaSeniorReader.getCustomMetadata().put("difficulty", "senior");

            // 3. 分割文档
            TokenTextSplitter splitter = new TokenTextSplitter();
            List<Document> javaDocs = splitter.apply(javaReader.get());
            List<Document> frontendDocs = splitter.apply(frontendReader.get());
            List<Document> pythonDocs = splitter.apply(pythonReader.get());

            List<Document> javaJuniorDocs = splitter.apply(javaJuniorReader.get());
            List<Document> javaMidDocs = splitter.apply(javaMidReader.get());
            List<Document> javaSeniorDocs = splitter.apply(javaSeniorReader.get());

            // 4. 存入向量数据库
            vectorStore.add(javaDocs);
            vectorStore.add(frontendDocs);
            vectorStore.add(pythonDocs);

            vectorStore.add(javaJuniorDocs);
            vectorStore.add(javaMidDocs);
            vectorStore.add(javaSeniorDocs);

            int totalDocs = javaDocs.size() + frontendDocs.size() + pythonDocs.size()
                    + javaJuniorDocs.size() + javaMidDocs.size() + javaSeniorDocs.size();
            log.info("知识库加载完成，共 {} 个文档片段", totalDocs);

        } catch (Exception e) {
            log.error("知识库加载失败", e);
        }
    }

    /**
     * 根据技能方向检索 JD
     */
    public List<Document> searchJdBySkill(String skillId, int topK) {
        log.info("检索 JD，skillId={}, topK={}", skillId, topK);

        String query = switch (skillId) {
            case "java-backend" -> "Java 后端开发工程师岗位要求";
            case "frontend" -> "前端开发工程师岗位要求";
            case "python" -> "Python 开发工程师岗位要求";
            default -> "软件开发工程师岗位要求";
        };

        return vectorStore.similaritySearch(
                org.springframework.ai.vectorstore.SearchRequest.builder()
                        .query(query)
                        .topK(topK)
                        .build()
        );
    }

    /**
     * 根据技能方向和难度检索面试题
     */
    public List<Document> searchQuestionsBySkillAndDifficulty(String skillId, String difficulty, int topK) {
        log.info("检索面试题，skillId={}, difficulty={}, topK={}", skillId, difficulty, topK);

        String query = switch (skillId) {
            case "java-backend" -> "Java 后端面试题";
            case "frontend" -> "前端面试题";
            case "python" -> "Python 面试题";
            default -> "软件开发面试题";
        };

        query += " " + switch (difficulty) {
            case "junior" -> "初级基础";
            case "mid" -> "中级进阶";
            case "senior" -> "高级架构";
            default -> "";
        };

        return vectorStore.similaritySearch(
                org.springframework.ai.vectorstore.SearchRequest.builder()
                        .query(query)
                        .topK(topK)
                        .build()
        );
    }
}
