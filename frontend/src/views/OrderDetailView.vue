<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useOrderStore } from '../stores/Order'
import { useUserStore } from '../stores/User'

const route = useRoute()
const router = useRouter()
const orderStore = useOrderStore()
const userStore = useUserStore()
const orderId = Number(route.params.id)
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
    await orderStore.fetchOrderById(orderId)
  } catch (err) {
    error.value = '获取订单详情失败'
  } finally {
    loading.value = false
  }
}

// 计算订单状态
const orderStatusText = computed(() => {
  if (!orderStore.currentOrder) return ''
  switch (orderStore.currentOrder.status) {
    case 'pending': return '待付款'
    case 'paid': return '已付款'
    case 'shipped': return '已发货'
    case 'completed': return '已完成'
    case 'canceled': return '已取消'
    default: return '未知状态'
  }
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
    const paymentUrl = await orderStore.payOrder(orderId)
    window.location.href = paymentUrl
  } catch (err) {
    error.value = '创建支付失败，请重试'
  }
}

// 取消订单
const cancelOrder = async () => {
  if (!confirm('确定要取消此订单吗？')) return

  try {
    await orderStore.cancelOrder(orderId)
    alert('订单已取消')
    loadOrderDetail() // 重新加载订单信息
  } catch (err) {
    error.value = '取消订单失败'
  }
}

// 确认收货
const confirmReceived = async () => {
  if (!confirm('确定已收到商品吗？确认后订单将完成。')) return

  try {
    await orderStore.completeOrder(orderId)
    alert('订单已完成')
    loadOrderDetail() // 重新加载订单信息
  } catch (err) {
    error.value = '操作失败'
  }
}

