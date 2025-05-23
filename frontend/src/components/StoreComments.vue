<!-- src/components/StoreComments.vue -->
<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useCommentStore } from '../stores/Comment'
import { useUserStore } from '../stores/User'
import { ROLES } from '../constants/roles'

const props = defineProps({
  storeId: {
    type: Number,
    required: true
  },
  storeMerchantId: {
    type: Number,
    required: true
  }
})

const commentStore = useCommentStore()
const userStore = useUserStore()
const newComment = ref('')
const error = ref('')

// 计算属性判断用户角色
const isAdmin = computed(() => userStore.user?.role === ROLES.ADMIN)
const isMerchant = computed(() => userStore.user?.id === props.storeMerchantId)
const canAddComment = computed(() => userStore.isLoggedIn && !isAdmin.value && !isMerchant.value)

// 加载评论
const loadComments = async () => {
  await commentStore.fetchStoreComments(props.storeId)
}

// 提交评论
const submitComment = async () => {
  if (!newComment.value.trim()) {
    error.value = '评论内容不能为空'
    return
  }

  try {
    await commentStore.addComment(props.storeId, newComment.value)
    newComment.value = ''
    error.value = ''
  } catch (err) {
    error.value = '发表评论失败，请重试'
  }
}

// 删除评论 (管理员权限)
const removeComment = async (commentId: number) => {
  if (!isAdmin.value) return

  if (confirm('确定要删除这条评论吗？')) {
    try {
      await commentStore.deleteComment(commentId)
    } catch (err) {
      error.value = '删除评论失败'
    }
  }
}

onMounted(() => {
  loadComments()
})
</script>

<template>
  <div class="comments-section">
    <h2>店铺评论</h2>

    <!-- 评论表单 - 只对普通用户显示 -->
    <div v-if="canAddComment" class="comment-form">
      <h3>发表评论</h3>
      <div v-if="error" class="error-message">{{ error }}</div>
      <textarea
        v-model="newComment"
        placeholder="请输入您对这家店铺的评价..."
        rows="3"
      ></textarea>
      <button @click="submitComment" class="submit-button">发表评论</button>
    </div>

    <!-- 评论列表 -->
    <div class="comment-list">
      <div v-if="commentStore.loading" class="loading">加载中...</div>
      <div v-else-if="commentStore.comments.length === 0" class="no-comments">
        暂无评论
      </div>
      <div v-else>
        <div v-for="comment in commentStore.comments" :key="comment.id" class="comment-item">
          <div class="comment-header">
            <span class="comment-author">{{ comment.username }}</span>
            <span class="comment-date">{{ new Date(comment.createdAt).toLocaleString() }}</span>
          </div>
          <div class="comment-content">{{ comment.content }}</div>
          <div v-if="isAdmin" class="comment-actions">
            <button @click="removeComment(comment.id)" class="delete-button">删除</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.comments-section {
  margin-top: 40px;
}

h2 {
  color: #ff6347;
  margin-bottom: 20px;
}

.comment-form {
  background: white;
  padding: 20px;
  border-radius: 8px;
  margin-bottom: 30px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

h3 {
  color: #333;
  margin-bottom: 15px;
}

textarea {
  width: 100%;
  padding: 12px;
  border: 1px solid #ddd;
  border-radius: 8px;
  resize: vertical;
  margin-bottom: 15px;
  font-family: inherit;
}

.submit-button {
  background-color: #ff6347;
  color: white;
  border: none;
  padding: 10px 20px;
  border-radius: 8px;
  cursor: pointer;
  font-weight: bold;
  font-size: 16px;
}

.submit-button:hover {
  background-color: #ff4500;
}

.error-message {
  color: red;
  margin-bottom: 15px;
  padding: 10px;
  background-color: rgba(255, 0, 0, 0.1);
  border-radius: 4px;
}

.comment-list {
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.comment-item {
  padding: 20px;
  border-bottom: 1px solid #eee;
}

.comment-item:last-child {
  border-bottom: none;
}

.comment-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 10px;
}

.comment-author {
  font-weight: bold;
  color: #333;
}

.comment-date {
  color: #888;
  font-size: 0.9em;
}

.comment-content {
  line-height: 1.6;
}

.comment-actions {
  margin-top: 10px;
  text-align: right;
}

.delete-button {
  background-color: #ff6347;
  color: white;
  border: none;
  padding: 5px 10px;
  border-radius: 4px;
  cursor: pointer;
}

.loading, .no-comments {
  padding: 20px;
  text-align: center;
  color: #666;
}
</style>
