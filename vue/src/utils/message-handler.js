// message-handler.js
import { ElMessage, ElMessageBox } from 'element-plus'
import { h } from 'vue'
/**
 * 处理STOMP消息并根据type展示通知
 * @param {Object} message - STOMP消息对象
 */
export function handleStompMessage(message) {
  const position = {
    offset: 20,
    duration: 3000,
    showClose: true
  };

  try {
    const data = JSON.parse(message.body)
    const { type, content } = data

    switch (type) {
      case 0: // 系统全局信息
        ElMessage({
          ...position,
          type: 'info',
          message: content
        })
        break

      case -1: // 系统特别通知
        ElMessage({
          ...position,
          type: 'warning',
          message: content
        })
        break

      case 1: // 好友申请通知
        ElMessage({
          ...position,
          type: 'info',
          message: content
        })
        break

      case 2: // 申请通过通知
        ElMessage({
          ...position,
          type: 'success',
          message: content
        })
        break

      case 3: // 等待生成通知
        ElMessage({
          ...position,
          type: 'info',
          message: content
        })
        break

      case 4: // 等待打分通知
        ElMessage({
          ...position,
          type: 'info',
          message: content
        })
        break

      case 5: // 完成通知
        ElMessage({
          ...position,
          type: 'success',
          message: content
        })
        break

      case 61: // 好友消息
        const [direction, msgType, ...msgContent] = content.split(':')
        const friendsList = JSON.parse(localStorage.getItem('friendsList') || '[]')
        const userId = data.userId
        const friend = friendsList.find(f => f.id === userId)
        const username = friend ? friend.username : '未知用户'
        const messageDirection = direction === '0' ? '发送给' : '收到来自'
        const displayContent = msgContent.join(':')
        
        ElMessage({
          ...position,
          type: 'info',
          message: `${messageDirection} ${username}: ${displayContent}`
        })
        break

      default:
        console.warn('未知消息类型:', data)
    }
  } catch (e) {
    console.error('消息处理失败:', e)
    ElMessage.error('消息解析失败')
  }
}
// 示例函数（需根据实际项目实现）
function markAsRead(messageId) {
  console.log('标记消息为已读:', messageId)
  // 调用API接口更新消息状态
}

function redirectTo(path) {
  // 路由跳转逻辑
}
let messageQueue = [];
let isShowing = false;

function showNextMessage() {
  if (messageQueue.length === 0 || isShowing) return;
  
  isShowing = true;
  const { config, resolve } = messageQueue.shift();
  
  const instance = ElMessage({
    ...config,
    onClose: () => {
      isShowing = false;
      resolve();
      showNextMessage();
    }
  });

  // 自动处理持续时间
  if (config.duration > 0) {
    setTimeout(() => {
      instance.close();
    }, config.duration);
  }
}

export function queueMessage(config) {
  return new Promise(resolve => {
    messageQueue.push({ config, resolve });
    showNextMessage();
  });
}

// // 使用示例
// queueMessage({
//   type: 'success',
//   message: '操作成功',
//   duration: 2000
// });