// 格式化日期
const formatDate = (dateString) => {
  const date = new Date(dateString)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: 'numeric',
    day: 'numeric',
    hour: 'numeric',
    minute: 'numeric'
  })
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
    <h1>订单详情</h1>

    <div v-if="loading" class="loading">
      <p>加载中...</p>
    </div>

    <div v-else-if="error" class="error-message">
      {{ error }}
    </div>

    <div v-else-if="!orderStore.currentOrder" class="not-found">
      <p>订单不存在或已被删除</p>
      <button class="btn-primary" @click="router.push('/orders')">返回订单列表</button>
    </div>

    <div v-else class="order-detail">
      <!-- 订单状态横幅 -->
      <div class="status-banner" :class="orderStore.currentOrder.status">
        <div class="status-icon">
          <i v-if="orderStore.currentOrder.status === 'pending'" class="icon-pending">⏱️</i>
          <i v-else-if="orderStore.currentOrder.status === 'paid'" class="icon-paid">💳</i>
          <i v-else-if="orderStore.currentOrder.status === 'shipped'" class="icon-shipped">🚚</i>
          <i v-else-if="orderStore.currentOrder.status === 'completed'" class="icon-completed">✅</i>
          <i v-else class="icon-canceled">❌</i>
        </div>
        <div class="status-text">
          <h2>{{ orderStatusText }}</h2>
          <p v-if="orderStore.currentOrder.status === 'pending'">请尽快完成支付</p>
          <p v-else-if="orderStore.currentOrder.status === 'paid'">商家正在处理您的订单</p>
          <p v-else-if="orderStore.currentOrder.status === 'shipped'">商品已发出，请留意物流信息</p>
          <p v-else-if="orderStore.currentOrder.status === 'completed'">感谢您的购买</p>
          <p v-else>订单已取消</p>
        </div>
      </div>

      <!-- 订单信息部分 -->
      <div class="detail-section">
        <h3>订单信息</h3>
        <div class="detail-row">
          <span class="label">订单号：</span>
          <span class="value">{{ orderStore.currentOrder.orderNumber }}</span>
        </div>
        <div class="detail-row">
          <span class="label">创建时间：</span>
          <span class="value">{{ formatDate(orderStore.currentOrder.createdAt) }}</span>
        </div>
        <div class="detail-row">
          <span class="label">支付方式：</span>
          <span class="value">{{ orderStore.currentOrder.paymentMethod === 'alipay' ? '支付宝' : '其他' }}</span>
        </div>
        <div class="detail-row total">
          <span class="label">订单总额：</span>
          <span class="value">¥{{ orderStore.currentOrder.totalAmount.toFixed(2) }}</span>
        </div>
      </div>

      <!-- 收货信息部分 -->
      <div class="detail-section">
        <h3>收货信息</h3>
        <div class="detail-row">
          <span class="label">收货人：</span>
          <span class="value">{{ orderStore.currentOrder.contactName }}</span>
        </div>
        <div class="detail-row">
          <span class="label">联系电话：</span>
          <span class="value">{{ orderStore.currentOrder.contactPhone }}</span>
        </div>
        <div class="detail-row">
          <span class="label">收货地址：</span>
          <span class="value">{{ orderStore.currentOrder.address }}</span>
        </div>
      </div>

      <!-- 物流信息部分 -->
      <div v-if="orderStore.currentOrder.status === 'shipped' || orderStore.currentOrder.status === 'completed'" class="detail-section">
        <div class="section-header" @click="showLogistics = !showLogistics">
          <h3>物流信息</h3>
          <span class="toggle-button">{{ showLogistics ? '收起' : '查看' }}</span>
        </div>
        <div v-if="showLogistics" class="logistics-info">
          <div v-for="(item, index) in logisticsInfo" :key="index" class="logistics-item">
            <div class="time-line">
              <div class="time-dot"></div>
              <div v-if="index !== logisticsInfo.length - 1" class="time-line-bar"></div>
            </div>
            <div class="logistics-content">
              <div class="logistics-time">{{ item.time }}</div>
              <div class="logistics-text">{{ item.content }}</div>
            </div>
          </div>
        </div>
      </div>

      <!-- 商品列表部分 -->
      <div class="detail-section">
        <h3>商品信息</h3>
        <div class="order-items">
          <div v-for="item in orderStore.currentOrder.items" :key="item.id" class="order-item">
            <div class="item-image">
              <img :src="item.imageUrl || 'https://via.placeholder.com/60x80'" :alt="item.title" />
            </div>
            <div class="item-details">
              <h4>{{ item.title }}</h4>
              <p>{{ item.author }}</p>
              <div class="item-price-info">
                <span>¥{{ item.price.toFixed(2) }} × {{ item.quantity }}</span>
                <span class="item-subtotal">¥{{ item.subtotal.toFixed(2) }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 订单操作部分 -->
      <div class="order-actions">
        <button v-if="orderStore.currentOrder.status === 'pending'" class="btn-primary" @click="payOrder">
          立即付款
        </button>
        <button v-if="orderStore.currentOrder.status === 'shipped'" class="btn-primary" @click="confirmReceived">
          确认收货
        </button>
        <button v-if="canCancelOrder" class="btn-secondary" @click="cancelOrder">
          取消订单
        </button>
        <button class="btn-return" @click="router.push('/orders')">
          返回订单列表
        </button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.order-detail-container {
  max-width: 900px;
  margin: 0 auto;
  padding: 20px;
}

h1 {
  text-align: center;
  color: #ff6347;
  margin-bottom: 30px;
}

.loading, .error-message, .not-found {
  text-align: center;
  padding: 40px;
  font-size: 16px;
}

.error-message {
  color: #ff6347;
  background-color: rgba(255, 99, 71, 0.1);
  border-radius: 8px;
}

.btn-primary, .btn-secondary, .btn-return {
  padding: 12px 24px;
  border-radius: 8px;
  font-weight: bold;
  cursor: pointer;
  transition: all 0.3s;
}

.btn-primary {
  background-color: #ff6347;
  color: white;
  border: none;
}

.btn-secondary {
  background-color: #f5f5f5;
  color: #333;
  border: 1px solid #ddd;
}

.btn-return {
  background-color: #fff;
  color: #333;
  border: 1px solid #ddd;
}

.order-detail {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

/* 状态横幅 */
.status-banner {
  display: flex;
  align-items: center;
  padding: 20px;
  border-radius: 8px;
  color: white;
}

.status-banner.pending {
  background-color: #ff6347;
}

.status-banner.paid {
  background-color: #2196f3;
}

.status-banner.shipped {
  background-color: #ff9800;
}

.status-banner.completed {
  background-color: #4caf50;
}

.status-banner.canceled {
  background-color: #9e9e9e;
}

.status-icon {
  font-size: 32px;
  margin-right: 20px;
}

.status-text h2 {
  margin: 0;
  font-size: 20px;
}

.status-text p {
  margin: 5px 0 0;
  font-size: 14px;
  opacity: 0.9;
}

/* 详情部分 */
.detail-section {
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  padding: 20px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  cursor: pointer;
}

.toggle-button {
  color: #ff6347;
  font-size: 14px;
}

h3 {
  margin: 0 0 15px;
  padding-bottom: 10px;
  border-bottom: 1px solid #eee;
  color: #333;
}

.detail-row {
  display: flex;
  justify-content: space-between;
  margin-bottom: 10px;
}

.detail-row.total {
  font-weight: bold;
  font-size: 16px;
  margin-top: 10px;
  padding-top: 10px;
  border-top: 1px dashed #eee;
}

.detail-row .label {
  color: #666;
}

.detail-row .value {
  text-align: right;
  color: #333;
  font-weight: 500;
}

.detail-row.total .value {
  color: #ff6347;
}

/* 物流信息 */
.logistics-info {
  margin-top: 15px;
}

.logistics-item {
  display: flex;
  margin-bottom: 15px;
}

.time-line {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-right: 15px;
}

.time-dot {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  background-color: #ff6347;
}

.time-line-bar {
  width: 2px;
  height: 40px;
  background-color: #ddd;
  margin: 5px 0;
}

.logistics-content {
  flex: 1;
}

.logistics-time {
  font-size: 12px;
  color: #999;
}

.logistics-text {
  font-size: 14px;
  color: #333;
}

/* 商品列表 */
.order-items {
  margin-top: 15px;
}

.order-item {
  display: flex;
  padding: 15px 0;
  border-bottom: 1px solid #eee;
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

.item-details h4 {
  font-size: 16px;
  margin: 0 0 5px;
}

.item-details p {
  font-size: 14px;
  color: #666;
  margin: 0 0 10px;
}

.item-price-info {
  display: flex;
  justify-content: space-between;
}

.item-subtotal {
  font-weight: bold;
  color: #ff6347;
}

/* 订单操作部分 */
.order-actions {
  display: flex;
  justify-content: center;
  gap: 15px;
  margin-top: 10px;
}

@media (max-width: 768px) {
  .status-banner {
    flex-direction: column;
    text-align: center;
  }

  .status-icon {
    margin-right: 0;
    margin-bottom: 10px;
  }

  .order-actions {
    flex-direction: column;
  }

  .btn-primary, .btn-secondary, .btn-return {
    width: 100%;
  }
}
</style>
