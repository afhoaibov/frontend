import { defineStore } from 'pinia'

export const useUiStore = defineStore('ui', {
  state: () => ({
    loginModalVisible: false,
    registerModalVisible: false,
  }),
  actions: {
    openLoginModal() {
      this.closeRegisterModal()
      this.loginModalVisible = true
    },
    closeLoginModal() {
      this.loginModalVisible = false
    },
    openRegisterModal() {
      this.closeLoginModal()
      this.registerModalVisible = true
    },
    closeRegisterModal() {
      this.registerModalVisible = false
    },
  },
}) 