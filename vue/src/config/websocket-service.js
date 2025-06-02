import { Client } from '@stomp/stompjs';

class WebSocketService {
  constructor() {
    if (WebSocketService.instance) {
      return WebSocketService.instance;
    }
    WebSocketService.instance = this;

    // 初始化代码...
    this.connectionPromise = null;
    this.connected = false;
    this.stompClient = null;
    this.globalSubscriptions = new Map();
    this.userSubscriptions = new Map();
    this.receivedMessages = new Set(); // 用于存储已接收的消息以实现去重

    // 初始化 STOMP 客户端
    this.initializeClient();
  }

  static getInstance() {
    if (!WebSocketService.instance) {
      WebSocketService.instance = new WebSocketService();
    }
    return WebSocketService.instance;
  }

  initializeClient() {
    const token = localStorage.getItem('token');

    this.stompClient = new Client({
      brokerURL: `ws://localhost:80/websocket?token=${encodeURIComponent(token)}`, // 替换为你的WebSocket服务器URL
      connectHeaders: {
        Authorization: token ? `${token}` : ''
      },
      reconnectDelay: 5000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,
      // debug: (str) => console.debug('STOMP:', str)
    });

    this.stompClient.onConnect = (frame) => {
      this.connected = true;
      console.log('WebSocket连接成功', frame);
    };

    this.stompClient.onStompError = (frame) => {
      console.error('STOMP协议错误:', frame.headers.message);
    };

    this.stompClient.onDisconnect = () => {
      this.connected = false;
      console.log('WebSocket连接断开');
    };
  }

  // 连接方法
  connect() {
    if (this.connectionPromise) {
      return this.connectionPromise;
    }

    if (this.connected) {
      return Promise.resolve();
    }

    this.connectionPromise = new Promise((resolve, reject) => {
      // 保存原始回调
      const originalOnConnect = this.stompClient.onConnect;
      const originalOnStompError = this.stompClient.onStompError;

      // 临时覆盖回调
      this.stompClient.onConnect = (frame) => {
        this.connected = true;
        console.log('WebSocket连接成功', frame);
        // 恢复原始回调
        this.stompClient.onConnect = originalOnConnect;
        resolve();
      };

      this.stompClient.onStompError = (frame) => {
        console.error('STOMP协议错误:', frame.headers.message);
        this.stompClient.onStompError = originalOnStompError;
        reject(new Error(frame.headers.message));
      };

      try {
        const token = localStorage.getItem('token');
        this.stompClient.activate({ 
          Authorization: token ? `${token}` : '' 
        });
      } catch (error) {
        reject(error);
      }
    });

    return this.connectionPromise;
  }

  // 订阅全局通知
  subscribeToGlobalNotifications(callback) {
    const subscriptionId = `global-${Date.now()}`;

    if (!this.globalSubscriptions.has(subscriptionId)) {
      this.connect().then(() => {
        const subscription = this.stompClient.subscribe('/all/notice', (message) => {
          if (!this.receivedMessages.has(message.body)) {
            this.receivedMessages.add(message.body);
            callback(message);
          }
        });
        this.globalSubscriptions.set(subscriptionId, subscription);
        console.log(`已订阅全局通知: ${subscriptionId}`);
      }).catch((error) => {
        console.error('连接失败:', error);
      });
    } else {
      console.warn(`重复的全局通知订阅尝试: ${subscriptionId}`);
    }

    return subscriptionId;
  }

