<!--
 * 这是书店的订单管理页面
 * 主要功能：
 * 1. 显示店里所有的订单
 * 2. 可以按状态筛选订单
 * 3. 处理订单发货
 * 4. 查看订单详情
 * 5. 导出订单数据
-->
<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../stores/User'
import { useOrderStore } from '../stores/Order'
import type { Order } from '../stores/Order'

const router = useRouter()
const userStore = useUserStore()
const orderStore = useOrderStore()

const orders = ref<Order[]>([])
const loading = ref(false)
const error = ref('')

// 获取店铺订单列表
const fetchStoreOrders = async () => {
  loading.value = true
  error.value = ''
  try {
    if (userStore.user?.storeId) {
      await orderStore.fetchOrdersByStore(userStore.user.storeId)
    }
  } catch (err) {
    error.value = '获取订单列表失败'
    console.error(err)
  } finally {
    loading.value = false
  }
}

// 查看订单详情
const viewOrderDetail = (orderId: string) => {
  router.push(`/store/order/${orderId}`)
}

// 更新订单状态
const updateOrderStatus = async (orderId: string, newStatus: Order['status']) => {
  try {
    const allOrders = JSON.parse(localStorage.getItem('orders') || '[]')
    const orderIndex = allOrders.findIndex((order: Order) => order.id === orderId)
    
    if (orderIndex !== -1) {
      allOrders[orderIndex].status = newStatus
      localStorage.setItem('orders', JSON.stringify(allOrders))
      await fetchStoreOrders() // 重新加载订单列表
    }
  } catch (err) {
    error.value = '更新订单状态失败'
    console.error(err)
  }
}

onMounted(() => {
  fetchStoreOrders()
})
</script>

<template>
  <div class="store-order-list-container">
    <div class="page-header">
      <h1>店铺订单管理</h1>
    </div>

    <div v-if="loading" class="loading">
      <p>加载中...</p>
    </div>

    <div v-else-if="error" class="error">
      <p>{{ error }}</p>
    </div>

    <div v-else-if="orderStore.orders.length === 0" class="no-orders">
      <p>暂无订单</p>
    </div>

    <div v-else class="orders-list">
      <div
        v-for="order in orderStore.orders"
        :key="order.id"
        class="order-card"
      >
        <div class="order-header">
          <span class="order-id">订单号：{{ order.id }}</span>
          <div class="order-actions">
            <span class="order-status" :class="order.status">{{ order.status }}</span>
            <div class="status-actions" v-if="order.status === 'pending'">
              <button 
                class="btn-complete"
                @click="updateOrderStatus(order.id, 'completed')"
              >
                完成订单
              </button>
              <button 
                class="btn-cancel"
                @click="updateOrderStatus(order.id, 'cancelled')"
              >
                取消订单
              </button>
            </div>
          </div>
        </div>
        <div class="order-info" @click="viewOrderDetail(order.id)">
          <div class="order-items">
            <div v-for="item in order.items" :key="item.bookId" class="order-item">
              <img :src="item.imageUrl" :alt="item.title" class="book-cover" />
              <div class="item-info">
                <h3>{{ item.title }}</h3>
                <p>数量：{{ item.quantity }}</p>
                <p>单价：¥{{ item.price.toFixed(2) }}</p>
              </div>
            </div>
          </div>
          <div class="order-summary">
            <div class="customer-info">
              <p><strong>买家：</strong>{{ order.contactName }}</p>
              <p><strong>联系电话：</strong>{{ order.contactPhone }}</p>
              <p><strong>收货地址：</strong>{{ order.address }}</p>
            </div>
            <div class="order-meta">
              <p class="total-amount">总计：¥{{ order.totalAmount.toFixed(2) }}</p>
              <p class="order-date">{{ new Date(order.createdAt).toLocaleString() }}</p>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.store-order-list-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.page-header {
  margin-bottom: 30px;
}

.page-header h1 {
  color: #ff6347;
  font-size: 24px;
}

.orders-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.order-card {
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  padding: 20px;
}

.order-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
  padding-bottom: 10px;
  border-bottom: 1px solid #eee;
}

.order-id {
  font-weight: bold;
  color: #666;
}

.order-actions {
  display: flex;
  align-items: center;
  gap: 15px;
}

.order-status {
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 14px;
}

.order-status.pending {
  background: #fff3cd;
  color: #856404;
}

.order-status.completed {
  background: #d4edda;
  color: #155724;
}

.order-status.cancelled {
  background: #f8d7da;
  color: #721c24;
}

.status-actions {
  display: flex;
  gap: 10px;
}

.btn-complete, .btn-cancel {
  padding: 6px 12px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  transition: background-color 0.2s;
}

.btn-complete {
  background-color: #28a745;
  color: white;
}

.btn-complete:hover {
  background-color: #218838;
}

.btn-cancel {
  background-color: #dc3545;
  color: white;
}

.btn-cancel:hover {
  background-color: #c82333;
}

.order-info {
  cursor: pointer;
}

.order-items {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.order-item {
  display: flex;
  gap: 15px;
  align-items: center;
}

.book-cover {
  width: 80px;
  height: 120px;
  object-fit: cover;
  border-radius: 4px;
}

.item-info h3 {
  margin: 0 0 8px 0;
  font-size: 16px;
}

.item-info p {
  margin: 4px 0;
  color: #666;
}

.order-summary {
  margin-top: 15px;
  padding-top: 15px;
  border-top: 1px solid #eee;
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}

.customer-info {
  flex: 1;
}

.customer-info p {
  margin: 5px 0;
  color: #666;
}

.order-meta {
  text-align: right;
}

.total-amount {
  font-size: 18px;
  font-weight: bold;
  color: #ff6347;
  margin-bottom: 5px;
}

.order-date {
  color: #666;
}

.loading, .error, .no-orders {
  text-align: center;
  padding: 40px;
  color: #666;
}

.error {
  color: #dc3545;
}
</style> 