<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useStoreStore } from '../stores/Store'
import { useUserStore } from '../stores/User'

const router = useRouter()
const storeStore = useStoreStore()
const userStore = useUserStore()

const title = ref('')
const description = ref('')
const imageUrl = ref('')
const budget = ref(100)
const startDate = ref('')
const endDate = ref('')
const selectedStoreId = ref(0)
const targetAudience = ref('all') // all, students, adults, seniors
const loading = ref(false)
const errorMessage = ref('')
const successMessage = ref('')
const userStores = ref<any[]>([])

// 设置最小开始日期为当前日期
const minDate = new Date().toISOString().split('T')[0]
const maxDate = new Date(new Date().setMonth(new Date().getMonth() + 3)).toISOString().split('T')[0]

// 获取当前用户的店铺
const fetchUserStores = async () => {
  loading.value = true
  try {
    // 假设后端提供了获取用户店铺的API
    const stores = await storeStore.fetchStoresByOwner()
    userStores.value = stores
    if (stores.length > 0) {
      selectedStoreId.value = stores[0].id
    }
  } catch (err) {
    errorMessage.value = '获取店铺信息失败'
    console.error(err)
  } finally {
    loading.value = false
  }
}

const submitAdvertisement = async () => {
  if (!selectedStoreId.value) {
    errorMessage.value = '请选择要推广的店铺'
    return
  }

  if (!title.value || !description.value || !imageUrl.value) {
    errorMessage.value = '请填写完整的广告信息'
    return
  }

  if (!startDate.value || !endDate.value) {
    errorMessage.value = '请选择广告投放时间段'
    return
  }

  // 验证日期
  if (new Date(startDate.value) > new Date(endDate.value)) {
    errorMessage.value = '结束日期必须晚于开始日期'
    return
  }

  loading.value = true
  errorMessage.value = ''
  successMessage.value = ''

  try {
    // 这里需要调用后端API创建广告申请
    // 假设我们有一个 createAdvertisement 方法
    await createAdvertisement({
      storeId: selectedStoreId.value,
      title: title.value,
      description: description.value,
      imageUrl: imageUrl.value,
      budget: Number(budget.value),
      startDate: startDate.value,
      endDate: endDate.value,
      targetAudience: targetAudience.value
    })

    successMessage.value = '广告申请已提交，等待管理员审核'
    // 清空表单
    title.value = ''
    description.value = ''
    imageUrl.value = ''
    budget.value = 100
    startDate.value = ''
    endDate.value = ''
    targetAudience.value = 'all'

  } catch (err: any) {
    errorMessage.value = '提交广告申请失败：' + (err.message || '未知错误')
    console.error(err)
  } finally {
    loading.value = false
  }
}

// 模拟创建广告的API调用
const createAdvertisement = async (adData: any) => {
  // 实际项目中，这里应该是调用真实的API
  return new Promise((resolve) => {
    setTimeout(() => {
      console.log('已提交广告申请:', adData)
      resolve({ success: true })
    }, 1000)
  })
}

// 页面加载时获取店铺列表
onMounted(() => {
  if (!userStore.isLoggedIn) {
    router.push('/login?redirect=/ad/apply')
    return
  }
  fetchUserStores()
})
</script>

<template>
  <div class="ad-apply-container">
    <h1>申请店铺广告</h1>

    <div v-if="loading" class="loading-message">
      <p>加载中，请稍候...</p>
    </div>

    <div v-if="errorMessage" class="error-message">
      {{ errorMessage }}
    </div>

    <div v-if="successMessage" class="success-message">
      {{ successMessage }}
    </div>

    <form @submit.prevent="submitAdvertisement" class="ad-form">
      <div class="form-section">
        <h2>基本信息</h2>

        <div class="form-group">
          <label for="storeSelect">选择要推广的店铺</label>
          <select
            id="storeSelect"
            v-model="selectedStoreId"
            required
          >
            <option value="0" disabled>请选择店铺</option>
            <option v-for="store in userStores" :key="store.id" :value="store.id">
              {{ store.name }}
            </option>
          </select>
        </div>

        <div class="form-group">
          <label for="title">广告标题</label>
          <input
            id="title"
            v-model="title"
            type="text"
            placeholder="请输入吸引人的广告标题"
            required
            maxlength="30"
          />
          <small>最多30个字符</small>
        </div>

        <div class="form-group">
          <label for="description">广告描述</label>
          <textarea
            id="description"
            v-model="description"
            rows="4"
            placeholder="详细描述您的店铺特色和推广内容"
            required
            maxlength="200"
          ></textarea>
          <small>最多200个字符</small>
        </div>

        <div class="form-group">
          <label for="imageUrl">广告图片URL</label>
          <input
            id="imageUrl"
            v-model="imageUrl"
            type="url"
            placeholder="请输入广告图片的链接地址"
            required
          />
          <small>建议使用尺寸为1200×628像素的图片</small>
        </div>
      </div>

      <div class="form-section">
        <h2>投放设置</h2>

        <div class="form-group">
          <label for="budget">广告预算</label>
          <div class="budget-input">
            <span class="currency-symbol">￥</span>
            <input
              id="budget"
              v-model="budget"
              type="number"
              min="100"
              step="50"
              required
            />
          </div>
          <small>最低预算100元</small>
        </div>

        <div class="form-group date-range">
          <div class="date-input">
            <label for="startDate">开始日期</label>
            <input
              id="startDate"
              v-model="startDate"
              type="date"
              :min="minDate"
              :max="maxDate"
              required
            />
          </div>
          <div class="date-input">
            <label for="endDate">结束日期</label>
            <input
              id="endDate"
              v-model="endDate"
              type="date"
              :min="startDate || minDate"
              :max="maxDate"
              required
            />
          </div>
        </div>

        <div class="form-group">
          <label>目标受众</label>
          <div class="radio-group">
            <label class="radio-label">
              <input type="radio" v-model="targetAudience" value="all" />
              <span>所有用户</span>
            </label>
            <label class="radio-label">
              <input type="radio" v-model="targetAudience" value="students" />
              <span>学生</span>
            </label>
            <label class="radio-label">
              <input type="radio" v-model="targetAudience" value="adults" />
              <span>成年人</span>
            </label>
            <label class="radio-label">
              <input type="radio" v-model="targetAudience" value="seniors" />
              <span>老年人</span>
            </label>
          </div>
        </div>
      </div>

      <div class="form-actions">
        <button type="button" class="btn-secondary" @click="router.push('/store-dashboard')">
          取消
        </button>
        <button type="submit" class="btn-primary" :disabled="loading">
          {{ loading ? '提交中...' : '提交申请' }}
        </button>
      </div>
    </form>

    <div class="ad-guidelines">
      <h3>广告申请须知</h3>
      <ul>
        <li>广告内容必须真实，不得含有欺骗性信息</li>
        <li>禁止发布违反法律法规的内容</li>
        <li>广告申请提交后将由管理员审核，通常需要1-3个工作日</li>
        <li>广告费用将在审核通过后从您的账户中扣除</li>
        <li>如有问题，请联系客服：support@tomatomall.com</li>
      </ul>
    </div>
  </div>
