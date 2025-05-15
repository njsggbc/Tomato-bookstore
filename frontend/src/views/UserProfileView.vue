<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useUserStore } from '../stores/User'
import { ROLE_NAMES } from '../constants/roles'

const userStore = useUserStore()
const username = ref('')
const email = ref('')
const phone = ref('')
const address = ref('')
const errorMessage = ref('')
const successMessage = ref('')
const roleName = ref('')

onMounted(async () => {
  if (userStore.isLoggedIn) {
    try {
      await userStore.getUserProfile()
      username.value = userStore.user?.username || ''
      email.value = userStore.user?.email || ''
      phone.value = userStore.user?.phone || ''
      address.value = userStore.user?.address || ''
      // 获取角色名称
      roleName.value = userStore.user?.role ? ROLE_NAMES[userStore.user.role] : ''
    } catch (error: any) {
      errorMessage.value = '获取用户信息失败'
    }
  }
})

const updateProfile = async () => {
  try {
    await userStore.updateUserProfile({
      username: username.value,
      email: email.value,
      phone: phone.value,
      address: address.value
    })
    successMessage.value = '个人信息更新成功'
    errorMessage.value = ''

    // 3秒后清除成功消息
    setTimeout(() => {
      successMessage.value = ''
    }, 3000)

  } catch (error: any) {
    errorMessage.value = error.message || '更新个人信息失败'
    successMessage.value = ''
  }
}
</script>

<template>
  <div class="profile-container">
    <h1>个人信息</h1>

    <div v-if="!userStore.isLoggedIn" class="not-logged-in">
      <p>请先登录查看个人信息</p>
      <RouterLink to="/login" class="btn-primary">去登录</RouterLink>
    </div>

    <template v-else>
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
            readonly
          />
        </div>

        <div class="form-group">
          <label for="role">用户身份</label>
          <input
            id="role"
            v-model="roleName"
            type="text"
            placeholder="用户身份"
            readonly
          />
        </div>

        <div class="form-group">
          <label for="email">邮箱</label>
          <input
            id="email"
            v-model="email"
            type="email"
            placeholder="邮箱"
            required
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
    </template>
  </div>
</template>

<style scoped>
.profile-container {
  max-width: 600px;
  margin: 0 auto;
  padding: 20px;
  background: #ffffff;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.12);
  border-radius: 12px;
}

h1 {
  text-align: center;
  font-size: 26px;
  margin-bottom: 25px;
  color: #ff6347; /* 番茄红 */
}

.form-group {
  margin-bottom: 20px;
}

label {
  display: block;
  margin-bottom: 8px;
  font-weight: bold;
  font-size: 16px;
}

input, textarea {
  width: 100%;
  padding: 14px;
  border: 1px solid var(--color-border);
  border-radius: 8px;
  font-size: 16px;
}

textarea {
  resize: vertical;
}

.form-actions {
  margin-top: 30px;
  text-align: center;
}

.btn-primary {
  background-color: #ff6347; /* 番茄红 */
  color: white;
  border: none;
  padding: 14px 28px;
  border-radius: 8px;
  cursor: pointer;
  font-size: 18px;
  font-weight: bold;
  display: inline-block;
  text-decoration: none;
  transition: all 0.3s ease;
}

.btn-primary:hover {
  background-color: #ff4500; /* 深番茄红 */
  transform: scale(1.05);
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

.not-logged-in {
  text-align: center;
  padding: 30px;
}

.not-logged-in p {
  margin-bottom: 20px;
  font-size: 18px;
}
</style>
