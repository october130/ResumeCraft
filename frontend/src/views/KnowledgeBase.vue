<template>
  <div class="knowledge-base">
    <!-- 顶部介绍 -->
    <el-card class="intro-banner" shadow="never">
      <div class="intro-content">
        <div class="intro-text">
          <div class="intro-badge">
            <el-icon :size="14"><Cpu /></el-icon>
            RAG · 检索增强生成
          </div>
          <h2>AI 知识库</h2>
          <p>通过向量检索技术，在 AI 评分和出题前自动检索匹配的岗位 JD 和面试题库，让 AI 回答更精准、更有针对性。</p>
        </div>
        <div class="intro-stats">
          <div class="stat-item">
            <div class="stat-num">{{ docs.length }}</div>
            <div class="stat-label">知识库文档</div>
          </div>
          <div class="stat-item">
            <div class="stat-num">{{ totalChars }}</div>
            <div class="stat-label">总字符数</div>
          </div>
          <div class="stat-item">
            <div class="stat-num">{{ vectorStatus }}</div>
            <div class="stat-label">向量存储</div>
          </div>
        </div>
      </div>
    </el-card>

    <!-- RAG 流程说明 -->
    <el-card shadow="never" class="flow-card">
      <template #header>
        <div class="card-header">
          <span class="header-icon">🔄</span>
          <h3>RAG 工作流程</h3>
        </div>
      </template>
      <div class="flow-diagram">
        <div class="flow-node" v-for="(step, idx) in flowSteps" :key="idx">
          <div class="flow-node-icon" :style="{ background: step.color }">
            <el-icon :size="20"><component :is="step.icon" /></el-icon>
          </div>
          <div class="flow-node-text">
            <h4>{{ step.title }}</h4>
            <p>{{ step.desc }}</p>
          </div>
          <div class="flow-arrow" v-if="idx < flowSteps.length - 1">
            <el-icon><ArrowRight /></el-icon>
          </div>
        </div>
      </div>
    </el-card>

    <el-row :gutter="20">
      <!-- 知识库文档列表 -->
      <el-col :span="14">
        <el-card shadow="never" class="docs-card">
          <template #header>
            <div class="card-header">
              <span class="header-icon">📚</span>
              <h3>知识库文档</h3>
              <el-tag size="small" type="success" effect="dark">
                <el-icon><CircleCheck /></el-icon> 已加载
              </el-tag>
            </div>
          </template>

          <div class="doc-tabs">
            <div
              class="doc-tab"
              :class="{ active: activeTab === 'jd' }"
              @click="activeTab = 'jd'"
            >
              <el-icon><Document /></el-icon>
              岗位 JD（{{ jdDocs.length }}）
            </div>
            <div
              class="doc-tab"
              :class="{ active: activeTab === 'questions' }"
              @click="activeTab = 'questions'"
            >
              <el-icon><Notebook /></el-icon>
              面试题库（{{ questionDocs.length }}）
            </div>
          </div>

          <div class="doc-list" v-loading="loading">
            <div
              v-for="doc in filteredDocs"
              :key="doc.id"
              class="doc-item"
              @click="viewDoc(doc)"
            >
              <div class="doc-item-icon" :style="{ background: doc.type === 'jd' ? 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)' : 'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)' }">
                <el-icon :size="18" color="#fff">
                  <component :is="doc.type === 'jd' ? 'Document' : 'Notebook'" />
                </el-icon>
              </div>
              <div class="doc-item-info">
                <div class="doc-item-name">{{ doc.name }}</div>
                <div class="doc-item-meta">
                  <el-tag size="small" :type="doc.type === 'jd' ? '' : 'warning'" effect="plain">
                    {{ doc.type === 'jd' ? '岗位描述' : '面试题库' }}
                  </el-tag>
                  <el-tag v-if="doc.skill" size="small" type="info" effect="plain">{{ skillLabel(doc.skill) }}</el-tag>
                  <el-tag v-if="doc.difficulty" size="small" effect="plain">{{ diffLabel(doc.difficulty) }}</el-tag>
                  <span class="doc-chars">{{ doc.charCount }} 字符</span>
                </div>
              </div>
              <el-icon class="doc-item-arrow"><ArrowRight /></el-icon>
            </div>
          </div>
        </el-card>
      </el-col>

      <!-- 语义搜索演示 -->
      <el-col :span="10">
        <el-card shadow="never" class="search-card">
          <template #header>
            <div class="card-header">
              <span class="header-icon">🔍</span>
              <h3>向量检索演示</h3>
            </div>
          </template>

          <div class="search-form">
            <label class="search-label">技能方向</label>
            <el-select v-model="searchSkill" placeholder="选择技能方向" style="width: 100%">
              <el-option label="Java 后端" value="java-backend" />
              <el-option label="前端开发" value="frontend" />
              <el-option label="Python" value="python" />
            </el-select>
          </div>

          <div class="search-form">
            <label class="search-label">检索类型</label>
            <el-radio-group v-model="searchType">
              <el-radio-button value="jd">岗位 JD</el-radio-button>
              <el-radio-button value="questions">面试题库</el-radio-button>
            </el-radio-group>
          </div>

          <el-button
            type="primary"
            style="width: 100%; margin-top: 8px"
            :loading="searching"
            @click="handleSearch"
          >
            <el-icon><Search /></el-icon>
            执行向量检索
          </el-button>

          <div class="search-results" v-if="searchResults.length">
            <div class="results-header">
              <span>检索结果（{{ searchResults.length }} 条）</span>
              <el-tag size="small" type="success">语义匹配</el-tag>
            </div>
            <div
              v-for="(result, idx) in searchResults"
              :key="idx"
              class="result-item"
            >
              <div class="result-header">
                <span class="result-rank">#{{ idx + 1 }}</span>
                <el-tag size="small" type="info">
                  相似度 {{ (result.score * 100).toFixed(1) }}%
                </el-tag>
              </div>
              <div class="result-content">{{ result.content }}</div>
            </div>
          </div>

          <div class="search-empty" v-else-if="searched">
            <el-empty description="未检索到相关内容" :image-size="60" />
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 文档详情弹窗 -->
    <el-dialog
      v-model="docDialogVisible"
      :title="currentDoc?.name"
      width="680px"
      destroy-on-close
    >
      <div class="doc-dialog-meta" v-if="currentDoc">
        <el-tag size="small">{{ currentDoc.type === 'jd' ? '岗位描述' : '面试题库' }}</el-tag>
        <el-tag v-if="currentDoc.skill" size="small" type="info">{{ skillLabel(currentDoc.skill) }}</el-tag>
        <el-tag v-if="currentDoc.difficulty" size="small">{{ diffLabel(currentDoc.difficulty) }}</el-tag>
      </div>
      <div class="doc-dialog-content" v-loading="docLoading">
        <pre>{{ docContent }}</pre>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { getKnowledgeBaseList, getKnowledgeDoc, searchKnowledgeBase } from '../api/knowledge'

