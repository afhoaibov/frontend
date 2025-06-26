import { ElNotification } from 'element-plus'
import { useUserStore } from '@/stores/user'

class SimpleWebSocketService {
  constructor() {
    this.ws = null
    this.connected = false
    this.userStore = null
    this.reconnectAttempts = 0
    this.maxReconnectAttempts = 5
    this.reconnectInterval = 3000
    this.heartbeatInterval = null
    this.isDestroyed = false
    // 添加事件监听器数组
    this.eventListeners = {
      'notification': []
    }
  }

  // 添加事件监听器
  addEventListener(event, callback) {
    if (!this.eventListeners[event]) {
      this.eventListeners[event] = []
    }
    this.eventListeners[event].push(callback)
  }

  // 移除事件监听器
  removeEventListener(event, callback) {
    if (this.eventListeners[event]) {
      const index = this.eventListeners[event].indexOf(callback)
      if (index > -1) {
        this.eventListeners[event].splice(index, 1)
      }
    }
  }

  // 触发事件
  emit(event, data) {
    if (this.eventListeners[event]) {
      this.eventListeners[event].forEach(callback => {
        try {
          callback(data)
        } catch (error) {
          console.error('事件回调执行失败:', error)
        }
      })
    }
  }

  // 获取用户store
  getUserStore() {
    try {
      if (!this.userStore && !this.isDestroyed) {
        this.userStore = useUserStore()
      }
      return this.userStore
    } catch (error) {
      console.warn('获取用户store失败:', error)
      return null
    }
  }

  // 连接WebSocket
  connect(userId) {
    if (this.isDestroyed) {
      console.warn('WebSocket服务已销毁，无法连接')
      return Promise.reject(new Error('WebSocket服务已销毁'))
    }

    return new Promise((resolve, reject) => {
      try {
        // 使用原生WebSocket
        const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:'
        const wsUrl = `${protocol}//${window.location.host}/ws/websocket`
        
        this.ws = new WebSocket(wsUrl)
        
        this.ws.onopen = () => {
          if (this.isDestroyed) {
            this.ws.close()
            return
          }
          console.log('WebSocket连接成功')
          this.connected = true
          this.reconnectAttempts = 0
          
          // 发送用户认证信息
          this.sendMessage({
            type: 'AUTH',
            userId: userId
          })
          
          // 启动心跳
          this.startHeartbeat()
          
          resolve()
        }
        
        this.ws.onmessage = (event) => {
          if (this.isDestroyed) return
          try {
            const data = JSON.parse(event.data)
            this.handleMessage(data)
          } catch (error) {
            console.error('解析WebSocket消息失败:', error)
          }
        }
        
        this.ws.onclose = () => {
          if (this.isDestroyed) return
          console.log('WebSocket连接关闭')
          this.connected = false
          this.stopHeartbeat()
          
          // 自动重连
          this.handleReconnect(userId, reject)
        }
        
        this.ws.onerror = (error) => {
          if (this.isDestroyed) return
          console.error('WebSocket错误:', error)
          this.connected = false
          reject(error)
        }
        
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

  // 处理消息
  handleMessage(data) {
    if (data.type === 'NOTIFICATION') {
      this.handleNotification(data.payload)
    }
  }

  // 处理通知
  handleNotification(notification) {
    const { type, content, fromUser, timestamp } = notification
    
    // 更新未读消息数量
    try {
      const userStore = this.getUserStore()
      if (userStore) {
        userStore.updateUnreadCount()
      }
    } catch (error) {
      console.warn('无法更新未读消息数量:', error)
    }
    
    // 显示通知
    try {
      ElNotification({
        title: this.getNotificationTitle(type),
        message: content,
        type: this.getNotificationType(type),
        duration: 5000,
        position: 'top-right'
      })
    } catch (error) {
      console.warn('显示通知失败:', error)
    }
    
    console.log('收到通知:', notification)
    
    // 触发通知事件，让组件知道有新消息
    this.emit('notification', notification)
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

  // 发送消息
  sendMessage(message) {
    if (this.ws && this.connected) {
      try {
        this.ws.send(JSON.stringify(message))
      } catch (error) {
        console.error('发送消息失败:', error)
      }
    }
  }

  // 启动心跳
  startHeartbeat() {
    this.heartbeatInterval = setInterval(() => {
      this.sendMessage({ type: 'PING' })
    }, 30000) // 30秒发送一次心跳
  }

  // 停止心跳
  stopHeartbeat() {
    if (this.heartbeatInterval) {
      clearInterval(this.heartbeatInterval)
      this.heartbeatInterval = null
    }
  }

  // 断开连接
  disconnect() {
    this.isDestroyed = true
    if (this.ws) {
      try {
        this.ws.close()
      } catch (error) {
        console.error('断开WebSocket连接失败:', error)
      }
      this.connected = false
      this.reconnectAttempts = 0
      this.stopHeartbeat()
      this.ws = null
    }
    this.userStore = null
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
const simpleWebSocketService = new SimpleWebSocketService()

export default simpleWebSocketService 