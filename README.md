<div align="center">

# ResumeCraft - AI 智能简历分析系统

**Spring AI + RAG 增强检索 | 大模型驱动的求职辅助平台**

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.16-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://openjdk.java.net/)
[![Spring AI](https://img.shields.io/badge/Spring%20AI-1.1.2-blue.svg)](https://spring.io/projects/spring-ai)
[![RAG](https://img.shields.io/badge/RAG-向量检索-purple.svg)](https://spring.io/projects/spring-ai)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

</div>

---

## 项目简介

ResumeCraft 是一个基于 **Spring AI + RAG（检索增强生成）** 的智能求职辅助平台，通过大语言模型为求职者提供精准的简历分析和模拟面试服务。

### 核心能力

| 功能模块 | 核心能力 | 技术亮点 |
|---------|---------|---------|
| **智能简历分析** | 上传 PDF/Word 简历，AI 自动 5 维度评分 | 异步处理 + MD5 去重 + PDF 报告导出 |
| **AI 模拟面试** | 根据技能方向自动生成面试题，逐题评估 | **RAG 增强出题** + 分布式锁 + 状态机 |
| **知识库检索** | JD 文档 + 面试题库的语义搜索 | **向量存储 + Embedding** |
| **数据可视化** | 雷达图、环形图、趋势分析 | ECharts 5 + JFreeChart |

---

## 系统架构

```
┌─────────────────────────────────────────────────────────────────────┐
│                         前端 (Vue 3 + ECharts)                       │
│              Vue Router + Pinia + Element Plus + Axios               │
└────────────────────────────────┬────────────────────────────────────┘
                                 │ RESTful API
                                 ▼
┌─────────────────────────────────────────────────────────────────────┐
│                      后端 (Spring Boot 3.5 + Java 21)                │
│                                                                     │
│  ┌──────────────┐    ┌──────────────┐    ┌──────────────┐          │
│  │   简历模块    │    │   面试模块    │    │   用户模块    │          │
│  │ ResumeCtrl   │    │InterviewCtrl │    │  UserCtrl    │          │
│  └──────┬───────┘    └──────┬───────┘    └──────┬───────┘          │
│         │                   │                   │                   │
│  ┌──────┴───────────────────┴───────────────────┴───────┐          │
│  │                  Service 层 + AOP 切面                 │          │
│  │     AsyncConfig / RateLimitAspect / AuthInterceptor   │          │
│  └──────────────────────────┬───────────────────────────┘          │
│                             │                                       │
│  ┌──────────────────────────┴───────────────────────────┐          │
│  │              Spring AI + RAG 知识库                    │          │
│  │     ChatModel / EmbeddingModel / VectorStore          │          │
│  │     KnowledgeBaseService (向量检索 + 上下文增强)        │          │
│  └──────────────────────────┬───────────────────────────┘          │
│                             │                                       │
└─────────────────────────────┼───────────────────────────────────────┘
                              │
        ┌─────────────────────┼─────────────────────┐
        ▼                     ▼                     ▼
   ┌─────────┐         ┌───────────┐        ┌─────────────┐
   │  MySQL  │         │   Redis   │        │ 阿里云 OSS  │
   │  数据库  │         │ 缓存+分布式锁│       │  文件存储   │
   └─────────┘         └───────────┘        └─────────────┘
                              │
                              ▼
                    ┌─────────────────────┐
                    │  Spring AI Alibaba  │
                    │  (通义千问 DashScope) │
                    │   + 向量存储 (RAG)   │
                    └─────────────────────┘
```

---

## 技术栈

### 后端

| 技术 | 版本 | 用途 |
|------|------|------|
| Spring Boot | 3.5.16 | 核心框架 |
| Java | 21 | 开发语言 (LTS) |
| **Spring AI Alibaba** | **1.1.2.2** | **AI 统一抽象层** |
| **SimpleVectorStore** | **Spring AI 内置** | **向量存储 (RAG)** |
| MyBatis-Plus | 3.5.5 | ORM 框架 |
| MySQL | 8.x | 关系型数据库 |
| Redis | 7.x | 缓存 + 会话管理 |
| Redisson | 3.40.2 | 分布式锁 |
| JJWT | 0.12.6 | JWT 认证 |
| 阿里云 OSS | 3.17.4 | 文件存储 |
| Apache PDFBox | 3.0.4 | PDF 文本提取 |
| Apache POI | 5.3.0 | Word 文本提取 |
| OpenPDF | 2.0.3 | PDF 报告生成 |
| JFreeChart | 1.5.5 | 雷达图生成 |

### 前端

| 技术 | 版本 | 用途 |
|------|------|------|
| Vue | 3.x | 前端框架 |
| Vue Router | 4.x | 路由管理 |
| Pinia | 2.x | 状态管理 |
| Element Plus | 2.x | UI 组件库 |
| Axios | 1.x | HTTP 客户端 |
| ECharts | 5.x | 数据可视化 |
| Vite | 6.x | 构建工具 |

---

## 核心功能

### 1. 智能简历分析模块

- [x] 上传简历（支持 PDF/Word，≤10MB）
- [x] **MD5 文件去重**（避免重复上传，节省 AI 调用成本）
- [x] **异步解析**（PDFBox/POI 提取文本 → AI 评分 → 存入数据库）
- [x] **5 维度评分**（内容完整度、专业技能、项目经验、教育背景、排版设计）
- [x] Redis 缓存简历详情（24 小时过期）
- [x] 失败重试机制
- [x] **PDF 报告导出**（含 JFreeChart 雷达图）

### 2. AI 模拟面试模块

- [x] 创建面试（选择技能方向、难度、题数）
- [x] **RAG 增强出题**（先检索知识库 JD + 题库，再结合上下文生成面试题）
- [x] 逐题作答（状态机管理面试流程）
- [x] **Redisson 分布式锁**（防止并发提交答案）
- [x] 异步评估（最后一题提交后触发 AI 评估）
- [x] 面试报告（每题评分 + 参考答案 + 总评 + 改进建议）

### 3. RAG 知识库模块

- [x] **内置知识库**（3 个 JD 文档 + 3 个难度面试题库）
- [x] 应用启动自动加载文档到向量存储
- [x] **语义搜索**（根据技能方向检索相关 JD 和题目）
- [x] 文档列表/详情查看接口
- [x] 支持扩展更多知识文档

### 4. 用户认证模块

- [x] 注册/登录（手机号 + 密码）
- [x] **JWT + Redis 双重鉴权**（支持主动失效 token）
- [x] 短信验证码（Redis 存储，5 分钟过期）
- [x] 退出登录（清除 Redis token）

### 5. 数据分析模块

- [x] 仪表盘（简历数、面试数、平均分统计）
- [x] 面试状态分布（ECharts 环形图）
- [x] 快捷入口 + 最近面试记录

---

## 技术亮点（面试重点）

### 1. Spring AI + RAG 增强检索

**问题**：直接用大模型生成面试题，容易出现偏题或超纲，质量不稳定。

**方案**：引入 RAG（检索增强生成）架构，先检索知识库再让 AI 生成。

```java
// 1. 应用启动时，将知识库文档向量化存储
@PostConstruct
public void initKnowledgeBase() {
    TextReader reader = new TextReader("classpath:knowledge-base/jd/java-backend.md");
    TokenTextSplitter splitter = new TokenTextSplitter();
    List<Document> docs = splitter.apply(reader.get());
    vectorStore.add(docs);  // 文档 -> 分割 -> Embedding -> 向量存储
}

// 2. 面试出题时，先检索相关知识片段
public List<Document> searchQuestionsBySkillAndDifficulty(String skillId, String difficulty, int topK) {
    return vectorStore.similaritySearch(
        SearchRequest.builder()
            .query("Java 后端面试题 中级进阶")
            .topK(topK)
            .build()
    );
}

// 3. 将检索结果作为上下文，传给 AI 生成题目
String context = docs.stream().map(Document::getText).collect(Collectors.joining("\n"));
String prompt = "基于以下岗位要求生成面试题：\n" + context + "\n请生成5道中级难度的面试题...";
ChatResponse response = chatModel.call(new Prompt(prompt));
```

**效果**：
- 题目质量更稳定，贴合岗位要求
- Spring AI 提供统一抽象层，切换模型（如 OpenAI）只需改配置
- SimpleVectorStore 适合开发，生产可升级为 Milvus/Pinecone

---

### 2. 异步任务处理

**问题**：简历解析和面试评估耗时 10-30 秒，同步调用会导致请求超时。

**方案**：`@Async` + 自定义线程池，两个独立线程池互不干扰。

```java
@Async("resumeParseExecutor")  // 简历解析专用线程池
public void resumeParseAsync(Long resumeId) {
    // 1. 提取文本（PDFBox/POI）
    // 2. 调用 Spring AI 评分
    // 3. 保存评分结果
    // 4. 更新解析状态
}

@Async("interviewExecutor")    // 面试评估专用线程池
public void evaluateInterviewAsync(Long sessionId) {
    // 面试评估逻辑
}
```

**线程池配置**：
```java
@Bean("resumeParseExecutor")
public Executor resumeParseExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(4);
    executor.setMaxPoolSize(8);
    executor.setQueueCapacity(100);
    executor.setThreadNamePrefix("resume-parse-");
    executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
    executor.initialize();
    return executor;
}
```

---

### 3. Redisson 分布式锁

**问题**：面试答题时用户快速点击"提交"，可能导致并发请求，状态混乱。

**方案**：Redisson 分布式锁，锁粒度为 sessionId。

```java
RLock lock = redissonClient.getLock("lock:answer:" + sessionId);
if (lock.tryLock(0, 5, TimeUnit.SECONDS)) {
    try {
        // 提交答案 + 更新会话状态 + 判断是否触发评估
    } finally {
        lock.unlock();
    }
} else {
    throw new BusinessException("请求过于频繁，请稍后再试");
}
```

**优势**：
- 自动续期（看门狗机制）
- 可重入锁
- 比 ZooKeeper 轻量，适合 Redis 场景

---

### 4. AOP 接口限流

**问题**：简历上传和面试创建是高频操作，需要防止恶意刷接口。

**方案**：自定义 `@RateLimit` 注解 + AOP 切面，基于 Redis INCR 原子计数。

```java
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimit {
    String key();           // 限流 key
    int limit() default 5;  // 限制次数
    int time() default 1;   // 时间窗口（秒）
}

@Aspect
@Component
public class RateLimitAspect {
    @Around("@annotation(rateLimit)")
    public Object around(ProceedingJoinPoint point, RateLimit rateLimit) throws Throwable {
        String redisKey = "rateLimit:" + rateLimit.key() + ":" + userId;
        Long count = redisTemplate.opsForValue().increment(redisKey);
        if (count == 1) {
            redisTemplate.expire(redisKey, rateLimit.time(), TimeUnit.SECONDS);
        }
        if (count > rateLimit.limit()) {
            throw new BusinessException("请求过于频繁");
        }
        return point.proceed();
    }
}
```

**使用**：
```java
@RateLimit(key = "upload", limit = 5, time = 1)  // 每秒最多5次
@PostMapping("/upload")
public Result<Void> uploadResume(@RequestParam("file") MultipartFile file) {
    // 上传逻辑
}
```

---

### 5. JWT + Redis 双重鉴权

**问题**：纯 JWT 退出登录后 token 仍然有效，无法主动失效。

**方案**：JWT + Redis 双重验证。

```java
// 登录时：生成 JWT，同时存入 Redis
String token = jwtUtils.generateToken(userId);
redisTemplate.opsForValue().set("token:" + userId, token, 24, TimeUnit.HOURS);

// 请求拦截器：双重验证
public boolean preHandle(HttpServletRequest request, HttpServletResponse response) {
    String token = request.getHeader("Authorization");
    Long userId = jwtUtils.getUserIdFromToken(token);  // 1. 验证 JWT 签名
    String redisToken = redisTemplate.opsForValue().get("token:" + userId);
    if (!token.equals(redisToken)) {
        throw new BusinessException(ResultCode.UNAUTHORIZED);  // 2. 验证 Redis token
    }
    return true;
}

// 退出登录：删除 Redis token，JWT 立即失效
redisTemplate.delete("token:" + userId);
```

---

### 6. MD5 文件去重

**问题**：用户可能重复上传同一份简历，浪费 OSS 存储和 AI 调用成本。

**方案**：上传时计算文件 MD5，检查是否已存在。

```java
String fileMd5 = Md5Utils.md5Digest(file.getInputStream());

// 查询是否已存在相同文件
ResumeRecord existing = resumeRecordMapper.selectOne(
    new LambdaQueryWrapper<ResumeRecord>()
        .eq(ResumeRecord::getFileMd5, fileMd5)
        .eq(ResumeRecord::getUserId, userId)
);

if (existing != null) {
    // 直接返回已有记录，不重复上传
    return Result.success(existing);
}

// 不存在则上传 OSS + 调用 AI 解析
fileStorageService.upload(file, fileName);
resumeService.parseResumeAsync(resumeId);
```

---

## 项目截图

> 待补充（启动项目后截图）

| 功能 | 截图 |
|-----|------|
| 登录页 | ![登录页](docs/screenshots/login.png) |
| 仪表盘 | ![仪表盘](docs/screenshots/dashboard.png) |
| 简历上传 | ![简历上传](docs/screenshots/resume-upload.png) |
| 简历评分 | ![简历评分](docs/screenshots/resume-scores.png) |
| 创建面试 | ![创建面试](docs/screenshots/interview-create.png) |
| 面试答题 | ![面试答题](docs/screenshots/interview-session.png) |
| 面试报告 | ![面试报告](docs/screenshots/interview-report.png) |
| 数据分析 | ![数据分析](docs/screenshots/analytics.png) |

---

## 快速开始

### 环境要求

- JDK 21+
- MySQL 8.x
- Redis 7.x
- Node.js 18+（前端）

### 后端启动

```bash
# 1. 克隆项目
git clone https://github.com/october130/ResumeCraft.git
cd ResumeCraft

# 2. 创建数据库
mysql -u root -p -e "CREATE DATABASE resume_analyzer CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"

# 3. 执行建表 SQL
mysql -u root -p resume_analyzer < sql/interview_tables.sql

# 4. 配置环境变量（可选）
export JWT_SECRET=你的JWT密钥
export DB_PASSWORD=你的数据库密码
export ALIYUN_ACCESS_KEY_ID=你的阿里云AK
export ALIYUN_ACCESS_KEY_SECRET=你的阿里云SK
export AI_API_KEY=你的DashScope密钥

# 5. 启动 Spring Boot
mvn spring-boot:run
```

后端运行在 `http://localhost:8081`

### 前端启动

```bash
cd frontend
npm install
npm run dev
```

前端运行在 `http://localhost:5173`，自动代理 `/api` 请求到后端。

---

## 项目结构

```
ResumeCraft/
├── src/main/java/com/example/myself_resume_analyzer/
│   ├── common/                        # 公共模块
│   │   ├── annotation/                # 自定义注解（@RateLimit）
│   │   ├── aspect/                    # AOP 切面（限流）
│   │   ├── config/                    # 配置类
│   │   │   ├── AsyncConfig            # 异步线程池
│   │   │   ├── RedisConfig            # Redis 序列化
│   │   │   ├── RedissonConfig         # Redisson 分布式锁
│   │   │   ├── OssConfig              # 阿里云 OSS
│   │   │   └── VectorStoreConfig      # 向量存储（RAG）
│   │   ├── controller/                # 知识库控制器
│   │   ├── exception/                 # 全局异常处理
│   │   ├── interceptor/               # JWT 鉴权拦截器
│   │   ├── service/                   # 知识库服务（RAG）
│   │   └── utils/                     # 工具类（JWT/MD5）
│   ├── resume/                        # 简历模块
│   │   ├── controller/                # ResumeController
│   │   ├── service/                   # 同步/异步服务
│   │   ├── mapper/                    # MyBatis-Plus Mapper
│   │   ├── entity/                    # 实体类
│   │   ├── dto/                       # 请求 DTO
│   │   └── vo/                        # 响应 VO
│   └── interview/                     # 面试模块
│       ├── controller/                # InterviewController
│       ├── service/                   # 会话/题目/异步服务
│       ├── mapper/
│       ├── entity/
│       ├── dto/
│       └── vo/
├── src/main/resources/
│   ├── application.yml                # 配置文件
│   ├── knowledge-base/                # RAG 知识库
│   │   ├── jd/                        # 岗位 JD（Java/前端/Python）
│   │   └── questions/                 # 面试题库（初/中/高级）
│   ├── prompts/                       # AI Prompt 模板
│   └── sql/                           # 建表 SQL
└── frontend/                          # 前端项目
    ├── src/
    │   ├── api/                       # API 封装
    │   ├── components/                # 公共组件（雷达图/环形图）
    │   ├── views/                     # 页面（12 个）
    │   ├── router/                    # 路由
    │   └── store/                     # 状态管理
    └── package.json
```

---

## 开发计划

- [ ] 面试记录导出 PDF
- [ ] WebSocket 实时通知（评估完成推送）
- [ ] 简历模板推荐（根据评分推荐改进方向）
- [ ] 面试题库管理（收藏题目）
- [ ] 数据大屏优化（趋势图/对比图）
- [ ] 向量存储升级为 Milvus（生产级向量数据库）

---

## License

MIT License

---

<div align="center">

**作者**：October130 | **GitHub**：[ResumeCraft](https://github.com/october130/ResumeCraft)

如果这个项目对你有帮助，请给一个 Star 支持！

</div>
