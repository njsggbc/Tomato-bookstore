<!--
 * 这是支付结果页面
 * 主要功能：
 * 1. 显示支付是否成功
 * 2. 显示订单号
 * 3. 显示支付了多少钱
 * 4. 可以查看订单详情
 * 5. 可以返回首页继续购物
-->
<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'

const router = useRouter()
const route = useRoute()

const isSuccess = ref(Boolean(route.query.success === 'true'))
const orderId = ref(route.query.orderId as string || '')
const amount = ref(route.query.amount as string || '')
const payTime = ref(new Date().toLocaleString())
const errorMessage = ref(route.query.error as string || '')

const goToHome = () => {
  router.push('/home')
}

const viewOrders = () => {
  router.push('/orders')
}
</script>

<template>
  <div class="payment-result-container">
    <div v-if="isSuccess" class="success-result">
      <div class="result-icon">
        <svg
          xmlns="http://www.w3.org/2000/svg"
          width="64"
          height="64"
          viewBox="0 0 24 24"
          fill="none"
          stroke="currentColor"
          stroke-width="2"
          stroke-linecap="round"
          stroke-linejoin="round"
        >
          <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"></path>
          <polyline points="22 4 12 14.01 9 11.01"></polyline>
        </svg>
      </div>

      <h1>支付成功</h1>
      <div class="order-info">
        <div class="info-item">
          <span class="label">订单编号:</span>
          <span class="value">{{ orderId }}</span>
        </div>
        <div class="info-item">
          <span class="label">支付金额:</span>
          <span class="value price">¥{{ amount }}</span>
        </div>
        <div class="info-item">
          <span class="label">支付时间:</span>
          <span class="value">{{ payTime }}</span>
        </div>
      </div>

      <p class="success-message">您的订单已支付成功，我们将尽快为您安排发货！</p>

      <div class="action-buttons">
        <button class="btn-primary" @click="viewOrders">查看订单</button>
        <button class="btn-secondary" @click="goToHome">继续购物</button>
      </div>
    </div>

    <div v-else class="fail-result">
      <div class="result-icon fail">
        <svg
          xmlns="http://www.w3.org/2000/svg"
          width="64"
          height="64"
          viewBox="0 0 24 24"
          fill="none"
          stroke="currentColor"
          stroke-width="2"
          stroke-linecap="round"
          stroke-linejoin="round"
        >
          <circle cx="12" cy="12" r="10"></circle>
          <line x1="15" y1="9" x2="9" y2="15"></line>
          <line x1="9" y1="9" x2="15" y2="15"></line>
        </svg>
      </div>

      <h1>支付失败</h1>
      <p class="error-message">{{ errorMessage || '支付过程中发生错误，请稍后重试' }}</p>

      <div class="action-buttons">
        <button class="btn-primary" @click="router.push('/checkout')">重新支付</button>
        <button class="btn-secondary" @click="goToHome">返回首页</button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.payment-result-container {
  max-width: 800px;
  margin: 0 auto;
  padding: 40px 20px;
  text-align: center;
}

.result-icon {
  margin-bottom: 20px;
  color: #4caf50;
}

.result-icon.fail {
  color: #ff6347;
}

h1 {
  font-size: 28px;
  margin-bottom: 30px;
  color: #333;
}

.order-info {
  background-color: #f9f9f9;
  border-radius: 10px;
  padding: 20px;
  margin-bottom: 30px;
}

.info-item {
  display: flex;
  justify-content: space-between;
  padding: 10px 0;
  border-bottom: 1px solid #eee;
}

.info-item:last-child {
  border-bottom: none;
}

.label {
  font-weight: bold;
  color: #666;
}

.value {
  color: #333;
}

.value.price {
  font-weight: bold;
  color: #ff6347;
  font-size: 18px;
}

.success-message {
  font-size: 16px;
  color: #4caf50;
  margin-bottom: 30px;
}

.error-message {
  font-size: 16px;
  color: #ff6347;
  margin-bottom: 30px;
}

.action-buttons {
  display: flex;
  justify-content: center;
  gap: 20px;
}

.btn-primary, .btn-secondary {
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

.btn-primary:hover {
  background-color: #ff4500;
  transform: translateY(-2px);
}

.btn-secondary {
  background-color: #f5f5f5;
  color: #333;
  border: 1px solid #ddd;
}

.btn-secondary:hover {
  background-color: #e5e5e5;
  transform: translateY(-2px);
}

@media (max-width: 768px) {
  .payment-result-container {
    padding: 20px;
  }

  .action-buttons {
    flex-direction: column;
    gap: 10px;
  }

  .btn-primary, .btn-secondary {
    width: 100%;
  }
}
</style>
