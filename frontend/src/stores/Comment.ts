// src/stores/Comment.ts
import { defineStore } from 'pinia'
import { ref } from 'vue'
import { api } from '@/Services/api'

// 评论信息接口
interface Comment {
  id: number
  content: string
  userId: number
  username: string
  storeId: number
  createdAt: string
}

export const useCommentStore = defineStore('comment', () => {
  const comments = ref<Comment[]>([])
  const loading = ref(false)
  const error = ref('')

  // 获取店铺评论
  const fetchStoreComments = async (storeId: number) => {
    loading.value = true
    error.value = ''

    try {
      const response = await api.get(`/stores/${storeId}/comments`)
      comments.value = response.data
      return response.data
    } catch (err: any) {
      error.value = '获取评论失败'
      console.error(err)
      return []
    } finally {
      loading.value = false
    }
  }

  // 添加评论
  const addComment = async (storeId: number, content: string) => {
    loading.value = true
    error.value = ''

    try {
      const response = await api.post(`/stores/${storeId}/comments`, { content })
      comments.value.unshift(response.data) // 将新评论添加到列表开头
      return response.data
    } catch (err: any) {
      error.value = '发表评论失败'
      console.error(err)
      throw err
    } finally {
      loading.value = false
    }
  }

  // 删除评论 (管理员权限)
  const deleteComment = async (commentId: number) => {
    loading.value = true
    error.value = ''

    try {
      await api.delete(`/comments/${commentId}`)
      // 从评论列表中移除
      comments.value = comments.value.filter(comment => comment.id !== commentId)
      return true
    } catch (err: any) {
      error.value = '删除评论失败'
      console.error(err)
      throw err
    } finally {
      loading.value = false
    }
  }

  return {
    comments,
    loading,
    error,
    fetchStoreComments,
    addComment,
    deleteComment
  }
})
