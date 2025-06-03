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
                <div class="paper-actions">
                  <el-button 
                    type="primary" 
                    size="small" 
                    @click="showPaperDetail(paper.id)"
                  >
                    查看
                  </el-button>
                  <el-button 
                    type="warning" 
                    size="small" 
                    @click="handleEditPaper(paper)"
                  >
                    编辑
                  </el-button>
                </div>
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
      <!-- 添加管理按钮 -->
      <el-button 
        v-if="hasAdminPermission" 
        @click="toggleAdminPanel" 
        class="admin-btn" 
        :icon="Setting" 
        circle 
      />
      <el-drawer
        v-model="adminPanelVisible"
        title="系统管理"
        direction="rtl"
        size="350px"
        :with-header="true"
        :show-close="true"
        :modal="true"
        :append-to-body="true"
      >
        <template #header>
          <div style="display: flex; justify-content: space-between; align-items: center;">
            <span>系统管理</span>
          </div>
        </template>
        <div class="admin-panel">
          <el-tabs>
            <el-tab-pane label="系统状态">
              <div class="status-section">
                <h4>RabbitMQ状态</h4>
                <el-descriptions :column="1" border>
                  <el-descriptions-item label="题目请求队列">
                    {{ rabbitMqStatus.question_request || 0 }}
                  </el-descriptions-item>
                  <el-descriptions-item label="评分请求队列">
                    {{ rabbitMqStatus.scoring_request || 0 }}
                  </el-descriptions-item>
                </el-descriptions>
                <h4>WebSocket连接数</h4>
                <el-descriptions :column="1" border>
                  <el-descriptions-item label="当前连接数">
                    {{ websocketCount || 0 }}
                  </el-descriptions-item>
                </el-descriptions>
              </div>
            </el-tab-pane>
            <el-tab-pane label="用户管理">
              <div class="user-management">
                <h4>封禁用户列表</h4>
                <div v-if="bannedUsers.length > 0">
                  <div v-for="user in bannedUsers" :key="user.id" class="banned-user-item">
                    <el-descriptions :column="1" border>
                      <el-descriptions-item label="用户ID">
                        {{ user.id }}
                      </el-descriptions-item>
                      <el-descriptions-item label="用户名">
                        {{ user.username }}
                      </el-descriptions-item>
                      <el-descriptions-item label="邮箱">
                        {{ user.email }}
                      </el-descriptions-item>
                      <el-descriptions-item label="解封时间">
                        {{ formatDate(user.liftTime) }}
                      </el-descriptions-item>
                    </el-descriptions>
                    <div class="banned-user-actions">
                      <el-button 
                        type="primary" 
                        size="small"
                        @click="updateBanUser(user.id, Date.now())"
                      >
                        解除封禁
                      </el-button>
                      <el-button 
                        type="warning" 
                        size="small"
                        @click="showUpdateBanDialog(user)"
                      >
                        修改时间
                      </el-button>
                    </div>
                  </div>
                </div>
                <el-empty v-else description="暂无封禁用户" />
                
                <div class="ban-user-section">
                  <h4>封禁用户</h4>
                  <el-form :model="banForm" label-width="80px">
                    <el-form-item label="用户ID">
                      <el-input v-model="banForm.userId" />
                    </el-form-item>
                    <el-form-item label="封禁时长">
                      <el-select v-model="banForm.duration">
                        <el-option label="1天" :value="24 * 60 * 60 * 1000" />
                        <el-option label="7天" :value="7 * 24 * 60 * 60 * 1000" />
                        <el-option label="30天" :value="30 * 24 * 60 * 60 * 1000" />
                        <el-option label="永久" :value="365 * 24 * 60 * 60 * 1000" />
                      </el-select>
                    </el-form-item>
                    <el-form-item>
                      <el-button type="primary" @click="banUser">确认封禁</el-button>
                    </el-form-item>
                  </el-form>
                </div>

                <div class="permission-section">
                  <h4>修改用户权限</h4>
                  <el-form :model="permissionForm" label-width="100px">
                    <el-form-item label="用户ID">
                      <el-input v-model="permissionForm.userId" />
                    </el-form-item>
                    <el-form-item label="权限">
                      <el-select v-model="permissionForm.permission">
                        <el-option label="普通用户" :value="0" />
                        <el-option label="管理员" :value="1" />
                        <el-option label="超级管理员" :value="0b111111111111111111" />
                      </el-select>
                    </el-form-item>
                    
                    <el-form-item>
                      <el-button type="primary" @click="updateUserPermission">确认修改</el-button>
                    </el-form-item>
                  </el-form>
                </div>
              </div>
            </el-tab-pane>
            <el-tab-pane label="通知管理">
              <div class="notice-management">
                <h4>发送通知</h4>
                <el-form :model="noticeForm" label-width="80px">
                  <el-form-item label="通知内容">
                    <el-input 
                      v-model="noticeForm.content" 
                      type="textarea" 
                      :rows="4"
                      placeholder="请输入通知内容"
                    />
                  </el-form-item>
                  <el-form-item>
                    <el-button type="primary" @click="sendNotice">发送通知</el-button>
                  </el-form-item>
                </el-form>
              </div>
            </el-tab-pane>
          </el-tabs>
        </div>
      </el-drawer>
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

  <!-- 修改编辑试卷对话框 -->
  <el-dialog
    v-model="showEditPaperDialog"
    title="编辑试卷"
    width="80%"
    :close-on-click-modal="false"
    class="edit-paper-dialog"
  >
    <el-form
      ref="editPaperFormRef"
      :model="editPaperForm"
      :rules="editPaperRules"
      label-width="100px"
    >
      <el-form-item label="试卷名称" prop="title">
        <el-input v-model="editPaperForm.title" placeholder="请输入试卷名称" />
      </el-form-item>
      
      <el-form-item label="试卷内容">
        <div class="paper-editor">
          <div class="editor-section">
            <h3>试卷描述</h3>
            <el-input
              v-model="editPaperForm.description"
              type="textarea"
              :rows="3"
              placeholder="请输入试卷描述"
            />
          </div>

          <div v-for="(block, blockIndex) in editPaperForm.questionBlocks" :key="blockIndex" class="question-block">
            <div class="block-header">
              <h3>题目组 {{ blockIndex + 1 }}</h3>
              <el-button type="danger" size="small" @click="removeQuestionBlock(blockIndex)">删除题目组</el-button>
            </div>
            
            <el-form-item label="题目组标题">
              <el-input v-model="block.questionsTitle" placeholder="请输入题目组标题" />
            </el-form-item>
            
            <el-form-item label="题目类型">
              <el-select v-model="block.questionType" placeholder="请选择题目类型">
                <el-option label="选择题" :value="1" />
                <el-option label="填空题" :value="2" />
                <el-option label="简答题" :value="3" />
              </el-select>
            </el-form-item>

            <div v-for="(question, qIndex) in block.questions" :key="qIndex" class="question-item">
              <div class="question-header">
                <h4>题目 {{ qIndex + 1 }}</h4>
                <el-button type="danger" size="small" @click="removeQuestion(blockIndex, qIndex)">删除题目</el-button>
              </div>

              <el-form-item label="题目内容">
                <el-input
                  v-model="question.question"
                  type="textarea"
                  :rows="2"
                  placeholder="请输入题目内容"
                />
              </el-form-item>

              <template v-if="block.questionType === 1">
                <el-form-item label="选项A">
                  <el-input v-model="question.chooseA" placeholder="请输入选项A" />
                </el-form-item>
                <el-form-item label="选项B">
                  <el-input v-model="question.chooseB" placeholder="请输入选项B" />
                </el-form-item>
                <el-form-item label="选项C">
                  <el-input v-model="question.chooseC" placeholder="请输入选项C" />
                </el-form-item>
                <el-form-item label="选项D">
                  <el-input v-model="question.chooseD" placeholder="请输入选项D" />
                </el-form-item>
                <el-form-item label="正确答案">
                  <el-select v-model="question.rightChoose" placeholder="请选择正确答案">
                    <el-option label="A" value="A" />
                    <el-option label="B" value="B" />
                    <el-option label="C" value="C" />
                    <el-option label="D" value="D" />
                  </el-select>
                </el-form-item>
              </template>

              <template v-else>
                <el-form-item label="参考答案">
                  <el-input
                    v-model="question.rightAnswer"
                    type="textarea"
                    :rows="2"
                    placeholder="请输入参考答案"
                  />
                </el-form-item>
              </template>
            </div>

            <el-button type="primary" @click="addQuestion(blockIndex)" class="add-question-btn">
              添加题目
            </el-button>
          </div>

          <el-button type="primary" @click="addQuestionBlock" class="add-block-btn">
            添加题目组
          </el-button>
        </div>
      </el-form-item>
    </el-form>

    <template #footer>
      <span class="dialog-footer">
        <el-button @click="showEditPaperDialog = false">取消</el-button>
        <el-button type="primary" @click="submitEditPaper" :loading="isEditing">
          保存
        </el-button>
      </span>
    </template>
  </el-dialog>
