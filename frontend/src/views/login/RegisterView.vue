<template>
  <div class="register-container">
    <el-card class="register-card">
      <h2>注册职路领航账号</h2>

      <el-form :model="form" label-width="90px">
        <el-form-item label="身份">
          <el-radio-group v-model="form.role">
            <el-radio-button label="student">学生</el-radio-button>
            <el-radio-button label="teacher">教师</el-radio-button>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="用户名">
          <el-input v-model="form.username" placeholder="请输入用户名" />
        </el-form-item>

        <el-form-item label="密码">
          <el-input v-model="form.password" type="password" placeholder="请输入密码" show-password />
        </el-form-item>

        <el-form-item label="确认密码">
          <el-input v-model="confirmPassword" type="password" placeholder="请再次输入密码" show-password />
        </el-form-item>

        <template v-if="form.role === 'teacher'">
          <el-form-item label="展示名称">
            <el-input v-model="form.displayName" placeholder="例如：王老师" />
          </el-form-item>
          <el-form-item label="院系">
            <el-input v-model="form.department" placeholder="例如：软件工程系" />
          </el-form-item>
          <el-form-item label="职称">
            <el-input v-model="form.title" placeholder="例如：讲师、副教授" />
          </el-form-item>
          <el-form-item label="擅长方向">
            <el-input v-model="form.expertiseTags" placeholder="例如：Java 后端, 前端, 校招简历" />
          </el-form-item>
          <el-form-item label="个人简介">
            <el-input v-model="form.bio" type="textarea" :rows="3" placeholder="介绍你的指导方向和经验" />
          </el-form-item>
        </template>

        <el-button type="primary" class="register-btn" @click="handleRegister">注册</el-button>
        <el-button class="register-btn" @click="router.push('/login')">返回登录</el-button>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { reactive, ref } from "vue"
import { useRouter } from "vue-router"
import { ElMessage } from "element-plus"
import { registerApi } from "../../api/auth"
import { useUserStore } from "../../stores/user"

const router = useRouter()
const userStore = useUserStore()
const confirmPassword = ref("")

const form = reactive({
  username: "",
  password: "",
  role: "student",
  displayName: "",
  department: "",
  title: "",
  bio: "",
  expertiseTags: ""
})

async function handleRegister() {
  if (!form.username || !form.password || !form.role) {
    ElMessage.warning("请填写用户名、密码和身份")
    return
  }
  if (form.password !== confirmPassword.value) {
    ElMessage.warning("两次输入的密码不一致")
    return
  }
  if (form.role === "teacher" && !form.displayName) {
    ElMessage.warning("教师注册请填写展示名称")
    return
  }

  const result = await registerApi(form)
  userStore.login(result.username, result.token, result.role)
  ElMessage.success(form.role === "teacher" ? "注册成功，请完善资料并等待管理员审核" : "注册成功")
  router.push(result.role === "teacher" ? "/teacher/profile" : "/student/home")
}
</script>

<style scoped>
.register-container {
  min-height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 32px 0;
  background: #f5f7fa;
}

.register-card {
  width: 520px;
}

.register-card h2 {
  text-align: center;
  margin-bottom: 24px;
}

.register-btn {
  width: 100%;
  margin: 0 0 12px;
}
</style>
