// src/stores/Store.ts
import { defineStore } from 'pinia'
import { ref } from 'vue'
import { api } from '@/Services/api'

// 店铺信息接口
interface Store {
  id: number
  name: string
  description: string
  address: string
  phone: string
  imageUrl: string
  userId: number
  rating?: number
  createdAt?: string
}

// 书籍信息接口
interface Book {
  id: number
  title: string
  author: string
  description: string
  price: number
  stock: number
  storeId: number
  category: string
  cover: string
  rating: number
  sales: number
  createdAt: string
}

// 模拟店铺数据
const mockStores: Store[] = [
  {
    id: 1,
    name: '文学书店',
    description: '专注于文学和艺术类书籍',
    address: '北京市海淀区中关村大街1号',
    phone: '010-12345678',
    imageUrl: 'https://images.unsplash.com/photo-1481627834876-b7833e8f5570?w=800&h=400&fit=crop',
    userId: 2,
    rating: 4.5,
    createdAt: '2024-01-02T00:00:00Z'
  },
  {
    id: 2,
    name: '科技书城',
    description: '计算机、科技类图书专营',
    address: '北京市朝阳区建国路2号',
    phone: '010-87654321',
    imageUrl: 'https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?w=800&h=400&fit=crop',
    userId: 3,
    rating: 4.8,
    createdAt: '2024-01-03T00:00:00Z'
  },
  {
    id: 3,
    name: '儿童书店',
    description: '儿童读物、绘本、教育类图书',
    address: '北京市西城区西单3号',
    phone: '010-23456789',
    imageUrl: 'https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=800&h=400&fit=crop',
    userId: 4,
    rating: 4.6,
    createdAt: '2024-01-04T00:00:00Z'
  },
  {
    id: 4,
    name: '古籍书店',
    description: '古籍、历史、文化类图书',
    address: '北京市东城区王府井4号',
    phone: '010-34567890',
    imageUrl: 'https://images.unsplash.com/photo-1512820790803-83ca734da794?w=800&h=400&fit=crop',
    userId: 5,
    rating: 4.9,
    createdAt: '2024-01-05T00:00:00Z'
  }
]

// 模拟书籍数据
const mockBooks: Book[] = [
  {
    id: 1,
    title: '三体',
    author: '刘慈欣',
    description: '科幻小说代表作',
    price: 59.8,
    stock: 100,
    storeId: 1,
    category: '科幻',
    cover: 'https://images.unsplash.com/photo-1451187580459-43490279c0fa?w=200&h=300&fit=crop',
    rating: 4.8,
    sales: 1000,
    createdAt: '2024-01-03T00:00:00Z'
  },
  {
    id: 2,
    title: '活着',
    author: '余华',
    description: '经典文学作品',
    price: 39.8,
    stock: 50,
    storeId: 1,
    category: '文学',
    cover: 'https://images.unsplash.com/photo-1481627834876-b7833e8f5570?w=200&h=300&fit=crop',
    rating: 4.9,
    sales: 800,
    createdAt: '2024-01-04T00:00:00Z'
  },
  {
    id: 3,
    title: 'JavaScript高级程序设计',
    author: 'Nicholas C. Zakas',
    description: 'JavaScript经典教程',
    price: 99.0,
    stock: 30,
    storeId: 2,
    category: '计算机',
    cover: 'https://images.unsplash.com/photo-1555066931-4365d14bab8c?w=200&h=300&fit=crop',
    rating: 4.7,
    sales: 500,
    createdAt: '2024-01-05T00:00:00Z'
  },
  {
    id: 4,
    title: '小王子',
    author: '圣埃克苏佩里',
    description: '经典儿童文学',
    price: 29.8,
    stock: 200,
    storeId: 3,
    category: '儿童文学',
    cover: 'https://images.unsplash.com/photo-1506905925346-21bda4d32df4?w=200&h=300&fit=crop',
    rating: 4.9,
    sales: 1200,
    createdAt: '2024-01-06T00:00:00Z'
  }
]

