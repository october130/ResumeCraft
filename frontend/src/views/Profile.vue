<template>
  <div class="profile">
    <h2>个人中心</h2>

    <el-row :gutter="20">
      <el-col :span="8">
        <el-card shadow="hover" class="user-card">
          <div class="avatar-section">
            <el-avatar :size="80" icon="UserFilled" />
            <div class="user-name">{{ userStore.username || '用户' }}</div>
            <div class="user-role">普通用户</div>
          </div>
          <el-divider />
          <div class="info-row"><span>注册时间</span><span>{{ registerTime }}</span></div>
        </el-card>
      </el-col>

      <el-col :span="16">
        <el-card shadow="hover">
          <template #header><span>📊 数据概览</span></template>
          <el-row :gutter="20">
            <el-col :span="8">
              <div class="overview-item">
                <div class="ov-num">{{ resumeCount }}</div>
                <div class="ov-label">简历</div>
              </div>
            </el-col>
            <el-col :span="8">
              <div class="overview-item">
                <div class="ov-num">{{ interviewCount }}</div>
                <div class="ov-label">面试</div>
              </div>
            </el-col>
            <el-col :span="8">
              <div class="overview-item">
                <div class="ov-num">{{ completedCount }}</div>
                <div class="ov-label">已完成评估</div>
              </div>
            </el-col>
          </el-row>
        </el-card>

        <el-card shadow="hover" style="margin-top: 20px">
          <template #header><span>📈 简历评分分布</span></template>
          <RingChart :data="scoreStats" v-if="scoreStats.length" />
          <div v-else class="empty">暂无评分数据，上传简历后这里会有图表展示</div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import RingChart from '../components/RingChart.vue'
import { useUserStore } from '../store/user'
import { getResumeList } from '../api/resume'
import { getInterviewList } from '../api/interview'

const userStore = useUserStore()
const resumeCount = ref(0)
const interviewCount = ref(0)
const completedCount = ref(0)
const registerTime = ref('')
const scoreStats = ref([])

onMounted(async () => {
  try {
    const r = await getResumeList({ pageNum: 1, pageSize: 999 })
    if (r.code === 200) {
      const records = r.data.records || []
      resumeCount.value = records.length

      // 评分分布
      const ranges = { '优秀 (80+)': 0, '良好 (60-79)': 0, '一般 (<60)': 0 }
      records.forEach(item => {
        const s = item.latestScore
        if (s >= 80) ranges['优秀 (80+)']++
        else if (s >= 60) ranges['良好 (60-79)']++
        else if (s != null) ranges['一般 (<60)']++
      })
      scoreStats.value = Object.entries(ranges).map(([name, value]) => ({ name, value })).filter(d => d.value > 0)
    }
  } catch {}

  try {
    const i = await getInterviewList()
    if (i.code === 200 && Array.isArray(i.data)) {
      interviewCount.value = i.data.length
      completedCount.value = i.data.filter(x => x.evaluateStatus === 'COMPLETED').length
    }
  } catch {}
})
</script>

<style scoped>
.profile h2 { margin-bottom: 20px; }
.user-card { text-align: center; }
.avatar-section { padding: 20px 0; }
.user-name { margin-top: 12px; font-size: 18px; font-weight: bold; color: #303133; }
.user-role { font-size: 13px; color: #909399; margin-top: 4px; }
.info-row { display: flex; justify-content: space-between; color: #606266; font-size: 14px; padding: 4px 0; }
.overview-item { text-align: center; padding: 20px 0; }
.ov-num { font-size: 36px; font-weight: bold; color: #409eff; }
.ov-label { font-size: 14px; color: #909399; margin-top: 4px; }
.empty { color: #909399; text-align: center; padding: 40px; }
</style>
