// src/stores/Comment.ts
import { defineStore } from 'pinia'
import { ref } from 'vue'
import { api } from '@/Services/api'
import { useUserStore } from './User'
import { ROLES } from '../constants/roles'

// 评论信息接口
interface Comment {
  id: number
  content: string
  userId: number
  username: string
  storeId: number
  createdAt: string
}

// 模拟评论数据
const mockComments: Comment[] = [
  {
    id: 1,
    content: '这家书店的书籍种类很丰富，服务态度也很好！',
    userId: 1,
    username: '张三',
    storeId: 1,
    createdAt: '2024-01-15T10:30:00Z'
  },
  {
    id: 2,
    content: '环境很安静，适合读书，推荐大家来逛逛。',
    userId: 2,
    username: '李四',
    storeId: 1,
    createdAt: '2024-01-16T14:20:00Z'
  }
]

export const useCommentStore = defineStore('comment', () => {
  const comments = ref<Comment[]>([])
  const loading = ref(false)
  const error = ref('')
  const userStore = useUserStore()

  // 获取店铺评论
  const fetchStoreComments = async (storeId: number) => {
    loading.value = true
    error.value = ''

    try {
      // 模拟API请求延迟
      await new Promise(resolve => setTimeout(resolve, 500))
      
      // 过滤出指定店铺的评论
      comments.value = mockComments.filter(comment => comment.storeId === storeId)
      return comments.value
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
    if (!userStore.isLoggedIn) {
      throw new Error('请先登录后再发表评论')
    }

    if (userStore.user?.role !== ROLES.CUSTOMER) {
      throw new Error('只有顾客才能发表评论')
    }

    loading.value = true
    error.value = ''

    try {
      // 模拟API请求延迟
      await new Promise(resolve => setTimeout(resolve, 500))
      
      // 创建新评论
      const newComment: Comment = {
        id: mockComments.length + 1,
        content,
        userId: userStore.user.id,
        username: userStore.user.username,
        storeId,
        createdAt: new Date().toISOString()
      }
      
      // 添加到评论列表
      mockComments.unshift(newComment)
      comments.value.unshift(newComment)
      
      return newComment
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
    if (!userStore.isLoggedIn || userStore.user?.role !== ROLES.ADMIN) {
      throw new Error('没有权限删除评论')
    }

    loading.value = true
    error.value = ''

    try {
      // 模拟API请求延迟
      await new Promise(resolve => setTimeout(resolve, 500))
      
      // 从评论列表中移除
      const index = mockComments.findIndex(comment => comment.id === commentId)
      if (index !== -1) {
        mockComments.splice(index, 1)
      }
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
