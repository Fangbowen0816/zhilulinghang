<template>
  <div class="page">
    <div class="page-header">
      <div>
        <h2>制作简历</h2>
        <p>维护多份简历，选择一份编辑后可提交给教师审核。</p>
      </div>
      <el-tag :type="statusType">{{ statusText }}</el-tag>
    </div>

    <div class="resume-toolbar">
      <el-select
        v-model="resumeId"
        placeholder="请选择简历"
        class="resume-select"
        :disabled="resumes.length === 0"
        @change="selectResume"
      >
        <el-option
          v-for="resume in resumes"
          :key="resume.id"
          :label="`${resume.title || resume.name || '未命名简历'} #${resume.id}`"
          :value="resume.id"
        >
          <span>{{ resume.title || resume.name || "未命名简历" }}</span>
          <el-tag size="small" class="option-tag">{{ statusMap[resume.status] || resume.status }}</el-tag>
        </el-option>
      </el-select>

      <el-button @click="newResume">新建简历</el-button>
      <el-button type="danger" :disabled="!resumeId" @click="deleteResume">删除简历</el-button>
    </div>

    <el-form label-width="90px" :model="form" class="resume-form">
      <el-form-item label="简历模板">
        <el-select v-model="form.templateId" placeholder="请选择简历模板" class="template-select">
          <el-option
            v-for="template in templates"
            :key="template.id"
            :label="`${template.name}｜${template.industry || '通用'}｜${template.jobType || '通用'}`"
            :value="template.id"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="简历名称">
        <el-input v-model="form.title" placeholder="例如：Java 后端校招简历、前端实习简历" />
      </el-form-item>
      <el-form-item label="姓名">
        <el-input v-model="form.name" placeholder="请输入姓名" />
      </el-form-item>
      <el-form-item label="手机号">
        <el-input v-model="form.phone" placeholder="请输入手机号" />
      </el-form-item>
      <el-form-item label="邮箱">
        <el-input v-model="form.email" placeholder="请输入邮箱" />
      </el-form-item>
      <el-form-item label="求职意向">
        <el-input v-model="form.targetPosition" placeholder="例如：Java 后端开发实习生" />
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
      <el-form-item label="奖项证书">
        <el-input v-model="form.awards" type="textarea" :rows="3" placeholder="竞赛奖项、证书、荣誉称号等" />
      </el-form-item>
      <el-form-item label="自我评价">
        <el-input v-model="form.selfEvaluation" type="textarea" :rows="3" placeholder="概括个人优势、学习能力、协作能力等" />
      </el-form-item>
      <el-form-item label="润色目标">
        <el-input
          v-model="polishGoal"
          type="textarea"
          :rows="2"
          placeholder="可选，例如：面向 Java 后端校招，突出项目成果和技术能力"
        />
      </el-form-item>
      <el-alert
        v-if="polishError"
        :title="polishError"
        type="warning"
        show-icon
        class="polish-error"
        @close="polishError = ''"
      />
      <el-form-item>
        <el-button type="primary" @click="saveResume">保存草稿</el-button>
        <el-button type="success" @click="openReviewRequestDialog">提交审核</el-button>
        <el-button type="warning" :loading="polishLoading" @click="polishResume">
          {{ polishLoading ? "AI 润色中" : "AI 润色" }}
        </el-button>
        <el-button @click="previewResume">预览</el-button>
        <el-button :disabled="!resumeId && !hasResumeContent()" @click="exportResume">导出/打印</el-button>
        <el-button :disabled="!resumeId" @click="router.push(`/student/resume/${resumeId}/versions`)">返回版本</el-button>
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

    <el-dialog v-model="polishDialogVisible" title="AI 润色结果" width="720px">
      <el-alert
        v-if="polishedResume.summary"
        :title="polishedResume.summary"
        type="success"
        show-icon
        :closable="false"
        class="polish-summary"
      />
      <el-descriptions :column="1" border>
        <el-descriptions-item label="简历名称">{{ polishedResume.title || "未填写" }}</el-descriptions-item>
        <el-descriptions-item label="姓名">{{ polishedResume.name || "未填写" }}</el-descriptions-item>
        <el-descriptions-item label="手机号">{{ polishedResume.phone || "未填写" }}</el-descriptions-item>
        <el-descriptions-item label="邮箱">{{ polishedResume.email || "未填写" }}</el-descriptions-item>
        <el-descriptions-item label="求职意向">{{ polishedResume.targetPosition || "未填写" }}</el-descriptions-item>
        <el-descriptions-item label="教育经历">
          <div class="polished-text">{{ polishedResume.education || "未填写" }}</div>
        </el-descriptions-item>
        <el-descriptions-item label="项目经历">
          <div class="polished-text">{{ polishedResume.experience || "未填写" }}</div>
        </el-descriptions-item>
        <el-descriptions-item label="技能">
          <div class="polished-text">{{ polishedResume.skills || "未填写" }}</div>
        </el-descriptions-item>
        <el-descriptions-item label="奖项证书">
          <div class="polished-text">{{ polishedResume.awards || "未填写" }}</div>
        </el-descriptions-item>
        <el-descriptions-item label="自我评价">
          <div class="polished-text">{{ polishedResume.selfEvaluation || "未填写" }}</div>
        </el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="polishDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="applyPolishedResume">采纳润色结果</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="previewDialogVisible" title="简历预览" width="860px">
      <iframe v-if="previewHtml" class="preview-frame" :srcdoc="previewHtml" title="简历预览"></iframe>
      <el-empty v-else description="暂无可预览内容" />
      <template #footer>
        <el-button @click="exportResume">导出/打印</el-button>
        <el-button type="primary" @click="previewDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="reviewRequestDialogVisible" title="提交给教师审核" width="720px">
      <el-form label-width="90px">
        <el-form-item label="分配方式">
          <el-radio-group v-model="reviewRequestForm.assignMode">
            <el-radio-button label="SELECTED">指定教师</el-radio-button>
            <el-radio-button label="RANDOM">随机分配</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="reviewRequestForm.assignMode === 'SELECTED'" label="选择教师">
          <el-select
            v-model="reviewRequestForm.teacherIds"
            multiple
            filterable
            placeholder="请选择一位或多位教师"
            style="width: 100%"
          >
            <el-option
              v-for="teacher in availableTeachers"
              :key="teacher.teacherId"
              :label="`${teacher.displayName}｜${teacher.expertiseTags || '未填写擅长方向'}`"
              :value="teacher.teacherId"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="请求说明">
          <el-input
            v-model="reviewRequestForm.studentMessage"
            type="textarea"
            :rows="4"
            placeholder="可说明希望教师重点关注的问题"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="reviewRequestDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="createReviewRequest">确认提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref, watch } from "vue"
