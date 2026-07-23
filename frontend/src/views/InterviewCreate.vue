<template>
  <div class="interview-create">
    <div class="page-header">
      <h2>创建面试</h2>
      <p class="page-desc">
        选择技能方向和难度，开始你的 AI 模拟面试之旅
        <el-tag size="small" type="success" effect="dark" class="rag-badge">
          <el-icon :size="12"><Cpu /></el-icon>
          RAG 知识库已启用
        </el-tag>
      </p>
    </div>

    <el-row :gutter="24">
      <el-col :span="16">
        <el-card shadow="never" class="create-card">
          <template #header>
            <div class="card-header">
              <span class="header-icon">📝</span>
              <div>
                <h3>面试配置</h3>
                <p class="header-desc">请根据你的需求选择合适的面试参数</p>
              </div>
            </div>
          </template>

          <!-- 技能方向选择 -->
          <div class="form-section">
            <label class="section-title">
              <el-icon><Aim /></el-icon>
              技能方向
            </label>
            <div class="skill-grid">
              <div
                v-for="skill in skillOptions"
                :key="skill.value"
                class="skill-card"
                :class="{ active: form.skillId === skill.value }"
                @click="form.skillId = skill.value"
              >
                <div class="skill-icon" :style="{ background: skill.gradient }">
                  <el-icon :size="24"><component :is="skill.icon" /></el-icon>
                </div>
                <div class="skill-info">
                  <span class="skill-name">{{ skill.label }}</span>
                  <span class="skill-desc">{{ skill.desc }}</span>
                </div>
                <div class="skill-check" v-if="form.skillId === skill.value">
                  <el-icon><CircleCheckFilled /></el-icon>
                </div>
              </div>
            </div>
          </div>

          <!-- 难度选择 -->
          <div class="form-section">
            <label class="section-title">
              <el-icon><TrendCharts /></el-icon>
              难度级别
            </label>
            <div class="difficulty-grid">
              <div
                v-for="diff in difficultyOptions"
                :key="diff.value"
                class="difficulty-card"
                :class="{ active: form.difficulty === diff.value, [diff.value]: true }"
                @click="form.difficulty = diff.value"
              >
                <div class="diff-header">
                  <span class="diff-icon">{{ diff.icon }}</span>
                  <span class="diff-name">{{ diff.label }}</span>
                </div>
                <p class="diff-desc">{{ diff.desc }}</p>
                <div class="diff-stars">
                  <el-icon v-for="i in diff.stars" :key="i" :size="14"><Star /></el-icon>
                </div>
              </div>
            </div>
          </div>

          <!-- 题目数量 -->
          <div class="form-section">
            <label class="section-title">
              <el-icon><List /></el-icon>
              题目数量
            </label>
            <div class="question-count">
              <el-slider
                v-model="form.questionCount"
                :min="3"
                :max="10"
                :step="1"
                :marks="marks"
                show-input
              />
              <p class="count-hint">建议 {{ form.difficulty === 'junior' ? '3-5' : form.difficulty === 'mid' ? '5-8' : '8-10' }} 题</p>
            </div>
          </div>

          <!-- 关联简历 -->
          <div class="form-section">
            <label class="section-title">
              <el-icon><Document /></el-icon>
              关联简历
              <el-tag size="small" type="info" effect="plain">选填</el-tag>
            </label>
            <el-select
              v-model="form.resumeId"
              placeholder="选择要关联的简历（可选）"
              clearable
              style="width: 100%"
              size="large"
            >
              <el-option
                v-for="r in resumeList"
                :key="r.resumeId"
                :label="r.fileName"
                :value="r.resumeId"
              >
                <div class="resume-option">
                  <el-icon><Document /></el-icon>
                  <span>{{ r.fileName }}</span>
                  <el-tag size="small" :type="r.fileType === 'pdf' ? 'danger' : 'primary'">
                    {{ r.fileType?.toUpperCase() }}
                  </el-tag>
                </div>
              </el-option>
            </el-select>
            <p class="resume-hint">关联简历后，AI 会根据你的简历内容出题，面试更有针对性</p>
          </div>

          <!-- 提交按钮 -->
          <div class="form-actions">
            <el-button type="primary" size="large" :loading="submitting" @click="handleCreate" class="submit-btn">
              <el-icon><VideoPlay /></el-icon>
              开始面试
            </el-button>
          </div>
        </el-card>
      </el-col>

      <!-- 右侧说明卡片 -->
      <el-col :span="8">
        <el-card shadow="never" class="info-card">
          <template #header>
            <div class="card-header">
              <span class="header-icon">💡</span>
              <h3>面试流程</h3>
            </div>
          </template>
          <div class="flow-steps">
            <div class="flow-step">
              <div class="step-num">1</div>
              <div class="step-content">
                <h4>配置面试</h4>
                <p>选择技能方向和难度</p>
              </div>
            </div>
            <div class="flow-step">
              <div class="step-num rag">2</div>
              <div class="step-content">
                <h4>RAG 知识检索 <el-tag size="small" type="success" effect="plain" style="margin-left:4px">NEW</el-tag></h4>
                <p>向量检索匹配的岗位 JD 和面试题库</p>
              </div>
            </div>
            <div class="flow-step">
              <div class="step-num">3</div>
              <div class="step-content">
                <h4>AI 出题</h4>
                <p>基于知识库上下文生成面试题</p>
              </div>
            </div>
            <div class="flow-step">
              <div class="step-num">4</div>
              <div class="step-content">
                <h4>逐题作答</h4>
                <p>在线回答每道题目</p>
              </div>
            </div>
            <div class="flow-step">
              <div class="step-num">5</div>
              <div class="step-content">
                <h4>获取报告</h4>
                <p>AI 评估并生成详细报告</p>
              </div>
            </div>
          </div>
        </el-card>

        <el-card shadow="never" class="tips-card" style="margin-top: 16px">
          <template #header>
            <div class="card-header">
              <span class="header-icon">⚠️</span>
              <h3>温馨提示</h3>
            </div>
          </template>
          <ul class="tips-list">
            <li><el-icon color="#e6a23c"><InfoFilled /></el-icon>面试过程中请勿关闭页面</li>
            <li><el-icon color="#e6a23c"><InfoFilled /></el-icon>每题限时 10 分钟，超时自动提交</li>
            <li><el-icon color="#e6a23c"><InfoFilled /></el-icon>答案提交后不可修改</li>
            <li><el-icon color="#e6a23c"><InfoFilled /></el-icon>评估报告需等待 1-2 分钟</li>
          </ul>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { createSession } from '../api/interview'
