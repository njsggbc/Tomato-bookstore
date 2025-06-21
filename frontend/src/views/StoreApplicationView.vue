<!--
 * 这是申请开书店的页面
 * 主要功能：
 * 1. 填写书店的基本信息
 * 2. 上传书店的照片
 * 3. 上传营业执照
 * 4. 选择要卖的书
 * 5. 提交开店申请
-->
<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../stores/User'

interface StoreApplication {
  id: number
  name: string
  description: string
  address: string
  phone: string
  imageUrl: string
  status: 'pending' | 'approved' | 'rejected'
  createdAt: string
  userId: number
  rejectReason?: string
}

const router = useRouter()
const userStore = useUserStore()

const storeName = ref('')
const storeDescription = ref('')
const storeAddress = ref('')
const storePhone = ref('')
const storeImage = ref<File | null>(null)
const imagePreview = ref('')
const errorMessage = ref('')
const successMessage = ref('')
const loading = ref(false)
const myApplication = ref<StoreApplication | null>(null)

// 检查是否已有申请
const checkExistingApplication = () => {
  const applications = JSON.parse(localStorage.getItem('storeApplications') || '[]')
  myApplication.value = applications.find((app: StoreApplication) => app.userId === userStore.user?.id)
}

// 处理图片选择
const handleImageSelect = (event: Event) => {
  const input = event.target as HTMLInputElement
  if (input.files && input.files[0]) {
    storeImage.value = input.files[0]
    const reader = new FileReader()
    reader.onload = (e) => {
      imagePreview.value = e.target?.result as string
    }
    reader.readAsDataURL(input.files[0])
  }
}

// 提交申请
const submitApplication = async () => {
  if (!storeName.value || !storeDescription.value || !storeAddress.value || !storePhone.value || !storeImage.value) {
    errorMessage.value = '请填写所有必填字段'
    return
  }

  loading.value = true
  errorMessage.value = ''
  successMessage.value = ''

  try {
    const applications = JSON.parse(localStorage.getItem('storeApplications') || '[]')
    const newApplication: StoreApplication = {
      id: Date.now(),
      name: storeName.value,
      description: storeDescription.value,
      address: storeAddress.value,
      phone: storePhone.value,
      imageUrl: imagePreview.value,
      status: 'pending',
      createdAt: new Date().toISOString(),
      userId: userStore.user!.id
    }

    applications.push(newApplication)
    localStorage.setItem('storeApplications', JSON.stringify(applications))

    successMessage.value = '申请提交成功，请等待管理员审核'
    myApplication.value = newApplication

    // 清空表单
    storeName.value = ''
    storeDescription.value = ''
    storeAddress.value = ''
    storePhone.value = ''
    storeImage.value = null
    imagePreview.value = ''
  } catch (err) {
    errorMessage.value = '申请提交失败，请重试'
    console.error(err)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  checkExistingApplication()
})
</script>

