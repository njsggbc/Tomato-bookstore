<script setup lang="ts">
import { ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useBookStore } from '../stores/Book'

const route = useRoute()
const router = useRouter()
const bookStore = useBookStore()
const storeId = Number(route.params.storeId)
const title = ref('')
const author = ref('')
const price = ref(0)
const description = ref('')
const imageUrl = ref('')
const errorMessage = ref('')
const successMessage = ref('')

const createBook = async () => {
  if (!title.value || !author.value || !price.value) {
    errorMessage.value = '请填写必要的书籍信息'
    return
  }

  try {
    const bookData = {
      title: title.value,
      author: author.value,
      price: Number(price.value),
      description: description.value,
      imageUrl: imageUrl.value || 'https://via.placeholder.com/150'
    }

    await bookStore.createBook(storeId, bookData)
    successMessage.value = '书籍创建成功！'
    errorMessage.value = ''

    // 清空表单
    title.value = ''
    author.value = ''
    price.value = 0
    description.value = ''
    imageUrl.value = ''

    // 3秒后跳转回店铺页面
    setTimeout(() => {
      router.push(`/store/${storeId}`)
    }, 3000)
  } catch (err) {
    errorMessage.value = '创建书籍失败，请重试'
    successMessage.value = ''
  }
}

const cancel = () => {
  router.push(`/store/${storeId}`)
}
</script>

<template>
  <div class="book-create-container">
    <h1>添加新书籍</h1>

    <div v-if="errorMessage" class="error-message">
      {{ errorMessage }}
    </div>

    <div v-if="successMessage" class="success-message">
      {{ successMessage }}
    </div>

    <form @submit.prevent="createBook">
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
          min="0"
          step="0.01"
          placeholder="请输入价格"
          required
        />
      </div>

      <div class="form-group">
        <label for="description">描述</label>
        <textarea
          id="description"
          v-model="description"
          rows="5"
          placeholder="请输入书籍描述"
        ></textarea>
      </div>

      <div class="form-group">
        <label for="imageUrl">图片URL</label>
        <input
          id="imageUrl"
          v-model="imageUrl"
          type="text"
          placeholder="请输入图片地址"
        />
      </div>

      <div class="form-actions">
        <button type="submit" class="btn-primary">创建书籍</button>
        <button type="button" class="btn-secondary" @click="cancel">取消</button>
      </div>
    </form>
  </div>
</template>

<style scoped>
.book-create-container {
  max-width: 800px;
  margin: 0 auto;
  padding: 30px;
  background: #ffffff;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  border-radius: 12px;
}

h1 {
  text-align: center;
  color: #ff6347;
  margin-bottom: 30px;
}

.form-group {
  margin-bottom: 20px;
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

textarea {
  resize: vertical;
}

.form-actions {
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

.error-message {
  color: red;
  margin-bottom: 20px;
  padding: 14px;
  background-color: rgba(255, 0, 0, 0.1);
  border-radius: 8px;
  text-align: center;
}

.success-message {
  color: green;
  margin-bottom: 20px;
  padding: 14px;
  background-color: rgba(0, 128, 0, 0.1);
  border-radius: 8px;
  text-align: center;
}
</style>
