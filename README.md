# ResumeCraft - AI 智能简历分析系统

> 基于大语言模型的智能简历分析 + AI 模拟面试系统，帮助求职者精准定位、高效备战。

## 📖 项目简介

ResumeCraft 是一个面向求职者的 AI 辅助平台，核心功能包括：

- **智能简历分析**：上传简历（PDF/Word），AI 自动进行 5 维度评分（内容完整度、专业技能、项目经验、教育背景、排版设计），生成改进建议
- **AI 模拟面试**：选择技能方向和难度，AI 自动生成面试题，逐题作答后生成详细评估报告
- **数据可视化**：简历评分雷达图、面试状态分布图、技能趋势分析

## 🏗️ 技术架构

```
┌─────────────────────────────────────────────────────────┐
│                    前端 (Vue 3)                          │
│  Vue Router + Pinia + Element Plus + ECharts + Axios    │
└──────────────────────┬──────────────────────────────────┘
                       │ HTTP /api
                       ▼
┌─────────────────────────────────────────────────────────┐
│              后端 (Spring Boot 3 + Java 21)              │
│                                                         │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  │
│  │ 简历模块     │  │ 面试模块     │  │ 用户模块     │  │
│  │ ResumeCtrl   │  │ InterviewCtrl│  │ UserCtrl     │  │
│  └──────┬───────┘  └──────┬───────┘  └──────┬───────┘  │
│         │                 │                 │           │
│  ┌──────┴─────────────────┴─────────────────┴───────┐  │
│  │              Service 层 + AOP 切面                │  │
│  │  AsyncConfig / RateLimitAspect / AuthInterceptor  │  │
│  └──────────────────────┬───────────────────────────┘  │
└─────────────────────────┼───────────────────────────────┘
                          │
        ┌─────────────────┼─────────────────┐
        ▼                 ▼                 ▼
   ┌─────────┐     ┌───────────┐    ┌─────────────┐
   │  MySQL  │     │   Redis   │    │ 阿里云 OSS  │
   │ 数据库  │     │ 缓存+锁   │    │ 文件存储    │
   └─────────┘     └───────────┘    └─────────────┘
                          │
                          ▼
                 ┌─────────────────┐
                 │  DashScope AI   │
                 │  (通义千问)     │
                 └─────────────────┘
```

## 🛠️ 技术栈

### 后端
| 技术 | 版本 | 用途 |
|------|------|------|
| Spring Boot | 3.5.16 | 核心框架 |
| Java | 21 | 开发语言 |
| MyBatis-Plus | 3.5.5 | ORM 框架 |
| MySQL | 8.x | 数据库 |
| Redis | 7.x | 缓存 + 会话管理 |
| Redisson | 3.40.2 | 分布式锁 |
| JWT (JJWT) | 0.12.6 | 用户认证 |
| DashScope SDK | 2.16.4 | AI 能力（通义千问） |
| 阿里云 OSS | 3.17.4 | 文件存储 |
| Apache PDFBox | 3.0.4 | PDF 文本提取 |
| Apache POI | 5.3.0 | Word 文本提取 |
| OpenPDF | 2.0.3 | PDF 报告生成 |
| JFreeChart | 1.5.5 | 图表生成（雷达图） |

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

## ✨ 核心功能

### 1. 简历模块
- ✅ 上传简历（支持 PDF/Word，≤10MB）
- ✅ MD5 文件去重（防止重复上传）
- ✅ 异步解析（PDFBox/POI 提取文本 → AI 评分 → 存入数据库）
- ✅ 5 维度评分（内容完整度、专业技能、项目经验、教育背景、排版设计）
- ✅ 简历详情（Redis 缓存，24 小时过期）
- ✅ 重新分析（失败状态可重试）
- ✅ PDF 报告导出（含 JFreeChart 雷达图）

### 2. 面试模块
- ✅ 创建面试（选择技能方向、难度、题数）
- ✅ AI 出题（DashScope 根据技能方向生成面试题）
- ✅ 逐题作答（逐题推进，状态机管理）
- ✅ 分布式锁（Redisson 防止并发提交同一题）
- ✅ 异步评估（最后一题提交后触发 AI 评估）
- ✅ 面试报告（每题评分 + 参考答案 + 总评 + 改进建议）

