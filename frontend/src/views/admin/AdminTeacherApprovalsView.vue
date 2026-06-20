<template>
  <div class="page">
    <div class="page-header">
      <h2>教师资料审核</h2>
      <el-button @click="loadProfiles">刷新</el-button>
    </div>

    <el-table :data="profiles" border empty-text="暂无教师资料">
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="displayName" label="展示名称" width="130" />
      <el-table-column prop="department" label="院系" width="150" />
      <el-table-column prop="title" label="职称" width="120" />
      <el-table-column prop="expertiseTags" label="擅长方向" />
      <el-table-column label="状态" width="120">
        <template #default="{ row }">
          <el-tag :type="getStatusType(row.approvalStatus)">
            {{ statusMap[row.approvalStatus] || row.approvalStatus }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="接收请求" width="100">
        <template #default="{ row }">
          {{ row.available ? "开启" : "关闭" }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="190">
        <template #default="{ row }">
          <el-button type="success" size="small" @click="approve(row)">通过</el-button>
          <el-button type="danger" size="small" @click="openReject(row)">拒绝</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="rejectDialogVisible" title="拒绝教师资料" width="520px">
      <el-input
        v-model="rejectComment"
        type="textarea"
        :rows="4"
        placeholder="请填写拒绝原因，教师将看到该意见"
      />
      <template #footer>
        <el-button @click="rejectDialogVisible = false">取消</el-button>
        <el-button type="danger" @click="reject">确认拒绝</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, ref } from "vue"
import { ElMessage } from "element-plus"
import {
  approveTeacherProfileApi,
  getPendingTeacherProfilesApi,
  rejectTeacherProfileApi
} from "../../api/teacher"

const profiles = ref([])
const rejectDialogVisible = ref(false)
const rejectingProfile = ref(null)
const rejectComment = ref("")

const statusMap = {
  PENDING: "待审核",
  APPROVED: "已通过",
  REJECTED: "已拒绝"
}

onMounted(loadProfiles)

async function loadProfiles() {
  profiles.value = await getPendingTeacherProfilesApi()
}

async function approve(row) {
  await approveTeacherProfileApi(row.id)
  ElMessage.success("已通过教师资料")
  await loadProfiles()
}

function openReject(row) {
  rejectingProfile.value = row
  rejectComment.value = ""
  rejectDialogVisible.value = true
}

async function reject() {
  if (!rejectComment.value) {
    ElMessage.warning("请填写拒绝原因")
    return
  }
  await rejectTeacherProfileApi(rejectingProfile.value.id, rejectComment.value)
  rejectDialogVisible.value = false
  ElMessage.success("已拒绝教师资料")
  await loadProfiles()
}

function getStatusType(status) {
  if (status === "APPROVED") return "success"
  if (status === "REJECTED") return "danger"
  return "warning"
}
</script>

<style scoped>
.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
}

h2 {
  margin: 0;
}
</style>
