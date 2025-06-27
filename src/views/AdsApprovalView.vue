<template>
  <div class="ads-approval-container">
    <h1>广告审批</h1>
    <div v-if="pendingAds.length === 0" class="no-ads">暂无待审批广告</div>
    <div v-else class="ads-list">
      <div v-for="ad in pendingAds" :key="ad.id" class="ad-card">
        <img :src="ad.imageUrl" :alt="ad.title" class="ad-image" />
        <div class="ad-info">
          <h3>{{ ad.title }}</h3>
          <p>{{ ad.description }}</p>
          <p><strong>店铺：</strong>{{ ad.storeName }}</p>
          <p><strong>投放时间：</strong>{{ ad.startDate }} ~ {{ ad.endDate }}</p>
        </div>
        <div class="ad-actions">
          <button class="btn-approve" @click="approveAd(ad.id)">同意</button>
          <button class="btn-reject" @click="rejectAd(ad.id)">拒绝</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useAdvertisementStore } from '../stores/Advertisement'

const adStore = useAdvertisementStore()
const pendingAds = ref<any[]>([])

const fetchPendingAds = () => {
  pendingAds.value = adStore.advertisements.filter((ad: any) => ad.status === 'pending')
}

const approveAd = (adId: number) => {
  adStore.updateAdStatus(adId, 'approved')
  fetchPendingAds()
}

const rejectAd = (adId: number) => {
  adStore.updateAdStatus(adId, 'rejected')
  fetchPendingAds()
}

onMounted(() => {
  fetchPendingAds()
})
</script>

<style scoped>
.ads-approval-container {
  max-width: 900px;
  margin: 0 auto;
  padding: 30px;
}
.ads-list {
  display: flex;
  flex-direction: column;
  gap: 24px;
}
.ad-card {
  display: flex;
  align-items: center;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.08);
  padding: 20px;
  gap: 20px;
}
.ad-image {
  width: 120px;
  height: 120px;
  object-fit: cover;
  border-radius: 4px;
}
.ad-info {
  flex: 1;
}
.ad-actions {
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.btn-approve {
  background: #28a745;
  color: #fff;
  border: none;
  padding: 8px 18px;
  border-radius: 4px;
  cursor: pointer;
  font-weight: bold;
}
.btn-approve:hover {
  background: #218838;
}
.btn-reject {
  background: #dc3545;
  color: #fff;
  border: none;
  padding: 8px 18px;
  border-radius: 4px;
  cursor: pointer;
  font-weight: bold;
}
.btn-reject:hover {
  background: #c82333;
}
.no-ads {
  text-align: center;
  color: #888;
  font-size: 18px;
  margin-top: 40px;
}
</style> 