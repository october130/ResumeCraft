<template>
  <div class="interview-list">
    <div class="page-header">
      <h2>模拟面试</h2>
      <el-button type="primary" @click="showCreateDialog = true">
        <el-icon><Plus /></el-icon> 创建面试
      </el-button>
    </div>

    <!-- 创建面试对话框 -->
    <el-dialog v-model="showCreateDialog" title="创建面试" width="500px">
      <el-form :model="createForm" label-width="120px">
        <el-form-item label="技能方向" required>
          <el-select v-model="createForm.skillId" placeholder="选择技能方向" style="width: 100%">
            <el-option label="Java 后端" value="java-backend" />
            <el-option label="前端开发" value="frontend" />
            <el-option label="Python" value="python" />
            <el-option label="Go 开发" value="go" />
            <el-option label="数据结构与算法" value="algorithm" />
          </el-select>
        </el-form-item>
        <el-form-item label="难度" required>
          <el-radio-group v-model="createForm.difficulty">
            <el-radio value="junior">初级</el-radio>
            <el-radio value="mid">中级</el-radio>
            <el-radio value="senior">高级</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="题目数量" required>
          <el-select v-model="createForm.questionCount" style="width: 100%">
            <el-option label="3 题" :value="3" />
            <el-option label="5 题" :value="5" />
            <el-option label="8 题" :value="8" />
            <el-option label="10 题" :value="10" />
          </el-select>
        </el-form-item>
        <el-form-item label="关联简历">
          <el-select v-model="createForm.resumeId" placeholder="可选，关联简历" clearable style="width: 100%">
            <el-option v-for="r in resumeList" :key="r.resumeId" :label="r.fileName" :value="r.resumeId" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCreateDialog = false">取消</el-button>
        <el-button type="primary" :loading="creating" @click="handleCreate">开始面试</el-button>
      </template>
    </el-dialog>

    <!-- 面试列表 -->
    <el-table :data="list" v-loading="loading" stripe style="width: 100%">
      <el-table-column label="技能方向" width="140">
        <template #default="{ row }">{{ row.skillId || '综合' }}</template>
      </el-table-column>
      <el-table-column label="难度" width="80">
        <template #default="{ row }">
          <el-tag size="small">{{ diffText(row.difficulty) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="题目数" width="80" prop="totalQuestions" />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="statusTag(row.status)" size="small">{{ statusText(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="评估" width="100">
        <template #default="{ row }">
          <el-tag :type="evalTag(row.evaluationStatus)" size="small">{{ evalText(row.evaluationStatus) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="评分" width="80">
        <template #default="{ row }">
          <span v-if="row.overallScore != null" :style="{ color: scoreColor(row.overallScore) }">{{ row.overallScore }}</span>
          <span v-else style="color: #909399">-</span>
        </template>
      </el-table-column>
      <el-table-column label="创建时间" width="170" prop="createdAt" />
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="{ row }">
          <el-button v-if="row.status === 'IN_PROGRESS' || row.status === 'CREATED'" text type="primary"
            @click="$router.push(`/interview/session/${row.sessionId}`)">继续答题</el-button>
          <el-button v-else text type="primary"
            @click="$router.push(`/interview/report/${row.sessionId}`)">查看报告</el-button>
          <el-button text type="danger" @click="handleDelete(row.sessionId)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getInterviewList, createSession, deleteSession } from '../api/interview'
import { getResumeList } from '../api/resume'

const router = useRouter()
const list = ref([])
const loading = ref(false)
const showCreateDialog = ref(false)
const creating = ref(false)
const resumeList = ref([])
const createForm = ref({ skillId: 'java-backend', difficulty: 'junior', questionCount: 5, resumeId: null })

onMounted(() => {
  fetchList()
  fetchResumes()
})

async function fetchList() {
  loading.value = true
  try {
    const res = await getInterviewList()
    if (res.code === 200) list.value = res.data || []
  } finally {
    loading.value = false
  }
}

async function fetchResumes() {
  try {
    const res = await getResumeList({ pageNum: 1, pageSize: 999 })
    if (res.code === 200) resumeList.value = res.data.records || []
  } catch {}
}

async function handleCreate() {
  if (!createForm.value.skillId || !createForm.value.difficulty) {
    ElMessage.warning('请选择技能方向和难度')
    return
  }
  creating.value = true
  try {
    const res = await createSession(createForm.value)
    if (res.code === 200) {
      // 把题目数据存到 localStorage，面试答题页面需要
      const sessionData = res.data
      localStorage.setItem(`session_${sessionData.sessionId}`, JSON.stringify(sessionData))
      ElMessage.success('面试创建成功')
      showCreateDialog.value = false
      router.push(`/interview/session/${sessionData.sessionId}`)
    }
  } finally {
    creating.value = false
  }
}

async function handleDelete(sessionId) {
  await ElMessageBox.confirm('确定删除该面试记录？', '提示')
  const res = await deleteSession(sessionId)
  if (res.code === 200) {
    ElMessage.success('删除成功')
    fetchList()
  }
}

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
function scoreColor(s) {
  if (s >= 80) return '#67c23a'
  if (s >= 60) return '#e6a23c'
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
