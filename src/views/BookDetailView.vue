<!--
 * 这是图书详情页面
 * 主要功能：
 * 1. 显示书的封面、书名、作者等信息
 * 2. 显示书的详细描述和目录
 * 3. 显示价格和库存数量
 * 4. 可以加入购物车或直接购买
 * 5. 显示其他读者的评价
-->
<script setup lang="ts">
import { onMounted, ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '../stores/User'
import { useBookStore } from '../stores/Book'
import { ROLES } from '../constants/roles'
import { useStoreStore } from '../stores/Store'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const bookStore = useBookStore()
const storeStore = useStoreStore()
const bookId = Number(route.params.id)
const quantity = ref(1)
const errorMessage = ref('')
const successMessage = ref('')
const storeId = ref(0)

// 计算属性判断用户角色与权限
const isAdmin = computed(() => userStore.user?.role === ROLES.ADMIN)
const isMerchant = computed(() => {
  if (!userStore.user || !bookStore.currentBook) return false
  return userStore.user.role === ROLES.MERCHANT &&
    storeStore.currentStore?.ownerId === userStore.user.username
})

// 删除书籍
const deleteBook = async () => {
  if (confirm('确定要删除此书籍吗？此操作不可撤销！')) {
    try {
      await bookStore.deleteBook(bookId)
      successMessage.value = '书籍已成功删除'
      setTimeout(() => {
        router.push(`/store/${storeId.value}`)
      }, 2000)
    } catch (error) {
      errorMessage.value = '删除书籍失败'
    }
  }
}

// 添加到购物车
const addToCart = () => {
  if (!bookStore.currentBook) return

  // 获取当前购物车数据
  const cartItems = JSON.parse(localStorage.getItem('cart') || '[]')
  
  // 检查商品是否已在购物车中
  const existingItemIndex = cartItems.findIndex((item: any) => item.bookId === bookStore.currentBook?.id)
  
  if (existingItemIndex !== -1) {
    // 如果商品已存在，更新数量
    cartItems[existingItemIndex].quantity += quantity.value
  } else {
    // 如果商品不存在，添加新商品
    cartItems.push({
      id: Date.now(), // 生成唯一ID
      bookId: bookStore.currentBook.id,
      title: bookStore.currentBook.title,
      price: bookStore.currentBook.price,
      quantity: quantity.value,
      cover: bookStore.currentBook.cover
    })
  }
  
  // 保存更新后的购物车数据
  localStorage.setItem('cart', JSON.stringify(cartItems))
  
  successMessage.value = `已将${quantity.value}本《${bookStore.currentBook.title}》添加到购物车`
  setTimeout(() => {
    successMessage.value = ''
  }, 3000)
}

// 立即购买
const buyNow = () => {
  // TODO: 实现立即购买功能
  router.push('/checkout')
}

// 返回店铺
const backToStore = () => {
  router.push(`/store/${storeId.value}`)
}

onMounted(async () => {
  try {
    await bookStore.fetchBookById(bookId)
    if (bookStore.currentBook) {
      storeId.value = bookStore.currentBook.storeId
      await storeStore.fetchStoreById(bookStore.currentBook.storeId)
    }
  } catch (error) {
    errorMessage.value = '获取书籍信息失败'
  }
})
</script>

<template>
  <div class="book-detail-container">
    <div v-if="bookStore.loading" class="loading">
      <p>加载中...</p>
    </div>

    <div v-else-if="errorMessage" class="error">
      <p>{{ errorMessage }}</p>
      <button class="back-button" @click="router.push('/home')">返回主页</button>
    </div>

    <div v-else-if="!bookStore.currentBook" class="no-book">
      <p>书籍不存在或已被删除</p>
      <button class="back-button" @click="router.push('/home')">返回主页</button>
    </div>

    <div v-else class="book-content">
      <div v-if="successMessage" class="success-message">
        {{ successMessage }}
      </div>

      <div class="book-header">
        <button class="back-link" @click="backToStore">返回店铺</button>
      </div>

      <div class="book-main">
        <div class="book-image">
          <img :src="bookStore.currentBook.cover" :alt="bookStore.currentBook.title" />
        </div>

        <div class="book-info">
          <h1>{{ bookStore.currentBook.title }}</h1>
          <p class="book-author">作者: {{ bookStore.currentBook.author }}</p>
          <div class="book-price-stock">
            <p class="book-price">¥{{ bookStore.currentBook.price.toFixed(2) }}</p>
            <p class="book-stock">库存: {{ bookStore.currentBook.stock }}本</p>
          </div>

          <div class="book-description">
            <h3>图书简介</h3>
            <p>{{ bookStore.currentBook.description }}</p>
          </div>

          <!-- 顾客操作区 -->
          <div v-if="userStore.user?.role === ROLES.CUSTOMER" class="customer-actions">
            <div class="quantity-control">
              <button @click="quantity > 1 ? quantity-- : null" :disabled="quantity <= 1">-</button>
              <input type="number" v-model="quantity" min="1" :max="bookStore.currentBook.stock" />
              <button @click="quantity < bookStore.currentBook.stock ? quantity++ : null" :disabled="quantity >= bookStore.currentBook.stock">+</button>
            </div>
            <p class="total-price">总价: ¥{{ (bookStore.currentBook.price * quantity).toFixed(2) }}</p>

            <div class="purchase-actions">
              <button class="cart-button" @click="addToCart" :disabled="bookStore.currentBook.stock <= 0">
                {{ bookStore.currentBook.stock > 0 ? '加入购物车' : '暂时缺货' }}
              </button>
              <button class="buy-button" @click="buyNow" :disabled="bookStore.currentBook.stock <= 0">
                {{ bookStore.currentBook.stock > 0 ? '立即购买' : '暂时缺货' }}
              </button>
            </div>
          </div>

          <!-- 商家/管理员操作区 -->
          <div v-if="isAdmin || isMerchant" class="admin-actions">
            <button class="delete-button" @click="deleteBook">删除书籍</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.book-detail-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.loading, .error, .no-book {
  text-align: center;
  padding: 40px;
  font-size: 18px;
}

.error {
  color: #ff6347;
}

.success-message {
  background-color: rgba(0, 128, 0, 0.1);
  color: green;
  padding: 15px;
  border-radius: 8px;
  margin-bottom: 20px;
  text-align: center;
}

.back-button, .back-link {
  background-color: #f5f5f5;
  color: #333;
  border: none;
  padding: 10px 20px;
  border-radius: 8px;
  margin-top: 20px;
  cursor: pointer;
  font-size: 16px;
  display: inline-block;
}

.book-header {
  margin-bottom: 30px;
}

.book-main {
  display: flex;
  gap: 30px;
}

.book-image {
  flex: 0 0 40%;
  max-width: 400px;
}

.book-image img {
  width: 100%;
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.1);
  border-radius: 8px;
}

