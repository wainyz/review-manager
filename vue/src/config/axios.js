import axios from 'axios'
import { BASE_URL } from './api'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
const router = useRouter()

// 创建axios实例
const instance = axios.create({
  baseURL: BASE_URL,
  timeout: 3000,
  headers: {
    'Content-Type': 'application/json',
   }
})

// 请求拦截器
instance.interceptors.request.use(
  config => {
    // 从localStorage获取token
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `${token}`
    }
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

// 响应拦截器
instance.interceptors.response.use(
  response => {
    const res = response.data
    if (res.code !== 200) {
      ElMessage.error(res.message || '请求失败')
      return Promise.reject(new Error(res.message || '请求失败'))
    }
    return response
  },
  error => {
    if (error.response) {
      switch (error.response.status) {
        case 401:
          ElMessage.error('未授权，请重新登录')
          // 可以在这里处理登出逻辑
          localStorage.removeItem('token')
          localStorage.removeItem('userInfo')
          router.push('/login')
          break
        case 403:
          ElMessage.error('403拒绝访问')
          break
        case 404:
          ElMessage.error('404请求错误，未找到该资源')
          break
        case 500:
          ElMessage.error('500服务器错误')
          break
        default:
          ElMessage.error(`其他错误码${error.response.status}`)
      }
    } else {
      if (error.message.includes('timeout')) {
        ElMessage.error('请求超时')
      } else {
        ElMessage.error('未知错误')
      }
    }
    return Promise.reject(error)
  }
)

export default instance