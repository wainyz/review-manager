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
      <el-button v-if="!selectedDocumentId" type="primary" @click="handleStartExam()">
        <el-icon><Reading /></el-icon>
        考试生成
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
           <!-- 好友列表按钮 -->
      <div class="list-container">
        <el-button @click="toggleFriendList(); fetchFriendList();fetchFriends();" class="list-btn" :icon="User" circle />
        <el-drawer
          v-model="friendListVisible"
          title="好友列表"
          direction="rtl"
          size="350px"
          :with-header="true"
          :show-close="true"
          :modal="true"
          :append-to-body="true"
          @open="fetchFriendList"
        >
          <template #header>
            <div style="display: flex; justify-content: space-between; align-items: center;">
              <span>好友列表</span>
              <el-button @click="showSearchDialog = true" type="primary" size="small">添加好友</el-button>
            </div>
          </template>
          <div class="friend-list">
            <div v-if="friends.length > 0">
              <div v-for="(friend, index) in friends" :key="'friend-' + index" class="friend-item">
                <el-avatar :size="40" :src="friend.avatar" />
                <div class="friend-info">
                  <div class="friend-name">{{ friend.name }}</div>
                  <div class="friend-status">{{ friend.status }}</div>
                </div>
                <el-button type="danger" size="small" @click="deleteFriend(friend)">删除好友</el-button>
                <el-button type="primary" size="small" @click="openChat(friend)">聊天</el-button>
              </div>
            </div>
            <div v-else class="empty-list">
              <el-empty description="暂无好友" />
            </div>
          </div>

          <!-- 搜索好友对话框 -->
          <el-dialog
            v-model="showSearchDialog"
            title="搜索好友"
            width="30%"
            :close-on-click-modal="false"
          >
            <div class="search-container">
              <el-input
                v-model="searchKeyword"
                placeholder="请输入用户名或邮箱"
                class="search-input"
              >
                <template #append>
                  <el-button @click="handleSearch">搜索</el-button>
                </template>
              </el-input>
            </div>

            <!-- 搜索结果列表 -->
            <div class="search-results" v-if="searchResults.length > 0">
              <div v-for="(user, index) in searchResults" :key="'search-' + index" class="search-item">
                <el-avatar :size="40" :src="user.avatar || 'head.png'" :class="'user-avatar'" />
                <div class="user-info">
                  <div class="user-name">{{ user.username }}</div>
                  <div class="user-email">{{ user.email }}</div>
                </div>
                <div class="action-area">
                  <el-button 
                    v-if="!user.isFriend && !user.hasRequested"
                    type="primary" 
                    size="small"
                    @click="sendFriendRequest(user.user_id)"
                    :loading="user.isRequesting"
                  >
                    申请添加
                  </el-button>
                  <el-tag v-else-if="user.hasRequested" type="warning">已申请</el-tag>
                  <el-tag v-else type="success">已是好友</el-tag>
                </div>
              </div>
            </div>
            <div v-else-if="hasSearched" class="empty-search">
              <el-empty description="未找到相关用户" />
            </div>
          </el-dialog>
        </el-drawer>
      </div>
      
      <!-- 班级列表按钮 -->
      <div class="list-container">
        <el-button @click="toggleClassList" class="list-btn" :icon="School" circle />
        <el-drawer
          v-model="classListVisible"
          title="班级列表"
          direction="rtl"
          size="350px"
          :with-header="true"
          :show-close="true"
          :modal="true"
          :append-to-body="true"
        >
          <template #header>
            <div style="display: flex; justify-content: space-between; align-items: center;">
              <span>班级列表</span>
              <div class="class-actions">
                <el-button type="primary" size="small" @click="handleCreateClass()">创建班级</el-button>
                <el-button type="primary" size="small" @click="showSearchClassDialog = true">加入班级</el-button>
              </div>
            </div>
          </template>
          <div class="class-list">
            <div v-if="classes.length > 0">
              <div v-for="(classItem, index) in classes" :key="'class-' + index" class="class-item">
                <div class="class-info">
                  <div class="class-name">{{ classItem.name }}</div>
                  <div class="class-teacher">{{ classItem.description }}</div>
                </div>
                <el-button type="primary" @click="enterClassChat(classItem.id,classItem.name)" size="small">进入</el-button>
              </div>
            </div>
            <div v-else class="empty-list">
              <el-empty description="暂无班级" />
            </div>
          </div>
        </el-drawer>
      </div>
      
      <!-- 试卷列表按钮 -->
      <div class="list-container">
        <el-button @click="togglePaperList" class="list-btn" :icon="Files" circle />
        <el-drawer
          v-model="paperListVisible"
          title="试卷列表"
          direction="rtl"
          size="350px"
          :with-header="true"
          :show-close="true"
          :modal="true"
          :append-to-body="true"
        >
          <template #header>
            <div style="display: flex; justify-content: space-between; align-items: center;">
              <span>试卷列表</span>
              <el-button type="primary" size="small" @click="handleStartExam()" >创建试卷</el-button>
            </div>
          </template>
          <div class="paper-list">
            <div v-if="papers.length > 0">
              <div v-for="(paper, index) in papers" :key="'paper-' + index" class="paper-item">
                <div class="paper-info">
                  <div class="paper-name">{{ paper.name }}</div>
                </div>
                <el-button 
                  type="primary" 
                  size="small" 
                  @click="showPaperDetail(paper.id)"
                >
                  查看
                </el-button>
              </div>
            </div>
            <div v-else class="empty-list">
              <el-empty description="暂无试卷" />
            </div>
          </div>
        </el-drawer>
      </div>
      
      <!-- 考试列表按钮 -->
      <div class="list-container">
        <el-button @click="toggleExamList" class="list-btn" :icon="Calendar" circle />
        <el-drawer
          v-model="examListVisible"
          title="考试列表"
          direction="rtl"
          size="350px"
          :with-header="true"
          :show-close="true"
          :modal="true"
          :append-to-body="true"
        >
          <template #header>
            <div style="display: flex; justify-content: space-between; align-items: center;">
              <span>考试列表</span>
              <el-button type="primary" size="small" @click="handleStartExam()">创建考试</el-button>
            </div>
          </template>
          <div class="exam-list">
            <div v-if="exams.length > 0">
              <div v-for="(exam, index) in exams" :key="'exam-' + index" class="exam-item">
                <div class="exam-info">
                  <div class="exam-name">{{ exam.name }}</div>
                  <div class="exam-time">考试时间: {{ formatDate(exam.examTime) }}</div>
                </div>
                <el-button type="primary" size="small">参加</el-button>
              </div>
            </div>
            <div v-else class="empty-list">
              <el-empty description="暂无考试" />
            </div>
          </div>
        </el-drawer>
      </div>
      
      <el-button @click="$emit('toggle-theme')" class="theme-btn" :icon="Sunny" circle />
      <div class="message-container">
        <el-badge :value="unreadCount" :hidden="unreadCount === 0" class="message-badge">
          <el-button @click="toggleMessagePopover" class="message-btn" :icon="Bell" circle />
        </el-badge>
        <el-drawer
          v-model="messageVisible"
          title="消息列表"
          direction="rtl"
          size="350px"
          :with-header="true"
          :show-close="true"
          :modal="true"
          :append-to-body="true"
        >
          <template #header>
            <div style="display: flex; justify-content: space-between; align-items: center;">
              <span>消息列表</span>
              <el-button type="text" @click="markAllAsRead" :disabled="!hasUnreadMessages">全部已读</el-button>
            </div>
          </template>
          <MessageList 
            :messages="messages" 
            @read-message="readMessage" 
            @mark-all-read="markAllAsRead" 
          />
        </el-drawer>
      </div>
      <el-avatar @click="$emit('show-profile')" :size="40" src="head.png" />
    </div>
  </header>

  <!-- 搜索班级对话框 -->
  <el-dialog
    v-model="showSearchClassDialog"
    title="搜索班级"
    width="30%"
    :close-on-click-modal="false"
  >
    <div class="search-container">
      <el-input
        v-model="classSearchKeyword"
        placeholder="请输入班级名称或班级编号"
        class="search-input"
      >
        <template #append>
          <el-button @click="handleClassSearch">搜索</el-button>
        </template>
      </el-input>
    </div>

    <!-- 搜索结果列表 -->
    <div class="search-results" v-if="classSearchResults.length > 0">
      <div v-for="(classItem, index) in classSearchResults" :key="'class-search-' + index" class="search-item">
        <div class="class-info">
          <div class="class-name">{{ classItem.class_name }}</div>
          <div class="class-description">{{ classItem.description }}</div>
        </div>
        <el-button 
          v-if="!classItem.isJoined"
          type="primary" 
          size="small"
          @click="joinClass(classItem.id)"
          :loading="classItem.isJoining"
        >
          申请加入
        </el-button>
        <el-tag v-else type="success">已申请</el-tag>
      </div>
    </div>
    <div v-else-if="hasClassSearched" class="empty-search">
      <el-empty description="未找到相关班级" />
    </div>
  </el-dialog>

  <!-- 创建班级对话框 -->
  <el-dialog
    v-model="showCreateClassDialog"
    title="创建班级"
    width="30%"
    :close-on-click-modal="false"
  >
    <el-form
      ref="createClassFormRef"
      :model="createClassForm"
      :rules="createClassRules"
      label-width="100px"
      class="create-class-form"
    >
      <el-form-item label="班级名称" prop="className">
        <el-input v-model="createClassForm.className" placeholder="请输入班级名称" />
      </el-form-item>
      <el-form-item label="班级描述" prop="description">
        <el-input
          v-model="createClassForm.description"
          type="textarea"
          :rows="3"
          placeholder="请输入班级描述"
        />
      </el-form-item>
      <el-form-item label="班级类型" prop="type">
        <el-select v-model="createClassForm.isPublic" placeholder="请选择班级类型">
          <el-option label="公开班级" value="1" />
          <el-option label="私有班级" value="0" />
        </el-select>
      </el-form-item>
    </el-form>
    <template #footer>
      <span class="dialog-footer">
        <el-button @click="showCreateClassDialog = false">取消</el-button>
        <el-button type="primary" @click="submitCreateClass" :loading="isCreating">
          创建
        </el-button>
      </span>
    </template>
  </el-dialog>
