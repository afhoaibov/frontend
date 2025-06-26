<template>
  <el-dialog
    v-model="uiStore.loginModalVisible"
    width="400px"
    :before-close="handleClose"
    class="login-dialog"
    :show-close="false"
    align-center
    destroy-on-close
  >
    <div class="login-header">
      <h2>社交平台</h2>
      <p>欢迎回来，请登录您的账户</p>
    </div>

    <el-form
      ref="loginFormRef"
      :model="loginForm"
      :rules="loginRules"
      class="login-form"
      @submit.prevent="handleLogin"
    >
      <el-form-item prop="username">
        <el-input
          v-model="loginForm.username"
          placeholder="用户名"
          size="large"
          prefix-icon="User"
        />
      </el-form-item>
      
      <el-form-item prop="password">
        <el-input
          v-model="loginForm.password"
          type="password"
          placeholder="密码"
          size="large"
          prefix-icon="Lock"
          show-password
        />
      </el-form-item>
      
      <el-form-item>
        <el-button
          type="primary"
          size="large"
          class="login-button"
          :loading="loading"
          @click="handleLogin"
        >
          立即登录
        </el-button>
      </el-form-item>
    </el-form>
    
    <div class="login-footer">
      <p>
        还没有账户？
        <el-button type="text" @click="switchToRegister">
          立即注册
        </el-button>
      </p>
    </div>
  </el-dialog>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { useUiStore } from '@/stores/ui'
import { userApi } from '@/utils/api'
import { ElMessage } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()
const uiStore = useUiStore()

const loginFormRef = ref(null)
const loading = ref(false)

const loginForm = reactive({
  username: '',
  password: ''
})

const loginRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
  ]
}

const handleLogin = async () => {
  if (!loginFormRef.value) return
  
  // 1. Validate the form first
  try {
    await loginFormRef.value.validate()
  } catch (validationError) {
    // Validation failed. Element Plus will automatically display error messages on the form items.
    // We just need to stop the function execution here.
    return
  }

  // 2. If validation succeeds, call the API
  loading.value = true
  try {
    const response = await userApi.login(loginForm)
    
    userStore.setToken(response.token)
    userStore.setUser(response.user)
    
    ElMessage.success('登录成功')
    uiStore.closeLoginModal()
    
    // Reload the page to ensure all components are updated with the new user state
    // window.location.reload()
    router.push('/')

  } catch (apiError) {
    // The API interceptor will show most errors, but we add a fallback here for clarity.
    if (apiError.response && apiError.response.data && apiError.response.data.message) {
      ElMessage.error(apiError.response.data.message)
    } else {
      ElMessage.error('登录失败，请检查用户名或密码是否正确')
    }
    console.error('Login API Error:', apiError)
  } finally {
    loading.value = false
  }
}

const switchToRegister = () => {
  uiStore.openRegisterModal()
}

const handleClose = () => {
  uiStore.closeLoginModal()
}
</script>

<style scoped>
/* Use deep selector to override element-plus styles for a modern look */
:deep(.el-dialog) {
  border-radius: 12px !important;
  box-shadow: 0 10px 30px -10px rgba(0, 0, 0, 0.2);
}

:deep(.el-dialog__body) {
  padding: 30px 40px;
}

:deep(.el-input__wrapper) {
  border-radius: 8px !important;
  background-color: #f4f5f7 !important;
  box-shadow: none !important;
  padding: 4px 15px;
}

:deep(.el-input__wrapper.is-focus) {
  background-color: #fff !important;
  box-shadow: 0 0 0 1px var(--el-color-primary) !important;
}

.login-header {
  text-align: center;
  margin-bottom: 24px;
}

.login-header h2 {
  margin-bottom: 8px;
  font-size: 24px;
  color: #1f2937;
  font-weight: 600;
}

.login-header p {
  color: #6b7280;
  font-size: 15px;
}

.login-form .el-form-item {
  margin-bottom: 20px;
}

.login-button {
  width: 100%;
  border-radius: 8px;
  font-weight: 500;
  letter-spacing: 0.5px;
  padding: 22px 0;
  font-size: 16px;
}

.login-footer {
  text-align: center;
  padding-top: 15px;
  margin-top: 10px;
  border-top: 1px solid #e5e7eb;
}

.login-footer p {
  font-size: 14px;
  color: #4b5563;
}

.login-footer .el-button {
  font-weight: 500;
}
</style> 