<template>
  <div class="application-container">
    <div class="page-header">
      <button class="back-button" @click="router.back()">
        <i class="fas fa-arrow-left"></i> 返回
      </button>
      <h1>申请开店</h1>
    </div>

    <div v-if="myApplication" class="application-status">
      <div class="status-card">
        <h2>申请状态</h2>
        <div class="status-info">
          <div class="status-badge" :class="myApplication.status">
            {{ 
              myApplication.status === 'pending' ? '审核中' :
              myApplication.status === 'approved' ? '已通过' : '已拒绝'
            }}
          </div>
          <p class="application-date">
            申请时间：{{ new Date(myApplication.createdAt).toLocaleString() }}
          </p>
          <div v-if="myApplication.status === 'rejected' && myApplication.rejectReason" class="reject-reason">
            <h3>拒绝理由：</h3>
            <p>{{ myApplication.rejectReason }}</p>
          </div>
        </div>
      </div>
    </div>

    <div v-else class="application-form">
      <div v-if="errorMessage" class="error-message">
        {{ errorMessage }}
      </div>

      <div v-if="successMessage" class="success-message">
        {{ successMessage }}
      </div>

      <form @submit.prevent="submitApplication">
        <div class="form-group">
          <label for="storeName">店铺名称</label>
          <input
            id="storeName"
            v-model="storeName"
            type="text"
            placeholder="请输入店铺名称"
            required
          />
        </div>

        <div class="form-group">
          <label for="storeDescription">店铺描述</label>
          <textarea
            id="storeDescription"
            v-model="storeDescription"
            placeholder="请输入店铺描述"
            rows="4"
            required
          ></textarea>
        </div>

        <div class="form-group">
          <label for="storeAddress">店铺地址</label>
          <input
            id="storeAddress"
            v-model="storeAddress"
            type="text"
            placeholder="请输入店铺地址"
            required
          />
        </div>

        <div class="form-group">
          <label for="storePhone">联系电话</label>
          <input
            id="storePhone"
            v-model="storePhone"
            type="tel"
            placeholder="请输入联系电话"
            required
          />
        </div>

        <div class="form-group">
          <label for="storeImage">店铺图片</label>
          <div class="image-upload">
            <input
              id="storeImage"
              type="file"
              accept="image/*"
              @change="handleImageSelect"
              required
            />
            <div v-if="imagePreview" class="image-preview">
              <img :src="imagePreview" alt="店铺图片预览" />
            </div>
          </div>
        </div>

        <div class="form-actions">
          <button type="submit" class="btn-primary" :disabled="loading">
            {{ loading ? '提交中...' : '提交申请' }}
          </button>
        </div>
      </form>
    </div>
  </div>
</template>

<style scoped>
.application-container {
  max-width: 800px;
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

.application-status {
  margin-bottom: 30px;
}

.status-card {
  background: white;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.status-card h2 {
  color: #333;
  margin-bottom: 20px;
  padding-bottom: 10px;
  border-bottom: 1px solid #eee;
}

.status-info {
  text-align: center;
}

.status-badge {
  display: inline-block;
  padding: 8px 16px;
  border-radius: 20px;
  font-weight: bold;
  margin-bottom: 15px;
}

.status-badge.pending {
  background-color: #fff3cd;
  color: #856404;
}

.status-badge.approved {
  background-color: #d4edda;
  color: #155724;
}

.status-badge.rejected {
  background-color: #f8d7da;
  color: #721c24;
}

.application-date {
  color: #666;
  margin-bottom: 15px;
}

.reject-reason {
  margin-top: 20px;
  padding: 15px;
  background-color: #f8f9fa;
  border-radius: 4px;
}

.reject-reason h3 {
  color: #721c24;
  margin-bottom: 10px;
}

.reject-reason p {
  color: #666;
  line-height: 1.5;
}

.application-form {
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
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 16px;
}

textarea {
  resize: vertical;
}

.image-upload {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.image-preview {
  max-width: 300px;
  margin-top: 10px;
}

.image-preview img {
  width: 100%;
  height: auto;
  border-radius: 4px;
}

.form-actions {
  margin-top: 30px;
  text-align: center;
}

.btn-primary {
  background-color: #ff6347;
  color: white;
  border: none;
  padding: 12px 24px;
  border-radius: 4px;
  font-size: 16px;
  font-weight: bold;
  cursor: pointer;
  transition: background-color 0.3s;
}

.btn-primary:hover:not(:disabled) {
  background-color: #ff4500;
}

.btn-primary:disabled {
  background-color: #ccc;
  cursor: not-allowed;
}

.error-message {
  color: #dc3545;
  background-color: #f8d7da;
  padding: 10px;
  border-radius: 4px;
  margin-bottom: 20px;
}

.success-message {
  color: #28a745;
  background-color: #d4edda;
  padding: 10px;
  border-radius: 4px;
  margin-bottom: 20px;
}

@media (max-width: 768px) {
  .application-container {
    padding: 15px;
  }

  .image-preview {
    max-width: 100%;
  }
}
</style>
