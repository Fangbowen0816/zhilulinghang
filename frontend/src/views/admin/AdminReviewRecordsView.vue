<template>
  <div class="page">
    <div class="page-header">
      <h2>审核记录管理</h2>
      <div>
        <el-button @click="exportCsv">导出CSV</el-button>
        <el-button @click="loadRecords">刷新</el-button>
      </div>
    </div>

    <div class="filters">
      <el-input v-model="keyword" placeholder="搜索教师/简历" clearable />
      <el-select v-model="hiddenFilter" placeholder="教师端隐藏" clearable>
        <el-option label="已隐藏" :value="true" />
        <el-option label="未隐藏" :value="false" />
      </el-select>
    </div>

    <el-table :data="filteredRecords" border empty-text="暂无审核记录">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="requestId" label="请求ID" width="90" />
      <el-table-column prop="sourceResumeTitle" label="源简历" />
      <el-table-column prop="returnedResumeTitle" label="返回简历" />
      <el-table-column prop="teacherDisplayName" label="教师" width="150" />
      <el-table-column prop="action" label="动作" width="110" />
      <el-table-column prop="comment" label="审核意见" />
      <el-table-column label="教师端隐藏" width="120">
        <template #default="{ row }">{{ row.teacherDeleted ? "是" : "否" }}</template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="190" />
    </el-table>

    <h2 class="section-title">批注监管</h2>
    <el-table :data="filteredAnnotations" border empty-text="暂无批注">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="requestId" label="请求ID" width="90" />
      <el-table-column prop="resumeTitle" label="简历" />
      <el-table-column prop="teacherDisplayName" label="教师" width="150" />
      <el-table-column prop="studentUsername" label="学生" width="130" />
      <el-table-column prop="fieldName" label="字段" width="130" />
      <el-table-column prop="markType" label="类型" width="120" />
      <el-table-column prop="content" label="批注内容" />
      <el-table-column prop="updateTime" label="更新时间" width="190" />
    </el-table>

    <h2 class="section-title">评分监管</h2>
    <el-table :data="filteredScores" border empty-text="暂无评分">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="requestId" label="请求ID" width="90" />
      <el-table-column prop="resumeTitle" label="简历" />
      <el-table-column prop="teacherDisplayName" label="教师" width="150" />
      <el-table-column prop="studentUsername" label="学生" width="130" />
      <el-table-column prop="score" label="分数" width="90" />
      <el-table-column prop="remark" label="评分说明" />
      <el-table-column prop="updateTime" label="更新时间" width="190" />
    </el-table>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from "vue"
import {
  getAdminResumeAnnotationsApi,
  getAdminResumeScoresApi,
  getAdminReviewRecordsApi
} from "../../api/admin"

const records = ref([])
const annotations = ref([])
const scores = ref([])
const keyword = ref("")
const hiddenFilter = ref("")

const filteredRecords = computed(() => {
  return records.value.filter(record => {
    const text = `${record.sourceResumeTitle || ""}${record.returnedResumeTitle || ""}${record.teacherDisplayName || ""}`
    const matchesKeyword = !keyword.value || text.includes(keyword.value)
    const matchesHidden = hiddenFilter.value === "" || record.teacherDeleted === hiddenFilter.value
    return matchesKeyword && matchesHidden
  })
})

const filteredAnnotations = computed(() => {
  return annotations.value.filter(annotation => {
    const text = `${annotation.resumeTitle || ""}${annotation.teacherDisplayName || ""}${annotation.studentUsername || ""}${annotation.fieldName || ""}${annotation.content || ""}`
    return !keyword.value || text.includes(keyword.value)
  })
})

const filteredScores = computed(() => {
  return scores.value.filter(score => {
    const text = `${score.resumeTitle || ""}${score.teacherDisplayName || ""}${score.studentUsername || ""}${score.remark || ""}`
    return !keyword.value || text.includes(keyword.value)
  })
})

onMounted(loadRecords)

async function loadRecords() {
  records.value = await getAdminReviewRecordsApi()
  annotations.value = await getAdminResumeAnnotationsApi()
  scores.value = await getAdminResumeScoresApi()
}

function exportCsv() {
  const header = ["ID", "请求ID", "源简历", "返回简历", "教师", "动作", "审核意见", "教师端隐藏", "创建时间"]
  const rows = filteredRecords.value.map(record => [
    record.id,
    record.requestId,
    record.sourceResumeTitle,
    record.returnedResumeTitle,
    record.teacherDisplayName,
    record.action,
    record.comment,
    record.teacherDeleted ? "是" : "否",
    record.createTime
  ])
  const csv = [header, ...rows]
    .map(row => row.map(value => `"${String(value ?? "").replaceAll('"', '""')}"`).join(","))
    .join("\n")
  const blob = new Blob(["\uFEFF" + csv], { type: "text/csv;charset=utf-8;" })
  const url = URL.createObjectURL(blob)
  const link = document.createElement("a")
  link.href = url
  link.download = "review-records.csv"
  link.click()
  URL.revokeObjectURL(url)
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

.section-title {
  margin: 24px 0 16px;
}
</style>
