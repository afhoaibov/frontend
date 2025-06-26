<template>
  <div class="home-container">
    <!-- 顶部导航栏 -->
    <el-header class="header">
      <div class="header-content">
        <div class="logo">
          <h2>社交平台</h2>
        </div>
        <div class="user-menu">
          <!-- 已登录用户显示用户菜单 -->
          <el-dropdown v-if="userStore.user" @command="handleCommand">
            <div class="user-info">
              <el-avatar :size="32" :src="userStore.user?.avatar">
                {{ userStore.user?.nickname ? userStore.user.nickname.charAt(0) : userStore.user?.username?.charAt(0) }}
              </el-avatar>
              <span class="username">{{ userStore.user?.nickname || userStore.user?.username }}</span>
              <el-icon><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">
                  <el-icon><User /></el-icon>
                  个人资料
                </el-dropdown-item>
                <el-dropdown-item v-if="isAdmin" command="admin">
                  <el-icon><Setting /></el-icon>
                  管理员中心
                </el-dropdown-item>
<!--                <el-dropdown-item command="settings">-->
<!--                  <el-icon><Setting /></el-icon>-->
<!--                  设置-->
<!--                </el-dropdown-item>-->
                <el-dropdown-item divided command="logout">
                  <el-icon><SwitchButton /></el-icon>
                  退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
          
          <!-- 未登录用户显示登录/注册按钮 -->
          <div v-else class="auth-buttons">
            <el-button type="primary" @click="uiStore.openLoginModal()">
              登录
            </el-button>
            <el-button @click="uiStore.openRegisterModal()">
              注册
            </el-button>
          </div>
        </div>
      </div>
    </el-header>

    <el-container>
      <!-- 侧边栏 -->
      <el-aside width="400px" class="sidebar">
        <el-card class="sidebar-card">
          <template #header>
            <div class="sidebar-header">
              <h3>实时排行榜</h3>
              <el-button type="text" @click="$router.push('/ranking')">
                查看更多
              </el-button>
            </div>
          </template>
          
          <div class="ranking-list">
            <div 
              v-for="(userRanking, index) in topUsers" 
              :key="userRanking.user.id" 
              class="ranking-item"
              @click="viewProfile(userRanking.user.id)"
            >
              <div class="rank-number" :class="getRankClass(index + 1)">
                {{ index + 1 }}
              </div>
              <el-avatar :size="32" :src="userRanking.user.avatar">
                {{ userRanking.user.nickname ? userRanking.user.nickname.charAt(0) : userRanking.user.username.charAt(0) }}
              </el-avatar>
              <div class="user-info">
                <div class="username">{{ userRanking.user.nickname || userRanking.user.username }}</div>
                <div class="score">分数: {{ userRanking.compositeScore }}</div>
              </div>
            </div>
          </div>
        </el-card>

        <el-card class="sidebar-card" style="margin-top: 20px;">
          <template #header>
            <div class="sidebar-header">
              <h3>通知中心</h3>
              <el-badge v-if="userStore.user" :value="unreadCount" :hidden="unreadCount === 0">
                <el-button type="text" @click="showNotifications = true">
                  查看全部
                </el-button>
              </el-badge>
              <el-button v-else type="text" @click="uiStore.openLoginModal()">
                登录查看
              </el-button>
            </div>
          </template>
          
          <div v-if="userStore.user" class="notification-list">
            <div 
              v-for="notification in recentNotifications" 
              :key="notification.id" 
              class="notification-item"
              :class="{ unread: !notification.isRead }"
            >
              <div class="notification-content">{{ notification.content }}</div>
              <div class="notification-time">{{ formatTime(notification.createdAt) }}</div>
            </div>
          </div>
          <div v-else class="login-prompt-sidebar">
            <el-icon size="32" color="#909399"><Bell /></el-icon>
            <p>登录后查看通知</p>
          </div>
        </el-card>
      </el-aside>

      <!-- 主内容区 -->
      <el-main class="main-content">
        <!-- 发布动态卡片 - 只有登录用户才能看到 -->
        <el-card v-if="userStore.user" class="post-card">
          <template #header>
            <div class="post-header">
              <h2>发布动态</h2>
            </div>
          </template>
          
          <div class="post-form">
            <el-input
              v-model="postContent"
              type="textarea"
              :rows="4"
              placeholder="分享你的想法..."
              maxlength="500"
              show-word-limit
            />
            <div class="post-actions">
              <el-button type="primary" @click="publishPost" :loading="publishing">
                发布
              </el-button>
            </div>
          </div>
        </el-card>

        <!-- 未登录用户提示 -->
