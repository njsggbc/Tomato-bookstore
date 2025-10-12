<!--
 * 这是注册页面
 * 主要功能：
 * 1. 填写用户名、密码等信息
 * 2. 验证邮箱是否可用
 * 3. 设置头像（可选）
 * 4. 同意用户协议
 * 5. 注册成功后自动登录
-->
<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../stores/User'
import { ROLES, ROLE_OPTIONS } from '../constants/roles'

const username = ref('')
const password = ref('')
const confirmPassword = ref('')
const email = ref('')
const role = ref(ROLES.CUSTOMER) // 默认为顾客
const errorMessage = ref('')
const userStore = useUserStore()
const router = useRouter()

const register = async () => {
  // 表单验证
  if (!username.value || !password.value || !confirmPassword.value || !email.value) {
    errorMessage.value = '请填写所有必填字段'
    return
  }

  if (password.value !== confirmPassword.value) {
    errorMessage.value = '两次输入的密码不一致'
    return
  }

  try {
    await userStore.register({
      username: username.value,
      password: password.value,
      email: email.value,
      role: role.value
    })
    await router.push('/home')
  } catch (error: any) {
    errorMessage.value = error.message || '注册失败，请重试'
  }
}
</script>

<template>
  <div class="register-container">
    <h1>用户注册</h1>

    <div v-if="errorMessage" class="error-message">
      {{ errorMessage }}
    </div>

    <form @submit.prevent="register">
      <div class="form-group">
        <label for="username">用户名</label>
        <input
          id="username"
          v-model="username"
          type="text"
          placeholder="请输入用户名"
          required
        />
      </div>

      <div class="form-group">
        <label for="email">邮箱</label>
        <input
          id="email"
          v-model="email"
          type="email"
          placeholder="请输入邮箱"
          required
        />
      </div>

      <div class="form-group">
        <label for="password">密码</label>
        <input
          id="password"
          v-model="password"
          type="password"
          placeholder="请输入密码"
          required
        />
      </div>

      <div class="form-group">
        <label for="confirm-password">确认密码</label>
        <input
          id="confirm-password"
          v-model="confirmPassword"
          type="password"
          placeholder="请再次输入密码"
          required
        />
      </div>

      <div class="form-group">
        <label for="role">身份选择</label>
        <div class="role-options">
          <label v-for="option in ROLE_OPTIONS" :key="option.value" class="role-option">
            <input
              type="radio"
              v-model="role"
              :value="option.value"
              name="role"
            />
            <span>{{ option.label }}</span>
          </label>
        </div>
      </div>

      <div class="form-actions">
        <button type="submit" class="btn-primary">注册</button>
        <RouterLink to="/login" class="login-link">
          已有账号？去登录
        </RouterLink>
      </div>
    </form>
  </div>
</template>

<style scoped>
/* 进一步增大注册框 */
.register-container {
  max-width: 600px; /* 由 500px 调整为 600px */
  margin: 5vh auto;
  padding: 40px;
  background: #ffffff;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.12);
  border-radius: 12px;
}

h1 {
  text-align: center;
  font-size: 26px;
  margin-bottom: 25px;
}

.form-group {
  margin-bottom: 25px;
}

label {
  display: block;
  margin-bottom: 8px;
  font-weight: bold;
  font-size: 16px;
}

input {
  width: 100%;
  padding: 14px;
  border: 1px solid var(--color-border);
  border-radius: 8px;
  font-size: 18px;
}

.role-options {
  display: flex;
  gap: 20px;
  margin-top: 10px;
}

.role-option {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 10px 15px;
  border: 1px solid var(--color-border);
  border-radius: 8px;
  transition: all 0.3s ease;
}

.role-option:hover {
  background-color: var(--color-background-soft);
}

.role-option input[type="radio"] {
  width: auto;
  margin: 0;
}

.role-option span {
  font-size: 16px;
}

.form-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 30px;
}

.btn-primary {
  background-color: var(--color-text);
  color: white;
  border: none;
  padding: 14px 28px;
  border-radius: 8px;
  cursor: pointer;
  font-size: 20px;
  font-weight: bold;
}

.btn-primary:hover {
  opacity: 0.9;
}

.error-message {
  color: red;
  margin-bottom: 20px;
  padding: 14px;
  background-color: rgba(255, 0, 0, 0.1);
  border-radius: 8px;
  text-align: center;
}

.login-link {
  text-decoration: underline;
  font-size: 18px;
}
</style>
