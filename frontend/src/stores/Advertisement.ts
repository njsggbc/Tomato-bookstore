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
  storeId: number // 关联的店铺ID
  startDate: string
  endDate: string
  isActive: boolean
  status: 'pending' | 'approved' | 'rejected' | 'active'
}

// 模拟广告数据
const mockAdvertisements: Advertisement[] = [
  {
    id: 1,
    title: '科幻小说特惠',
    description: '《三体》系列图书限时8折，科幻迷不容错过！',
    imageUrl: 'https://images.unsplash.com/photo-1451187580459-43490279c0fa?w=800&h=400&fit=crop',
    storeId: 1,
    startDate: '2024-01-01T00:00:00Z',
    endDate: '2024-12-31T23:59:59Z',
    isActive: true,
    status: 'approved'
  },
  {
    id: 2,
    title: 'Java学习必备',
    description: 'java学习必备书籍，适合初学者和进阶者',
    imageUrl: 'https://images.unsplash.com/photo-1555066931-4365d14bab8c?w=800&h=400&fit=crop',
    storeId: 2,
    startDate: '2024-01-01T00:00:00Z',
    endDate: '2024-12-31T23:59:59Z',
    isActive: true,
    status: 'approved'
  },
  {
    id: 3,
    title: '新书上架',
    description: '一起探索小王子的世界',
    imageUrl: 'https://images.unsplash.com/photo-1506905925346-21bda4d32df4?w=800&h=400&fit=crop',
    storeId: 3,
    startDate: '2024-01-01T00:00:00Z',
    endDate: '2024-12-31T23:59:59Z',
    isActive: true,
    status: 'approved'
  }
]

export type { Advertisement }
export const useAdvertisementStore = defineStore('advertisement', () => {
  const advertisements = ref<Advertisement[]>([])
  const loading = ref(false)
  const error = ref('')

  // 获取所有广告
  const fetchAdvertisements = async () => {
    loading.value = true
    error.value = ''

    try {
      // 模拟API请求延迟
      await new Promise(resolve => setTimeout(resolve, 500))
      advertisements.value = mockAdvertisements
      return advertisements.value
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
      // 模拟API请求延迟
      await new Promise(resolve => setTimeout(resolve, 500))
      
      // 过滤出活跃的广告
      advertisements.value = mockAdvertisements.filter(ad => ad.isActive)
      return advertisements.value
    } catch (err: any) {
      error.value = '获取广告失败'
      console.error(err)
      return []
    } finally {
      loading.value = false
    }
  }

  // 更新广告状态
  const updateAdStatus = (adId: number, status: 'pending' | 'approved' | 'rejected' | 'active') => {
    const ad = advertisements.value.find(ad => ad.id === adId)
    if (ad) {
      ad.status = status
      // 审批通过时自动激活广告
      if (status === 'approved') {
        ad.isActive = true
      }
      if (status === 'rejected') {
        ad.isActive = false
      }
    }
  }

  // 添加广告申请
  const addAdvertisement = (ad: Omit<Advertisement, 'id' | 'status' | 'isActive'>) => {
    const newId = advertisements.value.length > 0 ? Math.max(...advertisements.value.map(a => a.id)) + 1 : 1
    advertisements.value.push({
      ...ad,
      id: newId,
      status: 'pending',
      isActive: false
    })
  }

  return {
    advertisements,
    loading,
    error,
    fetchAdvertisements,
    fetchActiveAdvertisements,
    updateAdStatus,
    addAdvertisement
  }
})
