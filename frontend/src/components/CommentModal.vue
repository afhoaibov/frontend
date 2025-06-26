<template>
  <el-dialog
    v-model="visible"
    :title="`评论 (${post?.userInfo?.nickname || post?.userInfo?.username}的动态)`"
    width="600px"
    :before-close="handleClose"
    class="comment-modal"
  >
    <!-- 动态内容预览 -->
    <div class="post-preview">
      <div class="post-user">
        <el-avatar :size="32" :src="post?.userInfo?.avatar">
          {{ post?.userInfo?.nickname ? post.userInfo.nickname.charAt(0) : post?.userInfo?.username?.charAt(0) }}
        </el-avatar>
        <div class="user-info">
          <div class="username">{{ post?.userInfo?.nickname || post?.userInfo?.username }}</div>
          <div class="post-time">{{ formatTime(post?.createdAt) }}</div>
        </div>
      </div>
      <div class="post-content">{{ post?.content }}</div>
    </div>

    <!-- 发表评论 -->
    <div class="comment-form">
      <el-input
        v-model="commentContent"
        type="textarea"
        :rows="3"
        placeholder="写下你的评论..."
        maxlength="200"
        show-word-limit
        @keydown.ctrl.enter="submitComment"
      />
      <div class="form-actions">
        <el-button type="primary" @click="submitComment" :loading="submitting">
          发表评论
        </el-button>
        <span class="tip">Ctrl + Enter 快速发表</span>
      </div>
    </div>

    <!-- 评论列表 -->
    <div class="comments-section">
      <div class="comments-header">
        <h4>评论 ({{ totalComments }})</h4>
      </div>

      <div v-if="comments.length === 0" class="no-comments">
        <el-icon size="48" color="#c0c4cc"><ChatDotRound /></el-icon>
        <p>还没有评论，快来发表第一条评论吧！</p>
      </div>

      <div v-else class="comments-list">
        <div 
          v-for="comment in comments" 
          :key="comment.id" 
          class="comment-item"
        >
          <div class="comment-user">
            <el-avatar :size="32" :src="comment.userInfo?.avatar">
              {{ comment.userInfo?.nickname ? comment.userInfo.nickname.charAt(0) : comment.userInfo?.username?.charAt(0) }}
            </el-avatar>
            <div class="user-info">
              <div class="username">{{ comment.userInfo?.nickname || comment.userInfo?.username }}</div>
              <div class="comment-time">{{ formatTime(comment.createdAt) }}</div>
            </div>
            <div class="comment-actions">
              <!-- 点赞按钮 -->
              <el-button 
                type="text" 
                size="small"
                @click="likeComment(comment.id)"
                :class="{ liked: comment.isLiked }"
              >
                👍
                {{ comment.likeCount || 0 }}
              </el-button>
              
              <!-- 删除按钮 - 只有评论作者或动态作者可以删除 -->
              <el-button 
                v-if="canDeleteComment(comment)"
                type="text" 
                size="small"
                @click="deleteComment(comment.id)"
                class="delete-btn"
              >
                <el-icon><Delete /></el-icon>
                删除
              </el-button>
            </div>
          </div>
          
          <div class="comment-content">
            {{ comment.content }}
          </div>
        </div>
      </div>

      <!-- 分页 -->
      <el-pagination
        v-if="totalComments > pageSize"
        background
        layout="prev, pager, next, total"
        :total="totalComments"
        :page-size="pageSize"
        :current-page="currentPage"
        @current-change="handlePageChange"
        class="pagination"
      />
    </div>
  </el-dialog>
</template>

<script>
import { ref, watch, computed } from 'vue'
import { postApi } from '@/utils/api'
import { useUserStore } from '@/stores/user'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ChatDotRound, Delete } from '@element-plus/icons-vue'
import dayjs from 'dayjs'
import relativeTime from 'dayjs/plugin/relativeTime'
import 'dayjs/locale/zh-cn'

// 配置dayjs插件和语言
dayjs.extend(relativeTime)
dayjs.locale('zh-cn')