<!--        <el-card v-else class="login-prompt-card">-->
<!--          <div class="login-prompt">-->
<!--            <el-icon size="48" color="#409EFF"><User /></el-icon>-->
<!--            <h3>登录后发布你的第一条动态</h3>-->
<!--            <p>加入我们的社区，分享你的想法和故事</p>-->
<!--            <el-button type="primary" @click="uiStore.openLoginModal()">-->
<!--              立即登录-->
<!--            </el-button>-->
<!--            <el-button @click="uiStore.openRegisterModal()">-->
<!--              注册账号-->
<!--            </el-button>-->
<!--          </div>-->
<!--        </el-card>-->

        <div class="posts-container">
          <el-card 
            v-for="post in posts" 
            :key="post.id" 
            class="post-item"
            style="margin-bottom: 20px;"
          >
            <div class="post-user">
              <el-avatar :size="40" :src="post.userInfo?.avatar">
                {{ post.userInfo?.nickname ? post.userInfo.nickname.charAt(0) : post.userInfo?.username?.charAt(0) }}
              </el-avatar>
              <div class="user-info">
                <div class="username">{{ post.userInfo?.nickname || post.userInfo?.username }}</div>
                <div class="post-time">{{ formatTime(post.createdAt) }}</div>
              </div>
              <!-- 删除按钮 - 只有动态作者可以删除 -->
              <el-button 
                v-if="canDeletePost(post)"
                type="text" 
                size="small"
                @click="deletePost(post.id)"
                class="delete-btn"
              >
                <el-icon><Delete /></el-icon>
                删除
              </el-button>
            </div>
            
            <div class="post-content">
              {{ post.content }}
            </div>
            
            <div class="post-actions">
              <!-- 点赞按钮 -->
              <el-button 
                type="text" 
                @click="likePost(post.id)"
                :class="{ liked: post.isLiked }"
              >
                👍
                {{ post.likeCount || 0 }} 点赞
              </el-button>
              
              <!-- 评论按钮 -->
              <el-button 
                type="text" 
                @click="userStore.user ? showComments(post.id) : uiStore.openLoginModal()"
              >
                <el-icon><ChatDotRound /></el-icon>
                {{ post.commentCount || 0 }} 评论
              </el-button>
              
              <!-- 分享按钮 -->
<!--              <el-button -->
<!--                type="text" -->
<!--                @click="userStore.user ? sharePost(post.id) : uiStore.openLoginModal()"-->
<!--              >-->
<!--                <el-icon><Share /></el-icon>-->
<!--                分享-->
<!--              </el-button>-->
            </div>
          </el-card>

          <el-pagination
            v-if="totalPosts > pageSize"
            background
            layout="prev, pager, next"
            :total="totalPosts"
            :page-size="pageSize"
            :current-page="currentPage"
            @current-change="handlePageChange"
            class="pagination-container"
          />
        </div>
      </el-main>
    </el-container>

    <!-- 通知抽屉 -->
    <el-drawer
      v-model="showNotifications"
      title="通知中心"
      direction="rtl"
      size="400px"
    >
      <div class="notifications-drawer">
        <div 
          v-for="notification in notifications" 
          :key="notification.id" 
          class="notification-item"
          :class="{ unread: !notification.isRead }"
        >
          <div class="notification-content">{{ notification.content }}</div>
          <div class="notification-time">{{ formatTime(notification.createdAt) }}</div>
        </div>
      </div>
    </el-drawer>

    <!-- 评论模态框 -->
    <CommentModal
      v-model="showCommentModal"
      :post="currentPost"
      @comment-added="handleCommentAdded"
      @comment-deleted="handleCommentDeleted"
    />
  </div>
</template>

