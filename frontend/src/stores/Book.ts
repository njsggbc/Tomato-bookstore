// src/stores/Book.ts
import { defineStore } from 'pinia'
import { ref } from 'vue'
import { api } from '@/Services/api'

// 书籍信息接口
interface Book {
  id: number
  title: string
  author: string
  price: number
  description: string
  imageUrl: string
  storeId: number
}

export const useBookStore = defineStore('book', () => {
  const books = ref<Book[]>([])
  const currentBook = ref<Book | null>(null)
  const loading = ref(false)
  const error = ref('')

  // 获取单本书籍详情
  const fetchBookById = async (bookId: number) => {
    loading.value = true
    error.value = ''

    try {
      const response = await api.get(`/books/${bookId}`)
      currentBook.value = response.data
      return response.data
    } catch (err: any) {
      error.value = '获取书籍详情失败'
      console.error(err)
      throw err
    } finally {
      loading.value = false
    }
  }

  // 更新书籍信息
  const updateBook = async (bookId: number, bookData: any) => {
    loading.value = true
    error.value = ''

    try {
      const response = await api.put(`/books/${bookId}`, bookData)
      currentBook.value = response.data
      return response.data
    } catch (err: any) {
      error.value = '更新书籍失败'
      console.error(err)
      throw err
    } finally {
      loading.value = false
    }
  }

  // 删除书籍
  const deleteBook = async (bookId: number) => {
    loading.value = true
    error.value = ''

    try {
      await api.delete(`/books/${bookId}`)
      return true
    } catch (err: any) {
      error.value = '删除书籍失败'
      console.error(err)
      throw err
    } finally {
      loading.value = false
    }
  }

  // 创建新书籍
  const createBook = async (storeId: number, bookData: any) => {
    loading.value = true
    error.value = ''

    try {
      const data = { ...bookData, storeId }
      const response = await api.post('/books', data)
      return response.data
    } catch (err: any) {
      error.value = '创建书籍失败'
      console.error(err)
      throw err
    } finally {
      loading.value = false
    }
  }

  // 搜索书籍
  const searchBooks = async (query: string) => {
    loading.value = true
    error.value = ''

    try {
      const response = await api.get(`/books/search?q=${encodeURIComponent(query)}`)
      books.value = response.data
      return response.data
    } catch (err: any) {
      error.value = '搜索书籍失败'
      console.error(err)
      return []
    } finally {
      loading.value = false
    }
  }

  return {
    books,
    currentBook,
    loading,
    error,
    fetchBookById,
    updateBook,
    deleteBook,
    createBook,
    searchBooks
  }
})