  // 订阅用户个人通知
  subscribeToUserNotifications(callback) {
    const userId = localStorage.getItem('userId');
    const subscriptionId = `/user/${userId}/message`;

    if (!this.userSubscriptions.has(subscriptionId)) {
      this.connect().then(() => {
        const subscription = this.stompClient.subscribe(
          subscriptionId,
          (message) => {
            if (!this.receivedMessages.has(message.body)) {
              this.receivedMessages.add(message.body);
              callback(message);
            }
          },
          { id: subscriptionId } // 明确设置订阅ID
        );
        this.userSubscriptions.set(subscriptionId, subscription);
        console.log(`已订阅用户通知: ${subscriptionId}`);
      }).catch((error) => {
        console.error('连接失败:', error);
      });
    } else {
      console.warn(`重复的用户通知订阅尝试: ${subscriptionId}`);
    }

    return subscriptionId;
  }
  subscribeToUserMessage(callback) {
    const userId = localStorage.getItem('userId');
    const subscriptionId = `/user/${userId}/message`;

    if (!this.userSubscriptions.has(subscriptionId)) {
      this.connect().then(() => {
        const subscription = this.stompClient.subscribe(
          subscriptionId,
          (message) => {
            if (!this.receivedMessages.has(message.body)) {
              this.receivedMessages.add(message.body);
              callback(message);
            }
          },
          { id: subscriptionId } // 明确设置订阅ID
        );
        this.userSubscriptions.set(subscriptionId, subscription);
        console.log(`已订阅用户通知: ${subscriptionId}`);
      }).catch((error) => {
        console.error('连接失败:', error);
      });
    } else {
      console.warn(`重复的用户通知订阅尝试: ${subscriptionId}`);
    }

    return subscriptionId;
  }
  subscribeAnyChannel(channel,callback) {
    const subscriptionId = channel;

    if (!this.userSubscriptions.has(subscriptionId)) {
      this.connect().then(() => {
        const subscription = this.stompClient.subscribe(
          channel,
          (message) => {
            if (!this.receivedMessages.has(message.body)) {
              this.receivedMessages.add(message.body);
              callback(message);
            }
          },
          { id: subscriptionId } // 明确设置订阅ID
        );
        this.userSubscriptions.set(subscriptionId, subscription);
        console.log(`已订阅频道: ${subscriptionId}`);
      }).catch((error) => {
        console.error('连接失败:', error);
      });
    } else {
      console.warn(`重复的用户通知订阅尝试: ${subscriptionId}`);
    }
    return subscriptionId;
  }
  subscribeToClassMessage(classId,callback) {
    const userId = localStorage.getItem('classId');
    const subscriptionId =  `/user/class/${userId}/message`;

    if (!this.userSubscriptions.has(subscriptionId)) {
      this.connect().then(() => {
        const subscription = this.stompClient.subscribe(
          subscriptionId,
          (message) => {
            if (!this.receivedMessages.has(message.body)) {
              this.receivedMessages.add(message.body);
              callback(message);
            }
          },
          { id: subscriptionId } // 明确设置订阅ID
        );
        this.userSubscriptions.set(subscriptionId, subscription);
        console.log(`已订阅班级通知: ${subscriptionId}`);
      }).catch((error) => {
        console.error('连接失败:', error);
      });
    } else {
      console.warn(`重复的用户通知订阅尝试: ${subscriptionId}`);
    }
    return subscriptionId;
  }
  // 取消订阅
  unsubscribe(subscriptionId) {
    if (this.globalSubscriptions.has(subscriptionId)) {
      this.globalSubscriptions.get(subscriptionId).unsubscribe();
      this.globalSubscriptions.delete(subscriptionId);
      return true;
    }

    if (this.userSubscriptions.has(subscriptionId)) {
      this.userSubscriptions.get(subscriptionId).unsubscribe();
      this.userSubscriptions.delete(subscriptionId);
      return true;
    }

    return false;
  }

  // 发送消息到服务器
  sendMessage(destination, body, headers = {}) {
    this.connect().then(() => {
      this.stompClient.publish({
        destination: `/ws${destination}`,
        body: JSON.stringify(body),
        headers: {
          ...headers,
          'content-type': 'application/json'
        }
      });
    }).catch((error) => {
      console.error('发送消息失败:', error);
    });
  }

  // 断开连接
  disconnect() {
    this.globalSubscriptions.forEach(sub => sub.unsubscribe());
    this.userSubscriptions.forEach(sub => sub.unsubscribe());
    this.globalSubscriptions.clear();
    this.userSubscriptions.clear();

    if (this.stompClient && this.connected) {
      this.stompClient.deactivate();
    }

    this.connected = false;
    this.connectionPromise = null;
  }
}

const webSocketService = new WebSocketService();
export default webSocketService;



