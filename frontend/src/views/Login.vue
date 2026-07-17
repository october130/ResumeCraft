<template>
  <div class="login-page">
    <div class="login-bg"></div>
    <div class="login-container">
      <div class="login-left">
        <div class="brand-content">
          <div class="brand-icon">
            <el-icon :size="40"><MagicStick /></el-icon>
          </div>
          <h1>AI 智能简历分析系统</h1>
          <p class="brand-desc">基于大语言模型，智能分析简历、模拟面试、提供专业评估报告</p>
          <div class="feature-list">
            <div class="feature-item">
              <el-icon :size="20"><DocumentChecked /></el-icon>
              <span>多维度简历评分</span>
            </div>
            <div class="feature-item">
              <el-icon :size="20"><ChatDotSquare /></el-icon>
              <span>AI 模拟面试</span>
            </div>
            <div class="feature-item">
              <el-icon :size="20"><TrendCharts /></el-icon>
              <span>智能数据分析</span>
            </div>
          </div>
        </div>
      </div>
      <div class="login-right">
        <div class="login-form-wrapper">
          <h2 class="form-title">{{ activeTab === 'login' ? '欢迎登录' : '创建账号' }}</h2>
          <p class="form-subtitle">请填写您的信息</p>

          <el-tabs v-model="activeTab" class="login-tabs" stretch>
            <el-tab-pane label="登录" name="login">
              <el-form ref="loginFormRef" :model="loginForm" :rules="rules" label-width="0" class="login-form">
                <el-form-item prop="phone">
                  <el-input
                    v-model="loginForm.phone"
                    placeholder="手机号"
                    size="large"
                    :prefix-icon="Iphone"
                  />
                </el-form-item>
                <el-form-item prop="password">
                  <el-input
                    v-model="loginForm.password"
                    type="password"
                    placeholder="密码"
                    size="large"
                    :prefix-icon="Lock"
                    show-password
                  />
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" size="large" :loading="loading" class="submit-btn" @click="handleLogin">
                    登 录
                  </el-button>
                </el-form-item>
              </el-form>
            </el-tab-pane>
            <el-tab-pane label="注册" name="register">
              <el-form ref="registerFormRef" :model="registerForm" :rules="registerRules" label-width="0" class="login-form">
                <el-form-item prop="username">
                  <el-input v-model="registerForm.username" placeholder="用户名" size="large" :prefix-icon="User" />
                </el-form-item>
                <el-form-item prop="phone">
                  <el-input v-model="registerForm.phone" placeholder="手机号" size="large" :prefix-icon="Iphone" maxlength="11" />
                </el-form-item>
                <el-form-item prop="email">
                  <el-input v-model="registerForm.email" placeholder="邮箱（选填）" size="large" :prefix-icon="Message" />
                </el-form-item>
                <el-form-item prop="password">
                  <el-input v-model="registerForm.password" type="password" placeholder="密码" size="large" :prefix-icon="Lock" show-password />
                </el-form-item>
                <el-form-item prop="confirmPassword">
                  <el-input v-model="registerForm.confirmPassword" type="password" placeholder="确认密码" size="large" :prefix-icon="Lock" show-password />
                </el-form-item>
                <el-form-item prop="code">
                  <div class="code-input">
                    <el-input v-model="registerForm.code" placeholder="验证码" size="large" maxlength="4" />
                    <el-button size="large" :disabled="codeSending || countdown > 0" @click="handleSendCode" class="code-btn">
                      {{ countdown > 0 ? `${countdown}s` : '获取验证码' }}
                    </el-button>
                  </div>
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" size="large" :loading="loading" class="submit-btn" @click="handleRegister">
                    注 册
                  </el-button>
                </el-form-item>
              </el-form>
            </el-tab-pane>
          </el-tabs>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { User, Lock, Iphone, Message } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { login, register, sendSms } from '../api/user'
import { useUserStore } from '../store/user'

const router = useRouter()
const userStore = useUserStore()

const activeTab = ref('login')
const loading = ref(false)

const loginForm = reactive({ phone: '', password: '' })
const loginFormRef = ref(null)
const rules = {
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

async function handleLogin() {
  const valid = await loginFormRef.value.validate().catch(() => false)
  if (!valid) return
  loading.value = true
  try {
    const res = await login({ phone: loginForm.phone, password: loginForm.password })
    if (res.code === 200) {
      userStore.setToken(res.data.token)
      userStore.setUserInfo(res.data)
      ElMessage.success('登录成功')
      router.push('/dashboard')
    }
  } finally {
    loading.value = false
  }
}

const codeSending = ref(false)
const countdown = ref(0)
let countdownTimer = null

async function handleSendCode() {
  if (!/^1[3-9]\d{9}$/.test(registerForm.phone)) {
    ElMessage.warning('请输入正确的手机号')
    return
  }
  codeSending.value = true
  try {
    const res = await sendSms(registerForm.phone)
    if (res.code === 200) {
      ElMessage.success('验证码已发送')
      countdown.value = 60
      countdownTimer = setInterval(() => {
        countdown.value--
        if (countdown.value <= 0) {
          clearInterval(countdownTimer)
          countdownTimer = null
        }
      }, 1000)
    }
  } finally {
    codeSending.value = false
  }
}

const registerForm = reactive({
  username: '',
  phone: '',
  email: '',
  password: '',
  confirmPassword: '',
  code: ''
})
const registerFormRef = ref(null)
const registerRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ],
  email: [{ type: 'email', message: '邮箱格式不正确', trigger: 'blur', required: false }],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码至少6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== registerForm.password) {
          callback(new Error('两次密码不一致'))
        } else {
          callback()
        }
      }, trigger: 'blur'
    }
  ],
  code: [
    { required: true, message: '请输入验证码', trigger: 'blur' },
    { len: 4, message: '验证码为4位', trigger: 'blur' }
  ]
}

