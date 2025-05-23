// src/router/index.ts
import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '../stores/User'
import {ROLES} from "@/constants/roles.ts";

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
    {
      path: '/home',
      name: 'home',
      component: () => import('../views/HomePage.vue'),
      meta: { requiresAuth: true } // 添加登录验证
    },
    {
      path: '/store/:id',
      name: 'store',
      component: () => import('../views/StoreView.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/book/:id',
      name: 'book',
      component: () => import('../views/BookDetailView.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/store/create',
      name: 'store-create',
      component: () => import('../views/StoreCreateView.vue'),
      meta: { requiresAuth: true, roles: ['admin'] }
    },
    {
      path: '/store/:storeId/book/create',
      name: 'book-create',
      component: () => import('../views/BookCreateView.vue'),
      meta: { requiresAuth: true, roles: ['merchant'] }
    },
    {
      path: '/search/:type',
      name: 'search',
      component: () => import('../views/SearchResultsView.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/',
      redirect: '/login'
    },
    {
      path: '/:pathMatch(.*)*',
      redirect: '/login'
    },
    {
      path: '/advertisements',
      name: 'advertisements',
      component: () => import('../views/AdvertisementView.vue')
    },
    {
      path: '/cart',
      name: 'cart',
      component: () => import('../views/CartView.vue')
    },
    {
      path: '/store/apply',
      name: 'StoreApplication',
      component: () => import('../views/StoreApplicationView.vue'),
      meta: {
        requiresAuth: true,
        roles: [ROLES.MERCHANT]
      }
    },
    {
      path: '/ad/apply',
      name: 'adApply',
      component: () => import('../views/AdvertisementApplyView.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/admin/dashboard',
      name: 'AdminDashboard',
      component: () => import('../views/admin/AdminDashboardView.vue'),
      meta: { requiresAdmin: true }
    },
    {
      path: '/admin/stores/approval',
      name: 'StoreApproval',
      component: () => import('../views/admin/StoreApprovalView.vue'),
      meta: { requiresAdmin: true }
    },
    {
      path: '/admin/ads/approval',
      name: 'AdApproval',
      component: () => import('../views/admin/AdApprovalView.vue'),
      meta: { requiresAdmin: true }
    },
  ],
})

// 路由守卫，保护需要登录的路由
router.beforeEach((to, from, next) => {
  const userStore = useUserStore()

  if (to.meta.requiresAuth && !userStore.isLoggedIn) {
    next({ name: 'login' })
  } else if (to.meta.roles && Array.isArray(to.meta.roles) &&
    userStore.user && !to.meta.roles.includes(userStore.user.role)) {
    // 角色权限验证
    next({ name: 'home' })
  } else if (userStore.isLoggedIn && (to.name === 'login' || to.name === 'register')) {
    next({ name: 'home' })
  } else {
    next()
  }
})

export default router