import { useRoute, useRouter } from "vue-router"
import { ElMessage, ElMessageBox } from "element-plus"
import {
  createResumeApi,
  deleteResumeApi,
  getResumeExportApi,
  getResumePreviewApi,
  getResumeTemplatesApi,
  getMyResumesApi,
  polishResumeApi,
  updateResumeApi
} from "../../api/resume"
import { getAvailableTeachersApi } from "../../api/teacher"
import { createReviewRequestsApi } from "../../api/reviewRequest"

const route = useRoute()
const router = useRouter()
const resumes = ref([])
const resumeId = ref(null)
const status = ref("DRAFT")
const teacherComment = ref("")
const polishGoal = ref("")
const polishLoading = ref(false)
const polishError = ref("")
const polishDialogVisible = ref(false)
const previewDialogVisible = ref(false)
const reviewRequestDialogVisible = ref(false)
const availableTeachers = ref([])
const templates = ref([])
const previewHtml = ref("")

const form = reactive({
  templateId: null,
  title: "",
  name: "",
  phone: "",
  email: "",
  targetPosition: "",
  education: "",
  experience: "",
  skills: "",
  awards: "",
  selfEvaluation: ""
})

const polishedResume = reactive({
  title: "",
  name: "",
  phone: "",
  email: "",
  targetPosition: "",
  education: "",
  experience: "",
  skills: "",
  awards: "",
  selfEvaluation: "",
  summary: ""
})

const reviewRequestForm = reactive({
  assignMode: "SELECTED",
  teacherIds: [],
  studentMessage: ""
})

const statusMap = {
  DRAFT: "草稿",
  SUBMITTED: "待审核",
  APPROVED: "已通过",
  REJECTED: "已退回"
}

const statusText = computed(() => (resumeId.value ? statusMap[status.value] || "未知" : "新简历"))
const statusType = computed(() => {
  if (status.value === "APPROVED") return "success"
  if (status.value === "REJECTED") return "danger"
  if (status.value === "SUBMITTED") return "warning"
  return "info"
})

onMounted(async () => {
  await loadTemplates()
  await loadResumes(routeResumeId.value)
})