</template>

<script setup>
import { Document, Reading, Upload, Sunny, Bell, User, School, Files, Calendar, Setting } from '@element-plus/icons-vue'
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
  // 1 根据不同的notice类型，渲染不同的内容
  const newMessage = {
    raw: data.content,
    id: data.id,
    title: '',
    type: data.type,
    content: data.content,
    time: data.timestamp,
    read: false
  }
  switch(data.type){
    // 1.1 SYSTEM_ALL 0通知
    case 0:
      newMessage.title = "系统通知"
      newMessage.content = data.content
      break;
    // 1.1 ADD_FRIEND_APPLY 1通知
    case 1:
      newMessage.title = "好友申请"
      //拓展文本
      newMessage.content = "用户：" + data.content.split(",")[1] +"请求加为好友"
      break;
    // 1.1 AGREE_FRIEND_APPLY 2通知
    case 2:
      newMessage.title = "好友申请结果"
      //拓展文本
      newMessage.content = data.content.split(",")[1] +"同意了您的好友申请。"
      break;
    // 1.1 WAITING_GENERATION 3通知
    case 3:
      newMessage.title = data.content
      //拓展文本
      newMessage.content = "正在生成中..."
      break;
      // 1.1 WAITING_SCORING 4通知
      case 4:
      newMessage.title = "测试题"
      //拓展文本
      newMessage.content = "正在打分中..."
      break;
      case 5:
      // 1.1 OVER_WAITING 5通知
      
      newMessage.title =  data.content.split(",")[1] +"任务完成"
      //拓展文本
      newMessage.content = data.content.split(",")[0]
      // 1.1.2 删除对应的等待类型通知
      let deleteMessageId =  data.content.split(",")[2]
      messages.value = messages.value.filter(message => message.id !== deleteMessageId)
      break;
      case 6:
        // 1.1 ADD_CLASS_APPLY 6通知
      newMessage.title =  "班级申请"
      //拓展文本
      newMessage.content = `用户:${data.content.split(",")[3]} 申请加入班级: ${data.content.split(",")[4]}。\n"+"申请理由: ${data.content.split(",")[2]}`
            break;
      // 1.1 AGREE_CLASS_APPLY 7通知
      case 7:
        newMessage.title =  "班级申请成功"
      //拓展文本
      newMessage.content = `已加入班级  ${data.content.split(",")[1]}`
            break;
      // 1.1 REJECT_CLASS_APPLY 8通知
      case 8:
      newMessage.title =  "班级申请被拒绝"
      //拓展文本
      newMessage.content = `无法加入班级  ${data.content.split(",")[1]}`
            break;
      // 1.1 REJECT_FRIEND_APPLY 9通知
      case 9:
        //TODO: 暂时没做
            break;
        // 1.1 KICK_OUT_CLASS 10通知
      case 10:
         //TODO: 暂时没做
            break;
         // 1.1 CLASS_UPDATE 11通知
      case 11:
        newMessage.title =  "班级信息更新"
      //拓展文本
      newMessage.content = `${data.content.split(",")[2]} 信息更新提示: ${data.content.split(",")[1]}`
            break;
      // 1.1 SYSTEM_ONE -1通知
      case -1:
        newMessage.title = "系统通知"
        newMessage.content = data.content
              break;

  }
  // 如果是over类型的通知，那么需要对应删除wating对应的通知。
  // 使用数组解构来创建新的数组，确保触发响应式更新
  messages.value = [newMessage, ...messages.value]
  unreadCount.value = messages.value.filter(msg => !msg.read).length
}
// STOMP 消息订阅----------------

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
  //弹出消息
  handleStompMessage(message);
  //存入通知列表
  addMessages(message);
});
// STOMP 消息订阅===============

