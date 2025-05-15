<!-- src/views/OrderConfirmView.vue -->
<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useOrderStore } from '../stores/Order'

const route = useRoute()
const router = useRouter()
const orderStore = useOrderStore()
const orderId = Number(route.params.id)
const loading = ref(true)
const error = ref('')

onMounted(async () => {
  try {
    await orderStore.fetchOrderById(orderId)
    loading.value = false
  } catch (err) {
    error.value = '获取订单信息失败'
    loading.value = false
  }
})

// 支付订单
const payOrder = async () => {
  try {
    const paymentUrl = await orderStore.payOrder(orderId)
    // 重定向到支付页面
    window.location.href = paymentUrl
  } catch (err) {
    error.value = '创建支付失败，请重试'
  }
}

// 取消订单
const cancelOrder = async () => {
  if (confirm('确定要取消此订单吗？')) {
    try {
      await orderStore.cancelOrder(orderId)
      alert('订单已取消')
      router.push('/orders')
    } catch (err) {
      error.value = '取消订单失败'
    }
  }
}
</script>

<template>
  <div class="order-confirm-container">
    <h1>订单确认</h1>

    <div v-if="loading" class="loading">
      <p>加载中...</p>
    </div>

    <div v-else-if="error" class="error-message">
      {{ error }}
    </div>

    <div v-else-if="!orderStore.currentOrder" class="not-found">
      <p>订单不存在或已被删除</p>
      <button class="btn-primary" @click="router.push('/home')">返回主页</button>
    </div>

    <div v-else class="order-details">
      <div class="order-header">
        <div class="order-number">
          <span>订单号：{{ orderStore.currentOrder.orderNumber }}</span>
        </div>
        <div class="order-status">
          <span :class="orderStore.currentOrder.status">{{ orderStore.currentOrder.status === 'pending' ? '待付款' :
            orderStore.currentOrder.status === 'paid' ? '已支付' :
              orderStore.currentOrder.status === 'shipped' ? '已发货' :
                orderStore.currentOrder.status === 'completed' ? '已完成' : '已取消' }}</span>
        </div>
      </div>

      <div class="order-section">
        <h2>收货信息</h2>
        <div class="info-group">
          <p><strong>收货人：</strong>{{ orderStore.currentOrder.contactName }}</p>
          <p><strong>联系电话：</strong>{{ orderStore.currentOrder.contactPhone }}</p>
          <p><strong>收货地址：</strong>{{ orderStore.currentOrder.address }}</p>
        </div>
      </div>

      <div class="order-section">
        <h2>商品信息</h2>
        <div class="order-items">
          <div v-for="item in orderStore.currentOrder.items" :key="item.id" class="order-item">
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
      </div>

      <div class="order-section">
        <h2>支付信息</h2>
        <div class="payment-details">
          <p><strong>支付方式：</strong>{{ orderStore.currentOrder.paymentMethod === 'alipay' ? '支付宝' : '其他' }}</p>
          <p><strong>订单总额：</strong>¥{{ orderStore.currentOrder.totalAmount.toFixed(2) }}</p>
        </div>
      </div>

      <div v-if="orderStore.currentOrder.status === 'pending'" class="order-actions">
        <button class="btn-secondary" @click="cancelOrder">取消订单</button>
        <button class="btn-primary" @click="payOrder">立即付款</button>
      </div>

      <div v-else-if="orderStore.currentOrder.status === 'paid'" class="order-message success">
        <p>订单已支付成功，请等待发货</p>
      </div>

      <div v-else-if="orderStore.currentOrder.status === 'canceled'" class="order-message warning">
        <p>订单已取消</p>
      </div>
    </div>
  </div>
</template>

<style scoped>
.order-confirm-container {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
}

h1 {
  text-align: center;
  color: #ff6347;
  margin-bottom: 30px;
}

h2 {
  color: #333;
  border-bottom: 1px solid #eee;
  padding-bottom: 10px;
  margin-bottom: 15px;
}

.loading, .error-message, .not-found {
  text-align: center;
  padding: 40px;
  font-size: 18px;
}

.error-message {
  color: red;
  background-color: rgba(255, 0, 0, 0.1);
  border-radius: 8px;
}

.order-details {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.order-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.order-status .pending {
  color: #ff6347;
}

.order-status .paid {
  color: #28a745;
}

.order-status .shipped {
  color: #007bff;
}

.order-status .completed {
  color: #28a745;
}

.order-status .canceled {
  color: #6c757d;
}

.order-section {
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  padding: 20px;
}

.info-group p {
  margin-bottom: 8px;
}

.order-items {
  margin-top: 10px;
}

.order-item {
  display: flex;
  padding: 10px 0;
  border-bottom: 1px solid #eee;
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

.item-price-info {
  display: flex;
  justify-content: space-between;
  margin-top: 10px;
}

.item-subtotal {
  font-weight: bold;
  color: #ff6347;
}

.order-actions {
  display: flex;
  justify-content: space-between;
  margin-top: 20px;
}

.btn-primary, .btn-secondary {
  padding: 12px 24px;
  border-radius: 8px;
  font-weight: bold;
  cursor: pointer;
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

.order-message {
  text-align: center;
  padding: 20px;
  border-radius: 8px;
  margin-top: 20px;
}

.success {
  background-color: rgba(40, 167, 69, 0.1);
  color: #28a745;
}

.warning {
  background-color: rgba(108, 117, 125, 0.1);
  color: #6c757d;
}

@media (max-width: 600px) {
  .order-header {
    flex-direction: column;
    gap: 10px;
  }

  .order-actions {
    flex-direction: column;
    gap: 10px;
  }

  .btn-primary, .btn-secondary {
    width: 100%;
  }
}
</style>
