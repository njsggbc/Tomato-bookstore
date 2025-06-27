// src/stores/Book.ts
import { defineStore } from 'pinia'
import { ref } from 'vue'

// 书籍信息接口
interface Book {
  id: number
  title: string
  author: string
  description: string
  price: number
  stock: number
  storeId: number
  category: string
  cover: string
  rating: number
  sales: number
  createdAt: string
}

// 模拟书籍数据
const mockBooks: Book[] = [
  {
    id: 1,
    title: '三体',
    author: '刘慈欣',
    description: '科幻小说代表作',
    price: 59.8,
    stock: 100,
    storeId: 1,
    category: '科幻',
    cover: 'https://picsum.photos/200/300?random=1',
    rating: 4.8,
    sales: 1000,
    createdAt: '2024-01-03T00:00:00Z'
  },
  {
    id: 2,
    title: '活着',
    author: '余华',
    description: '经典文学作品',
    price: 39.8,
    stock: 50,
    storeId: 1,
    category: '文学',
    cover: 'https://picsum.photos/200/300?random=2',
    rating: 4.9,
    sales: 800,
    createdAt: '2024-01-04T00:00:00Z'
  },
  {
    id: 3,
    title: 'JavaScript高级程序设计',
    author: 'Nicholas C. Zakas',
    description: 'JavaScript经典教程',
    price: 99.0,
    stock: 30,
    storeId: 2,
    category: '计算机',
    cover: 'https://picsum.photos/200/300?random=3',
    rating: 4.7,
    sales: 500,
    createdAt: '2024-01-05T00:00:00Z'
  },
  {
    id: 4,
    title: '小王子',
    author: '圣埃克苏佩里',
    description: '经典儿童文学',
    price: 29.8,
    stock: 200,
    storeId: 3,
    category: '儿童文学',
    cover: 'https://picsum.photos/200/300?random=4',
    rating: 4.9,
    sales: 1200,
    createdAt: '2024-01-06T00:00:00Z'
  }
]

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
      // 先从模拟书籍中查找
      const mockBook = mockBooks.find(b => b.id === bookId)
      if (mockBook) {
        currentBook.value = mockBook
        return mockBook
      }

      // 如果模拟书籍中没有，则从localStorage中查找
      const storedBooks = JSON.parse(localStorage.getItem('books') || '[]')
      const storedBook = storedBooks.find((b: Book) => b.id === bookId)
      
      if (storedBook) {
        currentBook.value = storedBook
        return storedBook
      }

      // 如果都没有找到，返回null
      currentBook.value = null
      return null
    } catch (err: any) {
      error.value = '获取书籍详情失败'
      console.error(err)
      throw err
    } finally {
      loading.value = false
    }
  }

  // 更新书籍信息
  const updateBook = async (bookId: number, bookData: Partial<Book>) => {
    loading.value = true
    error.value = ''

    try {
      // 先从模拟书籍中查找
      const mockIndex = mockBooks.findIndex(b => b.id === bookId)
      if (mockIndex !== -1) {
        mockBooks[mockIndex] = { ...mockBooks[mockIndex], ...bookData }
        currentBook.value = mockBooks[mockIndex]
        return mockBooks[mockIndex]
      }

      // 如果模拟书籍中没有，则从localStorage中查找
      const storedBooks = JSON.parse(localStorage.getItem('books') || '[]')
      const storedIndex = storedBooks.findIndex((b: Book) => b.id === bookId)
      
      if (storedIndex !== -1) {
        storedBooks[storedIndex] = { ...storedBooks[storedIndex], ...bookData }
        localStorage.setItem('books', JSON.stringify(storedBooks))
        currentBook.value = storedBooks[storedIndex]
        return storedBooks[storedIndex]
      }

      throw new Error('书籍不存在')
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
      // 先从模拟书籍中查找
      const mockIndex = mockBooks.findIndex(b => b.id === bookId)
      if (mockIndex !== -1) {
        mockBooks.splice(mockIndex, 1)
        currentBook.value = null
        return true
      }

      // 如果模拟书籍中没有，则从localStorage中查找
      const storedBooks = JSON.parse(localStorage.getItem('books') || '[]')
      const storedIndex = storedBooks.findIndex((b: Book) => b.id === bookId)
      
      if (storedIndex !== -1) {
        storedBooks.splice(storedIndex, 1)
        localStorage.setItem('books', JSON.stringify(storedBooks))
        currentBook.value = null
        return true
      }

      throw new Error('书籍不存在')
    } catch (err: any) {
      error.value = '删除书籍失败'
      console.error(err)
      throw err
    } finally {
      loading.value = false
    }
  }

  // 创建新书籍
  const createBook = async (storeId: number, bookData: Omit<Book, 'id'>) => {
    loading.value = true
    error.value = ''

    try {
      // 获取所有书籍以生成新的ID
      const storedBooks = JSON.parse(localStorage.getItem('books') || '[]')
      const allBooks = [...mockBooks, ...storedBooks]
      const maxId = Math.max(...allBooks.map(book => book.id), 0)
      
      const newBook = {
        ...bookData,
        id: maxId + 1,
        createdAt: new Date().toISOString()
      }
      
      // 将新书籍保存到localStorage
      storedBooks.push(newBook)
      localStorage.setItem('books', JSON.stringify(storedBooks))
      
      return newBook
    } catch (err: any) {
      error.value = '创建书籍失败'
      console.error(err)
      throw err
    } finally {
      loading.value = false
    }
  }

  // 获取所有书籍
  const fetchAllBooks = async () => {
    loading.value = true
    error.value = ''

    try {
      books.value = mockBooks
      return mockBooks
    } catch (err: any) {
      error.value = '获取书籍列表失败'
      console.error(err)
      return []
    } finally {
      loading.value = false
    }
  }

  // 获取推荐书籍
  const fetchRecommendedBooks = async () => {
    loading.value = true
    error.value = ''

    try {
      books.value = mockBooks.sort((a, b) => b.rating - a.rating).slice(0, 4)
      return books.value
    } catch (err: any) {
      error.value = '获取推荐书籍失败'
      console.error(err)
      return []
    } finally {
      loading.value = false
    }
  }

  // 搜索图书
  const searchBooks = async (query: string) => {
    loading.value = true
    error.value = ''

    try {
      // 搜索模拟书籍
      const mockResults = mockBooks.filter(book => 
        book.title.toLowerCase().includes(query.toLowerCase()) ||
        book.author.toLowerCase().includes(query.toLowerCase()) ||
        book.description.toLowerCase().includes(query.toLowerCase()) ||
        book.category.toLowerCase().includes(query.toLowerCase())
      )

      // 搜索用户创建的书籍
      const storedBooks = JSON.parse(localStorage.getItem('books') || '[]')
      const storedResults = storedBooks.filter((book: Book) => 
        book.title.toLowerCase().includes(query.toLowerCase()) ||
        book.author.toLowerCase().includes(query.toLowerCase()) ||
        book.description.toLowerCase().includes(query.toLowerCase()) ||
        book.category.toLowerCase().includes(query.toLowerCase())
      )

      // 合并搜索结果
      const results = [...mockResults, ...storedResults]
      return results
    } catch (err: any) {
      error.value = '搜索图书失败'
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
    fetchAllBooks,
    fetchRecommendedBooks,
    searchBooks
  }
})
