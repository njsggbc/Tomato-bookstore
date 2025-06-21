// src/stores/Order.ts
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { api } from '@/Services/api'
import { useCartStore } from './Cart'
import { useUserStore } from './User'

// 订单信息接口
export interface OrderItem {
  id: string
  bookId: string
  title: string
  author: string
  price: number
  quantity: number
  imageUrl: string
  storeId: number
}

export interface Order {
  id: string
  userId: string
  storeId: number
  items: OrderItem[]
  totalAmount: number
  status: 'pending' | 'paid' | 'shipped' | 'completed' | 'cancelled'
  createdAt: string
  updatedAt: string
  address?: string
  contactPhone?: string
  contactName?: string
  paymentMethod?: string
}

export const useOrderStore = defineStore('order', () => {
  const orders = ref<Order[]>([])
  const currentOrder = ref<Order | null>(null)
  const loading = ref(false)
  const error = ref<string | null>(null)
  const cartStore = useCartStore()
  const userStore = useUserStore()

  // 从本地存储获取订单数据
  const getStoredOrders = (): Order[] => {
    const storedOrders = localStorage.getItem('orders')
    return storedOrders ? JSON.parse(storedOrders) : []
  }

  // 保存订单数据到本地存储
  const saveOrders = (orders: Order[]) => {
    localStorage.setItem('orders', JSON.stringify(orders))
  }

  // 获取订单列表
  const fetchOrders = async () => {
    loading.value = true
    error.value = null
    try {
      // 模拟API请求延迟
      await new Promise(resolve => setTimeout(resolve, 500))
      const storedOrders = getStoredOrders()
      if (userStore.user?.id) {
        orders.value = storedOrders.filter(order => order.userId === userStore.user.id.toString())
      } else {
        orders.value = []
      }
    } catch (err) {
      error.value = '获取订单列表失败'
      console.error('获取订单列表失败:', err)
    } finally {
      loading.value = false
    }
  }

  // 获取订单详情
  const fetchOrderDetail = async (orderId: string) => {
    loading.value = true
    error.value = null
    try {
      // 模拟API请求延迟
      await new Promise(resolve => setTimeout(resolve, 500))
      const storedOrders = getStoredOrders()
      const order = storedOrders.find(o => o.id === orderId)
      if (order) {
        currentOrder.value = order
        return order
      } else {
        error.value = '订单不存在'
        throw new Error('订单不存在')
      }
    } catch (err) {
      error.value = '获取订单详情失败'
      console.error('获取订单详情失败:', err)
      throw err
    } finally {
      loading.value = false
    }
  }

  // 创建订单
  const createOrder = async (orderData: {
    address: string,
    contactPhone: string,
    contactName: string,
    paymentMethod: string
  }) => {
    if (!userStore.isLoggedIn) {
      throw new Error('请先登录')
    }

    if (cartStore.cartItems.length === 0) {
      throw new Error('购物车为空')
    }

    loading.value = true
    error.value = ''

    try {
      // 创建新订单
      const newOrder: Order = {
        id: Date.now().toString(),
        userId: userStore.user!.id.toString(),
        storeId: cartStore.cartItems[0].storeId,
        items: cartStore.cartItems.map(item => ({
          id: item.id.toString(),
          bookId: item.bookId.toString(),
          title: item.title,
          author: item.author,
          price: item.price,
          quantity: item.quantity,
          imageUrl: item.imageUrl,
          storeId: item.storeId
        })),
        totalAmount: cartStore.totalAmount,
        status: 'pending',
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString(),
        ...orderData
      }

      // 保存订单
      const storedOrders = getStoredOrders()
      storedOrders.push(newOrder)
      saveOrders(storedOrders)

      // 清空购物车
      await cartStore.clearCart()

      // 更新当前订单
      currentOrder.value = newOrder
      orders.value = storedOrders.filter(order => order.userId === userStore.user!.id.toString())

      return newOrder
    } catch (err: any) {
      error.value = err.message || '创建订单失败'
      console.error(err)
      throw err
    } finally {
      loading.value = false
    }
  }

  // 支付订单
  const payOrder = async (orderId: string) => {
    loading.value = true
    error.value = ''

    try {
      // 模拟支付过程
      await new Promise(resolve => setTimeout(resolve, 1000))

      const storedOrders = getStoredOrders()
      const orderIndex = storedOrders.findIndex(o => o.id === orderId)
      
      if (orderIndex === -1) {
        throw new Error('订单不存在')
      }

      // 更新订单状态为已支付
      storedOrders[orderIndex].status = 'shipped'
      storedOrders[orderIndex].updatedAt = new Date().toISOString()
      saveOrders(storedOrders)

      // 更新当前订单
      if (currentOrder.value && currentOrder.value.id === orderId) {
        currentOrder.value = storedOrders[orderIndex]
      }

      // 更新订单列表
      if (userStore.user && userStore.user.id) {
        orders.value = storedOrders.filter(order => order.userId === userStore.user.id.toString())
      } else {
        orders.value = []
      }

      return true
    } catch (err: any) {
      error.value = err.message || '支付失败'
      console.error(err)
      throw err
    } finally {
      loading.value = false
    }
  }

  // 发货
  const shipOrder = async (orderId: string) => {
    loading.value = true
    error.value = ''

    try {
      // 模拟发货过程
      await new Promise(resolve => setTimeout(resolve, 1000))

      const storedOrders = getStoredOrders()
      const orderIndex = storedOrders.findIndex(o => o.id === orderId)
      
      if (orderIndex === -1) {
        throw new Error('订单不存在')
      }

      // 更新订单状态为已发货
      storedOrders[orderIndex].status = 'shipped'
      storedOrders[orderIndex].updatedAt = new Date().toISOString()
      saveOrders(storedOrders)

      // 更新当前订单
      if (currentOrder.value && currentOrder.value.id === orderId) {
        currentOrder.value = storedOrders[orderIndex]
      }

      // 更新订单列表
      if (userStore.user && userStore.user.id) {
        orders.value = storedOrders.filter(order => order.userId === userStore.user.id.toString())
      } else {
        orders.value = []
      }

      return true
    } catch (err: any) {
      error.value = err.message || '发货失败'
      console.error(err)
      throw err
    } finally {
      loading.value = false
    }
  }

  // 确认收货
  const confirmReceived = async (orderId: string) => {
    loading.value = true
    error.value = ''

    try {
      // 模拟确认收货过程
      await new Promise(resolve => setTimeout(resolve, 1000))

      const storedOrders = getStoredOrders()
      const orderIndex = storedOrders.findIndex(o => o.id === orderId)
      
      if (orderIndex === -1) {
        throw new Error('订单不存在')
      }

      // 更新订单状态为已完成
      storedOrders[orderIndex].status = 'completed'
      storedOrders[orderIndex].updatedAt = new Date().toISOString()
      saveOrders(storedOrders)

      // 更新当前订单
      if (currentOrder.value && currentOrder.value.id === orderId) {
        currentOrder.value = storedOrders[orderIndex]
      }

      // 更新订单列表
      if (userStore.user && userStore.user.id) {
        orders.value = storedOrders.filter(order => order.userId === userStore.user.id.toString())
      } else {
        orders.value = []
      }

      return true
    } catch (err: any) {
      error.value = err.message || '确认收货失败'
      console.error(err)
      throw err
    } finally {
      loading.value = false
    }
  }

  // 取消订单
  const cancelOrder = async (orderId: string) => {
    loading.value = true
    error.value = ''

    try {
      const storedOrders = getStoredOrders()
      const orderIndex = storedOrders.findIndex(o => o.id === orderId)
      
      if (orderIndex === -1) {
        throw new Error('订单不存在')
      }

      // 更新订单状态为已取消
      storedOrders[orderIndex].status = 'cancelled'
      storedOrders[orderIndex].updatedAt = new Date().toISOString()
      saveOrders(storedOrders)

      // 更新当前订单
      if (currentOrder.value && currentOrder.value.id === orderId) {
        currentOrder.value = storedOrders[orderIndex]
      }

      // 更新订单列表
      if (userStore.user && userStore.user.id) {
        orders.value = storedOrders.filter(order => order.userId === userStore.user.id.toString())
      } else {
        orders.value = []
      }

      return true
    } catch (err: any) {
      error.value = err.message || '取消订单失败'
      console.error(err)
      throw err
    } finally {
      loading.value = false
    }
  }

  // 删除订单
  const deleteOrder = async (orderId: string) => {
    loading.value = true
    error.value = ''
    try {
      const storedOrders = getStoredOrders()
      const newOrders = storedOrders.filter(o => o.id !== orderId)
      saveOrders(newOrders)
      // 更新当前订单
      if (currentOrder.value && currentOrder.value.id === orderId) {
        currentOrder.value = null
      }
      // 更新订单列表
      if (userStore.user && userStore.user.id) {
        orders.value = newOrders.filter(order => order.userId === userStore.user.id.toString())
      } else {
        orders.value = []
      }
      return true
    } catch (err: any) {
      error.value = err.message || '删除订单失败'
      console.error(err)
      throw err
    } finally {
      loading.value = false
    }
  }

  // 获取指定店铺的所有订单
  const fetchOrdersByStore = async (storeId: number) => {
    loading.value = true
    error.value = ''
    try {
      const storedOrders = getStoredOrders()
      orders.value = storedOrders.filter(order => order.storeId === storeId)
      return orders.value
    } catch (err: any) {
      error.value = err.message || '获取店铺订单失败'
      console.error(err)
      throw err
    } finally {
      loading.value = false
    }
  }

  return {
    orders,
    currentOrder,
    loading,
    error,
    fetchOrders,
    fetchOrderDetail,
    createOrder,
    payOrder,
    shipOrder,
    confirmReceived,
    cancelOrder,
    deleteOrder,
    fetchOrdersByStore
  }
})