const routeResumeId = computed(() => {
  const id = Number(route.params.id)
  return Number.isFinite(id) && id > 0 ? id : null
})

watch(routeResumeId, id => {
  loadResumes(id)
})

async function loadResumes(selectId) {
  resumes.value = await getMyResumesApi()
  const selected = resumes.value.find(item => item.id === selectId) || resumes.value[0]
  if (selectId && !selected) {
    ElMessage.warning("简历不存在或无权访问")
    router.replace("/student/resume/edit")
    newResume()
  } else if (selected && (selectId || route.path !== "/student/resume/edit")) {
    applyResume(selected)
  } else {
    newResume()
  }
}

async function loadTemplates() {
  templates.value = await getResumeTemplatesApi()
  if (!form.templateId && templates.value.length > 0) {
    form.templateId = templates.value[0].id
  }
}

function selectResume(id) {
  const selected = resumes.value.find(item => item.id === id)
  if (selected) {
    router.push(`/student/resume/${id}`)
  }
}

function applyResume(resume) {
  resumeId.value = resume.id
  status.value = resume.status
  teacherComment.value = resume.teacherComment || ""
  form.title = resume.title || ""
  form.templateId = resume.templateId || templates.value[0]?.id || null
  form.name = resume.name || ""
  form.phone = resume.phone || ""
  form.email = resume.email || ""
  form.targetPosition = resume.targetPosition || ""
  form.education = resume.education || ""
  form.experience = resume.experience || ""
  form.skills = resume.skills || ""
  form.awards = resume.awards || ""
  form.selfEvaluation = resume.selfEvaluation || ""
}

function newResume() {
  if (route.path !== "/student/resume/edit") {
    router.push("/student/resume/edit")
  }
  resumeId.value = null
  status.value = "DRAFT"
  teacherComment.value = ""
  polishGoal.value = ""
  form.title = ""
  form.templateId = templates.value[0]?.id || null
  form.name = ""
  form.phone = ""
  form.email = ""
  form.targetPosition = ""
  form.education = ""
  form.experience = ""
  form.skills = ""
  form.awards = ""
  form.selfEvaluation = ""
}

async function saveResume() {
  const validationMessage = validateDraft()
  if (validationMessage) {
    ElMessage.warning(validationMessage)
    return
  }
  ensureTitle()
  const payload = { ...form }
  const resume = resumeId.value
    ? await updateResumeApi(resumeId.value, payload)
    : await createResumeApi(payload)

  await loadResumes(resume.id)
  if (routeResumeId.value !== resume.id) {
    router.replace(`/student/resume/${resume.id}`)
  }
  ElMessage.success("简历已保存")
  return resume
}

async function ensureSavedForPreview() {
  const resume = await saveResume()
  if (!resume?.id) {
    return null
  }
  return resume.id
}

async function previewResume() {
  const id = await ensureSavedForPreview()
  if (!id) return
  const result = await getResumePreviewApi(id)
  previewHtml.value = result.html || ""
  previewDialogVisible.value = true
}

async function exportResume() {
  const id = await ensureSavedForPreview()
  if (!id) return
  const result = await getResumeExportApi(id)
  openPrintableHtml(result.html || "")
}

function openPrintableHtml(html) {
  const win = window.open("", "_blank")
  if (!win) {
    ElMessage.warning("浏览器阻止了弹窗，请允许弹窗后重试")
    return
  }
  win.document.open()
  win.document.write(html)
  win.document.close()
  win.focus()
}

async function openReviewRequestDialog() {
  const validationMessage = validateSubmit()
  if (validationMessage) {
    ElMessage.warning(validationMessage)
    return
  }
  ensureTitle()
  if (!resumeId.value) {
    await saveResume()
    if (!resumeId.value) {
      return
    }
  }
  availableTeachers.value = await getAvailableTeachersApi()
  reviewRequestForm.assignMode = "SELECTED"
  reviewRequestForm.teacherIds = []
  reviewRequestForm.studentMessage = ""
  reviewRequestDialogVisible.value = true
}

async function createReviewRequest() {
  if (reviewRequestForm.assignMode === "SELECTED" && reviewRequestForm.teacherIds.length === 0) {
    ElMessage.warning("请选择至少一位教师")
    return
  }
  const requests = await createReviewRequestsApi({
    sourceResumeId: resumeId.value,
    assignMode: reviewRequestForm.assignMode,
    teacherIds: reviewRequestForm.teacherIds,
    studentMessage: reviewRequestForm.studentMessage
  })
  reviewRequestDialogVisible.value = false
  await loadResumes(resumeId.value)
  ElMessage.success(`已创建 ${requests.length} 个审核请求`)
}

