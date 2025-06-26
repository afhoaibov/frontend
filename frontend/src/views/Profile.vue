<template>
  <div class="profile-container" v-if="userStore">
    <!-- 加载状态 -->
    <div v-if="loading" class="loading-container">
      <el-skeleton :rows="10" animated />
    </div>

    <!-- 主要内容 -->
    <div v-else>
      <el-card class="profile-card">
        <div class="profile-header">
          <div class="profile-avatar">
            <el-avatar :size="120" :src="userInfo?.avatar">
              {{ userInfo?.nickname ? userInfo?.nickname.charAt(0) : userInfo?.username?.charAt(0) }}
            </el-avatar>
          </div>
          
          <div class="profile-info">
            <div class="user-header">
              <h1>{{ userInfo?.nickname || userInfo?.username }}</h1>
              <div v-if="userInfo?.role === 'ADMIN'" class="admin-badge">
                <el-tag type="danger" size="small">管理员</el-tag>
              </div>
            </div>
            <p class="bio">{{ userInfo?.bio || '这个人很懒，什么都没写~' }}</p>
            
            <div class="user-details">
              <div class="detail-item">
                <el-icon><User /></el-icon>
                <span>用户名: {{ userInfo?.username }}</span>
              </div>
              <div class="detail-item">
                <el-icon><Calendar /></el-icon>
                <span>注册时间: {{ formatDate(userInfo?.createdAt) }}</span>
              </div>
              <div v-if="userInfo?.email" class="detail-item">
                <el-icon><Message /></el-icon>
                <span>邮箱: {{ userInfo?.email }}</span>
              </div>
            </div>
            
            <div class="stats">
              <!-- <div class="stat-item">
                <div class="stat-number">{{ userInfo?.followerCount || 0 }}</div>
                <div class="stat-label">粉丝</div>
              </div>
              <div class="stat-item">
                <div class="stat-number">{{ userInfo?.followingCount || 0 }}</div>
                <div class="stat-label">关注</div>
              </div> -->
              <div class="stat-item">
                <div class="stat-number">{{ userInfo?.postCount || 0 }}</div>
                <div class="stat-label">动态</div>
              </div>
              <div class="stat-item">
                <div class="stat-number">{{ userInfo?.score || 0 }}</div>
                <div class="stat-label">分数</div>
              </div>
            </div>
            
            <div class="actions">
              <!-- 暂时隐藏关注按钮 -->
              <!-- <el-button 
                v-if="!isCurrentUser && userStore.user" 
                type="primary" 
                @click="followUser"
                :loading="following"
              >
                {{ isFollowing ? '取消关注' : '关注' }}
              </el-button>
              <el-button 
                v-else-if="!isCurrentUser && !userStore.user" 
                type="primary" 
                @click="$router.push('/login')"
              >
                登录后关注
              </el-button> -->
              <el-button v-if="isCurrentUser" @click="showEditModal = true">
                编辑资料
              </el-button>
              <el-button v-if="isCurrentUser" @click="handleSettings">
                设置
              </el-button>
            </div>
          </div>
        </div>
      </el-card>

      <!-- 排行榜信息 -->
      <el-card class="ranking-card" style="margin-top: 20px;" v-if="userStore?.user && userStore?.user?.id">
        <template #header>
          <div class="card-header">
            <h3>排行榜信息</h3>
            <el-button type="text" @click="$router.push('/ranking')">
              查看完整排行榜
            </el-button>
          </div>
        </template>
        
        <div class="ranking-info">
          <div class="ranking-item">
            <span class="label">综合排名:</span>
            <span class="value" :class="getRankClass(userRank.score)">
              {{ userRank.score ? `第${userRank.score}名` : '暂无' }}
            </span>
          </div>
          <!-- <div class="ranking-item">
            <span class="label">粉丝排名:</span>
            <span class="value" :class="getRankClass(userRank.followers)">
              {{ userRank.followers ? `第${userRank.followers}名` : '暂无' }}
            </span>
          </div>
          <div class="ranking-item">
            <span class="label">动态排名:</span>
            <span class="value" :class="getRankClass(userRank.posts)">
              {{ userRank.posts ? `第${userRank.posts}名` : '暂无' }}
            </span>
          </div> -->
        </div>
      </el-card>

      <!-- 动态列表 -->
      <el-card class="posts-card" style="margin-top: 20px;">
        <template #header>
          <div class="card-header">
            <h3>动态列表</h3>
            <div class="header-actions">
              <el-button 
                v-if="isCurrentUser" 
                type="primary" 
                size="small"
                @click="$router.push('/')"
              >
                发布新动态
              </el-button>
            </div>
          </div>
        </template>
        
        <div class="posts-list">
          <div 
            v-for="post in posts" 
            :key="post.id" 
            class="post-item"
          >
            <div class="post-header">
              <div class="post-user">
                <el-avatar :size="32" :src="post.userInfo?.avatar">
                  {{ post.userInfo?.nickname ? post.userInfo?.nickname.charAt(0) : post.userInfo?.username?.charAt(0) }}
                </el-avatar>
                <div class="user-info">
                  <div class="username">{{ post.userInfo?.nickname || post.userInfo?.username }}</div>
                  <div class="post-time">{{ formatTime(post.createdAt) }}</div>
                </div>
              </div>
              <!-- 删除按钮 -->
              <el-button 
                v-if="canDeletePost(post)"
                type="text" 
                size="small"
                @click="deletePost(post.id)"
                class="delete-btn"
              >
                <el-icon><Delete /></el-icon>
              </el-button>
            </div>
            
            <div class="post-content">{{ post.content }}</div>
            
            <div class="post-actions">
              <el-button 
                type="text" 
                @click="likePost(post.id)"
                :class="{ liked: post.isLiked }"
              >
                👍 {{ post.likeCount || 0 }}
              </el-button>
              <el-button 
                type="text" 
                @click="showComments(post.id)"
              >
                <el-icon><ChatDotRound /></el-icon>
                {{ post.commentCount || 0 }}
              </el-button>
            </div>
          </div>
          
          <div v-if="posts.length === 0" class="empty-posts">
            <el-empty description="暂无动态" />
            <el-button v-if="isCurrentUser" type="primary" @click="$router.push('/')">
              发布第一条动态
            </el-button>
          </div>
        </div>
      </el-card>
    </div>

    <!-- 编辑资料模态框 -->
    <el-dialog
      v-model="showEditModal"
      title="编辑个人资料"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form :model="editForm" :rules="editRules" ref="editFormRef" label-width="80px">
        <el-form-item label="昵称" prop="nickname">
          <el-input v-model="editForm.nickname" placeholder="请输入昵称" />
        </el-form-item>
        <el-form-item label="个人简介" prop="bio">
          <el-input 
            v-model="editForm.bio" 
            type="textarea" 
            :rows="3"
            placeholder="介绍一下自己吧..."
            maxlength="200"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="editForm.email" placeholder="请输入邮箱" />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="showEditModal = false">取消</el-button>
        <el-button type="primary" @click="saveProfile" :loading="saving">
          保存
        </el-button>
      </template>
    </el-dialog>

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
import { ref, onMounted, computed, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { userApi, rankingApi, postApi } from '@/utils/api'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  User,
  Calendar,
  Message,
  Delete,
  ChatDotRound,
} from '@element-plus/icons-vue'
import dayjs from 'dayjs'
import relativeTime from 'dayjs/plugin/relativeTime'
import 'dayjs/locale/zh-cn'
import CommentModal from '@/components/CommentModal.vue'

