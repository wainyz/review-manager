<template>
  <div class="message-list">
    <div class="button-container">
      <button 
        @click="showUnread = true" 
        :class="{ active: showUnread }"
      >
        未读消息 ({{ unreadCount }})
      </button>
      <button 
        @click="showUnread = false" 
        :class="{ active: !showUnread }"
      >
        已读消息 ({{ readCount }})
      </button>
    </div>
    <div class="message-content" v-if="filteredMessages.length > 0">
      <div class="message-item" 
        v-for="(message, index) in filteredMessages" :key="'msg-' + index"
        :class="{ 
          'unread': !message.read,
          'message-system': getMessageType(message) === '系统全局通知' || getMessageType(message) === '系统特别通知',
          'message-reminder': getMessageType(message) === '等待生成通知' || getMessageType(message) === '等待打分通知',
          'message-update': getMessageType(message) === '好友申请通知' || getMessageType(message) === '通过申请通知'
        }"
        @click="handleClick(message)"
        @mouseenter="hoveredMessage = message"
        @mouseleave="hoveredMessage = null"
      >
        <div class="close-button" @click.stop="handleClose(message)" v-if="!isWaitingNotification(message)">
          X
        </div>
        <div class="message-title">
          <span class="message-type-indicator" :class="`type-${getMessageType(message)}`"></span>
          <span>{{ message.title }}</span>
          <span class="message-time">{{ formatTime(message.time) }}</span>
        </div>
        <div class="message-body">{{ message.content }}</div>
        <!-- Tooltip -->
        <div class="tooltip" v-if="hoveredMessage === message">
          <p>{{ message.content }}</p>
        </div>
        <!-- Progress Bar for Waiting Notifications -->
        <div class="progress-bar" v-if="isWaitingNotification(message)">
          <div class="progress" :style="{ width: `${progressMap[message.id]}%` }"></div>
        </div>
      </div>
    </div>

    <div class="empty-message" v-else>
      <el-empty description="暂无消息" />
    </div>

    <!-- 详细窗口 ✖️-->
    <div class="detail-window" v-if="selectedMessage" @click.self="closeDetail">
      <div class="detail-content">
        <div class="detail-header">
          <h3>{{ selectedMessage.title }}</h3>
          <span class="detail-close" @click="closeDetail">✖️</span>
        </div>
        <div class="detail-body">
          <p>{{ selectedMessage.content }}</p>
        </div>
        <div class="detail-actions">
          <button v-if="getMessageType(selectedMessage) === '好友申请通知'" @click="acceptFriendRequest(selectedMessage)">
             接收
          </button>
          <button v-if="getMessageType(selectedMessage) === '好友申请通知'" @click="rejectFriendRequest(selectedMessage)">
             拒绝
          </button>
          <!-- 其他快捷功能按钮可以根据需要添加 -->
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { defineProps, defineEmits, computed, ref, onMounted, watch } from 'vue'
import instance from '../../config/axios'

const props = defineProps({
  messages: {
    type: Array,
    default: () => []
  }
})

const emit = defineEmits(['read-message', 'mark-all-read'])

const showUnread = ref(true)
const selectedMessage = ref(null)
const hoveredMessage = ref(null)

const openDetail = (message) => {
  if (!message.read) {
    emit('read-message', message.id)
  }
  selectedMessage.value = message
}

const closeDetail = () => {
  selectedMessage.value = null
}

const handleClose = (message) => {
  if (!message.read) {
    emit('read-message', message.id)
  } else {
    // 删除通知功能API暂时留空
    console.log('Delete notification:', message.id)
  }
}

// 判断是否是等待通知
const isWaitingNotification = (message) => {
  return [3, 4, 5].includes(message.type);
}

const handleClick = (message) => {
  if (!isWaitingNotification(message)) {
    openDetail(message);
  }
}

// 将消息分为未读消息和已读消息
const unreadMessages = computed(() => {
  return props.messages.filter(message => !message.read).sort((a, b) => b.time - a.time)
})

const readMessages = computed(() => {
  return props.messages.filter(message => message.read).sort((a, b) => b.time - a.time)
})

const filteredMessages = computed(() => {
  return showUnread.value ? unreadMessages.value : readMessages.value
})

const unreadCount = computed(() => unreadMessages.value.length)
const readCount = computed(() => readMessages.value.length)

// 根据消息标题判断消息类型
const getMessageType = (message) => {
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
  return typeTitles[message.type] || '通知'
}

// 模拟API调用获取实时的平均等待时间
let averageWaitTimes = ref(60)

