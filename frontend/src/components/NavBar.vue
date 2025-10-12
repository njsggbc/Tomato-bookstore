<script setup lang="ts">
import { useUserStore } from '../stores/User'

const userStore = useUserStore()
</script>

<template>
  <nav class="navbar">
    <div v-if="userStore.isLoggedIn" class="nav-links">
      <router-link to="/cart" class="nav-link">
        <i class="fas fa-shopping-cart"></i>
        购物车
      </router-link>
      <router-link 
        :to="userStore.user?.role === 'merchant' ? '/store/orders' : '/orders'" 
        class="nav-link"
      >
        <i class="fas fa-list"></i>
        我的订单
      </router-link>
    </div>
  </nav>
</template>

<style scoped>
.navbar {
  background-color: #fff;
  padding: 1rem;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.nav-links {
  display: flex;
  gap: 1rem;
}

.nav-link {
  color: #333;
  text-decoration: none;
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.nav-link:hover {
  color: #ff6347;
}

.nav-link i {
  font-size: 1.2rem;
}
</style> 