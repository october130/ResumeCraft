<template>
  <div class="interview-report" v-loading="loading">
    <div class="page-header">
      <el-button text @click="$router.push('/interview')">
        <el-icon><ArrowLeft /></el-icon> 返回列表
      </el-button>
      <h2>面试评估报告</h2>
    </div>

    <!-- 基本信息 -->
    <el-card class="info-card" v-if="detail.sessionId">
      <el-descriptions :column="3" border>
        <el-descriptions-item label="技能方向">{{ detail.skillId || '综合' }}</el-descriptions-item>
        <el-descriptions-item label="难度">{{ diffText(detail.difficulty) }}</el-descriptions-item>
        <el-descriptions-item label="题目数">{{ detail.totalQuestions }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="statusTag(detail.status)" size="small">{{ statusText(detail.status) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="评估状态">
          <el-tag :type="evalTag(detail.evaluateStatus)" size="small">{{ evalText(detail.evaluateStatus) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ detail.createdAt?.substring(0, 16).replace('T', ' ') }}</el-descriptions-item>
      </el-descriptions>
    </el-card>

    <!-- 总评分 -->
    <el-card class="score-card" v-if="detail.overallScore != null">
      <template #header><span>🏆 总体评分</span></template>
      <div class="overall-score">
        <el-progress type="circle" :percentage="detail.overallScore" :width="160" :stroke-width="12">
          <span class="big-score">{{ detail.overallScore }}</span>
        </el-progress>
        <div class="overall-feedback">
          <p>{{ detail.overallFeedback }}</p>
        </div>
      </div>
    </el-card>

    <!-- 优势 & 改进 -->
    <el-row :gutter="20" style="margin-top: 20px" v-if="detail.strengths?.length || detail.improvements?.length">
      <el-col :span="12">
        <el-card>
          <template #header><span>💪 优势</span></template>
          <ul v-if="detail.strengths?.length" class="strength-list">
            <li v-for="(s, i) in detail.strengths" :key="i">{{ s }}</li>
          </ul>
          <div v-else class="empty">暂无数据</div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card>
          <template #header><span>📈 改进建议</span></template>
          <ul v-if="detail.improvements?.length" class="improve-list">
            <li v-for="(s, i) in detail.improvements" :key="i">{{ s }}</li>
          </ul>
          <div v-else class="empty">暂无数据</div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 各题详情 -->
    <el-card style="margin-top: 20px" v-if="detail.answers?.length">
      <template #header><span>📝 答题详情</span></template>
      <div v-for="(ans, i) in detail.answers" :key="i" class="answer-detail">
        <div class="answer-header">
          <span class="answer-q-title">第 {{ ans.questionIndex + 1 }} 题：{{ ans.category }}</span>
          <el-tag v-if="ans.score != null" :type="scoreTag(ans.score)" size="small">{{ ans.score }} 分</el-tag>
        </div>
        <div class="answer-q">{{ ans.question }}</div>
        <div class="answer-label">你的回答：</div>
        <div class="answer-text">{{ ans.userAnswer }}</div>
        <div class="answer-label">参考答案：</div>
        <div class="answer-text reference">{{ ans.referenceAnswer }}</div>
        <div class="answer-label">AI 反馈：</div>
        <div class="answer-text feedback">{{ ans.feedback }}</div>
        <el-divider v-if="i < detail.answers.length - 1" />
      </div>
    </el-card>

    <!-- 等待评估 -->
    <el-result v-if="detail.status === 'COMPLETED' && detail.evaluateStatus !== 'COMPLETED' && detail.evaluateStatus !== 'FAILED'"
      icon="info" title="AI 评估中" sub-title="面试已完成，AI正在评估你的回答，请稍后查看">
    </el-result>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { getInterviewDetail } from '../api/interview'

const route = useRoute()
const loading = ref(false)
const detail = ref({})

onMounted(async () => {
  loading.value = true
  try {
    const res = await getInterviewDetail(route.params.sessionId)
    if (res.code === 200) {
      detail.value = res.data
    }
  } finally {
    loading.value = false
  }
})

function diffText(d) {
  return { junior: '初级', mid: '中级', senior: '高级' }[d] || d
}
function statusTag(s) {
  return { CREATED: 'info', IN_PROGRESS: 'warning', COMPLETED: '', EVALUATED: 'success' }[s] || 'info'
}
function statusText(s) {
  return { CREATED: '待开始', IN_PROGRESS: '进行中', COMPLETED: '待评估', EVALUATED: '已完成' }[s] || s
}
function evalTag(s) {
  return { PENDING: 'info', RUNNING: 'warning', COMPLETED: 'success', FAILED: 'danger' }[s] || 'info'
}
function evalText(s) {
  return { PENDING: '待评估', RUNNING: '评估中', COMPLETED: '已完成', FAILED: '失败' }[s] || s
}
function scoreTag(s) {
  if (s >= 80) return 'success'
  if (s >= 60) return 'warning'
  return 'danger'
}
</script>

<style scoped>
.page-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 20px;
}
.page-header h2 { flex: 1; margin: 0; }
.info-card { margin-bottom: 20px; }
.overall-score {
  display: flex;
  gap: 40px;
  align-items: center;
}
.big-score { font-size: 32px; font-weight: bold; }
.overall-feedback {
  flex: 1;
  font-size: 15px;
  line-height: 1.6;
  color: #606266;
}
.strength-list li, .improve-list li {
  margin-bottom: 8px;
  line-height: 1.5;
}
.answer-detail { margin-bottom: 8px; }
.answer-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}
.answer-q-title { font-weight: bold; color: #303133; }
.answer-q {
  padding: 8px 12px;
  background: #f5f7fa;
  border-radius: 6px;
  margin-bottom: 12px;
  line-height: 1.5;
}
.answer-label {
  font-size: 13px;
  color: #909399;
  margin-bottom: 4px;
}
.answer-text {
  padding: 8px 12px;
  background: #f5f7fa;
  border-radius: 6px;
  margin-bottom: 12px;
  line-height: 1.5;
  white-space: pre-wrap;
}
.answer-text.reference { background: #ecf5ff; }
.answer-text.feedback { background: #f0f9eb; }
.empty { color: #909399; text-align: center; padding: 20px; }
</style>
