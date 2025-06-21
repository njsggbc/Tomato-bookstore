import axios from 'axios'
import { userApi } from './mockApi'
import { storeApi } from './mockApi'
import { bookApi } from './mockApi'
import { orderApi } from './mockApi'
import { advertisementApi } from './mockApi'
import { advertisementApplicationApi } from './mockApi'
import { cartApi } from './mockApi'

// 创建axios实例
export const api = axios.create({
  baseURL: '/api',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// 请求拦截器 - 添加token到请求头
api.interceptors.request.use(
  config => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

// 响应拦截器 - 处理常见错误
api.interceptors.response.use(
  response => {
    return response
  },
  error => {
    // 处理401错误 - 未授权/token过期
    if (error.response && error.response.status === 401) {
      // 清除本地存储的认证信息
      localStorage.removeItem('token')
      localStorage.removeItem('user')

      // 可以在这里添加重定向到登录页的逻辑
      window.location.href = '/login'
    }
    return Promise.reject(error)
  }
)

// 导出所有API
export {
  userApi,
  storeApi,
  bookApi,
  orderApi,
  advertisementApi,
  advertisementApplicationApi,
  cartApi
}
