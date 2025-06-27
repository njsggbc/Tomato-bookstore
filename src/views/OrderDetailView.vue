<!--
 * 这是订单详情页面
 * 主要功能：
 * 1. 显示订单里的所有商品
 * 2. 显示订单状态（待付款、已发货等）
 * 3. 显示物流信息
 * 4. 可以取消未发货的订单
 * 5. 收到货后可以评价
-->
<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useOrderStore } from '@/stores/Order'
import { useUserStore } from '@/stores/User'

const route = useRoute()
const router = useRouter()
const orderStore = useOrderStore()
const userStore = useUserStore()
const orderId = route.params.id as string
const loading = ref(true)
const error = ref('')
const showLogistics = ref(false)
const logisticsInfo = ref([
  { time: '2023-05-20 14:30:00', content: '包裹已签收，签收人：门卫' },
  { time: '2023-05-19 09:15:00', content: '包裹已到达派送点，派件员正在派件' },
  { time: '2023-05-18 22:40:00', content: '包裹已到达【北京市海淀区中关村营业部】' },
  { time: '2023-05-17 18:20:00', content: '包裹已从【上海中转中心】发出' },
  { time: '2023-05-17 10:05:00', content: '卖家已发货' },
])

// 加载订单详情
const loadOrderDetail = async () => {
  loading.value = true
  error.value = ''
  try {
    await orderStore.fetchOrderDetail(orderId)
  } catch (err) {
    error.value = err instanceof Error ? err.message : '获取订单详情失败'
    if (error.value === '订单不存在') {
      router.push('/orders')
    }
  } finally {
    loading.value = false
  }
}

// 计算订单状态
const orderStatusText = computed(() => {
  if (!orderStore.currentOrder) return ''
  const statusMap = {
    pending: '待付款',
    paid: '已付款',
    shipped: '已发货',
    completed: '已完成',
    cancelled: '已取消'
  }
  return statusMap[orderStore.currentOrder.status] || '未知状态'
})

// 是否可以取消订单
const canCancelOrder = computed(() => {
  if (!orderStore.currentOrder) return false
  // 只有待付款或已付款但未发货的订单可以取消
  return ['pending', 'paid'].includes(orderStore.currentOrder.status)
})

// 支付订单
const payOrder = async () => {
  try {
    await orderStore.payOrder()
    await loadOrderDetail()
  } catch (err) {
    error.value = '支付失败，请重试'
  }
}

// 取消订单
const cancelOrder = async () => {
  if (!confirm('确定要取消此订单吗？')) return

  try {
    await orderStore.cancelOrder(orderId)
    await loadOrderDetail()
  } catch (err) {
    error.value = '取消订单失败'
  }
}

// 确认收货
const confirmReceived = async () => {
  if (!confirm('确定已收到商品吗？确认后订单将完成。')) return

  try {
    await orderStore.confirmReceived(orderId)
    await loadOrderDetail()
  } catch (err) {
    error.value = '确认收货失败'
  }
}

// 格式化日期
const formatDate = (dateString: string) => {
  const date = new Date(dateString)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: 'numeric',
    day: 'numeric',
    hour: 'numeric',
    minute: 'numeric'
  })
}

// 返回订单列表
const goBack = () => {
  router.push('/orders')
}

onMounted(() => {
  if (userStore.isLoggedIn) {
    loadOrderDetail()
  } else {
    router.push('/login?redirect=' + route.fullPath)
  }
})
</script>