async function handleRegister() {
  const valid = await registerFormRef.value.validate().catch(() => false)
  if (!valid) return
  loading.value = true
  try {
    const res = await register({
      username: registerForm.username,
      password: registerForm.password,
      phone: registerForm.phone,
      email: registerForm.email || undefined,
      code: registerForm.code
    })
    if (res.code === 200) {
      ElMessage.success('注册成功，请登录')
      activeTab.value = 'login'
      loginForm.phone = registerForm.phone
    }
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
}

.login-bg {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  z-index: -1;
}

.login-bg::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background:
    radial-gradient(circle at 20% 50%, rgba(255,255,255,0.1) 0%, transparent 50%),
    radial-gradient(circle at 80% 80%, rgba(255,255,255,0.1) 0%, transparent 50%);
}

.login-container {
  width: 900px;
  background: #fff;
  border-radius: 20px;
  overflow: hidden;
  box-shadow: 0 20px 60px rgba(0,0,0,0.3);
  display: flex;
}

.login-left {
  flex: 1;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 60px 40px;
  display: flex;
  align-items: center;
  color: #fff;
  position: relative;
  overflow: hidden;
}

.login-left::before {
  content: '';
  position: absolute;
  top: -50%;
  right: -50%;
  width: 200%;
  height: 200%;
  background: radial-gradient(circle, rgba(255,255,255,0.1) 0%, transparent 70%);
  animation: float 20s ease-in-out infinite;
}

@keyframes float {
  0%, 100% { transform: translate(0, 0); }
  50% { transform: translate(-50px, 50px); }
}

.brand-content {
  position: relative;
  z-index: 1;
}

.brand-icon {
  width: 72px;
  height: 72px;
  border-radius: 16px;
  background: rgba(255,255,255,0.2);
  backdrop-filter: blur(10px);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 24px;
  box-shadow: 0 8px 24px rgba(0,0,0,0.2);
}

.brand-content h1 {
  font-size: 28px;
  font-weight: 700;
  margin: 0 0 12px 0;
}

.brand-desc {
  font-size: 14px;
  opacity: 0.9;
  line-height: 1.6;
  margin: 0 0 32px 0;
}

.feature-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.feature-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  background: rgba(255,255,255,0.15);
  border-radius: 10px;
  backdrop-filter: blur(10px);
  font-size: 14px;
}

.login-right {
  flex: 1;
  padding: 60px 40px;
  display: flex;
  align-items: center;
}

.login-form-wrapper {
  width: 100%;
}

.form-title {
  font-size: 28px;
  font-weight: 700;
  color: #303133;
  margin: 0 0 8px 0;
}

.form-subtitle {
  font-size: 14px;
  color: #909399;
  margin: 0 0 32px 0;
}

.login-tabs {
  margin-bottom: 0;
}

:deep(.el-tabs__nav-wrap::after) {
  display: none;
}

:deep(.el-tabs__item) {
  font-size: 15px;
  font-weight: 500;
  padding: 0 20px;
  height: 50px;
  line-height: 50px;
}

:deep(.el-tabs__active-bar) {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  height: 3px;
  border-radius: 3px;
}

:deep(.el-tabs__item.is-active) {
  color: #667eea;
}

.login-form {
  margin-top: 24px;
}

:deep(.el-input__wrapper) {
  border-radius: 10px;
  box-shadow: none;
  border: 1px solid #dcdfe6;
  transition: all 0.2s;
}

:deep(.el-input__wrapper:hover) {
  border-color: #c0c4cc;
}

:deep(.el-input__wrapper.is-focus) {
  border-color: #667eea;
  box-shadow: 0 0 0 2px rgba(102,126,234,0.1);
}

.submit-btn {
  width: 100%;
  height: 44px;
  font-size: 15px;
  font-weight: 600;
  border-radius: 10px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  box-shadow: 0 4px 12px rgba(102,126,234,0.4);
  transition: all 0.2s;
}

.submit-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(102,126,234,0.5);
}

.code-input {
  display: flex;
  gap: 12px;
  width: 100%;
}

.code-input .el-input {
  flex: 1;
}

.code-btn {
  min-width: 120px;
  border-radius: 10px;
}
</style>
