import { createRouter, createWebHistory } from 'vue-router'
import Home from '@/views/Home.vue'
import Ranking from '@/views/Ranking.vue'
import Profile from '@/views/Profile.vue'
import Login from '@/views/Login.vue'
import Register from '@/views/Register.vue'
import AdminNotification from '@/views/AdminNotification.vue'

const routes = [
  {
    path: '/',
    name: 'Home',
    component: Home,
    meta: { requiresAuth: false }
  },
  {
    path: '/ranking',
    name: 'Ranking',
    component: Ranking,
    meta: { requiresAuth: false }
  },
  {
    path: '/profile/:id?',
    name: 'Profile',
    component: Profile,
    meta: { requiresAuth: false }
  },
  {
    path: '/admin/notification',
    name: 'AdminNotification',
    component: AdminNotification,
    meta: { requiresAuth: true, requiresAdmin: true }
  },
  {
    path: '/login',
    name: 'Login',
    component: Login
  },
  {
    path: '/register',
    name: 'Register',
    component: Register
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  // 添加延迟以确保Pinia完全初始化
  setTimeout(() => {
    const token = localStorage.getItem('token')
    const userRole = localStorage.getItem('userRole')
    
    if (to.meta.requiresAuth && !token) {
      next('/login')
    } else if (to.meta.requiresAdmin && userRole !== 'ADMIN') {
      next('/home')
    } else {
      next()
    }
  }, 0)
})

export default router 