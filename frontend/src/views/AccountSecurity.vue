<template>
  <div class="account-security">
    <h2>账号安全</h2>

    <el-row :gutter="20">
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header><span>🔒 账号信息</span></template>
          <el-descriptions :column="1" border>
            <el-descriptions-item label="用户名">{{ userStore.username || '-' }}</el-descriptions-item>
            <el-descriptions-item label="用户ID">{{ userStore.userInfo?.userId || '-' }}</el-descriptions-item>
            <el-descriptions-item label="登录方式">密码登录</el-descriptions-item>
            <el-descriptions-item label="账号状态">
              <el-tag size="small" type="success">正常</el-tag>
            </el-descriptions-item>
          </el-descriptions>
        </el-card>
      </el-col>

      <el-col :span="12">
        <el-card shadow="hover">
          <template #header><span>📱 安全设置</span></template>
          <div class="security-item">
            <div class="security-left">
              <el-icon :size="20" color="#67c23a"><Iphone /></el-icon>
              <span>手机号</span>
            </div>
            <el-tag size="small" type="info">已绑定</el-tag>
          </div>
          <el-divider style="margin: 12px 0" />
          <div class="security-item">
            <div class="security-left">
              <el-icon :size="20" color="#e6a23c"><Message /></el-icon>
              <span>邮箱</span>
            </div>
            <el-tag size="small" type="info">未设置</el-tag>
          </div>
          <el-divider style="margin: 12px 0" />
          <div class="security-item">
            <div class="security-left">
              <el-icon :size="20" color="#409eff"><Lock /></el-icon>
              <span>登录密码</span>
            </div>
            <el-tag size="small" type="success">已设置</el-tag>
          </div>
        </el-card>

        <el-card shadow="hover" style="margin-top: 20px">
          <template #header><span>📋 操作记录</span></template>
          <div class="log-item"><span>上次登录</span><span style="color: #909399; font-size: 13px">当前会话</span></div>
          <el-divider style="margin: 8px 0" />
          <div class="log-item"><span>注册时间</span><span style="color: #909399; font-size: 13px">-</span></div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { useUserStore } from '../store/user'
const userStore = useUserStore()
</script>

<style scoped>
h2 { margin-bottom: 20px; }
.security-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 4px 0;
}
.security-left { display: flex; align-items: center; gap: 8px; color: #606266; }
.log-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  color: #606266;
  padding: 4px 0;
}
</style>
