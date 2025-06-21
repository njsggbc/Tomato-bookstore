<!--
 * 这是我的订单列表页面
 * 主要功能：
 * 1. 显示我所有的订单
 * 2. 可以按状态筛选（待付款、已发货等）
 * 3. 点击可以看订单详情
 * 4. 可以取消还没发货的订单
 * 5. 收到货后可以确认收货
-->
<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useOrderStore } from '../stores/Order'

const router = useRouter()
const orderStore = useOrderStore()

interface OrderItem {
  id: string
  bookId: string
  title: string
  author: string
  price: number
  quantity: number
  imageUrl: string
  storeId: number
}

interface Order {
  id: string
  userId: string
  storeId: number
  items: OrderItem[]
  totalAmount: number
  status: 'pending' | 'paid' | 'shipped' | 'completed' | 'cancelled'
  createdAt: string
  updatedAt: string
  address?: string
  contactPhone?: string
  contactName?: string
  paymentMethod?: string
}

const orders = ref<Order[]>([])

// 查看订单详情
const viewOrderDetail = (orderId: string) => {
  router.push(`/order/${orderId}`)
}

// 删除订单
const deleteOrder = async (orderId: string) => {
  if (confirm('确定要删除该订单吗？删除后无法恢复。')) {
    await orderStore.deleteOrder(orderId)
    await orderStore.fetchOrders()
  }
}

onMounted(() => {
  orderStore.fetchOrders()
})

const getStatusText = (status: Order['status']) => {
  const statusMap = {
    pending: '待付款',
    paid: '已付款',
    shipped: '已发货',
    completed: '已完成',
    cancelled: '已取消'
  }
  return statusMap[status]
}

const getStatusClass = (status: Order['status']) => {
  const classMap = {
    pending: 'status-pending',
    paid: 'status-paid',
    shipped: 'status-shipped',
    completed: 'status-completed',
    cancelled: 'status-cancelled'
  }
  return classMap[status]
}

const formatDate = (dateString: string) => {
  const date = new Date(dateString)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}
</script>

<template>
  <div class="orders-container">
    <h1>我的订单</h1>

    <div v-if="orderStore.loading" class="loading">
      <p>加载中...</p>
    </div>

    <div v-else-if="orderStore.error" class="error">
      <p>{{ orderStore.error }}</p>
    </div>

    <div v-else-if="orderStore.orders.length === 0" class="no-orders">
      <p>暂无订单记录</p>
    </div>

    <div v-else class="orders-list">
      <div
        v-for="order in orderStore.orders"
        :key="order.id"
        class="order-card"
        @click="viewOrderDetail(order.id)"
      >
        <div class="order-header">
          <div class="order-info">
            <span class="order-id">订单号：{{ order.id }}</span>
            <span class="order-date">{{ formatDate(order.createdAt) }}</span>
          </div>
          <span class="order-status" :class="order.status">
            {{ getStatusText(order.status) }}
          </span>
        </div>

        <div class="order-items">
          <div v-for="item in order.items" :key="item.id" class="order-item">
            <img :src="item.imageUrl" :alt="item.title" class="book-cover" />
            <div class="item-details">
              <h3>{{ item.title }}</h3>
              <p class="price">¥{{ item.price }}</p>
              <p class="quantity">数量：{{ item.quantity }}</p>
            </div>
            <p class="subtotal">¥{{ item.price * item.quantity }}</p>
          </div>
        </div>

        <div class="order-footer">
          <div class="total-amount">
            <span>总计：</span>
            <span class="amount">¥{{ order.totalAmount }}</span>
          </div>
          <button class="delete-button" @click.stop="deleteOrder(order.id)">删除订单</button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.orders-container {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
}

h1 {
  text-align: center;
  color: #ff6347;
  margin-bottom: 30px;
}

.loading, .error, .no-orders {
  text-align: center;
  padding: 40px;
  background: #f9f9f9;
  border-radius: 8px;
}

.loading p, .error p, .no-orders p {
  margin-bottom: 20px;
  font-size: 18px;
  color: #666;
}

.orders-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.order-card {
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  overflow: hidden;
  cursor: pointer;
  transition: transform 0.3s, box-shadow 0.3s;
}

.order-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.order-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px 20px;
  background: #f9f9f9;
  border-bottom: 1px solid #eee;
}

.order-info {
  display: flex;
  gap: 20px;
}

.order-id {
  font-weight: bold;
}

.order-date {
  color: #666;
}

.order-status {
  padding: 4px 12px;
  border-radius: 4px;
  font-size: 14px;
}

.status-pending {
  background: #fff3cd;
  color: #856404;
}

.status-paid {
  background: #cce5ff;
  color: #004085;
}

.status-shipped {
  background: #d4edda;
  color: #155724;
}

.status-completed {
  background: #d1e7dd;
  color: #0f5132;
}

.status-cancelled {
  background: #f8d7da;
  color: #721c24;
}

.order-items {
  padding: 20px;
}

.order-item {
  display: flex;
  align-items: center;
  padding: 10px 0;
  border-bottom: 1px solid #eee;
}

.order-item:last-child {
  border-bottom: none;
}

.book-cover {
  width: 60px;
  height: 90px;
  object-fit: cover;
  border-radius: 4px;
  margin-right: 15px;
}

.item-details {
  flex: 1;
}

.item-details h3 {
  margin: 0 0 5px 0;
  font-size: 16px;
}

.price {
  color: #ff6347;
  margin: 0;
}

.quantity {
  color: #666;
  margin: 5px 0 0 0;
  font-size: 14px;
}

.subtotal {
  font-weight: bold;
  margin: 0 0 0 20px;
  min-width: 80px;
  text-align: right;
}

.order-footer {
  padding: 15px 20px;
  background: #f9f9f9;
  border-top: 1px solid #eee;
  display: flex;
  justify-content: flex-end;
}

.total-amount {
  font-size: 16px;
}

.amount {
  color: #ff6347;
  font-weight: bold;
  font-size: 20px;
  margin-left: 10px;
}

.btn-primary {
  display: inline-block;
  padding: 12px 24px;
  background: #ff6347;
  color: white;
  text-decoration: none;
  border-radius: 4px;
  transition: background-color 0.3s;
}

.btn-primary:hover {
  background: #ff4500;
}

.delete-button {
  background: #fff0f0;
  color: #ff3b3b;
  border: 1px solid #ffb3b3;
  border-radius: 4px;
  padding: 6px 14px;
  margin-left: 16px;
  cursor: pointer;
  transition: background 0.2s;
}

.delete-button:hover {
  background: #ffb3b3;
  color: #fff;
}
</style> 