<script setup lang="ts">
import { onMounted, ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '../stores/User'
import { useOrderStore } from '../stores/Order'
import { ROLES } from '../constants/roles'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const orderStore = useOrderStore()

// 获取路由参数中的店铺ID
const storeId = Number(route.params.storeId)
const orders = ref([])
const loading = ref(true)
const error = ref('')
const currentStatus = ref('all') // 用于筛选订单状态

// 权限检查 - 确保用户是店铺所有者
const checkPermission = async () => {
  if (!userStore.isLoggedIn) {
    router.push('/login?redirect=' + encodeURIComponent(route.fullPath))
    return false
  }

  // 检查用户是否是该店铺的所有者
  try {
    const response = await fetch(`/api/stores/${storeId}/check-owner`)
    const data = await response.json()

    if (!data.isOwner) {
      // 如果不是店铺所有者，重定向到首页
      router.push('/home')
      return false
    }

    return true
  } catch (err) {
    error.value = '验证店铺所有权失败'
    return false
  }
}

// 获取店铺订单
const fetchStoreOrders = async () => {
  loading.value = true
  error.value = ''

  try {
    const response = await fetch(`/api/stores/${storeId}/orders`)
    const data = await response.json()
    orders.value = data
  } catch (err) {
    error.value = '获取店铺订单失败'
  } finally {
    loading.value = false
  }
}

// 确认发货
const shipOrder = async (orderId) => {
  if (confirm('确认发货此订单？')) {
    try {
      const response = await fetch(`/api/orders/${orderId}/ship`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        }
      })

      if (response.ok) {
        alert('订单已标记为已发货')
        // 更新订单列表
        await fetchStoreOrders()
      } else {
        throw new Error('更新订单状态失败')
      }
    } catch (err) {
      error.value = '操作失败，请重试'
    }
  }
}

// 订单状态过滤
const filteredOrders = computed(() => {
  if (currentStatus.value === 'all') {
    return orders.value
  }
  return orders.value.filter(order => order.status === currentStatus.value)
})

// 格式化日期
const formatDate = (dateString) => {
  const date = new Date(dateString)
  return new Intl.DateTimeFormat('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  }).format(date)
}

// 获取状态中文描述
const getStatusText = (status) => {
  const statusMap = {
    'pending': '待付款',
    'paid': '已付款',
    'shipped': '已发货',
    'completed': '已完成',
    'canceled': '已取消'
  }
  return statusMap[status] || status
}

onMounted(async () => {
  const hasPermission = await checkPermission()
  if (hasPermission) {
    await fetchStoreOrders()
  }
})
</script>

<template>
  <div class="store-orders-container">
    <h1>店铺订单管理</h1>

    <div v-if="loading" class="loading">
      <p>加载中...</p>
    </div>

    <div v-else-if="error" class="error-message">
      {{ error }}
    </div>

    <template v-else>
      <!-- 订单筛选 -->
      <div class="filter-controls">
        <div class="filter-label">订单状态：</div>
        <div class="filter-options">
          <button
            :class="['filter-btn', currentStatus === 'all' ? 'active' : '']"
            @click="currentStatus = 'all'"
          >
            全部订单
          </button>
          <button
            :class="['filter-btn', currentStatus === 'paid' ? 'active' : '']"
            @click="currentStatus = 'paid'"
          >
            待发货
          </button>
          <button
            :class="['filter-btn', currentStatus === 'shipped' ? 'active' : '']"
            @click="currentStatus = 'shipped'"
          >
            已发货
          </button>
          <button
            :class="['filter-btn', currentStatus === 'completed' ? 'active' : '']"
            @click="currentStatus = 'completed'"
          >
            已完成
          </button>
        </div>
      </div>

      <!-- 订单列表 -->
      <div v-if="filteredOrders.length === 0" class="no-orders">
        <p>暂无符合条件的订单</p>
      </div>

      <div v-else class="orders-list">
        <div v-for="order in filteredOrders" :key="order.id" class="order-card">
          <div class="order-header">
            <div class="order-info">
              <span class="order-number">订单号：{{ order.orderNumber }}</span>
              <span class="order-date">下单时间：{{ formatDate(order.createdAt) }}</span>
            </div>
            <div class="order-status" :class="order.status">
              {{ getStatusText(order.status) }}
            </div>
          </div>

          <div class="order-items">
            <div v-for="item in order.items" :key="item.id" class="order-item">
              <div class="item-image">
                <img :src="item.imageUrl || 'https://via.placeholder.com/60x80'" :alt="item.title" />
              </div>
              <div class="item-details">
                <h3>{{ item.title }}</h3>
                <p>{{ item.author }}</p>
                <div class="item-price-info">
                  <span>¥{{ item.price.toFixed(2) }} × {{ item.quantity }}</span>
                  <span class="item-subtotal">¥{{ item.subtotal.toFixed(2) }}</span>
                </div>
              </div>
            </div>
          </div>

          <div class="order-footer">
            <div class="customer-info">
              <p><strong>收货人：</strong>{{ order.contactName }}</p>
              <p><strong>电话：</strong>{{ order.contactPhone }}</p>
              <p><strong>地址：</strong>{{ order.address }}</p>
            </div>
            <div class="order-actions">
              <p class="total-amount">订单总额：<span>¥{{ order.totalAmount.toFixed(2) }}</span></p>
              <!-- 只有已付款状态的订单才显示发货按钮 -->
              <button
                v-if="order.status === 'paid'"
                class="btn-ship"
                @click="shipOrder(order.id)"
              >
                确认发货
              </button>
              <span v-else-if="order.status === 'shipped'" class="shipped-time">
                发货时间：{{ formatDate(order.shippedAt) }}
              </span>
            </div>
          </div>
        </div>
      </div>
    </template>
  </div>
</template>

<style scoped>
.store-orders-container {
  max-width: 1000px;
  margin: 0 auto;
  padding: 20px;
}

h1 {
  text-align: center;
  color: #ff6347;
  margin-bottom: 30px;
}

.loading, .error-message, .no-orders {
  text-align: center;
  padding: 40px;
  font-size: 18px;
}

.error-message {
  color: red;
  background-color: rgba(255, 0, 0, 0.1);
  border-radius: 8px;
}

.filter-controls {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
  background: white;
  padding: 15px;
  border-radius: 8px;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.1);
}