export default {
  name: 'CommentModal',
  props: {
    modelValue: {
      type: Boolean,
      default: false
    },
    post: {
      type: Object,
      default: null
    }
  },
  emits: ['update:modelValue', 'comment-added', 'comment-deleted'],
  setup(props, { emit }) {
    const userStore = useUserStore()
    
    const visible = ref(false)
    const comments = ref([])
    const commentContent = ref('')
    const submitting = ref(false)
    const loading = ref(false)
    const currentPage = ref(1)
    const pageSize = ref(3)
    const totalComments = ref(0)

    // 监听modelValue变化
    watch(() => props.modelValue, (newVal) => {
      visible.value = newVal
      if (newVal && props.post) {
        loadComments()
      }
    })

    // 监听visible变化
    watch(visible, (newVal) => {
      emit('update:modelValue', newVal)
      if (!newVal) {
        // 关闭时重置状态
        comments.value = []
        commentContent.value = ''
        currentPage.value = 1
        totalComments.value = 0
      }
    })

    // 判断是否可以删除评论
    const canDeleteComment = (comment) => {
      if (!userStore.user) return false
      // 只有评论作者可以删除自己的评论
      return comment.userId === userStore.user.id
    }

    // 加载评论列表
    const loadComments = async () => {
      if (!props.post?.id) return
      
      loading.value = true
      try {
        const response = await postApi.getComments(props.post.id, currentPage.value - 1, pageSize.value)
        comments.value = response.data || []
        totalComments.value = response.totalElements || 0
        
        // 设置用户点赞状态和初始化点赞数
        if (userStore.user) {
          comments.value.forEach(comment => {
            comment.isLiked = false // 默认未点赞，实际应该从后端获取
            // 确保点赞数有初始值
            if (comment.likeCount === undefined || comment.likeCount === null) {
              comment.likeCount = 0
            }
          })
        } else {
          // 未登录用户，也要初始化点赞数
          comments.value.forEach(comment => {
            if (comment.likeCount === undefined || comment.likeCount === null) {
              comment.likeCount = 0
            }
          })
        }
        
        console.log('加载的评论数据:', comments.value)
      } catch (error) {
        console.error('获取评论失败:', error)
        ElMessage.error('获取评论失败')
      } finally {
        loading.value = false
      }
    }

    // 处理分页变化
    const handlePageChange = (page) => {
      currentPage.value = page
      loadComments()
    }

    // 发表评论
    const submitComment = async () => {
      if (!userStore.user) {
        ElMessage.warning('请先登录')
        return
      }
      
      if (!commentContent.value.trim()) {
        ElMessage.warning('请输入评论内容')
        return
      }
      
      submitting.value = true
      try {
        const response = await postApi.createComment(props.post.id, {
          userId: userStore.user.id,
          content: commentContent.value
        })
        
        ElMessage.success('评论发表成功')
        commentContent.value = ''
        
        // 重新加载评论列表
        await loadComments()
        
        // 通知父组件评论已添加
        emit('comment-added')
      } catch (error) {
        ElMessage.error('评论发表失败')
      } finally {
        submitting.value = false
      }
    }

    // 点赞评论
    const likeComment = async (commentId) => {
      if (!userStore.user) {
        ElMessage.warning('请先登录')
        return
      }
      
      try {
        const comment = comments.value.find(c => c.id === commentId)
        if (!comment) return
        
        let response
        if (comment.isLiked) {
          // 取消点赞
          response = await postApi.unlikeComment(props.post.id, commentId, userStore.user.id)
          comment.isLiked = false
          ElMessage.success('取消点赞成功')
        } else {
          // 点赞
          response = await postApi.likeComment(props.post.id, commentId, userStore.user.id)
          comment.isLiked = true
          ElMessage.success('点赞成功')
        }
        
        // 更新点赞数 - 根据后端返回的数据结构更新
        if (response && response.likeCount !== undefined) {
          comment.likeCount = response.likeCount
        } else if (response && response.data && response.data.likeCount !== undefined) {
          comment.likeCount = response.data.likeCount
        } else {
          // 如果后端没有返回点赞数，手动更新
          comment.likeCount = comment.likeCount || 0
          if (comment.isLiked) {
            comment.likeCount++
          } else {
            comment.likeCount = Math.max(0, comment.likeCount - 1)
          }
        }
        
        console.log('评论点赞响应:', response)
        console.log('更新后的点赞数:', comment.likeCount)
      } catch (error) {
        console.error('评论点赞失败:', error)
        ElMessage.error('操作失败')
      }
    }

    // 删除评论
    const deleteComment = async (commentId) => {
      try {
        await ElMessageBox.confirm(
          '确定要删除这条评论吗？',
          '提示',
          {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning',
          }
        )
        
        await postApi.deleteComment(props.post.id, commentId, userStore.user.id)
        ElMessage.success('评论删除成功')
        
        // 重新加载评论列表
        await loadComments()
        
        // 通知父组件评论已删除
        emit('comment-deleted')
      } catch (error) {
        if (error !== 'cancel') {
          ElMessage.error('删除失败')
        }
      }
    }

    // 格式化时间
    const formatTime = (time) => {
      return dayjs(time).fromNow()
    }

    // 关闭对话框
    const handleClose = () => {
      visible.value = false
    }

    return {
      visible,
      comments,
      commentContent,
      submitting,
      loading,
      currentPage,
      pageSize,
      totalComments,
      userStore,
      canDeleteComment,
      loadComments,
      handlePageChange,
      submitComment,
      likeComment,
      deleteComment,
      formatTime,
      handleClose
    }
  }
}
</script>

