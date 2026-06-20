<template>
  <div class="page">
    <div class="page-header">
      <h2>审核请求管理</h2>
      <el-button @click="loadRequests">刷新</el-button>
    </div>

    <div class="filters">
      <el-input v-model="keyword" placeholder="搜索学生/教师/简历" clearable />
      <el-select v-model="statusFilter" placeholder="状态" clearable>
        <el-option v-for="(label, value) in statusMap" :key="value" :label="label" :value="value" />
      </el-select>
      <el-select v-model="assignModeFilter" placeholder="分配方式" clearable>
        <el-option label="指定教师" value="SELECTED" />
        <el-option label="随机分配" value="RANDOM" />
      </el-select>
    </div>

    <el-table :data="filteredRequests" border empty-text="暂无审核请求">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="sourceResumeTitle" label="源简历" />
      <el-table-column prop="studentUsername" label="学生" width="130" />
      <el-table-column prop="teacherDisplayName" label="教师" width="150" />
      <el-table-column label="分配方式" width="110">
        <template #default="{ row }">{{ row.assignMode === "RANDOM" ? "随机分配" : "指定教师" }}</template>
      </el-table-column>
      <el-table-column label="状态" width="120">
        <template #default="{ row }">
          <el-tag :type="getStatusType(row.status)">{{ statusMap[row.status] || row.status }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="updateTime" label="更新时间" width="190" />
      <el-table-column label="操作" width="100">
        <template #default="{ row }">
          <el-button size="small" @click="openDetail(row)">详情</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="detailVisible" title="审核请求详情" width="760px">
      <el-descriptions v-if="selected" :column="1" border>
        <el-descriptions-item label="源简历">{{ selected.sourceResumeTitle }}</el-descriptions-item>
        <el-descriptions-item label="学生">{{ selected.studentUsername }}</el-descriptions-item>
        <el-descriptions-item label="教师">{{ selected.teacherDisplayName }}</el-descriptions-item>
        <el-descriptions-item label="状态">{{ statusMap[selected.status] || selected.status }}</el-descriptions-item>
        <el-descriptions-item label="学生说明">{{ selected.studentMessage || "未填写" }}</el-descriptions-item>
        <el-descriptions-item label="教师回复">{{ selected.teacherReply || "未填写" }}</el-descriptions-item>
        <el-descriptions-item label="拒绝理由">{{ selected.declineReason || "无" }}</el-descriptions-item>
        <el-descriptions-item label="拒绝建议">{{ selected.declineSuggestion || "无" }}</el-descriptions-item>
        <el-descriptions-item label="撤回原因">{{ selected.withdrawReason || "无" }}</el-descriptions-item>
        <el-descriptions-item label="管理员意见">{{ selected.adminComment || "无" }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from "vue"
import { getAdminReviewRequestsApi } from "../../api/admin"

const requests = ref([])
const keyword = ref("")
const statusFilter = ref("")
const assignModeFilter = ref("")
const detailVisible = ref(false)
const selected = ref(null)

const statusMap = {
  PENDING: "待处理",
  ACCEPTED: "已接受",
  DECLINED: "已拒绝",
  WITHDRAW_PENDING: "撤回处理中",
  WITHDRAWN: "已撤回",
  CANCELLED: "已取消",
  COMPLETED: "已完成"
}

const filteredRequests = computed(() => {
  return requests.value.filter(request => {
    const text = `${request.sourceResumeTitle || ""}${request.studentUsername || ""}${request.teacherDisplayName || ""}`
    const matchesKeyword = !keyword.value || text.includes(keyword.value)
    const matchesStatus = !statusFilter.value || request.status === statusFilter.value
    const matchesAssignMode = !assignModeFilter.value || request.assignMode === assignModeFilter.value
    return matchesKeyword && matchesStatus && matchesAssignMode
  })
})

onMounted(loadRequests)

async function loadRequests() {
  requests.value = await getAdminReviewRequestsApi()
}

function openDetail(row) {
  selected.value = row
  detailVisible.value = true
}

function getStatusType(status) {
  if (status === "ACCEPTED" || status === "COMPLETED") return "success"
  if (status === "DECLINED" || status === "CANCELLED" || status === "WITHDRAWN") return "danger"
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