</template>

<script setup>
import { Document, Reading, Upload, Sunny, Bell, User, School, Files, Calendar } from '@element-plus/icons-vue'
import { ref, computed, onUnmounted, onMounted, watch } from 'vue'
import request from '../../config/axios'
import { API_PATHS, getFullUrl } from '../../config/api'
import MessageList from './MessageList.vue'
import ChatDialog from '../../views/ChatView.vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import instance from '../../config/axios'
import webSocketService from '../../config/websocket-service'
import { handleStompMessage } from '../../utils/message-handler'

const props = defineProps({
  selectedDocumentId: {
    type: Number,
    default: 0
  }
})

const emit = defineEmits([
  'toggle-sidebar',
  'handle-upload',
  'start-random-review',
])

const router = useRouter()
const route = useRoute()

// 监听路由变化
watch(
  () => route.path,
  (newPath) => {
    if (newPath === '/home') {
      // 重新加载数据
      fetchMessages()
      fetchFriends()
      fetchClasses()
      fetchPapers()
      fetchExams()
    }
  }
)

// 修改返回首页的方法
const goToHome = () => {
  router.push('/home').then(() => {
    // 强制刷新组件
    window.location.reload()
  })
}

// 修改打开聊天对话框的方法
const openChat = (friend) => {
  selectedFriend.value = friend
  console.log('打开聊天对话框:', friend)
  router.push({
    path: `/chat/${friend.id}`,
    params: {
      friendId: friend.id
    }
  }).then(() => {
    // 强制刷新组件
    window.location.reload()
  })
}

