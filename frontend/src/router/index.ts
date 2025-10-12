// src/router/index.ts
import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '../stores/User'
import { ROLES } from "@/constants/roles"
import HomePage from '../views/HomePage.vue'
import LoginView from '../views/LoginView.vue'
import RegisterView from '../views/RegisterView.vue'
import UserProfileView from '../views/UserProfileView.vue'
import StoreView from '../views/StoreView.vue'
import BookDetailView from '../views/BookDetailView.vue'
import CartView from '../views/CartView.vue'
import OrdersView from '../views/OrdersView.vue'
import OrderDetailView from '../views/OrderDetailView.vue'
import StoresView from '../views/StoresView.vue'
import CheckoutView from '../views/CheckoutView.vue'
import StoreManageView from '../views/StoreManageView.vue'
import BookCreateView from '../views/BookCreateView.vue'
import StoreOrderListView from '../views/StoreOrderListView.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      redirect: '/home'
    },
    {
      path: '/home',
      name: 'home',
      component: HomePage
    },
    {
      path: '/login',
      name: 'login',
      component: LoginView
    },
    {
      path: '/register',
      name: 'register',
      component: RegisterView
    },
    {
      path: '/profile',
      name: 'profile',
      component: UserProfileView,
      meta: { requiresAuth: true }
    },
    {
      path: '/store/:id',
      name: 'store',
      component: StoreView
    },
    {
      path: '/book/:id',
      name: 'book',
      component: BookDetailView
    },
    {
      path: '/cart',
      name: 'cart',
      component: CartView,
      meta: { requiresAuth: true }
    },
    {
      path: '/checkout',
      name: 'checkout',
      component: CheckoutView,
      meta: { requiresAuth: true }
    },
    {
      path: '/orders',
      name: 'orders',
      component: OrdersView,
      meta: { requiresAuth: true }
    },
    {
      path: '/store/orders',
      name: 'store-orders',
      component: StoreOrderListView,
      meta: { requiresAuth: true, roles: [ROLES.MERCHANT] }
    },
    {
      path: '/store/create',
      name: 'store-create',
      component: () => import('../views/StoreCreateView.vue'),
      meta: { requiresAuth: true, roles: [ROLES.MERCHANT] }
    },
    {
      path: '/store/:storeId/book/create',
      name: 'book-create',
      component: BookCreateView,
      meta: { requiresAuth: true, roles: [ROLES.MERCHANT] }
    },
    {
      path: '/store/:storeId/book/:bookId/edit',
      name: 'book-edit',
      component: BookCreateView,
      meta: { requiresAuth: true, roles: [ROLES.MERCHANT] }
    },
    {
      path: '/search/:type',
      name: 'search',
      component: () => import('../views/SearchResultsView.vue')
    },
    {
      path: '/store/apply',
      name: 'store-apply',
      component: () => import('../views/StoreApplicationView.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/ad/apply',
      name: 'ad-apply',
      component: () => import('../views/AdvertisementApplyView.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/ads-approval',
      name: 'ads-approval',
      component: () => import('../views/AdsApprovalView.vue'),
      meta: { requiresAuth: true, roles: [ROLES.ADMIN] }
    },
    {
      path: '/order/:id',
      name: 'order-detail',
      component: OrderDetailView,
      meta: { requiresAuth: true }
    },
    {
      path: '/stores',
      name: 'stores',
      component: StoresView
    },
    {
      path: '/store/:storeId/manage',
      name: 'store-manage',
      component: StoreManageView,
      meta: { requiresAuth: true, roles: [ROLES.MERCHANT] }
    },
    {
      path: '/:pathMatch(.*)*',
      redirect: '/home'
    }
  ]
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const userStore = useUserStore()
  const token = localStorage.getItem('token')

  // 如果用户已登录（有token）
  if (token) {
    // 如果访问登录或注册页面，重定向到首页
    if (to.name === 'login' || to.name === 'register') {
      next({ name: 'home' })
      return
    }

    // 检查角色权限
    if (to.meta.roles && userStore.user) {
      const roles = to.meta.roles as string[]
      if (!roles.includes(userStore.user.role)) {
        next({ name: 'home' })
        return
      }
    }
  } else {
    // 如果用户未登录，且访问需要认证的页面
    if (to.meta.requiresAuth) {
      next({ name: 'login' })
      return
    }
  }

  next()
})

export default router
