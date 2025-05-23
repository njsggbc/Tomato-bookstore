<!-- src/views/admin/AdminDashboardView.vue -->
<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../../stores/User'
import { ROLES } from '../../constants/roles'

const router = useRouter()
const userStore = useUserStore()

// 检查用户是否为管理员
onMounted(() => {
  if (!userStore.user || userStore.user.role !== ROLES.ADMIN) {
    router.push('/home')
  }
})
</script>

<template>
  <div class="admin-dashboard">
    <h1>管理员控制面板</h1>

    <div class="admin-menu">
      <div class="menu-card" @click="router.push('/admin/stores/approval')">
        <h3>店铺审批</h3>
        <p>审核待创建的店铺申请</p>
      </div>

      <div class="menu-card" @click="router.push('/admin/ads/approval')">
        <h3>广告审批</h3>
        <p>审核商家提交的广告申请</p>
      </div>

      <div class="menu-card" @click="router.push('/admin/users')">
        <h3>用户管理</h3>
        <p>管理系统用户</p>
      </div>
    </div>
  </div>
</template>

<style scoped>
.admin-dashboard {
  max-width: 1000px;
  margin: 0 auto;
  padding: 20px;
}

h1 {
  text-align: center;
  color: #ff6347;
  margin-bottom: 30px;
}

.admin-menu {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
}

.menu-card {
  background-color: #fff;
  border-radius: 10px;
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
  padding: 20px;
  cursor: pointer;
  transition: transform 0.3s, box-shadow 0.3s;
}

.menu-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 6px 12px rgba(0, 0, 0, 0.15);
}

.menu-card h3 {
  color: #ff6347;
  margin-bottom: 10px;
}

.menu-card p {
  color: #666;
}
</style>
