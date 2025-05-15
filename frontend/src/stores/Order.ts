// src/stores/Order.ts
import { defineStore } from 'pinia'
import { ref } from 'vue'
import { api } from '@/Services/api'
import { useCartStore } from './Cart'

// 订单信息接口
interface Order {
  id: number
  orderNumber: string
  totalAmount: number
  status: string // pending, paid, shipped, completed, canceled
  items: OrderItem[]
  address: string
  contactPhone: string
  contactName: string
  createdAt: string
  paymentMethod: string
}

interface OrderItem {
  id: number
  bookId: number
  title: string
  author: string
  price: number
  quantity: number
  subtotal: number
}

export const useOrderStore = defineStore('order', () => {
  const orders = ref<Order[]>([])
  const currentOrder = ref<Order | null>(null)
  const loading = ref(false)
  const error = ref('')
  const cartStore = useCartStore()

  // 创建订单
  const createOrder = async (orderData: {
    address: string,
    contactPhone: string,
    contactName: string,
    paymentMethod: string
  }) => {
    loading.value = true
    error.value = ''

    try {
      const response = await api.post('/orders', orderData)
      currentOrder.value = response.data
      // 创建订单后清空购物车
      await cartStore.clearCart()
      return response.data
    } catch (err: any) {
      error.value = '创建订单失败'
      console.error(err)
      throw err
    } finally {
      loading.value = false
    }
  }

  // 获取用户所有订单
  const fetchUserOrders = async () => {
    loading.value = true
    error.value = ''

    try {
      const response = await api.get('/orders')
      orders.value = response.data
      return response.data
    } catch (err: any) {
      error.value = '获取订单列表失败'
      console.error(err)
      return []
    } finally {
      loading.value = false
    }
  }

  // 获取订单详情
  const fetchOrderById = async (orderId: number) => {
    loading.value = true
    error.value = ''

    try {
      const response = await api.get(`/orders/${orderId}`)
      currentOrder.value = response.data
      return response.data
    } catch (err: any) {
      error.value = '获取订单详情失败'
      console.error(err)
      throw err
    } finally {
      loading.value = false
    }
  }

  // 发起支付
  const payOrder = async (orderId: number) => {
    loading.value = true
    error.value = ''

    try {
      // 获取支付链接
      const response = await api.post(`/payments/alipay/create`, { orderId })
      return response.data.paymentUrl
    } catch (err: any) {
      error.value = '创建支付失败'
      console.error(err)
      throw err
    } finally {
      loading.value = false
    }
  }

  // 检查支付状态
  const checkPaymentStatus = async (orderId: number) => {
    try {
      const response = await api.get(`/payments/status/${orderId}`)
      if (response.data.paid) {
        // 如果已支付，更新本地订单状态
        currentOrder.value = response.data.order
      }
      return response.data
    } catch (err: any) {
      console.error('检查支付状态失败', err)
      throw err
    }
  }

  // 取消订单
  const cancelOrder = async (orderId: number) => {
    loading.value = true
    error.value = ''

    try {
      const response = await api.post(`/orders/${orderId}/cancel`)
      // 更新订单状态
      if (currentOrder.value && currentOrder.value.id === orderId) {
        currentOrder.value = response.data
      }
      return response.data
    } catch (err: any) {
      error.value = '取消订单失败'
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
    createOrder,
    fetchUserOrders,
    fetchOrderById,
    payOrder,
    checkPaymentStatus,
    cancelOrder
  }
})
