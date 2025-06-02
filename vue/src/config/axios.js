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
          ElMessage.error('您的token已过期，请重新登录')
          break
        case 404:
          ElMessage.error('请求错误，未找到该资源'+error.response.data.message)
          break
        case 500:
          ElMessage.error('500服务器错误')
          break
        default:
          ElMessage.error(`${error.response.data.message}`)
      }
    } else {
      if (error.message.includes('timeout')) {
        ElMessage.error('请求超时')
      } else {
        //ElMessage.error('未知错误')
      }
    }
    return Promise.reject(error)
  }
)

export default instance