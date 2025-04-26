<template>
  <div class="register-container">
    <el-card class="register-card">
      <template #header>
        <h2>注册</h2>
      </template>
      <el-form :model="registerForm" :rules="rules" ref="registerFormRef">
        <el-form-item prop="email">
          <el-input v-model="registerForm.email" placeholder="请输入邮箱">
            <template #prefix>
              <el-icon><Message /></el-icon>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="registerForm.password" type="password" placeholder="请输入密码">
            <template #prefix>
              <el-icon><Lock /></el-icon>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item prop="captcha">
          <div class="captcha-container">
            <el-input v-model="registerForm.captcha" placeholder="请输入验证码">
              <template #prefix>
                <el-icon><Key /></el-icon>
              </template>
            </el-input>
            <img :src="captchaUrl" @click="refreshCaptcha" class="captcha-image" alt="验证码">
          </div>
        </el-form-item>
        <el-form-item prop="emailCode" v-if="showEmailCode">
          <div class="email-code-container">
            <el-input v-model="registerForm.emailCode" placeholder="请输入邮箱验证码">
              <template #prefix>
                <el-icon><Message /></el-icon>
              </template>
            </el-input>
            <el-button 
              type="primary" 
              :disabled="countdown > 0"
              @click="sendEmailCode"
            >
              {{ countdown > 0 ? `${countdown}秒后重试` : '发送邮箱验证码' }}
            </el-button>
          </div>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleRegister" class="register-button">
            {{ showEmailCode ? '完成注册' : '下一步' }}
          </el-button>
        </el-form-item>
        <div class="login-link">
          <router-link to="/login">已有账号？立即登录</router-link>
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
import { API_PATHS } from '../config/api'
import request from '../config/axios'

const router = useRouter()
const registerFormRef = ref(null)
// $需要修改$ - 获取验证码的API地址
const captchaUrl = ref('')
const showEmailCode = ref(false)
const countdown = ref(0)

const registerForm = reactive({
  email: '',
  password: '',
  image_code: '',
  image_id: '0000',
  emailCode: ''
})

const rules = {
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能小于6位', trigger: 'blur' }
  ],
  captcha: [
    { required: true, message: '请输入验证码', trigger: 'blur' },
    { len: 4, message: '验证码长度必须为4位', trigger: 'blur' }
  ],
  emailCode: [
    { required: true, message: '请输入邮箱验证码', trigger: 'blur' },
    { len: 4, message: '验证码长度必须为4位', trigger: 'blur' }
  ]
}

// 定义刷新验证码的方法
const refreshCaptcha = async () => {
  try {
    const { data } = await (await request.post(API_PATHS.REFRESH_CAPTCHA + '?image_id=' + registerForm.image_id)).data
    registerForm.image_id = data
    captchaUrl.value = `${request.defaults.baseURL}${API_PATHS.GET_CAPTCHA}/${data}`
  } catch (error) {
    ElMessage.error('发送验证码失败')
  }
}
// 在组件挂载时自动执行一次刷新验证码
onMounted(() => {
  refreshCaptcha()
})


const startCountdown = () => {
  countdown.value = 60
  const timer = setInterval(() => {
    countdown.value--
    if (countdown.value <= 0) {
      clearInterval(timer)
    }
  }, 1000)
}

const sendEmailCode = async () => {
  try {
    await request.post(API_PATHS.SEND_EMAIL_CODE, {
      email: registerForm.email,
      image_code: registerForm.captcha,
      image_id: registerForm.image_id
    })
    ElMessage.success('验证码已发送到您的邮箱')
    startCountdown()
  } catch (error) {
    ElMessage.error('邮箱验证码发送失败')
    refreshCaptcha()
  }
}

const handleRegister = async () => {
  if (!registerFormRef.value) return

  try {
    await registerFormRef.value.validate()
    if (!showEmailCode.value) {
      showEmailCode.value = true
      await sendEmailCode()
      return
    }

    const loading = ElLoading.service({
      lock: true,
      text: '注册中...',
      background: 'rgba(0, 0, 0, 0.7)'
    })

    try {
      await request.post(API_PATHS.REGISTER, {
        email: registerForm.email,
        password: registerForm.password,
        email_code: registerForm.emailCode
      })
      ElMessage.success('注册成功')
      router.push('/login')
    } catch (error) {
      ElMessage.error('注册失败')
      refreshCaptcha()
    } finally {
      loading.close()
    }
  } catch (validationError) {
    ElMessage.error('请正确填写所有必填项')
  }
}
</script>

<style scoped>
.register-container {
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: #f5f5f5;
}

.register-card {
  width: 400px;
}

.captcha-container,
.email-code-container {
  display: flex;
  gap: 10px;
}

.captcha-image {
  height: 40px;
  cursor: pointer;
}

.register-button {
  width: 100%;
}

.login-link {
  text-align: center;
  margin-top: 10px;
}
</style>