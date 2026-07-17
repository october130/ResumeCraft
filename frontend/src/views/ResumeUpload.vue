<template>
  <div class="resume-upload">
    <h2>上传简历</h2>
    <el-card class="upload-card">
      <el-upload
        drag
        :show-file-list="false"
        :before-upload="handleUpload"
        accept=".pdf,.docx"
        class="upload-area"
      >
        <el-icon :size="48" color="#409eff"><UploadFilled /></el-icon>
        <div class="upload-text">将简历拖拽到此处，或 <em>点击上传</em></div>
        <template #tip>
          <div class="upload-tip">支持 PDF、DOCX 格式，文件不超过 10MB</div>
        </template>
      </el-upload>
    </el-card>

    <el-card style="margin-top: 20px">
      <template #header><span>📋 上传记录</span></template>
      <el-table :data="recentUploads" v-loading="loading" stripe v-if="recentUploads.length">
        <el-table-column prop="fileName" label="文件名" min-width="200" />
        <el-table-column prop="fileType" label="类型" width="80">
          <template #default="{ row }">
            <el-tag :type="row.fileType === 'pdf' ? 'danger' : 'primary'" size="small">{{ row.fileType?.toUpperCase() }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="parseTagType(row.parseStatus)" size="small">{{ parseText(row.parseStatus) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="评分" width="80">
          <template #default="{ row }">
            <span v-if="row.latestScore" :style="{ color: scoreColor(row.latestScore) }">{{ row.latestScore }}</span>
            <span v-else style="color: #909399">-</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100">
          <template #default="{ row }">
            <el-button text type="primary" @click="$router.push(`/resume/${row.resumeId}`)">查看</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div v-else class="empty">还没有上传过简历</div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { uploadResume, getResumeList } from '../api/resume'

const router = useRouter()
const loading = ref(false)
const recentUploads = ref([])

onMounted(async () => {
  loading.value = true
  try {
    const res = await getResumeList({ pageNum: 1, pageSize: 10 })
    if (res.code === 200) recentUploads.value = res.data.records || []
  } finally { loading.value = false }
})

async function handleUpload(file) {
  try {
    const res = await uploadResume(file)
    if (res.code === 200) {
      ElMessage.success('上传成功，正在解析...')
      router.push(`/resume/${res.data.resumeId}`)
    }
  } catch {}
  return false
}

function parseTagType(s) { return { 0: 'info', 1: 'warning', 2: 'success', 3: 'danger' }[s] || 'info' }
function parseText(s) { return { 0: '待解析', 1: '解析中', 2: '已完成', 3: '失败' }[s] || '未知' }
function scoreColor(s) { return s >= 80 ? '#67c23a' : s >= 60 ? '#e6a23c' : '#f56c6c' }
</script>

<style scoped>
h2 { margin-bottom: 20px; }
.upload-card { padding: 20px; }
.upload-area { display: flex; flex-direction: column; align-items: center; gap: 12px; padding: 40px; }
.upload-text { font-size: 14px; color: #606266; }
.upload-text em { color: #409eff; font-style: normal; font-weight: 500; }
.upload-tip { font-size: 12px; color: #909399; margin-top: 8px; }
.empty { color: #909399; text-align: center; padding: 40px; }
</style>
