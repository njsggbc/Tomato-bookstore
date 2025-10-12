<!--
 * 这是添加新书的页面
 * 主要功能：
 * 1. 填写书名、作者、价格等信息
 * 2. 上传书的封面图片
 * 3. 选择书的分类
 * 4. 写书的简介
 * 5. 设置库存数量
-->
<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '../stores/User'
import { useBookStore } from '../stores/Book'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const bookStore = useBookStore()

const storeId = ref(Number(route.params.storeId))
const bookId = ref(route.params.bookId ? Number(route.params.bookId) : null)
const isEdit = ref(!!bookId.value)

const title = ref('')
const author = ref('')
const price = ref('')
const stock = ref('')
const description = ref('')
const imageUrl = ref('')
const loading = ref(false)
const errorMessage = ref('')

// 获取书籍信息（编辑模式）
const fetchBookInfo = async () => {
  if (!bookId.value) return

  loading.value = true
  errorMessage.value = ''

  try {
    // 从 localStorage 获取所有书籍
    const allBooks = JSON.parse(localStorage.getItem('books') || '[]')
    
    // 查找要编辑的书籍
    const book = allBooks.find((b: any) => b.id === bookId.value)

    if (book) {
      title.value = book.title
      author.value = book.author
      price.value = book.price.toString()
      stock.value = book.stock.toString()
      description.value = book.description
      imageUrl.value = book.cover || book.imageUrl // 优先使用cover字段
    } else {
      errorMessage.value = '未找到书籍信息'
    }
  } catch (err) {
    errorMessage.value = '获取书籍信息失败'
    console.error(err)
  } finally {
    loading.value = false
  }
}

// 提交表单
const submitForm = async () => {
  if (!title.value || !author.value || !price.value || !stock.value) {
    errorMessage.value = '请填写所有必填信息'
    return
  }

  loading.value = true
  errorMessage.value = ''

  try {
    const bookData = {
      title: title.value,
      author: author.value,
      price: Number(price.value),
      stock: Number(stock.value),
      description: description.value,
      cover: imageUrl.value || 'https://via.placeholder.com/200',
      storeId: storeId.value,
      category: '未分类',
      rating: 5,
      sales: 0,
      createdAt: new Date().toISOString()
    }

    if (isEdit.value && bookId.value !== null) {
      // 更新现有书籍
      await bookStore.updateBook(bookId.value, bookData)
    } else {
      // 创建新书籍
      await bookStore.createBook(storeId.value, bookData)
    }

    // 返回店铺管理页面
    router.push(`/store/${storeId.value}/manage`)
  } catch (err) {
    errorMessage.value = isEdit.value ? '更新书籍失败' : '创建书籍失败'
    console.error(err)
  } finally {
    loading.value = false
  }
}

onMounted(async () => {
  if (isEdit.value) {
    await fetchBookInfo()
  }
})
</script>

<template>
  <div class="book-form-container">
    <div class="page-header">
      <button class="back-button" @click="router.back()">
        <i class="fas fa-arrow-left"></i> 返回
      </button>
      <h1>{{ isEdit ? '编辑书籍' : '添加新书' }}</h1>
    </div>

    <div v-if="errorMessage" class="error-message">
      {{ errorMessage }}
    </div>

    <form @submit.prevent="submitForm" class="book-form">
      <div class="form-group">
        <label for="title">书名</label>
        <input
          id="title"
          v-model="title"
          type="text"
          placeholder="请输入书名"
          required
        />
      </div>

      <div class="form-group">
        <label for="author">作者</label>
        <input
          id="author"
          v-model="author"
          type="text"
          placeholder="请输入作者"
          required
        />
      </div>

      <div class="form-group">
        <label for="price">价格</label>
        <input
          id="price"
          v-model="price"
          type="number"
          step="0.01"
          min="0"
          placeholder="请输入价格"
          required
        />
      </div>

      <div class="form-group">
        <label for="stock">库存</label>
        <input
          id="stock"
          v-model="stock"
          type="number"
          min="0"
          placeholder="请输入库存数量"
          required
        />
      </div>

      <div class="form-group">
        <label for="description">描述</label>
        <textarea
          id="description"
          v-model="description"
          rows="4"
          placeholder="请输入书籍描述"
        ></textarea>
      </div>

      <div class="form-group">
        <label for="imageUrl">封面图片URL</label>
        <input
          id="imageUrl"
          v-model="imageUrl"
          type="text"
          placeholder="请输入封面图片地址"
        />
      </div>

      <div class="form-actions">
        <button type="button" class="btn-secondary" @click="router.push(`/store/${storeId}/manage`)">
          取消
        </button>
        <button type="submit" class="btn-primary" :disabled="loading">
          {{ loading ? '提交中...' : (isEdit ? '更新' : '创建') }}
        </button>
      </div>
    </form>
  </div>
</template>

<style scoped>
.book-form-container {
  max-width: 600px;
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

.book-form {
  background: white;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.form-group {
  margin-bottom: 20px;
}

label {
  display: block;
  margin-bottom: 8px;
  font-weight: bold;
  color: #333;
}

input, textarea {
  width: 100%;
  padding: 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 16px;
}

textarea {
  resize: vertical;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 30px;
}

.btn-primary, .btn-secondary {
  padding: 12px 24px;
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

.btn-primary:disabled {
  background-color: #ccc;
  cursor: not-allowed;
}

.btn-secondary {
  background-color: #f5f5f5;
  color: #333;
  border: 1px solid #ddd;
}

.btn-secondary:hover {
  background-color: #e5e5e5;
}

@media (max-width: 768px) {
  .form-actions {
    flex-direction: column;
  }

  .btn-primary, .btn-secondary {
    width: 100%;
  }
}
</style>