### 3. 用户模块
- ✅ 注册/登录（手机号 + 密码）
- ✅ JWT 认证（Redis 双重验证）
- ✅ 短信验证码（Redis 存储，5 分钟过期）
- ✅ 退出登录（清除 Redis token）

### 4. 数据分析
- ✅ 仪表盘（简历数、面试数、平均分统计）
- ✅ 面试状态分布（环形图）
- ✅ 快捷入口（快速跳转核心功能）
- ✅ 最近面试记录（表格展示）

## 🔥 技术亮点

### 1. 异步任务处理
使用 `@Async` + 自定义线程池（`resumeParseExecutor` + `interviewExecutor`），避免 AI 调用阻塞主线程：

```java
@Async("resumeParseExecutor")
public void resumeParseAsync(Long resumeId) {
    // 1. 提取文本（PDFBox/POI）
    // 2. 调用 DashScope AI 评分
    // 3. 保存评分结果
    // 4. 更新解析状态
}
```

**面试话术**：简历解析和面试评估都是耗时操作（AI 调用 10-30 秒），如果用同步方式会导致请求超时。我设计了两个独立线程池，分别处理简历和面试任务，互不干扰。

### 2. Redisson 分布式锁
面试答题时，用户可能快速点击"提交"导致并发请求，用 Redisson 分布式锁防止重复提交：

```java
RLock lock = redissonClient.getLock("lock:answer:" + sessionId);
if (lock.tryLock(0, 5, TimeUnit.SECONDS)) {
    try {
        // 提交答案逻辑
    } finally {
        lock.unlock();
    }
}
```

**面试话术**：面试场景中，用户提交答案后需要更新会话状态并判断是否触发评估。如果并发提交，会导致状态混乱。我用 Redisson 分布式锁，锁的粒度是 sessionId，确保同一会话的并发请求串行执行。

### 3. AOP 限流
自定义 `@RateLimit` 注解 + AOP 切面，基于 Redis INCR 原子计数实现接口限流：

```java
@RateLimit(key = "upload", limit = 5, time = 1)
@PostMapping("/upload")
public Result<Void> uploadResume(@RequestParam("file") MultipartFile file) {
    // 上传逻辑
}
```

**面试话术**：简历上传和面试创建都是高频操作，我用自定义注解 + AOP 实现限流。每个用户每个接口一个 Redis key，比如 `rateLimit:upload:1001`，设置过期时间 1 秒，超过 5 次就拒绝。比 Spring Cloud Gateway 轻量，适合单体应用。

### 4. JWT + Redis 双重鉴权
JWT token 生成后存入 Redis（24 小时过期），每次请求时：
1. 验证 JWT 签名 + 过期时间
2. 验证 Redis 中 token 是否存在

```java
// AuthInterceptor
Long userId = jwtUtils.getUserIdFromToken(token);
String redisKey = "token:" + userId;
String redisToken = redisTemplate.opsForValue().get(redisKey);
if (!token.equals(redisToken)) {
    throw new BusinessException(ResultCode.UNAUTHORIZED);
}
```

**面试话术**：单纯 JWT 的问题是，用户退出登录后 token 仍然有效。我用 JWT + Redis 双重验证，退出时删除 Redis 中的 token，即使用户拿着旧 token 也无法通过验证。

### 5. MD5 文件去重
上传简历时计算文件 MD5，检查是否已存在相同文件：

```java
String fileMd5 = Md5Utils.md5Digest(file.getInputStream());
ResumeRecord existing = resumeRecordMapper.selectOne(
    new LambdaQueryWrapper<ResumeRecord>()
        .eq(ResumeRecord::getFileMd5, fileMd5)
        .eq(ResumeRecord::getUserId, userId)
);
if (existing != null) {
    // 返回已存在的简历
}
```