const loading = ref(false)
const docs = ref([])
const activeTab = ref('jd')
const docDialogVisible = ref(false)
const currentDoc = ref(null)
const docContent = ref('')
const docLoading = ref(false)

// 搜索相关
const searchSkill = ref('java-backend')
const searchType = ref('jd')
const searching = ref(false)
const searchResults = ref([])
const searched = ref(false)

const jdDocs = computed(() => docs.value.filter(d => d.type === 'jd'))
const questionDocs = computed(() => docs.value.filter(d => d.type === 'questions'))
const filteredDocs = computed(() => activeTab.value === 'jd' ? jdDocs.value : questionDocs.value)
const totalChars = computed(() => {
  const total = docs.value.reduce((sum, d) => sum + (d.charCount || 0), 0)
  return total > 1000 ? (total / 1000).toFixed(1) + 'k' : total
})
const vectorStatus = computed(() => docs.value.length ? '就绪' : '未加载')

const flowSteps = [
  { title: '知识入库', desc: 'JD 和面试题库向量化存储', icon: 'Upload', color: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)' },
  { title: '语义检索', desc: '根据岗位方向检索相关内容', icon: 'Search', color: 'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)' },
  { title: 'Prompt 增强', desc: '将检索结果注入提示词', icon: 'EditPen', color: 'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)' },
  { title: 'AI 生成', desc: '大模型基于上下文精准回答', icon: 'MagicStick', color: 'linear-gradient(135deg, #43e97b 0%, #38f9d7 100%)' },
]

onMounted(async () => {
  loading.value = true
  try {
    const res = await getKnowledgeBaseList()
    if (res.code === 200) docs.value = res.data || []
  } finally { loading.value = false }
})

async function viewDoc(doc) {
  currentDoc.value = doc
  docDialogVisible.value = true
  docLoading.value = true
  try {
    const res = await getKnowledgeDoc(doc.id)
    if (res.code === 200) docContent.value = res.data.content || ''
  } finally { docLoading.value = false }
}

async function handleSearch() {
  searching.value = true
  searched.value = false
  searchResults.value = []
  try {
    const res = await searchKnowledgeBase({
      skillId: searchSkill.value,
      type: searchType.value,
      topK: 3
    })
    if (res.code === 200) {
      searchResults.value = res.data || []
    }
  } finally {
    searching.value = false
    searched.value = true
  }
}

function skillLabel(id) {
  return { 'java-backend': 'Java 后端', frontend: '前端开发', python: 'Python' }[id] || id
}
function diffLabel(d) {
  return { junior: '初级', mid: '中级', senior: '高级' }[d] || d
}
</script>

