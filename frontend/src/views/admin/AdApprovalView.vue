<!-- src/views/admin/AdApprovalView.vue -->
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
const selectedAd = ref(null)
const showRejectModal = ref(false)
const showPreviewModal = ref(false)
const previewAd = ref(null)

// 预览广告
const previewAdContent = (ad) => {
  previewAd.value = ad
  showPreviewModal.value = true
}

// 审核通过
const approveAd = async (adId) => {
  try {
    await adminStore.approveAd(adId, true)
    alert('广告已通过审核')
  } catch (err) {
    alert('审核操作失败，请重试')
  }
}

// 审核拒绝
const rejectAd = (ad) => {
  selectedAd.value = ad
  showRejectModal.value = true
}

// 确认拒绝
const confirmReject = async () => {
  if (!selectedAd.value) return

  try {
    await adminStore.approveAd(selectedAd.value.id, false, rejectReason.value)
    showRejectModal.value = false
    rejectReason.value = ''
    selectedAd.value = null
    alert('已拒绝广告申请')
  } catch (err) {
    alert('操作失败，请重试')
  }
}

// 检查用户权限并加载待审核广告
onMounted(async () => {
  if (!userStore.user || userStore.user.role !== ROLES.ADMIN) {
    router.push('/home')
    return
  }

  await adminStore.fetchPendingAds()
})
</script>

<template>
  <div class="approval-container">
    <div class="approval-header">
      <h1>广告审批管理</h1>
      <button class="btn-secondary" @click="router.push('/admin/dashboard')">返回控制面板</button>
    </div>

    <div v-if="adminStore.loading" class="loading">
      <p>加载中...</p>
    </div>

    <div v-else-if="adminStore.error" class="error-message">
      {{ adminStore.error }}
    </div>

    <div v-else-if="adminStore.pendingAds.length === 0" class="no-data">
      <p>目前没有待审核的广告</p>
    </div>

    <div v-else class="approval-list">
      <div v-for="ad in adminStore.pendingAds" :key="ad.id" class="approval-item">
        <div class="ad-info">
          <div class="ad-image">
            <img :src="ad.imageUrl" :alt="ad.title" @click="previewAdContent(ad)" />
          </div>
          <div class="ad-details">
            <h3>{{ ad.title }}</h3>
            <p class="store-name">店铺：{{ ad.storeName }}</p>
            <p class="description">{{ ad.content }}</p>
            <p class="meta">投放时间: {{ new Date(ad.startDate).toLocaleDateString() }} 至 {{ new Date(ad.endDate).toLocaleDateString() }}</p>
            <p class="meta">申请时间: {{ new Date(ad.createdAt).toLocaleString() }}</p>
            <p class="link" @click="window.open(ad.targetUrl, '_blank')">目标链接: {{ ad.targetUrl }}</p>
          </div>
        </div>

        <div class="approval-actions">
          <button class="btn-preview" @click="previewAdContent(ad)">预览</button>
          <button class="btn-approve" @click="approveAd(ad.id)">通过</button>
          <button class="btn-reject" @click="rejectAd(ad)">拒绝</button>
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

    <!-- 广告预览弹窗 -->
    <div v-if="showPreviewModal && previewAd" class="modal-overlay">
      <div class="preview-modal">
        <button class="close-button" @click="showPreviewModal = false">×</button>
        <h3>广告预览: {{ previewAd.title }}</h3>
        <div class="preview-content">
          <img :src="previewAd.imageUrl" alt="广告预览" />
          <div class="preview-text">
            <h4>{{ previewAd.title }}</h4>
            <p>{{ previewAd.content }}</p>
          </div>
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

.ad-info {
  display: flex;
  gap: 20px;
}

.ad-image img {
  width: 200px;
  height: 150px;
  object-fit: cover;
  border-radius: 8px;
  cursor: pointer;
}

.ad-details {
  flex: 1;
}

.ad-details h3 {
  color: #333;
  margin-bottom: 10px;
}

.store-name {
  color: #ff6347;
  font-weight: bold;
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

.link {
  color: #0077cc;
  cursor: pointer;
  text-decoration: underline;
}

.approval-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.btn-preview, .btn-approve, .btn-reject, .btn-secondary, .btn-primary {
  padding: 10px 20px;
  border-radius: 5px;
  font-weight: bold;
  cursor: pointer;
  border: none;
}

.btn-preview {
  background-color: #2196f3;
  color: white;
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

.preview-modal {
  background: white;
  padding: 20px;
  border-radius: 10px;
  width: 90%;
  max-width: 800px;
  position: relative;
}

.close-button {
  position: absolute;
  right: 10px;
  top: 10px;
  background: none;
  border: none;
  font-size: 24px;
  cursor: pointer;
  color: #666;
}

.preview-content {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.preview-content img {
  max-width: 100%;
  max-height: 400px;
  object-fit: contain;
  margin-bottom: 20px;
}

.preview-text {
  width: 100%;
  padding: 15px;
  background-color: #f9f9f9;
  border-radius: 8px;
}

.modal h3, .preview-modal h3 {
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

@media (max-width: 768px) {
  .ad-info {
    flex-direction: column;
  }

  .ad-image img {
    width: 100%;
    height: auto;
  }
}
</style>
