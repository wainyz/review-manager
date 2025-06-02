<template>
  <div class="home-container">
    <HomeHeader
      @toggle-sidebar="toggleSidebar"
      @handle-upload="handleUpload"
      @start-random-review="startRandomReview"
      @toggle-theme="toggleTheme"
      @show-profile="showProfile"
      @refresh-data="refreshData"
    />

    <DocumentSidebar
      v-model="sidebarVisible"
      :documents="filteredDocuments"
      v-model:searchQuery="searchQuery"
      @select-document="handleDocumentSelect"
    />

    <ProfileDrawer
      v-model="profileVisible"
      :userInfo="userInfo"
      @update-profile="handleProfileUpdate"
      @logout="handleLogout"
    />

    <MainContent :currentView="currentView" :documentId="selectedDocumentId" />

    <UploadDialog
      v-model="uploadDialogVisible"
      @submit="submitUpload"
      @close="handleUploadClose"
    />
  </div>
</template>

<script setup>
import { reactive, ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { getFullUrl } from '../config/api'
import request from '../config/axios'
import { API_PATHS } from '../config/api'
import { ElMessage } from 'element-plus'
import {
  HomeHeader,
  DocumentSidebar,
  ProfileDrawer,
  UploadDialog,
  MainContent,
  StatusFooter
} from '../components/home'

const router = useRouter()

// 当前视图状态
const currentView = ref('welcome')
const selectedDocumentId = ref(0)

// 个人信息抽屉状态
const profileVisible = ref(false)
const isDarkTheme = ref(localStorage.getItem('theme') === 'dark')

const handleDocumentSelect = (documentId) => {
  selectedDocumentId.value = documentId
  currentView.value = 'document'
}

const toggleTheme = () => {
  isDarkTheme.value = !isDarkTheme.value
  const theme = isDarkTheme.value ? 'dark' : 'light'
  localStorage.setItem('theme', theme)
  document.documentElement.setAttribute('data-theme', theme)
  document.documentElement.classList.toggle('dark', isDarkTheme.value)
}

// 个人信息
const userInfo = reactive({
  username: JSON.parse(localStorage.getItem('userInfo'))?.username || '用户名',
  email: JSON.parse(localStorage.getItem('userInfo'))?.email || '',
  documentCount: 0,
})

// 显示个人信息
const showProfile = () => {
  // 从localStorage获取最新的用户信息
  const storedUserInfo = JSON.parse(localStorage.getItem('userInfo'))
  if (storedUserInfo) {
    userInfo.username = storedUserInfo.username
    userInfo.email = storedUserInfo.email
  }
  profileVisible.value = true
}
const updateProfile = async () => {
  const response = await request.get(getFullUrl(API_PATHS.DocumentCount))
  userInfo.documentCount = response.data.data
}

onMounted(() => {
  updateProfile()
})

// 退出登录
const handleLogout = () => {
  localStorage.removeItem('token')
  localStorage.removeItem('userInfo')
  router.push('/login')
}

// 刷新页面数据
const refreshData = async () => {
  try {
    // 根据当前视图刷新相应的数据
    if (currentView.value === 'review') {
      const response = await request.get(API_PATHS.GET_CURRENT_QUESTION)
      if (response.data.code === 200) {
        questions.value = response.data.data
      }
    } else if (currentView.value === 'document') {
      // 刷新文档列表
      await fetchDocuments()
    }
  } catch (error) {
    console.error('刷新数据失败:', error)
    ElMessage.error('刷新数据失败')
  }
}

// 开始随机复习
const startRandomReview = async () => {
  try {
    headerRef.value.startProgress()
    const response = await request.get(API_PATHS.GenerateQuestions)
    if (response.data.code === 200) {
      currentView.value = 'review'
      ElMessage.success('题目生成成功')
    }
  } catch (error) {
    console.error('生成题目失败:', error)
    ElMessage.error('生成题目失败')
  }
}

// 文件上传对话框状态
const uploadDialogVisible = ref(false)
const uploadLoading = ref(false)

// 处理上传
const handleUpload = () => {
  uploadDialogVisible.value = true
}

// 提交上传
const submitUpload = async (formData) => {
  try {
    uploadLoading.value = true
    // 验证表单数据
    if (!formData.get('file')) {
      throw new Error('请选择要上传的文件')
    }
    if (!formData.get('title')) {
      throw new Error('请输入文档标题')
    }
    
    // 确保isPublish字段为字符串类型
    const isPublish = formData.get('isPublish')
    formData.set('isPublish', isPublish === 'true' ? 'true' : 'false')
    
    const response = await request.post(
      getFullUrl(API_PATHS.SubmitDocument),
      formData,
      {
        headers: {
          'Content-Type': 'multipart/form-data'
        }
      }
    )
    
    if (response.data.code === 200) {
      ElMessage.success('上传成功')
      // 刷新文档列表
      await searchDocuments()
      uploadDialogVisible.value = false
    } else {
      throw new Error(response.data.message || '上传失败')
    }
  } catch (error) {
  } finally {
    uploadLoading.value = false
  }
}
// const submitUpload = async () => {
//   if (!formRef.value) return
//   await formRef.value.validate(async (valid) => {
//     if (valid) {
//       loading.value = true
//       try {
//         emit('submit', {
//           file: form.value.file,
//           title: form.value.title,
//           isPublish: form.value.isPublish
//         })
//       } finally {
//         loading.value = false
//       }
//     }
//   })
// }
// 处理上传对话框关闭
const handleUploadClose = () => {
  uploadDialogVisible.value = false
  }

// 个人文档抽屉状态
const sidebarVisible = ref(false)
const searchQuery = ref('')
const toggleSidebar = () => {
  sidebarVisible.value = !sidebarVisible.value
  searchDocuments()
}

// 个人文档列表
const documents = ref([])

// 过滤后的文档列表
const filteredDocuments = computed(() => {
  if (!searchQuery.value) return documents.value
  const query = searchQuery.value.toLowerCase()
  return documents.value.filter(doc => doc.title.toLowerCase().includes(query))
})
// 查询文档列表
const searchDocuments = async () => {
  try {
    const response = await request.get(getFullUrl(API_PATHS.TitleList))
    documents.value = response.data.data
  } catch (error) {
    console.error('查询文档列表失败:', error)
  }
}

// 编辑个人信息
const handleProfileUpdate = async (updatedInfo) => {
  try {
    const response = await request.post(getFullUrl(API_PATHS.UpdateProfile), updatedInfo)
    if (response.data.code === 200) {
      // 更新本地存储的用户信息
      const storedUserInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
      Object.assign(storedUserInfo, updatedInfo)
      localStorage.setItem('userInfo', JSON.stringify(storedUserInfo))
      
      // 更新当前显示的用户信息
      userInfo.username = updatedInfo.username
      userInfo.email = updatedInfo.email
      
      ElMessage.success('个人信息更新成功')
    } else {
      ElMessage.error(response.data.message || '更新失败')
    }
  } catch (error) {
    console.error('更新个人信息失败:', error)
    ElMessage.error('更新个人信息失败')
  }
}

// 最低掌握度文档
const lowestMasteryDoc = ref('')

const handleDocumentClick = async (fileId) => {
  if (!fileId || currentView.value !== 'document') return
  await fetchDocumentData(fileId)
}
</script>

<style scoped>
.home-container {
  height: 100vh;
  display: flex;
  flex-direction: column;
}
</style>