// 搜索好友
const searchFriends = async (keyword) => {
  try {
    if (!keyword) {
      ElMessage.warning('请输入搜索关键词');
      return;
    }

    // 调用搜索用户API
    const response = await instance.get(`/search/user/${keyword}`);

    // 处理响应数据
    if (response.data.code === 200) {
      // 成功获取搜索结果
      console.log('搜索结果:', response.data);
      // TODO: 处理搜索结果，例如显示在弹窗中
      return response.data.data;
    } else {
      // 搜索失败
      ElMessage.error(response.data.message || '搜索好友失败');
      return null;
    }
  } catch (error) {
    console.error('搜索好友失败:', error);
    ElMessage.error('搜索好友失败，请稍后重试');
    return null;
  }
}

// 消息相关状态
const messageVisible = ref(false)
const messages = ref([])
const unreadCount = ref(0)

// 计算是否有未读消息
const hasUnreadMessages = computed(() => {
  return messages.value.some(message => !message.read)
})

// 添加消息到消息列表
function addMessages(message) {
  const data = JSON.parse(message.body)
  const newMessage = {
    id: data.id,
    title: getNoticeTitle(data.type),
    type: data.type,
    content: data.content,
    time: data.timestamp,
    read: false
  }
  
  // 使用数组解构来创建新的数组，确保触发响应式更新
  messages.value = [newMessage, ...messages.value]
  unreadCount.value = messages.value.filter(msg => !msg.read).length
}

// 订阅全局通知
let globalNotificationSubscription = webSocketService.subscribeToGlobalNotifications((message) => {
  const data = JSON.parse(message.body);
  console.log('收到全局通知:', data);
  handleStompMessage(message);
  addMessages(message);
});

// 订阅用户通知
let userNotificationSubscription = webSocketService.subscribeToUserNotifications((message) => {
  const data = JSON.parse(message.body);
  console.log('收到用户通知:', data);
  handleStompMessage(message);
  addMessages(message);
});

