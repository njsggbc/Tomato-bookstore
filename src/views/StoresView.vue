<!-- src/views/StoresView.vue -->
<!--
 * 这是书店列表页面
 * 主要功能：
 * 1. 显示所有书店
 * 2. 可以按评分排序
 * 3. 可以按地区筛选
 * 4. 可以搜索书店
 * 5. 点击书店可以进入详情页
-->
<script setup lang="ts">
import { onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useStoreStore } from '../stores/Store'
import { useUserStore } from '../stores/User'
import { ROLES } from '../constants/roles'

const router = useRouter()
const storeStore = useStoreStore()
const userStore = useUserStore()

// 前往店铺详情
const goToStore = (storeId: number) => {
  router.push(`/store/${storeId}`)
}

onMounted(() => {
  storeStore.fetchAllStores()
})
</script>

<template>
  <div class="stores-container">
    <h1>全部商店</h1>

    <div v-if="storeStore.loading" class="loading">
      <p>加载中...</p>
    </div>

    <div v-else-if="storeStore.error" class="error">
      <p>{{ storeStore.error }}</p>
    </div>

    <div v-else-if="storeStore.stores.length === 0" class="no-stores">
      <p>暂无商店</p>
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
          <div class="store-details">
            <p><strong>地址：</strong>{{ store.address }}</p>
            <p><strong>电话：</strong>{{ store.phone }}</p>
          </div>
          <div class="store-rating" v-if="store.rating">
            <span class="stars">★★★★★</span>
            <span class="rating-value">{{ store.rating }}</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 管理员控制区域 -->
    <div v-if="userStore.user?.role === ROLES.ADMIN" class="admin-controls">
      <button class="add-button" @click="router.push('/store/create')">添加新店铺</button>
    </div>
  </div>
</template>

<style scoped>
.stores-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

h1 {
  color: #ff6347;
  font-size: 28px;
  margin-bottom: 30px;
  text-align: center;
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

.store-details {
  margin: 10px 0;
}

.store-details p {
  margin: 5px 0;
  font-size: 14px;
}

.store-rating {
  display: flex;
  align-items: center;
  margin-top: 10px;
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