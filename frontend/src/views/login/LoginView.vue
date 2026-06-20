<template>
  <div class="login-container">
    <el-card class="login-card">
      <h2>职路领航登录</h2>

      <el-form :model="loginForm">
        <el-form-item label="用户名">
          <el-input v-model="loginForm.username" placeholder="请输入用户名" />
        </el-form-item>

        <el-form-item label="密码">
          <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="请输入密码"
          />
        </el-form-item>

        <el-form-item label="角色">
          <el-select v-model="loginForm.role" placeholder="请选择角色" style="width: 100%">
            <el-option label="管理员" value="admin" />
            <el-option label="学生" value="student" />
            <el-option label="教师" value="teacher" />
          </el-select>
        </el-form-item>

        <el-button type="primary" class="login-btn" @click="handleLogin">
          登录
        </el-button>
        <el-button class="login-btn secondary-btn" @click="router.push('/register')">
          注册账号
        </el-button>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { reactive } from "vue"
import { useRouter } from "vue-router"
import { useUserStore } from "../../stores/user"
import { loginApi } from "../../api/auth"
import { ElMessage } from "element-plus"

const router = useRouter()
const userStore = useUserStore()

const loginForm = reactive({
  username: "",
  password: "",
  role: ""
})

const handleLogin = async () => {
  if (!loginForm.username || !loginForm.password || !loginForm.role) {
    ElMessage.warning("请输入用户名、密码并选择角色")
    return
  }

  try {
    const result = await loginApi(loginForm)
    userStore.login(result.username, result.token, result.role)
    ElMessage.success("登录成功")

    if (result.role === "admin") {
      router.push("/admin/home")
    } else if (result.role === "student") {
      router.push("/student/home")
    } else if (result.role === "teacher") {
      router.push("/teacher/home")
    }
  } catch (error) {
    console.error(error)
  }
}
</script>

<style scoped>
.login-container {
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background: #f5f7fa;
}

.login-card {
  width: 400px;
}

.login-card h2 {
  text-align: center;
  margin-bottom: 24px;
}

.login-btn {
  width: 100%;
}

.secondary-btn {
  margin: 12px 0 0;
}
</style>