// 模拟获取消息数据
const fetchMessages = async () => {
  try {
    // 调用实际API获取消息
    const response = await instance.get('/notice/list')
    
    // 转换API返回数据为前端需要的格式
    messages.value = response.data.data.map(item => ({
      id: item.id,
      title: getNoticeTitle(item.type), // 根据type生成标题
      type: item.type,
      content: item.content,
      time: item.timestamp,
      read: false // 默认未读，你可以根据需要调整
    }))
    // 计算未读消息数量
    updateUnreadCount()
  } catch (error) {
    console.error('获取消息失败:', error)
    // 可以在这里添加错误处理，比如显示错误提示
    ElMessage.error('获取消息失败，请稍后重试')
  }
}
const updateUnreadCount = async () =>  {
  // 首先查询上次阅读消息的时间
  const response = await instance.get('/notice/get/last_read_time')
  let lastReadTime = response.data.data
  messages.value.forEach(msg => { 
    if(msg.time > lastReadTime) 
      msg.read = false 
    else 
      msg.read = true 
    })
  unreadCount.value = messages.value.filter(msg => !msg.read).length
}
/*
- 系统全局通知 0
  
- 好友申请通知 1
  
- 通过申请通知 2
  
- 等待生成通知 3
  
- 等待打分通知 4
  
- 完成等待通知 5
  
- 系统特别通知 -1
*/
// 根据通知类型返回对应的标题
function getNoticeTitle(type) {
  const typeTitles = {
    
    0: '系统全局通知',
    1: '好友申请通知',
    2: '通过申请通知',
    3: '等待生成通知',
    4: '等待打分通知',
    5: '完成等待通知',
    100: '系统特别通知',
    // 可以根据实际需要添加更多类型
  }
  return typeTitles[type] || '通知'
}

// 切换消息弹出框显示状态
const toggleMessagePopover = () => {
  messageVisible.value = !messageVisible.value
}

// 标记消息为已读
const readMessage = async (messageId) => {
  try {
    // 这里应该调用实际的API标记消息已读
    var formdata = new FormData();
    formdata.append("timestamp", Date.now());
    instance.post('/notice/update/last_read_time', formdata, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }})
    // 目前直接修改本地数据
    const message = messages.value.find(msg => msg.id === messageId)
    if (message) {
      message.read = true
      unreadCount.value = unreadCount.value - 1;
    }
  } catch (error) {
    console.error('标记消息已读失败:', error)
  }
}

// 标记所有消息为已读
const markAllAsRead = async () => {
  try {
    // 这里应该调用实际的API标记所有消息已读
    // 目前直接修改本地数据
    messages.value.forEach(msg => {
      if(msg.type === 3 || msg.type === 4 || msg.type === 5) msg.read = false
      else msg.read = true
    })
    unreadCount.value = 0;
  } catch (error) {
    console.error('标记所有消息已读失败:', error)
  }
}

// 在组件挂载时获取消息和其他列表数据
onMounted(() => {
  fetchMessages()
  fetchFriends()
  fetchClasses()
  fetchPapers()
  fetchExams()
})

// 好友列表相关状态
const friendListVisible = ref(false)
const friends = ref([])

// 班级列表相关状态
const classListVisible = ref(false)
const classes = ref([])

// 试卷列表相关状态
const paperListVisible = ref(false)
const papers = ref([])

// 考试列表相关状态
const examListVisible = ref(false)
const exams = ref([])

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
  if (progressTimer.value) {
    clearInterval(progressTimer.value)
    progressTimer.value = null
  }
  if (checkStatusTimer.value) {
    clearInterval(checkStatusTimer.value)
    checkStatusTimer.value = null
  }
  
  // // 取消 WebSocket 订阅
   webSocketService.unsubscribe(globalNotificationSubscription)
   webSocketService.unsubscribe(userNotificationSubscription)
  
  // 清理消息状态
  messages.value = []
  unreadCount.value = 0
  
  // 清理其他状态
  friendListVisible.value = false
  classListVisible.value = false
  paperListVisible.value = false
  examListVisible.value = false
  messageVisible.value = false
  showProgress.value = false
})

const handleStartExam = () => {
  router.push('/paper/config')
}

// 切换好友列表显示状态
const toggleFriendList = () => {
  friendListVisible.value = !friendListVisible.value
}

// 切换班级列表显示状态
const toggleClassList = () => {
  classListVisible.value = !classListVisible.value
}

// 切换试卷列表显示状态
const togglePaperList = () => {
  paperListVisible.value = !paperListVisible.value
}

