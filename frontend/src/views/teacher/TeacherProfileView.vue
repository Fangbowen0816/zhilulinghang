<template>
  <div class="page">
    <div class="page-header">
      <div>
        <h2>个人资料</h2>
        <p>学生会依据这些资料选择审核教师，通过管理员审核后才能开启接收请求。</p>
      </div>
      <el-tag :type="statusType">{{ statusText }}</el-tag>
    </div>

    <el-alert
      v-if="profile.approvalComment"
      title="审核意见"
      :description="profile.approvalComment"
      type="warning"
      show-icon
      :closable="false"
      class="profile-alert"
    />

    <el-form label-width="100px" :model="form" class="profile-form">
      <el-form-item label="展示名称">
        <el-input v-model="form.displayName" placeholder="例如：王老师" />
      </el-form-item>
      <el-form-item label="院系">
        <el-input v-model="form.department" placeholder="例如：软件工程系" />
      </el-form-item>
      <el-form-item label="职称">
        <el-input v-model="form.title" placeholder="例如：讲师、副教授" />
      </el-form-item>
      <el-form-item label="擅长方向">
        <el-input v-model="form.expertiseTags" placeholder="例如：Java 后端, Vue 前端, 校招简历" />
      </el-form-item>
      <el-form-item label="个人简介">
        <el-input v-model="form.bio" type="textarea" :rows="5" placeholder="介绍你的指导方向和经验" />
      </el-form-item>
      <el-form-item label="接收请求">
        <el-switch
          v-model="form.available"
          :disabled="profile.approvalStatus !== 'APPROVED'"
          active-text="开启"
          inactive-text="关闭"
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="saveProfile">保存资料</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive } from "vue"
import { ElMessage } from "element-plus"
import { getMyTeacherProfileApi, updateMyTeacherProfileApi } from "../../api/teacher"

const profile = reactive({
  approvalStatus: "",
  approvalComment: ""
})

const form = reactive({
  displayName: "",
  department: "",
  title: "",
  bio: "",
  expertiseTags: "",
  available: false
})

const statusMap = {
  PENDING: "待管理员审核",
  APPROVED: "已通过",
  REJECTED: "已拒绝"
}

const statusText = computed(() => statusMap[profile.approvalStatus] || "未知")
const statusType = computed(() => {
  if (profile.approvalStatus === "APPROVED") return "success"
  if (profile.approvalStatus === "REJECTED") return "danger"
  return "warning"
})

onMounted(loadProfile)

async function loadProfile() {
  const result = await getMyTeacherProfileApi()
  Object.assign(profile, result)
  form.displayName = result.displayName || ""
  form.department = result.department || ""
  form.title = result.title || ""
  form.bio = result.bio || ""
  form.expertiseTags = result.expertiseTags || ""
  form.available = Boolean(result.available)
}

async function saveProfile() {
  if (!form.displayName) {
    ElMessage.warning("请填写展示名称")
    return
  }
  const result = await updateMyTeacherProfileApi(form)
  Object.assign(profile, result)
  form.available = Boolean(result.available)
  ElMessage.success("资料已保存")
}
</script>

<style scoped>
.page {
  max-width: 860px;
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

.profile-alert {
  margin-bottom: 18px;
}

.profile-form {
  max-width: 720px;
}
</style>
