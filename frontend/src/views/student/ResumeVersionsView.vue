<template>
  <div class="page">
    <div class="page-header">
      <h2>教师返回版本</h2>
      <el-button @click="router.back()">返回</el-button>
    </div>

    <el-empty v-if="versions.length === 0" description="暂无教师返回版本" />

    <el-table v-else :data="versions" border>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="title" label="简历名称" />
      <el-table-column label="版本类型" width="140">
        <template #default="{ row }">{{ versionTypeMap[row.versionType] || row.versionType }}</template>
      </el-table-column>
      <el-table-column prop="teacherComment" label="教师意见" />
      <el-table-column prop="updateTime" label="返回时间" width="190" />
      <el-table-column label="操作" width="220">
        <template #default="{ row }">
          <el-button type="primary" size="small" @click="router.push(`/student/resume/${row.id}`)">编辑</el-button>
          <el-button size="small" @click="preview(row)">预览</el-button>
          <el-button size="small" @click="exportVersion(row)">导出</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="previewVisible" title="版本预览" width="860px">
      <iframe v-if="previewHtml" class="preview-frame" :srcdoc="previewHtml" title="版本预览"></iframe>
      <template #footer>
        <el-button @click="openPrintableHtml(previewHtml)">导出/打印</el-button>
        <el-button type="primary" @click="previewVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, ref } from "vue"
import { useRoute, useRouter } from "vue-router"
import { ElMessage } from "element-plus"
import { getResumeExportApi, getResumePreviewApi, getResumeVersionsApi } from "../../api/resume"

const route = useRoute()
const router = useRouter()
const versions = ref([])
const previewVisible = ref(false)
const previewHtml = ref("")

const versionTypeMap = {
  ORIGINAL: "原始版本",
  TEACHER_RETURNED: "教师返回版本"
}

onMounted(loadVersions)

async function loadVersions() {
  versions.value = await getResumeVersionsApi(route.params.id)
}

async function preview(row) {
  const result = await getResumePreviewApi(row.id)
  previewHtml.value = result.html || ""
  previewVisible.value = true
}

async function exportVersion(row) {
  const result = await getResumeExportApi(row.id)
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

.preview-frame {
  width: 100%;
  height: 640px;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
}
</style>
