<template>
  <div class="page">
    <div class="page-header">
      <h2>审核请求</h2>
      <el-button @click="loadRequests">刷新</el-button>
    </div>

    <el-table :data="requests" border empty-text="暂无审核请求">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="sourceResumeTitle" label="简历" />
      <el-table-column prop="studentUsername" label="学生" width="130" />
      <el-table-column prop="studentMessage" label="学生说明" />
      <el-table-column label="状态" width="120">
        <template #default="{ row }">
          <el-tag :type="getStatusType(row.status)">{{ statusMap[row.status] || row.status }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="updateTime" label="更新时间" width="190" />
      <el-table-column label="操作" width="320">
        <template #default="{ row }">
          <el-button size="small" @click="openDetail(row)">查看</el-button>
          <el-button type="success" size="small" :disabled="row.status !== 'PENDING'" @click="accept(row)">接受</el-button>
          <el-button type="danger" size="small" :disabled="row.status !== 'PENDING'" @click="openDecline(row)">拒绝</el-button>
          <el-button type="warning" size="small" :disabled="row.status !== 'ACCEPTED'" @click="openFeedback(row)">批注评分</el-button>
          <el-button type="primary" size="small" :disabled="row.status !== 'ACCEPTED'" @click="openReturn(row)">返回</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="detailDialogVisible" title="审核请求详情" width="760px">
      <el-descriptions v-if="detail.request" :column="1" border>
        <el-descriptions-item label="简历">{{ detail.request.sourceResumeTitle }}</el-descriptions-item>
        <el-descriptions-item label="学生">{{ detail.request.studentUsername }}</el-descriptions-item>
        <el-descriptions-item label="学生说明">{{ detail.request.studentMessage || "未填写" }}</el-descriptions-item>
        <el-descriptions-item label="状态">{{ statusMap[detail.request.status] || detail.request.status }}</el-descriptions-item>
        <el-descriptions-item label="求职意向">{{ detail.resume?.targetPosition || "未填写" }}</el-descriptions-item>
        <el-descriptions-item label="教育经历">{{ detail.resume?.education || "未填写" }}</el-descriptions-item>
        <el-descriptions-item label="项目经历">{{ detail.resume?.experience || "未填写" }}</el-descriptions-item>
        <el-descriptions-item label="技能">{{ detail.resume?.skills || "未填写" }}</el-descriptions-item>
        <el-descriptions-item label="奖项证书">{{ detail.resume?.awards || "未填写" }}</el-descriptions-item>
        <el-descriptions-item label="自我评价">{{ detail.resume?.selfEvaluation || "未填写" }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="declineDialogVisible" title="拒绝审核请求" width="560px">
      <el-form label-width="90px">
        <el-form-item label="拒绝理由">
          <el-input v-model="declineForm.declineReason" type="textarea" :rows="3" placeholder="请说明无法接受该请求的原因" />
        </el-form-item>
        <el-form-item label="修改建议">
          <el-input v-model="declineForm.declineSuggestion" type="textarea" :rows="4" placeholder="请给学生一些后续建议" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="declineDialogVisible = false">取消</el-button>
        <el-button type="danger" @click="decline">确认拒绝</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="returnDialogVisible" title="返回新简历" width="760px">
      <el-form label-width="90px" :model="returnForm">
        <el-form-item label="简历名称">
          <el-input v-model="returnForm.title" />
        </el-form-item>
        <el-form-item label="姓名">
          <el-input v-model="returnForm.name" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="returnForm.phone" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="returnForm.email" />
        </el-form-item>
        <el-form-item label="求职意向">
          <el-input v-model="returnForm.targetPosition" />
        </el-form-item>
        <el-form-item label="教育经历">
          <el-input v-model="returnForm.education" type="textarea" :rows="4" />
        </el-form-item>
        <el-form-item label="项目经历">
          <el-input v-model="returnForm.experience" type="textarea" :rows="5" />
        </el-form-item>
        <el-form-item label="技能">
          <el-input v-model="returnForm.skills" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="奖项证书">
          <el-input v-model="returnForm.awards" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="自我评价">
          <el-input v-model="returnForm.selfEvaluation" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="审核意见">
          <el-input v-model="returnForm.comment" type="textarea" :rows="4" placeholder="请说明主要修改点和建议" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="returnDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="returnResume">确认返回</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="feedbackDialogVisible" title="批注与评分" width="820px">
      <el-form label-width="90px">
        <el-form-item label="批注字段">
          <el-select v-model="annotationForm.fieldName" placeholder="请选择字段">
            <el-option v-for="item in fieldOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="批注类型">
          <el-select v-model="annotationForm.markType">
            <el-option label="文本表达" value="TEXT" />
            <el-option label="结构优化" value="STRUCTURE" />
            <el-option label="关键词" value="KEYWORD" />
          </el-select>
        </el-form-item>
        <el-form-item label="批注内容">
          <el-input v-model="annotationForm.content" type="textarea" :rows="3" placeholder="请输入字段级修改建议" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="submitAnnotation">添加批注</el-button>
        </el-form-item>
        <el-form-item label="总分">
          <el-input-number v-model="scoreForm.score" :min="0" :max="100" />
        </el-form-item>
        <el-form-item label="评分说明">
          <el-input v-model="scoreForm.remark" type="textarea" :rows="3" placeholder="请输入评分说明" />
        </el-form-item>
        <el-form-item>
          <el-button type="success" @click="submitScore">保存评分</el-button>
        </el-form-item>
      </el-form>

      <el-divider />
      <el-table :data="feedbackAnnotations" border empty-text="暂无批注">
        <el-table-column prop="fieldName" label="字段" width="130" />
        <el-table-column prop="markType" label="类型" width="120" />
        <el-table-column prop="content" label="内容" />
        <el-table-column label="操作" width="100">
          <template #default="{ row }">
            <el-button type="danger" size="small" @click="removeAnnotation(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <template #footer>
        <el-button @click="feedbackDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from "vue"
import { ElMessage } from "element-plus"
import {
  acceptReviewRequestApi,
  createReviewAnnotationApi,
  deleteReviewAnnotationApi,
  declineReviewRequestApi,
  getReviewAnnotationsApi,
  getReviewRequestDetailApi,
  getReviewScoreApi,
  getTeacherReviewRequestsApi,
  returnReviewRequestApi,
  saveReviewScoreApi
} from "../../api/reviewRequest"

const requests = ref([])
const detailDialogVisible = ref(false)
const declineDialogVisible = ref(false)
const returnDialogVisible = ref(false)
const feedbackDialogVisible = ref(false)
const decliningRequest = ref(null)
const returningRequest = ref(null)
const feedbackRequest = ref(null)
const feedbackAnnotations = ref([])

const detail = reactive({
  request: null,
  resume: null
})

const declineForm = reactive({
  declineReason: "",
  declineSuggestion: ""
})

const returnForm = reactive({
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
  comment: ""
})

const annotationForm = reactive({
  fieldName: "experience",
  markType: "TEXT",
  content: ""
})

const scoreForm = reactive({
  score: 80,
  remark: ""
})

const fieldOptions = [
  { label: "简历名称", value: "title" },
  { label: "姓名", value: "name" },
  { label: "手机号", value: "phone" },
  { label: "邮箱", value: "email" },
  { label: "求职意向", value: "target_position" },
  { label: "教育经历", value: "education" },
  { label: "项目经历", value: "experience" },
  { label: "技能", value: "skills" },
  { label: "奖项证书", value: "awards" },
  { label: "自我评价", value: "self_evaluation" }
]

const statusMap = {
  PENDING: "待处理",
  ACCEPTED: "已接受",
  DECLINED: "已拒绝",
  WITHDRAW_PENDING: "撤回处理中",
  WITHDRAWN: "已撤回",
  CANCELLED: "已取消",
  COMPLETED: "已完成"
}

onMounted(loadRequests)

async function loadRequests() {
  requests.value = await getTeacherReviewRequestsApi()
}

async function openDetail(row) {
  const result = await getReviewRequestDetailApi(row.id)
  detail.request = result.request
  detail.resume = result.resume
  detailDialogVisible.value = true
}

async function accept(row) {
  await acceptReviewRequestApi(row.id, "")
  ElMessage.success("已接受审核请求")
  await loadRequests()
}

function openDecline(row) {
  decliningRequest.value = row
  declineForm.declineReason = ""
  declineForm.declineSuggestion = ""
  declineDialogVisible.value = true
}

async function decline() {
  if (!declineForm.declineReason || !declineForm.declineSuggestion) {
    ElMessage.warning("请填写拒绝理由和建议")
    return
  }
  await declineReviewRequestApi(decliningRequest.value.id, { ...declineForm })
  declineDialogVisible.value = false
  ElMessage.success("已拒绝审核请求")
  await loadRequests()
}

async function openReturn(row) {
  const result = await getReviewRequestDetailApi(row.id)
  returningRequest.value = row
  const resume = result.resume || {}
  returnForm.title = `${resume.title || "简历"} - 教师返回版本`
  returnForm.name = resume.name || ""
  returnForm.phone = resume.phone || ""
  returnForm.email = resume.email || ""
  returnForm.targetPosition = resume.targetPosition || ""
  returnForm.education = resume.education || ""
  returnForm.experience = resume.experience || ""
  returnForm.skills = resume.skills || ""
  returnForm.awards = resume.awards || ""
  returnForm.selfEvaluation = resume.selfEvaluation || ""
  returnForm.comment = ""
  returnDialogVisible.value = true
}

async function openFeedback(row) {
  feedbackRequest.value = row
  annotationForm.fieldName = "experience"
  annotationForm.markType = "TEXT"
  annotationForm.content = ""
  await loadFeedback(row.id)
  feedbackDialogVisible.value = true
}

async function loadFeedback(requestId) {
  feedbackAnnotations.value = await getReviewAnnotationsApi(requestId)
  const score = await getReviewScoreApi(requestId)
  scoreForm.score = score?.score ?? 80
  scoreForm.remark = score?.remark || ""
}

async function submitAnnotation() {
  if (!annotationForm.content) {
    ElMessage.warning("请填写批注内容")
    return
  }
  await createReviewAnnotationApi(feedbackRequest.value.id, { ...annotationForm })
  annotationForm.content = ""
  ElMessage.success("已添加批注")
  await loadFeedback(feedbackRequest.value.id)
}

async function removeAnnotation(row) {
  await deleteReviewAnnotationApi(row.id)
  ElMessage.success("已删除批注")
  await loadFeedback(feedbackRequest.value.id)
}

async function submitScore() {
  await saveReviewScoreApi(feedbackRequest.value.id, { ...scoreForm })
  ElMessage.success("已保存评分")
  await loadFeedback(feedbackRequest.value.id)
}

async function returnResume() {
  if (!returnForm.name) {
    ElMessage.warning("请填写姓名")
    return
  }
  if (!returnForm.comment) {
    ElMessage.warning("请填写审核意见")
    return
  }
  await returnReviewRequestApi(returningRequest.value.id, { ...returnForm })
  returnDialogVisible.value = false
  ElMessage.success("已返回新的简历版本")
  await loadRequests()
}

function getStatusType(status) {
  if (status === "ACCEPTED" || status === "COMPLETED") return "success"
  if (status === "DECLINED" || status === "CANCELLED" || status === "WITHDRAWN") return "danger"
  return "warning"
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
