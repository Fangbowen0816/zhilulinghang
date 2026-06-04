<template>
  <el-container class="layout-container">
    <el-aside width="220px" class="aside">
      <div class="logo">智路领航</div>

      <el-menu
        router
        default-active="/home"
        background-color="#304156"
        text-color="#fff"
        active-text-color="#ffd04b"
      >
        <el-menu-item index="/home">首页</el-menu-item>
        <el-menu-item index="/user">用户管理</el-menu-item>
        <el-menu-item index="/project">项目管理</el-menu-item>
        <el-menu-item index="/ai">AI 助手</el-menu-item>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="header">
        <span>后台管理系统</span>
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
  text-align: center;
  color: white;
  font-size: 20px;
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