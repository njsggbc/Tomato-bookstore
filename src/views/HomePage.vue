<!--
 * 这是网站的首页
 * 主要功能：
 * 1. 顶部有轮播广告，展示各种促销活动
 * 2. 中间是推荐的书店列表，可以点击进入
 * 3. 右上角有"查看全部商店"按钮
 * 4. 如果是管理员，可以看到"添加新店铺"按钮
 * 5. 右下角会显示系统通知
-->
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

// 前往全部商店页面
const goToAllStores = () => {
  router.push('/stores')
}

onMounted(() => {
  storeStore.fetchRecommendedStores()
})
</script>

<template>
  <div class="home-container">
    <AdvertisementCarousel />
    <div class="stores-header">
      <h1 class="page-title">推荐店铺</h1>
      <button class="all-stores-button" @click="goToAllStores">查看全部商店</button>
    </div>

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

.stores-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
}

.page-title {
  color: #ff6347;
  font-size: 28px;
  margin: 0;
}

.all-stores-button {
  background-color: #ff6347;
  color: white;
  border: none;
  padding: 10px 20px;
  border-radius: 8px;
  cursor: pointer;
  font-size: 16px;
  transition: background-color 0.3s;
}

.all-stores-button:hover {
  background-color: #ff4500;
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
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 25px;
  margin-bottom: 40px;
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
  height: 200px;
  object-fit: cover;
}

.store-info {
  padding: 15px;
}

.store-info h3 {
  font-size: 20px;
  margin-bottom: 10px;
  color: #333;
}

.store-info p {
  color: #666;
  font-size: 16px;
  margin-bottom: 15px;
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
  color: #ffc107;
  font-size: 18px;
  margin-right: 8px;
}

.rating-value {
  color: #666;
  font-size: 16px;
}

.admin-controls {
  margin-top: 30px;
  text-align: center;
}

.add-button {
  background-color: #ff6347;
  color: white;
  border: none;
  padding: 12px 24px;
  border-radius: 8px;
  cursor: pointer;
  font-size: 16px;
  font-weight: bold;
  transition: background-color 0.3s;
}

.add-button:hover {
  background-color: #ff4500;
}

@media (max-width: 768px) {
  .stores-header {
    flex-direction: column;
    gap: 15px;
    text-align: center;
  }

  .stores-grid {
    grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
    gap: 15px;
  }

  .store-info h3 {
    font-size: 18px;
  }

  .store-info p {
    font-size: 14px;
  }
}
</style>
