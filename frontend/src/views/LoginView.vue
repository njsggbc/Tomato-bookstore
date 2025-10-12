<!--
 * 这是登录页面
 * 主要功能：
 * 1. 输入用户名和密码登录
 * 2. 记住密码功能
 * 3. 忘记密码可以找回
 * 4. 没有账号可以跳转到注册
 * 5. 登录成功会自动跳转到首页
-->
<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../stores/User'

const email = ref('')
const password = ref('')
const errorMessage = ref('')
const userStore = useUserStore()
const router = useRouter()

const login = async () => {
  if (!email.value || !password.value) {
    errorMessage.value = '请输入邮箱和密码'
    return
  }

  try {
    await userStore.login(email.value, password.value)
    await router.push('/home')
  } catch (error: any) {
    errorMessage.value = error.message || '登录失败，请重试'
  }
}

</script>

<template>
  <div class="login-container">
    <h1>用户登录</h1>

    <div v-if="errorMessage" class="error-message">
      {{ errorMessage }}
    </div>

    <form @submit.prevent="login">
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

      <div class="form-actions">
        <button type="submit" class="btn-primary">登录</button>
        <RouterLink to="/register" class="register-link">
          没有账号？去注册
        </RouterLink>
      </div>
    </form>
  </div>
</template>

<style scoped>
/* 进一步增大登录框 */
.login-container {
  max-width: 600px;
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

.register-link {
  text-decoration: underline;
  font-size: 18px;
}
</style>
