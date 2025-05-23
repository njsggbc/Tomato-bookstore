<script setup lang="ts">
import { onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../stores/User'
import { useStoreStore } from '../stores/Store'
import { ROLES } from '../constants/roles'
import AdvertisementCarousel from '../components/AdvertisementCarousel.vue'
import UserNotifications from '../components/UserNotifications.vue'


const router = useRouter()
const userStore = useUserStore()
const storeStore = useStoreStore()

// 前往店铺页面
const goToStore = (storeId) => {
  router.push(`/store/${storeId}`)
}

onMounted(() => {
  storeStore.fetchRecommendedStores()
})
</script>

<template>
  <div class="home-container">
    <AdvertisementCarousel />
    <h1 class="page-title">推荐店铺</h1>

    <div v-if="storeStore.loading" class="loading">
      <p>加载中...</p>
    </div>

    <div v-else-if="storeStore.error" class="error">
      <p>{{ storeStore.error }}</p>
    </div>

    <div v-else-if="storeStore.stores.length === 0" class="no-stores">
      <p>暂无推荐店铺</p>
    </div>

    <div v-else class="stores-grid">
      <div
        v-for="store in storeStore.stores"
        :key="store.id"
        class="store-card"
        @click="goToStore(store.id)"
      >
        <div class="store-image">
          <img :src="store.imageUrl" :alt="store.name" />
        </div>
        <div class="store-info">
          <h3>{{ store.name }}</h3>
          <p>{{ store.description }}</p>
          <div class="store-rating">
            <span class="stars">★★★★★</span>
            <span class="rating-value">{{ store.rating }}</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 管理员控制区域 -->
    <div v-if="userStore.user?.role === ROLES.ADMIN" class="admin-controls">
      <button class="add-button" @click="router.push('/store/new')">添加新店铺</button>
    </div>
    <UserNotifications />
  </div>
</template>

<style scoped>
.home-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.page-title {
  color: #ff6347; /* 番茄红 */
  text-align: center;
  margin-bottom: 30px;
  font-size: 28px;
}

.loading, .error, .no-stores {
  text-align: center;
  padding: 40px;
  font-size: 18px;
}

.error {
  color: #ff6347;
}

.stores-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
  gap: 25px;
}

.store-card {
  background: white;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.1);
  transition: transform 0.3s, box-shadow 0.3s;
  cursor: pointer;
}

.store-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 6px 14px rgba(0, 0, 0, 0.15);
}

.store-image img {
  width: 100%;
  height: 180px;
  object-fit: cover;
}

.store-info {
  padding: 15px;
}

.store-info h3 {
  font-size: 18px;
  margin-bottom: 8px;
  color: #333;
}

.store-info p {
  color: #666;
  font-size: 14px;
  margin-bottom: 10px;
  /* 文本溢出时显示省略号 */
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.store-rating {
  display: flex;
  align-items: center;
}

.stars {
  color: #ffc107; /* 金黄色星星 */
  margin-right: 5px;
}

.rating-value {
  font-weight: bold;
  color: #555;
}

/* 管理员控制区域样式 */
.admin-controls {
  margin-top: 30px;
  text-align: center;
}

.add-button {
  background-color: #4caf50; /* 绿色 */
  color: white;
  border: none;
  padding: 12px 24px;
  border-radius: 8px;
  cursor: pointer;
  font-size: 16px;
  font-weight: bold;
  transition: all 0.3s;
}

.add-button:hover {
  background-color: #388e3c;
  transform: scale(1.05);
}

/* 响应式样式 */
@media (max-width: 768px) {
  .stores-grid {
    grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
    gap: 15px;
  }

  .store-info h3 {
    font-size: 16px;
  }

  .store-info p {
    font-size: 12px;
  }
}
</style>