<template>
  <div class="order-detail-container">
    <div class="header">
      <button class="back-button" @click="goBack">
        <i class="fas fa-arrow-left"></i> 返回
      </button>
      <h1>订单详情</h1>
    </div>

    <div v-if="loading" class="loading">
      <p>加载中...</p>
    </div>

    <div v-else-if="error" class="error">
      <p>{{ error }}</p>
    </div>

    <div v-else-if="!orderStore.currentOrder" class="no-order">
      <p>订单不存在或已被删除</p>
    </div>

    <div v-else class="order-content">
      <div class="order-info">
        <div class="info-item">
          <span class="label">订单编号：</span>
          <span class="value">{{ orderStore.currentOrder.id }}</span>
        </div>
        <div class="info-item">
          <span class="label">创建时间：</span>
          <span class="value">{{ formatDate(orderStore.currentOrder.createdAt) }}</span>
        </div>
        <div class="info-item">
          <span class="label">订单状态：</span>
          <span class="value status" :class="orderStore.currentOrder.status">
            {{ orderStatusText }}
          </span>
        </div>
        <div class="info-item">
          <span class="label">总金额：</span>
          <span class="value price">¥{{ orderStore.currentOrder.totalAmount.toFixed(2) }}</span>
        </div>
        <div class="action-buttons">
          <template v-if="orderStore.currentOrder.status === 'pending'">
            <button class="pay-button" @click="payOrder">立即支付</button>
            <button class="cancel-button" @click="cancelOrder">取消订单</button>
          </template>
          <template v-if="orderStore.currentOrder.status === 'shipped'">
            <button class="confirm-button" @click="confirmReceived">确认收货</button>
          </template>
        </div>
      </div>

      <div class="order-items">
        <h2>订单商品</h2>
        <div class="items-list">
          <div v-for="item in orderStore.currentOrder.items" :key="item.id" class="item-card">
            <div class="item-image">
              <img :src="item.imageUrl" :alt="item.title" />
            </div>
            <div class="item-info">
              <h3>{{ item.title }}</h3>
              <p class="author">{{ item.author }}</p>
              <div class="price-qty">
                <span class="price">¥{{ item.price.toFixed(2) }}</span>
                <span class="quantity">x{{ item.quantity }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.order-detail-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.header {
  display: flex;
  align-items: center;
  margin-bottom: 30px;
  gap: 20px;
}

.back-button {
  background: none;
  border: none;
  color: #666;
  font-size: 16px;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px;
  border-radius: 4px;
  transition: background-color 0.3s;
}

.back-button:hover {
  background-color: #f5f5f5;
}

h1 {
  color: #333;
  font-size: 28px;
  margin: 0;
}

.loading, .error, .no-order {
  text-align: center;
  padding: 40px;
  font-size: 18px;
}

.error {
  color: #ff6347;
}

.order-content {
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.order-info {
  padding: 20px;
  background: #f8f9fa;
  border-bottom: 1px solid #eee;
}

.info-item {
  display: flex;
  margin-bottom: 12px;
}

.info-item:last-child {
  margin-bottom: 0;
}

.label {
  color: #666;
  width: 100px;
}

.value {
  color: #333;
  font-weight: 500;
}

.value.status {
  padding: 4px 12px;
  border-radius: 4px;
  font-size: 14px;
}

.value.status.pending {
  background-color: #fff3cd;
  color: #856404;
}

.value.status.completed {
  background-color: #d4edda;
  color: #155724;
}

.value.status.cancelled {
  background-color: #f8d7da;
  color: #721c24;
}

.value.price {
  color: #ff6347;
  font-size: 18px;
}

.order-items {
  padding: 20px;
}

h2 {
  color: #333;
  font-size: 20px;
  margin-bottom: 20px;
}

.items-list {
  display: grid;
  gap: 20px;
}

.item-card {
  display: flex;
  gap: 20px;
  padding: 15px;
  background: #f8f9fa;
  border-radius: 8px;
}

.item-image img {
  width: 100px;
  height: 150px;
  object-fit: cover;
  border-radius: 4px;
}

.item-info {
  flex: 1;
}

.item-info h3 {
  font-size: 18px;
  color: #333;
  margin-bottom: 8px;
}

.author {
  color: #666;
  font-size: 14px;
  margin-bottom: 12px;
}

.price-qty {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.price {
  color: #ff6347;
  font-size: 18px;
  font-weight: bold;
}

.quantity {
  color: #666;
  font-size: 16px;
}

.action-buttons {
  margin-top: 20px;
  display: flex;
  gap: 15px;
}

.pay-button, .cancel-button, .confirm-button {
  padding: 10px 20px;
  border: none;
  border-radius: 6px;
  font-size: 16px;
  font-weight: bold;
  cursor: pointer;
  transition: background-color 0.3s;
}

.pay-button {
  background-color: #ff6347;
  color: white;
}

.pay-button:hover {
  background-color: #ff4500;
}

.cancel-button {
  background-color: #f8f9fa;
  color: #666;
  border: 1px solid #ddd;
}

.cancel-button:hover {
  background-color: #e9ecef;
}

.confirm-button {
  background-color: #28a745;
  color: white;
}

.confirm-button:hover {
  background-color: #218838;
}

@media (max-width: 768px) {
  .header {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }

  .info-item {
    flex-direction: column;
    gap: 4px;
  }

  .label {
    width: auto;
  }

  .item-card {
    flex-direction: column;
    align-items: center;
    text-align: center;
  }

  .price-qty {
    justify-content: center;
    gap: 20px;
  }
}
</style>