// 模拟获取消息数据
const fetchMessages = async () => {
  try {
    // 调用实际API获取消息
    const response = await instance.get('/notice/list')
    
    // 转换API返回数据为前端需要的格式
    messages.value = response.data.data.map(item => ({
      raw: item.content,
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
      if(msg.type === 3 || msg.type === 4 ) msg.read = false
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
function renderExamToHTML(paper) {
  let examData = JSON.parse(paper.content)
  let html = `<h1>${paper.name}</h1><h3>试卷描述：</h3><p>${escapeHtml(examData.description)}</p>`;
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
    ElMessageBox.alert(renderExamToHTML(paper), paper.name, {
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
    const content = renderExamToHTML(paper)
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

// 在 script setup 中修改相关状态和方法
const showEditPaperDialog = ref(false)
const editPaperFormRef = ref(null)
const isEditing = ref(false)
const currentPaperId = ref(null)

const editPaperForm = ref({
  title: '',
  description: '',
  questionBlocks: []
})

const editPaperRules = {
  title: [
    { required: true, message: '请输入试卷名称', trigger: 'blur' },
    { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  description: [
    { required: true, message: '请输入试卷描述', trigger: 'blur' },
    { min: 5, max: 200, message: '长度在 5 到 200 个字符', trigger: 'blur' }
  ]
}

// 处理编辑试卷
const handleEditPaper = (paper) => {
  currentPaperId.value = paper.id
  try {
    const paperContent = typeof paper.content === 'string' ? JSON.parse(paper.content) : paper.content
    editPaperForm.value = {
      title: paper.name,
      description: paperContent.description || '',
      questionBlocks: paperContent.questionBlocks || []
    }
    showEditPaperDialog.value = true
  } catch (error) {
    console.error('解析试卷内容失败:', error)
    ElMessage.error('试卷格式错误，无法编辑')
  }
}

// 添加题目组
const addQuestionBlock = () => {
  editPaperForm.value.questionBlocks.push({
    questionsTitle: '',
    questionType: 1,
    questions: []
  })
}

// 删除题目组
const removeQuestionBlock = (blockIndex) => {
  editPaperForm.value.questionBlocks.splice(blockIndex, 1)
}

// 添加题目
const addQuestion = (blockIndex) => {
  const block = editPaperForm.value.questionBlocks[blockIndex]
  block.questions.push({
    question: '',
    rightChoose: '',
    rightAnswer: '',
    chooseA: '',
    chooseB: '',
    chooseC: '',
    chooseD: ''
  })
}

// 删除题目
const removeQuestion = (blockIndex, questionIndex) => {
  editPaperForm.value.questionBlocks[blockIndex].questions.splice(questionIndex, 1)
}

// 提交编辑试卷
const submitEditPaper = async () => {
  if (!editPaperFormRef.value) return
  
  try {
    await editPaperFormRef.value.validate()
    isEditing.value = true
    
    // 构建试卷内容
    const paperContent = {
      description: editPaperForm.value.description,
      questionBlocks: editPaperForm.value.questionBlocks
    }
    
    // 创建FormData对象并添加数据
    const formData = new FormData()
    formData.append('paperId', currentPaperId.value)
    formData.append('title', editPaperForm.value.title)
    formData.append('content', JSON.stringify(paperContent))
    
    const response = await instance.post('/paper/update', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
    
    if (response.data.code === 200) {
      ElMessage.success('试卷更新成功')
      showEditPaperDialog.value = false
      // 刷新试卷列表
      fetchPapers()
    } else {
      ElMessage.error(response.data.message || '更新试卷失败')
    }
  } catch (error) {
    console.error('更新试卷失败:', error)
    ElMessage.error('更新试卷失败，请稍后重试')
  } finally {
    isEditing.value = false
  }
}

// 添加管理按钮
const toggleAdminPanel = () => {
  adminPanelVisible.value = !adminPanelVisible.value
}

// 在 script setup 中添加新的状态和方法
const adminPanelVisible = ref(false)
const rabbitMqStatus = ref({})
const websocketCount = ref(0)
const bannedUsers = ref([])
const banForm = ref({})
const noticeForm = ref({})

// 添加新的方法
const sendNotice = async () => {
  if (!noticeForm.value.content) {
    ElMessage.warning('请输入通知内容')
    return
  }

  try {
    var formdata = new FormData()
    formdata.append("content",noticeForm.value.content)
    const response = await instance.post(getFullUrl(API_PATHS.NOTICE_ALL), formdata, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }})
    if (response.data && response.data.code === 200) {
      ElMessage.success('发送通知成功')
      noticeForm.value.content = ''
    } else {
      ElMessage.error(response.data?.message || '发送通知失败')
    }
  } catch (error) {
    console.error('发送通知失败:', error)
    ElMessage.error('发送通知失败')
  }
}

const updateBanUser = async (userId, liftTime) => {
  try {
    const response = await request.post(getFullUrl(API_PATHS.UPDATE_BAN_USER), null, {
      params: {
        banUserId: userId,
        liftTime: liftTime
      }
    })

    if (response.data && response.data.code === 200) {
      ElMessage.success('更新封禁用户成功')
      bannedUsers.value = JSON.parse(response.data.data)
    } else {
      ElMessage.error(response.data?.message || '更新封禁用户失败')
    }
  } catch (error) {
    console.error('更新封禁用户失败:', error)
    ElMessage.error('更新封禁用户失败')
  }
}

const showUpdateBanDialog = (user) => {
  // 实现显示更新封禁用户时间的对话框逻辑
  console.log('显示更新封禁用户:', user)
}

// 添加权限相关的状态和方法
const userPermission = ref(0)
const hasAdminPermission = computed(() => {
  // 检查是否有showInfo权限
  return (userPermission.value & (1 << 0)) !== 0
})

// 获取用户权限
const fetchUserPermission = async () => {
  try {
    const response = await instance.post(getFullUrl(API_PATHS.GET_USER_PERMISSION))
    if (response.data && response.data.code === 200) {
      userPermission.value = response.data.data
    }
  } catch (error) {
    console.error('获取用户权限失败:', error)
  }
}

// 获取RabbitMQ状态
const fetchRabbitMqStatus = async () => {
  try {
    const response = await request.post(getFullUrl(API_PATHS.SHOW_RABBITMQ_INFO))
    if (response.data && response.data.code === 200) {
      rabbitMqStatus.value = JSON.parse(response.data.data)
    }
  } catch (error) {
    console.error('获取RabbitMQ状态失败:', error)
  }
}

// 获取WebSocket连接数
const fetchWebsocketCount = async () => {
  try {
    const response = await instance.post(getFullUrl(API_PATHS.SHOW_WEBSOCKET))
    if (response.data && response.data.code === 200) {
      websocketCount.value = parseInt(response.data.data)
    }
  } catch (error) {
    console.error('获取WebSocket连接数失败:', error)
  }
}

// 获取封禁用户列表
const fetchBannedUsers = async () => {
  try {
    const response = await request.post(getFullUrl(API_PATHS.SHOW_BAN_USERS))
    if (response.data && response.data.code === 200) {
      // 解析返回的 JSON 字符串
      const bannedUsersData = JSON.parse(response.data.data)
      bannedUsers.value = bannedUsersData.map(user => ({
        id: user.user_id,
        email: user.email,
        username: user.username,
        liftTime: user.lift_time
      }))
    }
  } catch (error) {
    console.error('获取封禁用户列表失败:', error)
  }
}

// 封禁用户
const banUser = async () => {
  if (!banForm.value.userId || !banForm.value.duration) {
    ElMessage.warning('请填写完整的封禁信息')
    return
  }

  try {
    const liftTime = Date.now() + banForm.value.duration
    const response = await request.post(getFullUrl(API_PATHS.BAN_USER), null, {
      params: {
        banUserId: banForm.value.userId,
        liftTime: liftTime
      }
    })

    if (response.data && response.data.code === 200) {
      ElMessage.success('封禁用户成功')
      const bannedUsersData = JSON.parse(response.data.data)
      bannedUsers.value = bannedUsersData.map(user => ({
        id: user.user_id,
        email: user.email,
        username: user.username,
        liftTime: user.lift_time
      }))
    } else {
      ElMessage.error(response.data?.message || '封禁用户失败')
    }
  } catch (error) {
    console.error('封禁用户失败:', error)
    ElMessage.error('封禁用户失败')
  }
}

// 在组件挂载时获取权限
onMounted(() => {
  fetchUserPermission()
})

// 监听管理面板的显示状态
watch(adminPanelVisible, (newVal) => {
  if (newVal) {
    fetchRabbitMqStatus()
    fetchWebsocketCount()
    fetchBannedUsers()
  }
})

// 在 script setup 部分添加
const permissionForm = ref({
  userId: '',
  permission: 0
})

// 更新用户权限
const updateUserPermission = async () => {
  if (!permissionForm.value.userId) {
    ElMessage.warning('请输入用户ID')
    return
  }

  try {
    const response = await request.post(getFullUrl(API_PATHS.UPDATE_USER_PERMISSION), null, {
      params: {
        updateUserId: permissionForm.value.userId,
        permission: permissionForm.value.permission
      }
    })

    if (response.data && response.data.code === 200) {
      ElMessage.success('更新用户权限成功')
      permissionForm.value.userId = ''
      permissionForm.value.permission = 0
    } else {
      ElMessage.error(response.data?.message || '更新用户权限失败')
    }
  } catch (error) {
    console.error('更新用户权限失败:', error)
    ElMessage.error('更新用户权限失败')
  }
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

.paper-actions {
  display: flex;
  gap: 8px;
}

/* 修改编辑试卷对话框样式 */
.edit-paper-dialog {
  .paper-editor {
    padding: 20px;
    background-color: var(--el-bg-color);
    border-radius: 4px;
    width: 1392px;
  }

  .editor-section {
    margin-bottom: 20px;
  }

  .question-block {
    margin: 20px 0;
    padding: 20px;
    border: 1px solid var(--el-border-color-light);
    border-radius: 4px;
    background-color: var(--el-bg-color-overlay);
  }

  .block-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
  }

  .question-item {
    margin: 20px 0;
    padding: 20px;
    border: 1px solid var(--el-border-color-lighter);
    border-radius: 4px;
    background-color: var(--el-bg-color);
  }

  .question-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 15px;
  }

  .add-question-btn {
    margin-top: 10px;
  }

  .add-block-btn {
    margin-top: 20px;
  }
}

/* 管理面板样式 */
.admin-panel {
  padding: 20px;
}

.status-section {
  margin-bottom: 20px;
}

.status-section h4 {
  margin-bottom: 10px;
  color: var(--el-text-color-primary);
}

.user-management {
  margin-bottom: 20px;
}

.banned-user-item {
  margin-bottom: 15px;
  padding: 10px;
  border: 1px solid var(--el-border-color-light);
  border-radius: 4px;
}

.banned-user-actions {
  margin-top: 10px;
  display: flex;
  gap: 10px;
}

.ban-user-section {
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid var(--el-border-color-light);
}

.notice-management {
  padding: 10px;
}

.admin-btn {
  margin-right: 10px;
}

.permission-section {
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid var(--el-border-color-light);
}
</style>