<template>
  <div class="resume-list">
    <div class="page-header">
      <h2>简历管理</h2>
      <el-upload
        :show-file-list="false"
        :before-upload="handleUpload"
        accept=".pdf,.docx"
      >
        <el-button type="primary" :loading="uploading">
          <el-icon><Upload /></el-icon> 上传简历
        </el-button>
      </el-upload>
    </div>

    <el-table :data="list" v-loading="loading" stripe style="width: 100%">
      <el-table-column prop="fileName" label="文件名" min-width="200" />
      <el-table-column prop="fileType" label="类型" width="80">
        <template #default="{ row }">
          <el-tag :type="row.fileType === 'pdf' ? 'danger' : 'primary'" size="small">
            {{ row.fileType?.toUpperCase() }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="fileSize" label="大小" width="100">
        <template #default="{ row }">{{ (row.fileSize / 1024).toFixed(1) }} KB</template>
      </el-table-column>
      <el-table-column prop="parseStatus" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="parseTagType(row.parseStatus)" size="small">
            {{ parseText(row.parseStatus) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="latestScore" label="最新评分" width="100">
        <template #default="{ row }">
          <span v-if="row.latestScore" :style="{ color: scoreColor(row.latestScore) }">{{ row.latestScore }}</span>
          <span v-else style="color: #909399">-</span>
        </template>
      </el-table-column>
      <el-table-column prop="uploadTime" label="上传时间" width="180" />
      <el-table-column label="操作" width="240" fixed="right">
        <template #default="{ row }">
          <el-button text type="primary" @click="$router.push(`/resume/${row.resumeId}`)">详情</el-button>
          <el-button text type="danger" @click="handleDelete(row.resumeId)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination
      v-if="total > 0"
      v-model:current-page="pageNum"
      v-model:page-size="pageSize"
      :total="total"
      layout="prev, pager, next"
      style="margin-top: 20px; justify-content: flex-end"
      @current-change="fetchList"
    />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getResumeList, deleteResume, uploadResume } from '../api/resume'

const list = ref([])
const loading = ref(false)
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const uploading = ref(false)

onMounted(() => fetchList())

async function fetchList() {
  loading.value = true
  try {
    const res = await getResumeList({ pageNum: pageNum.value, pageSize: pageSize.value })
    if (res.code === 200) {
      list.value = res.data.records || []
      total.value = res.data.total || 0
    }
  } finally {
    loading.value = false
  }
}

async function handleUpload(file) {
  uploading.value = true
  try {
    const res = await uploadResume(file)
    if (res.code === 200) {
      ElMessage.success('上传成功，正在解析...')
      fetchList()
    }
  } finally {
    uploading.value = false
  }
  return false // 阻止默认上传
}

async function handleDelete(id) {
  await ElMessageBox.confirm('确定删除该简历吗？', '提示')
  const res = await deleteResume(id)
  if (res.code === 200) {
    ElMessage.success('删除成功')
    fetchList()
  }
}

function parseTagType(status) {
  return { 0: 'info', 1: 'warning', 2: 'success', 3: 'danger' }[status] || 'info'
}

function parseText(status) {
  return { 0: '待解析', 1: '解析中', 2: '已完成', 3: '失败' }[status] || '未知'
}

function scoreColor(score) {
  if (score >= 80) return '#67c23a'
  if (score >= 60) return '#e6a23c'
  return '#f56c6c'
}
</script>

<style scoped>
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}
.page-header h2 { margin: 0; }
</style>
