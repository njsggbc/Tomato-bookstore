<!--
 * 这是所有书店的列表页面
 * 主要功能：
 * 1. 显示所有书店的列表
 * 2. 可以按评分高低排序
 * 3. 可以搜索书店
 * 4. 可以按地区筛选
 * 5. 点击书店可以进入详情页
-->
<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useStoreStore } from '../stores/Store'
import { useUserStore } from '../stores/User'
import { ROLES } from '../constants/roles'

const router = useRouter()
const storeStore = useStoreStore()
const userStore = useUserStore()
const loading = ref(false)
const error = ref('')
const currentPage = ref(1)
const pageSize = ref(12)
const totalPages = ref(1)

// 计算属性判断用户角色
const isAdmin = ref(userStore.user?.role === ROLES.ADMIN)

// 前往店铺页面
const goToStore = (storeId) => {
  router.push(`/store/${storeId}`)
}

// 加载所有店铺
const loadAllStores = async () => {
  loading.value = true
  error.value = ''

  try {
    // 调用store的方法获取所有店铺
    await storeStore.fetchAllStores(currentPage.value, pageSize.value)
    totalPages.value = Math.ceil(storeStore.totalStores / pageSize.value)
  } catch (err) {
    error.value = '加载店铺失败，请稍后再试'
  } finally {
    loading.value = false
  }
}

// 翻页函数
const changePage = (page) => {
  currentPage.value = page
  loadAllStores()
}

onMounted(() => {
  loadAllStores()
})
</script>

<template>
  <div class="stores-container">
    <h1 class="page-title">所有店铺</h1>

    <div v-if="loading" class="loading">
      <p>加载中...</p>
    </div>

    <div v-else-if="error" class="error">
      <p>{{ error }}</p>
    </div>

    <div v-else-if="storeStore.stores.length === 0" class="no-stores">
      <p>暂无店铺数据</p>
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

    <!-- 分页控件 -->
    <div v-if="totalPages > 1" class="pagination">
      <button
        :disabled="currentPage === 1"
        @click="changePage(currentPage - 1)"
        class="page-button"
      >
        上一页
      </button>
      <span class="page-info">{{ currentPage }} / {{ totalPages }}</span>
      <button
        :disabled="currentPage === totalPages"
        @click="changePage(currentPage + 1)"
        class="page-button"
      >
        下一页
      </button>
    </div>

    <!-- 管理员控制区域 -->
    <div v-if="isAdmin" class="admin-controls">
      <button class="add-button" @click="router.push('/store/new')">添加新店铺</button>
    </div>
  </div>
</template>

<style scoped>
.stores-container {
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

/* 分页控件样式 */
.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  margin-top: 30px;
  gap: 15px;
}

.page-button {
  background-color: #ff6347;
  color: white;
  border: none;
  padding: 8px 16px;
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
}

.page-button:disabled {
  background-color: #ccc;
  cursor: not-allowed;
}

.page-info {
  font-size: 16px;
  color: #333;
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
