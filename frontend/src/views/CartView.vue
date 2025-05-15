<script setup lang="ts">
import { onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useCartStore } from '../stores/Cart'
import { useUserStore } from '../stores/User'

const router = useRouter()
const cartStore = useCartStore()
const userStore = useUserStore()

const updateQuantity = (itemId, quantity) => {
  if (quantity < 1) quantity = 1
  cartStore.updateItemQuantity(itemId, quantity)
}

const removeItem = (itemId) => {
  if (confirm('确定要从购物车中删除这件商品吗？')) {
    cartStore.removeFromCart(itemId)
  }
}

const goToCheckout = () => {
  router.push('/checkout')
}

onMounted(async () => {
  if (userStore.isLoggedIn) {
    await cartStore.fetchCartItems()
  } else {
    router.push('/login?redirect=/cart')
  }
})
</script>

<template>
  <div class="cart-container">
    <h1>我的购物车</h1>

    <div v-if="!userStore.isLoggedIn" class="not-logged-in">
      <p>请先登录查看购物车</p>
      <RouterLink to="/login" class="btn-primary">去登录</RouterLink>
    </div>

    <template v-else>
      <div v-if="cartStore.loading" class="loading">
        <p>加载中...</p>
      </div>

      <div v-else-if="cartStore.error" class="error-message">
        {{ cartStore.error }}
      </div>

      <div v-else-if="cartStore.cartItems.length === 0" class="empty-cart">
        <p>购物车是空的，去添加一些好书吧！</p>
        <RouterLink to="/home" class="btn-primary">去购物</RouterLink>
      </div>

      <div v-else class="cart-items">
        <div v-for="item in cartStore.cartItems" :key="item.id" class="cart-item">
          <div class="item-image">
            <img :src="item.imageUrl" :alt="item.title" />
          </div>
          <div class="item-details">
            <h3>{{ item.title }}</h3>
            <p class="author">{{ item.author }}</p>
            <p class="price">¥{{ item.price.toFixed(2) }}</p>
          </div>
          <div class="item-actions">
            <div class="quantity-controls">
              <button @click="updateQuantity(item.id, item.quantity - 1)">-</button>
              <span>{{ item.quantity }}</span>
              <button @click="updateQuantity(item.id, item.quantity + 1)">+</button>
            </div>
            <button class="remove-btn" @click="removeItem(item.id)">删除</button>
          </div>
          <div class="item-total">
            <p>小计: ¥{{ (item.price * item.quantity).toFixed(2) }}</p>
          </div>
        </div>

        <div class="cart-summary">
          <div class="summary-row">
            <span>商品总数:</span>
            <span>{{ cartStore.totalItems }} 件</span>
          </div>
          <div class="summary-row total">
            <span>总计:</span>
            <span>¥{{ cartStore.totalAmount.toFixed(2) }}</span>
          </div>
          <div class="checkout-actions">
            <button class="btn-clear" @click="cartStore.clearCart()">清空购物车</button>
            <button class="btn-checkout" @click="goToCheckout">去结算</button>
          </div>
        </div>
      </div>
    </template>
  </div>
</template>

<style scoped>
/* CSS样式省略，保持与网站风格一致 */
.cart-container {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
}

h1 {
  text-align: center;
  color: #ff6347;
  margin-bottom: 30px;
}

.cart-item {
  display: flex;
  align-items: center;
  padding: 15px;
  margin-bottom: 15px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.1);
}

.item-image img {
  width: 80px;
  height: 120px;
  object-fit: cover;
}

.item-details {
  flex: 1;
  margin-left: 15px;
}

.item-total {
  font-weight: bold;
  color: #ff6347;
}

.quantity-controls {
  display: flex;
  align-items: center;
  margin-bottom: 10px;
}

.quantity-controls button {
  width: 30px;
  height: 30px;
  background: #f5f5f5;
  border: 1px solid #ddd;
  border-radius: 4px;
}

.quantity-controls span {
  margin: 0 10px;
  width: 30px;
  text-align: center;
}

.cart-summary {
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.1);
  padding: 20px;
  margin-top: 30px;
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
  border-top: 1px solid #eee;
  padding-top: 10px;
  margin-top: 10px;
}

.checkout-actions {
  display: flex;
  justify-content: space-between;
  margin-top: 20px;
}

.btn-checkout {
  background-color: #ff6347;
  color: white;
  padding: 12px 24px;
  border: none;
  border-radius: 8px;
  font-weight: bold;
  cursor: pointer;
}

.btn-clear {
  background-color: #f5f5f5;
  color: #333;
  padding: 12px 24px;
  border: 1px solid #ddd;
  border-radius: 8px;
  cursor: pointer;
}
</style>
