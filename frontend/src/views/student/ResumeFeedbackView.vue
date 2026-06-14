<template>
  <div class="page">
    <h2>简历反馈</h2>

    <el-empty v-if="resumes.length === 0" description="暂无简历反馈" />

    <el-table v-else :data="resumes" border>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="title" label="简历名称" width="180">
        <template #default="{ row }">
          {{ row.title || row.name || "未命名简历" }}
        </template>
      </el-table-column>
      <el-table-column prop="targetPosition" label="求职意向" width="180" />
      <el-table-column label="状态" width="110">
        <template #default="{ row }">
          <el-tag :type="getStatusType(row.status)">{{ statusMap[row.status] || row.status }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="teacherComment" label="教师意见">
        <template #default="{ row }">
          {{ row.teacherComment || "教师暂未填写反馈" }}
        </template>
      </el-table-column>
      <el-table-column prop="updateTime" label="更新时间" width="190" />
    </el-table>
  </div>
</template>

<script setup>
import { onMounted, ref } from "vue"
import { getResumeFeedbackApi } from "../../api/resume"

const resumes = ref([])

const statusMap = {
  DRAFT: "草稿",
  SUBMITTED: "待审核",
  APPROVED: "已通过",
  REJECTED: "已退回"
}

onMounted(async () => {
  const result = await getResumeFeedbackApi()
  resumes.value = result.resumes || []
})

function getStatusType(status) {
  if (status === "APPROVED") return "success"
  if (status === "REJECTED") return "danger"
  if (status === "SUBMITTED") return "warning"
  return "info"
}
</script>

<style scoped>
.page {
  max-width: 960px;
}

h2 {
  margin: 0 0 20px;
}
</style>