<script>
import { ref, onMounted, onUnmounted, computed, nextTick, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { rankingApi, notificationApi, postApi } from '@/utils/api'
import simpleWebSocketService from '@/utils/websocket-simple'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  ArrowDown,
  User,
  Setting,
  SwitchButton,
  Bell,
  ChatDotRound,
  Share,
  Delete
} from '@element-plus/icons-vue'
import dayjs from 'dayjs'
import relativeTime from 'dayjs/plugin/relativeTime'
import 'dayjs/locale/zh-cn'
import { useUiStore } from '@/stores/ui'
import CommentModal from '@/components/CommentModal.vue'

// 配置dayjs插件和语言
dayjs.extend(relativeTime)
dayjs.locale('zh-cn')

export default {
  name: 'Home',
  components: {
    Setting,
    SwitchButton,
    ArrowDown,
    User,
    CommentModal
  },
  setup() {
    const router = useRouter()
    const userStore = useUserStore()
    const uiStore = useUiStore()
    
    const topUsers = ref([])
    const posts = ref([])
    const notifications = ref([])
    const recentNotifications = ref([])
    const showNotifications = ref(false)
    const postContent = ref('')
    const publishing = ref(false)
    const unreadCount = ref(0)
    const currentPage = ref(1)
    const pageSize = ref(3)
    const totalPosts = ref(0)
    
    // 评论相关状态
    const showCommentModal = ref(false)
    const currentPost = ref(null)
    
    // WebSocket通知回调函数引用
    const notificationCallback = async (notification) => {
      console.log('收到WebSocket通知，刷新通知列表')
      // 刷新通知列表
      await fetchNotifications()
    }

    // 判断是否为管理员
    const isAdmin = computed(() => {
      return userStore.user?.role === 'ADMIN' || localStorage.getItem('userRole') === 'ADMIN'
    })

    // 获取排行榜数据
    const fetchTopUsers = async () => {
      try {
        const response = await rankingApi.getCompositeScoreRanking(0, 9)
        // 适配新的数据格式，使用userRankings数组
        const userRankings = response.userRankings || []
        // 按照compositeScore进行降序排序
        topUsers.value = userRankings.sort((a, b) => b.compositeScore - a.compositeScore)
      } catch (error) {
        console.error('获取排行榜数据失败:', error)
      }
    }

    // 获取动态列表
    const fetchPosts = async () => {
      try {
        const response = await postApi.getPosts(currentPage.value - 1, pageSize.value)
        posts.value = response.data || []
        totalPosts.value = response.totalElements || 0
        
        // 初始化动态数据
        posts.value.forEach(post => {
          // 确保点赞数有初始值
          if (post.likeCount === undefined || post.likeCount === null) {
            post.likeCount = 0
          }
          // 确保评论数有初始值
          if (post.commentCount === undefined || post.commentCount === null) {
            post.commentCount = 0
          }
          // 设置默认点赞状态
          post.isLiked = false
        })
        
        console.log('加载的动态数据:', posts.value)
      } catch (error) {
        console.error('获取动态列表失败:', error)
      }
    }

    // 获取通知数据
    const fetchNotifications = async () => {
      if (!userStore.user || !userStore.user.id) return
      
      try {
        const response = await notificationApi.getUserMessages(userStore.user.id, 0, 20)
        notifications.value = response.messages || []
        recentNotifications.value = notifications.value.slice(0, 5)
        
        const unreadResponse = await notificationApi.getUnreadCount(userStore.user.id)
        unreadCount.value = unreadResponse.unreadCount || 0
        userStore.setUnreadCount(unreadCount.value)
      } catch (error) {
        console.error('获取通知数据失败:', error)
      }
    }

    // 发布动态
    const publishPost = async () => {
      if (!postContent.value.trim()) {
        ElMessage.warning('请输入动态内容')
        return
      }
      
      publishing.value = true
      try {
        const response = await postApi.createPost({
          userId: userStore.user.id,
          content: postContent.value
        })
        
        ElMessage.success(response.message || '动态发布成功')
        postContent.value = ''
        
        // 重新获取动态列表
        await fetchPosts()
      } catch (error) {
        ElMessage.error('发布失败')
      } finally {
        publishing.value = false
      }
    }

    // 点赞动态
    const likePost = async (postId) => {
      if (!userStore.user) {
        uiStore.openLoginModal()
        return
      }
      
      try {
        const post = posts.value.find(p => p.id === postId)
        if (!post) return
        
        let response
        if (post.isLiked) {
          // 如果已经点赞，则取消点赞
          response = await postApi.unlikePost(postId, userStore.user.id)
          post.isLiked = false
          ElMessage.success(response.message || '取消点赞成功')
        } else {
          // 如果未点赞，则点赞
          response = await postApi.likePost(postId, userStore.user.id)
          post.isLiked = true
          ElMessage.success(response.message || '点赞成功')
        }
        
        // 更新点赞数 - 根据后端返回的数据结构更新
        if (response && response.likeCount !== undefined) {
          post.likeCount = response.likeCount
        } else if (response && response.data && response.data.likeCount !== undefined) {
          post.likeCount = response.data.likeCount
        } else {
          // 如果后端没有返回点赞数，手动更新
          post.likeCount = post.likeCount || 0
          if (post.isLiked) {
            post.likeCount++
          } else {
            post.likeCount = Math.max(0, post.likeCount - 1)
          }
        }
        
        console.log('动态点赞响应:', response)
        console.log('更新后的点赞数:', post.likeCount)
      } catch (error) {
        console.error('动态点赞失败:', error)
        ElMessage.error('操作失败')
      }
    }

    // 查看评论
    const showComments = (postId) => {
      if (!userStore.user) {
        uiStore.openLoginModal()
        return
      }
      
      // 找到对应的动态
      const post = posts.value.find(p => p.id === postId)
      if (post) {
        currentPost.value = post
        showCommentModal.value = true
      }
    }

    // 分享动态
    const sharePost = (postId) => {
      if (!userStore.user) {
        uiStore.openLoginModal()
        return
      }
      // 这里应该实现分享功能
      ElMessage.info('分享功能开发中...')
    }

    // 查看用户主页
    const viewProfile = (userId) => {
      router.push(`/profile/${userId}`)
    }

    // 获取排名样式
    const getRankClass = (rank) => {
      if (rank === 1) return 'rank-gold'
      if (rank === 2) return 'rank-silver'
      if (rank === 3) return 'rank-bronze'
      return 'rank-normal'
    }

    // 格式化时间
    const formatTime = (time) => {
      return dayjs(time).fromNow()
    }

    // 连接WebSocket
    const connectWebSocket = async () => {
      if (userStore.user && userStore.user.id) {
        try {
          await simpleWebSocketService.connect(userStore.user.id)
          
          // 添加通知事件监听器
          simpleWebSocketService.addEventListener('notification', notificationCallback)
        } catch (error) {
          console.error('WebSocket连接失败:', error)
          // 不显示错误提示，避免影响用户体验
        }
      }
    }

    // 处理下拉菜单命令
    const handleCommand = async (command) => {
      switch (command) {
        case 'profile':
          router.push(`/profile/${userStore.user.id}`)
          break
        case 'admin':
          router.push('/admin/notification')
          break
        case 'settings':
          ElMessage.info('设置功能开发中...')
          break
        case 'logout':
          await handleLogout()
          break
      }
    }

    // 退出登录
    const handleLogout = async () => {
      try {
        await ElMessageBox.confirm(
          '确定要退出登录吗？',
          '提示',
          {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning',
          }
        )
        
        // 移除事件监听器
        simpleWebSocketService.removeEventListener('notification', notificationCallback)
        // 断开WebSocket连接
        simpleWebSocketService.disconnect()
        
        // 清除用户信息
        userStore.clearUser()
        
        ElMessage.success('已退出登录')
        // router.push('/login')
      } catch (error) {
        // 用户取消退出
      }
    }

    // 处理分页变化
    const handlePageChange = (page) => {
      currentPage.value = page
      fetchPosts()
    }

    // 处理评论添加事件
    const handleCommentAdded = () => {
      // 更新动态的评论数
      if (currentPost.value) {
        currentPost.value.commentCount = (currentPost.value.commentCount || 0) + 1
      }
    }

    // 处理评论删除事件
    const handleCommentDeleted = () => {
      // 更新动态的评论数
      if (currentPost.value) {
        currentPost.value.commentCount = Math.max(0, (currentPost.value.commentCount || 0) - 1)
      }
    }

    // 判断是否可以删除动态
    const canDeletePost = (post) => {
      if (!userStore.user || !userStore.user.id) return false
      return post.userInfo?.id === userStore.user.id
    }

    // 删除动态
    const deletePost = async (postId) => {
      if (!userStore.user) {
        uiStore.openLoginModal()
        return
      }
      
      try {
        await ElMessageBox.confirm(
          '确定要删除这条动态吗？删除后无法恢复。',
          '提示',
          {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning',
          }
        )
        
        const response = await postApi.deletePost(postId, userStore.user.id)
        ElMessage.success(response.message || '动态删除成功')
        
        // 重新获取动态列表
        await fetchPosts()
      } catch (error) {
        if (error !== 'cancel') {
          ElMessage.error('删除失败')
        }
      }
    }

    onMounted(async () => {
      // 无论用户是否登录，都获取动态列表和排行榜
      fetchTopUsers()
      fetchPosts()

      // 等待用户状态初始化完成
      await nextTick()
      
      // 只有登录用户才获取通知和连接WebSocket
      if (userStore.user && userStore.user.id) {
        await fetchNotifications()
        connectWebSocket()
      }
    })

    // 监听用户状态变化
    watch(() => userStore.user, async (newUser) => {
      if (newUser && newUser.id) {
        // 用户登录后，获取通知和连接WebSocket
        await fetchNotifications()
        connectWebSocket()
      } else {
        // 用户退出后，清空通知数据
        notifications.value = []
        recentNotifications.value = []
        unreadCount.value = 0
        // 移除事件监听器
        simpleWebSocketService.removeEventListener('notification', notificationCallback)
        // 断开WebSocket连接
        simpleWebSocketService.disconnect()
      }
    }, { immediate: false })

    // 组件卸载时清理资源
    onUnmounted(() => {
      try {
        // 移除事件监听器
        simpleWebSocketService.removeEventListener('notification', notificationCallback)
        // 断开WebSocket连接
        simpleWebSocketService.disconnect()
      } catch (error) {
        console.error('组件卸载时清理WebSocket失败:', error)
      }
    })

    return {
      userStore,
      isAdmin,
      topUsers,
      posts,
      notifications,
      recentNotifications,
      showNotifications,
      postContent,
      publishing,
      unreadCount,
      currentPage,
      pageSize,
      totalPosts,
      showCommentModal,
      currentPost,
      handleCommand,
      publishPost,
      likePost,
      showComments,
      sharePost,
      viewProfile,
      getRankClass,
      formatTime,
      uiStore,
      handlePageChange,
      handleCommentAdded,
      handleCommentDeleted,
      canDeletePost,
      deletePost
    }
  }
}
</script>

