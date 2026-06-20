<template>
  <div class="page">
    <div class="page-header">
      <h2>管理员工作台</h2>
      <el-button @click="loadDashboard">刷新</el-button>
    </div>

    <div class="stats-grid">
      <div v-for="item in stats" :key="item.label" class="stat-card">
        <span>{{ item.label }}</span>
        <strong>{{ item.value }}</strong>
      </div>
    </div>

    <div class="quick-actions">
      <el-button type="primary" @click="router.push('/admin/teacher-approvals')">教师资料审核</el-button>
      <el-button type="warning" @click="router.push('/admin/withdraw-requests')">撤回处理</el-button>
      <el-button @click="router.push('/admin/review-requests')">审核请求管理</el-button>
      <el-button @click="router.push('/admin/review-records')">审核记录</el-button>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from "vue"
import { useRouter } from "vue-router"
import { getAdminDashboardApi } from "../../api/admin"

const router = useRouter()
const dashboard = ref({})

const stats = computed(() => [
  { label: "学生数量", value: dashboard.value.studentCount || 0 },
  { label: "教师数量", value: dashboard.value.teacherCount || 0 },
  { label: "待审核教师", value: dashboard.value.pendingTeacherCount || 0 },
  { label: "简历总数", value: dashboard.value.resumeCount || 0 },
  { label: "冻结简历", value: dashboard.value.frozenResumeCount || 0 },
  { label: "审核请求", value: dashboard.value.reviewRequestCount || 0 },
  { label: "待处理请求", value: dashboard.value.pendingRequestCount || 0 },
  { label: "撤回待处理", value: dashboard.value.withdrawPendingCount || 0 },
  { label: "已完成请求", value: dashboard.value.completedRequestCount || 0 },
  { label: "审核记录", value: dashboard.value.reviewRecordCount || 0 }
])

onMounted(loadDashboard)

async function loadDashboard() {
  dashboard.value = await getAdminDashboardApi()
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

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(160px, 1fr));
  gap: 14px;
}

.stat-card {
  display: flex;
  flex-direction: column;
  gap: 10px;
  padding: 18px;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  background: #fff;
}

.stat-card span {
  color: #667085;
  font-size: 14px;
}

.stat-card strong {
  color: #1f2937;
  font-size: 28px;
}

.quick-actions {
  margin-top: 22px;
}
</style>