// 切换考试列表显示状态
const toggleExamList = () => {
  examListVisible.value = !examListVisible.value
}
// 获取好友列表数据
const fetchFriends = async () => {
  try {
    const response = await instance.get('/friends/list')
    if (response.data.code === 200) {
      // 将API返回的用户数据映射为前端所需的格式
      friends.value = response.data.data.map(user => ({
        id: user.user_id,
        name: user.username,
        email: user.email,
        avatar: 'head.png', // 默认头像
        status: '在线' // 默认状态
      }))
    } else {
      ElMessage.error(response.data.message || '获取好友列表失败')
    }
  } catch (error) {
    console.error('获取好友列表失败:', error)
    ElMessage.error('获取好友列表失败，请稍后重试')
  }
}
// 获取班级列表数据
const fetchClasses = async () => {
  try {
    // 调用API获取班级列表
    const response = await instance.get('/class/my_classes')
    if (response.data.code === 200 && response.data.data) {
      // 确保data是数组
      const classData = Array.isArray(response.data.data) ? response.data.data : []
      if(response.data.data == null){
        return;
      }
      // 直接使用返回的数据数组
      classes.value = classData.map(item => {
        // 解析班级信息
        const classInfo = {
          id: parseInt(item.id),
          name: item.class_name,
          description: item.description,
          // 判断当前用户是否为班级拥有者
          isOwner: item.owner === parseInt(localStorage.getItem('userId')),
          teacherList: JSON.parse(item.teacher_list || '[]'),
          studentList: JSON.parse(item.student_list || '[]'), 
          examList: JSON.parse(item.exam_list || '[]'),
          isPublic: item.is_public === 1,
          // 添加额外展示信息
          teacherCount: JSON.parse(item.teacher_list || '[]').length,
          studentCount: JSON.parse(item.student_list || '[]').length,
          examCount: JSON.parse(item.exam_list || '[]').length,
          // 添加创建时间和更新时间
          createTime: item.create_time,
          updateTime: item.update_time,
          // 添加班级状态
          status: item.status || '正常',
          // 添加班级编号
          classCode: item.class_code || `CLASS_${item.id}`
        }
        return classInfo
      })
    } else {
      classes.value = []
      // ElMessage.error(response.data.message || '获取班级列表失败')
    }
  } catch (error) {
    // console.error('获取班级列表失败:', error)
    classes.value = []
    // ElMessage.error('获取班级列表失败，请稍后重试')
  }
}
import { ElMessageBox } from 'element-plus'
function escapeHtml(unsafe) {
          return unsafe
            .replace(/&/g, "&amp;")
            .replace(/</g, "&lt;")
            .replace(/>/g, "&gt;")
            .replace(/"/g, "&quot;")
            .replace(/'/g, "&#039;")
            .replace("${blank_1}",)
}
function renderExamToHTML(examData) {
  examData = JSON.parse(examData)
  let html = `<h3>试卷描述：</h3><p>${escapeHtml(examData.description)}</p>`;
  examData.questionBlocks.forEach(block => {
    html += `<div class="question-block"><h2>${escapeHtml(block.questionsTitle)}</h2>`;
    block.questions.forEach(question => {
      html += `<div class="question"><strong>${escapeHtml(question.question)}</strong>`;
      if (block.questionType === 1) {
        html += `<p>${escapeHtml(question.chooseA)}</p>`;
        html += `<p>${escapeHtml(question.chooseB)}</p>`;
        html += `<p>${escapeHtml(question.chooseC)}</p>`;
        html += `<p>${escapeHtml(question.chooseD)}</p>`;
        html += `<p class="answer">正确答案: ${escapeHtml(question.rightChoose)}</p>`;
      } else if (block.questionType === 2 || block.questionType === 3) {
        html += `<p class="answer">正确答案: ${escapeHtml(question.rightAnswer)}</p>`;
      }
      html += `</div>`;
    });
    html += `</div>`;
  });
  return html;
}
const showPaperDetail = (paperId) => {
  const paper = papers.value.find(p => p.id === paperId)
  if (paper) {
    ElMessageBox.alert(renderExamToHTML(paper.content), paper.name, {
      confirmButtonText: '导出为word',
      width: '80%',
      customClass: 'large-message-box',
      dangerouslyHtml: true,
      callback: () => {},
      // 允许渲染HTML内容
      dangerouslyUseHTMLString: true,
      // 添加自定义按钮
      showCancelButton: true,
      cancelButtonText: '退出',
      beforeClose: (action, instance, done) => {
        if (action !== 'cancel') {
          exportToWord(paper)
          done()
        } else {
          done()
        }
      }
    });
  } else {
    ElMessage.warning('未找到试卷信息');
  }
}

// 导出为Word文档
const exportToWord = (paper) => {
  try {
    // 创建一个新的 Blob 对象
    const content = renderExamToHTML(paper.content)
    const blob = new Blob([`
      <html xmlns:o='urn:schemas-microsoft-com:office:office' 
            xmlns:w='urn:schemas-microsoft-com:office:word' 
            xmlns='http://www.w3.org/TR/REC-html40'>
      <head>
        <meta charset='utf-8'>
        <title>${paper.name}</title>
        <style>
          body { font-family: SimSun; }
          h1, h2, h3 { font-weight: bold; }
          .question { margin: 15px 0; }
          .answer { color: #666; margin-top: 5px; }
        </style>
      </head>
      <body>
        ${content}
      </body>
      </html>
    `], { type: 'application/msword' })

    // 创建下载链接
    const link = document.createElement('a')
    link.href = URL.createObjectURL(blob)
    link.download = `${paper.name}.doc`
    
    // 触发下载
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    
    // 释放 URL 对象
    URL.revokeObjectURL(link.href)
    
    ElMessage.success('试卷导出成功')
  } catch (error) {
    console.error('导出试卷失败:', error)
    ElMessage.error('导出试卷失败，请稍后重试')
  }
}
const handleCreateClass = () => {
  showCreateClassDialog.value = true
  // 重置表单
  if (createClassFormRef.value) {
    createClassFormRef.value.resetFields()
  }
}
const handleJoinClass = () => {
  showSearchClassDialog.value = true
  classSearchKeyword.value = ''
  classSearchResults.value = []
  hasClassSearched.value = false
}
// 获取试卷列表数据
const fetchPapers = async () => {
  try {
// 调用API获取试卷列表
const response = await instance.get('/paper/list')
if (response.data.code === 200) {
  papers.value = response.data.data.map(paper => ({
    id: paper.id,
    name: paper.title,
    content: paper.content,
  }))
} else {
  ElMessage.error(response.data.message || '获取试卷列表失败')
}
    // // 目前使用模拟数据
    // papers.value = [
    //   { id: 1, name: '期中考试试卷', createTime: Date.now() - 7 * 24 * 60 * 60 * 1000 },
    //   { id: 2, name: '期末考试试卷', createTime: Date.now() - 3 * 24 * 60 * 60 * 1000 },
    //   { id: 3, name: '模拟考试试卷', createTime: Date.now() - 24 * 60 * 60 * 1000 }
    // ]
  } catch (error) {
    console.error('获取试卷列表失败:', error)
    ElMessage.error('获取试卷列表失败，请稍后重试')
  }
}

// 获取考试列表数据
const fetchExams = async () => {
  try {
    // 这里应该调用实际的API获取考试列表
    // 目前使用模拟数据
    exams.value = [
      { id: 1, name: '期中考试', examTime: Date.now() + 7 * 24 * 60 * 60 * 1000 },
      { id: 2, name: '期末考试', examTime: Date.now() + 30 * 24 * 60 * 60 * 1000 },
      { id: 3, name: '模拟考试', examTime: Date.now() + 3 * 24 * 60 * 60 * 1000 }
    ]
  } catch (error) {
    console.error('获取考试列表失败:', error)
    ElMessage.error('获取考试列表失败，请稍后重试')
  }
}

// 格式化日期
const formatDate = (timestamp) => {
  const date = new Date(timestamp)
  return date.toLocaleDateString('zh-CN', { year: 'numeric', month: '2-digit', day: '2-digit' })
}

// 聊天相关
const selectedFriend = ref(null)
const showChatDialog = ref(false)
const deleteFriend = async (friend) => {
  try {
    // 发送删除好友请求
    const response = await instance.get(`/friends/delete/${friend.id}`)
    
    if (response.data.code === 200) {
      // 解析返回消息
      
      ElMessage.success()
      
      // 从本地好友列表中移除该好友
      const index = friends.value.findIndex(f => f.id === friend.id)
      if (index !== -1) {
        friends.value.splice(index, 1)
      }
    } else {
      //ElMessage.error(response.data.message || '删除好友失败')
    }
  } catch (error) {
    //console.error('删除好友失败:', error)
    //ElMessage.error('删除好友失败，请稍后重试')
  }
}

// 好友相关状态
const showSearchDialog = ref(false)
const searchKeyword = ref('')
const searchResults = ref([])
const hasSearched = ref(false)

// 处理搜索好友
const handleSearch = async () => {
  if (!searchKeyword.value.trim()) {
    ElMessage.warning('请输入搜索关键词')
    return
  }
  try {
    const results = await searchFriends(searchKeyword.value.trim())
    if (results) {
      searchResults.value = results.map(user => ({
        ...user,
        isRequesting: false,
        hasRequested: false  // 添加申请状态标记
      }))
    }
    hasSearched.value = true
  } catch (error) {
    console.error('搜索好友失败:', error)
    ElMessage.error('搜索好友失败，请稍后重试')
  }
}

// 发送好友申请
const sendFriendRequest = async (userId) => {
  const user = searchResults.value.find(u => u.user_id === userId)
  if (!user || user.isRequesting) return
  
  user.isRequesting = true
  try {
    const response = await instance.get(`/friends/apply/add/${userId}`)
    if (response.data.code === 200) {
      ElMessage.success('好友申请已发送')
      user.hasRequested = true  // 修改为已申请状态
    } else {
      ElMessage.error(response.data.message || '发送好友申请失败')
    }
  } catch (error) {
    console.error('发送好友申请失败:', error)
    ElMessage.error('发送好友申请失败，'+error.data.data.message)
  } finally {
    user.isRequesting = false
  }
}

// 处理好友聊天
const handleChat = (friendId) => {
  // TODO: 实现聊天功能
  console.log('开始与好友聊天:', friendId)
}

// 班级搜索相关状态
const showSearchClassDialog = ref(false)
const classSearchKeyword = ref('')
const classSearchResults = ref([])
const hasClassSearched = ref(false)

// 搜索班级
const searchClasses = async (keyword) => {
  try {
    if (!keyword) {
      ElMessage.warning('请输入搜索关键词');
      return;
    }

    // 创建FormData对象并添加关键词
    const formData = new FormData();
    formData.append('key', keyword);

    const response = await instance.post('/class/search', formData,
      {headers: {
        'Content-Type': 'multipart/form-data'
      }});
    
    if (response.data.code === 200) {
      return response.data.data;
    } else {
      ElMessage.error(response.data.message || '搜索班级失败');
      return null;
    }
  } catch (error) {
    console.error('搜索班级失败:', error);
    ElMessage.error('搜索班级失败，请稍后重试');
    return null;
  }
}

// 处理班级搜索
const handleClassSearch = async () => {
  if (!classSearchKeyword.value.trim()) {
    ElMessage.warning('请输入搜索关键词')
    return
  }
  try {
    const results = await searchClasses(classSearchKeyword.value.trim())
    if (results) {
      classSearchResults.value = results.map(classItem => ({
        ...classItem,
        isJoining: false
      }))
    }
    hasClassSearched.value = true
  } catch (error) {
    console.error('搜索班级失败:', error)
    ElMessage.error('搜索班级失败，请稍后重试')
  }
}

// 加入班级
const joinClass = async (classId) => {
  const classItem = classSearchResults.value.find(c => c.id === classId)
  if (!classItem || classItem.isJoining) return
  
  // 弹出输入申请理由的对话框
  try {
    const { value: description } = await ElMessageBox.prompt('请输入申请理由', '加入班级', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputValidator: (value) => {
        if (!value) {
          return '申请理由不能为空'
        }
        if (value.length > 200) {
          return '申请理由不能超过200字'
        }
        return true
      },
      inputErrorMessage: '请输入有效的申请理由'
    })

    if (!description) return

    classItem.isJoining = true
    
    // 创建FormData对象并添加数据
    const formData = new FormData()
    formData.append("classId", classId)
    formData.append("description", description)
    
    const response = await instance.post('/class/apply/add', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
    
    if (response.data.code === 200) {
      ElMessage.success('申请已发送，等待教师审核')
      classItem.isJoined = true
    } else {
      ElMessage.error(response.data.message || '申请加入班级失败')
    }
  } catch (error) {
    console.error('申请加入班级失败:',error)
    ElMessage.error('取消')
  } finally {
    classItem.isJoining = false
  }
}

// 创建班级相关状态
const showCreateClassDialog = ref(false)
const createClassFormRef = ref(null)
const isCreating = ref(false)

const createClassForm = ref({
  className: '',
  description: '',
  isPublic: ""
})

const createClassRules = {
  name: [
    { required: true, message: '请输入班级名称', trigger: 'blur' },
    { min: 2, max: 20, message: '长度在 2 到 20 个字符', trigger: 'blur' }
  ],
  description: [
    { required: true, message: '请输入班级描述', trigger: 'blur' },
    { min: 5, max: 200, message: '长度在 5 到 200 个字符', trigger: 'blur' }
  ]
}

// 提交创建班级表单
const submitCreateClass = async () => {
  if (!createClassFormRef.value) return
  
  try {
    await createClassFormRef.value.validate()
    isCreating.value = true
    
    // 创建FormData对象并添加数据
    const formData = new FormData()
    formData.append('className', createClassForm.value.className)
    formData.append('description', createClassForm.value.description)
    formData.append('isPublic', createClassForm.value.isPublic)
    
    const response = await instance.post('/class/create', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
    
    if (response.data.code === 200) {
      ElMessage.success('班级创建成功')
      showCreateClassDialog.value = false
      // 重置表单
      createClassFormRef.value.resetFields()
      // 刷新班级列表
      fetchClasses()
    } else {
      ElMessage.error(response.data.message || '创建班级失败')
    }
  } catch (error) {
    if (error.response) {
      ElMessage.error(error.response.data.message || '创建班级失败')
    } else {
      console.error('创建班级失败:', error)
      ElMessage.error('创建班级失败，请稍后重试')
    }
  } finally {
    isCreating.value = false
  }
}
function enterClassChat(id,name){
  router.push({
    path: `/class/chat/${id}`,
    params: {
      classId: id,
      className: name
    }
  }).then(() => {
    // 强制刷新组件
    window.location.reload()
  })
}
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

.message-container {
  position: relative;
}

.message-badge :deep(.el-badge__content) {
  z-index: 1;
}

.message-btn {
  transition: all 0.3s;
}

.message-btn:hover {
  background-color: var(--el-color-primary-light-8);
}

.list-container {
  position: relative;
}

.list-btn {
  transition: all 0.3s;
}

.list-btn:hover {
  background-color: var(--el-color-primary-light-8);
}

.friend-item, .class-item, .paper-item, .exam-item {
  display: flex;
  align-items: center;
  padding: 12px 16px;
  border-bottom: 1px solid var(--el-border-color-lighter);
  transition: background-color 0.3s;
}

.friend-item:hover, .class-item:hover, .paper-item:hover, .exam-item:hover {
  background-color: var(--el-fill-color-light);
}

.friend-info, .class-info, .paper-info, .exam-info {
  flex: 1;
  margin-left: 12px;
}

.friend-name, .class-name, .paper-name, .exam-name {
  font-weight: 500;
  margin-bottom: 4px;
}

.friend-status, .class-teacher, .paper-date, .exam-time {
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

.empty-list {
  padding: 20px 0;
}

.header-right :deep(.el-button--text) {
  transition: all 0.3s;
  padding: 8px 12px;
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 4px;
  color: var(--el-text-color-regular);
  background-color: transparent;
}

.header-right :deep(.el-button--text:hover) {
  color: var(--el-color-primary);
  background-color: var(--el-color-primary-light-9);
  border-color: var(--el-color-primary);
  transform: translateY(-1px);
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.header-right :deep(.el-button--text:active) {
  transform: scale(0.95) translateY(0);
  box-shadow: none;
}

/* 搜索结果样式 */
.search-results {
  margin-top: 16px;
  max-height: 400px;
  overflow-y: auto;
}

.search-item {
  display: flex;
  align-items: center;
  padding: 12px 16px;
  border-bottom: 1px solid var(--el-border-color-lighter);
  transition: all 0.3s ease;
}

.search-item:hover {
  background-color: var(--el-fill-color-light);
}

.search-item .user-avatar {
  margin-right: 12px;
  border: 2px solid var(--el-border-color);
  transition: all 0.3s ease;
}

.search-item:hover .user-avatar {
  border-color: var(--el-color-primary);
  transform: scale(1.05);
}

.search-item .user-info {
  flex: 1;
  min-width: 0;
}

.search-item .user-name {
  font-size: 16px;
  font-weight: 500;
  color: var(--el-text-color-primary);
  margin-bottom: 4px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.search-item .user-email {
  font-size: 13px;
  color: var(--el-text-color-secondary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.search-item .action-area {
  margin-left: 16px;
  min-width: 80px;
  text-align: right;
}

.search-item .el-button {
  padding: 8px 16px;
  transition: all 0.3s ease;
}

.search-item .el-button:hover {
  transform: translateY(-1px);
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.search-item .el-tag {
  padding: 6px 12px;
  border-radius: 4px;
}

.empty-search {
  padding: 32px 0;
  text-align: center;
}

/* 定义 large-message-box 类 */
.large-message-box {
  width: 80% !important;
  max-width: 800px;
  height: 80vh;
  max-height: 600px;
}

.large-message-box .el-message-box__content {
  padding: 20px;
  overflow-y: auto;
  max-height: calc(80vh - 120px);
}

.large-message-box .el-message-box__header {
  padding: 20px;
}

.large-message-box .el-message-box__btns {
  padding: 20px;
}

.class-actions {
  display: flex;
  gap: 8px;
}

.class-info {
  flex: 1;
  min-width: 0;
}

.class-name {
  font-size: 16px;
  font-weight: 500;
  color: var(--el-text-color-primary);
  margin-bottom: 4px;
}

.class-teacher, .class-code {
  font-size: 13px;
  color: var(--el-text-color-secondary);
  margin-top: 2px;
}

.create-class-form {
  padding: 20px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}
</style>