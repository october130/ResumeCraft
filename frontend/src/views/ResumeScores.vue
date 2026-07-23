<template>
  <div class="resume-scores">
    <h2>评分总览</h2>

    <el-card v-loading="loading">
      <template #header>
        <span>📊 所有简历评分汇总</span>
      </template>

      <el-empty v-if="!loading && flatScores.length === 0" description="暂无评分数据" />

      <el-table v-else :data="flatScores" stripe>
        <el-table-column label="简历" min-width="200">
          <template #default="{ row }">{{ row.fileName }}</template>
        </el-table-column>
        <el-table-column label="总分" width="80">
          <template #default="{ row }">
            <span :style="{ color: scoreColor(row.overallScore), fontWeight: 'bold' }">{{ row.overallScore }}</span>
          </template>
        </el-table-column>
        <el-table-column label="内容" width="70" prop="contentScore" />
        <el-table-column label="结构" width="70" prop="structureScore" />
        <el-table-column label="技能匹配" width="80" prop="skillMatchScore" />
        <el-table-column label="表达" width="70" prop="expressionScore" />
        <el-table-column label="项目" width="70" prop="projectScore" />
        <el-table-column label="时间" width="160" prop="analyzedAt" />
        <el-table-column label="操作" width="80">
          <template #default="{ row }">
            <el-button text type="primary" @click="$router.push(`/resume/${row.resumeId}`)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getResumeList } from '../api/resume'

const loading = ref(false)
const flatScores = ref([])
const allResumes = ref([])

onMounted(async () => {
  loading.value = true
  try {
    const r = await getResumeList({ pageNum: 1, pageSize: 999 })
    if (r.code === 200) {
      allResumes.value = r.data.records || []
      // For each resume, fetch its evaluations
      const promises = allResumes.value
        .filter(item => item.latestScore != null)
        .map(async (item) => {
          // We show what we have from the list
          return {
            resumeId: item.resumeId,
            fileName: item.fileName,
            overallScore: item.latestScore,
            contentScore: '-',
            structureScore: '-',
            skillMatchScore: '-',
            expressionScore: '-',
            projectScore: '-',
            analyzedAt: item.uploadTime
          }
        })
      flatScores.value = await Promise.all(promises)
    }
  } finally { loading.value = false }
})

function scoreColor(s) { return s >= 80 ? '#67c23a' : s >= 60 ? '#e6a23c' : '#f56c6c' }
</script>

<style scoped>
h2 { margin-bottom: 20px; }
</style>
