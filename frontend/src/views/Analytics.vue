<template>
  <div class="analytics">
    <h2>数据分析</h2>

    <!-- 概览卡片 -->
    <el-row :gutter="20">
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card"><div class="stat-inner">
          <div class="stat-icon" style="background: rgba(64,158,255,0.1)"><el-icon :size="24" color="#409eff"><Document /></el-icon></div>
          <div><div class="stat-num">{{ totalResumes }}</div><div class="stat-label">简历总数</div></div>
        </div></el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card"><div class="stat-inner">
          <div class="stat-icon" style="background: rgba(103,194,58,0.1)"><el-icon :size="24" color="#67c23a"><ChatDotSquare /></el-icon></div>
          <div><div class="stat-num">{{ totalInterviews }}</div><div class="stat-label">面试总数</div></div>
        </div></el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card"><div class="stat-inner">
          <div class="stat-icon" style="background: rgba(230,162,60,0.1)"><el-icon :size="24" color="#e6a23c"><CircleCheck /></el-icon></div>
          <div><div class="stat-num">{{ completedEval }}</div><div class="stat-label">已完成评估</div></div>
        </div></el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card"><div class="stat-inner">
          <div class="stat-icon" style="background: rgba(64,158,255,0.1)"><el-icon :size="24" color="#409eff"><TrendCharts /></el-icon></div>
          <div><div class="stat-num">{{ avgScore || '-' }}</div><div class="stat-label">平均评分</div></div>
        </div></el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header><span>🎯 面试状态分布</span></template>
          <RingChart :data="statusData" v-if="statusData.length" />
          <div v-else class="empty">暂无数据</div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header><span>📊 简历评分分布</span></template>
          <RingChart :data="scoreDistData" v-if="scoreDistData.length" />
          <div v-else class="empty">暂无数据</div>
        </el-card>
      </el-col>
    </el-row>

    <el-card style="margin-top: 20px">
      <template #header><span>🗂️ 技能方向统计</span></template>
      <el-row :gutter="20">
        <el-col :span="8" v-for="s in skillStats" :key="s.name" style="margin-bottom: 16px">
          <div class="skill-item">
            <span class="skill-name">{{ s.name }}</span>
            <span class="skill-count">{{ s.count }} 场面试</span>
          </div>
        </el-col>
        <div v-if="!skillStats.length" class="empty" style="padding: 20px">暂无数据</div>
      </el-row>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import RingChart from '../components/RingChart.vue'
import { getResumeList } from '../api/resume'
import { getInterviewList } from '../api/interview'

const totalResumes = ref(0)
const totalInterviews = ref(0)
const completedEval = ref(0)
const avgScore = ref(0)
const statusData = ref([])
const scoreDistData = ref([])
const skillStats = ref([])

onMounted(async () => {
  try {
    const r = await getResumeList({ pageNum: 1, pageSize: 999 })
    if (r.code === 200) {
      const records = r.data.records || []
      totalResumes.value = r.data.total || records.length

      const ranges = { '优秀 (80+)': 0, '良好 (60-79)': 0, '一般 (<60)': 0 }
      records.forEach(item => {
        const s = item.latestScore
        if (s >= 80) ranges['优秀 (80+)']++
        else if (s >= 60) ranges['良好 (60-79)']++
        else if (s != null) ranges['一般 (<60)']++
      })
      scoreDistData.value = Object.entries(ranges).map(([name, value]) => ({ name, value })).filter(d => d.value > 0)
    }
  } catch {}

  try {
    const i = await getInterviewList()
    if (i.code === 200 && Array.isArray(i.data)) {
      totalInterviews.value = i.data.length
      completedEval.value = i.data.filter(x => x.evaluateStatus === 'COMPLETED').length

      const scored = i.data.filter(x => x.overallScore != null)
      avgScore.value = scored.length ? Math.round(scored.reduce((a, b) => a + b.overallScore, 0) / scored.length) : 0

      const sMap = { CREATED: '待开始', IN_PROGRESS: '进行中', COMPLETED: '待评估', EVALUATED: '已完成' }
      const counts = {}
      i.data.forEach(x => {
        const key = sMap[x.status] || x.status
        counts[key] = (counts[key] || 0) + 1
      })
      statusData.value = Object.entries(counts).map(([name, value]) => ({ name, value }))

      // 技能统计
      const skillMap = {}
      i.data.forEach(x => {
        const label = { 'java-backend': 'Java 后端', frontend: '前端开发', python: 'Python', go: 'Go 开发', algorithm: '算法' }[x.skillId] || x.skillId || '综合'
        skillMap[label] = (skillMap[label] || 0) + 1
      })
      skillStats.value = Object.entries(skillMap).map(([name, count]) => ({ name, count }))
    }
  } catch {}
})
</script>

<style scoped>
h2 { margin-bottom: 20px; }
.stat-card { cursor: default; }
.stat-inner { display: flex; align-items: center; gap: 12px; }
.stat-icon { width: 44px; height: 44px; border-radius: 10px; display: flex; align-items: center; justify-content: center; }
.stat-num { font-size: 24px; font-weight: bold; color: #303133; }
.stat-label { font-size: 13px; color: #909399; margin-top: 2px; }
.skill-item { display: flex; justify-content: space-between; align-items: center; padding: 10px 12px; background: #f5f7fa; border-radius: 8px; }
.skill-name { color: #303133; font-weight: 500; }
.skill-count { color: #909399; font-size: 13px; }
.empty { color: #909399; text-align: center; padding: 40px; }
</style>
