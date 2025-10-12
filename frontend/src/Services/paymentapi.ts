import { api } from './api'

export const paymentapi = {
  /**
   * 根据订单ID查询支付结果/交易状态
   * 对应后端的 "GET 查询交易状态"
   * @param orderId 订单ID
   * @returns {Promise<object>} 返回一个包含支付结果信息的对象。
   * @example { success: true, orderId: "123", amount: 99.9, paymentTime: "2023-01-01T12:00:00Z", errorMessage: "" }
   */
  getPaymentResult: async (orderId: string) => {
    const response = await api.get(`/payments/${orderId}/result`)
    return response.data
  },

  /**
   * 发起支付
   * 对应后端的 "POST 发起支付"
   * @param paymentId 支付单ID
   * @param paymentMethod 支付方式 (例如 "ALIPAY")
   * @returns {Promise<string>} 返回一个可供跳转的支付URL。
   * @example "https://sandbox.alipay.com/..."
   */
  initiatePayment: async (paymentId: number, paymentMethod: string) => {
    const response = await api.post(`/api/payments/pay`, {
        totalAmount: 100
      })
    return response.data.data
  },

  /**
   * 取消支付
   * 对应后端的 "POST 取消支付"
   * @param paymentId 支付单ID
   * @returns {Promise<void>} 操作成功时无特定返回内容。
   */
  cancelPayment: async (paymentId: number) => {
    const response = await api.post(`/payments/${paymentId}/cancel`)
    return response.data
  },

  /**
   * 查询退款状态
   * 对应后端的 "GET 查询退款状态"
   * @param orderId 订单ID
   * @returns {Promise<object>} 返回一个包含退款状态信息的对象。
   */
  getRefundStatus: async (orderId: string) => {
    const response = await api.get(`/orders/${orderId}/refund-status`)
    return response.data
  }
} 