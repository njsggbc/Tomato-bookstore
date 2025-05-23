<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useOrderStore } from '../stores/Order'
import { useUserStore } from '../stores/User'

const router = useRouter()
const orderStore = useOrderStore()
const userStore = useUserStore()
const loading = ref(false)
const error = ref('')
const activeTab = ref('all') // all, pending, paid, shipped, completed, canceled

// 过滤订单
const filteredOrders = computed(() => {
  if (activeTab.value === 'all') {
    return orderStore.orders
  } else {
    return orderStore.orders.filter(order => order.status === activeTab.value)
  }
})

// 加载所有订单
const loadOrders = async () => {
  loading.value = true
  error.value = ''

  try {
    await orderStore.fetchUserOrders()
  } catch (err) {
    error.value = '加载订单失败，请稍后再试'
  } finally {
    loading.value = false
  }
}

// 查看订单详情
const viewOrderDetail = (orderId) => {
  router.push(`/order/${orderId}`)
}

// 格式化日期
const formatDate = (dateString) => {
  const date = new Date(dateString)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: 'numeric',
    day: 'numeric',
    hour: 'numeric',
    minute: 'numeric'
  })
}

// 获取订单状态文本
const getStatusText = (status) => {
  switch (status) {
    case 'pending': return '待付款'
    case 'paid': return '已付款'
    case 'shipped': return '已发货'
    case 'completed': return '已完成'
    case 'canceled': return '已取消'
    default: return '未知状态'
  }
}

onMounted(() => {
  if (userStore.isLoggedIn) {
    loadOrders()
  } else {
    router.push('/login?redirect=/orders')
  }
})
</script>

<template>
  <div class="orders-container">
    <h1>我的订单</h1>

    <div v-if="!userStore.isLoggedIn" class="not-logged-in">
      <p>请先登录查看订单</p>
      <RouterLink to="/login" class="btn-primary">去登录</RouterLink>
    </div>

    <template v-else>
      <div v-if="loading" class="loading">
        <p>加载中...</p>
      </div>

      <div v-else-if="error" class="error-message">
        {{ error }}
      </div>

      <div v-else>
        <!-- 订单筛选标签 -->
        <div class="order-tabs">
          <button
            class="tab-button"
            :class="{ active: activeTab === 'all' }"
            @click="activeTab = 'all'"
          >
            全部订单
          </button>
          <button
            class="tab-button"
            :class="{ active: activeTab === 'pending' }"
            @click="activeTab = 'pending'"
          >
            待付款
          </button>
          <button
            class="tab-button"
            :class="{ active: activeTab === 'paid' }"
            @click="activeTab = 'paid'"
          >
            已付款
          </button>
          <button
            class="tab-button"
            :class="{ active: activeTab === 'shipped' }"
            @click="activeTab = 'shipped'"
          >
            已发货
          </button>
          <button
            class="tab-button"
            :class="{ active: activeTab === 'completed' }"
            @click="activeTab = 'completed'"
          >
            已完成
          </button>
          <button
            class="tab-button"
            :class="{ active: activeTab === 'canceled' }"
            @click="activeTab = 'canceled'"
          >
            已取消
          </button>
        </div>

        <div v-if="filteredOrders.length === 0" class="no-orders">
          <p>暂无订单记录</p>
          <RouterLink to="/home" class="btn-primary">去购物</RouterLink>
        </div>

        <div v-else class="order-list">
          <div
            v-for="order in filteredOrders"
            :key="order.id"
            class="order-card"
            @click="viewOrderDetail(order.id)"
          >
            <div class="order-header">
              <span class="order-number">订单号：{{ order.orderNumber }}</span>
              <span class="order-status" :class="order.status">
                {{ getStatusText(order.status) }}
              </span>
            </div>

            <div class="order-items">
              <div v-for="item in order.items.slice(0, 2)" :key="item.id" class="order-item">
                <div class="item-image">
                  <img :src="item.imageUrl || 'https://via.placeholder.com/60x80'" :alt="item.title" />
                </div>
                <div class="item-details">
                  <h3>{{ item.title }}</h3>
                  <p>{{ item.author }}</p>
                  <span class="item-price">¥{{ item.price.toFixed(2) }} × {{ item.quantity }}</span>
                </div>
              </div>
              <div v-if="order.items.length > 2" class="more-items">
                还有 {{ order.items.length - 2 }} 件商品
              </div>
            </div>

            <div class="order-footer">
              <div class="order-time">下单时间：{{ formatDate(order.createdAt) }}</div>
              <div class="order-total">总计：¥{{ order.totalAmount.toFixed(2) }}</div>
            </div>
          </div>
        </div>
      </div>
    </template>
  </div>
</template>

<style scoped>
.orders-container {
  max-width: 900px;
  margin: 0 auto;
  padding: 20px;
}

h1 {
  text-align: center;
  color: #ff6347;
  margin-bottom: 30px;
}

.loading, .error-message, .no-orders, .not-logged-in {
  text-align: center;
  padding: 40px;
  font-size: 16px;
}

.error-message {
  color: #ff6347;
  background-color: rgba(255, 99, 71, 0.1);
  border-radius: 8px;
}

.btn-primary {
  display: inline-block;
  margin-top: 15px;
  padding: 10px 20px;
  background-color: #ff6347;
  color: white;
  border-radius: 8px;
  text-decoration: none;
}

/* 订单筛选标签 */
.order-tabs {
  display: flex;
  justify-content: space-between;
  margin-bottom: 20px;
  overflow-x: auto;
  padding-bottom: 10px;
}

.tab-button {
  padding: 8px 15px;
  border: 1px solid #ddd;
  background: #fff;
  border-radius: 20px;
  cursor: pointer;
  white-space: nowrap;
  transition: all 0.3s;
}

.tab-button.active {
  background-color: #ff6347;
  color: white;
  border-color: #ff6347;
}

/* 订单列表 */
.order-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.order-card {
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  overflow: hidden;
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;
}

.order-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.order-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px;
  background-color: #f9f9f9;
  border-bottom: 1px solid #eee;
}

.order-number {
  font-size: 14px;
  color: #666;
}

.order-status {
  font-weight: bold;
}

.order-status.pending {
  color: #ff6347;
}

.order-status.paid {
  color: #2196f3;
}

.order-status.shipped {
  color: #ff9800;
}

.order-status.completed {
  color: #4caf50;
}

.order-status.canceled {
  color: #9e9e9e;
}

.order-items {
  padding: 15px;
  border-bottom: 1px solid #eee;
}

.order-item {
  display: flex;
  margin-bottom: 10px;
}

.item-image img {
  width: 60px;
  height: 80px;
  object-fit: cover;
}

.item-details {
  margin-left: 10px;
  flex-grow: 1;
}

.item-details h3 {
  font-size: 14px;
  margin-bottom: 5px;
}

.item-details p {
  font-size: 12px;
  color: #666;
  margin-bottom: 5px;
}

.item-price {
  font-size: 12px;
  color: #ff6347;
}

.more-items {
  font-size: 12px;
  color: #999;
  text-align: center;
  padding: 5px;
}

.order-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px;
}

.order-time {
  font-size: 12px;
  color: #666;
}

.order-total {
  font-weight: bold;
  color: #ff6347;
}

@media (max-width: 768px) {
  .order-tabs {
    padding-bottom: 10px;
  }

  .tab-button {
    font-size: 12px;
    padding: 6px 10px;
  }
}
</style>
