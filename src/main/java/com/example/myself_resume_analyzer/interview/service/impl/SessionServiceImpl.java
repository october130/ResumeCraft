package com.example.myself_resume_analyzer.interview.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.myself_resume_analyzer.common.Result.Result;
import com.example.myself_resume_analyzer.interview.dto.CreateInterviewRequestDTO;
import com.example.myself_resume_analyzer.interview.dto.SubmitAnswerRequestDTO;
import com.example.myself_resume_analyzer.interview.entity.Answer;
import com.example.myself_resume_analyzer.interview.entity.Session;
import com.example.myself_resume_analyzer.interview.mapper.AnswerMapper;
import com.example.myself_resume_analyzer.interview.mapper.SessionMapper;
import com.example.myself_resume_analyzer.interview.service.InterviewAsyncService;
import com.example.myself_resume_analyzer.interview.service.QuestionService;
import com.example.myself_resume_analyzer.interview.service.SessionService;
import com.example.myself_resume_analyzer.interview.vo.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 面试会话服务实现
 */
@Slf4j
@Service
public class SessionServiceImpl implements SessionService {
    @Resource
    private QuestionService questionService;
    @Resource
    private RedisTemplate redisTemplate;
    @Resource
    private SessionMapper interviewSessionMapper;
    @Resource
    private AnswerMapper answerMapper;
    @Resource
    private ObjectMapper objectMapper;
    @Resource
    private InterviewAsyncService interviewAsyncService;
    @Resource
    private RedissonClient redissonClient;
    @Override
    public Result<SessionVO> createSession(CreateInterviewRequestDTO request, Long userId) {
        Session session = new Session();
        session.setSessionId(UUID.randomUUID().toString());
        try {
            Result<List<QuestionVO>> questions = questionService.createQuestion(request);
            List<QuestionVO> questionVOList = questions.getData();

            log.info("AI创建面试题目成功,一共有{}题", questionVOList.size());

             if (request.getResumeId() != null){
                 redisTemplate.opsForValue().set(
                         "interview:resume:" + request.getResumeId(),
                          session.getSessionId(),
                         24, TimeUnit.HOURS
                 );
             }
                     //将信息存到数据库中
            session.setResumeId(request.getResumeId());
             session.setUserId(userId);
             session.setSkillId(request.getSkillId());
             session.setDifficulty(request.getDifficulty());
             session.setTotalQuestions(questionVOList.size());
             session.setCurrentIndex(0);
             session.setStatus("CREATED");
             session.setQuestionsJson(objectMapper.writeValueAsString(questionVOList));
             session.setCreatedAt(LocalDateTime.now());


             interviewSessionMapper.insert(session);
             log.info("面试会话保存成功");

             // 缓存完整Session对象到Redis（供submitAnswer快速读取，24h过期）
             String sessionJson = objectMapper.writeValueAsString(session);
             redisTemplate.opsForValue().set(
                     "interview_session:" + session.getSessionId(),
                     sessionJson,
                     24,
                     TimeUnit.HOURS
             );
             log.info("面试会话缓存到Redis成功");


             return Result.success(SessionVO.builder()
                     .sessionId(session.getSessionId())
                     .totalQuestions(questionVOList.size())
                     .currentQuestionIndex(0)
                     .questions(questionVOList)
                     .status("CREATED")
                     .build());


        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Result<SubmitAnswerResponseVO> submitAnswer(SubmitAnswerRequestDTO Request) {
        // 1. 优先从 Redis 获取会话（快），miss 则查数据库（兜底）
        Session session = getSessionFromRedisOrDB(Request.getSessionId());
        if (session == null) {
            return Result.error("面试会话不存在");
        }

        // 2. 分布式锁：防止并发提交同一题
        String lockKey = "lock:session:" + Request.getSessionId();
        RLock lock = redissonClient.getLock(lockKey);
        try {
            if (!lock.tryLock(0, 10, TimeUnit.SECONDS)) {
                log.warn("会话{}正被其他请求处理，拒绝重复提交", Request.getSessionId());
                return Result.error("操作中，请勿重复提交");
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        try {
            // 加锁后重新读取会话（可能被上一个请求更新过了）
            session = getSessionFromRedisOrDB(Request.getSessionId());
            if (session == null) {
                return Result.error("面试会话不存在");
            }

            if ("EVALUATED".equals(session.getStatus())){
                return Result.error("面试已经完成评估，无法继续答题");
            }
            Integer currentIndex = session.getCurrentIndex();
            if (!Request.getQuestionIndex().equals(currentIndex)){
                return Result.error("题号不匹配，当前应答第" + session.getCurrentIndex() + "题");
            }
            String questionsJson = session.getQuestionsJson();

            try {
                // 从 questionsJson 解析出当前题目
                JsonNode questions = objectMapper.readTree(questionsJson);
                JsonNode currentQ = questions.get(Request.getQuestionIndex());

                // 构建 Answer 存入数据库
                Answer answer = new Answer();
                answer.setSessionId(Request.getSessionId());
                answer.setQuestionIndex(Request.getQuestionIndex());
                answer.setQuestion(currentQ.get("question").asText());
                answer.setCategory(currentQ.get("category").asText());
                answer.setUserAnswer(Request.getAnswer());
                answerMapper.insert(answer);

                // 更新 session 进度
                session.setCurrentIndex(currentIndex + 1);
                session.setStatus("IN_PROGRESS");

                // 判断是否全部答完
                boolean hasNext = currentIndex + 1 < session.getTotalQuestions();
                QuestionVO nextQuestion = null;
                if (hasNext) {
                    JsonNode nextQ = questions.get(currentIndex + 1);
                    nextQuestion = QuestionVO.builder()
                            .questionIndex(currentIndex + 1)
                            .question(nextQ.get("question").asText())
                            .category(nextQ.get("category").asText())
                            .build();
                } else {
                    session.setStatus("COMPLETED");
                    session.setCompletedAt(LocalDateTime.now());
                    interviewAsyncService.evaluateAsync(session.getSessionId());
                }

                interviewSessionMapper.updateById(session);

                // 同步更新 Redis 缓存
                try {
                    String updatedJson = objectMapper.writeValueAsString(session);
                    redisTemplate.opsForValue().set(
                            "interview_session:" + session.getSessionId(),
                            updatedJson,
                            24,
                            TimeUnit.HOURS
                    );
                } catch (Exception ex) {
                    log.warn("Redis缓存同步失败，不影响业务", ex);
                }

                return Result.success(SubmitAnswerResponseVO.builder()
                        .hasNextQuestion(hasNext)
                        .nextQuestion(nextQuestion)
                        .currentIndex(currentIndex + 1)
                        .totalQuestions(session.getTotalQuestions())
                        .build());

            } catch (Exception e) {
                log.error("提交答案失败", e);
                return Result.error("提交答案失败");
            }
        } finally {
            lock.unlock();
        }
    }

    @Override
    public Result<List<ListItemVO>> listSessions(Long userId) {
        List<Session> sessions = interviewSessionMapper.selectList(
                new QueryWrapper<Session>()
                        .eq("user_id", userId)
                        .orderByDesc("created_at")
        );

        List<ListItemVO> list = sessions.stream().map(session ->
                ListItemVO.builder()
                        .sessionId(session.getSessionId())
                        .skillId(session.getSkillId())
                        .difficulty(session.getDifficulty())
                        .resumeId(session.getResumeId())
                        .totalQuestions(session.getTotalQuestions())
                        .status(session.getStatus())
                        .evaluationStatus(session.getEvaluateStatus())
                        .overallScore(session.getOverallScore())
                        .createdAt(session.getCreatedAt())
                        .completedAt(session.getCompletedAt())
                        .build()
        ).collect(Collectors.toList());

        return Result.success(list);
    }

    @Override
    public Result<InterviewDetailVO> getInterviewDetail(String sessionId) {
        // 1. 查询面试会话信息
        Session session = interviewSessionMapper.selectOne(
                new QueryWrapper<Session>().eq("session_id", sessionId)
        );
        if (session == null) {
            return Result.error("面试会话不存在");
        }

        // 2. 查询该会话的所有答题记录，按题号排序
        List<Answer> answerList = answerMapper.selectList(
                new QueryWrapper<Answer>()
                        .eq("session_id", sessionId)
                        .orderByAsc("question_index")
        );

        // 3. Answer 实体转 QuestionVO
        List<QuestionVO> answerDetailList = answerList.stream().map(answer ->
                QuestionVO.builder()
                        .questionIndex(answer.getQuestionIndex())
                        .question(answer.getQuestion())
                        .category(answer.getCategory())
                        .userAnswer(answer.getUserAnswer())
                        .score(answer.getScore())
                        .feedback(answer.getFeedback())
                        .referenceAnswer(answer.getReferenceAnswer())
                        .build()
        ).collect(Collectors.toList());

        // 4. 解析 strengthsJson 和 improvementsJson（JSON数组 → List<String>）
        List<String> strengths = parseJsonArray(session.getStrengthsJson());
        List<String> improvements = parseJsonArray(session.getImprovementsJson());

        // 5. 组装 InterviewDetailVO 返回
        InterviewDetailVO detail = InterviewDetailVO.builder()
                .sessionId(session.getSessionId())
                .skillId(session.getSkillId())
                .difficulty(session.getDifficulty())
                .totalQuestions(session.getTotalQuestions())
                .status(session.getStatus())
                .evaluateStatus(session.getEvaluateStatus())
                .overallScore(session.getOverallScore())
                .overallFeedback(session.getOverallFeedback())
                .strengths(strengths)
                .improvements(improvements)
                .createdAt(session.getCreatedAt())
                .completedAt(session.getCompletedAt())
                .answers(answerDetailList)
                .build();

        return Result.success(detail);
    }

    @Override
    public Result deleteSession(String sessionId) {
        // 1. 从数据库查会话是否存在（以DB为准，Redis可能已过期）
        Session session = interviewSessionMapper.selectOne(
                new QueryWrapper<Session>().eq("session_id", sessionId)
        );
        if (session == null) {
            return Result.error("面试记录不存在");
        }

        // 2. 删除 Redis 缓存（如果还在的话）
        redisTemplate.delete("interview_session:" + sessionId);
        if (session.getResumeId() != null) {
            redisTemplate.delete("interview:resume:" + session.getResumeId());
        }
        log.info("Redis缓存已清理");

        // 3. 删除 answer 表记录（按 session_id 删，不是按主键 id）
        answerMapper.delete(new QueryWrapper<Answer>().eq("session_id", sessionId));

        // 4. 删除 session 表记录
        interviewSessionMapper.delete(new QueryWrapper<Session>().eq("session_id", sessionId));

        log.info("面试记录删除成功, sessionId={}", sessionId);
        return Result.success();
    }

    /**
     * 优先从 Redis 获取 Session，miss 则查数据库并回填 Redis
     */
    private Session getSessionFromRedisOrDB(String sessionId) {
        String redisKey = "interview_session:" + sessionId;
        try {
            // 1. 先查 Redis
            Object cached = redisTemplate.opsForValue().get(redisKey);
            if (cached != null) {
                return objectMapper.readValue(cached.toString(), Session.class);
            }
        } catch (Exception e) {
            log.warn("Redis读取失败，降级查数据库", e);
        }

        // 2. Redis 没有，查数据库
        Session session = interviewSessionMapper.selectOne(
                new QueryWrapper<Session>().eq("session_id", sessionId)
        );

        // 3. 回填 Redis（下次就不用查DB了）
        if (session != null) {
            try {
                String json = objectMapper.writeValueAsString(session);
                redisTemplate.opsForValue().set(redisKey, json, 24, TimeUnit.HOURS);
            } catch (Exception e) {
                log.warn("Redis回填失败", e);
            }
        }
        return session;
    }

    /**
     * 工具方法：将 JSON 数组字符串解析为 List<String>
     * 例如 '["优势1","优势2"]' → ["优势1", "优势2"]
     * 如果输入为 null 或空，返回空列表
     */
    private List<String> parseJsonArray(String json) {
        if (json == null || json.isEmpty()) {
            return List.of();
        }
        try {
            JsonNode node = objectMapper.readTree(json);
            List<String> list = new java.util.ArrayList<>();
            for (JsonNode item : node) {
                list.add(item.asText());
            }
            return list;
        } catch (Exception e) {
            log.warn("JSON数组解析失败: {}", json, e);
            return List.of();
        }
    }
}


