// src/stores/Notification.ts
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { api } from '@/Services/api'

// 通知接口定义
export interface Notification {
  id: number
  title: string
  content: string
  type: 'system' | 'order' | 'promotion'
  isRead: boolean
  createdAt: string
  link?: string
}

export const useNotificationStore = defineStore('notification', () => {
  const notifications = ref<Notification[]>([])
  const loading = ref(false)
  const error = ref('')

  // 未读通知数量
  const unreadCount = computed(() => {
    return notifications.value.filter(n => !n.isRead).length
  })

  // 获取用户通知
  const fetchNotifications = async () => {
    loading.value = true
    error.value = ''

    try {
      // 实际项目中这里调用后端 API
      const response = await api.get('/user/notifications')
      notifications.value = response.data
    } catch (err: any) {
      error.value = '获取通知失败'
      console.error(err)
    } finally {
      loading.value = false
    }
  }

  // 标记通知为已读
  const markAsRead = async (notificationId: number) => {
    try {
      await api.put(`/user/notifications/${notificationId}/read`)

      // 更新本地状态
      const notification = notifications.value.find(n => n.id === notificationId)
      if (notification) {
        notification.isRead = true
      }
    } catch (err) {
      error.value = '操作失败'
      console.error(err)
    }
  }

  // 标记所有通知为已读
  const markAllAsRead = async () => {
    try {
      await api.put('/user/notifications/read-all')

      // 更新本地所有通知状态
      notifications.value.forEach(n => {
        n.isRead = true
      })
    } catch (err) {
      error.value = '操作失败'
      console.error(err)
    }
  }

  // 删除通知
  const deleteNotification = async (notificationId: number) => {
    try {
      await api.delete(`/user/notifications/${notificationId}`)

      // 从本地列表中删除
      notifications.value = notifications.value.filter(n => n.id !== notificationId)
    } catch (err) {
      error.value = '删除失败'
      console.error(err)
    }
  }

  return {
    notifications,
    loading,
    error,
    unreadCount,
    fetchNotifications,
    markAsRead,
    markAllAsRead,
    deleteNotification
  }
})
