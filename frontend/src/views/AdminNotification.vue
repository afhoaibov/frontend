<template>
  <div class="admin-notification-container">
    <el-header class="header">
      <div class="header-content">
        <div class="logo">
          <h2>管理员通知中心</h2>
        </div>
        <div class="user-menu">
          <el-button @click="$router.push('/')" type="text">
            <el-icon><Back /></el-icon>
            返回首页
          </el-button>
        </div>
      </div>
    </el-header>

    <el-container>
      <el-main class="main-content">
        <!-- 发布通知表单 -->
        <el-card class="notification-form-card">
          <template #header>
            <div class="card-header">
              <h3>发布系统通知</h3>
            </div>
          </template>
          
          <el-form :model="notificationForm" :rules="rules" ref="notificationFormRef" label-width="100px">
            <el-form-item label="通知类型" prop="type">
              <el-select v-model="notificationForm.type" placeholder="选择通知类型">
                <el-option label="系统公告" value="SYSTEM"></el-option>
                <el-option label="活动通知" value="ACTIVITY"></el-option>
                <el-option label="维护通知" value="MAINTENANCE"></el-option>
                <el-option label="其他" value="OTHER"></el-option>
              </el-select>
            </el-form-item>
            
            <el-form-item label="通知标题" prop="title">
              <el-input v-model="notificationForm.title" placeholder="请输入通知标题" maxlength="100" show-word-limit />
            </el-form-item>
            
            <el-form-item label="通知内容" prop="content">
              <el-input 
                v-model="notificationForm.content" 
                type="textarea" 
                :rows="6" 
                placeholder="请输入通知内容..." 
                maxlength="1000" 
                show-word-limit 
              />
            </el-form-item>
            
            <el-form-item label="发送范围" prop="targetType">
              <el-radio-group v-model="notificationForm.targetType">
                <el-radio label="ALL">所有用户</el-radio>
                <el-radio label="SPECIFIC">指定用户</el-radio>
              </el-radio-group>
            </el-form-item>
            
            <el-form-item v-if="notificationForm.targetType === 'SPECIFIC'" label="目标用户" prop="targetUserIds">
              <el-select 
                v-model="notificationForm.targetUserIds" 
                multiple 
                filterable 
                placeholder="选择目标用户"
                style="width: 100%"
              >
                <el-option 
                  v-for="user in allUsers" 
                  :key="user.id" 
                  :label="user.nickname || user.username" 
                  :value="user.id"
                >
                  <span>{{ user.nickname || user.username }}</span>
                  <span style="float: right; color: #8492a6; font-size: 13px">{{ user.username }}</span>
                </el-option>
              </el-select>
            </el-form-item>
            
            <el-form-item>
              <el-button type="primary" @click="publishNotification" :loading="publishing">
                发布通知
              </el-button>
              <el-button @click="resetForm">重置</el-button>
            </el-form-item>
          </el-form>
        </el-card>

        <!-- 通知列表 -->
        <el-card class="notification-list-card" style="margin-top: 20px;">
          <template #header>
            <div class="card-header">
              <h3>通知历史</h3>
              <el-button type="primary" @click="refreshNotifications">
                <el-icon><Refresh /></el-icon>
                刷新
              </el-button>
            </div>
          </template>
          
          <el-table :data="notifications" style="width: 100%">
            <el-table-column prop="type" label="类型" width="120">
              <template #default="scope">
                <el-tag :type="getTypeTagType(scope.row.type)">
                  {{ getTypeLabel(scope.row.type) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="title" label="标题" />
            <el-table-column prop="content" label="内容" show-overflow-tooltip />
            <el-table-column prop="targetType" label="范围" width="100">
              <template #default="scope">
                {{ scope.row.targetType === 'ALL' ? '所有用户' : '指定用户' }}
              </template>
            </el-table-column>
            <el-table-column prop="createdAt" label="发布时间" width="180">
              <template #default="scope">
                {{ formatTime(scope.row.createdAt) }}
              </template>
            </el-table-column>
            <el-table-column label="操作" width="120">
              <template #default="scope">
                <el-button type="text" @click="viewNotification(scope.row)">
                  查看
                </el-button>
                <el-button type="text" @click="deleteNotification(scope.row.id)" style="color: #f56c6c;">
                  删除
                </el-button>
              </template>
            </el-table-column>
          </el-table>
          
          <div class="pagination-container">
            <el-pagination
              v-model:current-page="currentPage"
              v-model:page-size="pageSize"
              :page-sizes="[10, 20, 50, 100]"
              :total="total"
              layout="total, sizes, prev, pager, next, jumper"
              @size-change="handleSizeChange"
              @current-change="handleCurrentChange"
            />
          </div>
        </el-card>
      </el-main>
    </el-container>

    <!-- 通知详情对话框 -->
    <el-dialog v-model="showNotificationDetail" title="通知详情" width="600px">
      <div v-if="selectedNotification" class="notification-detail">
        <div class="detail-item">
          <label>类型：</label>
          <el-tag :type="getTypeTagType(selectedNotification.type)">
            {{ getTypeLabel(selectedNotification.type) }}
          </el-tag>
        </div>
        <div class="detail-item">
          <label>标题：</label>
          <span>{{ selectedNotification.title }}</span>
        </div>
        <div class="detail-item">
          <label>内容：</label>
          <div class="content-text">{{ selectedNotification.content }}</div>
        </div>
        <div class="detail-item">
          <label>发送范围：</label>
          <span>{{ selectedNotification.targetType === 'ALL' ? '所有用户' : '指定用户' }}</span>
        </div>
        <div class="detail-item">
          <label>发布时间：</label>
          <span>{{ formatTime(selectedNotification.createdAt) }}</span>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { ref, onMounted, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { notificationApi, userApi } from '@/utils/api'
import { ElMessage, ElMessageBox } from 'element-plus'
import dayjs from 'dayjs'

export default {
  name: 'AdminNotification',
  setup() {
    const router = useRouter()
    
    const notificationFormRef = ref()
    const notifications = ref([])
    const allUsers = ref([])
    const currentPage = ref(1)
    const pageSize = ref(20)
    const total = ref(0)
    const publishing = ref(false)
    const showNotificationDetail = ref(false)
    const selectedNotification = ref(null)
    
    const notificationForm = reactive({
      type: 'SYSTEM',
      title: '',
      content: '',
      targetType: 'ALL',
      targetUserIds: []
    })
    
    const rules = {
      type: [{ required: true, message: '请选择通知类型', trigger: 'change' }],
      title: [{ required: true, message: '请输入通知标题', trigger: 'blur' }],
      content: [{ required: true, message: '请输入通知内容', trigger: 'blur' }],
      targetType: [{ required: true, message: '请选择发送范围', trigger: 'change' }],
      targetUserIds: [{ 
        required: true, 
        message: '请选择目标用户', 
        trigger: 'change',
        validator: (rule, value, callback) => {
          if (notificationForm.targetType === 'SPECIFIC' && (!value || value.length === 0)) {
            callback(new Error('请选择目标用户'))
          } else {
            callback()
          }
        }
      }]
    }

    // 获取所有用户列表
    const fetchAllUsers = async () => {
      try {
        const response = await userApi.getAllUsers()
        allUsers.value = response.users || []
      } catch (error) {
        console.error('获取用户列表失败:', error)
      }
    }

    // 获取通知列表
    const fetchNotifications = async () => {
      try {
        const response = await notificationApi.getAdminNotifications(currentPage.value - 1, pageSize.value)
        notifications.value = response.notifications || []
        total.value = response.total || 0
      } catch (error) {
        console.error('获取通知列表失败:', error)
      }
    }

    // 发布通知
    const publishNotification = async () => {
      try {
        await notificationFormRef.value.validate()
        
        publishing.value = true
        const response = await notificationApi.publishNotification({
          type: notificationForm.type,
          title: notificationForm.title,
          content: notificationForm.content,
          targetType: notificationForm.targetType,
          targetUserIds: notificationForm.targetType === 'SPECIFIC' ? notificationForm.targetUserIds : []
        })
        
        ElMessage.success(response.message || '通知发布成功')
        resetForm()
        fetchNotifications()
      } catch (error) {
        if (error.message) {
          ElMessage.error(error.message)
        } else {
          ElMessage.error('发布失败')
        }
      } finally {
        publishing.value = false
      }
    }

    // 重置表单
    const resetForm = () => {
      notificationFormRef.value?.resetFields()
      notificationForm.type = 'SYSTEM'
      notificationForm.targetType = 'ALL'
      notificationForm.targetUserIds = []
    }

    // 查看通知详情
    const viewNotification = (notification) => {
      selectedNotification.value = notification
      showNotificationDetail.value = true
    }

    // 删除通知
    const deleteNotification = async (notificationId) => {
      try {
        await ElMessageBox.confirm(
          '确定要删除这条通知吗？',
          '提示',
          {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning',
          }
        )
        
        await notificationApi.deleteNotification(notificationId)
        ElMessage.success('删除成功')
        fetchNotifications()
      } catch (error) {
        if (error !== 'cancel') {
          ElMessage.error('删除失败')
        }
      }
    }

    // 刷新通知列表
    const refreshNotifications = () => {
      fetchNotifications()
    }

    // 分页处理
    const handleSizeChange = (val) => {
      pageSize.value = val
      currentPage.value = 1
      fetchNotifications()
    }

    const handleCurrentChange = (val) => {
      currentPage.value = val
      fetchNotifications()
    }

    // 获取类型标签样式
    const getTypeTagType = (type) => {
      const typeMap = {
        'SYSTEM': 'primary',
        'ACTIVITY': 'success',
        'MAINTENANCE': 'warning',
        'OTHER': 'info'
      }
      return typeMap[type] || 'info'
    }

    // 获取类型标签文本
    const getTypeLabel = (type) => {
      const typeMap = {
        'SYSTEM': '系统公告',
        'ACTIVITY': '活动通知',
        'MAINTENANCE': '维护通知',
        'OTHER': '其他'
      }
      return typeMap[type] || '其他'
    }

    // 格式化时间
    const formatTime = (time) => {
      return dayjs(time).format('YYYY-MM-DD HH:mm:ss')
    }

    onMounted(() => {
      fetchAllUsers()
      fetchNotifications()
    })

    return {
      notificationFormRef,
      notificationForm,
      rules,
      notifications,
      allUsers,
      currentPage,
      pageSize,
      total,
      publishing,
      showNotificationDetail,
      selectedNotification,
      publishNotification,
      resetForm,
      viewNotification,
      deleteNotification,
      refreshNotifications,
      handleSizeChange,
      handleCurrentChange,
      getTypeTagType,
      getTypeLabel,
      formatTime
    }
  }
}
</script>

<style scoped>
.admin-container {
  padding: 24px;
  background-color: #f4f5f7;
  min-height: 100vh;
}

.page-header {
  background-color: #ffffff;
  padding: 24px;
  border-radius: 8px;
  margin-bottom: 24px;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.05);
}

.page-title {
  margin: 0 0 8px 0;
  color: #303133;
  font-size: 24px;
  font-weight: 600;
}

.page-description {
  color: #606266;
  font-size: 14px;
  margin: 0;
}

.content-area {
  display: flex;
  gap: 24px;
}

.left-panel {
  flex: 1;
  background-color: #ffffff;
  border-radius: 8px;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.05);
  overflow: hidden;
}

.right-panel {
  width: 400px;
  background-color: #ffffff;
  border-radius: 8px;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.05);
  overflow: hidden;
}

