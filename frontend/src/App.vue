<template>
  <div id="app">
    <router-view />
    <LoginModal />
    <RegisterModal />
  </div>
</template>

<script>
import { onMounted, onUnmounted } from 'vue'
import simpleWebSocketService from '@/utils/websocket-simple'
import LoginModal from '@/components/LoginModal.vue'
import RegisterModal from '@/components/RegisterModal.vue'

export default {
  name: 'App',
  components: {
    LoginModal,
    RegisterModal
  },
  setup() {
    onMounted(() => {
      console.log('应用启动')
    })

    onUnmounted(() => {
      console.log('应用卸载，清理资源')
      try {
        simpleWebSocketService.disconnect()
      } catch (error) {
        console.error('应用卸载时清理WebSocket失败:', error)
      }
    })
  }
}
</script>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'Roboto', 'Oxygen',
    'Ubuntu', 'Cantarell', 'Fira Sans', 'Droid Sans', 'Helvetica Neue',
    sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  background-color: #f5f5f5;
}

#app {
  min-height: 100vh;
}
</style> 