</template>

<style scoped>
.ad-apply-container {
  max-width: 800px;
  margin: 0 auto;
  padding: 30px 20px;
}

h1 {
  text-align: center;
  color: #ff6347;
  margin-bottom: 30px;
  font-size: 28px;
}

.loading-message, .error-message, .success-message {
  padding: 15px;
  border-radius: 8px;
  margin-bottom: 20px;
  text-align: center;
}

.loading-message {
  background-color: #f8f9fa;
  color: #666;
}

.error-message {
  background-color: #fff0f0;
  color: #d9534f;
  border: 1px solid #f5c6cb;
}

.success-message {
  background-color: #f0fff0;
  color: #5cb85c;
  border: 1px solid #c3e6cb;
}

.ad-form {
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  padding: 20px;
  margin-bottom: 30px;
}

.form-section {
  margin-bottom: 30px;
  padding-bottom: 20px;
  border-bottom: 1px solid #eee;
}

.form-section h2 {
  color: #444;
  font-size: 20px;
  margin-bottom: 20px;
}

.form-group {
  margin-bottom: 20px;
}

label {
  display: block;
  margin-bottom: 8px;
  font-weight: bold;
  color: #333;
}

input, select, textarea {
  width: 100%;
  padding: 12px;
  border: 1px solid #ddd;
  border-radius: 8px;
  font-size: 16px;
}

textarea {
  resize: vertical;
}

small {
  display: block;
  color: #666;
  margin-top: 5px;
  font-size: 13px;
}

.budget-input {
  position: relative;
  display: flex;
  align-items: center;
}

.currency-symbol {
  position: absolute;
  left: 12px;
  font-weight: bold;
  color: #333;
}

.budget-input input {
  padding-left: 30px;
}

.date-range {
  display: flex;
  gap: 20px;
}

.date-input {
  flex: 1;
}

.radio-group {
  display: flex;
  flex-wrap: wrap;
  gap: 15px;
}

.radio-label {
  display: flex;
  align-items: center;
  gap: 5px;
  cursor: pointer;
}

.radio-label input {
  width: auto;
}

.form-actions {
  display: flex;
  justify-content: space-between;
  margin-top: 30px;
}

.btn-primary, .btn-secondary {
  padding: 12px 24px;
  border-radius: 8px;
  font-weight: bold;
  cursor: pointer;
}

.btn-primary {
  background-color: #ff6347;
  color: white;
  border: none;
}

.btn-primary:hover:not(:disabled) {
  background-color: #ff4500;
}

.btn-primary:disabled {
  background-color: #ffac9c;
  cursor: not-allowed;
}

.btn-secondary {
  background-color: #f5f5f5;
  color: #333;
  border: 1px solid #ddd;
}

.btn-secondary:hover {
  background-color: #e5e5e5;
}

.ad-guidelines {
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  padding: 20px;
}

.ad-guidelines h3 {
  color: #ff6347;
  margin-bottom: 15px;
}

.ad-guidelines ul {
  padding-left: 20px;
  color: #555;
}

.ad-guidelines li {
  margin-bottom: 8px;
}

@media (max-width: 600px) {
  .date-range {
    flex-direction: column;
    gap: 10px;
  }

  .form-actions {
    flex-direction: column-reverse;
    gap: 10px;
  }

  .btn-primary, .btn-secondary {
    width: 100%;
  }

  .radio-group {
    flex-direction: column;
    gap: 10px;
  }
}
</style>
