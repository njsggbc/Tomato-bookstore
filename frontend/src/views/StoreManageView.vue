<!-- src/views/StoreManageView.vue -->
<!--
 * 这是书店管理页面
 * 主要功能：
 * 1. 修改书店的基本信息
 * 2. 管理店里的图书库存
 * 3. 处理顾客的订单
 * 4. 查看销售统计
 * 5. 设置店铺营业时间等
-->
<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../stores/User'
import { useStoreStore } from '../stores/Store'

interface Store {
  id: number
  name: string
  description: string
  address: string
  phone: string
}

interface Book {
  id: number
  title: string
  author: string
  price: number
  cover: string
  stock: number
}

const router = useRouter()
const userStore = useUserStore()
const storeStore = useStoreStore()

const store = ref<Store | null>(null)
const books = ref<Book[]>([])
const loading = ref(false)
const errorMessage = ref('')

// 获取店铺信息
const fetchStoreInfo = async () => {
  loading.value = true
  errorMessage.value = ''

  try {
    // 从 localStorage 获取所有店铺
    const storedStores = JSON.parse(localStorage.getItem('stores') || '[]')
    
    // 查找当前用户的店铺
    const userStoreData = storedStores.find((s: any) => s.userId === userStore.user?.id)

    if (userStoreData) {
      store.value = userStoreData
    } else {
      errorMessage.value = '您还没有创建店铺'
    }
  } catch (err) {
    errorMessage.value = '获取店铺信息失败'
    console.error(err)
  } finally {
    loading.value = false
  }
}

// 获取店铺书籍列表
const fetchBooks = async () => {
  if (!store.value) return

  loading.value = true
  errorMessage.value = ''

  try {
    // 从 localStorage 获取书籍列表
    const allBooks = JSON.parse(localStorage.getItem('books') || '[]')
    
    // 筛选出当前店铺的书籍
    books.value = allBooks.filter((book: any) => book.storeId === store.value?.id)
  } catch (err) {
    errorMessage.value = '获取书籍列表失败'
    console.error(err)
  } finally {
    loading.value = false
  }
}

// 删除书籍
const deleteBook = async (bookId: number) => {
  if (!confirm('确定要删除这本书吗？')) {
    return
  }

  try {
    // 从 localStorage 获取所有书籍
    const allBooks = JSON.parse(localStorage.getItem('books') || '[]')
    
    // 过滤掉要删除的书籍
    const updatedBooks = allBooks.filter((book: any) => book.id !== bookId)
    
    // 保存回 localStorage
    localStorage.setItem('books', JSON.stringify(updatedBooks))

    // 重新获取书籍列表
    await fetchBooks()
  } catch (err) {
    errorMessage.value = '删除书籍失败'
    console.error(err)
  }
}

// 更新库存
const updateStock = async (bookId: number, newStock: number) => {
  try {
    // 从 localStorage 获取所有书籍
    const allBooks = JSON.parse(localStorage.getItem('books') || '[]')
    
    // 更新指定书籍的库存
    const updatedBooks = allBooks.map((book: any) => {
      if (book.id === bookId) {
        return { ...book, stock: newStock }
      }
      return book
    })
    
    // 保存回 localStorage
    localStorage.setItem('books', JSON.stringify(updatedBooks))

    // 重新获取书籍列表
    await fetchBooks()
  } catch (err) {
    errorMessage.value = '更新库存失败'
    console.error(err)
  }
}

onMounted(async () => {
  await fetchStoreInfo()
  if (store.value) {
    await fetchBooks()
  }
})
</script>

