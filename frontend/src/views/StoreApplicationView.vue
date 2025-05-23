<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useStoreStore } from '../stores/Store'
import { useUserStore } from '../stores/User'
import { ROLES } from '../constants/roles'

const router = useRouter()
const storeStore = useStoreStore()
const userStore = useUserStore()

const name = ref('')
const description = ref('')
const category = ref('')
const imageUrl = ref('')
const businessLicense = ref('')
const contactPhone = ref('')
const address = ref('')

const loading = ref(false)
const errorMessage = ref('')
const successMessage = ref('')

// 检查用户是否是商家角色
onMounted(() => {
  if (!userStore.isLoggedIn) {
    router.push('/login?redirect=/store/apply')
    return
  }

  if (userStore.user?.role !== ROLES.MERCHANT) {
    router.push('/home')
    return
  }
})

const submitApplication = async () => {
  if (!name.value || !description.value || !category.value) {
    errorMessage.value = '请填写必要的店铺信息'
    return
  }

  if (!businessLicense.value) {
    errorMessage.value = '请提供营业执照信息'
    return
  }

  loading.value = true
  errorMessage.value = ''

  try {
    const storeData = {
      name: name.value,
      description: description.value,
      category: category.value,
      imageUrl: imageUrl.value || 'https://via.placeholder.com/200',
      businessLicense: businessLicense.value,
      contactPhone: contactPhone.value,
      address: address.value,
      status: 'pending' // 默认为待审核状态
    }

    await storeStore.createStore(storeData)
    successMessage.value = '店铺申请已提交，请等待管理员审核'
    errorMessage.value = ''

    // 清空表单
    name.value = ''
    description.value = ''
    category.value = ''
    imageUrl.value = ''
    businessLicense.value = ''
    contactPhone.value = ''
    address.value = ''

    // 3秒后跳转回首页
    setTimeout(() => {
      router.push('/home')
    }, 3000)
  } catch (err) {
    errorMessage.value = '提交店铺申请失败，请重试'
    successMessage.value = ''
  } finally {
    loading.value = false
  }
}

const cancel = () => {
  router.push('/home')
}
</script>

<template>
  <div class="store-application-container">
    <h1>申请开设店铺</h1>

    <div v-if="errorMessage" class="error-message">
      {{ errorMessage }}
    </div>

    <div v-if="successMessage" class="success-message">
      {{ successMessage }}
    </div>

    <form @submit.prevent="submitApplication">
      <div class="form-group">
        <label for="name">店铺名称</label>
        <input
          id="name"
          v-model="name"
          type="text"
          placeholder="请输入店铺名称"
          required
        />
      </div>

      <div class="form-group">
        <label for="description">店铺描述</label>
        <textarea
          id="description"
          v-model="description"
          rows="5"
          placeholder="请详细描述您的店铺特色和经营方向"
          required
        ></textarea>
      </div>

      <div class="form-group">
        <label for="category">店铺分类</label>
        <select id="category" v-model="category" required>
          <option value="">-- 请选择分类 --</option>
          <option value="文学小说">文学小说</option>
          <option value="教育考试">教育考试</option>
          <option value="人文社科">人文社科</option>
          <option value="经管励志">经管励志</option>
          <option value="科技计算机">科技计算机</option>
          <option value="生活艺术">生活艺术</option>
          <option value="少儿读物">少儿读物</option>
          <option value="进口原版">进口原版</option>
        </select>
      </div>

      <div class="form-group">
        <label for="imageUrl">店铺LOGO图片URL</label>
        <input
          id="imageUrl"
          v-model="imageUrl"
          type="text"
          placeholder="请输入店铺LOGO图片地址"
        />
      </div>

      <div class="form-group">
        <label for="businessLicense">营业执照号码</label>
        <input
          id="businessLicense"
          v-model="businessLicense"
          type="text"
          placeholder="请输入营业执照号码"
          required
        />
      </div>

      <div class="form-group">
        <label for="contactPhone">联系电话</label>
        <input
          id="contactPhone"
          v-model="contactPhone"
          type="tel"
          placeholder="请输入店铺联系电话"
        />
      </div>

      <div class="form-group">
        <label for="address">店铺地址</label>
        <textarea
          id="address"
          v-model="address"
          rows="3"
          placeholder="请输入店铺实体地址（如有）"
        ></textarea>
      </div>

      <div class="form-notice">
        <p>注意事项：</p>
        <ul>
          <li>提交申请后，管理员将会在1-3个工作日内进行审核</li>
          <li>请确保提供的营业执照信息真实有效</li>
          <li>审核通过后，您将收到系统通知，并可以正式开始运营店铺</li>
        </ul>
      </div>

      <div class="form-actions">
        <button type="submit" class="btn-primary" :disabled="loading">
          {{ loading ? '提交中...' : '提交申请' }}
        </button>
        <button type="button" class="btn-secondary" @click="cancel">取消</button>
      </div>
    </form>
  </div>
</template>

<style scoped>
.store-application-container {
  max-width: 800px;
  margin: 0 auto;
  padding: 30px;
  background: #ffffff;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  border-radius: 12px;
}

h1 {
  text-align: center;
  color: #ff6347;
  margin-bottom: 30px;
}

.form-group {
  margin-bottom: 20px;
}

label {
  display: block;
  margin-bottom: 8px;
  font-weight: bold;
}

input, textarea, select {
  width: 100%;
  padding: 12px;
  border: 1px solid #ddd;
  border-radius: 8px;
  font-size: 16px;
}

textarea {
  resize: vertical;
}

select {
  height: 45px;
  background-color: #fff;
}

.form-notice {
  margin: 25px 0;
  padding: 15px;
  background-color: #f9f9f9;
  border-radius: 8px;
  border-left: 4px solid #ff6347;
}

.form-notice p {
  font-weight: bold;
  margin-bottom: 10px;
}

.form-notice ul {
  padding-left: 20px;
}

.form-notice li {
  margin-bottom: 5px;
  color: #666;
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
  transition: all 0.3s;
}

.btn-primary {
  background-color: #ff6347;
  color: white;
  border: none;
}

.btn-primary:hover {
  background-color: #ff4500;
  transform: scale(1.05);
}

.btn-primary:disabled {
  background-color: #ccc;
  cursor: not-allowed;
  transform: none;
}

.btn-secondary {
  background-color: #f5f5f5;
  color: #333;
  border: 1px solid #ddd;
}

.btn-secondary:hover {
  background-color: #e5e5e5;
}

.error-message {
  color: red;
  margin-bottom: 20px;
  padding: 14px;
  background-color: rgba(255, 0, 0, 0.1);
  border-radius: 8px;
  text-align: center;
}

.success-message {
  color: green;
  margin-bottom: 20px;
  padding: 14px;
  background-color: rgba(0, 128, 0, 0.1);
  border-radius: 8px;
  text-align: center;
}

@media (max-width: 768px) {
  .store-application-container {
    padding: 20px;
    margin: 0 15px;
  }

  .form-actions {
    flex-direction: column;
    gap: 10px;
  }

  .btn-primary, .btn-secondary {
    width: 100%;
  }
}
</style>
