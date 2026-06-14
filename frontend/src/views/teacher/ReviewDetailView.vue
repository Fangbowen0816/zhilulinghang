<template>
  <div class="page">
    <div class="page-header">
      <h2>审核简历</h2>
      <el-button @click="router.back()">返回</el-button>
    </div>

    <el-empty v-if="!resume" description="未找到简历" />

    <template v-else>
      <el-descriptions :column="1" border class="resume-detail">
        <el-descriptions-item label="简历名称">{{ resume.title }}</el-descriptions-item>
        <el-descriptions-item label="姓名">{{ resume.name }}</el-descriptions-item>
        <el-descriptions-item label="手机号">{{ resume.phone || "未填写" }}</el-descriptions-item>
        <el-descriptions-item label="邮箱">{{ resume.email || "未填写" }}</el-descriptions-item>
        <el-descriptions-item label="求职意向">{{ resume.targetPosition || "未填写" }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag>{{ resume.status }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="教育经历">{{ resume.education }}</el-descriptions-item>
        <el-descriptions-item label="项目经历">{{ resume.experience }}</el-descriptions-item>
        <el-descriptions-item label="技能">{{ resume.skills }}</el-descriptions-item>
        <el-descriptions-item label="奖项证书">{{ resume.awards || "未填写" }}</el-descriptions-item>
        <el-descriptions-item label="自我评价">{{ resume.selfEvaluation || "未填写" }}</el-descriptions-item>
      </el-descriptions>

      <el-form label-width="90px" class="review-form">
        <el-form-item label="审核意见">
          <el-input
            v-model="teacherComment"
            type="textarea"
            :rows="5"
            placeholder="请填写给学生的修改建议或通过说明"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="success" @click="submitReview('APPROVED')">审核通过</el-button>
          <el-button type="danger" @click="submitReview('REJECTED')">退回修改</el-button>
        </el-form-item>
      </el-form>
    </template>
  </div>
</template>

<script setup>
import { onMounted, ref } from "vue"
import { useRoute, useRouter } from "vue-router"
import { ElMessage } from "element-plus"
import { getReviewDetailApi, submitReviewApi } from "../../api/review"

const route = useRoute()
const router = useRouter()
const resume = ref(null)
const teacherComment = ref("")

onMounted(loadDetail)

async function loadDetail() {
  resume.value = await getReviewDetailApi(route.params.id)
  teacherComment.value = resume.value.teacherComment || ""
}

async function submitReview(status) {
  if (!teacherComment.value) {
    ElMessage.warning("请先填写审核意见")
    return
  }
  resume.value = await submitReviewApi(route.params.id, {
    status,
    teacherComment: teacherComment.value
  })
  ElMessage.success(status === "APPROVED" ? "已审核通过" : "已退回修改")
  router.push("/teacher/review")
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

h2 {
  margin: 0;
}

.resume-detail {
  margin-bottom: 24px;
}

.review-form {
  max-width: 760px;
}
</style>
