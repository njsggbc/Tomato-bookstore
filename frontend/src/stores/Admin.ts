// src/stores/Admin.ts
import { defineStore } from 'pinia'
import { ref } from 'vue'
import { api } from '@/Services/api'

// 待审批店铺接口
interface PendingStore {
  id: number
  name: string
  description: string
  imageUrl: string
  category: string
  ownerId: number
  ownerName: string
  status: 'pending' | 'approved' | 'rejected'
  createdAt: string
}

// 待审批广告接口
interface PendingAd {
  id: number
  title: string
  content: string
  imageUrl: string
  targetUrl: string
  storeId: number
  storeName: string
  startDate: string
  endDate: string
  status: 'pending' | 'approved' | 'rejected'
  createdAt: string
}

export const useAdminStore = defineStore('admin', () => {
  const pendingStores = ref<PendingStore[]>([])
  const pendingAds = ref<PendingAd[]>([])
  const loading = ref(false)
  const error = ref('')

  // 获取待审批店铺列表
  const fetchPendingStores = async () => {
    loading.value = true
    error.value = ''

    try {
      const response = await api.get('/admin/stores/pending')
      pendingStores.value = response.data
      return response.data
    } catch (err: any) {
      error.value = '获取待审批店铺失败'
      console.error(err)
      return []
    } finally {
      loading.value = false
    }
  }

  // 审批店铺
  const approveStore = async (storeId: number, approve: boolean, reason: string = '') => {
    loading.value = true
    error.value = ''

    try {
      const response = await api.post(`/admin/stores/${storeId}/review`, {
        approved: approve,
        reason: reason
      })
      // 更新本地列表
      pendingStores.value = pendingStores.value.filter(store => store.id !== storeId)
      return response.data
    } catch (err: any) {
      error.value = '处理店铺审批失败'
      console.error(err)
      throw err
    } finally {
      loading.value = false
    }
  }

  // 获取待审批广告列表
  const fetchPendingAds = async () => {
    loading.value = true
    error.value = ''

    try {
      const response = await api.get('/admin/ads/pending')
      pendingAds.value = response.data
      return response.data
    } catch (err: any) {
      error.value = '获取待审批广告失败'
      console.error(err)
      return []
    } finally {
      loading.value = false
    }
  }

  // 审批广告
  const approveAd = async (adId: number, approve: boolean, reason: string = '') => {
    loading.value = true
    error.value = ''

    try {
      const response = await api.post(`/admin/ads/${adId}/review`, {
        approved: approve,
        reason: reason
      })
      // 更新本地列表
      pendingAds.value = pendingAds.value.filter(ad => ad.id !== adId)
      return response.data
    } catch (err: any) {
      error.value = '处理广告审批失败'
      console.error(err)
      throw err
    } finally {
      loading.value = false
    }
  }

  return {
    pendingStores,
    pendingAds,
    loading,
    error,
    fetchPendingStores,
    approveStore,
    fetchPendingAds,
    approveAd
  }
})
