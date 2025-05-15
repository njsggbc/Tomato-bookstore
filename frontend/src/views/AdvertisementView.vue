<!-- src/views/AdvertisementView.vue -->
<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAdvertisementStore } from '../stores/Advertisement'
import { useBookStore } from '../stores/Book'

const router = useRouter()
const adStore = useAdvertisementStore()
const bookStore = useBookStore()
const errorMessage = ref('')

// 跳转到广告关联的书籍详情页
const goToProductDetail = async (productId: number) => {
  try {
    // 导航到书籍详情页
    router.push(`/book/${productId}`)
  } catch (error) {
    errorMessage.value = '无法跳转到指定商品'
  }
}

onMounted(async () => {
  try {
    await adStore.fetchActiveAdvertisements()
  } catch (error) {
    errorMessage.value = '加载广告失败'
  }
})
</script>

<template>
  <div class="advertisement-container">
    <h1 class="page-title">精选推广</h1>

    <div v-if="adStore.loading" class="loading">
      <p>加载中...</p>
    </div>

    <div v-else-if="errorMessage" class="error-message">
      {{ errorMessage }}
    </div>

    <div v-else-if="adStore.advertisements.length === 0" class="no-ads">
      <p>暂无推广内容</p>
    </div>

    <div v-else class="advertisements-grid">
      <div
        v-for="ad in adStore.advertisements"
        :key="ad.id"
        class="ad-card"
        @click="goToProductDetail(ad.productId)"
      >
        <div class="ad-image">
          <img :src="ad.imageUrl" :alt="ad.title" />
        </div>
        <div class="ad-info">
          <h3>{{ ad.title }}</h3>
          <p>{{ ad.description }}</p>
          <button class="view-button">查看详情</button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.advertisement-container {
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

.loading, .error-message, .no-ads {
  text-align: center;
  padding: 40px;
  font-size: 18px;
}

.error-message {
  color: #dc3545;
  margin-bottom: 20px;
  padding: 14px;
  background-color: rgba(255, 0, 0, 0.1);
  border-radius: 8px;
}

.advertisements-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 25px;
}

.ad-card {
  background: white;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.1);
  transition: transform 0.3s, box-shadow 0.3s;
  cursor: pointer;
}

.ad-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 6px 14px rgba(0, 0, 0, 0.15);
}

.ad-image img {
  width: 100%;
  height: 200px;
  object-fit: cover;
}

.ad-info {
  padding: 15px;
}

.ad-info h3 {
  font-size: 20px;
  margin-bottom: 10px;
  color: #333;
}

.ad-info p {
  color: #666;
  font-size: 16px;
  margin-bottom: 15px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.view-button {
  background-color: #ff6347;
  color: white;
  border: none;
  padding: 10px 18px;
  border-radius: 8px;
  font-weight: bold;
  cursor: pointer;
  transition: background-color 0.3s;
}

.view-button:hover {
  background-color: #ff4500;
}

@media (max-width: 768px) {
  .advertisements-grid {
    grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
    gap: 15px;
  }

  .ad-info h3 {
    font-size: 18px;
  }

  .ad-info p {
    font-size: 14px;
  }
}
</style>
