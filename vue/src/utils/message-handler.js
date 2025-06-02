// message-handler.js
import { ElMessage, ElMessageBox } from 'element-plus'
import { h } from 'vue'
/**
 * 处理STOMP消息并根据type展示通知
 * @param {Object} message - STOMP消息对象
 */
export function handleStompMessage(message) {
    const position = {
        offset: 50,      // 距离顶部40px
        customClass: 'right-top-message',
        appendTo: document.querySelector('#message-container') || document.body // 指定容器
      };
  try {
    const data = JSON.parse(message.body)
    const { type, content, timestamp } = data
    const time = new Date(timestamp).toLocaleString()

    switch (type) {
      case 0: // 系统全局信息
      queueMessage({
        ...position,
        showClose: true,
        type: 'info',
        message: `📢 系统公告 (${time}): ${content}`,
        duration: 2000
      });
        break

      case -1: // 系统精确通知
      queueMessage({
        ...position,
        showClose: true,
        type: 'warning',
        message: `⚠️ 重要通知 (${time}): ${content}`,
        duration: 6000,
        onClose: () => markAsRead(data.id) // 标记已读回调
      });
        break

      case 1: // 用户申请通知
        ElMessage.success({ ...position,
          message: `✅ 申请已提交: ${content}`,
          duration: 5000,
          grouping: true,
          showClose: true
        })
        break

      case 2: // 申请通过通知
        ElMessageBox.alert(
          `🎉 申请通过 (${time}): ${content}`,
          '操作成功',
          {
            confirmButtonText: '我知道了',
            callback: () => redirectTo('/approved'),
            showClose: true
          }
        )
        break

      case 3: // 等待生成通知
        showProgressMessage('⏳ 正在生成报告', content,)
        break

      case 4: // 等待打分通知
        showProgressMessage('📝 评分进行中', content, 60)
        break

      case 5: // 等待完成通知
        showProgressMessage('✨ 已完成', content, 90)
        break

      case 61:
        // 根据userid，从本地存储的好友列表中找到对应的username
        //  然后解析content，第一个冒号前的事方向，定义0值方向为id小的向id大的用户发送消息，所以1值方向为id大的向id小的用户发送消息
        // 然后第二个冒号前是消息的类型，这个暂时没有拓展，所以暂不处理
        //第二个冒号后的就是消息的内容
        const [direction, msgType, ...msgContent] = content.split(':');
        // 从localStorage获取好友列表
        const friendsList = JSON.parse(localStorage.getItem('friendsList') || '[]');
        const userId = data.userId;
        
        // 查找好友信息
        const friend = friendsList.find(f => f.id === userId);
        const username = friend ? friend.username : '未知用户';
        
        // 构建消息显示内容
        const messageDirection = direction === '0' ? '发送给' : '收到来自';
        const displayContent = msgContent.join(':'); // 重新组合消息内容
        
        const finalMessage = `${messageDirection} ${username} 的消息: ${displayContent}`;
        
        ElMessage({
          type: 'success',
          message: finalMessage,
          duration: 5000,
          showClose: true,
          offset: 500,
          customClass: 'friend-message'
        });
        break
      default:
        console.warn('未知消息类型:', data)
    }
  } catch (e) {
    console.error('消息处理失败:', e, '原始数据:', message.body)
    ElMessage.error('消息解析失败，请检查控制台')
  }
}

// 辅助函数：显示进度条消息
function showProgressMessage(title, detail, percent = 30) {
  const message = `${title}\n${detail}`;
  ElMessage({
    type: 'info',
    duration: 5000,
    message: () => 
      h('div', [
        h('p', { class: 'font-bold' }, title),
        h('el-progress', { 
          percentage: percent,
          status: percent > 70 ? 'success' : ''
        }),
        h('p', { class: 'text-xs mt-2' }, detail)
      ]),
    customClass: 'progress-message'
  });
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