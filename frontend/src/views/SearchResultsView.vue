<!--
 * 这是搜索结果页面
 * 主要功能：
 * 1. 显示搜索到的书
 * 2. 可以按价格排序
 * 3. 可以按分类筛选
 * 4. 可以翻页查看更多
 * 5. 点击书可以看详情
-->
<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useStoreStore } from '../stores/Store'
import { useBookStore } from '../stores/Book'

const router = useRouter()
const route = useRoute()
const storeStore = useStoreStore()
const bookStore = useBookStore()

const searchType = ref(route.params.type as string)
const searchQuery = ref(route.query.q as string)
const loading = ref(false)
const error = ref('')
const stores = ref([])
const books = ref([])

// 搜索店铺
const searchStores = async () => {
  loading.value = true
  try {
    const results = await storeStore.searchStores(searchQuery.value)
    stores.value = results
  } catch (err) {
    error.value = '搜索店铺失败'
  } finally {
    loading.value = false
  }
}

// 搜索书籍
const searchBooks = async () => {
  loading.value = true
  try {
    const results = await bookStore.searchBooks(searchQuery.value)
    books.value = results
  } catch (err) {
    error.value = '搜索书籍失败'
  } finally {
    loading.value = false
  }
}

// 跳转到店铺详情
const goToStore = (storeId: number) => {
  router.push(`/store/${storeId}`)
}

// 跳转到书籍详情
const goToBook = (bookId: number) => {
  router.push(`/book/${bookId}`)
}

// 返回首页
const backToHome = () => {
  router.push('/home')
}

onMounted(async () => {
  if (!searchQuery.value) {
    error.value = '请输入搜索关键词'
    return
  }

  if (searchType.value === 'store') {
    await searchStores()
  } else if (searchType.value === 'book') {
    await searchBooks()
  } else {
    error.value = '无效的搜索类型'
  }
})
</script>

<template>
  <div class="search-results-container">
    <h1 class="page-title">
      {{ searchType === 'store' ? '店铺搜索结果' : '图书搜索结果' }}
    </h1>

    <div v-if="loading" class="loading">
      <p>搜索中...</p>
    </div>

    <div v-else-if="error" class="error">
      <p>{{ error }}</p>
      <button class="back-button" @click="backToHome">返回首页</button>
    </div>

    <!-- 店铺搜索结果 -->
    <div v-else-if="searchType === 'store' && stores.length > 0" class="stores-grid">
      <div
        v-for="store in stores"
        :key="store.id"
        class="store-card"
        @click="goToStore(store.id)"
      >
        <div class="store-image">
          <img :src="store.cover" :alt="store.name" />
        </div>
        <div class="store-info">
          <h3>{{ store.name }}</h3>
          <p>{{ store.description }}</p>
        </div>
      </div>
    </div>

    <!-- 图书搜索结果 -->
    <div v-else-if="searchType === 'book' && books.length > 0" class="books-grid">
      <div
        v-for="book in books"
        :key="book.id"
        class="book-card"
        @click="goToBook(book.id)"
      >
        <div class="book-image">
          <img :src="book.cover" :alt="book.title" />
        </div>
        <div class="book-info">
          <h3>{{ book.title }}</h3>
          <p class="book-author">作者: {{ book.author }}</p>
          <p class="book-price">¥{{ book.price.toFixed(2) }}</p>
        </div>
      </div>
    </div>

    <!-- 无搜索结果 -->
    <div v-else class="no-results">
      <p>未找到相关{{ searchType === 'store' ? '店铺' : '图书' }}</p>
      <button class="back-button" @click="backToHome">返回首页</button>
    </div>
  </div>
</template>

<style scoped>
.search-results-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.page-title {
  color: #ff6347;
  text-align: center;
  margin-bottom: 30px;
  font-size: 28px;
}

.loading, .error, .no-results {
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

.stores-grid, .books-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
  gap: 25px;
}

.store-card, .book-card {
  background: white;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.1);
  transition: transform 0.3s, box-shadow 0.3s;
  cursor: pointer;
}

.store-card:hover, .book-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 6px 14px rgba(0, 0, 0, 0.15);
}

.store-image img, .book-image img {
  width: 100%;
  height: 180px;
  object-fit: cover;
}

.store-info, .book-info {
  padding: 15px;
}

.store-info h3, .book-info h3 {
  font-size: 18px;
  margin-bottom: 8px;
  color: #333;
}

.store-info p, .book-info p {
  color: #666;
  font-size: 14px;
  margin-bottom: 10px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.book-author {
  color: #888;
  font-size: 14px;
  margin-bottom: 5px;
}

.book-price {
  color: #ff6347;
  font-size: 18px;
  font-weight: bold;
  margin: 0;
}

@media (max-width: 768px) {
  .stores-grid, .books-grid {
    grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
    gap: 15px;
  }

  .store-info h3, .book-info h3 {
    font-size: 16px;
  }

  .store-info p, .book-info p {
    font-size: 12px;
  }
}
</style>
