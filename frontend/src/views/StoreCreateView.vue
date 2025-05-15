<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useStoreStore } from '../stores/Store'
import { useUserStore } from '../stores/User'

const router = useRouter()
const storeStore = useStoreStore()
const userStore = useUserStore()
const name = ref('')
const description = ref('')
const imageUrl = ref('')
const errorMessage = ref('')
const successMessage = ref('')

const createStore = async () => {
  if (!name.value || !description.value) {
    errorMessage.value = '请填写必要的店铺信息'
    return
  }

  try {
    const storeData = {
      name: name.value,
      description: description.value,
      imageUrl: imageUrl.value || 'https://via.placeholder.com/150',
      rating: 5.0, // 默认评分
      ownerId: userStore.user?.username // 使用当前登录用户作为店主
    }

    await storeStore.createStore(storeData)
    successMessage.value = '店铺创建成功！'
    errorMessage.value = ''

    // 清空表单
    name.value = ''
    description.value = ''
    imageUrl.value = ''

    // 3秒后跳转回主页
    setTimeout(() => {
      router.push('/home')
    }, 3000)
  } catch (err) {
    errorMessage.value = '创建店铺失败，请重试'
    successMessage.value = ''
  }
}

const cancel = () => {
  router.push('/home')
}
</script>

<template>
  <div class="store-create-container">
    <h1>添加新店铺</h1>

    <div v-if="errorMessage" class="error-message">
      {{ errorMessage }}
    </div>

    <div v-if="successMessage" class="success-message">
      {{ successMessage }}
    </div>

    <form @submit.prevent="createStore">
      <div class="form-group">
        <label for="name">店铺名称</label>
        <input
          id="name"
          v-model="name"
          type="text"
          placeholder="请输入店铺名称"
          required
        />
      </div>

      <div class="form-group">
        <label for="description">店铺描述</label>
        <textarea
          id="description"
          v-model="description"
          rows="5"
          placeholder="请输入店铺描述"
          required
        ></textarea>
      </div>

      <div class="form-group">
        <label for="imageUrl">店铺图片URL</label>
        <input
          id="imageUrl"
          v-model="imageUrl"
          type="text"
          placeholder="请输入图片地址"
        />
      </div>

      <div class="form-actions">
        <button type="submit" class="btn-primary">创建店铺</button>
        <button type="button" class="btn-secondary" @click="cancel">取消</button>
      </div>
    </form>
  </div>
</template>

<style scoped>
.store-create-container {
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
