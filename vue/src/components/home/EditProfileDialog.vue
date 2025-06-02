<template>
  <el-dialog
    v-model="dialogVisible"
    title="修改个人信息"
    width="500px"
    @close="handleClose"
  >
    <el-form
      ref="formRef"
      :model="formData"
      :rules="rules"
      label-width="100px"
    >
      <el-form-item label="用户名" prop="username">
        <el-input v-model="formData.username" />
      </el-form-item>
      <el-form-item label="邮箱" prop="email">
        <el-input v-model="formData.email" />
      </el-form-item>
      <el-form-item label="新密码" prop="password">
        <el-input v-model="formData.password" type="password" show-password />
      </el-form-item>
      <el-form-item label="确认密码" prop="confirmPassword">
        <el-input v-model="formData.confirmPassword" type="password" show-password />
      </el-form-item>
    </el-form>
    <template #footer>
      <span class="dialog-footer">
        <el-button @click="handleClose">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确认</el-button>
      </span>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, watch } from 'vue'
import { ElMessage } from 'element-plus'

const props = defineProps({
  modelValue: Boolean,
  userInfo: {
    type: Object,
    required: true
  }
})

const emit = defineEmits(['update:modelValue', 'update-profile'])

const dialogVisible = ref(false)
const formRef = ref(null)

const formData = reactive({
  username: '',
  email: '',
  password: '',
  confirmPassword: ''
})

// 监听 modelValue 的变化
watch(() => props.modelValue, (newVal) => {
  dialogVisible.value = newVal
  if (newVal) {
    // 当对话框打开时，重置表单数据
    formData.username = props.userInfo.username
    formData.email = props.userInfo.email
    formData.password = ''
    formData.confirmPassword = ''
  }
})

// 监听 dialogVisible 的变化
watch(dialogVisible, (newVal) => {
  emit('update:modelValue', newVal)
})

const validatePass = (rule, value, callback) => {
  if (value === '') {
    callback()
  } else {
    if (formData.confirmPassword !== '') {
      formRef.value?.validateField('confirmPassword')
    }
    callback()
  }
}

const validatePass2 = (rule, value, callback) => {
  if (value === '') {
    callback()
  } else if (value !== formData.password) {
    callback(new Error('两次输入密码不一致!'))
  } else {
    callback()
  }
}

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '长度在 3 到 20 个字符', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱地址', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ],
  password: [
    { validator: validatePass, trigger: 'blur' }
  ],
  confirmPassword: [
    { validator: validatePass2, trigger: 'blur' }
  ]
}

const handleClose = () => {
  dialogVisible.value = false
  emit('update:modelValue', false)
}

const handleSubmit = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate((valid) => {
    if (valid) {
      emit('update-profile', {
        username: formData.username,
        email: formData.email,
        password: formData.password || undefined
      })
      handleClose()
      ElMessage.success('个人信息更新成功')
    }
  })
}
</script>

<style scoped>
.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
</style> 