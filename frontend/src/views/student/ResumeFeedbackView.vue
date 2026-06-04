<template>
  <div class="page">
    <h2>简历反馈</h2>

    <el-empty v-if="!resume" description="暂无简历反馈" />

    <el-descriptions v-else :column="1" border>
      <el-descriptions-item label="姓名">{{ resume.name }}</el-descriptions-item>
      <el-descriptions-item label="状态">
        <el-tag :type="statusType">{{ statusText }}</el-tag>
      </el-descriptions-item>
      <el-descriptions-item label="教师意见">
        {{ resume.teacherComment || "教师暂未填写反馈" }}
      </el-descriptions-item>
      <el-descriptions-item label="教育经历">{{ resume.education }}</el-descriptions-item>
      <el-descriptions-item label="项目经历">{{ resume.experience }}</el-descriptions-item>
      <el-descriptions-item label="技能">{{ resume.skills }}</el-descriptions-item>
    </el-descriptions>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from "vue"
import { getResumeFeedbackApi } from "../../api/resume"

const resume = ref(null)

const statusMap = {
  DRAFT: "草稿",
  SUBMITTED: "待审核",
  APPROVED: "已通过",
  REJECTED: "已退回"
}

const statusText = computed(() => statusMap[resume.value?.status] || "未知")
const statusType = computed(() => {
  if (resume.value?.status === "APPROVED") return "success"
  if (resume.value?.status === "REJECTED") return "danger"
  if (resume.value?.status === "SUBMITTED") return "warning"
  return "info"
})

onMounted(async () => {
  const result = await getResumeFeedbackApi()
  resume.value = result.resume
})
</script>

<style scoped>
.page {
  max-width: 900px;
}

h2 {
  margin: 0 0 20px;
}
</style>
