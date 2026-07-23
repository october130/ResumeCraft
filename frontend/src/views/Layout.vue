<template>
  <el-container style="height: 100vh">
    <!-- 侧边栏 -->
    <el-aside width="240px" class="aside">
      <div class="logo">
        <div class="logo-icon">
          <el-icon :size="20" color="#fff"><MagicStick /></el-icon>
        </div>
        <span class="logo-text">AI 简历分析</span>
      </div>

      <div class="user-panel">
        <el-avatar :size="40" icon="UserFilled" style="background: linear-gradient(135deg, #667eea 0%, #764ba2 100%)" />
        <div class="user-panel-info">
          <span class="user-panel-name">{{ userStore.username || '用户' }}</span>
          <span class="user-panel-role">
            <el-icon :size="12"><CircleCheck /></el-icon>
            已登录
          </span>
        </div>
      </div>

      <el-scrollbar class="menu-scrollbar">
        <el-menu
          :default-active="route.path"
          router
          background-color="transparent"
          text-color="rgba(255,255,255,0.7)"
          active-text-color="#fff"
        >
          <div class="menu-group-label">概览</div>
          <el-menu-item index="/dashboard">
            <el-icon><Odometer /></el-icon>
            <span>仪表盘</span>
          </el-menu-item>

          <div class="menu-group-label">简历</div>
          <el-menu-item index="/resume/upload">
            <el-icon><Upload /></el-icon>
            <span>上传简历</span>
          </el-menu-item>
          <el-menu-item index="/resume">
            <el-icon><List /></el-icon>
            <span>简历列表</span>
          </el-menu-item>
          <el-menu-item index="/resume/scores">
            <el-icon><TrendCharts /></el-icon>
            <span>评分总览</span>
          </el-menu-item>

          <div class="menu-group-label">面试</div>
          <el-menu-item index="/interview/create">
            <el-icon><Plus /></el-icon>
            <span>创建面试</span>
          </el-menu-item>
          <el-menu-item index="/interview">
            <el-icon><List /></el-icon>
            <span>面试记录</span>
          </el-menu-item>
          <el-menu-item index="/interview/reports">
            <el-icon><DocumentChecked /></el-icon>
            <span>评估报告</span>
          </el-menu-item>

          <div class="menu-group-label">AI 能力</div>
          <el-menu-item index="/knowledge">
            <el-icon><Coin /></el-icon>
            <span>知识库</span>
          </el-menu-item>

          <div class="menu-group-label">数据</div>
          <el-menu-item index="/analytics">
            <el-icon><DataAnalysis /></el-icon>
            <span>数据分析</span>
          </el-menu-item>

          <div class="menu-group-label">我的</div>
          <el-menu-item index="/profile">
            <el-icon><UserFilled /></el-icon>
            <span>个人中心</span>
          </el-menu-item>
          <el-menu-item index="/account/security">
            <el-icon><Lock /></el-icon>
            <span>账号安全</span>
          </el-menu-item>
        </el-menu>
      </el-scrollbar>

      <div class="sidebar-footer">
        <el-button text class="logout-btn" @click="handleLogout">
          <el-icon><SwitchButton /></el-icon>
          <span>退出登录</span>
        </el-button>
      </div>
    </el-aside>

    <!-- 主内容区 -->
    <el-container>
      <el-header class="header">
        <div class="header-left">
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ path: '/dashboard' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item v-if="route.path !== '/dashboard'">
              {{ breadcrumbName }}
            </el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div class="header-right">
          <el-tooltip content="刷新页面" placement="bottom">
            <el-button circle @click="handleRefresh">
              <el-icon><Refresh /></el-icon>
            </el-button>
          </el-tooltip>
          <el-dropdown @command="handleCommand" trigger="click">
            <div class="user-dropdown">
              <el-avatar :size="32" icon="UserFilled" style="background: linear-gradient(135deg, #667eea 0%, #764ba2 100%)" />
              <span class="user-name">{{ userStore.username || '用户' }}</span>
              <el-icon><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="/profile">
                  <el-icon><UserFilled /></el-icon>个人中心
                </el-dropdown-item>
                <el-dropdown-item command="/account/security">
                  <el-icon><Lock /></el-icon>账号安全
                </el-dropdown-item>
                <el-dropdown-item divided command="logout">
                  <el-icon><SwitchButton /></el-icon>退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>
      <el-main class="main-content">
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '../store/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const breadcrumbName = computed(() => {
  const map = {
    '/dashboard': '仪表盘',
    '/resume/upload': '上传简历',
    '/resume': '简历列表',
    '/resume/scores': '评分总览',
    '/interview/create': '创建面试',
    '/interview': '面试记录',
    '/interview/reports': '评估报告',
    '/knowledge': 'AI 知识库',
    '/analytics': '数据分析',
    '/profile': '个人中心',
    '/account/security': '账号安全'
  }
  return map[route.path] || ''
})