.book-info {
  flex: 1;
}

.book-info h1 {
  font-size: 28px;
  margin-bottom: 10px;
  color: #333;
}

.book-author {
  font-size: 18px;
  color: #666;
  margin-bottom: 15px;
}

.book-price-stock {
  display: flex;
  align-items: center;
  gap: 20px;
  margin-bottom: 20px;
}

.book-price {
  font-size: 32px;
  color: #ff6347;
  font-weight: bold;
  margin: 0;
}

.book-stock {
  font-size: 16px;
  color: #666;
  margin: 0;
}

.total-price {
  font-size: 20px;
  color: #ff6347;
  font-weight: bold;
  margin: 15px 0;
}

.book-description {
  margin-top: 30px;
  margin-bottom: 30px;
}

.book-description h3 {
  font-size: 20px;
  margin-bottom: 10px;
  border-bottom: 1px solid #eee;
  padding-bottom: 10px;
}

.book-description p {
  font-size: 16px;
  line-height: 1.8;
  color: #333;
}

.customer-actions {
  margin-top: 30px;
}

.quantity-control {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
  border: 1px solid #ddd;
  border-radius: 8px;
  overflow: hidden;
  width: fit-content;
}

.quantity-control button {
  width: 40px;
  height: 40px;
  font-size: 18px;
  background: #f5f5f5;
  border: none;
  cursor: pointer;
  transition: all 0.3s;
}

.quantity-control button:disabled {
  background: #eee;
  color: #999;
  cursor: not-allowed;
}

.quantity-control button:hover:not(:disabled) {
  background: #e0e0e0;
}

.quantity-control input {
  width: 60px;
  height: 40px;
  text-align: center;
  font-size: 18px;
  border: none;
  border-left: 1px solid #ddd;
  border-right: 1px solid #ddd;
}

.quantity-control input::-webkit-inner-spin-button,
.quantity-control input::-webkit-outer-spin-button {
  -webkit-appearance: none;
  margin: 0;
}

.purchase-actions {
  display: flex;
  gap: 15px;
}

.cart-button, .buy-button {
  flex: 1;
  padding: 15px 0;
  font-size: 18px;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-weight: bold;
  transition: all 0.3s;
}

.cart-button:disabled, .buy-button:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.cart-button {
  background-color: #fff0f0;
  color: #ff6347;
  border: 1px solid #ff6347;
}

.cart-button:hover:not(:disabled) {
  background-color: #ffe0e0;
}

.buy-button {
  background-color: #ff6347;
  color: white;
}

.buy-button:hover:not(:disabled) {
  background-color: #ff4500;
}

.admin-actions {
  margin-top: 30px;
  display: flex;
  gap: 15px;
}

.delete-button {
  padding: 12px 20px;
  font-size: 16px;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  background-color: #dc3545;
  color: white;
}

@media (max-width: 768px) {
  .book-main {
    flex-direction: column;
  }

  .book-image {
    max-width: 100%;
    margin-bottom: 20px;
  }
}
</style>
