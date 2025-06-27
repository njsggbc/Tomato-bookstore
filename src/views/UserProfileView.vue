<!--
 * 这是我的个人中心页面
 * 主要功能：
 * 1. 显示和修改个人资料
 * 2. 可以修改密码
 * 3. 查看我的订单历史
 * 4. 管理收货地址
 * 5. 查看我的收藏
-->
<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../stores/User'
import { ROLE_NAMES, ROLES } from '../constants/roles'
import UserNotifications from '../components/UserNotifications.vue'
import StoreApplicationList from '../components/StoreApplicationList.vue'

interface Store {
  id: number
  name: string
  description: string
  address: string
  phone: string
  imageUrl: string
  userId: number
}

const router = useRouter()
const userStore = useUserStore()
const username = ref('')
const email = ref('')
const phone = ref('')
const address = ref('')
const errorMessage = ref('')
const successMessage = ref('')
const roleName = ref('')
const activeTab = ref('info')
const showApplications = ref(false)
const myStore = ref<Store | null>(null)

onMounted(() => {
  if (userStore.user) {
    username.value = userStore.user.username
    email.value = userStore.user.email
    phone.value = userStore.user.phone || ''
    address.value = userStore.user.address || ''
    roleName.value = userStore.user.role ? ROLE_NAMES[userStore.user.role] : ''
  }
  fetchMyStore()
})

const updateProfile = async () => {
  try {
    await userStore.updateUserProfile({
      username: username.value,
      phone: phone.value,
      address: address.value
    })
    successMessage.value = '个人信息更新成功'
    errorMessage.value = ''

    setTimeout(() => {
      successMessage.value = ''
    }, 3000)
  } catch (error: any) {
    errorMessage.value = error.message || '更新个人信息失败'
    successMessage.value = ''
  }
}

// 获取我的店铺信息
const fetchMyStore = () => {
  if (userStore.user?.role !== 'merchant') return

  const stores = JSON.parse(localStorage.getItem('stores') || '[]')
  myStore.value = stores.find((store: Store) => store.userId === userStore.user?.id)
}
</script>

<template>
  <div class="profile-container">
    <div class="page-header">
      <button class="back-button" @click="router.back()">
        <i class="fas fa-arrow-left"></i> 返回
      </button>
      <h1>个人信息</h1>
    </div>

    <div class="profile-content">
      <div class="profile-info">
        <h2>基本信息</h2>
        <div v-if="errorMessage" class="error-message">
          {{ errorMessage }}
        </div>

        <div v-if="successMessage" class="success-message">
          {{ successMessage }}
        </div>

        <form @submit.prevent="updateProfile">
          <div class="form-group">
            <label for="username">用户名</label>
            <input
              id="username"
              v-model="username"
              type="text"
              placeholder="用户名"
              required
            />
          </div>

          <div class="form-group">
            <label for="email">邮箱</label>
            <input
              id="email"
              v-model="email"
              type="email"
              disabled
            />
          </div>

          <div class="form-group">
            <label for="phone">手机号码</label>
            <input
              id="phone"
              v-model="phone"
              type="tel"
              placeholder="手机号码"
            />
          </div>

          <div class="form-group">
            <label for="address">收货地址</label>
            <textarea
              id="address"
              v-model="address"
              placeholder="收货地址"
              rows="3"
            ></textarea>
          </div>

          <div class="form-actions">
            <button type="submit" class="btn-primary">保存修改</button>
          </div>
        </form>
      </div>

      <!-- 商家特有功能 -->
      <div v-if="userStore.user?.role === 'merchant'" class="merchant-section">
        <h2>商家功能</h2>
        
        <!-- 店铺信息 -->
        <div v-if="myStore" class="store-info">
          <h3>我的店铺</h3>
          <div class="store-card">
            <img :src="myStore.imageUrl" :alt="myStore.name" class="store-image" />
            <div class="store-details">
              <h4>{{ myStore.name }}</h4>
              <p>{{ myStore.description }}</p>
              <p><strong>地址：</strong>{{ myStore.address }}</p>
              <p><strong>电话：</strong>{{ myStore.phone }}</p>
            </div>
            <button class="btn-primary" @click="router.push(`/store/${myStore.id}/manage`)">
              管理店铺
            </button>
            <button class="btn-primary" style="margin-top: 10px;" @click="router.push('/ad/apply')">
              申请广告
            </button>
          </div>
        </div>

        <!-- 申请开店 -->
        <div v-else class="store-application">
          <h3>申请开店</h3>
          <p>您还没有店铺，点击下方按钮申请开店</p>
          <button class="btn-primary" @click="router.push('/store/apply')">
            申请开店
          </button>
        </div>
      </div>

      <!-- 管理员特有功能 -->
      <div v-if="userStore.user?.role === 'admin'" class="admin-section">
        <h2>管理员功能</h2>
        <button class="btn-primary" @click="showApplications = !showApplications">
          {{ showApplications ? '隐藏申请列表' : '查看店铺申请' }}
        </button>
        <button class="btn-primary" style="margin-left: 10px;" @click="router.push('/ads-approval')">
          管理广告
        </button>
        <div v-if="showApplications" class="applications-section">
          <StoreApplicationList />
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.profile-container {
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

.profile-content {
  background: white;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.profile-info {
  margin-bottom: 30px;
}

h2 {
  color: #333;
  margin-bottom: 20px;
  padding-bottom: 10px;
  border-bottom: 1px solid #eee;
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

input:disabled {
  background-color: #f5f5f5;
  cursor: not-allowed;
}

textarea {
  resize: vertical;
}

.form-actions {
  margin-top: 30px;
  text-align: center;
}

.merchant-section, .admin-section {
  margin-top: 30px;
  padding-top: 20px;
  border-top: 1px solid #eee;
}

.store-info, .store-application {
  margin-top: 20px;
}

.store-card {
  background: #f8f9fa;
  border-radius: 8px;
  padding: 20px;
  display: flex;
  gap: 20px;
  align-items: center;
}

.store-image {
  width: 150px;
  height: 150px;
  object-fit: cover;
  border-radius: 4px;
}

.store-details {
  flex: 1;
}

.store-details h4 {
  margin: 0 0 10px 0;
  color: #333;
}

.store-details p {
  margin: 5px 0;
  color: #666;
}

.store-application {
  text-align: center;
  padding: 30px;
  background: #f8f9fa;
  border-radius: 8px;
}

.store-application p {
  margin-bottom: 20px;
  color: #666;
}

.btn-primary {
  background-color: #ff6347;
  color: white;
  border: none;
  padding: 10px 20px;
  border-radius: 4px;
  cursor: pointer;
  font-weight: bold;
  transition: background-color 0.3s;
}

.btn-primary:hover {
  background-color: #ff4500;
}

.applications-section {
  margin-top: 20px;
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
  .store-card {
    flex-direction: column;
    text-align: center;
  }

  .store-image {
    width: 100%;
    height: 200px;
  }

  .info-item {
    flex-direction: column;
    align-items: flex-start;
  }

  .info-item label {
    margin-bottom: 5px;
  }
}
</style>
