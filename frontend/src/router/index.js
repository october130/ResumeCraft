import { createRouter, createWebHashHistory } from 'vue-router'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/Login.vue')
  },
  {
    path: '/',
    component: () => import('../views/Layout.vue'),
    redirect: '/dashboard',
    children: [
      { path: 'dashboard', name: 'Dashboard', component: () => import('../views/Dashboard.vue') },

      // 简历模块
      { path: 'resume/upload', name: 'ResumeUpload', component: () => import('../views/ResumeUpload.vue') },
      { path: 'resume', name: 'ResumeList', component: () => import('../views/ResumeList.vue') },
      { path: 'resume/:id', name: 'ResumeDetail', component: () => import('../views/ResumeDetail.vue') },
      { path: 'resume/scores', name: 'ResumeScores', component: () => import('../views/ResumeScores.vue') },

      // 面试模块
      { path: 'interview/create', name: 'InterviewCreate', component: () => import('../views/InterviewCreate.vue') },
      { path: 'interview', name: 'InterviewList', component: () => import('../views/InterviewList.vue') },
      { path: 'interview/reports', name: 'InterviewReports', component: () => import('../views/InterviewReports.vue') },
      { path: 'interview/session/:sessionId', name: 'InterviewSession', component: () => import('../views/InterviewSession.vue') },
      { path: 'interview/report/:sessionId', name: 'InterviewReport', component: () => import('../views/InterviewReport.vue') },

      // 知识库
      { path: 'knowledge', name: 'KnowledgeBase', component: () => import('../views/KnowledgeBase.vue') },

      // 数据
      { path: 'analytics', name: 'Analytics', component: () => import('../views/Analytics.vue') },

      // 我的
      { path: 'profile', name: 'Profile', component: () => import('../views/Profile.vue') },
      { path: 'account/security', name: 'AccountSecurity', component: () => import('../views/AccountSecurity.vue') }
    ]
  }
]

const router = createRouter({
  history: createWebHashHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  if (to.path !== '/login' && !token) {
    next('/login')
  } else {
    next()
  }
})

export default router
