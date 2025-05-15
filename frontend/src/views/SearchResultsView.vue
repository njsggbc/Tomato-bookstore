<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useStoreStore } from '../stores/Store'

const router = useRouter()
const storeStore = useStoreStore()
const categories = ref([
  { id: 1, name: '文学小说', description: '包含各类小说、散文、诗歌等文学作品', imageUrl: 'https://via.placeholder.com/200?text=文学小说' },
  { id: 2, name: '经济管理', description: '商业、经济、管理类书籍', imageUrl: 'https://via.placeholder.com/200?text=经济管理' },
  { id: 3, name: '科技计算机', description: '科学技术、计算机编程相关书籍', imageUrl: 'https://via.placeholder.com/200?text=科技计算机' },
  { id: 4, name: '生活艺术', description: '生活方式、艺术设计类图书', imageUrl: 'https://via.placeholder.com/200?text=生活艺术' },
  { id: 5, name: '历史文化', description: '历史、传统文化类书籍', imageUrl: 'https://via.placeholder.com/200?text=历史文化' },
  { id: 6, name: '教育考试', description: '教材、考试辅导类书籍', imageUrl: 'https://via.placeholder.com/200?text=教育考试' }
])
const loading = ref(false)
const error = ref('')

// 导航到特定分类
const goToCategory = (categoryId) => {
  // 调用store的方法获取指定分类的店铺
  router.push(`/category/${categoryId}`)
}

// 返回首页
const backToHome = () => {
  router.push('/home')
}

// 模拟获取分类数据
onMounted(() => {
  loading.value = true

  // 模拟API请求延迟
  setTimeout(() => {
    loading.value = false
  }, 500)

  // 实际项目中应该发起API请求获取真实分类数据
  // 例如：
  // fetchCategories()
})
</script>

<template>
  <div class="categories-container">
    <h1 class="page-title">店铺分类</h1>

    <div v-if="loading" class="loading">
      <p>加载中...</p>
    </div>

    <div v-else-if="error" class="error">
      <p>{{ error }}</p>
      <button class="back-button" @click="backToHome">返回首页</button>
    </div>

    <div v-else-if="categories.length === 0" class="no-categories">
      <p>暂无分类数据</p>
      <button class="back-button" @click="backToHome">返回首页</button>
    </div>

    <div v-else class="categories-grid">
      <div
        v-for="category in categories"
        :key="category.id"
        class="category-card"
        @click="goToCategory(category.id)"
      >
        <div class="category-image">
          <img :src="category.imageUrl" :alt="category.name" />
        </div>
        <div class="category-info">
          <h3>{{ category.name }}</h3>
          <p>{{ category.description }}</p>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.categories-container {
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

.loading, .error, .no-categories {
  text-align: center;
  padding: 40px;
  font-size: 18px;
}

.error {
  color: #ff6347;
}

.back-button {
  background-color: #ff6347;
  color: white;
  border: none;
  padding: 10px 20px;
  border-radius: 8px;
  margin-top: 20px;
  cursor: pointer;
  font-size: 16px;
}

.categories-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
  gap: 25px;
}

.category-card {
  background: white;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.1);
  transition: transform 0.3s, box-shadow 0.3s;
  cursor: pointer;
}

.category-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 6px 14px rgba(0, 0, 0, 0.15);
}

.category-image img {
  width: 100%;
  height: 180px;
  object-fit: cover;
}

.category-info {
  padding: 15px;
}

.category-info h3 {
  font-size: 18px;
  margin-bottom: 8px;
  color: #333;
}

.category-info p {
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

/* 响应式样式 */
@media (max-width: 768px) {
  .categories-grid {
    grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
    gap: 15px;
  }

  .category-info h3 {
    font-size: 16px;
  }

  .category-info p {
    font-size: 12px;
  }
}
</style>
