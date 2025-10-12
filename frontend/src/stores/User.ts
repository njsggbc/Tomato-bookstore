import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

// 用户信息接口
interface User {
  id: number
  username: string
  email: string
  role: string
  avatar: string
  createdAt: string
  password: string
  storeId?: number  // 添加 storeId 字段，可选
}

// 注册表单接口
interface RegisterForm {
  username: string
  email: string
  password: string
  role: string
}

// 用户资料更新接口
interface UserProfileUpdate {
  username?: string
  avatar?: string
}

// 从本地存储获取用户数据
const getStoredUsers = (): User[] => {
  const storedUsers = localStorage.getItem('users')
  return storedUsers ? JSON.parse(storedUsers) : []
}

// 保存用户数据到本地存储
const saveUsers = (users: User[]) => {
  localStorage.setItem('users', JSON.stringify(users))
}

export const useUserStore = defineStore('user', () => {
  const user = ref<User | null>(null)
  const loading = ref(false)
  const error = ref('')

  // 添加计算属性
  const isLoggedIn = computed(() => !!user.value)

  // 登录
  const login = async (email: string, password: string) => {
    loading.value = true
    error.value = ''

    try {
      // 模拟网络延迟
      await new Promise(resolve => setTimeout(resolve, 500))

      const users = getStoredUsers()
      const foundUser = users.find(u => u.email === email)

      if (!foundUser) {
        throw new Error('用户不存在')
      }

      // 验证密码
      if (password !== foundUser.password) {
        throw new Error('密码错误')
      }

      // 生成模拟token
      const token = 'mock_token_' + Date.now()
      localStorage.setItem('token', token)
      localStorage.setItem('user', JSON.stringify(foundUser))
      
      user.value = foundUser
      return foundUser
    } catch (err: any) {
      error.value = err.message || '登录失败'
      throw err
    } finally {
      loading.value = false
    }
  }

  // 注册
  const register = async (form: RegisterForm) => {
    loading.value = true
    error.value = ''

    try {
      // 模拟网络延迟
      await new Promise(resolve => setTimeout(resolve, 500))

      const users = getStoredUsers()

      // 检查邮箱是否已存在
      if (users.some(u => u.email === form.email)) {
        throw new Error('该邮箱已被注册')
      }

      // 创建新用户
      const newUser: User = {
        id: users.length + 1,
        username: form.username,
        email: form.email,
        password: form.password,
        role: form.role,
        avatar: 'https://picsum.photos/200/200?random=' + Date.now(),
        createdAt: new Date().toISOString()
      }

      // 保存新用户
      users.push(newUser)
      saveUsers(users)

      // 自动登录
      const token = 'mock_token_' + Date.now()
      localStorage.setItem('token', token)
      localStorage.setItem('user', JSON.stringify(newUser))
      
      user.value = newUser
      return newUser
    } catch (err: any) {
      error.value = err.message || '注册失败'
      throw err
    } finally {
      loading.value = false
    }
  }

  // 获取用户信息
  const getUserProfile = async () => {
    loading.value = true
    error.value = ''

    try {
      const storedUser = localStorage.getItem('user')
      if (storedUser) {
        user.value = JSON.parse(storedUser)
        return user.value
      }
      return null
    } catch (err: any) {
      error.value = '获取用户信息失败'
      console.error(err)
      return null
    } finally {
      loading.value = false
    }
  }

  // 更新用户信息
  const updateUserProfile = async (data: UserProfileUpdate) => {
    loading.value = true
    error.value = ''

    try {
      if (!user.value) {
        throw new Error('用户未登录')
      }

      const users = getStoredUsers()
      const index = users.findIndex(u => u.id === user.value?.id)

      if (index === -1) {
        throw new Error('用户不存在')
      }

      // 更新用户信息
      users[index] = {
        ...users[index],
        ...data
      }

      // 保存更新后的用户数据
      saveUsers(users)
      localStorage.setItem('user', JSON.stringify(users[index]))
      
      user.value = users[index]
      return user.value
    } catch (err: any) {
      error.value = err.message || '更新用户信息失败'
      throw err
    } finally {
      loading.value = false
    }
  }

  // 登出
  const logout = () => {
    localStorage.removeItem('token')
    localStorage.removeItem('user')
    user.value = null
  }

  // 初始化时检查本地存储中的用户信息
  const init = () => {
    const storedUser = localStorage.getItem('user')
    if (storedUser) {
      user.value = JSON.parse(storedUser)
    }
  }

  // 初始化
  init()

  return {
    user,
    loading,
    error,
    isLoggedIn,
    login,
    register,
    getUserProfile,
    updateUserProfile,
    logout
  }
})
