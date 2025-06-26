import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import App from './App.vue'
import router from './router'
import { useUserStore } from './stores/user'

const app = createApp(App)

// 全局错误处理
app.config.errorHandler = (err, vm, info) => {
  console.error('Vue应用错误:', err)
  console.error('错误信息:', info)
  console.error('组件:', vm)
  
  // 避免错误传播导致应用崩溃
  return false
}

// 全局警告处理
app.config.warnHandler = (msg, vm, trace) => {
  console.warn('Vue应用警告:', msg)
  console.warn('警告追踪:', trace)
  console.warn('组件:', vm)
}

// 注册Element Plus图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

const pinia = createPinia()
app.use(pinia)

// 初始化用户状态
const userStore = useUserStore()
userStore.initUserState()

app.use(router)
app.use(ElementPlus)

app.mount('#app') 