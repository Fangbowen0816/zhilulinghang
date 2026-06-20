<template>
  <div class="page">
    <div class="page-header">
      <h2>审核历史</h2>
      <el-button @click="loadRecords">刷新</el-button>
    </div>

    <el-table :data="records" border empty-text="暂无审核记录">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="sourceResumeTitle" label="源简历" />
      <el-table-column prop="returnedResumeTitle" label="返回版本" />
      <el-table-column prop="comment" label="审核意见" />
      <el-table-column prop="createTime" label="时间" width="190" />
      <el-table-column label="操作" width="120">
        <template #default="{ row }">
          <el-button type="danger" size="small" @click="hideRecord(row)">删除记录</el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup>
import { onMounted, ref } from "vue"
import { ElMessage, ElMessageBox } from "element-plus"
import { getTeacherReviewRecordsApi, hideReviewRecordApi } from "../../api/reviewRecord"

const records = ref([])

onMounted(loadRecords)

async function loadRecords() {
  records.value = await getTeacherReviewRecordsApi()
}

async function hideRecord(row) {
  await ElMessageBox.confirm("删除后仅在教师端隐藏，管理员仍可查看服务器记录。确认删除吗？", "删除审核记录", {
    type: "warning",
    confirmButtonText: "确认删除",
    cancelButtonText: "取消"
  })
  await hideReviewRecordApi(row.id)
  ElMessage.success("已从教师端隐藏该记录")
  await loadRecords()
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
