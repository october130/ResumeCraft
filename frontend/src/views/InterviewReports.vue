<template>
  <div class="interview-reports">
    <h2>评估报告</h2>

    <el-card v-loading="loading">
      <el-empty v-if="!loading && completedList.length === 0" description="暂无已完成评估的面试" />

      <el-table v-else :data="completedList" stripe @row-click="goReport">
        <el-table-column label="技能方向" min-width="140">
          <template #default="{ row }">{{ skillLabel(row.skillId) }}</template>
        </el-table-column>
        <el-table-column label="难度" width="80">
          <template #default="{ row }"><el-tag size="small">{{ diffLabel(row.difficulty) }}</el-tag></template>
        </el-table-column>
        <el-table-column label="题数" width="60" prop="totalQuestions" />
        <el-table-column label="评分" width="80">
          <template #default="{ row }">
            <span v-if="row.overallScore != null" :style="{ color: scoreColor(row.overallScore), fontWeight: 'bold', fontSize: '16px' }">{{ row.overallScore }}</span>
            <span v-else style="color: #909399">-</span>
          </template>
        </el-table-column>
        <el-table-column label="评估状态" width="100">
          <template #default="{ row }">
            <el-tag :type="evalTag(row.evaluationStatus)" size="small">{{ evalText(row.evaluationStatus) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="完成时间" width="170" prop="completedAt" />
        <el-table-column label="操作" width="80">
          <template #default="{ row }">
            <el-button text type="primary" @click.stop="goReport(row)">查看</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getInterviewList } from '../api/interview'

const router = useRouter()
const loading = ref(false)
const completedList = ref([])

onMounted(async () => {
  loading.value = true
  try {
    const res = await getInterviewList()
    if (res.code === 200 && Array.isArray(res.data)) {
      completedList.value = res.data.filter(i => i.status === 'EVALUATED' || i.evaluateStatus === 'COMPLETED')
    }
  } finally { loading.value = false }
})

function goReport(row) { router.push(`/interview/report/${row.sessionId}`) }
function skillLabel(id) { return { 'java-backend': 'Java 后端', frontend: '前端开发', python: 'Python', go: 'Go 开发', algorithm: '算法' }[id] || id || '综合' }
function diffLabel(d) { return { junior: '初级', mid: '中级', senior: '高级' }[d] || d }
function evalTag(s) { return { PENDING: 'info', RUNNING: 'warning', COMPLETED: 'success', FAILED: 'danger' }[s] || 'info' }
function evalText(s) { return { PENDING: '待评估', RUNNING: '评估中', COMPLETED: '已完成', FAILED: '失败' }[s] || s }
function scoreColor(s) { return s >= 80 ? '#67c23a' : s >= 60 ? '#e6a23c' : '#f56c6c' }
</script>

<style scoped>
h2 { margin-bottom: 20px; }
</style>
