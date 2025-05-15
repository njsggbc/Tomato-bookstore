<!-- src/views/CheckoutView.vue -->
<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useCartStore } from '../stores/Cart'
import { useOrderStore } from '../stores/Order'
import { useUserStore } from '../stores/User'

const router = useRouter()
const cartStore = useCartStore()
const orderStore = useOrderStore()
const userStore = useUserStore()

const address = ref('')
const contactName = ref('')
const contactPhone = ref('')
const paymentMethod = ref('alipay') // 默认支付宝支付
const errorMessage = ref('')

// 如果用户已有信息，自动填充
onMounted(async () => {
  if (!userStore.isLoggedIn) {
    router.push('/login?redirect=/checkout')
    return
  }

  if (userStore.user) {
    contactName.value = userStore.user.username || ''
    address.value = userStore.user.address || ''
    contactPhone.value = userStore.user.phone || ''
  }

  await cartStore.fetchCartItems()

  // 如果购物车为空，则跳回购物车页面
  if (cartStore.cartItems.length === 0) {
    router.push('/cart')
  }
})

// 创建订单
const submitOrder = async () => {
  if (!address.value || !contactName.value || !contactPhone.value) {
    errorMessage.value = '请填写完整的收货信息'
    return
  }

  try {
    const orderData = {
      address: address.value,
      contactName: contactName.value,
      contactPhone: contactPhone.value,
      paymentMethod: paymentMethod.value
    }

    const order = await orderStore.createOrder(orderData)

    // 跳转到确认页面
    router.push(`/order/${order.id}/confirm`)
  } catch (err) {
    errorMessage.value = '创建订单失败，请重试'
  }
}

// 返回购物车
const returnToCart = () => {
  router.push('/cart')
}
</script>

<template>
  <div class="checkout-container">
    <h1>订单结算</h1>

    <div v-if="errorMessage" class="error-message">
      {{ errorMessage }}
    </div>

    <div class="checkout-sections">
      <div class="checkout-section">
        <h2>收货信息</h2>
        <div class="form-group">
          <label for="contactName">收货人姓名</label>
          <input
            id="contactName"
            v-model="contactName"
            type="text"
            placeholder="请输入收货人姓名"
            required
          />
        </div>

        <div class="form-group">
          <label for="contactPhone">联系电话</label>
          <input
            id="contactPhone"
            v-model="contactPhone"
            type="tel"
            placeholder="请输入联系电话"
            required
          />
        </div>

        <div class="form-group">
          <label for="address">收货地址</label>
          <textarea
            id="address"
            v-model="address"
            rows="3"
            placeholder="请输入收货地址"
            required
          ></textarea>
        </div>
      </div>

      <div class="checkout-section">
        <h2>订单商品信息</h2>
        <div class="order-items">
          <div v-for="item in cartStore.cartItems" :key="item.id" class="order-item">
            <div class="item-image">
              <img :src="item.imageUrl" :alt="item.title" />
            </div>
            <div class="item-details">
              <h3>{{ item.title }}</h3>
              <p>{{ item.author }}</p>
              <div class="item-price-info">
                <span>¥{{ item.price.toFixed(2) }} × {{ item.quantity }}</span>
                <span class="item-subtotal">¥{{ (item.price * item.quantity).toFixed(2) }}</span>
              </div>
            </div>
          </div>
        </div>

        <div class="order-summary">
          <div class="summary-row">
            <span>商品总数:</span>
            <span>{{ cartStore.totalItems }}件</span>
          </div>
          <div class="summary-row total">
            <span>订单总计:</span>
            <span>¥{{ cartStore.totalAmount.toFixed(2) }}</span>
          </div>
        </div>
      </div>

      <div class="checkout-section">
        <h2>支付方式</h2>
        <div class="payment-methods">
          <label class="payment-method">
            <input
              type="radio"
              v-model="paymentMethod"
              value="alipay"
              checked
            />
            <div class="payment-icon alipay-icon">
              <img src="../assets/alipay-icon.png" alt="支付宝" />
            </div>
            <span>支付宝</span>
          </label>
        </div>
      </div>
    </div>

    <div class="checkout-actions">
      <button class="btn-secondary" @click="returnToCart">返回购物车</button>
      <button class="btn-primary" @click="submitOrder">提交订单</button>
    </div>
  </div>
</template>

<style scoped>
.checkout-container {
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

.error-message {
  color: red;
  margin-bottom: 20px;
  padding: 14px;
  background-color: rgba(255, 0, 0, 0.1);
  border-radius: 8px;
  text-align: center;
}

.checkout-sections {
  display: flex;
  flex-direction: column;
  gap: 30px;
}

.checkout-section {
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  padding: 20px;
}

.form-group {
  margin-bottom: 15px;
}

label {
  display: block;
  margin-bottom: 8px;
  font-weight: bold;
}

input, textarea {
  width: 100%;
  padding: 12px;
  border: 1px solid #ddd;
  border-radius: 8px;
  font-size: 16px;
}

.order-items {
  margin-bottom: 20px;
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

.order-summary {
  margin-top: 20px;
}

.summary-row {
  display: flex;
  justify-content: space-between;
  margin-bottom: 10px;
}

.summary-row.total {
  font-size: 1.2em;
  font-weight: bold;
  color: #ff6347;
  padding-top: 10px;
  margin-top: 10px;
}

.payment-methods {
  display: flex;
  gap: 20px;
}

.payment-method {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 8px;
  cursor: pointer;
}

.payment-method:has(input:checked) {
  border-color: #ff6347;
  background-color: rgba(255, 99, 71, 0.1);
}

.payment-icon img {
  width: 30px;
  height: 30px;
  object-fit: contain;
}

.checkout-actions {
  display: flex;
  justify-content: space-between;
  margin-top: 30px;
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

@media (max-width: 600px) {
  .checkout-actions {
    flex-direction: column;
    gap: 10px;
  }

  .btn-primary, .btn-secondary {
    width: 100%;
  }
}
</style>
