<template>
  <el-dialog
    v-model="uiStore.registerModalVisible"
    width="400px"
    :before-close="handleClose"
    class="register-dialog"
    :show-close="false"
    align-center
    destroy-on-close
  >
    <div class="register-header">
      <h2>创建您的账户</h2>
      <p>加入我们的社区</p>
    </div>

    <el-form
      ref="registerFormRef"
      :model="registerForm"
      :rules="registerRules"
      class="register-form"
      @submit.prevent="handleRegister"
    >
      <el-form-item prop="username">
        <el-input
          v-model="registerForm.username"
          placeholder="用户名"
          size="large"
          prefix-icon="User"
        />
      </el-form-item>
      
      <el-form-item prop="email">
        <el-input
          v-model="registerForm.email"
          placeholder="邮箱"
          size="large"
          prefix-icon="Message"
        />
      </el-form-item>
      
      <el-form-item prop="nickname">
        <el-input
          v-model="registerForm.nickname"
          placeholder="昵称"
          size="large"
          prefix-icon="UserFilled"
        />
      </el-form-item>

      <el-form-item prop="password">
        <el-input
          v-model="registerForm.password"
          type="password"
          placeholder="密码"
          size="large"
          prefix-icon="Lock"
          show-password
        />
      </el-form-item>
      
      <el-form-item prop="confirmPassword">
        <el-input
          v-model="registerForm.confirmPassword"
          type="password"
          placeholder="确认密码"
          size="large"
          prefix-icon="Lock"
          show-password
        />
      </el-form-item>

      <el-form-item>
        <el-button
          type="primary"
          size="large"
          class="register-button"
          :loading="loading"
          @click="handleRegister"
        >
          立即注册
        </el-button>
      </el-form-item>
    </el-form>
    
    <div class="register-footer">
      <p>
        已有账户？
        <el-button type="text" @click="switchToLogin">
          立即登录
        </el-button>
      </p>
    </div>
  </el-dialog>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useUiStore } from '@/stores/ui'
import { userApi } from '@/utils/api'
import { ElMessage } from 'element-plus'
import {useUserStore} from "@/stores/user";

const uiStore = useUiStore()

const registerFormRef = ref(null)
const loading = ref(false)

const registerForm = reactive({
  username: '',
  email: '',
  nickname: '',
  password: '',
  confirmPassword: ''
})

const validateConfirmPassword = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请再次输入密码'))
  } else if (value !== registerForm.password) {
    callback(new Error('两次输入密码不一致'))
  } else {
    callback()
  }
}

const registerRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度在 3 到 20 个字符', trigger: 'blur' },
    { pattern: /^[a-zA-Z0-9_]+$/, message: '用户名只能包含字母、数字和下划线', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: ['blur', 'change'] }
  ],
  nickname: [
    { required: true, message: '请输入昵称', trigger: 'blur' },
    { min: 2, max: 20, message: '昵称长度在 2 到 20 个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在 6 到 20 个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

const handleRegister = async () => {
  if (!registerFormRef.value) return
  
  try {
    await registerFormRef.value.validate()
  } catch (validationError) {
    return
  }

  loading.value = true
  try {
    const { confirmPassword, ...registerData } = registerForm
    await userApi.register(registerData)
    
    ElMessage.success('注册成功！已为您自动登录。')
    uiStore.closeRegisterModal()
    
    // 注册成功后直接登录
    const loginResponse = await userApi.login({ 
      username: registerData.username, 
      password: registerData.password 
    })
    
    const userStore = useUserStore()
    userStore.setToken(loginResponse.token)
    userStore.setUser(loginResponse.user)

  } catch (apiError) {
    if (apiError.response && apiError.response.data && apiError.response.data.message) {
      ElMessage.error(apiError.response.data.message)
    } else {
      ElMessage.error('注册失败，请稍后重试')
    }
  } finally {
    loading.value = false
  }
}

const switchToLogin = () => {
  uiStore.openLoginModal()
}

const handleClose = () => {
  uiStore.closeRegisterModal()
}
</script>

<style scoped>
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

.register-header {
  text-align: center;
  margin-bottom: 24px;
}

.register-header h2 {
  margin-bottom: 8px;
  font-size: 24px;
  color: #1f2937;
  font-weight: 600;
}

.register-header p {
  color: #6b7280;
  font-size: 15px;
}

.register-form .el-form-item {
  margin-bottom: 20px;
}

.register-button {
  width: 100%;
  border-radius: 8px;
  font-weight: 500;
  letter-spacing: 0.5px;
  padding: 22px 0;
  font-size: 16px;
}

.register-footer {
  text-align: center;
  padding-top: 15px;
  margin-top: 10px;
  border-top: 1px solid #e5e7eb;
}

.register-footer p {
  font-size: 14px;
  color: #4b5563;
}

.register-footer .el-button {
  font-weight: 500;
}
</style> 