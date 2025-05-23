<!-- src/views/admin/StoreApprovalView.vue -->
<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../../stores/User'
import { useAdminStore } from '../../stores/Admin'
import { ROLES } from '../../constants/roles'

const router = useRouter()
const userStore = useUserStore()
const adminStore = useAdminStore()
const rejectReason = ref('')
const selectedStore = ref(null)
const showRejectModal = ref(false)

// 审核通过
const approveStore = async (storeId) => {
  try {
    await adminStore.approveStore(storeId, true)
    alert('店铺已通过审核')
  } catch (err) {
    alert('审核操作失败，请重试')
  }
}

// 审核拒绝
const rejectStore = (store) => {
  selectedStore.value = store
  showRejectModal.value = true
}

// 确认拒绝
const confirmReject = async () => {
  if (!selectedStore.value) return

  try {
    await adminStore.approveStore(selectedStore.value.id, false, rejectReason.value)
    showRejectModal.value = false
    rejectReason.value = ''
    selectedStore.value = null
    alert('已拒绝店铺申请')
  } catch (err) {
    alert('操作失败，请重试')
  }
}

// 检查用户权限并加载待审核店铺
onMounted(async () => {
  if (!userStore.user || userStore.user.role !== ROLES.ADMIN) {
    router.push('/home')
    return
  }

  await adminStore.fetchPendingStores()
})
</script>

<template>
  <div class="approval-container">
    <div class="approval-header">
      <h1>店铺审批管理</h1>
      <button class="btn-secondary" @click="router.push('/admin/dashboard')">返回控制面板</button>
    </div>

    <div v-if="adminStore.loading" class="loading">
      <p>加载中...</p>
    </div>

    <div v-else-if="adminStore.error" class="error-message">
      {{ adminStore.error }}
    </div>

    <div v-else-if="adminStore.pendingStores.length === 0" class="no-data">
      <p>目前没有待审核的店铺</p>
    </div>

    <div v-else class="approval-list">
      <div v-for="store in adminStore.pendingStores" :key="store.id" class="approval-item">
        <div class="store-info">
          <div class="store-image">
            <img :src="store.imageUrl" :alt="store.name" />
          </div>
          <div class="store-details">
            <h3>{{ store.name }}</h3>
            <p class="description">{{ store.description }}</p>
            <p class="meta">分类: {{ store.category }}</p>
            <p class="meta">店主: {{ store.ownerName }}</p>
            <p class="meta">申请时间: {{ new Date(store.createdAt).toLocaleString() }}</p>
          </div>
        </div>

        <div class="approval-actions">
          <button class="btn-approve" @click="approveStore(store.id)">通过</button>
          <button class="btn-reject" @click="rejectStore(store)">拒绝</button>
        </div>
      </div>
    </div>

    <!-- 拒绝原因弹窗 -->
    <div v-if="showRejectModal" class="modal-overlay">
      <div class="modal">
        <h3>拒绝原因</h3>
        <textarea v-model="rejectReason" placeholder="请输入拒绝原因..."></textarea>
        <div class="modal-actions">
          <button class="btn-secondary" @click="showRejectModal = false">取消</button>
          <button class="btn-primary" @click="confirmReject">确认拒绝</button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.approval-container {
  max-width: 1000px;
  margin: 0 auto;
  padding: 20px;
}

.approval-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
}

h1 {
  color: #ff6347;
}

.approval-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.approval-item {
  background: white;
  border-radius: 10px;
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.store-info {
  display: flex;
  gap: 20px;
}

.store-image img {
  width: 120px;
  height: 120px;
  object-fit: cover;
  border-radius: 8px;
}

.store-details {
  flex: 1;
}

.store-details h3 {
  color: #333;
  margin-bottom: 10px;
}

.description {
  color: #666;
  margin-bottom: 15px;
}

.meta {
  color: #888;
  font-size: 0.9em;
  margin-bottom: 5px;
}

.approval-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.btn-approve, .btn-reject, .btn-secondary, .btn-primary {
  padding: 10px 20px;
  border-radius: 5px;
  font-weight: bold;
  cursor: pointer;
  border: none;
}

.btn-approve {
  background-color: #4caf50;
  color: white;
}

.btn-reject {
  background-color: #f44336;
  color: white;
}

.btn-secondary {
  background-color: #f5f5f5;
  color: #333;
  border: 1px solid #ddd;
}

.btn-primary {
  background-color: #ff6347;
  color: white;
}

.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
}

.modal {
  background: white;
  padding: 20px;
  border-radius: 10px;
  width: 90%;
  max-width: 500px;
}

.modal h3 {
  margin-bottom: 15px;
}

.modal textarea {
  width: 100%;
  height: 150px;
  margin-bottom: 15px;
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 5px;
}

.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.loading, .error-message, .no-data {
  text-align: center;
  padding: 40px;
  font-size: 18px;
}

.error-message {
  color: #f44336;
}
</style>
