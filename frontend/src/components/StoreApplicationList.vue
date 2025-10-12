<!-- src/components/StoreApplicationList.vue -->
<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useUserStore } from '../stores/User'

interface StoreApplication {
  id: number
  name: string
  description: string
  address: string
  phone: string
  imageUrl: string
  status: 'pending' | 'approved' | 'rejected'
  createdAt: string
  userId: number
  rejectReason?: string
}

const userStore = useUserStore()
const applications = ref<StoreApplication[]>([])
const loading = ref(false)
const errorMessage = ref('')
const selectedApplication = ref<StoreApplication | null>(null)
const rejectReason = ref('')
const showRejectDialog = ref(false)

// 获取申请列表
const fetchApplications = () => {
  loading.value = true
  errorMessage.value = ''

  try {
    const storedApplications = JSON.parse(localStorage.getItem('storeApplications') || '[]')
    applications.value = storedApplications.sort((a: StoreApplication, b: StoreApplication) => {
      return new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime()
    })
  } catch (err) {
    errorMessage.value = '获取申请列表失败'
    console.error(err)
  } finally {
    loading.value = false
  }
}

// 处理申请
const handleApplication = async (application: StoreApplication, action: 'approve' | 'reject') => {
  if (action === 'reject' && !rejectReason.value) {
    errorMessage.value = '请输入拒绝理由'
    return
  }

  try {
    const storedApplications = JSON.parse(localStorage.getItem('storeApplications') || '[]')
    const updatedApplications = storedApplications.map((app: StoreApplication) => {
      if (app.id === application.id) {
        return {
          ...app,
          status: action === 'approve' ? 'approved' : 'rejected',
          rejectReason: action === 'reject' ? rejectReason.value : undefined
        }
      }
      return app
    })

    localStorage.setItem('storeApplications', JSON.stringify(updatedApplications))
    
    // 如果批准申请，创建店铺
    if (action === 'approve') {
      const stores = JSON.parse(localStorage.getItem('stores') || '[]')
      stores.push({
        id: application.id,
        name: application.name,
        description: application.description,
        address: application.address,
        phone: application.phone,
        imageUrl: application.imageUrl,
        userId: application.userId
      })
      localStorage.setItem('stores', JSON.stringify(stores))
    }

    showRejectDialog.value = false
    rejectReason.value = ''
    fetchApplications()
  } catch (err) {
    errorMessage.value = '操作失败'
    console.error(err)
  }
}

// 显示拒绝对话框
const showRejectModal = (application: StoreApplication) => {
  selectedApplication.value = application
  showRejectDialog.value = true
}

onMounted(() => {
  fetchApplications()
})
</script>

<template>
  <div class="application-list">
    <h2>店铺申请列表</h2>

    <div v-if="errorMessage" class="error-message">
      {{ errorMessage }}
    </div>

    <div v-if="loading" class="loading">
      加载中...
    </div>

    <div v-else-if="applications.length === 0" class="empty-message">
      暂无申请
    </div>

    <div v-else class="applications">
      <div v-for="application in applications" :key="application.id" class="application-card">
        <div class="application-header">
          <h3>{{ application.name }}</h3>
          <span :class="['status', application.status]">
            {{ 
              application.status === 'pending' ? '待审核' :
              application.status === 'approved' ? '已通过' : '已拒绝'
            }}
          </span>
        </div>

        <div class="application-image">
          <img :src="application.imageUrl" :alt="application.name" />
        </div>

        <div class="application-info">
          <p><strong>描述：</strong>{{ application.description }}</p>
          <p><strong>地址：</strong>{{ application.address }}</p>
          <p><strong>电话：</strong>{{ application.phone }}</p>
          <p><strong>申请时间：</strong>{{ new Date(application.createdAt).toLocaleString() }}</p>
          <p v-if="application.rejectReason"><strong>拒绝理由：</strong>{{ application.rejectReason }}</p>
        </div>

        <div v-if="application.status === 'pending'" class="application-actions">
          <button class="btn-approve" @click="handleApplication(application, 'approve')">
            同意
          </button>
          <button class="btn-reject" @click="showRejectModal(application)">
            拒绝
          </button>
        </div>
      </div>
    </div>

    <!-- 拒绝理由对话框 -->
    <div v-if="showRejectDialog" class="modal">
      <div class="modal-content">
        <h3>请输入拒绝理由</h3>
        <textarea
          v-model="rejectReason"
          placeholder="请输入拒绝理由"
          rows="4"
        ></textarea>
        <div class="modal-actions">
          <button class="btn-secondary" @click="showRejectDialog = false">
            取消
          </button>
          <button 
            class="btn-reject" 
            @click="handleApplication(selectedApplication!, 'reject')"
            :disabled="!rejectReason"
          >
            确认拒绝
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.application-list {
  padding: 20px;
}

h2 {
  color: #333;
  margin-bottom: 20px;
}

.error-message {
  color: #ff4444;
  background-color: rgba(255, 68, 68, 0.1);
  padding: 10px;
  border-radius: 4px;
  margin-bottom: 20px;
}

.loading {
  text-align: center;
  padding: 20px;
  color: #666;
}

.empty-message {
  text-align: center;
  padding: 40px;
  color: #666;
  background: #f5f5f5;
  border-radius: 8px;
}

.applications {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
}

.application-card {
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.application-header {
  padding: 15px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 1px solid #eee;
}

.application-header h3 {
  margin: 0;
  color: #333;
}

.status {
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 14px;
}

.status.pending {
  background-color: #fff3cd;
  color: #856404;
}

.status.approved {
  background-color: #d4edda;
  color: #155724;
}

.status.rejected {
  background-color: #f8d7da;
  color: #721c24;
}

.application-image {
  width: 100%;
  height: 200px;
  overflow: hidden;
}

.application-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.application-info {
  padding: 15px;
}

.application-info p {
  margin: 8px 0;
  color: #666;
}

.application-info strong {
  color: #333;
}

.application-actions {
  padding: 15px;
  display: flex;
  gap: 10px;
  border-top: 1px solid #eee;
}

.btn-approve, .btn-reject {
  flex: 1;
  padding: 8px 16px;
  border-radius: 4px;
  font-weight: bold;
  cursor: pointer;
  transition: background-color 0.3s;
}

.btn-approve {
  background-color: #28a745;
  color: white;
  border: none;
}

.btn-approve:hover {
  background-color: #218838;
}

.btn-reject {
  background-color: #dc3545;
  color: white;
  border: none;
}

.btn-reject:hover {
  background-color: #c82333;
}

.modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.modal-content {
  background: white;
  padding: 20px;
  border-radius: 8px;
  width: 90%;
  max-width: 500px;
}

.modal-content h3 {
  margin-top: 0;
  color: #333;
}

.modal-content textarea {
  width: 100%;
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 4px;
  margin: 10px 0;
  resize: vertical;
}

.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 20px;
}

.btn-secondary {
  background-color: #f5f5f5;
  color: #333;
  border: 1px solid #ddd;
  padding: 8px 16px;
  border-radius: 4px;
  cursor: pointer;
}

.btn-secondary:hover {
  background-color: #e5e5e5;
}

@media (max-width: 768px) {
  .applications {
    grid-template-columns: 1fr;
  }
}
</style> 