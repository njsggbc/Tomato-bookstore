<script setup lang="ts">
import { onMounted, ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useStoreStore } from '../stores/Store'
import { useUserStore } from '../stores/User'
import {useBookStore} from "../stores/Book.ts";
import { ROLES } from '../constants/roles'

const route = useRoute()
const router = useRouter()
const storeStore = useStoreStore()
const bookStore = useBookStore()
const userStore = useUserStore()
const storeId = Number(route.params.id)

// 计算属性判断用户角色
const isAdmin = computed(() => userStore.user?.role === ROLES.ADMIN)
const isMerchant = computed(() =>
  userStore.user?.role === ROLES.MERCHANT &&
  storeStore.currentStore?.ownerId === userStore.user?.username
)

// 删除店铺
const deleteStore = async () => {
  if (confirm('确定要删除此店铺吗？此操作不可撤销！')) {
    try {
      await storeStore.deleteStore(storeId)
      router.push('/home')
    } catch (error) {
      console.error('删除店铺失败', error)
    }
  }
}

// 前往创建书籍页面
const goToCreateBook = () => {
  router.push(`/store/${storeId}/book/create`)
}

// 查看书籍详情
const viewBookDetail = (bookId) => {
  router.push(`/book/${bookId}`)
}

onMounted(async () => {
  await storeStore.fetchStoreById(storeId)
  await storeStore.fetchBooksByStore(storeId)
})
</script>

<template>
  <div class="store-container">
    <div v-if="storeStore.loading" class="loading">
      <p>加载中...</p>
    </div>

    <div v-else-if="storeStore.error" class="error">
      <p>{{ storeStore.error }}</p>
    </div>

    <div v-else-if="!storeStore.currentStore" class="no-store">
      <p>店铺不存在或已被删除</p>
      <button class="back-button" @click="router.push('/home')">返回主页</button>
    </div>

    <div v-else class="store-content">
      <div class="store-header">
        <div class="store-info">
          <h1>{{ storeStore.currentStore.name }}</h1>
          <div class="store-rating">
            <span class="stars">★★★★★</span>
            <span class="rating-value">{{ storeStore.currentStore.rating }}</span>
          </div>
          <p class="store-description">{{ storeStore.currentStore.description }}</p>
        </div>

        <div v-if="isAdmin" class="admin-controls">
          <button class="delete-button" @click="deleteStore">删除店铺</button>
        </div>
      </div>

      <div class="books-section">
        <div class="books-header">
          <h2>店铺图书</h2>
          <button v-if="isMerchant" class="add-button" @click="goToCreateBook">
            添加图书
          </button>
        </div>

        <div v-if="storeStore.storeBooks.length === 0" class="no-books">
          <p>该店铺暂无图书</p>
        </div>

        <div v-else class="books-grid">
          <div
            v-for="book in storeStore.storeBooks"
            :key="book.id"
            class="book-card"
            @click="viewBookDetail(book.id)"
          >
            <div class="book-image">
              <img :src="book.imageUrl" :alt="book.title" />
            </div>
            <div class="book-info">
              <h3>{{ book.title }}</h3>
              <p class="book-author">{{ book.author }}</p>
              <p class="book-price">¥{{ book.price.toFixed(2) }}</p>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.store-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.loading, .error, .no-store {
  text-align: center;
  padding: 40px;
  font-size: 18px;
}

.error {
  color: #ff6347;
}

.back-button {
  background-color: #ff6347;
  color: white;
  border: none;
  padding: 10px 20px;
  border-radius: 8px;
  margin-top: 20px;
  cursor: pointer;
  font-size: 16px;
}

.store-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 30px;
}

.store-info h1 {
  font-size: 32px;
  color: #333;
  margin-bottom: 10px;
}

.store-rating {
  display: flex;
  align-items: center;
  margin-bottom: 15px;
}

.stars {
  color: #ffc107;
  font-size: 20px;
  margin-right: 8px;
}

.store-description {
  font-size: 16px;
  color: #666;
  line-height: 1.6;
}

.admin-controls .delete-button {
  background-color: #dc3545;
  color: white;
  border: none;
  padding: 10px 20px;
  border-radius: 8px;
  cursor: pointer;
}

.books-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  border-bottom: 1px solid #eee;
  padding-bottom: 10px;
}

.books-header h2 {
  font-size: 24px;
  color: #333;
}

.add-button {
  background-color: #4caf50;
  color: white;
  border: none;
  padding: 10px 20px;
  border-radius: 8px;
  cursor: pointer;
}

.no-books {
  text-align: center;
  padding: 40px;
  color: #666;
  background-color: #f9f9f9;
  border-radius: 8px;
}

.books-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 20px;
}

.book-card {
  background: white;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  transition: transform 0.3s;
  cursor: pointer;
}

.book-card:hover {
  transform: translateY(-5px);
}

.book-image img {
  width: 100%;
  height: 200px;
  object-fit: cover;
}

.book-info {
  padding: 15px;
}

.book-info h3 {
  font-size: 16px;
  margin-bottom: 5px;
  color: #333;
}

.book-author {
  color: #666;
  font-size: 14px;
  margin-bottom: 8px;
}

.book-price {
  font-weight: bold;
  color: #ff6347;
  font-size: 18px;
}

@media (max-width: 768px) {
  .store-header {
    flex-direction: column;
  }

  .admin-controls {
    margin-top: 20px;
  }

  .books-grid {
    grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
  }
}
</style>
