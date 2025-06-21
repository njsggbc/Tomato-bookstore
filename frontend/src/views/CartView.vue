<!--
 * 这是购物车页面
 * 主要功能：
 * 1. 显示已选中的商品列表
 * 2. 可以修改每本书的数量
 * 3. 可以删除不需要的书
 * 4. 显示总价和优惠信息
 * 5. 点击结算进入支付页面
-->
<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'

interface CartItem {
  id: number
  bookId: number
  title: string
  price: number
  quantity: number
  cover: string
}

const router = useRouter()
const cartItems = ref<CartItem[]>([])
const totalAmount = ref(0)

onMounted(() => {
  // 从本地存储获取购物车数据
  const storedCart = localStorage.getItem('cart')
  if (storedCart) {
    cartItems.value = JSON.parse(storedCart)
    calculateTotal()
  }
})

const calculateTotal = () => {
  totalAmount.value = cartItems.value.reduce((total, item) => {
    return total + (item.price * item.quantity)
  }, 0)
}

const updateQuantity = (item: CartItem, delta: number) => {
  const index = cartItems.value.findIndex(i => i.id === item.id)
  if (index !== -1) {
    const newQuantity = item.quantity + delta
    if (newQuantity > 0) {
      cartItems.value[index].quantity = newQuantity
      saveCart()
      calculateTotal()
    }
  }
}

const removeItem = (itemId: number) => {
  cartItems.value = cartItems.value.filter(item => item.id !== itemId)
  saveCart()
  calculateTotal()
}

const saveCart = () => {
  localStorage.setItem('cart', JSON.stringify(cartItems.value))
}

const checkout = () => {
  if (cartItems.value.length === 0) {
    alert('购物车为空')
    return
  }
  // 跳转到结账页面
  router.push('/checkout')
}
</script>

<template>
  <div class="cart-container">
    <h1>我的购物车</h1>

    <div v-if="cartItems.length === 0" class="empty-cart">
      <p>购物车是空的</p>
      <RouterLink to="/home" class="btn-primary">去购物</RouterLink>
    </div>

    <template v-else>
      <div class="cart-items">
        <div v-for="item in cartItems" :key="item.id" class="cart-item">
          <img :src="item.cover" :alt="item.title" class="book-cover" />
          <div class="item-details">
            <h3>{{ item.title }}</h3>
            <p class="price">¥{{ item.price }}</p>
          </div>
          <div class="quantity-controls">
            <button @click="updateQuantity(item, -1)">-</button>
            <span>{{ item.quantity }}</span>
            <button @click="updateQuantity(item, 1)">+</button>
          </div>
          <p class="subtotal">¥{{ item.price * item.quantity }}</p>
          <button class="remove-btn" @click="removeItem(item.id)">删除</button>
        </div>
      </div>

      <div class="cart-summary">
        <div class="total">
          <span>总计：</span>
          <span class="total-amount">¥{{ totalAmount }}</span>
        </div>
        <button class="checkout-btn" @click="checkout">结算</button>
      </div>
    </template>
  </div>
</template>

<style scoped>
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

.empty-cart {
  text-align: center;
  padding: 40px;
  background: #f9f9f9;
  border-radius: 8px;
}

.empty-cart p {
  margin-bottom: 20px;
  font-size: 18px;
  color: #666;
}

.cart-items {
  margin-bottom: 30px;
}

.cart-item {
  display: flex;
  align-items: center;
  padding: 20px;
  background: white;
  border-radius: 8px;
  margin-bottom: 10px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.book-cover {
  width: 80px;
  height: 120px;
  object-fit: cover;
  border-radius: 4px;
  margin-right: 20px;
}

.item-details {
  flex: 1;
}

.item-details h3 {
  margin: 0 0 10px 0;
  font-size: 18px;
}

.price {
  color: #ff6347;
  font-weight: bold;
}

.quantity-controls {
  display: flex;
  align-items: center;
  gap: 10px;
  margin: 0 20px;
}

.quantity-controls button {
  width: 30px;
  height: 30px;
  border: 1px solid #ddd;
  background: white;
  border-radius: 4px;
  cursor: pointer;
}

.quantity-controls button:hover {
  background: #f5f5f5;
}

.subtotal {
  font-weight: bold;
  margin: 0 20px;
  min-width: 80px;
  text-align: right;
}

.remove-btn {
  padding: 8px 15px;
  background: #ff4444;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

.remove-btn:hover {
  background: #cc0000;
}

.cart-summary {
  background: white;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.total {
  font-size: 18px;
}

.total-amount {
  color: #ff6347;
  font-weight: bold;
  font-size: 24px;
  margin-left: 10px;
}

.checkout-btn {
  padding: 12px 30px;
  background: #ff6347;
  color: white;
  border: none;
  border-radius: 4px;
  font-size: 18px;
  cursor: pointer;
  transition: background-color 0.3s;
}

.checkout-btn:hover {
  background: #ff4500;
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
</style>
