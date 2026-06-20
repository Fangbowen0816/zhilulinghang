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
      <el-table-column prop="teacherComment" label="教师意见" />
      <el-table-column prop="updateTime" label="返回时间" width="190" />
      <el-table-column label="操作" width="120">
        <template #default="{ row }">
          <el-button type="primary" size="small" @click="router.push(`/student/resume/${row.id}`)">编辑</el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup>
import { onMounted, ref } from "vue"
import { useRoute, useRouter } from "vue-router"
import { getResumeVersionsApi } from "../../api/resume"

const route = useRoute()
const router = useRouter()
const versions = ref([])

onMounted(loadVersions)

async function loadVersions() {
  versions.value = await getResumeVersionsApi(route.params.id)
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