// 配置dayjs插件和语言
dayjs.extend(relativeTime)
dayjs.locale('zh-cn')

export default {
  name: 'Profile',
  components: {
    User,
    Calendar,
    Message,
    Delete,
    ChatDotRound,
    CommentModal
  },
  setup() {
    const route = useRoute()
    const router = useRouter()
    const userStore = useUserStore()
    
    // 确保userStore存在
    if (!userStore) {
      console.error('userStore未初始化')
      return {}
    }
    
    // 响应式数据
    const userInfo = ref({})
    const posts = ref([])
    const userRank = ref({})
    const loading = ref(false)
    const saving = ref(false)
    
    // 模态框状态
    const showEditModal = ref(false)
    const showCommentModal = ref(false)
    
    // 编辑表单
    const editFormRef = ref(null)
    const editForm = ref({
      nickname: '',
      bio: '',
      email: ''
    })
    
    // 评论相关
    const currentPost = ref(null)

    // 判断是否为当前用户
    const isCurrentUser = computed(() => {
      const userId = route.params.id
      return !userId || (userStore?.user && userId == userStore?.user?.id)
    })

    // 表单验证规则
    const editRules = {
      nickname: [
        { required: true, message: '请输入昵称', trigger: 'blur' },
        { min: 2, max: 20, message: '昵称长度在 2 到 20 个字符', trigger: 'blur' }
      ],
      email: [
        { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
      ]
    }

    // 获取用户信息
    const fetchUserInfo = async () => {
      loading.value = true
      try {
        let userId = route.params.id
        if (!userId) {
          if (userStore?.user?.id) {
            userId = userStore?.user?.id
          } else {
            router.push('/login')
            return
          }
        }
        
        const response = await userApi.getUserInfo(userId)
        // 处理API返回的数据结构 { "user": { ... } }
        userInfo.value = response.user || response
        
        // 初始化编辑表单
        editForm.value = {
          nickname: userInfo.value.nickname || '',
          bio: userInfo.value.bio || '',
          email: userInfo.value.email || ''
        }
        
        // 检查关注状态
        if (userStore?.user && userStore?.user?.id !== userId) {
          // await checkFollowStatus(userId)
        }
      } catch (error) {
        ElMessage.error('获取用户信息失败')
        console.error('获取用户信息失败:', error)
      } finally {
        loading.value = false
      }
    }

    // 获取用户动态列表
    const fetchUserPosts = async () => {
      try {
        const userId = route.params.id || userStore?.user?.id
        if (!userId) return
        
        const response = await postApi.getUserPosts(userId, 0, 20)
        // 处理API返回的数据结构，可能是 { "posts": [...] } 或直接是数组
        posts.value = response.posts || response.data || response || []
        
        // 初始化动态数据
        posts.value.forEach(post => {
          post.likeCount = post.likeCount || 0
          post.commentCount = post.commentCount || 0
          post.isLiked = false
        })
      } catch (error) {
        console.error('获取用户动态失败:', error)
      }
    }

    // 获取用户排名
    const fetchUserRank = async () => {
      try {
        const userId = route.params.id || userStore?.user?.id
        if (!userId) return
        
        const [scoreRank, followersRank, postsRank] = await Promise.all([
          rankingApi.getUserRank(userId, 'score'),
          rankingApi.getUserRank(userId, 'followers'),
          rankingApi.getUserRank(userId, 'posts')
        ])
        
        // 处理API返回的数据结构，可能是 { "rank": number } 或直接是数字
        userRank.value = {
          score: scoreRank.rank || scoreRank,
          followers: followersRank.rank || followersRank,
          posts: postsRank.rank || postsRank
        }
      } catch (error) {
        console.error('获取用户排名失败:', error)
      }
    }

    // 检查关注状态
    const checkFollowStatus = async (userId) => {
      // 暂时禁用关注功能
      // isFollowing.value = false
    }

    // 关注用户
    const followUser = async () => {
      ElMessage.info('关注功能暂时不可用，敬请期待...')
    }

    // 保存个人资料
    const saveProfile = async () => {
      if (!editFormRef.value) return
      
      try {
        await editFormRef.value.validate()
        saving.value = true
        
        const response = await userApi.updateUserInfo(userStore?.user?.id, editForm.value)
        ElMessage.success(response.message || '保存成功')
        
        // 更新用户信息
        Object.assign(userInfo.value, editForm.value)
        showEditModal.value = false
        
        // 如果是当前用户，更新store中的用户信息
        if (isCurrentUser.value) {
          userStore.updateUserInfo(editForm.value)
        }
      } catch (error) {
        if (error !== 'cancel') {
          ElMessage.error('保存失败')
        }
      } finally {
        saving.value = false
      }
    }

    // 获取粉丝列表
    const fetchFollowers = async () => {
      // 暂时禁用粉丝功能
      // followers.value = []
      ElMessage.info('粉丝功能暂时不可用，敬请期待...')
    }

    // 获取关注列表
    const fetchFollowing = async () => {
      // 暂时禁用关注功能
      // followingList.value = []
      ElMessage.info('关注功能暂时不可用，敬请期待...')
    }

    // 查看用户主页
    const viewProfile = (userId) => {
      router.push(`/profile/${userId}`)
    }

    // 点赞动态
    const likePost = async (postId) => {
      if (!userStore?.user) {
        ElMessage.warning('请先登录')
        return
      }
      
      try {
        const post = posts.value.find(p => p.id === postId)
        if (!post) return
        
        let response
        if (post.isLiked) {
          response = await postApi.unlikePost(postId, userStore?.user?.id)
          post.isLiked = false
        } else {
          response = await postApi.likePost(postId, userStore?.user?.id)
          post.isLiked = true
        }
        
        // 处理API返回的数据结构
        if (response && (response.likeCount !== undefined || response.data?.likeCount !== undefined)) {
          post.likeCount = response.likeCount || response.data?.likeCount || 0
        } else {
          post.likeCount = post.likeCount || 0
          if (post.isLiked) {
            post.likeCount++
          } else {
            post.likeCount = Math.max(0, post.likeCount - 1)
          }
        }
      } catch (error) {
        ElMessage.error('操作失败')
      }
    }

    // 查看评论
    const showComments = (postId) => {
      if (!userStore?.user) {
        ElMessage.warning('请先登录')
        return
      }
      
      const post = posts.value.find(p => p.id === postId)
      if (post) {
        currentPost.value = post
        showCommentModal.value = true
      }
    }

    // 删除动态
    const deletePost = async (postId) => {
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
        
        const response = await postApi.deletePost(postId, userStore?.user?.id)
        ElMessage.success(response.message || '动态删除成功')
        
        // 从列表中移除
        const index = posts.value.findIndex(p => p.id === postId)
        if (index > -1) {
          posts.value.splice(index, 1)
        }
      } catch (error) {
        if (error !== 'cancel') {
          ElMessage.error('删除失败')
        }
      }
    }

    // 判断是否可以删除动态
    const canDeletePost = (post) => {
      if (!userStore?.user || !userStore?.user?.id) return false
      return post.userInfo?.id === userStore?.user?.id
    }

    // 处理评论添加事件
    const handleCommentAdded = () => {
      if (currentPost.value) {
        currentPost.value.commentCount = (currentPost.value.commentCount || 0) + 1
      }
    }

    // 处理评论删除事件
    const handleCommentDeleted = () => {
      if (currentPost.value) {
        currentPost.value.commentCount = Math.max(0, (currentPost.value.commentCount || 0) - 1)
      }
    }

    // 获取排名样式
    const getRankClass = (rank) => {
      if (!rank) return ''
      if (rank <= 3) return 'rank-top'
      if (rank <= 10) return 'rank-good'
      return 'rank-normal'
    }

    // 格式化时间
    const formatTime = (time) => {
      return dayjs(time).fromNow()
    }

    // 格式化日期
    const formatDate = (date) => {
      return dayjs(date).format('YYYY年MM月DD日')
    }

    // 显示设置
    const handleSettings = () => {
      ElMessage.info('设置功能开发中，敬请期待...')
    }

    onMounted(async () => {
      try {
        await fetchUserInfo()
        await fetchUserPosts()
        
        if (userStore?.user) {
          await fetchUserRank()
        }
      } catch (error) {
        console.error('页面初始化失败:', error)
        ElMessage.error('页面加载失败，请刷新重试')
      }
    })

    return {
      userStore,
      userInfo,
      posts,
      userRank,
      loading,
      saving,
      isCurrentUser,
      showEditModal,
      showCommentModal,
      editFormRef,
      editForm,
      editRules,
      currentPost,
      saveProfile,
      fetchFollowers,
      viewProfile,
      likePost,
      showComments,
      deletePost,
      canDeletePost,
      handleCommentAdded,
      handleCommentDeleted,
      getRankClass,
      formatTime,
      formatDate,
      handleSettings
    }
  }
}
</script>