// 记录每个消息的开始时间
const startTimeMap = ref({})
const messageSpeed = ref({})
const progressMap = ref({})
onMounted(() => {
  props.messages.forEach(message => {
    if (isWaitingNotification(message)) {
      // console.log(message)
      startTimeMap.value[message.id] = message.time;
      messageSpeed.value[message.id] = Math.random();
    }
  });
  setInterval(() => {
    filteredMessages.value.forEach(message => {
      if (isWaitingNotification(message)) {
        progressMap.value[message.id] = calculateProgress(message);
      }
    });
  }, 1000);
  // 定时器模拟API调用
  setInterval(() => {
    updateAverageWaitTimes();
  }, 60000); // 每60秒更新一次
});

watch(filteredMessages, (newMessages) => {
  newMessages.forEach(message => {
    if (isWaitingNotification(message) && !startTimeMap.value[message.id]) {
      startTimeMap.value[message.id] = Date.now();
    }
  });
});


const updateAverageWaitTimes = async () => {
  try {
    // 从API获取新的平均等待时间
    const response = await instance.get('/notice/waiting/average_time');
    averageWaitTimes.value = response.data.data;
  } catch (error) {
    console.error('获取平均等待时间失败:', error);
    // 设置一个默认值，避免系统出错
    averageWaitTimes.value = 60;
  }
};

// 计算进度
const calculateProgress = (message) => {
  const startTime = message.time;
  const currentTime = Date.now();
  // 计算任务已经执行的时间
  const elapsedTime = (currentTime - startTime) / 1000;
  // 获取平均等待时间
  const totalWaitTime = averageWaitTimes.value;

  if (elapsedTime >= totalWaitTime) {
    return 99; // 最大值为99%
  }
  return (elapsedTime/totalWaitTime)*100

  // let progress;


  // const realProgress = elapsedTime * 100 / totalWaitTime;

  // let randomSpeed = messageSpeed.value[message.id] ; // 随机速度
  // if(isNaN(randomSpeed)){
  //     startTimeMap.value[message.id] = message.time;
  //     messageSpeed.value[message.id] = Math.random();
  // }
  // let speed =  randomSpeed;
  // if (realProgress <= 30) {
  //   // 前30% 到
   
  //   progress = realProgress * (1 + speed);
  // } else if (realProgress <= 70) {
  //   // 从80%到90%继续线性增长;
  //   progress = realProgress * (1.2 + speed);
  // } else {
  //   // 从70%到99%指数级减缓
  //   progress = realProgress * (1.3 + speed);
  // }
  // if (progress > 99) {
  //     progress = 99; // 最大值为99%
  //   }
  // //TODO 这里有报错
  // // console.log("平均等待时间，以及当前执行时间(s):",totalWaitTime,elapsedTime)
  // // console.log("计算得到的进度：",message.id,realProgress,progress)
  return progress;
}

// 格式化时间
const formatTime = (timestamp) => {
  const date = new Date(timestamp)
  const now = new Date()
  
  // 今天的消息只显示时间
  if (date.toDateString() === now.toDateString()) {
    return date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
  }
  
  // 昨天的消息显示"昨天"
  const yesterday = new Date(now)
  yesterday.setDate(now.getDate() - 1)
  if (date.toDateString() === yesterday.toDateString()) {
    return `昨天 ${date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })}`
  }
  
  // 其他日期显示完整日期
  return date.toLocaleDateString('zh-CN', { month: '2-digit', day: '2-digit' }) + ' ' + 
         date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
}

const acceptFriendRequest = async (message) => {
  // applyUserId=1,applyGoalUserId=3,
  let applyUserId = getParamValue(message.content,"applyUserId")
  // 处理接受好友请求的逻辑
  console.log('Accept friend request:', message)
  const response = await instance.get(`/friends/apply/agree/${applyUserId}`)
  console.log("好友处理",response)
}
const rejectFriendRequest = async (message) => {
  // applyUserId=1,applyGoalUserId=3,
  let applyUserId = getParamValue(message.content,"applyUserId")
  // 处理接受好友请求的逻辑
  console.log('Accept friend request:', message)
  const response = await instance.get(`/friends/apply/reject/${applyUserId}`)
  console.log("好友处理",response)
}
function getParamValue(str, paramName) {
  const parts = str.split(',');
  for (let part of parts) {
    if (part.startsWith(paramName + '=')) {
      return part.split('=')[1];
    }
  }
  return null; // 没有找到返回 null
}

</script>

<style scoped>
.message-list {
  width: 100%; /* 调整为100%以适应容器 */
  background-color: var(--el-bg-color);
  border-radius: 4px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  overflow-y: auto;
  position: relative;
}

