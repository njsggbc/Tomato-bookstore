<script setup lang="ts">
import { RouterLink, RouterView } from 'vue-router'
import { useUserStore } from './stores/User'

const userStore = useUserStore()

const logout = () => {
  userStore.logout()
}
</script>

<template>
  <header>
    <img alt="番茄读书logo" class="logo" src="@/assets/logo.jpg" />

    <div class="wrapper">
      <h1 class="main-title">番茄书店</h1> <!-- 可爱字体的标题 -->
      <h2 class="sub-title">线上书籍商城</h2> <!-- 更小的副标题 -->

      <nav>
        <RouterLink to="/login" class="btn" v-if="!userStore.isLoggedIn">登录</RouterLink>
        <RouterLink to="/register" class="btn" v-if="!userStore.isLoggedIn">注册</RouterLink>
        <RouterLink to="/profile" class="btn" v-if="userStore.isLoggedIn">个人信息</RouterLink>
        <a href="#" @click.prevent="logout" class="btn" v-if="userStore.isLoggedIn">退出登录</a>
      </nav>
    </div>
  </header>

  <RouterView />
</template>

<style scoped>
header {
  line-height: 1.5;
  max-height: 100vh;
}

.logo {
  display: block;
  margin: 0 auto 2rem;
  width: 100px;
  height: 100px;
  border-radius: 50%;
  object-fit: cover;
}

.main-title {
  font-size: 3.5rem; /* 更大的字体 */
  font-family: 'Poppins', sans-serif; /* 更加圆润的可爱字体 */
  text-align: center;
  color: #ff6347; /* 番茄红 */
  margin-bottom: 0.5rem;
  letter-spacing: 2px;
  transition: transform 0.3s ease;
}

.main-title:hover {
  transform: translateY(-5px); /* 鼠标悬停时标题浮动效果 */
}

.sub-title {
  font-size: 0.85rem; /* 更小的字体 */
  text-align: right; /* 更靠右 */
  color: #8B8B8B; /* 灰色字体 */
  margin-top: -0.5rem;
  margin-bottom: 2rem;
  font-family: 'Arial', sans-serif;
  transition: transform 0.3s ease;
}

.sub-title:hover {
  transform: translateX(5px); /* 小字浮动效果 */
}

nav {
  width: 100%;
  font-size: 14px;
  text-align: center;
  margin-top: 2rem;
}

nav a.router-link-exact-active {
  color: var(--color-text);
}

nav a.router-link-exact-active:hover {
  background-color: transparent;
}

nav a {
  display: inline-block;
  padding: 12px 20px;
  border-radius: 25px;
  background-color: #FF6347; /* 番茄红背景 */
  color: white;
  text-decoration: none;
  margin: 0 10px;
  font-size: 16px;
  font-weight: bold;
  transition: all 0.3s ease;
}

nav a:hover {
  background-color: #FF4500; /* 略深的番茄红 */
  transform: scale(1.05); /* 鼠标悬浮时按钮稍微放大 */
}

.btn {
  background-color: #ff6347; /* 番茄红 */
  color: white;
  border: none;
  padding: 10px 20px;
  border-radius: 8px;
  cursor: pointer;
  font-size: 16px;
  margin: 0 5px;
  text-decoration: none;
}

.btn:hover {
  background-color: #ff4500; /* 略深的番茄红 */
}

@media (min-width: 1024px) {
  header {
    display: flex;
    place-items: center;
    padding-right: calc(var(--section-gap) / 2);
  }

  .logo {
    margin: 0 2rem 0 0;
  }

  header .wrapper {
    display: flex;
    place-items: flex-start;
    flex-wrap: wrap;
  }

  .main-title {
    font-size: 4rem; /* 更大的字体 */
  }

  .sub-title {
    font-size: 1.2rem; /* 更小的副标题 */
    margin-right: 10%; /* 进一步向右调整 */
  }

  nav {
    text-align: left;
    margin-left: -1rem;
    font-size: 1rem;
    padding: 1rem 0;
    margin-top: 1rem;
  }
}
</style>