<style scoped>
.profile-container {
  max-width: 800px;
  margin: 20px auto;
  padding: 0 20px;
}

.loading-container {
  padding: 40px;
}

.profile-card {
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.profile-header {
  display: flex;
  gap: 24px;
  align-items: flex-start;
}

.profile-avatar {
  flex-shrink: 0;
  position: relative;
}

.avatar-edit {
  position: absolute;
  bottom: 0;
  right: 0;
  background: rgba(255, 255, 255, 0.9);
  border-radius: 50%;
  padding: 2px;
}

.user-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 8px;
}

.profile-info h1 {
  margin: 0;
  color: #303133;
  font-size: 28px;
  font-weight: 600;
}

.admin-badge {
  margin-left: 8px;
}

.bio {
  color: #606266;
  font-size: 16px;
  margin: 0 0 16px 0;
  line-height: 1.5;
}

.user-details {
  margin-bottom: 20px;
}

.detail-item {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
  color: #606266;
  font-size: 14px;
}

.stats {
  display: flex;
  gap: 32px;
  margin-bottom: 24px;
}

.stat-item {
  text-align: center;
  transition: all 0.3s ease;
  padding: 8px;
  border-radius: 8px;
}

.stat-item:hover {
  background-color: #f5f7fa;
}

.stat-number {
  font-size: 24px;
  font-weight: 600;
  color: #409eff;
  margin-bottom: 4px;
  transition: color 0.3s;
}

