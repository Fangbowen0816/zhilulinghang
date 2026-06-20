<template>
  <div class="page">
    <div class="page-header">
      <h2>撤回申请处理</h2>
      <el-button @click="loadRequests">刷新</el-button>
    </div>

    <el-table :data="requests" border empty-text="暂无待处理撤回申请">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="sourceResumeTitle" label="简历" />
      <el-table-column prop="studentUsername" label="学生" width="130" />
      <el-table-column prop="teacherDisplayName" label="教师" width="150" />
      <el-table-column prop="withdrawReason" label="撤回原因" />
      <el-table-column prop="updateTime" label="申请时间" width="190" />
      <el-table-column label="操作" width="180">
        <template #default="{ row }">
          <el-button type="success" size="small" @click="openDecision(row, 'approve')">同意</el-button>
          <el-button type="danger" size="small" @click="openDecision(row, 'reject')">拒绝</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="decisionDialogVisible" :title="decisionMode === 'approve' ? '同意撤回' : '拒绝撤回'" width="520px">
      <el-input
        v-model="adminComment"
        type="textarea"
        :rows="4"
        :placeholder="decisionMode === 'approve' ? '可填写处理说明' : '拒绝撤回时必须填写处理意见'"
      />
      <template #footer>
        <el-button @click="decisionDialogVisible = false">取消</el-button>
        <el-button :type="decisionMode === 'approve' ? 'success' : 'danger'" @click="submitDecision">
          确认
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, ref } from "vue"
import { ElMessage } from "element-plus"
import {
  approveWithdrawRequestApi,
  getAdminWithdrawRequestsApi,
  rejectWithdrawRequestApi
} from "../../api/reviewRequest"

const requests = ref([])
const decisionDialogVisible = ref(false)
const selectedRequest = ref(null)
const decisionMode = ref("approve")
const adminComment = ref("")

onMounted(loadRequests)

async function loadRequests() {
  requests.value = await getAdminWithdrawRequestsApi()
}

function openDecision(row, mode) {
  selectedRequest.value = row
  decisionMode.value = mode
  adminComment.value = ""
  decisionDialogVisible.value = true
}

async function submitDecision() {
  if (decisionMode.value === "reject" && !adminComment.value) {
    ElMessage.warning("拒绝撤回时必须填写处理意见")
    return
  }
  if (decisionMode.value === "approve") {
    await approveWithdrawRequestApi(selectedRequest.value.id, adminComment.value)
    ElMessage.success("已同意撤回申请")
  } else {
    await rejectWithdrawRequestApi(selectedRequest.value.id, adminComment.value)
    ElMessage.success("已拒绝撤回申请")
  }
  decisionDialogVisible.value = false
  await loadRequests()
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