function handleCommand(command) {
  if (command === 'logout') {
    userStore.logout()
    router.push('/login')
  } else if (command.startsWith('/')) {
    router.push(command)
  }
}

function handleLogout() {
  userStore.logout()
  router.push('/login')
}

function handleRefresh() {
  window.location.reload()
}
</script>

<style scoped>
.aside {
  background: linear-gradient(180deg, #1e3a5f 0%, #2d5a87 100%);
  display: flex;
  flex-direction: column;
  overflow: hidden;
  box-shadow: 2px 0 8px rgba(0,0,0,0.1);
}

.logo {
  height: 64px;
  display: flex;
  align-items: center;
  padding: 0 20px;
  gap: 12px;
  background: rgba(0,0,0,0.15);
  flex-shrink: 0;
}

.logo-icon {
  width: 36px;
  height: 36px;
  border-radius: 8px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 2px 8px rgba(102, 126, 234, 0.4);
}

.logo-text {
  color: #fff;
  font-size: 18px;
  font-weight: 600;
  letter-spacing: 0.5px;
}

.user-panel {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 20px 20px 16px;
  border-bottom: 1px solid rgba(255,255,255,0.08);
  flex-shrink: 0;
}

.user-panel-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.user-panel-name {
  color: #fff;
  font-size: 14px;
  font-weight: 500;
}

.user-panel-role {
  color: rgba(255,255,255,0.6);
  font-size: 12px;
  display: flex;
  align-items: center;
  gap: 4px;
}

.menu-scrollbar {
  flex: 1;
  overflow: hidden;
}

.el-menu {
  border-right: none;
  padding: 8px 12px;
}

.menu-group-label {
  padding: 16px 16px 6px;
  font-size: 11px;
  color: rgba(255,255,255,0.4);
  font-weight: 500;
  letter-spacing: 0.5px;
  text-transform: uppercase;
}

:deep(.el-menu-item) {
  border-radius: 8px;
  margin-bottom: 4px;
  transition: all 0.2s;
}

:deep(.el-menu-item:hover) {
  background: rgba(255,255,255,0.08) !important;
}

:deep(.el-menu-item.is-active) {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%) !important;
  box-shadow: 0 2px 8px rgba(102, 126, 234, 0.3);
}

.sidebar-footer {
  padding: 12px 16px;
  border-top: 1px solid rgba(255,255,255,0.08);
  flex-shrink: 0;
}

.logout-btn {
  color: rgba(255,255,255,0.7) !important;
  width: 100%;
  justify-content: flex-start;
  gap: 8px;
  transition: all 0.2s;
}

.logout-btn:hover {
  color: #fff !important;
  background: rgba(255,255,255,0.08) !important;
}

.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #fff;
  border-bottom: 1px solid #e8e8e8;
  padding: 0 24px;
  height: 60px;
  box-shadow: 0 1px 4px rgba(0,0,0,0.05);
}

.header-left {
  flex: 1;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.user-dropdown {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 6px 12px;
  border-radius: 8px;
  transition: background 0.2s;
}

.user-dropdown:hover {
  background: #f5f5f5;
}

.user-name {
  color: #303133;
  font-size: 14px;
  font-weight: 500;
}

.main-content {
  background: linear-gradient(135deg, #f5f7fa 0%, #e4e7eb 100%);
  overflow-y: auto;
  padding: 24px;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

:deep(.el-breadcrumb__inner) {
  font-size: 14px;
}

:deep(.el-scrollbar__thumb) {
  background: rgba(255,255,255,0.2) !important;
}

:deep(.el-scrollbar__thumb:hover) {
  background: rgba(255,255,255,0.3) !important;
}
</style>
