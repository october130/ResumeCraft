-- =====================================================
-- AI 面试模块 - 数据库建表语句
-- 数据库：resume_analyzer
-- =====================================================

-- 1. 面试会话表（主表）
DROP TABLE IF EXISTS interview_answer;
DROP TABLE IF EXISTS interview_session;

CREATE TABLE interview_session (
    id                BIGINT PRIMARY KEY AUTO_INCREMENT      COMMENT '自增主键',
    session_id        VARCHAR(36) NOT NULL UNIQUE             COMMENT '会话UUID（对外标识）',
    user_id           BIGINT NOT NULL                         COMMENT '用户ID',
    resume_id         BIGINT                                  COMMENT '关联简历ID（可为空，支持无简历面试）',
    skill_id          VARCHAR(64) DEFAULT 'java-backend'      COMMENT '面试技能方向',
    difficulty        VARCHAR(16) DEFAULT 'mid'               COMMENT '难度级别：junior/mid/senior',
    total_questions   INT DEFAULT 5                           COMMENT '题目总数',
    current_index     INT DEFAULT 0                           COMMENT '当前题号（从0开始）',
    status            VARCHAR(20) DEFAULT 'CREATED'           COMMENT '面试状态：CREATED→IN_PROGRESS→COMPLETED→EVALUATED',
    questions_json    TEXT                                    COMMENT '题目列表JSON',
    overall_score     INT                                     COMMENT '总分（0-100，评估后填充）',
    overall_feedback  TEXT                                    COMMENT '总体评价',
    strengths_json    TEXT                                    COMMENT '优势JSON数组',
    improvements_json TEXT                                    COMMENT '改进建议JSON数组',
    evaluate_status   VARCHAR(20)                             COMMENT '评估状态：PENDING/RUNNING/COMPLETED/FAILED',
    evaluate_error    VARCHAR(500)                            COMMENT '评估失败原因',
    created_at        DATETIME DEFAULT CURRENT_TIMESTAMP      COMMENT '创建时间',
    completed_at      DATETIME                                COMMENT '完成时间',
    INDEX idx_user_created (user_id, created_at),
    INDEX idx_resume_status (resume_id, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='面试会话表';

-- 2. 面试答案表（从表）
CREATE TABLE interview_answer (
    id                BIGINT PRIMARY KEY AUTO_INCREMENT      COMMENT '自增主键',
    session_id        VARCHAR(36) NOT NULL                    COMMENT '关联会话ID',
    question_index    INT NOT NULL                            COMMENT '题号（从0开始）',
    question          TEXT                                    COMMENT '题目内容',
    category          VARCHAR(50)                             COMMENT '题目类别：基础/框架/项目/算法',
    user_answer       TEXT                                    COMMENT '用户回答',
    score             INT                                     COMMENT 'AI评分（0-100）',
    feedback          TEXT                                    COMMENT 'AI反馈',
    reference_answer  TEXT                                    COMMENT 'AI参考答案',
    key_points_json   TEXT                                    COMMENT '关键点JSON数组',
    answered_at       DATETIME DEFAULT CURRENT_TIMESTAMP      COMMENT '答题时间',
    UNIQUE KEY uk_session_question (session_id, question_index),
    INDEX idx_session (session_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='面试答案表';
