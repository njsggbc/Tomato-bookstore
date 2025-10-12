<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../stores/User'
import { ROLES } from '../constants/roles'

const router = useRouter()
const searchQuery = ref('')
const searchType = ref('store') // 'store' 或 'book'
const userStore = useUserStore()

// 搜索函数
const search = () => {
  if (!searchQuery.value.trim()) return

  if (searchType.value === 'store') {
    router.push(`/search/store?q=${encodeURIComponent(searchQuery.value)}`)
  } else {
    router.push(`/search/book?q=${encodeURIComponent(searchQuery.value)}`)
  }
}

// 导航函数
const goBack = () => {
  router.back()
}
const goToProfile = () => router.push('/profile')
const goToCategories = () => router.push('/categories')
const goToHome = () => router.push('/')

const handleLogout = () => {
  userStore.logout()
  router.push('/login')
}
</script>

<template>
  <header class="header">
    <div class="header-left">
      <button class="nav-button back-button" @click="goBack">
        <i class="fas fa-arrow-left"></i>
        返回
      </button>
      <RouterLink to="/home" class="logo">
        <img src="@/assets/logo.jpg" alt="番茄书店logo" />
        <span>番茄书店</span>
      </RouterLink>
    </div>

    <div class="header-center">
      <div class="search-container">
        <input
          v-model="searchQuery"
          type="text"
          placeholder="搜索店铺或书籍..."
          @keyup.enter="search"
        />
        <select v-model="searchType">
          <option value="store">店铺</option>
          <option value="book">书籍</option>
        </select>
        <button @click="search">搜索</button>
      </div>
    </div>

    <div class="header-right">
      <RouterLink to="/cart" class="nav-button">
        <i class="fas fa-shopping-cart"></i>
        购物车
      </RouterLink>
      <RouterLink to="/orders" class="nav-button">
        <i class="fas fa-list"></i>
        我的订单
      </RouterLink>
      <RouterLink v-if="userStore.user" to="/profile" class="nav-button">
        <i class="fas fa-user"></i>
        {{ userStore.user.username }}
      </RouterLink>
      <button v-if="userStore.user" @click="handleLogout" class="nav-button">
        退出登录
      </button>
      <RouterLink v-else to="/login" class="nav-button">
        登录
      </RouterLink>
    </div>
  </header>
</template>

<style scoped>
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1rem;
  background-color: #ffffff;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.header-left {
  display: flex;
  align-items: center;
}

.logo {
  display: flex;
  align-items: center;
  text-decoration: none;
  color: #ff6347;
  font-size: 1.5rem;
  font-weight: bold;
}

.logo img {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  margin-right: 0.5rem;
}

.header-center {
  flex: 1;
  max-width: 600px;
  margin: 0 2rem;
}

.search-container {
  display: flex;
  gap: 0.5rem;
}

.search-container input {
  flex: 1;
  padding: 0.5rem;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 1rem;
}

.search-container select {
  padding: 0.5rem;
  border: 1px solid #ddd;
  border-radius: 4px;
  background-color: white;
}

.search-container button {
  padding: 0.5rem 1rem;
  background-color: #ff6347;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  transition: background-color 0.3s;
}

.search-container button:hover {
  background-color: #ff4500;
}

.header-right {
  display: flex;
  gap: 1rem;
  align-items: center;
}

.nav-button {
  padding: 0.5rem 1rem;
  text-decoration: none;
  color: #333;
  border: 1px solid #ddd;
  border-radius: 4px;
  transition: all 0.3s;
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.nav-button:hover {
  background-color: #f5f5f5;
  border-color: #ff6347;
  color: #ff6347;
}

.nav-button i {
  font-size: 1.1rem;
}

.back-button {
  margin-right: 1rem;
}

.back-button i {
  margin-right: 0rem;
}
</style>
