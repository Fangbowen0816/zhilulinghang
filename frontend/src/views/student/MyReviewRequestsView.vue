<template>
  <div class="page">
    <div class="page-header">
      <h2>我的审核请求</h2>
      <el-button @click="loadRequests">刷新</el-button>
    </div>

    <el-table :data="requests" border empty-text="暂无审核请求">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="sourceResumeTitle" label="简历" />
      <el-table-column prop="teacherDisplayName" label="教师" width="150" />
      <el-table-column label="分配方式" width="110">
        <template #default="{ row }">{{ assignModeMap[row.assignMode] || row.assignMode }}</template>
      </el-table-column>
      <el-table-column label="状态" width="110">
        <template #default="{ row }">
          <el-tag :type="getStatusType(row.status)">{{ statusMap[row.status] || row.status }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="declineReason" label="拒绝理由" />
      <el-table-column prop="declineSuggestion" label="建议" />
      <el-table-column prop="withdrawReason" label="撤回原因" />
      <el-table-column prop="adminComment" label="管理员意见" />
      <el-table-column prop="updateTime" label="更新时间" width="190" />
      <el-table-column label="操作" width="120">
        <template #default="{ row }">
          <el-button
            type="warning"
            size="small"
            :disabled="row.status !== 'ACCEPTED'"
            @click="openWithdraw(row)"
          >
            申请撤回
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <h2 class="section-title">审核记录</h2>
    <el-table :data="records" border empty-text="暂无审核记录">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="sourceResumeTitle" label="源简历" />
      <el-table-column prop="returnedResumeTitle" label="返回版本" />
      <el-table-column prop="teacherDisplayName" label="教师" width="150" />
      <el-table-column prop="comment" label="审核意见" />
      <el-table-column prop="createTime" label="时间" width="190" />
      <el-table-column label="操作" width="120">
        <template #default="{ row }">
          <el-button
            v-if="row.returnedResumeId"
            type="primary"
            size="small"
            @click="router.push(`/student/resume/${row.returnedResumeId}`)"
          >
            编辑版本
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="withdrawDialogVisible" title="申请撤回审核请求" width="520px">
      <el-input
        v-model="withdrawReason"
        type="textarea"
        :rows="4"
        placeholder="请填写申请撤回的原因，管理员会据此处理"
      />
      <template #footer>
        <el-button @click="withdrawDialogVisible = false">取消</el-button>
        <el-button type="warning" @click="submitWithdraw">提交申请</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, ref } from "vue"
import { useRouter } from "vue-router"
import { ElMessage } from "element-plus"
import { getStudentReviewRequestsApi, withdrawReviewRequestApi } from "../../api/reviewRequest"
import { getStudentReviewRecordsApi } from "../../api/reviewRecord"

const router = useRouter()
const requests = ref([])
const records = ref([])
const withdrawDialogVisible = ref(false)
const withdrawingRequest = ref(null)
const withdrawReason = ref("")

const statusMap = {
  PENDING: "待教师处理",
  ACCEPTED: "教师已接受",
  DECLINED: "教师已拒绝",
  WITHDRAW_PENDING: "撤回处理中",
  WITHDRAWN: "已撤回",
  CANCELLED: "已取消",
  COMPLETED: "已完成"
}

const assignModeMap = {
  SELECTED: "指定教师",
  RANDOM: "随机分配"
}

onMounted(loadRequests)

async function loadRequests() {
  requests.value = await getStudentReviewRequestsApi()
  records.value = await getStudentReviewRecordsApi()
}

function getStatusType(status) {
  if (status === "ACCEPTED" || status === "COMPLETED") return "success"
  if (status === "DECLINED" || status === "CANCELLED" || status === "WITHDRAWN") return "danger"
  return "warning"
}

function openWithdraw(row) {
  withdrawingRequest.value = row
  withdrawReason.value = ""
  withdrawDialogVisible.value = true
}

async function submitWithdraw() {
  if (!withdrawReason.value) {
    ElMessage.warning("请填写撤回原因")
    return
  }
  await withdrawReviewRequestApi(withdrawingRequest.value.id, withdrawReason.value)
  withdrawDialogVisible.value = false
  ElMessage.success("已提交撤回申请，等待管理员处理")
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

.section-title {
  margin: 24px 0 16px;
}
</style>
