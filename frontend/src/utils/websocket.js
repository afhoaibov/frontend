import SockJS from 'sockjs-client'
import { Stomp } from 'webstomp-client'
import { ElNotification } from 'element-plus'
import { useUserStore } from '@/stores/user'

class WebSocketService {
  constructor() {
    this.stompClient = null
    this.connected = false
    this.userStore = useUserStore()
    this.reconnectAttempts = 0
    this.maxReconnectAttempts = 5
    this.reconnectInterval = 3000
  }

  // 连接WebSocket
  connect(userId) {
    return new Promise((resolve, reject) => {
      try {
        const socket = new SockJS('/ws')
        this.stompClient = Stomp.over(socket)
        
        // 设置连接选项
        const connectOptions = {
          timeout: 5000,
          heartbeat: {
            incoming: 10000,
            outgoing: 10000
          }
        }
        
        this.stompClient.connect(
          connectOptions,
          (frame) => {
            console.log('WebSocket连接成功:', frame)
            this.connected = true
            this.reconnectAttempts = 0
            
            // 订阅个人通知
            this.subscribeToNotifications(userId)
            
            resolve(frame)
          },
          (error) => {
            console.error('WebSocket连接失败:', error)
            this.connected = false
            
            // 自动重连
            this.handleReconnect(userId, reject)
          }
        )
      } catch (error) {
        console.error('WebSocket初始化失败:', error)
        reject(error)
      }
    })
  }

  // 处理重连
  handleReconnect(userId, reject) {
    if (this.reconnectAttempts < this.maxReconnectAttempts) {
      this.reconnectAttempts++
      console.log(`尝试重连 (${this.reconnectAttempts}/${this.maxReconnectAttempts})...`)
      
      setTimeout(() => {
        this.connect(userId).catch(reject)
      }, this.reconnectInterval)
    } else {
      console.error('WebSocket重连失败，已达到最大重试次数')
      reject(new Error('WebSocket连接失败'))
    }
  }

  // 订阅通知
  subscribeToNotifications(userId) {
    if (!this.stompClient || !this.connected) return

    try {
      this.stompClient.subscribe(
        `/user/${userId}/queue/notifications`,
        (message) => {
          try {
            const notification = JSON.parse(message.body)
            this.handleNotification(notification)
          } catch (error) {
            console.error('解析通知消息失败:', error)
          }
        },
        {
          ack: 'auto'
        }
      )
      
      console.log('已订阅通知频道:', `/user/${userId}/queue/notifications`)
    } catch (error) {
      console.error('订阅通知失败:', error)
    }
  }

  // 处理通知
  handleNotification(notification) {
    const { type, content, fromUser, timestamp } = notification
    
    // 更新未读消息数量
    this.userStore.updateUnreadCount()
    
    // 显示通知
    ElNotification({
      title: this.getNotificationTitle(type),
      message: content,
      type: this.getNotificationType(type),
      duration: 5000,
      position: 'top-right'
    })
    
    console.log('收到通知:', notification)
  }

  // 获取通知标题
  getNotificationTitle(type) {
    const titles = {
      'LIKE': '点赞通知',
      'COMMENT': '评论通知',
      'FOLLOW': '关注通知',
      'SYSTEM': '系统通知'
    }
    return titles[type] || '新通知'
  }

  // 获取通知类型
  getNotificationType(type) {
    const types = {
      'LIKE': 'success',
      'COMMENT': 'info',
      'FOLLOW': 'warning',
      'SYSTEM': 'info'
    }
    return types[type] || 'info'
  }

  // 断开连接
  disconnect() {
    if (this.stompClient) {
      try {
        this.stompClient.disconnect(() => {
          console.log('WebSocket已断开连接')
        })
      } catch (error) {
        console.error('断开WebSocket连接失败:', error)
      }
      this.connected = false
      this.reconnectAttempts = 0
    }
  }

  // 发送消息
  send(destination, message) {
    if (this.stompClient && this.connected) {
      try {
        this.stompClient.send(destination, {}, JSON.stringify(message))
      } catch (error) {
        console.error('发送消息失败:', error)
      }
    }
  }

  // 检查连接状态
  isConnected() {
    return this.connected
  }

  // 重新连接
  reconnect(userId) {
    this.disconnect()
    setTimeout(() => {
      this.connect(userId)
    }, 1000)
  }
}

// 创建单例实例
const webSocketService = new WebSocketService()

export default webSocketService 