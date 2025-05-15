// src/stores/Advertisement.ts
import { defineStore } from 'pinia'
import { ref } from 'vue'
import { api } from '@/Services/api'

// 广告信息接口
interface Advertisement {
  id: number
  title: string
  description: string
  imageUrl: string
  productId: number // 关联的书籍ID
  startDate: string
  endDate: string
  isActive: boolean
}

export const useAdvertisementStore = defineStore('advertisement', () => {
  const advertisements = ref<Advertisement[]>([])
  const loading = ref(false)
  const error = ref('')

  // 获取所有广告
  const fetchAdvertisements = async () => {
    loading.value = true
    error.value = ''

    try {
      const response = await api.get('/advertisements')
      advertisements.value = response.data
      return response.data
    } catch (err: any) {
      error.value = '获取广告列表失败'
      console.error(err)
      return []
    } finally {
      loading.value = false
    }
  }

  // 获取活跃广告
  const fetchActiveAdvertisements = async () => {
    loading.value = true
    error.value = ''

    try {
      const response = await api.get('/advertisements/active')
      advertisements.value = response.data
      return response.data
    } catch (err: any) {
      error.value = '获取活跃广告失败'
      console.error(err)
      return []
    } finally {
      loading.value = false
    }
  }

  return {
    advertisements,
    loading,
    error,
    fetchAdvertisements,
    fetchActiveAdvertisements
  }
})
