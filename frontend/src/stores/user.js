import { defineStore } from 'pinia'
import { ref } from 'vue'
import api from '@/utils/api'

export const useUserStore = defineStore('user', () => {
  // 从localStorage恢复用户信息
  const getUserFromStorage = () => {
    try {
      const userStr = localStorage.getItem('user')
      return userStr ? JSON.parse(userStr) : null
    } catch (error) {
      console.error('解析用户信息失败:', error)
      return null
    }
  }

  const user = ref(getUserFromStorage())
  const token = ref(localStorage.getItem('token') || '')
  const unreadCount = ref(0)

  // 设置用户信息
  const setUser = (userData) => {
    user.value = userData
    // 保存用户信息到localStorage
    if (userData) {
      localStorage.setItem('user', JSON.stringify(userData))
      if (userData.role) {
        localStorage.setItem('userRole', userData.role)
      }
    } else {
      localStorage.removeItem('user')
      localStorage.removeItem('userRole')
    }
  }

  // 设置token
  const setToken = (tokenValue) => {
    token.value = tokenValue
    localStorage.setItem('token', tokenValue)
    // 设置到API实例的默认headers
    if (tokenValue) {
      api.defaults.headers.common['Authorization'] = `Bearer ${tokenValue}`
    } else {
      delete api.defaults.headers.common['Authorization']
    }
  }

  // 清除用户信息
  const clearUser = () => {
    user.value = null
    token.value = ''
    localStorage.removeItem('token')
    localStorage.removeItem('userRole')
    localStorage.removeItem('user')
    delete api.defaults.headers.common['Authorization']
  }

  // 初始化用户状态（在应用启动时调用）
  const initUserState = () => {
    const storedToken = localStorage.getItem('token')
    const storedUser = getUserFromStorage()
    
    if (storedToken && storedUser) {
      // 恢复token
      setToken(storedToken)
      // 恢复用户信息
      user.value = storedUser
    }
  }

  // 设置未读消息数量
  const setUnreadCount = (count) => {
    unreadCount.value = count
  }

  // 更新未读消息数量
  const updateUnreadCount = (increment = 1) => {
    unreadCount.value += increment
  }

  // 获取用户角色
  const getUserRole = () => {
    return user.value?.role || localStorage.getItem('userRole')
  }

  // 判断是否为管理员
  const isAdmin = () => {
    return getUserRole() === 'ADMIN'
  }

  // 验证token是否有效（可选，用于检查token是否过期）
  const validateToken = async () => {
    if (!token.value) return false
    
    try {
      // 这里可以调用后端API验证token是否有效
      // 例如：await api.get('/auth/validate')
      // 暂时返回true，实际项目中应该调用后端验证
      return true
    } catch (error) {
      console.error('Token验证失败:', error)
      clearUser()
      return false
    }
  }

  // 更新用户信息
  const updateUserInfo = (userInfo) => {
    if (user.value) {
      user.value = { ...user.value, ...userInfo }
      localStorage.setItem('user', JSON.stringify(user.value))
    }
  }

  // 更新用户头像
  const updateAvatar = (avatarUrl) => {
    if (user.value) {
      user.value.avatar = avatarUrl
      localStorage.setItem('user', JSON.stringify(user.value))
    }
  }

  return {
    user,
    token,
    unreadCount,
    setUser,
    setToken,
    clearUser,
    initUserState,
    setUnreadCount,
    updateUnreadCount,
    getUserRole,
    isAdmin,
    validateToken,
    updateUserInfo,
    updateAvatar
  }
}) 
 