<style scoped>
/* --- Global & Layout --- */
.home-container {
  min-height: 100vh;
  background-color: #f4f5f7;
}

.el-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px;
}

/* --- Header --- */
.header {
  background-color: #ffffff;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
  position: sticky;
  top: 0;
  z-index: 1000;
}

.header-content {
  max-width: 1200px;
  margin: 0 auto;
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 64px;
  padding: 0 24px;
}

.logo h2 {
  margin: 0;
  color: #303133;
  font-weight: 600;
  font-size: 22px;
}

.user-menu .user-info {
  display: flex;
  align-items: center;
  gap: 12px;
  cursor: pointer;
  padding: 8px;
  border-radius: 8px;
  transition: background-color 0.3s;
}

.user-menu .user-info:hover {
  background-color: #f5f5f5;
}

.user-info .username {
  font-weight: 500;
  color: #303133;
  font-size: 14px;
}

.auth-buttons .el-button {
  border-radius: 6px;
  font-weight: 500;
}

/* --- Sidebar --- */
.sidebar {
  margin-right: 24px;
}

.sidebar-card {
  background-color: #ffffff;
  border-radius: 8px;
  border: 1px solid #e6e6e6;
  box-shadow: none;
  margin-bottom: 24px;
}

.sidebar-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  border-bottom: 1px solid #f0f0f0;
}

