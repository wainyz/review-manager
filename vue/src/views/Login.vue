<template>
  <div class="login-container">
    <el-card class="login-card">
      <template #header>
        <h2>登录</h2>
      </template>
      <el-form :model="loginForm" :rules="rules" ref="loginFormRef">
        <el-form-item prop="email">
          <el-input v-model="loginForm.email" placeholder="请输入邮箱">
            <template #prefix>
              <el-icon><Message /></el-icon>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="loginForm.password" type="password" placeholder="请输入密码">
            <template #prefix>
              <el-icon><Lock /></el-icon>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item prop="image_code">
          <div class="captcha-container">
            <el-input v-model="loginForm.image_code" placeholder="请输入验证码">
              <template #prefix>
                <el-icon><Key /></el-icon>
              </template>
            </el-input>
            <img :src="captchaUrl" @click="refreshCaptcha" class="captcha-image" alt="验证码">
          </div>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleLogin" class="login-button">登录</el-button>
        </el-form-item>
        <div class="register-link">
          <router-link to="/register">还没有账号？立即注册</router-link>
        </div>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Message, Lock, Key } from '@element-plus/icons-vue'
import { ElMessage, ElLoading } from 'element-plus'
import { API_PATHS, getFullUrl,BASE_URL } from '../config/api'
import instance from '../config/axios'

const router = useRouter()
const loginForm = reactive({
  email: '',
  password: '',
  image_id: '0000',
  image_code: '' 
})

// 修改验证码URL的定义
const captchaUrl = ref('')

const rules = {
  email: [
    { required: true, message: '请输入邮箱或者用户名', trigger: 'blur' },
    { type: 'string', message: '请输入正确的字符和长度', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 8, message: '密码长度不能小于8位', trigger: 'blur' }
  ],
  image_code: [
    { required: true, message: '请输入验证码', trigger: 'blur' },
    { len: 4, message: '验证码长度必须为4位', trigger: 'blur' }
  ]
}

// 定义刷新验证码的方法
const refreshCaptcha = async () => {
  try {
    const { data } = await (await instance.post(getFullUrl(API_PATHS.REFRESH_CAPTCHA) + '?image_id=' + loginForm.image_id)).data
    loginForm.image_id = data
    captchaUrl.value = `${BASE_URL}${API_PATHS.GET_CAPTCHA}/${data}`
  } catch (error) {
    ElMessage.error('获取验证码失败')
  }
}

// 在组件挂载时自动执行一次刷新验证码
onMounted(() => {
  refreshCaptcha()
})
const loginFormRef = ref(null)
const handleLogin = async () => {
  if (!loginFormRef.value) return

  try {
    // 先进行表单验证
    await loginFormRef.value.validate()
  } catch (validationError) {
    // 表单验证失败
    ElMessage.error('请正确填写所有必填项')
  }
  // 显示加载动画
  const loading = ElLoading.service({
    lock: true,
    text: '登录中...',
    background: 'rgba(0, 0, 0, 0.7)'
  })
  let response = {}
  try {
    response = await instance.post(getFullUrl(API_PATHS.LOGIN), {
      email: loginForm.email,
      password: loginForm.password,
      image_code: loginForm.image_code,
      image_id: loginForm.image_id
    })
    let {data} = response.data
    // 存储用户信息和token
    let token = data.authorization
    if (!token) {
      throw new Error('未收到授权信息')
    }
    // 存储用户信息和token
    localStorage.setItem('token', token)
    localStorage.setItem('userInfo', JSON.stringify(data))
    localStorage.setItem('userId', data.user_id)
    ElMessage.success('登录成功')
    await router.push('/home')
  } catch (error) {
    ElMessage.error(`${error.response.data}`) 
  } finally {
    // 刷新验证码
    refreshCaptcha()
    loading.close()
  }
}
</script>

<style scoped>
.login-container {
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
}

.login-card {
  width: 400px;
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.captcha-container {
  display: flex;
  gap: 10px;
}

.captcha-image {
  height: 40px;
  cursor: pointer;
}

.login-button {
  width: 100%;
}

.register-link {
  text-align: center;
  margin-top: 10px;
}
</style>