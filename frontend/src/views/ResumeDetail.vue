<template>
  <div class="resume-detail" v-loading="loading">
    <div class="page-header">
      <el-button text @click="$router.push('/resume')">
        <el-icon><ArrowLeft /></el-icon> 返回
      </el-button>
      <h2>简历分析报告</h2>
      <div>
        <el-button type="primary" @click="handleExport">导出 PDF</el-button>
      </div>
    </div>

    <!-- 基本信息 -->
    <el-card class="info-card" v-if="detail.resumeInfo">
      <div class="resume-info">
        <div><strong>文件名：</strong>{{ detail.resumeInfo.fileName }}</div>
        <div><strong>类型：</strong>{{ detail.resumeInfo.fileType?.toUpperCase() }}</div>
        <div><strong>上传时间：</strong>{{ detail.resumeInfo.uploadTime }}</div>
        <el-button v-if="detail.resumeInfo.resumeText" text type="primary" @click="showText = !showText">
          {{ showText ? '收起原文' : '查看原文' }}
        </el-button>
      </div>
      <el-collapse-transition>
        <pre v-if="showText" class="resume-text">{{ detail.resumeInfo.resumeText }}</pre>
      </el-collapse-transition>
    </el-card>

    <!-- 评分概览 -->
    <el-card class="score-card" v-if="latestEval">
      <template #header><span>AI 评分概览 · 总分 {{ latestEval.overallScore }}</span></template>
      <el-row :gutter="40">
        <el-col :span="14">
          <ResumeRadar :scores="latestEval" />
        </el-col>
        <el-col :span="10">
          <div class="score-summary">
            <div v-if="latestEval.summary" class="summary-text">{{ latestEval.summary }}</div>
            <div class="score-details-list">
              <div class="score-line"><span>内容完整性</span><strong>{{ latestEval.contentScore }}/25</strong></div>
              <div class="score-line"><span>结构清晰度</span><strong>{{ latestEval.structureScore }}/20</strong></div>
              <div class="score-line"><span>技能匹配度</span><strong>{{ latestEval.skillMatchScore }}/25</strong></div>
              <div class="score-line"><span>表达专业性</span><strong>{{ latestEval.expressionScore }}/15</strong></div>
              <div class="score-line"><span>项目经验</span><strong>{{ latestEval.projectScore }}/15</strong></div>
            </div>
          </div>
        </el-col>
      </el-row>
    </el-card>

    <!-- 优点 & 建议 -->
    <el-row :gutter="20" style="margin-top: 20px" v-if="latestEval">
      <el-col :span="12">
        <el-card>
          <template #header><span>💪 优点</span></template>
          <ul v-if="latestEval.strengths?.length" class="strength-list">
            <li v-for="(s, i) in latestEval.strengths" :key="i">{{ s }}</li>
          </ul>
          <div v-else class="empty">暂无数据</div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card>
          <template #header><span>📈 改进建议</span></template>
          <div v-if="latestEval.suggestions?.length">
            <div v-for="(s, i) in latestEval.suggestions" :key="i" class="suggestion-item">
              <el-tag :type="priorityTag(s.priority)" size="small">{{ s.category }}</el-tag>
              <div class="suggestion-text">{{ s.issue }}</div>
              <div class="suggestion-rec">{{ s.recommendation }}</div>
            </div>
          </div>
          <div v-else class="empty">暂无数据</div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 历史评分 -->
    <el-card style="margin-top: 20px" v-if="detail.evaluations?.length > 1">
      <template #header><span>历史评分记录</span></template>
      <div v-for="evalItem in detail.evaluations" :key="evalItem.evaluationId" class="history-item">
        <span>{{ formatTime(evalItem.analyzedAt) }}：</span>
        <el-tag :type="scoreTag(evalItem.overallScore)" size="small">{{ evalItem.overallScore }} 分</el-tag>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getResumeDetail, exportPdf } from '../api/resume'
import ResumeRadar from '../components/ResumeRadar.vue'

const route = useRoute()
const loading = ref(false)
const detail = ref({})
const showText = ref(false)

const latestEval = computed(() => {
  const evals = detail.value.evaluations
  return evals?.length ? evals[0] : null
})

onMounted(async () => {
  loading.value = true
  try {
    const res = await getResumeDetail(route.params.id)
    if (res.code === 200) {
      detail.value = res.data
    }
  } finally {
    loading.value = false
  }
})

async function handleExport() {
  try {
    const res = await exportPdf(route.params.id)
    const url = window.URL.createObjectURL(new Blob([res]))
    const link = document.createElement('a')
    link.href = url
    link.download = '简历分析报告.pdf'
    link.click()
    URL.revokeObjectURL(url)
    ElMessage.success('导出成功')
  } catch {
    ElMessage.error('导出失败')
  }
}

function priorityTag(p) {
  return { HIGH: 'danger', MEDIUM: 'warning', LOW: 'info' }[p] || 'info'
}

function scoreTag(s) {
  if (s >= 80) return 'success'
  if (s >= 60) return 'warning'
  return 'danger'
}

function formatTime(t) {
  return t?.substring(0, 16).replace('T', ' ') || ''
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
.resume-info {
  display: flex;
  gap: 30px;
  align-items: center;
  flex-wrap: wrap;
}
.resume-text {
  margin-top: 12px;
  padding: 12px;
  background: #f5f7fa;
  border-radius: 6px;
  font-size: 13px;
  line-height: 1.6;
  max-height: 300px;
  overflow-y: auto;
  white-space: pre-wrap;
}
.score-summary { padding: 12px 0; }
.summary-text {
  font-size: 14px; color: #606266; line-height: 1.6;
  margin-bottom: 16px; padding: 12px; background: #f5f7fa; border-radius: 8px;
}
.score-details-list { display: flex; flex-direction: column; gap: 10px; }
.score-line {
  display: flex; justify-content: space-between; align-items: center;
  padding: 8px 12px; background: #fafafa; border-radius: 6px; font-size: 14px;
}
.score-line span { color: #606266; }
.score-line strong { color: #409eff; font-size: 16px; }
.strength-list li { margin-bottom: 8px; line-height: 1.5; }
.suggestion-item {
  padding: 10px 0;
  border-bottom: 1px solid #f0f0f0;
}
.suggestion-item:last-child { border-bottom: none; }
.suggestion-text { margin: 4px 0; color: #303133; }
.suggestion-rec { color: #909399; font-size: 13px; }
.empty { color: #909399; text-align: center; padding: 20px; }
.history-item {
  display: inline-block;
  margin-right: 20px;
  margin-bottom: 8px;
}
</style>