.sidebar-header h3 {
  margin: 0;
  color: #303133;
  font-size: 16px;
  font-weight: 600;
}

/* --- Ranking List --- */
.ranking-list {
  padding: 8px 16px;
}

.ranking-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 4px;
  cursor: pointer;
  border-radius: 6px;
  transition: background-color 0.3s;
  border-bottom: 1px solid #f5f5f5;
}

.ranking-item:last-child {
  border-bottom: none;
}

.ranking-item:hover {
  background-color: #fafafa;
}

.rank-number {
  width: 24px;
  height: 24px;
  flex-shrink: 0;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: bold;
  font-size: 12px;
  color: #ffffff;
}

.rank-gold { background-color: #ffc107; }
.rank-silver { background-color: #c0c0c0; }
.rank-bronze { background-color: #cd7f32; }
.rank-normal { background-color: #909399; }

.ranking-item .user-info {
  flex-grow: 1;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.ranking-item .username {
  font-weight: 500;
  color: #303133;
  font-size: 14px;
}

.ranking-item .score {
  font-size: 13px;
  color: #606266;
  font-weight: 500;
}

/* --- Notification List --- */
.notification-list {
  padding: 8px 16px;
}

.notification-item {
  padding: 12px 4px;
  border-bottom: 1px solid #f0f0f0;
  cursor: pointer;
  transition: background-color 0.2s;
}

.notification-item:last-child {
  border-bottom: none;
}

.notification-item:hover {
  background-color: #fafafa;
}

.notification-item.unread .notification-content {
  font-weight: 600;
  color: #303133;
}

.notification-content {
  font-size: 14px;
  color: #606266;
  margin-bottom: 6px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.notification-time {
  font-size: 12px;
  color: #909399;
}

.login-prompt-sidebar {
  text-align: center;
  padding: 40px 20px;
  color: #909399;
}

.login-prompt-sidebar p {
  margin-top: 12px;
}

/* --- Main Content --- */
.main-content {
  padding: 0;
}

.el-card {
  border-radius: 8px;
  border: 1px solid #e6e6e6;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.05);
}

.post-card {
  margin-bottom: 24px;
}

.post-form {
  padding: 20px;
}

.post-form .el-textarea__inner {
  border-radius: 6px;
  font-size: 15px;
}

.post-actions {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}

/* --- Post Item --- */
.post-item {
  margin-bottom: 24px;
}

.post-user {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px 20px;
}

.post-user .username {
  font-weight: 600;
  color: #303133;
}

.post-user .post-time {
  font-size: 12px;
  color: #909399;
}

.post-user .delete-btn {
  margin-left: auto;
  color: #f56c6c;
  font-size: 12px;
}

.post-user .delete-btn:hover {
  color: #f56c6c;
  background-color: #fef0f0;
}

.post-content {
  font-size: 15px;
  line-height: 1.7;
  color: #303133;
  padding: 0 20px 16px;
  white-space: pre-wrap;
}

.post-item .post-actions {
  display: flex;
  gap: 16px;
  border-top: 1px solid #f0f0f0;
  padding: 12px 20px;
  background-color: #fafafa;
}

.post-item .post-actions .el-button {
  color: #606266;
  font-weight: 500;
  transition: color 0.2s;
}

.post-item .post-actions .el-button:hover {
  color: #409EFF;
}

.post-item .post-actions .el-button.liked {
  color: #409EFF;
  font-weight: 600;
}

/* --- Login Prompt --- */
.login-prompt-card {
  border-color: #d9ecff;
  background-color: #f4faff;
  margin-bottom: 24px;
}

.login-prompt {
  text-align: center;
  padding: 40px 20px;
}

.login-prompt h3 {
  margin: 16px 0 8px 0;
  color: #303133;
  font-size: 18px;
}

.login-prompt p {
  margin: 0 0 24px 0;
  color: #606266;
  font-size: 14px;
}

/* --- Responsive Design --- */
@media (max-width: 992px) {
  .el-container {
    padding: 16px;
  }
  .header-content {
    padding: 0 16px;
  }
}

@media (max-width: 768px) {
  .sidebar {
    display: none;
  }
  .el-container {
    padding: 12px;
  }
  .header-content {
    padding: 0 12px;
  }
  .post-item, .post-card, .login-prompt-card {
    margin-bottom: 16px;
  }
}

/* --- Comment Modal Styles --- */
:deep(.comment-modal .el-dialog__body) {
  padding: 20px;
}

:deep(.comment-modal .el-dialog__header) {
  padding: 20px 20px 0;
  border-bottom: 1px solid #f0f0f0;
}

:deep(.comment-modal .el-dialog__title) {
  font-weight: 600;
  color: #303133;
}
</style> 