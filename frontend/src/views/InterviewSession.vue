<template>
  <div class="interview-session" v-loading="loading">
    <!-- 返回 -->
    <div class="back-bar">
      <el-button text @click="$router.push('/interview')">
        <el-icon><ArrowLeft /></el-icon> 返回列表
      </el-button>
    </div>

    <!-- 进度 -->
    <div class="progress-bar" v-if="totalQuestions > 0">
      <span>第 {{ currentIndex + 1 }} / {{ totalQuestions }} 题</span>
      <el-progress :percentage="Math.round(((currentIndex) / totalQuestions) * 100)" :stroke-width="8" />
    </div>

    <!-- 题目卡片 -->
    <el-card class="question-card" v-if="currentQuestion">
      <div class="question-header">
        <el-tag>{{ currentQuestion.category }}</el-tag>
        <span class="question-index">第 {{ currentIndex + 1 }} 题</span>
      </div>
      <div class="question-text">{{ currentQuestion.question }}</div>

      <!-- 答题区 -->
      <div class="answer-area">
        <el-input
          v-model="answer"
          type="textarea"
          :rows="6"
          placeholder="请输入你的回答..."
          :disabled="submitting"
        />
      </div>

      <div class="action-bar">
        <el-button :disabled="!answer.trim() || submitting" type="primary" size="large" @click="handleSubmit">
          <template v-if="submitting">提交中...</template>
          <template v-else>{{ isLast ? '提交并完成' : '提交下一题' }}</template>
        </el-button>
      </div>
    </el-card>

    <!-- 已完成状态 -->
    <el-result v-else-if="isCompleted" icon="success" title="面试已完成" sub-title="可以查看面试报告了">
      <template #extra>
        <el-button type="primary" @click="$router.push(`/interview/report/${sessionId}`)">查看报告</el-button>
      </template>
    </el-result>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { submitAnswer } from '../api/interview'

const route = useRoute()
const router = useRouter()

const sessionId = route.params.sessionId
const loading = ref(false)
const questions = ref([])
const currentIndex = ref(0)
const totalQuestions = ref(0)
const answer = ref('')
const submitting = ref(false)

const currentQuestion = computed(() => questions.value[currentIndex.value])
const isLast = computed(() => currentIndex.value >= totalQuestions.value - 1)
const isCompleted = computed(() => questions.value.length > 0 && currentIndex.value >= totalQuestions.value)

onMounted(() => {
  // 从 localStorage 读取面试题目（在创建时保存的）
  const saved = localStorage.getItem(`session_${sessionId}`)
  if (saved) {
    const data = JSON.parse(saved)
    questions.value = data.questions || []
    totalQuestions.value = data.totalQuestions || questions.value.length

    // 检查是否有已答过的题
    // 尝试从 answerDetail 恢复进度
    if (data.answers?.length) {
      currentIndex.value = data.answers.length
    }
  } else {
    ElMessage.error('面试数据不存在，请重新创建')
    router.push('/interview')
  }
})

async function handleSubmit() {
  if (!answer.value.trim()) return
  submitting.value = true
  try {
    const res = await submitAnswer(sessionId, {
      sessionId,
      questionIndex: currentIndex.value,
      answer: answer.value.trim()
    })
    if (res.code === 200) {
      answer.value = ''

      if (res.data.hasNextQuestion) {
        // 还有下一题
        currentIndex.value++
        ElMessage.success('回答已提交')
      } else {
        // 全部答完
        currentIndex.value = totalQuestions.value
        localStorage.removeItem(`session_${sessionId}`)
        ElMessage.success('所有题目已答完！AI正在评估中...')
      }
    }
  } catch (e) {
    // 错误已在拦截器中处理
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.interview-session {
  max-width: 800px;
  margin: 0 auto;
}
.back-bar { margin-bottom: 16px; }
.progress-bar {
  margin-bottom: 20px;
  display: flex;
  align-items: center;
  gap: 16px;
}
.progress-bar span {
  white-space: nowrap;
  color: #606266;
  font-size: 14px;
}
.progress-bar .el-progress { flex: 1; }
.question-card { margin-bottom: 20px; }
.question-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}
.question-index { color: #909399; font-size: 13px; }
.question-text {
  font-size: 16px;
  line-height: 1.6;
  color: #303133;
  margin-bottom: 20px;
  padding: 16px;
  background: #f5f7fa;
  border-radius: 8px;
}
.answer-area { margin-bottom: 16px; }
.action-bar { text-align: right; }
</style>