import { getResumeList } from '../api/resume'

const router = useRouter()
const form = ref({ skillId: 'java-backend', difficulty: 'junior', questionCount: 5, resumeId: null })
const submitting = ref(false)
const resumeList = ref([])

const skillOptions = [
  { value: 'java-backend', label: 'Java 后端', desc: 'Spring Boot、微服务等', icon: 'Monitor', gradient: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)' },
  { value: 'frontend', label: '前端开发', desc: 'Vue、React、CSS 等', icon: 'Picture', gradient: 'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)' },
  { value: 'python', label: 'Python', desc: 'Django、数据分析等', icon: 'Cpu', gradient: 'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)' },
  { value: 'go', label: 'Go 开发', desc: 'Gin、并发编程等', icon: 'Connection', gradient: 'linear-gradient(135deg, #43e97b 0%, #38f9d7 100%)' },
  { value: 'algorithm', label: '数据结构与算法', desc: 'LeetCode 风格', icon: 'Histogram', gradient: 'linear-gradient(135deg, #fa709a 0%, #fee140 100%)' }
]

const difficultyOptions = [
  { value: 'junior', label: '初级', icon: '🌱', desc: '适合应届生和实习生', stars: 1 },
  { value: 'mid', label: '中级', icon: '🔥', desc: '适合 1-3 年经验', stars: 2 },
  { value: 'senior', label: '高级', icon: '⚡', desc: '适合 3 年以上经验', stars: 3 }
]

const marks = {
  3: '3题',
  5: '5题',
  8: '8题',
  10: '10题'
}

onMounted(async () => {
  try {
    const r = await getResumeList({ pageNum: 1, pageSize: 999 })
    if (r.code === 200) resumeList.value = r.data.records || []
  } catch {}
})

async function handleCreate() {
  if (!form.value.skillId || !form.value.difficulty) {
    ElMessage.warning('请选择技能方向和难度')
    return
  }
  submitting.value = true
  try {
    const res = await createSession(form.value)
    if (res.code === 200) {
      localStorage.setItem(`session_${res.data.sessionId}`, JSON.stringify(res.data))
      ElMessage.success('面试创建成功')
      router.push(`/interview/session/${res.data.sessionId}`)
    }
  } finally { submitting.value = false }
}
</script>

