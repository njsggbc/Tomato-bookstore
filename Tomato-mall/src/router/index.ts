import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '../stores/User'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/login',
      name: 'login',
      component: () => import('../views/LoginView.vue')
    },
    {
      path: '/register',
      name: 'register',
      component: () => import('../views/RegisterView.vue')
    },
    {
      path: '/profile',
      name: 'profile',
      component: () => import('../views/UserProfileView.vue'),
      meta: { requiresAuth: true }
    },
    // 重定向根路径到登录页面
    {
      path: '/',
      redirect: '/login'
    },
    // 捕获所有未匹配的路由
    {
      path: '/:pathMatch(.*)*',
      redirect: '/login'
    }
  ],
})

// 路由守卫，保护需要登录的路由
router.beforeEach((to, from, next) => {
  const userStore = useUserStore()

  if (to.meta.requiresAuth && !userStore.isLoggedIn) {
    // 需要登录但用户未登录，重定向到登录页
    next({ name: 'login' })
  } else {
    next()
  }
})

export default router
