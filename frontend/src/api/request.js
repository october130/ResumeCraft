import axios from 'axios'
import { ElMessage } from 'element-plus'

// 创建 Axios 实例
const request = axios.create({
  baseURL: '/api',       // 所有请求以 /api 开头，Vite 代理转发到 8081
  timeout: 30000          // 30 秒超时
})

// 请求拦截器：自动带上 token
request.interceptors.request.use(
  config => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  error => Promise.reject(error)
)

// 响应拦截器：统一处理错误
request.interceptors.response.use(
  response => {
    const res = response.data
    // 后端统一返回 { code, message, data }
    if (res.code && res.code !== 200) {
      ElMessage.error(res.message || '请求失败')
      return Promise.reject(new Error(res.message))
    }
    return res
  },
  error => {
    if (error.response?.status === 401) {
      // token 过期或未登录，跳回登录页
      localStorage.removeItem('token')
      localStorage.removeItem('userInfo')
      window.location.href = '/#/login'
      ElMessage.error('登录已过期，请重新登录')
    } else {
      ElMessage.error(error.message || '网络错误')
    }
    return Promise.reject(error)
  }
)

export default request