<style scoped>
.interview-create {
  max-width: 1200px;
  margin: 0 auto;
}

.page-header {
  margin-bottom: 24px;
}

.page-header h2 {
  margin: 0 0 8px 0;
  font-size: 24px;
  font-weight: 600;
  color: #303133;
}

.page-desc {
  margin: 0;
  color: #909399;
  font-size: 14px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.rag-badge {
  vertical-align: middle;
}

.create-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 12px;
}

.header-icon {
  font-size: 24px;
}

.card-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.header-desc {
  margin: 4px 0 0 0;
  color: #909399;
  font-size: 13px;
}

.form-section {
  margin-bottom: 28px;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 15px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 12px;
}

.section-title .el-tag {
  margin-left: 8px;
}

/* 技能方向卡片 */
.skill-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 12px;
}

.skill-card {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
  border: 2px solid #e4e7ed;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.2s;
  position: relative;
}

.skill-card:hover {
  border-color: #c0c4cc;
  transform: translateY(-2px);
}

.skill-card.active {
  border-color: #667eea;
  background: rgba(102,126,234,0.05);
  box-shadow: 0 4px 12px rgba(102,126,234,0.2);
}

.skill-icon {
  width: 48px;
  height: 48px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  flex-shrink: 0;
}

.skill-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
  flex: 1;
}

.skill-name {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
}

.skill-desc {
  font-size: 12px;
  color: #909399;
}

.skill-check {
  color: #67c23a;
  font-size: 20px;
}

/* 难度卡片 */
.difficulty-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
}

.difficulty-card {
  padding: 20px;
  border: 2px solid #e4e7ed;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.2s;
  text-align: center;
}

.difficulty-card:hover {
  transform: translateY(-2px);
}

.difficulty-card.active.junior {
  border-color: #67c23a;
  background: rgba(103,194,58,0.05);
  box-shadow: 0 4px 12px rgba(103,194,58,0.2);
}

.difficulty-card.active.mid {
  border-color: #e6a23c;
  background: rgba(230,162,60,0.05);
  box-shadow: 0 4px 12px rgba(230,162,60,0.2);
}

.difficulty-card.active.senior {
  border-color: #f56c6c;
  background: rgba(245,108,108,0.05);
  box-shadow: 0 4px 12px rgba(245,108,108,0.2);
}

.diff-header {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}

.diff-icon {
  font-size: 32px;
}

.diff-name {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.diff-desc {
  font-size: 12px;
  color: #909399;
  margin: 0 0 12px 0;
}

.diff-stars {
  display: flex;
  justify-content: center;
  gap: 4px;
  color: #f7ba2a;
}

/* 题目数量 */
.question-count {
  padding: 20px;
  background: #f5f7fa;
  border-radius: 12px;
}

.count-hint {
  margin: 12px 0 0 0;
  font-size: 13px;
  color: #909399;
  text-align: center;
}

/* 简历选择 */
.resume-option {
  display: flex;
  align-items: center;
  gap: 8px;
}

.resume-hint {
  margin: 8px 0 0 0;
  font-size: 13px;
  color: #909399;
}

/* 提交按钮 */
.form-actions {
  margin-top: 32px;
  text-align: center;
}

.submit-btn {
  min-width: 200px;
  height: 48px;
  font-size: 16px;
  font-weight: 600;
  border-radius: 12px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  box-shadow: 0 4px 16px rgba(102,126,234,0.4);
  transition: all 0.2s;
}

.submit-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(102,126,234,0.5);
}

/* 右侧信息卡片 */
.info-card, .tips-card {
  margin-bottom: 0;
}

.flow-steps {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.flow-step {
  display: flex;
  gap: 12px;
  align-items: flex-start;
}

.step-num {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  flex-shrink: 0;
}

.step-num.rag {
  background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
}

.step-content h4 {
  margin: 0 0 4px 0;
  font-size: 14px;
  font-weight: 600;
  color: #303133;
}

.step-content p {
  margin: 0;
  font-size: 13px;
  color: #909399;
}

.tips-list {
  list-style: none;
  padding: 0;
  margin: 0;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.tips-list li {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: #606266;
}
</style>
