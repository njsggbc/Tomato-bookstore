// src/stores/Store.ts
import { defineStore } from 'pinia'
import { ref } from 'vue'
import { api } from '@/Services/api'

// 店铺信息接口
interface Store {
  id: number
  name: string
  description: string
  imageUrl: string
  rating: number
  ownerId?: string
}

// 书籍信息接口
interface Book {
  id: number
  title: string
  author: string
  price: number
  description: string
  imageUrl: string
  storeId: number
}

export const useStoreStore = defineStore('store', () => {
  const stores = ref<Store[]>([])
  const currentStore = ref<Store | null>(null)
  const storeBooks = ref<Book[]>([])
  const loading = ref(false)
  const error = ref('')

  // 获取所有推荐店铺
  const fetchRecommendedStores = async () => {
    loading.value = true
    error.value = ''

    try {
      const response = await api.get('/stores/recommended')
      stores.value = response.data
      return response.data
    } catch (err: any) {
      error.value = '获取推荐店铺失败'
      console.error(err)
      return []
    } finally {
      loading.value = false
    }
  }
  const fetchAllStores = async () => {
    loading.value = true
    error.value = ''

    try {
      const response = await api.get('/stores')
      stores.value = response.data
      return response.data
    } catch (err: any) {
      error.value = '获取店铺列表失败'
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
      const response = await api.get(`/stores/category/${categoryId}`)
      stores.value = response.data
      return response.data
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
      const response = await api.get('/stores/my-stores')
      return response.data
    } catch (err: any) {
      error.value = '获取我的店铺失败'
      console.error(err)
      return []
    } finally {
      loading.value = false
    }
  }

  // 获取单个店铺详情
  const fetchStoreById = async (storeId: number) => {
    loading.value = true
    error.value = ''

    try {
      const response = await api.get(`/stores/${storeId}`)
      currentStore.value = response.data
      return response.data
    } catch (err: any) {
      error.value = '获取店铺详情失败'
      console.error(err)
      return null
    } finally {
      loading.value = false
    }
  }

  // 获取店铺内的书籍
  const fetchBooksByStore = async (storeId: number) => {
    loading.value = true
    error.value = ''

    try {
      const response = await api.get(`/stores/${storeId}/books`)
      storeBooks.value = response.data
      return response.data
    } catch (err: any) {
      error.value = '获取店铺书籍失败'
      console.error(err)
      return []
    } finally {
      loading.value = false
    }
  }

  // 创建新店铺 (需要管理员权限)
  const createStore = async (storeData: Omit<Store, 'id'>) => {
    loading.value = true
    error.value = ''

    try {
      const response = await api.post('/stores', storeData)
      stores.value.push(response.data)
      return response.data
    } catch (err: any) {
      error.value = '创建店铺失败'
      console.error(err)
      throw err
    } finally {
      loading.value = false
    }
  }

  // 搜索店铺
  const searchStores = async (query: string) => {
    loading.value = true
    error.value = ''

    try {
      const response = await api.get(`/stores/search?q=${encodeURIComponent(query)}`)
      stores.value = response.data
      return response.data
    } catch (err: any) {
      error.value = '搜索店铺失败'
      console.error(err)
      return []
    } finally {
      loading.value = false
    }
  }

  //删除店铺
  const deleteStore = async (storeId: number) => {
    loading.value = true
    error.value = ''

    try {
      await api.delete(`/stores/${storeId}`)
      // 从列表中移除此店铺
      stores.value = stores.value.filter(store => store.id !== storeId)
      return true
    } catch (err: any) {
      error.value = '删除店铺失败'
      console.error(err)
      throw err
    } finally {
      loading.value = false
    }
  }









  // 搜索书籍
  const searchBooks = async (query: string) => {
    loading.value = true
    error.value = ''

    try {
      const response = await api.get(`/books/search?q=${encodeURIComponent(query)}`)
      return response.data
    } catch (err: any) {
      error.value = '搜索书籍失败'
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
    fetchStoreById,
    fetchBooksByStore,
    createStore,
    searchStores,
    deleteStore,
    fetchStoresByOwner,
  }
})
