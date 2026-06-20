<template>
  <el-container class="layout-container">
    <el-aside width="220px" class="aside">
      <div class="logo">职路领航·学生端</div>

      <el-menu
        router
        :default-active="$route.path"
        background-color="#304156"
        text-color="#fff"
        active-text-color="#ffd04b"
      >
        <el-menu-item index="/student/home">学生首页</el-menu-item>
        <el-menu-item index="/student/resume/edit">制作简历</el-menu-item>
        <el-menu-item index="/student/review-requests">审核请求</el-menu-item>
        <el-menu-item index="/student/resume/feedback">查看反馈</el-menu-item>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="header">
        <span>学生中心</span>
        <el-button type="danger" size="small" @click="handleLogout">
          退出登录
        </el-button>
      </el-header>

      <el-main class="main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { useRouter } from "vue-router"
import { useUserStore } from "../stores/user"

const router = useRouter()
const userStore = useUserStore()

const handleLogout = () => {
  userStore.logout()
  router.push("/login")
}
</script>

<style scoped>
.layout-container {
  height: 100vh;
}

.aside {
  background-color: #304156;
}

.logo {
  height: 60px;
  line-height: 60px;
  color: white;
  text-align: center;
  font-size: 18px;
  font-weight: bold;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: white;
  border-bottom: 1px solid #ddd;
}

.main {
  background: #f5f7fa;
}
</style>
