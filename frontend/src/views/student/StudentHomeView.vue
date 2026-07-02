<template>
  <div class="page">
    <div class="page-header">
      <div>
        <h2>我的简历</h2>
        <p>选择一份简历进入编辑、查看状态或提交审核。</p>
      </div>
      <el-button type="primary" @click="createResume">新建简历</el-button>
    </div>

    <el-empty v-if="!loading && resumes.length === 0" description="暂无简历">
      <el-button type="primary" @click="createResume">新建第一份简历</el-button>
    </el-empty>

    <el-scrollbar v-else v-loading="loading" class="resume-scroll">
      <div class="resume-list">
        <button
          v-for="resume in resumes"
          :key="resume.id"
          class="resume-item"
          type="button"
          @click="openResume(resume.id)"
        >
          <span class="resume-name">{{ resume.title || resume.name || `未命名简历 #${resume.id}` }}</span>
          <span class="resume-meta">
            <el-tag :type="getStatusType(resume.status)" size="small">
              {{ statusMap[resume.status] || resume.status }}
            </el-tag>
            <span>{{ formatTime(resume.updateTime) }}</span>
          </span>
        </button>
      </div>
    </el-scrollbar>

    <section class="reminder-section">
      <div class="section-header">
        <h2>待提醒事项</h2>
        <el-button text type="primary" @click="router.push('/student/applications')">查看投递记录</el-button>
      </div>
      <el-empty v-if="!reminderLoading && reminders.length === 0" description="暂无待提醒事项" />
      <el-table v-else v-loading="reminderLoading" :data="reminders" border>
        <el-table-column prop="jobTitle" label="岗位" />
        <el-table-column prop="company" label="公司" width="150" />
        <el-table-column label="类型" width="100">
          <template #default="{ row }">{{ reminderTypeMap[row.remindType] || row.remindType }}</template>
        </el-table-column>
        <el-table-column prop="content" label="内容" />
        <el-table-column label="时间" width="170">
          <template #default="{ row }">{{ formatTime(row.remindTime) }}</template>
        </el-table-column>
      </el-table>
    </section>
  </div>
</template>

<script setup>
import { onMounted, ref } from "vue"
import { useRouter } from "vue-router"
import { ElMessage } from "element-plus"
import { getPendingApplicationRemindersApi } from "../../api/application"
import { getMyResumesApi } from "../../api/resume"

const router = useRouter()
const loading = ref(false)
const reminderLoading = ref(false)
const resumes = ref([])
const reminders = ref([])

const statusMap = {
  DRAFT: "草稿",
  SUBMITTED: "待审核",
  APPROVED: "已通过",
  REJECTED: "已退回"
}

const reminderTypeMap = {
  FOLLOW_UP: "跟进",
  WRITTEN_TEST: "笔试",
  INTERVIEW: "面试",
  GENERAL: "通用"
}

onMounted(() => {
  loadResumes()
  loadReminders()
})

async function loadResumes() {
  loading.value = true
  try {
    resumes.value = await getMyResumesApi()
  } finally {
    loading.value = false
  }
}

async function loadReminders() {
  reminderLoading.value = true
  try {
    reminders.value = await getPendingApplicationRemindersApi()
  } finally {
    reminderLoading.value = false
  }
}

function openResume(id) {
  router.push(`/student/resume/${id}`)
}

function createResume() {
  router.push("/student/resume/edit")
  ElMessage.info("请填写简历内容后保存")
}

function getStatusType(status) {
  if (status === "APPROVED") return "success"
  if (status === "REJECTED") return "danger"
  if (status === "SUBMITTED") return "warning"
  return "info"
}

function formatTime(value) {
  if (!value) return "暂无更新时间"
  return String(value).replace("T", " ").slice(0, 16)
}
</script>

<style scoped>
.page {
  max-width: 900px;
}

.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0 0 8px;
}

.page-header p {
  margin: 0;
  color: #667085;
}

.resume-scroll {
  max-height: calc(100vh - 190px);
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  background: #fff;
}

.resume-list {
  display: flex;
  flex-direction: column;
}

.resume-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  width: 100%;
  min-height: 64px;
  padding: 14px 18px;
  border: 0;
  border-bottom: 1px solid #eef0f4;
  background: #fff;
  color: #1f2937;
  cursor: pointer;
  text-align: left;
}

.resume-item:hover {
  background: #f5f7fa;
}

.resume-item:last-child {
  border-bottom: 0;
}

.resume-name {
  overflow: hidden;
  font-size: 16px;
  font-weight: 600;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.resume-meta {
  display: flex;
  flex-shrink: 0;
  align-items: center;
  gap: 12px;
  color: #667085;
  font-size: 13px;
}

.reminder-section {
  margin-top: 24px;
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 14px;
}

.section-header h2 {
  margin: 0;
}
</style>