.stat-item:hover .stat-number {
  color: #66b1ff;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  transition: color 0.3s;
}

.stat-item:hover .stat-label {
  color: #606266;
}

.actions {
  display: flex;
  gap: 12px;
}

.ranking-card, .posts-card {
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header h3 {
  margin: 0;
  color: #303133;
  font-size: 18px;
  font-weight: 600;
}

.header-actions {
  display: flex;
  gap: 8px;
}

.ranking-info {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.ranking-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 0;
  border-bottom: 1px solid #f0f0f0;
}

.ranking-item:last-child {
  border-bottom: none;
}

.label {
  color: #606266;
  font-size: 14px;
}

.value {
  font-weight: 600;
  font-size: 16px;
}

.value.rank-top {
  color: #ffc107;
  font-weight: 700;
}

.value.rank-good {
  color: #67c23a;
  font-weight: 600;
}

.value.rank-normal {
  color: #409eff;
  font-weight: 500;
}

.posts-list {
  max-height: 600px;
  overflow-y: auto;
}

.post-item {
  padding: 16px 0;
  border-bottom: 1px solid #f0f0f0;
  transition: background-color 0.2s;
}

.post-item:hover {
  background-color: #fafafa;
}

.post-item:last-child {
  border-bottom: none;
}

.post-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 12px;
}