<style scoped>
.knowledge-base {
  max-width: 1400px;
  margin: 0 auto;
}

/* 顶部介绍 */
.intro-banner {
  margin-bottom: 20px;
  background: linear-gradient(135deg, #1e3a5f 0%, #2d5a87 100%);
  border: none;
  border-radius: 12px;
  color: #fff;
}

.intro-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 32px;
}

.intro-badge {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  background: rgba(255,255,255,0.15);
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 12px;
  margin-bottom: 8px;
  backdrop-filter: blur(10px);
}

.intro-text h2 {
  margin: 0 0 8px 0;
  font-size: 24px;
  font-weight: 600;
}

.intro-text p {
  margin: 0;
  opacity: 0.85;
  font-size: 14px;
  max-width: 500px;
  line-height: 1.6;
}

.intro-stats {
  display: flex;
  gap: 32px;
}

.stat-item {
  text-align: center;
}

.stat-num {
  font-size: 28px;
  font-weight: 700;
}

.stat-label {
  font-size: 12px;
  opacity: 0.7;
  margin-top: 4px;
}

/* 流程图 */
.flow-card {
  margin-bottom: 20px;
  border-radius: 12px;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 10px;
}

.header-icon {
  font-size: 20px;
}

.card-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.flow-diagram {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 0;
  overflow-x: auto;
}

.flow-node {
  display: flex;
  align-items: center;
  gap: 10px;
  flex: 1;
  min-width: 0;
}

.flow-node-icon {
  width: 44px;
  height: 44px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  flex-shrink: 0;
}

.flow-node-text h4 {
  margin: 0 0 2px 0;
  font-size: 13px;
  font-weight: 600;
  color: #303133;
}

.flow-node-text p {
  margin: 0;
  font-size: 11px;
  color: #909399;
}

.flow-arrow {
  color: #c0c4cc;
  font-size: 18px;
  flex-shrink: 0;
}

/* 文档卡片 */
.docs-card {
  border-radius: 12px;
}

.doc-tabs {
  display: flex;
  gap: 8px;
  margin-bottom: 16px;
}

.doc-tab {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  border-radius: 8px;
  cursor: pointer;
  font-size: 13px;
  font-weight: 500;
  color: #606266;
  background: #f5f7fa;
  transition: all 0.2s;
}

.doc-tab:hover {
  background: #ecf5ff;
  color: #409eff;
}

.doc-tab.active {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
}

.doc-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
  min-height: 200px;
}

.doc-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 16px;
  border: 1px solid #ebeef5;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.2s;
}

.doc-item:hover {
  border-color: #c6e2ff;
  background: #f0f7ff;
  transform: translateX(4px);
}

.doc-item-icon {
  width: 40px;
  height: 40px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.doc-item-info {
  flex: 1;
  min-width: 0;
}

.doc-item-name {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 6px;
}

.doc-item-meta {
  display: flex;
  align-items: center;
  gap: 6px;
  flex-wrap: wrap;
}

.doc-chars {
  font-size: 12px;
  color: #909399;
}

.doc-item-arrow {
  color: #c0c4cc;
  flex-shrink: 0;
}

/* 搜索卡片 */
.search-card {
  border-radius: 12px;
}

.search-form {
  margin-bottom: 14px;
}

.search-label {
  display: block;
  font-size: 13px;
  font-weight: 500;
  color: #606266;
  margin-bottom: 6px;
}

.search-results {
  margin-top: 20px;
}

.results-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  font-size: 13px;
  font-weight: 600;
  color: #303133;
}

.result-item {
  padding: 12px;
  border: 1px solid #ebeef5;
  border-radius: 8px;
  margin-bottom: 8px;
  transition: all 0.2s;
}

.result-item:hover {
  border-color: #c6e2ff;
  background: #f0f7ff;
}

.result-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.result-rank {
  font-weight: 700;
  color: #667eea;
  font-size: 14px;
}

.result-content {
  font-size: 12px;
  color: #606266;
  line-height: 1.6;
  max-height: 120px;
  overflow-y: auto;
  white-space: pre-wrap;
  word-break: break-all;
}

.search-empty {
  margin-top: 20px;
}

/* 文档详情弹窗 */
.doc-dialog-meta {
  display: flex;
  gap: 8px;
  margin-bottom: 16px;
}

.doc-dialog-content {
  max-height: 500px;
  overflow-y: auto;
  background: #f5f7fa;
  border-radius: 8px;
  padding: 16px;
}

.doc-dialog-content pre {
  margin: 0;
  white-space: pre-wrap;
  word-break: break-all;
  font-size: 13px;
  line-height: 1.7;
  color: #303133;
  font-family: inherit;
}
</style>
