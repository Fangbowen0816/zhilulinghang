<template>
  <div class="page">
    <div class="page-header">
      <h2>待审核简历</h2>
      <el-button @click="loadList">刷新</el-button>
    </div>

    <el-table :data="resumes" border empty-text="暂无待审核简历">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="title" label="简历名称" width="180" />
      <el-table-column prop="name" label="学生姓名" width="140" />
      <el-table-column prop="targetPosition" label="求职意向" width="180" />
      <el-table-column prop="education" label="教育经历" />
      <el-table-column prop="skills" label="技能" />
      <el-table-column prop="updateTime" label="提交时间" width="190" />
      <el-table-column label="操作" width="120">
        <template #default="{ row }">
          <el-button type="primary" size="small" @click="goDetail(row.id)">审核</el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup>
import { onMounted, ref } from "vue"
import { useRouter } from "vue-router"
import { getReviewListApi } from "../../api/review"

const router = useRouter()
const resumes = ref([])

onMounted(loadList)

async function loadList() {
  resumes.value = await getReviewListApi()
}

function goDetail(id) {
  router.push(`/teacher/review/${id}`)
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
