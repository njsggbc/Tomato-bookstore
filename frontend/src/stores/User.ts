import { defineStore } from 'pinia'
import { ref } from 'vue'
import { api } from '@/Services/api'

// 用户信息接口
interface User {
  username: string
  email: string
  phone?: string
  address?: string
  role: string
}

// 注册表单接口
interface RegisterForm {
  username: string
  password: string
  email: string
  role: string
}

// 用户信息更新接口
interface UserProfileUpdate {
  username: string
  email: string
  phone?: string
  address?: string
}

export const useUserStore = defineStore('user', () => {
  const user = ref<User | null>(null)
  const token = ref<string | null>(null)
  const isLoggedIn = ref(false)

  // 初始化 - 从本地存储加载用户状态
  const initUserState = () => {
    const storedToken = localStorage.getItem('token')
    if (storedToken) {
      token.value = storedToken
      isLoggedIn.value = true

      // 尝试从本地存储获取用户数据
      const storedUser = localStorage.getItem('user')
      if (storedUser) {
        try {
          user.value = JSON.parse(storedUser)
        } catch (e) {
          // 如果解析失败，清除存储
          localStorage.removeItem('user')
        }
      }
    }
  }

  // 登录
  const login = async (username: string, password: string) => {
    try {
      const response = await api.post('/login', { username, password })
      const { token: authToken, user: userData } = response.data

      token.value = authToken
      user.value = userData
      isLoggedIn.value = true

      // 保存到本地存储
      localStorage.setItem('token', authToken)
      localStorage.setItem('user', JSON.stringify(userData))

      return userData
    } catch (error) {
      throw new Error('登录失败，请检查用户名和密码')
    }
  }

  // 注册
  const register = async (registerData: RegisterForm) => {
    try {
      await api.post('/register', registerData)
      return true
    } catch (error) {
      throw new Error('注册失败，请稍后重试')
    }
  }

  // 获取用户信息
  const getUserProfile = async () => {
    if (!token.value) {
      throw new Error('用户未登录')
    }

    try {
      const response = await api.get('/user/profile')
      user.value = response.data

      // 更新本地存储
      localStorage.setItem('user', JSON.stringify(response.data))

      return response.data
    } catch (error) {
      throw new Error('获取用户信息失败')
    }
  }

  // 更新用户信息
  const updateUserProfile = async (userData: UserProfileUpdate) => {
    if (!token.value) {
      throw new Error('用户未登录')
    }

    try {
      const response = await api.put('/user/profile', userData)
      user.value = response.data

      // 更新本地存储
      localStorage.setItem('user', JSON.stringify(response.data))

      return response.data
    } catch (error) {
      throw new Error('更新用户信息失败')
    }
  }

  // 登出
  const logout = () => {
    user.value = null
    token.value = null
    isLoggedIn.value = false

    // 清除本地存储
    localStorage.removeItem('token')
    localStorage.removeItem('user')
  }

  // 初始化状态
  initUserState()

  return {
    user,
    isLoggedIn,
    login,
    register,
    getUserProfile,
    updateUserProfile,
    logout
  }
})
