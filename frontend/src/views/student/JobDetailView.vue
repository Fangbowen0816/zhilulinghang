<template>
  <div class="page">
    <div class="page-header">
      <el-button @click="router.back()">返回</el-button>
      <el-button type="primary" :disabled="job?.status !== 'OPEN'" @click="apply">
        投递岗位
      </el-button>
    </div>

    <el-skeleton v-if="loading" :rows="8" animated />

    <template v-else-if="job">
      <section class="job-head">
        <div>
          <h2>{{ job.title }}</h2>
          <p>{{ job.company }}</p>
        </div>
        <el-tag :type="job.status === 'OPEN' ? 'success' : 'info'">
          {{ statusMap[job.status] || job.status }}
        </el-tag>
      </section>

      <el-descriptions :column="2" border>
        <el-descriptions-item label="行业">{{ job.industry || "未填写" }}</el-descriptions-item>
        <el-descriptions-item label="城市">{{ job.city || "未填写" }}</el-descriptions-item>
        <el-descriptions-item label="薪资">{{ job.salaryRange || "未填写" }}</el-descriptions-item>
        <el-descriptions-item label="更新时间">{{ formatTime(job.updateTime) }}</el-descriptions-item>
      </el-descriptions>

      <section class="content-section">
        <h3>岗位要求</h3>
        <p>{{ job.requirement || "暂无岗位要求" }}</p>
      </section>

      <section class="content-section">
        <h3>岗位描述</h3>
        <p>{{ job.description || "暂无岗位描述" }}</p>
      </section>

      <section class="apply-section">
        <h3>选择投递简历</h3>
        <el-select v-model="selectedResumeId" placeholder="请选择一份简历" class="resume-select">
          <el-option
            v-for="resume in resumes"
            :key="resume.id"
            :label="resume.title || resume.name || `简历 #${resume.id}`"
            :value="resume.id"
          />
        </el-select>
      </section>
    </template>
  </div>
</template>

<script setup>
import { onMounted, ref } from "vue"
import { useRoute, useRouter } from "vue-router"
import { ElMessage } from "element-plus"
import { getMyResumesApi } from "../../api/resume"
import { applyJobApi, getJobByIdApi } from "../../api/job"

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const job = ref(null)
const resumes = ref([])
const selectedResumeId = ref(null)

const statusMap = {
  OPEN: "招聘中",
  CLOSED: "已关闭"
}

onMounted(loadData)

async function loadData() {
  loading.value = true
  try {
    const [jobData, resumeData] = await Promise.all([
      getJobByIdApi(route.params.id),
      getMyResumesApi()
    ])
    job.value = jobData
    resumes.value = resumeData
    selectedResumeId.value = resumeData[0]?.id || null
  } finally {
    loading.value = false
  }
}

async function apply() {
  if (!selectedResumeId.value) {
    ElMessage.warning("请先选择用于投递的简历")
    return
  }
  await applyJobApi(job.value.id, selectedResumeId.value)
  ElMessage.success("投递成功")
  router.push("/student/applications")
}

function formatTime(value) {
  if (!value) return "暂无"
  return String(value).replace("T", " ").slice(0, 16)
}
</script>

<style scoped>
.page {
  max-width: 920px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 20px;
}

.job-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 20px;
  margin-bottom: 20px;
  padding: 22px;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  background: #fff;
}

.job-head h2 {
  margin: 0 0 8px;
}

.job-head p {
  margin: 0;
  color: #667085;
}

.content-section,
.apply-section {
  margin-top: 18px;
  padding: 18px;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  background: #fff;
}

.content-section h3,
.apply-section h3 {
  margin: 0 0 12px;
}

.content-section p {
  margin: 0;
  color: #344054;
  line-height: 1.7;
  white-space: pre-wrap;
}

.resume-select {
  width: 360px;
  max-width: 100%;
}
</style>