.button-container {
  display: flex;
  justify-content: space-around;
  padding: 8px 0;
  border-bottom: 1px solid var(--el-border-color-lighter);
}

.button-container button {
  padding: 6px 12px;
  border: none;
  background-color: transparent;
  cursor: pointer;
  transition: background-color 0.3s;
}

.button-container button.active {
  background-color: #ccc;
}

.message-content {
  padding: 8px 0;
}

.message-item {
  position: relative;
  padding: 12px 16px;
  border-bottom: 1px solid var(--el-border-color-lighter);
  cursor: pointer;
  transition: background-color 0.3s;
}

.message-item:hover {
  background-color: var(--el-fill-color-light);
}

.message-item.unread {
  background-color: var(--el-color-primary-light-9);
}

.close-button {
  position: absolute;
  top: 12px;
  right: 16px;
  cursor: pointer;
  font-size: 16px;
  color: #999;
  z-index: 1; /* 确保按钮在最上方 */
}

/* 不同类型消息的样式 */
.message-system {
  border-left: 3px solid var(--el-color-primary);
}

.message-reminder {
  border-left: 3px solid var(--el-color-warning);
}

.message-update {
  border-left: 3px solid var(--el-color-success);
}

.message-title {
  display: flex;
  justify-content: left;
  align-items: center; /* 垂直居中对齐 */
  margin-bottom: 4px;
  font-weight: 500;
}

.message-type-indicator {
  display: inline-block;
  width: 8px;
  height: 8px;
  border-radius: 50%;
  margin-right: 8px;
}

.type-系统全局通知 {
  background-color: var(--el-color-primary);
}

.type-好友申请通知 {
  background-color: var(--el-color-success);
}

.type-通过申请通知 {
  background-color: var(--el-color-success);
}

.type-等待生成通知 {
  background-color: var(--el-color-warning);
}

.type-等待打分通知 {
  background-color: var(--el-color-warning);
}

.type-完成等待通知 {
  background-color: var(--el-color-warning);
}

.type-系统特别通知 {
  background-color: var(--el-color-primary-dark-2);
}

.type-默认 {
  background-color: var(--el-color-info);
}

.message-time {
  font-size: 12px;
  color: var(--el-text-color-secondary);
  font-weight: normal;
  padding-left: 2em;
}

.message-body {
  font-size: 14px;
  color: var(--el-text-color-regular);
  overflow-wrap: break-word; /* 确保长文本换行 */
}

.tooltip {
  position: absolute;
  bottom: calc(100% + 8px); /* 距离底部的距离 */
  left: 50%;
  transform: translateX(-50%);
  background-color: rgba(0, 0, 0, 0.8);
  color: white;
  padding: 10px;
  border-radius: 4px;
  white-space: normal; /* 允许文本换行 */
  max-width: 300px; /* 设置最大宽度 */
  word-wrap: break-word; /* 确保长单词也能换行 */
  z-index: 1000; /* 确保 tooltip 在最上方 */
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.5);
  pointer-events: none; /* 防止tooltip阻止鼠标事件 */
  opacity: 1;
  transition: opacity 0.3s ease;
  font-size: 13px;
  line-height: 1.4;
  text-align: left;
  overflow: hidden;
  max-height: 200px; /* 限制最大高度 */
}

.progress-bar {
  margin-top: 8px;
  width: 100%;
  background-color: #e0e0df;
  border-radius: 8px;
  overflow: hidden;
  height: 8px;
}

.progress {
  height: 100%;
  background-color: var(--el-color-primary);
  width: 0%; /* Initial width */
  transition: width 0.5s ease-in-out;
}

.empty-message {
  padding: 20px 0;
}

/* 分割线样式 */
.message-divider {
  margin: 10px 0;
  border-top: 1px solid var(--el-border-color-lighter);
  text-align: center;
  color: var(--el-text-color-secondary);
}

/* 详细窗口样式 */
.detail-window {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
}

.detail-content {
  background-color: white;
  width: 90%;
  max-width: 400px;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  overflow-y: auto;
  padding: 16px;
}

.detail-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.detail-header h3 {
  margin: 0;
}

.detail-close {
  cursor: pointer;
  font-size: 16px;
  color: #999;
}

.detail-body {
  margin-bottom: 16px;
}

.detail-actions {
  text-align: right;
}

.detail-actions button {
  padding: 6px 12px;
  border: none;
  background-color: var(--el-color-primary);
  color: white;
  cursor: pointer;
  border-radius: 4px;
  transition: background-color 0.3s;
}

.detail-actions button:hover {
  background-color: var(--el-color-primary-dark-2);
}



</style>