.filter-label {
  font-weight: bold;
  margin-right: 15px;
}

.filter-options {
  display: flex;
  gap: 10px;
}

.filter-btn {
  padding: 8px 16px;
  border: 1px solid #ddd;
  border-radius: 20px;
  background-color: white;
  cursor: pointer;
  transition: all 0.3s;
}

.filter-btn.active {
  background-color: #ff6347;
  color: white;
  border-color: #ff6347;
}

.orders-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.order-card {
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.order-header {
  display: flex;
  justify-content: space-between;
  padding: 15px;
  background-color: #f9f9f9;
  border-bottom: 1px solid #eee;
}

.order-number {
  font-weight: bold;
  margin-right: 20px;
}

.order-status {
  padding: 5px 10px;
  border-radius: 15px;
  font-weight: bold;
  font-size: 14px;
}

.order-status.pending {
  background-color: #ffeaa7;
  color: #d35400;
}

.order-status.paid {
  background-color: #d6f5d6;
  color: #27ae60;
}

.order-status.shipped {
  background-color: #cce5ff;
  color: #2980b9;
}

.order-status.completed {
  background-color: #e6e6e6;
  color: #2c3e50;
}

.order-status.canceled {
  background-color: #f8d7da;
  color: #c0392b;
}

.order-items {
  padding: 15px;
  border-bottom: 1px solid #eee;
}

.order-item {
  display: flex;
  padding: 10px 0;
  border-bottom: 1px solid #f5f5f5;
}

.order-item:last-child {
  border-bottom: none;
}

.item-image img {
  width: 60px;
  height: 80px;
  object-fit: cover;
}

.item-details {
  flex: 1;
  margin-left: 15px;
}

.item-details h3 {
  font-size: 16px;
  margin-bottom: 5px;
}

.item-price-info {
  display: flex;
  justify-content: space-between;
  margin-top: 5px;
  color: #666;
}

.item-subtotal {
  font-weight: bold;
  color: #ff6347;
}

.order-footer {
  display: flex;
  justify-content: space-between;
  padding: 15px;
  align-items: flex-end;
}

.customer-info p {
  margin-bottom: 5px;
}

.order-actions {
  text-align: right;
}

.total-amount {
  font-size: 16px;
  margin-bottom: 10px;
}

.total-amount span {
  font-weight: bold;
  color: #ff6347;
  font-size: 18px;
}

.btn-ship {
  background-color: #ff6347;
  color: white;
  border: none;
  padding: 8px 16px;
  border-radius: 4px;
  cursor: pointer;
  font-weight: bold;
  transition: all 0.3s;
}

.btn-ship:hover {
  background-color: #ff4500;
  transform: translateY(-2px);
}

.shipped-time {
  color: #666;
  font-size: 14px;
}

/* 响应式样式 */
@media (max-width: 768px) {
  .order-header, .order-footer {
    flex-direction: column;
    gap: 10px;
  }

  .order-status {
    align-self: flex-start;
  }

  .order-actions {
    text-align: left;
  }
}
</style>
