<template>
  <div class="page">
    <div class="page-header">
      <h2>简历管理</h2>
      <el-button @click="loadResumes">刷新</el-button>
    </div>

    <div class="filters">
      <el-input v-model="keyword" placeholder="搜索简历名称/姓名" clearable />
      <el-select v-model="statusFilter" placeholder="状态" clearable>
        <el-option label="草稿" value="DRAFT" />
        <el-option label="待审核" value="SUBMITTED" />
        <el-option label="已通过" value="APPROVED" />
        <el-option label="已退回" value="REJECTED" />
      </el-select>
      <el-select v-model="versionFilter" placeholder="版本类型" clearable>
        <el-option label="原始简历" value="ORIGINAL" />
        <el-option label="教师返回" value="TEACHER_RETURNED" />
      </el-select>
    </div>

    <el-table :data="filteredResumes" border empty-text="暂无简历">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="studentId" label="学生ID" width="90" />
      <el-table-column prop="title" label="简历名称" />
      <el-table-column prop="name" label="姓名" width="120" />
      <el-table-column prop="versionType" label="版本类型" width="140" />
      <el-table-column prop="sourceResumeId" label="源简历" width="90" />
      <el-table-column prop="generatedByTeacherId" label="生成教师" width="100" />
      <el-table-column prop="status" label="状态" width="110" />
      <el-table-column label="冻结" width="80">
        <template #default="{ row }">{{ row.frozen ? "是" : "否" }}</template>
      </el-table-column>
      <el-table-column prop="updateTime" label="更新时间" width="190" />
    </el-table>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from "vue"
import { getAdminResumesApi } from "../../api/admin"

const resumes = ref([])
const keyword = ref("")
const statusFilter = ref("")
const versionFilter = ref("")

const filteredResumes = computed(() => {
  return resumes.value.filter(resume => {
    const text = `${resume.title || ""}${resume.name || ""}`
    const matchesKeyword = !keyword.value || text.includes(keyword.value)
    const matchesStatus = !statusFilter.value || resume.status === statusFilter.value
    const matchesVersion = !versionFilter.value || resume.versionType === versionFilter.value
    return matchesKeyword && matchesStatus && matchesVersion
  })
})

onMounted(loadResumes)

async function loadResumes() {
  resumes.value = await getAdminResumesApi()
}
</script>

<style scoped>
.page-header,
.filters {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 20px;
}

.page-header {
  justify-content: space-between;
}

.filters .el-input {
  width: 260px;
}

.filters .el-select {
  width: 160px;
}

h2 {
  margin: 0;
}
</style>