<style scoped>
.comment-modal {
  border-radius: 8px;
}

/* 动态预览 */
.post-preview {
  background-color: #f8f9fa;
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 20px;
  border: 1px solid #e9ecef;
}

.post-user {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}

.post-user .username {
  font-weight: 600;
  color: #303133;
  font-size: 14px;
}

.post-user .post-time {
  font-size: 12px;
  color: #909399;
}

.post-content {
  font-size: 14px;
  line-height: 1.6;
  color: #303133;
  white-space: pre-wrap;
}

/* 评论表单 */
.comment-form {
  margin-bottom: 24px;
}

.form-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 12px;
}

.tip {
  font-size: 12px;
  color: #909399;
}

/* 评论区域 */
.comments-section {
  max-height: 500px;
  overflow-y: auto;
}

.comments-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding-bottom: 8px;
  border-bottom: 1px solid #f0f0f0;
  position: sticky;
  top: 0;
  background-color: #fff;
  z-index: 10;
}

.comments-header h4 {
  margin: 0;
  color: #303133;
  font-size: 16px;
  font-weight: 600;
}

.no-comments {
  text-align: center;
  padding: 40px 20px;
  color: #909399;
}

.no-comments p {
  margin: 12px 0 0 0;
  font-size: 14px;
}

/* 评论列表 */
.comments-list {
  margin-bottom: 20px;
}

.comment-item {
  padding: 16px 0;
  border-bottom: 1px solid #f0f0f0;
}

.comment-item:last-child {
  border-bottom: none;
}

.comment-user {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 8px;
}

.comment-user .user-info {
  flex-grow: 1;
}

.comment-user .username {
  font-weight: 600;
  color: #303133;
  font-size: 14px;
}

.comment-user .comment-time {
  font-size: 12px;
  color: #909399;
}

.comment-actions {
  display: flex;
  gap: 8px;
}

.comment-actions .el-button {
  color: #606266;
  font-size: 12px;
}

.comment-actions .el-button:hover {
  color: #409EFF;
}

.comment-actions .el-button.liked {
  color: #409EFF;
  font-weight: 600;
}

.comment-actions .delete-btn {
  color: #f56c6c;
}

.comment-actions .delete-btn:hover {
  color: #f56c6c;
  background-color: #fef0f0;
}

.comment-content {
  font-size: 14px;
  line-height: 1.6;
  color: #303133;
  white-space: pre-wrap;
  margin-left: 44px;
}

/* 分页 */
.pagination {
  margin-top: 20px;
  text-align: center;
  padding: 16px 0;
  border-top: 1px solid #f0f0f0;
  background-color: #fff;
  position: sticky;
  bottom: 0;
}

/* 滚动条样式 */
.comments-section::-webkit-scrollbar {
  width: 6px;
}

.comments-section::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 3px;
}

.comments-section::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 3px;
}

.comments-section::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}
</style> 