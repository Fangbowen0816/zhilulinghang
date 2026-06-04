<template>
  <div class="page">
    <div class="page-header">
      <div>
        <h2>制作简历</h2>
        <p>填写并保存简历，确认无误后提交给教师审核。</p>
      </div>
      <el-tag :type="statusType">{{ statusText }}</el-tag>
    </div>

    <el-form label-width="90px" :model="form" class="resume-form">
      <el-form-item label="姓名">
        <el-input v-model="form.name" placeholder="请输入姓名" />
      </el-form-item>
      <el-form-item label="教育经历">
        <el-input v-model="form.education" type="textarea" :rows="4" placeholder="学校、专业、课程或荣誉" />
      </el-form-item>
      <el-form-item label="项目经历">
        <el-input v-model="form.experience" type="textarea" :rows="5" placeholder="描述项目职责、技术栈和成果" />
      </el-form-item>
      <el-form-item label="技能">
        <el-input v-model="form.skills" type="textarea" :rows="3" placeholder="例如：Java, Spring Boot, Vue, MySQL" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="saveResume">保存草稿</el-button>
        <el-button type="success" :disabled="!resumeId" @click="submitResume">提交审核</el-button>
      </el-form-item>
    </el-form>

    <el-alert
      v-if="teacherComment"
      title="教师反馈"
      :description="teacherComment"
      type="info"
      show-icon
      :closable="false"
    />
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from "vue"
import { ElMessage } from "element-plus"
import { createResumeApi, getMyResumeApi, submitResumeApi, updateResumeApi } from "../../api/resume"

const resumeId = ref(null)
const status = ref("DRAFT")
const teacherComment = ref("")

const form = reactive({
  name: "",
  education: "",
  experience: "",
  skills: ""
})

const statusMap = {
  DRAFT: "草稿",
  SUBMITTED: "待审核",
  APPROVED: "已通过",
  REJECTED: "已退回"
}

const statusText = computed(() => statusMap[status.value] || "未创建")
const statusType = computed(() => {
  if (status.value === "APPROVED") return "success"
  if (status.value === "REJECTED") return "danger"
  if (status.value === "SUBMITTED") return "warning"
  return "info"
})

onMounted(loadResume)

async function loadResume() {
  const resume = await getMyResumeApi()
  if (!resume) return
  resumeId.value = resume.id
  status.value = resume.status
  teacherComment.value = resume.teacherComment || ""
  form.name = resume.name || ""
  form.education = resume.education || ""
  form.experience = resume.experience || ""
  form.skills = resume.skills || ""
}

async function saveResume() {
  if (!form.name) {
    ElMessage.warning("请先填写姓名")
    return
  }
  const payload = { ...form }
  const resume = resumeId.value
    ? await updateResumeApi(resumeId.value, payload)
    : await createResumeApi(payload)

  resumeId.value = resume.id
  status.value = resume.status
  teacherComment.value = resume.teacherComment || ""
  ElMessage.success("简历已保存")
}

async function submitResume() {
  const resume = await submitResumeApi(resumeId.value)
  status.value = resume.status
  ElMessage.success("已提交教师审核")
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

.resume-form {
  max-width: 760px;
}
</style>
