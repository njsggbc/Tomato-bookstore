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
const goBack = () => router.back()
const goToProfile = () => router.push('/profile')
const goToCategories = () => router.push('/categories')
</script>

<template>
  <header class="header">
    <div class="header-left">
      <button class="nav-button" @click="goBack">
        <span class="back-icon">←</span> 返回
      </button>
      <button class="nav-button" @click="goToProfile">
        个人信息
      </button>
    </div>

    <div class="header-center">
      <div class="search-container">
        <input
          type="text"
          v-model="searchQuery"
          placeholder="搜索..."
          @keyup.enter="search"
        />
        <div class="search-type-selector">
          <label>
            <input type="radio" v-model="searchType" value="store" />
            店铺
          </label>
          <label>
            <input type="radio" v-model="searchType" value="book" />
            图书
          </label>
        </div>
        <button class="search-button" @click="search">搜索</button>
      </div>
    </div>

    <div class="header-right">
      <button class="nav-button" @click="goToCategories">
        店铺分类
      </button>
      <button class="nav-button" @click="router.push('/advertisements')">
        精选推广
      </button>
      <button v-if="userStore.user && userStore.user.role === ROLES.ADMIN" class="nav-button admin-button" @click="router.push('/admin/dashboard')">
        管理后台
      </button>
    </div>
  </header>
</template>

<style scoped>
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px 0;
  margin-bottom: 20px;
  border-bottom: 1px solid #eee;
  background-color: #fff;
}

.header-left, .header-right {
  display: flex;
  gap: 10px;
}

.nav-button {
  background-color: #ff6347; /* 番茄红 */
  color: white;
  border: none;
  padding: 10px 15px;
  border-radius: 20px;
  cursor: pointer;
  font-size: 14px;
  font-weight: bold;
  transition: all 0.3s;
}

.nav-button:hover {
  background-color: #ff4500; /* 深番茄红 */
  transform: scale(1.05);
}

.back-icon {
  margin-right: 5px;
}

/* 搜索框样式 */
.header-center {
  flex: 1;
  display: flex;
  justify-content: center;
  padding: 0 20px;
}

.search-container {
  display: flex;
  align-items: center;
  width: 100%;
  max-width: 500px;
}

.search-container input {
  flex: 1;
  padding: 10px 15px;
  border: 1px solid #ddd;
  border-radius: 20px 0 0 20px;
  font-size: 16px;
  outline: none;
}

.search-type-selector {
  display: flex;
  gap: 10px;
  padding: 0 10px;
  background-color: #f5f5f5;
  border-top: 1px solid #ddd;
  border-bottom: 1px solid #ddd;
  align-items: center;
}

.search-type-selector label {
  display: flex;
  align-items: center;
  gap: 5px;
  cursor: pointer;
  font-size: 14px;
}

.search-button {
  background-color: #ff6347;
  color: white;
  border: none;
  padding: 10px 15px;
  border-radius: 0 20px 20px 0;
  cursor: pointer;
  font-size: 16px;
  font-weight: bold;
}

.search-button:hover {
  background-color: #ff4500;
}

/* 响应式样式 */
@media (max-width: 768px) {
  .header {
    flex-direction: column;
    gap: 15px;
  }

  .header-left, .header-center, .header-right {
    width: 100%;
  }

  .search-container {
    flex-wrap: wrap;
  }

  .search-type-selector {
    width: 100%;
    justify-content: center;
    padding: 10px;
    border-radius: 0;
  }

  .search-container input {
    border-radius: 20px;
  }

  .search-button {
    width: 100%;
    border-radius: 20px;
    margin-top: 10px;
  }
}
</style>
