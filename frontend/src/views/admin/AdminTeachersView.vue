<template>
  <div class="page">
    <div class="page-header">
      <h2>教师管理</h2>
      <el-button @click="loadTeachers">刷新</el-button>
    </div>

    <div class="filters">
      <el-input v-model="keyword" placeholder="搜索名称/院系/方向" clearable />
      <el-select v-model="approvalFilter" placeholder="审核状态" clearable>
        <el-option label="待审核" value="PENDING" />
        <el-option label="已通过" value="APPROVED" />
        <el-option label="已拒绝" value="REJECTED" />
      </el-select>
      <el-select v-model="availableFilter" placeholder="接收请求" clearable>
        <el-option label="开启" :value="true" />
        <el-option label="关闭" :value="false" />
      </el-select>
    </div>

    <el-table :data="filteredTeachers" border empty-text="暂无教师资料">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="teacherId" label="教师ID" width="90" />
      <el-table-column prop="displayName" label="展示名称" width="140" />
      <el-table-column prop="department" label="院系" width="150" />
      <el-table-column prop="title" label="职称" width="120" />
      <el-table-column prop="expertiseTags" label="擅长方向" />
      <el-table-column label="审核状态" width="120">
        <template #default="{ row }">
          <el-tag :type="getApprovalType(row.approvalStatus)">
            {{ approvalMap[row.approvalStatus] || row.approvalStatus }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="接收请求" width="140">
        <template #default="{ row }">
          <el-switch
            :model-value="row.available"
            :disabled="row.approvalStatus !== 'APPROVED'"
            active-text="开"
            inactive-text="关"
            @change="value => changeAvailable(row, value)"
          />
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from "vue"
import { ElMessage, ElMessageBox } from "element-plus"
import { getAdminTeachersApi, setTeacherAvailableApi } from "../../api/admin"

const teachers = ref([])
const keyword = ref("")
const approvalFilter = ref("")
const availableFilter = ref("")

const approvalMap = {
  PENDING: "待审核",
  APPROVED: "已通过",
  REJECTED: "已拒绝"
}

const filteredTeachers = computed(() => {
  return teachers.value.filter(teacher => {
    const text = `${teacher.displayName || ""}${teacher.department || ""}${teacher.expertiseTags || ""}`
    const matchesKeyword = !keyword.value || text.includes(keyword.value)
    const matchesApproval = !approvalFilter.value || teacher.approvalStatus === approvalFilter.value
    const matchesAvailable = availableFilter.value === "" || teacher.available === availableFilter.value
    return matchesKeyword && matchesApproval && matchesAvailable
  })
})

onMounted(loadTeachers)

async function loadTeachers() {
  teachers.value = await getAdminTeachersApi()
}

async function changeAvailable(row, available) {
  if (available && row.approvalStatus !== "APPROVED") {
    ElMessage.warning("只有审核通过的教师才能开启接收请求")
    await loadTeachers()
    return
  }

  const actionText = available ? "开启" : "关闭"
  try {
    await ElMessageBox.confirm(`确认${actionText}该教师的接收请求权限吗？`, `${actionText}接收请求`, {
      type: available ? "info" : "warning",
      confirmButtonText: `确认${actionText}`,
      cancelButtonText: "取消"
    })
    await setTeacherAvailableApi(row.id, available)
    ElMessage.success(`已${actionText}教师接收请求权限`)
  } catch (error) {
    if (error !== "cancel" && error !== "close") {
      throw error
    }
  } finally {
    await loadTeachers()
  }
}

function getApprovalType(status) {
  if (status === "APPROVED") return "success"
  if (status === "REJECTED") return "danger"
  return "warning"
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
  width: 260px;
}

.filters .el-select {
  width: 160px;
}

h2 {
  margin: 0;
}
</style>
