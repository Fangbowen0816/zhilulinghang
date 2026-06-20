<template>
  <div class="page">
    <div class="page-header">
      <h2>用户管理</h2>
      <el-button @click="loadUsers">刷新</el-button>
    </div>

    <div class="filters">
      <el-input v-model="keyword" placeholder="搜索用户名" clearable />
      <el-select v-model="roleFilter" placeholder="角色" clearable>
        <el-option label="管理员" value="ADMIN" />
        <el-option label="学生" value="STUDENT" />
        <el-option label="教师" value="TEACHER" />
      </el-select>
    </div>

    <el-table :data="filteredUsers" border empty-text="暂无用户">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="username" label="用户名" />
      <el-table-column prop="role" label="角色" width="120" />
      <el-table-column label="账号状态" width="140">
        <template #default="{ row }">
          <el-switch
            :model-value="row.enabled"
            :disabled="row.username === currentUsername"
            active-text="启用"
            inactive-text="禁用"
            @change="value => changeEnabled(row, value)"
          />
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="注册时间" width="190" />
      <el-table-column label="操作" width="130">
        <template #default="{ row }">
          <el-button type="primary" size="small" @click="openPasswordDialog(row)">重置密码</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="passwordDialogVisible" title="重置密码" width="420px">
      <el-form label-width="84px">
        <el-form-item label="用户">
          <span>{{ passwordUser?.username }}</span>
        </el-form-item>
        <el-form-item label="新密码">
          <el-input v-model="newPassword" type="password" show-password placeholder="请输入至少 6 位新密码" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="passwordDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="resetPassword">确认重置</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from "vue"
import { ElMessage, ElMessageBox } from "element-plus"
import { getAdminUsersApi, resetAdminUserPasswordApi, setAdminUserEnabledApi } from "../../api/admin"

const users = ref([])
const keyword = ref("")
const roleFilter = ref("")
const currentUsername = localStorage.getItem("username") || ""
const passwordDialogVisible = ref(false)
const passwordUser = ref(null)
const newPassword = ref("")

const filteredUsers = computed(() => {
  return users.value.filter(user => {
    const matchesKeyword = !keyword.value || user.username?.includes(keyword.value)
    const matchesRole = !roleFilter.value || user.role === roleFilter.value
    return matchesKeyword && matchesRole
  })
})

onMounted(loadUsers)

async function loadUsers() {
  users.value = await getAdminUsersApi()
}

async function changeEnabled(row, enabled) {
  const actionText = enabled ? "启用" : "禁用"
  try {
    await ElMessageBox.confirm(`确认${actionText}用户「${row.username}」吗？`, `${actionText}用户`, {
      type: enabled ? "info" : "warning",
      confirmButtonText: `确认${actionText}`,
      cancelButtonText: "取消"
    })
    await setAdminUserEnabledApi(row.id, enabled)
    ElMessage.success(`已${actionText}用户`)
  } catch (error) {
    if (error !== "cancel" && error !== "close") {
      throw error
    }
  } finally {
    await loadUsers()
  }
}

function openPasswordDialog(row) {
  passwordUser.value = row
  newPassword.value = ""
  passwordDialogVisible.value = true
}

async function resetPassword() {
  if (!newPassword.value || newPassword.value.length < 6) {
    ElMessage.warning("新密码至少需要 6 位")
    return
  }
  await resetAdminUserPasswordApi(passwordUser.value.id, newPassword.value)
  ElMessage.success("密码已重置")
  passwordDialogVisible.value = false
  await loadUsers()
}
</script>

<style scoped>
.page-header,
.filters {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 20px;
}

.page-header {
  justify-content: space-between;
}

.filters .el-input {
  width: 240px;
}

.filters .el-select {
  width: 160px;
}

h2 {
  margin: 0;
}
</style>
