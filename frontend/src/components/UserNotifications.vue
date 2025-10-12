<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useUserStore } from '../stores/User'

const userStore = useUserStore()
const showNotifications = ref(false)
const notifications = ref([])
const loading = ref(false)
const error = ref('')
const unreadCount = ref(0)

// 获取用户通知
const fetchNotifications = async () => {
  if (!userStore.isLoggedIn) return

  loading.value = true
  error.value = ''

  try {
    // 这里应调用实际的API
    const response = await fetch('/api/notifications')
    const data = await response.json()
    notifications.value = data

    // 计算未读消息数量
    unreadCount.value = data.filter(item => !item.read).length
  } catch (err) {
    error.value = '获取通知失败'
    console.error(err)
  } finally {
    loading.value = false
  }
}

// 标记通知为已读
const markAsRead = async (notificationId) => {
  try {
    await fetch(`/api/notifications/${notificationId}/read`, {
      method: 'POST'
    })

    // 更新本地状态
    const notification = notifications.value.find(item => item.id === notificationId)
    if (notification) {
      notification.read = true
      unreadCount.value = Math.max(0, unreadCount.value - 1)
    }
  } catch (err) {
    console.error('标记通知已读失败', err)
  }
}

// 标记所有通知为已读
const markAllAsRead = async () => {
  try {
    await fetch('/api/notifications/read-all', {
      method: 'POST'
    })

    // 更新本地状态
    notifications.value.forEach(notification => {
      notification.read = true
    })
    unreadCount.value = 0
  } catch (err) {
    console.error('标记所有通知已读失败', err)
  }
}

// 格式化日期
const formatDate = (dateString) => {
  const date = new Date(dateString)
  return new Intl.DateTimeFormat('zh-CN', {
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  }).format(date)
}

onMounted(() => {
  if (userStore.isLoggedIn) {
    fetchNotifications()
  }
})
</script>

<template>
  <div class="notification-container">
    <!-- 通知按钮 -->
    <button class="notification-button" @click="showNotifications = !showNotifications">
      <span class="notification-icon">🔔</span>
      <span v-if="unreadCount > 0" class="unread-badge">{{ unreadCount }}</span>
    </button>

    <!-- 通知弹窗 -->
    <div v-if="showNotifications" class="notification-panel">
      <div class="panel-header">
        <h3>我的消息</h3>
        <div class="header-actions">
          <button v-if="unreadCount > 0" class="read-all-btn" @click="markAllAsRead">
            全部已读
          </button>
          <button class="close-btn" @click="showNotifications = false">×</button>
        </div>
      </div>

      <div class="panel-content">
        <div v-if="loading" class="loading-state">
          加载中...
        </div>

        <div v-else-if="error" class="error-state">
          {{ error }}
        </div>

        <div v-else-if="notifications.length === 0" class="empty-state">
          暂无消息通知
        </div>

        <div v-else class="notifications-list">
          <div
            v-for="notification in notifications"
            :key="notification.id"
            :class="['notification-item', {'unread': !notification.read}]"
            @click="markAsRead(notification.id)"
          >
            <div class="notification-icon">
              <span v-if="notification.type === 'order'">📦</span>
              <span v-else-if="notification.type === 'promotion'">🎁</span>
              <span v-else-if="notification.type === 'system'">ℹ️</span>
              <span v-else>📢</span>
            </div>

            <div class="notification-content">
              <div class="notification-title">{{ notification.title }}</div>
              <div class="notification-message">{{ notification.message }}</div>
              <div class="notification-time">{{ formatDate(notification.createdAt) }}</div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.notification-container {
  position: fixed;
  right: 20px;
  bottom: 20px;
  z-index: 1000;
}

.notification-button {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  background-color: #ff6347;
  color: white;
  border: none;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.2);
}

.notification-icon {
  font-size: 20px;
}

.unread-badge {
  position: absolute;
  top: -5px;
  right: -5px;
  background-color: #ff0000;
  color: white;
  border-radius: 50%;
  width: 20px;
  height: 20px;
  font-size: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.notification-panel {
  position: absolute;
  bottom: 60px;
  right: 0;
  width: 320px;
  max-height: 400px;
  background-color: white;
  border-radius: 8px;
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.2);
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 15px;
  border-bottom: 1px solid #eee;
}

.panel-header h3 {
  margin: 0;
  color: #333;
}

.header-actions {
  display: flex;
  align-items: center;
}

.read-all-btn {
  font-size: 12px;
  padding: 4px 8px;
  background-color: #f0f0f0;
  border: none;
  border-radius: 4px;
  margin-right: 10px;
  cursor: pointer;
}

.close-btn {
  border: none;
  background: none;
  font-size: 20px;
  color: #888;
  cursor: pointer;
}

.panel-content {
  flex: 1;
  overflow-y: auto;
}

.loading-state,
.error-state,
.empty-state {
  padding: 20px;
  text-align: center;
  color: #666;
}

.error-state {
  color: #ff6347;
}

.notifications-list {
  padding: 0;
}

.notification-item {
  padding: 15px;
  display: flex;
  border-bottom: 1px solid #eee;
  cursor: pointer;
}

.notification-item:hover {
  background-color: #f9f9f9;
}

.notification-item.unread {
  background-color: rgba(255, 99, 71, 0.05);
}

.notification-item .notification-icon {
  margin-right: 12px;
  font-size: 18px;
}

.notification-content {
  flex: 1;
}

.notification-title {
  font-weight: 500;
  margin-bottom: 5px;
}

.notification-message {
  font-size: 14px;
  color: #666;
  margin-bottom: 5px;
}

.notification-time {
  font-size: 12px;
  color: #999;
}

@media (max-width: 768px) {
  .notification-panel {
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    width: 100%;
    max-height: none;
    border-radius: 0;
  }
}
</style>
