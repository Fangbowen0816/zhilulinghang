<template>
  <el-container class="layout-container">
    <el-aside width="220px" class="aside">
      <div class="logo">职路领航·教师端</div>

      <el-menu
        router
        :default-active="$route.path"
        background-color="#304156"
        text-color="#fff"
        active-text-color="#ffd04b"
      >
        <el-menu-item index="/teacher/home">教师首页</el-menu-item>
        <el-menu-item index="/teacher/profile">个人资料</el-menu-item>
        <el-menu-item index="/teacher/requests">审核请求</el-menu-item>
        <el-menu-item index="/teacher/history">审核历史</el-menu-item>
        <el-menu-item index="/teacher/review">待审核简历</el-menu-item>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="header">
        <span>教师中心</span>
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