.post-user {
  display: flex;
  align-items: center;
  gap: 12px;
}

.user-info .username {
  font-weight: 600;
  color: #303133;
  font-size: 14px;
  transition: color 0.2s;
}

.user-info .username:hover {
  color: #409eff;
  cursor: pointer;
}

.user-info .post-time {
  font-size: 12px;
  color: #909399;
}

.delete-btn {
  color: #f56c6c;
  font-size: 12px;
  opacity: 0;
  transition: opacity 0.2s;
}

.post-item:hover .delete-btn {
  opacity: 1;
}

.delete-btn:hover {
  color: #f56c6c;
  background-color: #fef0f0;
}

.post-content {
  font-size: 16px;
  line-height: 1.6;
  color: #303133;
  margin-bottom: 12px;
  white-space: pre-wrap;
  word-break: break-word;
}

.post-actions {
  display: flex;
  gap: 16px;
}

.post-actions .el-button {
  color: #606266;
  font-weight: 500;
  transition: all 0.2s;
  border-radius: 6px;
  padding: 6px 12px;
}

.post-actions .el-button:hover {
  color: #409EFF;
  background-color: #f0f9ff;
}

.post-actions .el-button.liked {
  color: #409EFF;
  font-weight: 600;
  background-color: #f0f9ff;
}

.empty-posts {
  padding: 40px 0;
  text-align: center;
}

/* 响应式设计改进 */
@media (max-width: 768px) {
  .profile-container {
    padding: 0 10px;
  }
  
  .profile-header {
    flex-direction: column;
    text-align: center;
  }
  
  .stats {
    justify-content: center;
    gap: 20px;
  }
  
  .actions {
    justify-content: center;
    flex-wrap: wrap;
  }
  
  .card-header {
    flex-direction: column;
    gap: 12px;
    align-items: flex-start;
  }
  
  .post-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }
  
  .post-actions {
    justify-content: flex-start;
  }
  
  .follower-item, .following-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }
  
  .follower-info, .following-info {
    width: 100%;
  }
}
</style> 