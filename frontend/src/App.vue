<script setup lang="ts">
import { RouterView, useRoute } from 'vue-router'
import { computed } from 'vue'
import Header from './components/Header.vue'

const route = useRoute()

// 登录和注册页面不显示导航栏
const shouldShowHeader = computed(() => {
  return !['login', 'register'].includes(route.name as string)
})
</script>

<template>
  <div class="app-container">
    <!-- 登录和注册页面显示Logo和标题 -->
    <div v-if="!shouldShowHeader" class="auth-header">
      <img alt="番茄读书logo" class="logo" src="@/assets/logo.jpg" />
      <h1 class="main-title">番茄书店</h1>
      <h2 class="sub-title">线上书籍商城</h2>
    </div>

    <!-- 其他页面显示导航栏 -->
    <Header v-else />

    <!-- 主内容区域 -->
    <div class="content">
      <RouterView />
    </div>
  </div>
</template>

<style scoped>
.app-container {
  max-width: 1280px;
  margin: 0 auto;
  padding: 0 1rem;
}

.auth-header {
  text-align: center;
  padding: 2rem 0;
}

.logo {
  display: block;
  margin: 0 auto 1rem;
  width: 100px;
  height: 100px;
  border-radius: 50%;
  object-fit: cover;
}

.main-title {
  font-size: 3rem;
  font-family: 'Poppins', sans-serif;
  color: #ff6347;
  margin-bottom: 0.5rem;
  letter-spacing: 2px;
}

.sub-title {
  font-size: 1rem;
  color: #8B8B8B;
  margin-bottom: 2rem;
}

.content {
  width: 100%;
}

@media (max-width: 768px) {
  .main-title {
    font-size: 2.5rem;
  }

  .app-container {
    padding: 0 0.5rem;
  }
}
</style>