**面试话术**：用户可能重复上传同一份简历，我用 MD5 去重。上传时计算文件指纹，如果数据库已有相同 MD5 的记录，直接返回，节省 OSS 存储空间和 AI 调用成本。

## 📸 项目截图

> 待补充（启动项目后截图）

### 登录页
![登录页](docs/screenshots/login.png)

### 仪表盘
![仪表盘](docs/screenshots/dashboard.png)

### 简历上传
![简历上传](docs/screenshots/resume-upload.png)

### 简历评分
![简历评分](docs/screenshots/resume-scores.png)

### 创建面试
![创建面试](docs/screenshots/interview-create.png)

### 面试答题
![面试答题](docs/screenshots/interview-session.png)

### 面试报告
![面试报告](docs/screenshots/interview-report.png)

### 数据分析
![数据分析](docs/screenshots/analytics.png)

## 🚀 快速开始

### 环境要求
- JDK 21+
- MySQL 8.x
- Redis 7.x
- Node.js 18+（前端）

### 后端启动

1. **克隆项目**
```bash
git clone https://github.com/your-username/ResumeCraft.git
cd ResumeCraft
```

2. **创建数据库**
```sql
CREATE DATABASE resume_analyzer CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

3. **执行建表 SQL**
```bash
mysql -u root -p resume_analyzer < sql/interview_tables.sql
```

4. **配置环境变量**（可选，不配置则使用默认值）
```bash
export JWT_SECRET=你的JWT密钥
export DB_PASSWORD=你的数据库密码
export ALIYUN_ACCESS_KEY_ID=你的阿里云AK
export ALIYUN_ACCESS_KEY_SECRET=你的阿里云SK
export DASHSCOPE_API_KEY=你的DashScope密钥
```

5. **启动 Spring Boot**
```bash
mvn spring-boot:run
```

后端启动在 `http://localhost:8081`

### 前端启动

```bash
cd frontend
npm install
npm run dev
```

前端启动在 `http://localhost:5173`，自动代理 `/api` 请求到后端 `8081` 端口。

## 📁 项目结构

```
ResumeCraft/
├── src/main/java/com/example/myself_resume_analyzer/
│   ├── common/                    # 公共模块
│   │   ├── annotation/            # 自定义注解（@RateLimit）
│   │   ├── aspect/                # AOP 切面
│   │   ├── config/                # 配置类（Async/Redis/Redisson/OSS）
│   │   ├── exception/             # 全局异常处理
│   │   ├── interceptor/           # JWT 鉴权拦截器
│   │   └── utils/                 # 工具类（JWT/MD5）
│   ├── resume/                    # 简历模块
│   │   ├── controller/            # 控制器
│   │   ├── service/               # 服务层（含异步服务）
│   │   ├── mapper/                # MyBatis-Plus Mapper
│   │   ├── entity/                # 实体类
│   │   ├── dto/                   # 请求 DTO
│   │   └── vo/                    # 响应 VO
│   └── interview/                 # 面试模块
│       ├── controller/
│       ├── service/
│       ├── mapper/
│       ├── entity/
│       ├── dto/
│       └── vo/
├── src/main/resources/
│   ├── application.yml            # 配置文件
│   ├── prompts/                   # AI Prompt 模板
│   └── sql/                       # 建表 SQL
└── frontend/                      # 前端项目
    ├── src/
    │   ├── api/                   # API 封装
    │   ├── components/            # 公共组件（雷达图/环形图）
    │   ├── views/                 # 页面（12 个）
    │   ├── router/                # 路由
    │   └── store/                 # 状态管理
    └── package.json
```

## 📝 开发计划

- [ ] 面试记录导出 PDF
- [ ] WebSocket 实时通知（评估完成推送）
- [ ] 简历模板推荐（根据评分推荐改进方向）
- [ ] 面试题库管理（收藏题目）
- [ ] 数据大屏优化（趋势图/对比图）

## 📄 License

MIT License

---

**项目作者**：小然  
**联系方式**：[your-email@example.com]  
**GitHub**：[https://github.com/your-username/ResumeCraft](https://github.com/your-username/ResumeCraft)
