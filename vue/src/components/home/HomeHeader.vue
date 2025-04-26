<template>
  <header class="header">
    <div class="header-left">
      <el-button @click="$emit('toggle-sidebar')">
        <el-icon><Document /></el-icon>
        个人文档
      </el-button>
      <el-button type="primary" @click="$emit('handle-upload')" class="upload-btn">
        <el-icon><Upload /></el-icon>
        上传文档
      </el-button>
    </div>
    <div class="header-center">
      <el-button v-if="!selectedDocumentId" type="primary" @click="$emit('start-random-review')">
        <el-icon><Reading /></el-icon>
        随机复习
      </el-button>
      <el-button v-else type="success" @click="$emit('start-review', selectedDocumentId)">
        <el-icon><Reading /></el-icon>
        开始复习
      </el-button>
    </div>
    <div class="header-right">
      <el-progress
        v-if="showProgress"
        type="circle"
        :percentage="progressPercentage"
        :status="progressStatus"
        :width="40"
      >
        <template #default>
          <span class="progress-info">{{ waitTimeText }}</span>
        </template>
      </el-progress>
      <el-button @click="$emit('toggle-theme')" class="theme-btn" :icon="Sunny" circle />
      <el-avatar @click="$emit('show-profile')" :size="40" src="https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png" />
    </div>
  </header>
</template>

<script setup>
import { Document, Reading, Upload, Sunny } from '@element-plus/icons-vue'
import { ref, computed, onUnmounted } from 'vue'
import request from '../../config/axios'
import { API_PATHS } from '../../config/api'

const props = defineProps({
  selectedDocumentId: {
    type: Number,
    default: 0
  }
})

const emit = defineEmits(['toggle-sidebar', 'handle-upload', 'start-random-review', 'start-review', 'toggle-theme', 'show-profile', 'refresh-data'])

// 进度指示器相关状态
const showProgress = ref(false)
const waitTime = ref(0)
const progressTimer = ref(null)
const checkStatusTimer = ref(null)
const progressPercentage = ref(0)
const progressStatus = ref('')

const waitTimeText = computed(() => {
  return `${Math.ceil(waitTime.value)}秒`
})

// 开始请求进度追踪
const startProgress = async () => {
  showProgress.value = true
  progressStatus.value = 'warning'
  
  try {
    const response = await request.get(API_PATHS.AvarageWaitTime)
    waitTime.value = response.data.data
    let elapsed = 0
    
    progressTimer.value = setInterval(() => {
      elapsed += 1
      progressPercentage.value = Math.min(Math.round((elapsed / waitTime.value) * 100), 99)
      
      if (elapsed >= waitTime.value) {
        clearInterval(progressTimer.value)
      }
    }, 1000)
    
    startStatusCheck()
  } catch (error) {
    console.error('获取等待时间失败:', error)
    showProgress.value = false
  }
}

// 检查请求状态
const startStatusCheck = () => {
  checkStatusTimer.value = setInterval(async () => {
    try {
      const response = await request.get(API_PATHS.RequestStatus)
      if (response.data.code === 200) {
        clearInterval(checkStatusTimer.value)
        clearInterval(progressTimer.value)
        progressPercentage.value = 100
        progressStatus.value = 'success'
        setTimeout(() => {
          showProgress.value = false
          emit('refresh-data')
        }, 1000)
      }
    } catch (error) {
      console.error('检查状态失败:', error)
    }
  }, 2000)
}

// 清理定时器
onUnmounted(() => {
  if (progressTimer.value) clearInterval(progressTimer.value)
  if (checkStatusTimer.value) clearInterval(checkStatusTimer.value)
})
</script>

<style scoped>
.header {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  height: 60px;
  background-color: var(--header-bg);
  box-shadow: 0 2px 4px var(--shadow-color);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  z-index: 1000;
}

.header-left,
.header-center,
.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.progress-info {
  font-size: 12px;
  color: var(--el-text-color-primary);
}
</style>