<template>
  <div class="store-manage-container">
    <div class="page-header">
      <button class="back-button" @click="router.back()">
        <i class="fas fa-arrow-left"></i> 返回
      </button>
      <h1>店铺管理</h1>
    </div>

    <div v-if="errorMessage" class="error-message">
      {{ errorMessage }}
    </div>

    <div v-if="loading" class="loading">
      加载中...
    </div>

    <template v-else>
      <div v-if="store" class="store-info">
        <h2>{{ store.name }}</h2>
        <p>{{ store.description }}</p>
        <div class="store-details">
          <p><strong>地址：</strong>{{ store.address }}</p>
          <p><strong>电话：</strong>{{ store.phone }}</p>
        </div>
      </div>

      <div v-if="store" class="books-section">
        <div class="section-header">
          <h2>书籍管理</h2>
          <button class="btn-primary" @click="router.push(`/store/${store.id}/book/create`)">
            添加新书
          </button>
        </div>

        <div class="books-list">
          <div v-for="book in books" :key="book.id" class="book-item">
            <img :src="book.cover" :alt="book.title" class="book-cover" />
            <div class="book-info">
              <h3>{{ book.title }}</h3>
              <p>{{ book.author }}</p>
              <p class="price">¥{{ book.price }}</p>
              <div class="stock-control">
                <label>库存：</label>
                <input
                  type="number"
                  :value="book.stock"
                  min="0"
                  @change="(e: Event) => updateStock(book.id, Number((e.target as HTMLInputElement).value))"
                />
              </div>
            </div>
            <div class="book-actions">
              <button class="btn-edit" @click="router.push(`/store/${store.id}/book/${book.id}/edit`)">
                编辑
              </button>
              <button class="btn-delete" @click="deleteBook(book.id)">
                删除
              </button>
            </div>
          </div>
        </div>
      </div>
    </template>
  </div>
</template>

<style scoped>
.store-manage-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.page-header {
  display: flex;
  align-items: center;
  margin-bottom: 30px;
  position: relative;
}

.back-button {
  position: absolute;
  left: 0;
  background: none;
  border: none;
  color: #666;
  cursor: pointer;
  font-size: 16px;
  display: flex;
  align-items: center;
  gap: 5px;
  padding: 5px 10px;
  border-radius: 4px;
  transition: background-color 0.3s;
}

.back-button:hover {
  background-color: #f5f5f5;
}

h1 {
  text-align: center;
  color: #ff6347;
  margin: 0 auto;
}

.error-message {
  color: #ff4444;
  background-color: rgba(255, 68, 68, 0.1);
  padding: 10px;
  border-radius: 4px;
  margin-bottom: 20px;
}

.loading {
  text-align: center;
  padding: 20px;
  color: #666;
}

.store-info {
  background: white;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  margin-bottom: 30px;
}

.store-info h2 {
  color: #333;
  margin-bottom: 10px;
}

.store-details {
  margin-top: 15px;
  color: #666;
}

.books-section {
  background: white;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.books-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
}

.book-item {
  display: flex;
  flex-direction: column;
  padding: 15px;
  border: 1px solid #eee;
  border-radius: 8px;
  transition: transform 0.3s;
}

.book-item:hover {
  transform: translateY(-5px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.book-cover {
  width: 100%;
  height: 200px;
  object-fit: cover;
  border-radius: 4px;
  margin-bottom: 15px;
}

.book-info {
  flex: 1;
}

.book-info h3 {
  margin: 0 0 10px 0;
  color: #333;
}

.price {
  color: #ff6347;
  font-weight: bold;
  margin: 10px 0;
}

.stock-control {
  display: flex;
  align-items: center;
  gap: 10px;
  margin: 10px 0;
}

.stock-control input {
  width: 80px;
  padding: 5px;
  border: 1px solid #ddd;
  border-radius: 4px;
}

.book-actions {
  display: flex;
  gap: 10px;
  margin-top: 15px;
}

.btn-primary, .btn-edit, .btn-delete {
  padding: 8px 16px;
  border-radius: 4px;
  font-weight: bold;
  cursor: pointer;
  transition: background-color 0.3s;
}

.btn-primary {
  background-color: #ff6347;
  color: white;
  border: none;
}

.btn-primary:hover {
  background-color: #ff4500;
}

.btn-edit {
  background-color: #4CAF50;
  color: white;
  border: none;
}

.btn-edit:hover {
  background-color: #45a049;
}

.btn-delete {
  background-color: #f44336;
  color: white;
  border: none;
}

.btn-delete:hover {
  background-color: #da190b;
}

@media (max-width: 768px) {
  .books-list {
    grid-template-columns: 1fr;
  }

  .section-header {
    flex-direction: column;
    gap: 10px;
  }

  .btn-primary {
    width: 100%;
  }
}
</style> 