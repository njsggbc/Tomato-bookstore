// src/stores/Cart.ts
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { api } from '@/Services/api'
import { useUserStore } from './User'

interface CartItem {
  id: number
  bookId: number
  title: string
  author: string
  price: number
  imageUrl: string
  quantity: number
  storeId: number
}

export const useCartStore = defineStore('cart', () => {
  const cartItems = ref<CartItem[]>([])
  const loading = ref(false)
  const error = ref('')
  const userStore = useUserStore()

  // 计算购物车总金额
  const totalAmount = computed(() => {
    return cartItems.value.reduce((sum, item) => sum + (item.price * item.quantity), 0)
  })

  // 计算购物车商品总数
  const totalItems = computed(() => {
    return cartItems.value.reduce((sum, item) => sum + item.quantity, 0)
  })

  // 获取购物车商品
  const fetchCartItems = async () => {
    loading.value = true
    error.value = ''

    try {
      const response = await api.get('/cart')
      cartItems.value = response.data
    } catch (err) {
      error.value = '获取购物车失败'
      console.error(err)
    } finally {
      loading.value = false
    }
  }

  // 添加商品到购物车
  const addToCart = async (book, quantity = 1) => {
    if (!userStore.isLoggedIn) {
      error.value = '请先登录'
      return false
    }

    loading.value = true
    error.value = ''

    try {
      const response = await api.post('/cart', {
        bookId: book.id,
        quantity: quantity
      })
      await fetchCartItems()
      return true
    } catch (err) {
      error.value = '添加到购物车失败'
      console.error(err)
      return false
    } finally {
      loading.value = false
    }
  }

  // 更新购物车商品数量
  const updateItemQuantity = async (cartItemId, quantity) => {
    loading.value = true
    error.value = ''

    try {
      await api.put(`/cart/${cartItemId}`, { quantity })
      await fetchCartItems()
    } catch (err) {
      error.value = '更新商品数量失败'
      console.error(err)
    } finally {
      loading.value = false
    }
  }

  // 从购物车中删除商品
  const removeFromCart = async (cartItemId) => {
    loading.value = true
    error.value = ''

    try {
      await api.delete(`/cart/${cartItemId}`)
      await fetchCartItems()
    } catch (err) {
      error.value = '删除商品失败'
      console.error(err)
    } finally {
      loading.value = false
    }
  }

  // 清空购物车
  const clearCart = async () => {
    loading.value = true
    error.value = ''

    try {
      await api.delete('/cart/clear')
      cartItems.value = []
    } catch (err) {
      error.value = '清空购物车失败'
      console.error(err)
    } finally {
      loading.value = false
    }
  }

  return {
    cartItems,
    loading,
    error,
    totalAmount,
    totalItems,
    fetchCartItems,
    addToCart,
    updateItemQuantity,
    removeFromCart,
    clearCart
  }
})
