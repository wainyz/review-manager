<template>
  <div class="chat-container">
    <div class="chat-header">
      <el-button @click="goBack" icon="ArrowLeft">返回</el-button>
      <div class="friend-info" v-if="friend">
         <el-avatar :size="40">
            <img src="../assets/head.png" alt="班级头像" /></el-avatar>
        <span class="friend-name">{{ friend.name }}</span>
      </div>
    </div>
    
    <div class="chat-messages" ref="messagesContainer">
      <div v-if="messages.length > 0">
        <div v-for="(message, index) in messages" :key="index" 
             :class="['message-item', message.isSelf ? 'self-message' : 'friend-message']">
           <el-avatar :size="30">
            <img src="../assets/head.png" alt="班级头像" />
          </el-avatar>
          <div class="message-content">
            <!-- 根据消息类型显示不同内容 -->
             <div class="message-text" :class="message.type">
               {{ message.content }}
             </div>
            
             <!-- 
            <div class="message-text" :class="message.type">
              <!-- 文本消息 
              <template v-if="!message.type || message.type === 'text' || message.type == undefined">
                {{ message.content }}
              </template>
              <!-- 图片消息 
              <template v-else-if="message.type === 'image'">
                <el-image 
                  :src="message.content" 
                  :preview-src-list="[message.content]"
                  fit="cover"
                  class="message-image"
                />
              </template>
              <!-- 文件消息
              <template v-else-if="message.type === 'file'">
                <div class="file-message" @click="downloadFile(message.content)">
                  <el-icon><Document /></el-icon>
                  <span>{{ getFileName(message.content) }}</span>
                </div>
              </template>
            </div> -->
            <div class="message-time">{{ formatTime(message.time) }}</div>
          </div>
        </div>
      </div>
      <div v-else class="empty-messages">
        <el-empty description="暂无消息记录" />
      </div>
    </div>
    
    <div class="chat-input">
      <div class="input-tools">
        <el-upload
          action="/api/upload"
          :show-file-list="false"
          :on-success="handleImageUpload"
          accept="image/*"
        >
          <el-button icon="Picture" circle></el-button>
        </el-upload>
        <el-upload
          action="/api/upload"
          :show-file-list="false"
          :on-success="handleFileUpload"
        >
          <el-button icon="Document" circle></el-button>
        </el-upload>
      </div>
      <el-input
        v-model="messageText"
        placeholder="输入消息..."
        @keyup.enter="sendMessage"
      >
      </el-input>
      <el-button type="primary" @click="sendMessage">发送</el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, nextTick, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft } from '@element-plus/icons-vue'
import instance from '../config/axios'

const route = useRoute()
const router = useRouter()

// 好友信息
const friend = ref(null)

// 用户头像
const userAvatar = '../assets/head.png'

// 消息列表
const messages = ref([])
const messageText = ref('')
const messagesContainer = ref(null)
// 连接WebSocket
import webSocketService from '../config/websocket-service';
// STOMP消息通信
webSocketService.subscribeToUserMessage((message) => {
  const data = JSON.parse(message.body);
  console.log('聊天框 收到用户通知:', data);
  // 检测消息类型是否为61
  if (data.type === 61) {
    //这里需要进一步解析content内容，有两个冒号，分割了三个区域。第一个区域中的值表示发送方向，第二个区域中的值是type，暂时没做处理。
    //第三个区域中的数据才是content
    // 根据第一个区域的值确定发送方向，如果是0那么是从小id发送大id方向，（通过比较userId和friendId确认自己是小id还是大id），如果是1则相反
    
    // 解析消息内容
    const contentParts = data.content.split(':');
    if (contentParts.length >= 3) {
      const direction = contentParts[0]; // 消息方向
      const messageType = contentParts[1]; // 消息类型
      const messageContent = contentParts[2]; // 实际消息内容
      
      // 获取当前用户ID和好友ID
      const userId = localStorage.getItem('userId'); // 假设用户ID存储在localStorage中
      const friendId = route.params.friendId;
      
      // 判断消息是否由自己发送
      let isSelf = false;
      if (userId && friendId) {
        const isCurrentUserSmallerId = Number(userId) < Number(friendId);
        // direction为0时是从小ID发向大ID，为1时是从大ID发向小ID
        isSelf = (direction === '0' && isCurrentUserSmallerId) || 
                (direction === '1' && !isCurrentUserSmallerId);
      }

      // 构建消息对象并添加到消息队列
      const messageObj = {
        content: messageContent,
        time: Date.now(),
        isSelf: isSelf,
        type: messageType
      };
      
      messages.value.push(messageObj);
      
      // 确保视图更新后滚动到底部
      nextTick(() => scrollToBottom());
    }
  }
})
// 获取好友信息
const getFriendInfo = async () => {
  try {
    if (route.params.friendId) {
      const response = await instance.get(`/user/get/${route.params.friendId}`)
      if (response.data.code === 200) {
        friend.value = {
          id: response.data.data.user_id,
          name: response.data.data.username,
          email: response.data.data.email,
          avatar: '../assets/head.png',
          status: '在线'
        }
      } else {
        ElMessage.error('获取好友信息失败')
        router.push('/home')
      }
    } else {
      ElMessage.error('好友信息不存在')
      router.push('/home')
    }
  } catch (error) {
    console.error('获取好友信息失败:', error)
    ElMessage.error('获取好友信息失败，请稍后重试')
    router.push('/home')
  }
}

