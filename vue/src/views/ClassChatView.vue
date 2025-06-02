<template>
  <div class="chat-container">
    <div class="chat-main">
      <div class="chat-header">
        <el-button @click="goBack" icon="ArrowLeft">返回</el-button>
        <div class="class-info" v-if="classInfo">
          <el-avatar :size="40">
            <img src="../assets/head.png" alt="班级头像" />
          </el-avatar>
          <span class="class-name">{{ classInfo.className }}</span>
        </div>
      </div>
      
      <div class="chat-messages" ref="messagesContainer">
        <div v-if="messages.length > 0">
          <div v-for="(message, index) in messages" :key="index" 
               :class="['message-item', message.isSelf ? 'self-message' : 'other-message']">
            <el-avatar :size="30">
            <img src="../assets/head.png" alt="班级头像" />
          </el-avatar>
            <div class="message-content">
              <div class="message-sender" v-if="!message.isSelf">{{ message.senderName }}</div>
              <div class="message-text">
                {{ message.content }}
              </div>
              <div class="message-time">{{ formatTime(message.time) }}</div>
            </div>
          </div>
        </div>
        <div v-else class="empty-messages">
          <el-empty description="暂无消息记录" />
        </div>
      </div>
      
      <div class="chat-input">
        <el-input
          v-model="messageText"
          placeholder="输入消息..."
          @keyup.enter="sendMessage"
        >
        </el-input>
        <el-button type="primary" @click="sendMessage">发送</el-button>
      </div>
    </div>

    <div class="class-sidebar">
      <div class="sidebar-header">
        <h3>班级信息</h3>
        <el-button 
          v-if="isOwner" 
          type="primary" 
          size="small" 
          @click="editClassInfo"
        >
          编辑信息
        </el-button>
      </div>
      <div class="sidebar-content" v-if="classInfo">
        <el-descriptions :column="1" border>
          <!-- <el-descriptions-item label="班级ID">
            <el-tag size="small">{{ classInfo.id }}</el-tag>
          </el-descriptions-item> -->
          <el-descriptions-item label="班级名称">
            <span class="highlight">{{ classInfo.className }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="班级描述">
            <div class="description">{{ classInfo.description }}</div>
          </el-descriptions-item>
          <el-descriptions-item label="班级类型">
            <el-tag :type="classInfo.is_public === 1 ? 'success' : 'info'" size="small">
              {{ classInfo.is_public === 1 ? '公开' : '私密' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="学生数量">
            <el-tag type="warning" size="small">{{ studentCount }}人</el-tag>
          </el-descriptions-item>
        </el-descriptions>

        <div class="section-title">
          <el-icon><User /></el-icon>
          <span>教师列表</span>
        </div>
        <div class="teacher-list">
          <template v-if="teacherList.length > 0">
            <el-tag
              v-for="teacher in teacherList"
              :key="teacher"
              class="teacher-item"
              effect="plain"
              size="small"
            >
              {{ teacher }}
            </el-tag>
          </template>
          <el-empty v-else description="暂无教师" :image-size="60" />
        </div>

        <div class="section-title">
          <el-icon><Calendar /></el-icon>
          <span>考试列表</span>
        </div>
        <div class="exam-list">
          <template v-if="examList.length > 0">
            <el-card
              v-for="exam in examList"
              :key="exam"
              class="exam-item"
              shadow="hover"
            >
              {{ exam }}
            </el-card>
          </template>
          <el-empty v-else description="暂无考试" :image-size="60" />
        </div>
      </div>
    </div>

    <!-- 添加编辑对话框 -->
    <el-dialog
      v-model="editDialogVisible"
      title="编辑班级信息"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="editFormRef"
        :model="editForm"
        :rules="editRules"
        label-width="100px"
      >
        <el-form-item label="班级名称" prop="className">
          <el-input v-model="editForm.className" placeholder="请输入班级名称" />
        </el-form-item>
        
        <el-form-item label="班级描述" prop="description">
          <el-input
            v-model="editForm.description"
            type="textarea"
            :rows="3"
            placeholder="请输入班级描述"
          />
        </el-form-item>
        
        <el-form-item label="班级类型" prop="isPublic">
          <el-switch
            v-model="editForm.isPublic"
            :active-value="1"
            :inactive-value="0"
            active-text="公开"
            inactive-text="私密"
          />
        </el-form-item>
        
        <el-form-item label="学生列表" prop="studentList">
          <el-input
            v-model="editForm.studentList"
            type="textarea"
            :rows="3"
            placeholder="请输入学生ID，用逗号分隔"
          />
        </el-form-item>
        
        <el-form-item label="教师列表" prop="teacherList">
          <el-input
            v-model="editForm.teacherList"
            type="textarea"
            :rows="3"
            placeholder="请输入教师ID，用逗号分隔"
          />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="editDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitEdit" :loading="submitting">
            确认
          </el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 添加消息通知图标 -->
    <div class="message-notification" v-if="unreadMessages.size > 0">
      <el-badge :value="unreadMessages.size" class="notification-badge">
        <el-button @click="showNotifications = true" icon="Bell" circle />
      </el-badge>
    </div>
    
    <!-- 消息通知抽屉 -->
    <el-drawer
      v-model="showNotifications"
      title="消息通知"
      direction="rtl"
      size="350px"
    >
      <template #header>
        <div class="notification-header">
          <span>消息通知</span>
          <el-button 
            type="text" 
            @click="markAllMessagesAsRead"
            :disabled="unreadMessages.size === 0"
          >
            全部已读
          </el-button>
        </div>
      </template>
      
      <div class="notification-list">
        <div v-if="messageNotifications.length > 0">
          <div 
            v-for="notification in messageNotifications" 
            :key="notification.id"
            class="notification-item"
            :class="{ 'unread': !notification.read }"
            @click="handleNotificationClick(notification)"
          >
            <div class="notification-content">
              <div class="notification-title">
                {{ notification.className }}
              </div>
              <div class="notification-message">
                {{ notification.senderName }}: {{ notification.content }}
              </div>
              <div class="notification-time">
                {{ formatTime(notification.time) }}
              </div>
            </div>
          </div>
        </div>
        <el-empty v-else description="暂无消息通知" />
      </div>
    </el-drawer>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, nextTick, watch, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import instance from '../config/axios'

const route = useRoute()
const router = useRouter()

// 班级信息
const classInfo = ref(null)
const userAvatar = '../assets/head.png'
const messages = ref([])
const messageText = ref('')
const messagesContainer = ref(null)

// 计算学生数量
const studentCount = computed(() => {
  if (!classInfo.value?.student_list) return 0
  try {
    const studentList = JSON.parse(classInfo.value.student_list)
    return studentList.length
  } catch {
    return 0
  }
})

// 计算教师列表
const teacherList = computed(() => {
  if (!classInfo.value?.teacher_list) return []
  try {
    return JSON.parse(classInfo.value.teacher_list)
  } catch {
    return []
  }
})

// 计算考试列表
const examList = computed(() => {
  if (!classInfo.value?.exam_list) return []
  try {
    return JSON.parse(classInfo.value.exam_list)
  } catch {
    return []
  }
})

// 连接WebSocket
import webSocketService from '../config/websocket-service';
// STOMP消息通信
webSocketService.subscribeAnyChannel(`/user/class/${route.params.classId}/message`, (message) => {
  const data = JSON.parse(message.body);
  console.log('聊天框 收到用户通知:', data);
  // 检测消息类型是否为61
    // 解析消息内容
    const contentParts = data.content.split(':');
    if (contentParts.length >= 3) {
      const senderId = contentParts[0]; // 消息方向
      const senderName = contentParts[1]
      const messageContent = contentParts[2]; // 实际消息内容
      
      // 获取当前用户ID和好友ID
      const userId = localStorage.getItem('userId'); // 假设用户ID存储在localStorage中
      
      // 判断消息是否由自己发送
      let isSelf = senderId === userId
      // 构建消息对象并添加到消息队列
      const messageObj = {
        content: messageContent,
        time: Date.now(),
        isSelf: isSelf,
        senderId: senderId,
        senderName: senderName
      };
      
      messages.value.push(messageObj);
      
      // 确保视图更新后滚动到底部
      nextTick(() => scrollToBottom());
    }
})

// 获取班级信息
const getClassInfo = async () => {
  try {
    if (route.params.classId) {
      let formdata = new FormData()
      formdata.append("classId", route.params.classId)
      const response = await instance.post(`/class/info`, formdata, {
        headers: {
          'Content-Type': 'multipart/form-data'
        }
      })
      if (response.data.code === 200 && response.data.data) {
        const classData = response.data.data
        classInfo.value = {
          id: classData.id,
          owner: classData.owner,
          className: classData.class_name,
          description: classData.description,
          student_list: classData.student_list || '[]',
          teacher_list: classData.teacher_list || '[]',
          exam_list: classData.exam_list || '[]',
          is_public: classData.is_public,
          avatar: '../assets/head.png'
        }
        console.log('班级信息:', classInfo.value)
      } else {
        ElMessage.error(response.data.message || '获取班级信息失败')
        router.push('/home')
      }
    } else {
      ElMessage.error('班级信息不存在')
      router.push('/home')
    }
  } catch (error) {
    console.error('获取班级信息失败:', error)
    ElMessage.error('获取班级信息失败，请稍后重试')
    router.push('/home')
  }
}

// 获取聊天记录
const getChatHistory = async () => {
  try {
    if (route.params.classId) {
      let formdata = new FormData()
      formdata.append("classId", route.params.classId)
      const response = await instance.post(`/class/message/list`, formdata, {
        headers: {
          'Content-Type': 'multipart/form-data'
        }
      })
      if (response.data && Array.isArray(response.data.data)) {
        const processedMessages = []
        
        response.data.data.forEach(messageBlock => {
          try {
            const contentArray = JSON.parse(messageBlock.content)
            contentArray.forEach(item => {
              if (typeof item === 'string' && item.includes(':')) {
                const parts = item.split(':')
                if (parts.length >= 2) {
                  const senderId = parts[0]
                  const senderName = parts[1]
                  const messageContent = parts.slice(2).join(':')
                  
                  processedMessages.push({
                    content: messageContent,
                    time: messageBlock.endtime,
                    senderName: senderName,
                    isSelf: senderId === localStorage.getItem('userId')
                  })
                }
              }
            })
          } catch (e) {
            console.error('解析聊天记录内容失败:', e)
          }
        })
        
        // 按时间排序
        processedMessages.sort((a, b) => a.time - b.time)
        messages.value = processedMessages
        
        // 滚动到底部
        await nextTick()
        scrollToBottom()
      }
    }
  } catch (error) {
    console.error('获取聊天记录失败:', error)
    ElMessage.error('获取聊天记录失败，请稍后重试')
  }
}

// 滚动到底部
const scrollToBottom = () => {
  if (messagesContainer.value) {
    messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
  }
}

// 格式化时间
const formatTime = (timestamp) => {
  const date = new Date(timestamp)
  return date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
}

// 返回上一页
const goBack = () => {
  router.push('/home').then(() => {
    window.location.reload()
  })
}

// 发送消息
const sendMessage = async () => {
  if (!messageText.value.trim()) return
  
  try {
    const formdata = new FormData()
    formdata.append("classId", route.params.classId)
    formdata.append("content", messageText.value)
    
    await instance.post(`/class/message`, formdata, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
    
    messageText.value = ''
  } catch (error) {
    console.error('发送消息失败:', error)
    ElMessage.error('发送消息失败，请稍后重试')
  }
}

// 初始化数据
const initData = async () => {
  try {
    await getClassInfo()
    await getChatHistory()
  } catch (error) {
    console.error('初始化数据失败:', error)
    ElMessage.error('加载数据失败，请刷新页面重试')
  }
}

// 监听班级ID变化
watch(() => route.params.classId, (newVal) => {
  if (newVal) {
    initData()
  }
})

// 组件挂载时获取数据
onMounted(() => {
  initData()
  //subscribeToAllClassMessages()
})

// 在组件卸载时取消订阅
onUnmounted(() => {
  // 取消所有订阅
  // webSocketService.unsubscribeAll()
})

// 处理图片上传
const handleImageUpload = (response) => {
  if (response.success) {
    sendMessage('image', response.url)
  } else {
    ElMessage.error('图片上传失败')
  }
}

// 处理文件上传
const handleFileUpload = (response) => {
  if (response.success) {
    sendMessage('file', response.url)
  } else {
    ElMessage.error('文件上传失败')
  }
}

// 获取文件名
const getFileName = (url) => {
  return url.split('/').pop()
}

// 下载文件
const downloadFile = (url) => {
  window.open(url, '_blank')
}

// 判断是否为班级所有者
const isOwner = computed(() => {
  return classInfo.value?.owner === Number(localStorage.getItem('userId'))
})

// 编辑对话框相关
const editDialogVisible = ref(false)
const editFormRef = ref(null)
const submitting = ref(false)

// 编辑表单数据
const editForm = ref({
  className: '',
  description: '',
  isPublic: 1,
  studentList: '',
  teacherList: ''
})

// 表单验证规则
const editRules = {
  className: [
    { required: true, message: '请输入班级名称', trigger: 'blur' },
    { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  description: [
    { required: true, message: '请输入班级描述', trigger: 'blur' }
  ],
  studentList: [
    { required: true, message: '请输入学生列表', trigger: 'blur' }
  ],
  teacherList: [
    { required: true, message: '请输入教师列表', trigger: 'blur' }
  ]
}

// 打开编辑对话框
const editClassInfo = () => {
  if (!classInfo.value) return
  
  // 初始化表单数据
  editForm.value = {
    className: classInfo.value.className,
    description: classInfo.value.description,
    isPublic: classInfo.value.is_public,
    studentList: classInfo.value.student_list ? JSON.parse(classInfo.value.student_list).join(',') : '',
    teacherList: classInfo.value.teacher_list ? JSON.parse(classInfo.value.teacher_list).join(',') : ''
  }
  
  editDialogVisible.value = true
}

// 提交编辑
const submitEdit = async () => {
  if (!editFormRef.value) return
  
  try {
    await editFormRef.value.validate()
    
    submitting.value = true
    
    const formdata = new FormData()
    formdata.append('classId', classInfo.value.id)
    formdata.append('className', editForm.value.className)
    formdata.append('description', editForm.value.description)
    formdata.append('isPublic', editForm.value.isPublic)
    formdata.append('studentList', editForm.value.studentList)
    formdata.append('teacherList', editForm.value.teacherList)
    
    const response = await instance.post('/class/update', formdata, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
    
    if (response.data.code === 200) {
      ElMessage.success('更新成功')
      editDialogVisible.value = false
      // 重新获取班级信息
      await getClassInfo()
    } else {
      ElMessage.error(response.data.message || '更新失败')
    }
  } catch (error) {
    console.error('更新班级信息失败:', error)
    ElMessage.error('更新失败，请稍后重试')
  } finally {
    submitting.value = false
  }
}

// 添加消息通知相关状态
const unreadMessages = ref(new Set())
const messageNotifications = ref([])

// // 订阅所有班级消息
// const subscribeToAllClassMessages = async () => {
//   try {
//     // 获取用户加入的所有班级
//     const response = await instance.get('/class/my_classes')
//     if (response.data.code === 200) {
//       const classes = response.data.data
      
//       // 为每个班级订阅消息
//       classes.forEach(classItem => {
//         webSocketService.subscribeToClassMessage(classItem.id, (message) => {
//           const data = JSON.parse(message.body)
//           if (data.type === 30) { // 班级消息类型
//             const contentParts = data.content.split(':')
//             if (contentParts.length >= 4) {
//               const [classId, className, senderName, messageContent] = contentParts
              
//               // 添加消息到通知列表
//               const notification = {
//                 id: Date.now(),
//                 type: 'class_message',
//                 classId: classId,
//                 className: className,
//                 senderName: senderName,
//                 content: messageContent,
//                 time: Date.now(),
//                 read: false
//               }
              
//               messageNotifications.value.unshift(notification)
//               unreadMessages.value.add(notification.id)
              
//               // 如果当前正在查看该班级的聊天，则直接添加到消息列表
//               if (route.params.classId === classId) {
//                 messages.value.push({
//                   content: messageContent,
//                   time: Date.now(),
//                   senderName: senderName,
//                   isSelf: senderName === localStorage.getItem('username')
//                 })
//                 nextTick(() => scrollToBottom())
//               }
//             }
//           }
//         })
//       })
//     }
//   } catch (error) {
//     console.error('订阅班级消息失败:', error)
//     ElMessage.error('订阅班级消息失败，请刷新页面重试')
//   }
// }

// 标记消息为已读
const markMessageAsRead = (messageId) => {
  unreadMessages.value.delete(messageId)
  const message = messageNotifications.value.find(msg => msg.id === messageId)
  if (message) {
    message.read = true
  }
}

// 标记所有消息为已读
const markAllMessagesAsRead = () => {
  unreadMessages.value.clear()
  messageNotifications.value.forEach(msg => {
    msg.read = true
  })
}

// 处理通知点击
const handleNotificationClick = (notification) => {
  markMessageAsRead(notification.id)
  // 处理通知点击逻辑
}

// 显示通知抽屉
const showNotifications = ref(false)
</script>

<style scoped>
.chat-container {
  display: flex;
  height: 100vh;
  background-color: #f5f7fa;
}

.chat-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  border-right: 1px solid #e0e0e0;
}

.chat-header {
  display: flex;
  align-items: center;
  padding: 10px 20px;
  background-color: #fff;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.1);
  z-index: 1;
}

.class-info {
  display: flex;
  align-items: center;
  margin-left: 20px;
}

.class-name {
  margin-left: 10px;
  font-size: 16px;
  font-weight: bold;
}

.chat-messages {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
}

.message-item {
  display: flex;
  margin-bottom: 15px;
  align-items: flex-start;
}

.self-message {
  flex-direction: row-reverse;
}

.message-content {
  max-width: 70%;
  margin: 0 10px;
}

.message-sender {
  font-size: 12px;
  color: #666;
  margin-bottom: 4px;
}

.message-text {
  padding: 10px 15px;
  border-radius: 10px;
  word-break: break-word;
}

.self-message .message-text {
  background-color: #95ec69;
  color: #000;
  border-top-right-radius: 0;
}

.other-message .message-text {
  background-color: #fff;
  color: #000;
  border-top-left-radius: 0;
}

.message-time {
  font-size: 12px;
  color: #999;
  margin-top: 5px;
  text-align: right;
}

.chat-input {
  display: flex;
  padding: 10px 20px;
  background-color: #fff;
  border-top: 1px solid #eee;
}

.chat-input .el-input {
  margin-right: 10px;
}

.empty-messages {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100%;
}

.class-sidebar {
  width: 320px;
  background-color: #fff;
  border-left: 1px solid #e0e0e0;
  display: flex;
  flex-direction: column;
}

.sidebar-header {
  padding: 16px 20px;
  border-bottom: 1px solid #e0e0e0;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.sidebar-header h3 {
  margin: 0;
  color: #333;
  font-size: 18px;
}

.sidebar-content {
  padding: 20px;
  flex: 1;
  overflow-y: auto;
}

.section-title {
  display: flex;
  align-items: center;
  margin: 24px 0 12px;
  color: #606266;
  font-size: 16px;
  font-weight: 500;
}

.section-title .el-icon {
  margin-right: 8px;
  font-size: 18px;
}

.description {
  white-space: pre-wrap;
  line-height: 1.6;
  color: #606266;
  font-size: 14px;
}

.teacher-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 16px;
}

.teacher-item {
  margin-bottom: 8px;
}

.exam-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.exam-item {
  font-size: 14px;
  cursor: pointer;
  transition: all 0.3s;
}

.exam-item:hover {
  transform: translateY(-2px);
}

.highlight {
  color: #409EFF;
  font-weight: 500;
}

:deep(.el-descriptions) {
  margin-bottom: 24px;
}

:deep(.el-descriptions__label) {
  width: 100px;
  color: #606266;
}

:deep(.el-descriptions__content) {
  color: #303133;
}

:deep(.el-empty) {
  padding: 20px 0;
}

:deep(.el-loading-mask) {
  background-color: rgba(255, 255, 255, 0.9);
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

:deep(.el-dialog__body) {
  padding-top: 20px;
}

:deep(.el-form-item__label) {
  font-weight: 500;
}

.message-notification {
  position: fixed;
  bottom: 20px;
  right: 20px;
  z-index: 1000;
}

.notification-badge :deep(.el-badge__content) {
  z-index: 1;
}

.notification-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
}

.notification-list {
  padding: 16px;
}

.notification-item {
  padding: 12px;
  border-bottom: 1px solid var(--el-border-color-lighter);
  cursor: pointer;
  transition: all 0.3s;
}

.notification-item:hover {
  background-color: var(--el-fill-color-light);
}

.notification-item.unread {
  background-color: var(--el-color-primary-light-9);
}

.notification-content {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.notification-title {
  font-weight: 500;
  color: var(--el-text-color-primary);
}

.notification-message {
  color: var(--el-text-color-regular);
  font-size: 14px;
}

.notification-time {
  font-size: 12px;
  color: var(--el-text-color-secondary);
}
</style>