.panel-header {
  padding: 20px 24px;
  border-bottom: 1px solid #f0f0f0;
  background-color: #fafafa;
}

.panel-title {
  margin: 0;
  color: #303133;
  font-size: 18px;
  font-weight: 600;
}

.panel-content {
  padding: 24px;
}

/* 通知表单 */
.notification-form .el-form-item {
  margin-bottom: 20px;
}

.notification-form .el-textarea__inner {
  border-radius: 6px;
  font-size: 14px;
}

.send-button {
  width: 100%;
  border-radius: 6px;
  font-weight: 500;
  padding: 12px 0;
}

/* 通知列表 */
.notifications-list {
  max-height: 600px;
  overflow-y: auto;
}

.notification-item {
  padding: 16px;
  border-bottom: 1px solid #f0f0f0;
  transition: background-color 0.2s;
}

.notification-item:last-child {
  border-bottom: none;
}

.notification-item:hover {
  background-color: #fafafa;
}

.notification-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.notification-title {
  font-weight: 600;
  color: #303133;
  font-size: 14px;
}

.notification-time {
  font-size: 12px;
  color: #909399;
}

.notification-content {
  font-size: 14px;
  line-height: 1.6;
  color: #606266;
  white-space: pre-wrap;
}

.notification-stats {
  display: flex;
  gap: 16px;
  margin-top: 12px;
  font-size: 12px;
  color: #909399;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 4px;
}

/* 统计卡片 */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
  margin-bottom: 24px;
}

.stat-card {
  background-color: #f8f9fa;
  border-radius: 8px;
  padding: 20px;
  text-align: center;
  border: 1px solid #e9ecef;
}

.stat-number {
  font-size: 28px;
  font-weight: 700;
  color: #303133;
  margin-bottom: 8px;
}

.stat-label {
  font-size: 14px;
  color: #606266;
  font-weight: 500;
}

/* 分页 */
.pagination-container {
  margin-top: 24px;
  text-align: center;
  padding: 16px 0;
  border-top: 1px solid #f0f0f0;
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .content-area {
    flex-direction: column;
  }
  
  .right-panel {
    width: 100%;
  }
}

@media (max-width: 768px) {
  .admin-container {
    padding: 16px;
  }
  
  .page-header {
    padding: 20px;
  }
  
  .panel-content {
    padding: 20px;
  }
  
  .stats-grid {
    grid-template-columns: 1fr;
  }
}

/* 滚动条样式 */
.notifications-list::-webkit-scrollbar {
  width: 6px;
}

.notifications-list::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 3px;
}

.notifications-list::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 3px;
}

.notifications-list::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}
</style> 