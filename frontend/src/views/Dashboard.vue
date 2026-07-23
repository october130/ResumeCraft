<template>
  <div class="dashboard">
    <!-- 欢迎横幅 -->
    <el-card class="welcome-banner" shadow="never">
      <div class="welcome-content">
        <div class="welcome-text">
          <h2>欢迎回来，{{ userStore.username || '用户' }} 👋</h2>
          <p>今天又是充满可能的一天，让我们开始工作吧！</p>
        </div>
        <div class="welcome-actions">
          <el-button type="primary" size="large" @click="$router.push('/resume/upload')">
            <el-icon><Upload /></el-icon>上传简历
          </el-button>
          <el-button type="success" size="large" @click="$router.push('/interview/create')">
            <el-icon><ChatDotSquare /></el-icon>开始面试
          </el-button>
        </div>
      </div>
    </el-card>

    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :span="6">
        <el-card class="stat-card blue" shadow="hover">
          <div class="stat-icon"><el-icon :size="28"><Document /></el-icon></div>
          <div class="stat-info">
            <div class="stat-num">{{ resumeCount }}</div>
            <div class="stat-label">简历总数</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card green" shadow="hover">
          <div class="stat-icon"><el-icon :size="28"><ChatDotSquare /></el-icon></div>
          <div class="stat-info">
            <div class="stat-num">{{ interviewCount }}</div>
            <div class="stat-label">面试次数</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card orange" shadow="hover">
          <div class="stat-icon"><el-icon :size="28"><CircleCheck /></el-icon></div>
          <div class="stat-info">
            <div class="stat-num">{{ evaluatedCount }}</div>
            <div class="stat-label">已完成评估</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card purple" shadow="hover">
          <div class="stat-icon"><el-icon :size="28"><TrendCharts /></el-icon></div>
          <div class="stat-info">
            <div class="stat-num">{{ avgScore || '-' }}</div>
            <div class="stat-label">平均评分</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 图表区域 -->
    <el-row :gutter="20" class="chart-row">
      <el-col :span="12">
        <el-card shadow="never" class="chart-card">
          <template #header>
            <div class="card-header">
              <span class="header-title">📊 面试状态分布</span>
              <el-tag size="small" type="info">全部</el-tag>
            </div>
          </template>
          <RingChart :data="interviewStats" v-if="interviewStats.length" />
          <div v-else class="empty-state">
            <el-empty description="暂无面试数据" />
          </div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="never" class="chart-card">
          <template #header>
            <div class="card-header">
              <span class="header-title">⚡ 快捷入口</span>
            </div>
          </template>
          <div class="quick-grid">
            <div class="quick-item" @click="$router.push('/resume')">
              <div class="quick-icon" style="background: linear-gradient(135deg, #409eff 0%, #66b1ff 100%)">
                <el-icon :size="24"><List /></el-icon>
              </div>
              <span>全部简历</span>
            </div>
            <div class="quick-item" @click="$router.push('/interview')">
              <div class="quick-icon" style="background: linear-gradient(135deg, #67c23a 0%, #95d475 100%)">
                <el-icon :size="24"><ChatDotSquare /></el-icon>
              </div>
              <span>面试记录</span>
            </div>
            <div class="quick-item" @click="$router.push('/interview/reports')">
              <div class="quick-icon" style="background: linear-gradient(135deg, #e6a23c 0%, #eebe77 100%)">
                <el-icon :size="24"><DocumentChecked /></el-icon>
              </div>
              <span>评估报告</span>
            </div>
            <div class="quick-item" @click="$router.push('/analytics')">
              <div class="quick-icon" style="background: linear-gradient(135deg, #f56c6c 0%, #f89898 100%)">
                <el-icon :size="24"><DataAnalysis /></el-icon>
              </div>
              <span>数据分析</span>
            </div>
            <div class="quick-item" @click="$router.push('/knowledge')">
              <div class="quick-icon" style="background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%)">
                <el-icon :size="24"><Cpu /></el-icon>
              </div>
              <span>知识库</span>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 最近面试 -->
    <el-card shadow="never" class="recent-card">
      <template #header>
        <div class="card-header">
          <span class="header-title">📋 最近面试</span>
          <el-button text type="primary" @click="$router.push('/interview')">
            查看全部 <el-icon><ArrowRight /></el-icon>
          </el-button>
        </div>
      </template>
      <el-table
        v-if="recentInterviews.length"
        :data="recentInterviews"
        stripe
        @row-click="goInterview"
        class="recent-table"
      >
        <el-table-column label="技能方向">
          <template #default="{ row }">
            <div class="skill-cell">
              <div class="skill-dot"></div>
              {{ skillLabel(row.skillId) }}
            </div>
          </template>
        </el-table-column>
        <el-table-column label="难度" width="100">
          <template #default="{ row }">
            <el-tag size="small" effect="plain">{{ diffLabel(row.difficulty) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="题数" width="80" prop="totalQuestions" />
        <el-table-column label="状态" width="120">
          <template #default="{ row }">
            <el-tag :type="statusTag(row.status)" size="small" effect="dark">
              {{ statusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="评分" width="100">
          <template #default="{ row }">
            <div v-if="row.overallScore != null" class="score-cell" :class="scoreClass(row.overallScore)">
              {{ row.overallScore }}
            </div>
            <span v-else class="no-score">-</span>
          </template>
        </el-table-column>
        <el-table-column label="时间" width="170" prop="createdAt" />
      </el-table>
      <div v-else class="empty-state">
        <el-empty description="暂无面试记录" />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../store/user'
import RingChart from '../components/RingChart.vue'
import { getResumeList } from '../api/resume'
import { getInterviewList } from '../api/interview'

const router = useRouter()
const userStore = useUserStore()
const resumeCount = ref(0)
const interviewCount = ref(0)
const evaluatedCount = ref(0)
const avgScore = ref(0)
const recentInterviews = ref([])
const interviewStats = ref([])

onMounted(async () => {
  try {
    const res = await getResumeList({ pageNum: 1, pageSize: 999 })
    if (res.code === 200) resumeCount.value = res.data.total || res.data.records?.length || 0
  } catch {}

  try {
    const res = await getInterviewList()
    if (res.code === 200 && Array.isArray(res.data)) {
      const list = res.data
      interviewCount.value = list.length
      evaluatedCount.value = list.filter(i => i.evaluateStatus === 'COMPLETED').length
      recentInterviews.value = list.slice(0, 8)

      const scored = list.filter(i => i.overallScore != null)
      avgScore.value = scored.length ? Math.round(scored.reduce((a, b) => a + b.overallScore, 0) / scored.length) : 0

      const statusMap = {}
      list.forEach(i => {
        const key = statusText(i.status)
        statusMap[key] = (statusMap[key] || 0) + 1
      })
      interviewStats.value = Object.entries(statusMap).map(([name, value]) => ({ name, value }))
    }
  } catch {}
})

function skillLabel(id) {
  const map = { 'java-backend': 'Java 后端', frontend: '前端开发', python: 'Python', go: 'Go 开发', algorithm: '数据结构与算法' }
  return map[id] || id || '综合'
}
function diffLabel(d) {
  return { junior: '初级', mid: '中级', senior: '高级' }[d] || d
}
function statusTag(s) {
  return { CREATED: 'info', IN_PROGRESS: 'warning', COMPLETED: 'success', EVALUATED: 'success' }[s] || 'info'
}
function statusText(s) {
  return { CREATED: '待开始', IN_PROGRESS: '进行中', COMPLETED: '待评估', EVALUATED: '已完成' }[s] || s
}
function scoreClass(s) {
  if (s >= 80) return 'score-excellent'
  if (s >= 60) return 'score-good'
  return 'score-normal'
}
function goInterview(row) {
  router.push(`/interview/report/${row.sessionId}`)
}
</script>

<style scoped>
.dashboard {
  max-width: 1400px;
  margin: 0 auto;
}

.welcome-banner {
  margin-bottom: 24px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  border-radius: 12px;
  color: #fff;
}

.welcome-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 24px;
}

.welcome-text h2 {
  margin: 0 0 8px 0;
  font-size: 24px;
  font-weight: 600;
}

.welcome-text p {
  margin: 0;
  opacity: 0.9;
  font-size: 14px;
}

.welcome-actions {
  display: flex;
  gap: 12px;
}

.welcome-actions .el-button {
  background: rgba(255,255,255,0.2);
  border: 1px solid rgba(255,255,255,0.3);
  color: #fff;
  backdrop-filter: blur(10px);
}

.welcome-actions .el-button:hover {
  background: rgba(255,255,255,0.3);
}

.stats-row {
  margin-bottom: 24px;
}

.stat-card {
  border-radius: 12px;
  transition: transform 0.2s;
}

.stat-card:hover {
  transform: translateY(-4px);
}

.stat-card :deep(.el-card__body) {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px;
}

.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.stat-card.blue .stat-icon { background: rgba(64,158,255,0.1); color: #409eff; }
.stat-card.green .stat-icon { background: rgba(103,194,58,0.1); color: #67c23a; }
.stat-card.orange .stat-icon { background: rgba(230,162,60,0.1); color: #e6a23c; }
.stat-card.purple .stat-icon { background: rgba(144,147,153,0.1); color: #909399; }

.stat-num {
  font-size: 28px;
  font-weight: 700;
  color: #303133;
  line-height: 1;
}

.stat-label {
  font-size: 13px;
  color: #909399;
  margin-top: 4px;
}

.chart-row {
  margin-bottom: 24px;
}

.chart-card {
  border-radius: 12px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.quick-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(120px, 1fr));
  gap: 16px;
  padding: 8px 0;
}

.quick-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  padding: 20px;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.2s;
  background: #f8f9fa;
}

.quick-item:hover {
  background: #f0f2f5;
  transform: translateY(-2px);
}

.quick-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
}

.quick-item span {
  font-size: 13px;
  color: #606266;
  font-weight: 500;
}

.recent-card {
  border-radius: 12px;
}

.recent-table {
  border-radius: 8px;
  overflow: hidden;
}

.skill-cell {
  display: flex;
  align-items: center;
  gap: 8px;
}

.skill-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.score-cell {
  font-weight: 700;
  font-size: 16px;
}

.score-excellent { color: #67c23a; }
.score-good { color: #e6a23c; }
.score-normal { color: #f56c6c; }

.no-score {
  color: #c0c4cc;
  font-size: 14px;
}

.empty-state {
  padding: 40px 0;
}

:deep(.el-table) {
  border-radius: 8px;
  overflow: hidden;
}

:deep(.el-table th) {
  background: #f5f7fa;
  font-weight: 600;
  color: #606266;
}

:deep(.el-table--striped .el-table__body tr.el-table__row--striped td) {
  background: #fafafa;
}
</style>