// 获取聊天记录
const getChatHistory = async () => {
  try {
    if (route.params.friendId) {
      // 调用实际的API获取聊天记录
      const response = await instance.get(`/message/history/${route.params.friendId}`)
      if (response.data.code === 200 && response.data.data) {
        // 处理返回的数据
        const historyData = response.data.data
        const processedMessages = []
        // 遍历每个消息块
            // 获取当前用户ID和好友ID
        const userId = localStorage.getItem('userId')
        const friendId = route.params.friendId
        const isCurrentUserSmallerId = Number(userId) < Number(friendId)
        historyData.forEach(messageBlock => {
          try {
            // 解析消息块中的content字段
            const contentArray = JSON.parse(messageBlock.content)
            
            // 处理消息块中的每条消息
            contentArray.forEach(item => {
              if (typeof item === 'string' && item.includes(':')) {
                const parts = item.split(':')
                if (parts.length >= 3) {
                  const direction = parts[0] // 消息方向：0表示小id到大id，1表示大id到小id
                  const messageType = parts[1] // 消息类型
                  const messageContent = parts.slice(2).join(':') // 实际消息内容
                  
                  // 根据消息方向和用户ID判断是否为自己发送的消息
                  const isSelf = (direction === '0' && isCurrentUserSmallerId) || 
                                (direction === '1' && !isCurrentUserSmallerId)
                  
                  processedMessages.push({
                    content: messageContent,
                    time: messageBlock.endtime,
                    type: messageType,
                    isSelf: isSelf
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
        
        // 更新消息列表
        messages.value = processedMessages
      } else {
        console.warn('获取聊天记录失败或数据为空:', response.data)
      }
      
      // 滚动到底部
      await nextTick()
      scrollToBottom()
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
  router.push('/home')
}

// 监听好友变化
watch(() => route.params.friendId, (newVal) => {
  if (newVal) {
    getFriendInfo()
    getChatHistory()
  }
})

// 组件挂载时获取数据
onMounted(() => {
  getFriendInfo()
  getChatHistory()
  
  // 订阅WebSocket消息
  webSocketService.subscribeToUserNotifications((message) => {
    const data = JSON.parse(message.body)
    console.log('收到聊天消息:', data)
    // 处理接收到的聊天消息
    if (data.type === 'CHAT' && data.senderId === friend.value?.id) {
      messages.value.push({
        content: data.content,
        time: data.timestamp,
        isSelf: false
      })
      nextTick(() => scrollToBottom())
    }
  })
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

// 修改发送消息方法
const sendMessage = async (type = 'text', content = messageText.value) => {
  if (!content.trim()) return
  
  try {
    const currentTime = Date.now()
    
    // 添加到本地消息列表
    const newMessage = {
      content: content,
      time: currentTime,
      isSelf: true,
      type: type
    }
    messages.value.push(newMessage)
    
    // 清空输入框
    if (type === 'text') {
      messageText.value = ''
    }
    
    // 滚动到底部
    await nextTick()
    scrollToBottom()
    
    // 发送消息到服务器
    var formdata = new FormData();
    formdata.append("content", messageText.value);
    formdata.append("friendId",`${route.params.friendId}`)
    await instance.post(`/message/talk`, formdata, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
    messageText.value = ""
  } catch (error) {
    console.error('发送消息失败:', error)
    ElMessage.error('发送消息失败，请稍后重试')
  }
  
}
</script>

<style scoped>
.chat-container {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background-color: #f5f7fa;
}

.chat-header {
  display: flex;
  align-items: center;
  padding: 10px 20px;
  background-color: #fff;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.1);
  z-index: 1;
}

.friend-info {
  display: flex;
  align-items: center;
  margin-left: 20px;
}

.friend-name {
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

.friend-message .message-text {
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

.input-tools {
  display: flex;
  gap: 8px;
  margin-right: 10px;
}

.message-image {
  max-width: 200px;
  max-height: 200px;
  border-radius: 8px;
  cursor: pointer;
}

.file-message {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
}

.file-message:hover {
  opacity: 0.8;
}

.file-message .el-icon {
  font-size: 20px;
}
</style>