export const useStoreStore = defineStore('store', () => {
  const stores = ref<Store[]>([])
  const currentStore = ref<Store | null>(null)
  const storeBooks = ref<Book[]>([])
  const loading = ref(false)
  const error = ref('')

  // 获取所有店铺
  const fetchAllStores = () => {
    loading.value = true
    error.value = ''

    try {
      // 从 localStorage 获取用户创建的店铺
      const storedStores = JSON.parse(localStorage.getItem('stores') || '[]')
      
      // 合并模拟店铺和用户创建的店铺
      // 过滤掉模拟店铺中已经存在的店铺（通过id判断）
      const existingStoreIds = new Set(storedStores.map((store: Store) => store.id))
      const uniqueMockStores = mockStores.filter(store => !existingStoreIds.has(store.id))
      
      // 合并所有店铺
      stores.value = [...uniqueMockStores, ...storedStores]
    } catch (err: any) {
      error.value = '获取店铺列表失败'
      console.error(err)
    } finally {
      loading.value = false
    }
  }

  // 获取单个店铺详情
  const fetchStoreById = (storeId: number) => {
    loading.value = true
    error.value = ''

    try {
      // 先从模拟店铺中查找
      const mockStore = mockStores.find(store => store.id === storeId)
      if (mockStore) {
        currentStore.value = mockStore
        return mockStore
      }

      // 如果模拟店铺中没有，则从localStorage中查找
      const storedStores = JSON.parse(localStorage.getItem('stores') || '[]')
      const store = storedStores.find((s: Store) => s.id === storeId)
      currentStore.value = store || null
      return store || null
    } catch (err: any) {
      error.value = '获取店铺详情失败'
      console.error(err)
      return null
    } finally {
      loading.value = false
    }
  }

  // 创建新店铺
  const createStore = (storeData: Omit<Store, 'id'>) => {
    loading.value = true
    error.value = ''

    try {
      const storedStores = JSON.parse(localStorage.getItem('stores') || '[]')
      const newStore = {
        ...storeData,
        id: Date.now(),
        createdAt: new Date().toISOString(),
        rating: 5
      }
      storedStores.push(newStore)
      localStorage.setItem('stores', JSON.stringify(storedStores))
      stores.value = storedStores
      return newStore
    } catch (err: any) {
      error.value = '创建店铺失败'
      console.error(err)
      throw err
    } finally {
      loading.value = false
    }
  }

  // 更新店铺信息
  const updateStore = (storeId: number, storeData: Partial<Store>) => {
    loading.value = true
    error.value = ''

    try {
      const storedStores = JSON.parse(localStorage.getItem('stores') || '[]')
      const storeIndex = storedStores.findIndex((s: Store) => s.id === storeId)
      
      if (storeIndex === -1) {
        throw new Error('店铺不存在')
      }

      storedStores[storeIndex] = {
        ...storedStores[storeIndex],
        ...storeData
      }

      localStorage.setItem('stores', JSON.stringify(storedStores))
      stores.value = storedStores
      return storedStores[storeIndex]
    } catch (err: any) {
      error.value = '更新店铺失败'
      console.error(err)
      throw err
    } finally {
      loading.value = false
    }
  }

  // 删除店铺
  const deleteStore = (storeId: number) => {
    loading.value = true
    error.value = ''

    try {
      const storedStores = JSON.parse(localStorage.getItem('stores') || '[]')
      const updatedStores = storedStores.filter((s: Store) => s.id !== storeId)
      localStorage.setItem('stores', JSON.stringify(updatedStores))
      stores.value = updatedStores
    } catch (err: any) {
      error.value = '删除店铺失败'
      console.error(err)
      throw err
    } finally {
      loading.value = false
    }
  }

  // 搜索店铺
  const searchStores = (query: string) => {
    loading.value = true
    error.value = ''

    try {
      // 搜索模拟店铺
      const mockResults = mockStores.filter(store => 
        store.name.toLowerCase().includes(query.toLowerCase()) ||
        store.description.toLowerCase().includes(query.toLowerCase())
      )

      // 搜索用户创建的店铺
      const storedStores = JSON.parse(localStorage.getItem('stores') || '[]')
      const storedResults = storedStores.filter((store: Store) => 
        store.name.toLowerCase().includes(query.toLowerCase()) ||
        store.description.toLowerCase().includes(query.toLowerCase())
      )

      // 合并搜索结果
      const results = [...mockResults, ...storedResults]
      return results
    } catch (err: any) {
      error.value = '搜索店铺失败'
      console.error(err)
      return []
    } finally {
      loading.value = false
    }
  }

  // 获取所有推荐店铺
  const fetchRecommendedStores = async () => {
    loading.value = true
    error.value = ''

    try {
      stores.value = mockStores
      return stores.value
    } catch (err: any) {
      error.value = '获取推荐店铺失败'
      console.error(err)
      return []
    } finally {
      loading.value = false
    }
  }

  // 获取指定分类的店铺
  const fetchStoresByCategory = async (categoryId: number) => {
    loading.value = true
    error.value = ''

    try {
      stores.value = mockStores
      return stores.value
    } catch (err: any) {
      error.value = '获取分类店铺失败'
      console.error(err)
      return []
    } finally {
      loading.value = false
    }
  }

  // 获取我的店铺
  const fetchStoresByOwner = async () => {
    loading.value = true
    error.value = ''

    try {
      // 获取当前登录用户id
      let userId = null
      try {
        const user = JSON.parse(localStorage.getItem('user') || 'null')
        userId = user?.id
      } catch { userId = null }
      if (!userId) {
        stores.value = []
        return []
      }
      const storedStores = JSON.parse(localStorage.getItem('stores') || '[]')
      const myStores = storedStores.filter((store: Store) => store.userId === userId)
      stores.value = myStores
      return myStores
    } catch (err: any) {
      error.value = '获取我的店铺失败'
      console.error(err)
      return []
    } finally {
      loading.value = false
    }
  }

  // 获取店铺内的书籍
  const fetchBooksByStore = async (storeId: number) => {
    loading.value = true
    error.value = ''

    try {
      // 获取模拟书籍
      const mockStoreBooks = mockBooks.filter(book => book.storeId === storeId)
      
      // 获取用户创建的书籍
      const storedBooks = JSON.parse(localStorage.getItem('books') || '[]')
      const userStoreBooks = storedBooks.filter((book: Book) => book.storeId === storeId)
      
      // 合并两种来源的书籍
      const allBooks = [...mockStoreBooks, ...userStoreBooks]
      
      // 更新storeBooks
      storeBooks.value = allBooks
      
      // 打印调试信息
      console.log('店铺ID:', storeId)
      console.log('模拟书籍:', mockStoreBooks)
      console.log('用户书籍:', userStoreBooks)
      console.log('合并后的书籍:', allBooks)
      
      return allBooks
    } catch (err: any) {
      error.value = '获取店铺书籍失败'
      console.error(err)
      return []
    } finally {
      loading.value = false
    }
  }

  // 搜索图书
  const searchBooks = async (query: string) => {
    loading.value = true
    error.value = ''

    try {
      // 搜索模拟书籍
      const mockResults = mockBooks.filter(book => 
        book.title.toLowerCase().includes(query.toLowerCase()) ||
        book.author.toLowerCase().includes(query.toLowerCase()) ||
        book.description.toLowerCase().includes(query.toLowerCase())
      )

      // 搜索用户创建的书籍
      const storedBooks = JSON.parse(localStorage.getItem('books') || '[]')
      const storedResults = storedBooks.filter((book: Book) => 
        book.title.toLowerCase().includes(query.toLowerCase()) ||
        book.author.toLowerCase().includes(query.toLowerCase()) ||
        book.description.toLowerCase().includes(query.toLowerCase())
      )

      // 合并搜索结果
      const results = [...mockResults, ...storedResults]
      return results
    } catch (err: any) {
      error.value = '搜索图书失败'
      console.error(err)
      return []
    } finally {
      loading.value = false
    }
  }

  return {
    stores,
    currentStore,
    storeBooks,
    loading,
    error,
    fetchRecommendedStores,
    fetchAllStores,
    fetchStoresByCategory,
    fetchStoresByOwner,
    fetchStoreById,
    fetchBooksByStore,
    createStore,
    updateStore,
    deleteStore,
    searchStores,
    searchBooks
  }
})
