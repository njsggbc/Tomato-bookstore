// src/stores/Cart.ts
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
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
      const storedCart = localStorage.getItem('cart')
      if (storedCart) {
        cartItems.value = JSON.parse(storedCart)
      }
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
      const existingItem = cartItems.value.find(item => item.bookId === book.id)
      if (existingItem) {
        existingItem.quantity += quantity
      } else {
        cartItems.value.push({
          id: Date.now(),
          bookId: book.id,
          title: book.title,
          author: book.author,
          price: book.price,
          imageUrl: book.imageUrl,
          quantity: quantity,
          storeId: book.storeId
        })
      }
      saveCart()
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
      const item = cartItems.value.find(item => item.id === cartItemId)
      if (item) {
        item.quantity = quantity
        saveCart()
      }
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
      cartItems.value = cartItems.value.filter(item => item.id !== cartItemId)
      saveCart()
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
      cartItems.value = []
      saveCart()
    } catch (err) {
      error.value = '清空购物车失败'
      console.error(err)
    } finally {
      loading.value = false
    }
  }

  // 保存购物车到本地存储
  const saveCart = () => {
    localStorage.setItem('cart', JSON.stringify(cartItems.value))
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