async function deleteResume() {
  await ElMessageBox.confirm("删除后无法恢复，确认删除这份简历吗？", "删除简历", {
    type: "warning",
    confirmButtonText: "确认删除",
    cancelButtonText: "取消"
  })
  await deleteResumeApi(resumeId.value)
  await loadResumes()
  router.replace("/student/resume/edit")
  ElMessage.success("简历已删除")
}

async function polishResume() {
  if (!hasResumeContent()) {
    ElMessage.warning("请先填写简历内容，再使用 AI 润色")
    return
  }

  polishError.value = ""
  polishLoading.value = true
  try {
    ElMessage.info("正在调用 /api/resume/polish 生成润色建议，请稍候")
    const result = await polishResumeApi({
      ...form,
      goal: polishGoal.value
    })
    polishedResume.title = result.title || ""
    polishedResume.name = result.name || ""
    polishedResume.phone = result.phone || ""
    polishedResume.email = result.email || ""
    polishedResume.targetPosition = result.targetPosition || ""
    polishedResume.education = result.education || ""
    polishedResume.experience = result.experience || ""
    polishedResume.skills = result.skills || ""
    polishedResume.awards = result.awards || ""
    polishedResume.selfEvaluation = result.selfEvaluation || ""
    polishedResume.summary = result.summary || ""
    polishDialogVisible.value = true
  } catch (error) {
    const message = error.response?.data?.message || "AI 润色暂时不可用，请稍后重试"
    polishError.value = message.includes("JSON")
      ? "AI 已返回内容，但格式不够规整。系统已增强解析能力；如果仍失败，请补充更明确的润色目标后重试。"
      : message
  } finally {
    polishLoading.value = false
  }
}

function applyPolishedResume() {
  form.title = polishedResume.title
  form.name = polishedResume.name
  form.phone = polishedResume.phone
  form.email = polishedResume.email
  form.targetPosition = polishedResume.targetPosition
  form.education = polishedResume.education
  form.experience = polishedResume.experience
  form.skills = polishedResume.skills
  form.awards = polishedResume.awards
  form.selfEvaluation = polishedResume.selfEvaluation
  polishDialogVisible.value = false
  ElMessage.success("已采纳润色结果，请确认后保存")
}

function hasResumeContent() {
  return [
    form.name,
    form.targetPosition,
    form.education,
    form.experience,
    form.skills,
    form.awards,
    form.selfEvaluation
  ].some(value => value && value.trim())
}

function validateDraft() {
  if (!form.name.trim()) {
    return "请先填写姓名"
  }
  if (form.email && !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.email.trim())) {
    return "邮箱格式不正确"
  }
  if (form.phone && !/^[0-9+\-\s()]{6,30}$/.test(form.phone.trim())) {
    return "手机号格式不正确"
  }
  return ""
}

function validateSubmit() {
  const draftMessage = validateDraft()
  if (draftMessage) {
    return draftMessage
  }
  if (!form.targetPosition.trim()) {
    return "提交审核前请填写求职意向"
  }
  if (![form.education, form.experience, form.skills].some(value => value && value.trim())) {
    return "提交审核前请至少填写教育经历、项目经历或技能"
  }
  return ""
}

function ensureTitle() {
  if (form.title.trim()) {
    form.title = form.title.trim()
    return
  }
  if (form.name.trim() && form.targetPosition.trim()) {
    form.title = `${form.name.trim()} - ${form.targetPosition.trim()}`
  } else if (form.targetPosition.trim()) {
    form.title = `${form.targetPosition.trim()}简历`
  } else {
    form.title = "我的简历"
  }
}
</script>

<style scoped>
.page {
  max-width: 960px;
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

.resume-toolbar {
  display: flex;
  gap: 12px;
  align-items: center;
  margin-bottom: 20px;
}

.resume-select {
  width: 320px;
}

.template-select {
  width: 360px;
  max-width: 100%;
}

.option-tag {
  float: right;
  margin-top: 2px;
}

.resume-form {
  max-width: 760px;
}

.polish-summary {
  margin-bottom: 16px;
}

.polish-error {
  margin-bottom: 16px;
}

.polished-text {
  white-space: pre-wrap;
  line-height: 1.7;
}

.preview-frame {
  width: 100%;
  height: 640px;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
}
</style>
