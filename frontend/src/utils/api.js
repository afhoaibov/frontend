import axios from 'axios'
import { ElMessage } from 'element-plus'

// 创建axios实例
const api = axios.create({
  baseURL: '/api',
  timeout: 10000,
  withCredentials: true
})

// 请求拦截器
api.interceptors.request.use(
  config => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

// 响应拦截器
api.interceptors.response.use(
  response => {
    return response.data
  },
  error => {
    console.error('API请求错误:', error)
    if (error.response) {
      const { status, data } = error.response
      
      switch (status) {
        case 401:
          ElMessage.error('登录已过期，请重新登录')
          localStorage.removeItem('token')
          window.location.href = '/login'
          break
        case 403:
          ElMessage.error('没有权限访问')
          break
        case 404:
          ElMessage.error('请求的资源不存在')
          break
        case 500:
          ElMessage.error('服务器内部错误')
          break
        default:
          ElMessage.error(data.message || '请求失败')
      }
    } else {
      ElMessage.error('网络错误，请检查网络连接')
    }
    return Promise.reject(error)
  }
)

// 动态相关API
export const postApi = {
  // 发布动态
  createPost: (data) => api.post('/posts', data),
  
  // 获取动态列表
  getPosts: (page = 0, size = 20) => 
    api.get(`/posts?page=${page}&size=${size}`),
  
  // 获取用户的动态列表
  getUserPosts: (userId, page = 0, size = 20) => 
    api.get(`/posts/user/${userId}?page=${page}&size=${size}`),
  
  // 删除动态
  deletePost: (postId, userId) => 
    api.delete(`/posts/${postId}`, { data: { userId } }),
  
  // 点赞动态
  likePost: (postId, userId) => 
    api.post(`/posts/${postId}/like`, { userId }),
  
  // 取消点赞动态
  unlikePost: (postId, userId) => 
    api.delete(`/posts/${postId}/like`, { data: { userId } }),
  
  // 获取动态评论列表
  getComments: (postId, page = 0, size = 20) => 
    api.get(`/posts/${postId}/comments?page=${page}&size=${size}`),
  
  // 发表评论
  createComment: (postId, data) => 
    api.post(`/posts/${postId}/comments`, data),
  
  // 删除评论
  deleteComment: (postId, commentId, userId) => 
    api.delete(`/posts/${postId}/comments/${commentId}`, { data: { userId } }),
  
  // 点赞评论
  likeComment: (postId, commentId, userId) => 
    api.post(`/posts/${postId}/comments/${commentId}/like`, { userId }),
  
  // 取消点赞评论
  unlikeComment: (postId, commentId, userId) => 
    api.delete(`/posts/${postId}/comments/${commentId}/like`, { data: { userId } })
}

// 排行榜相关API
export const rankingApi = {
  // 获取分数排行榜
  getScoreRanking: (start = 0, end = 9) => 
    api.get(`/ranking/score?start=${start}&end=${end}`),
  
  // 获取粉丝数排行榜
  getFollowerRanking: (start = 0, end = 9) => 
    api.get(`/ranking/followers?start=${start}&end=${end}`),
  
  // 获取动态数排行榜
  getPostRanking: (start = 0, end = 9) => 
    api.get(`/ranking/posts?start=${start}&end=${end}`),
  
  // 获取综合评分排行榜
  getCompositeScoreRanking: (start = 0, end = 9) => 
    api.get(`/ranking/composite-score?start=${start}&end=${end}`),
  
  // 获取用户排名
  getUserRank: (userId, type = 'score') => 
    api.get(`/ranking/user/${userId}?type=${type}`)
}

// 通知相关API
export const notificationApi = {
  // 获取用户消息
  getUserMessages: (userId, page = 0, size = 20) => 
    api.get(`/notifications/${userId}?page=${page}&size=${size}`),
  
  // 获取未读消息数量
  getUnreadCount: (userId) => 
    api.get(`/notifications/${userId}/unread`),
  
  // 标记所有消息为已读
  markAllAsRead: (userId) => 
    api.put(`/notifications/${userId}/read-all`),
  
  // 管理员发布通知
  publishNotification: (data) => 
    api.post('/notifications/admin/publish', data),
  
  // 获取管理员通知列表
  getAdminNotifications: (page = 0, size = 20) => 
    api.get(`/notifications/admin?page=${page}&size=${size}`),
  
  // 删除通知
  deleteNotification: (notificationId) => 
    api.delete(`/notifications/${notificationId}`)
}

// 用户相关API
export const userApi = {
  // 用户登录
  login: (data) => api.post('/auth/login', data),
  
  // 用户注册
  register: (data) => api.post('/auth/register', data),
  
  // 获取用户信息
  getUserInfo: (userId) => api.get(`/users/${userId}`),
  
  // 更新用户信息
  updateUserInfo: (userId, data) => api.put(`/users/${userId}`, data),
  
  // 上传头像
  uploadAvatar: (userId, file) => {
    const formData = new FormData()
    formData.append('avatar', file)
    return api.post(`/users/${userId}/avatar`, formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
  },
  
  // 获取所有用户列表
  getAllUsers: () => api.get('/users/all')
}

export default api 