<!-- src/components/AdvertisementCarousel.vue -->
<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAdvertisementStore } from '../stores/Advertisement'
import type { Advertisement } from '../stores/Advertisement'

const router = useRouter()
const adStore = useAdvertisementStore()
const currentAdIndex = ref(0)
const approvedAds = ref<Advertisement[]>([])

// 跳转到广告关联的店铺详情页
const goToStoreDetail = (storeId: number) => {
  router.push(`/store/${storeId}`)
}

// 轮播控制
const nextAd = () => {
  if (approvedAds.value.length > 0) {
    currentAdIndex.value = (currentAdIndex.value + 1) % approvedAds.value.length
  }
}

const prevAd = () => {
  if (approvedAds.value.length > 0) {
    currentAdIndex.value = (currentAdIndex.value - 1 + approvedAds.value.length) % approvedAds.value.length
  }
}

// 自动轮播
onMounted(async () => {
  await adStore.fetchAdvertisements()
  approvedAds.value = adStore.advertisements.filter(ad => ad.status === 'approved')
  // 每5秒自动切换广告
  setInterval(() => {
    nextAd()
  }, 5000)
})
</script>

<template>
  <div v-if="approvedAds.length > 0" class="ad-carousel">
    <div class="carousel-container">
      <button class="carousel-button prev" @click="prevAd">❮</button>

      <div
        class="carousel-slide"
        @click="goToStoreDetail(approvedAds[currentAdIndex].storeId)"
      >
        <img
          :src="approvedAds[currentAdIndex].imageUrl"
          :alt="approvedAds[currentAdIndex].title"
        />
        <div class="carousel-caption">
          <h3>{{ approvedAds[currentAdIndex].title }}</h3>
          <p>{{ approvedAds[currentAdIndex].description }}</p>
        </div>
      </div>

      <button class="carousel-button next" @click="nextAd">❯</button>
    </div>

    <div class="carousel-dots">
      <span
        v-for="(_, index) in approvedAds"
        :key="index"
        :class="{ active: index === currentAdIndex }"
        @click="currentAdIndex = index"
      ></span>
    </div>
  </div>
</template>

<style scoped>
.ad-carousel {
  margin-bottom: 40px;
}

.carousel-container {
  position: relative;
  width: 100%;
  height: 300px;
  overflow: hidden;
  border-radius: 12px;
}

.carousel-slide {
  width: 100%;
  height: 100%;
  cursor: pointer;
}

.carousel-slide img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.carousel-caption {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  background: rgba(0, 0, 0, 0.6);
  color: white;
  padding: 15px;
}

.carousel-caption h3 {
  margin-bottom: 8px;
  font-size: 20px;
}

.carousel-button {
  position: absolute;
  top: 50%;
  transform: translateY(-50%);
  background: rgba(255, 255, 255, 0.5);
  border: none;
  color: #333;
  font-size: 24px;
  width: 40px;
  height: 40px;
  border-radius: 50%;
  cursor: pointer;
  z-index: 10;
}

.carousel-button.prev {
  left: 10px;
}

.carousel-button.next {
  right: 10px;
}

.carousel-dots {
  display: flex;
  justify-content: center;
  margin-top: 10px;
}

.carousel-dots span {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  background: #ccc;
  margin: 0 5px;
  cursor: pointer;
}

.carousel-dots span.active {
  background: #ff6347;
}

@media (max-width: 768px) {
  .carousel-container {
    height: 200px;
  }

  .carousel-caption h3 {
    font-size: 16px;
  }

  .carousel-caption p {
    font-size: 14px;
  }
}
</style>
