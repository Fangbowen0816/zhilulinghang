<template>
  <div class="page">
    <div class="page-header">
      <h2>岗位列表</h2>
      <el-button @click="loadJobs">刷新</el-button>
    </div>

    <div class="filters">
      <el-input v-model="filters.keyword" placeholder="搜索岗位/公司/要求" clearable @keyup.enter="loadJobs" />
      <el-select v-model="filters.industry" placeholder="行业" clearable>
        <el-option v-for="industry in industries" :key="industry" :label="industry" :value="industry" />
      </el-select>
      <el-select v-model="filters.city" placeholder="城市" clearable>
        <el-option v-for="city in cities" :key="city" :label="city" :value="city" />
      </el-select>
      <el-select v-model="filters.status" placeholder="状态" clearable>
        <el-option label="招聘中" value="OPEN" />
        <el-option label="已关闭" value="CLOSED" />
      </el-select>
      <el-button type="primary" @click="loadJobs">查询</el-button>
    </div>

    <el-table v-loading="loading" :data="jobs" border empty-text="暂无岗位">
      <el-table-column prop="title" label="岗位" min-width="180" />
      <el-table-column prop="company" label="公司" min-width="150" />
      <el-table-column prop="industry" label="行业" width="120" />
      <el-table-column prop="city" label="城市" width="100" />
      <el-table-column prop="salaryRange" label="薪资" width="130" />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 'OPEN' ? 'success' : 'info'">
            {{ statusMap[row.status] || row.status }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="110">
        <template #default="{ row }">
          <el-button type="primary" size="small" @click="openJob(row.id)">详情</el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from "vue"
import { useRouter } from "vue-router"
import { getJobsApi } from "../../api/job"

const router = useRouter()
const loading = ref(false)
const jobs = ref([])
const filters = reactive({
  keyword: "",
  industry: "",
  city: "",
  status: "OPEN"
})

const statusMap = {
  OPEN: "招聘中",
  CLOSED: "已关闭"
}

const industries = computed(() => uniqueValues("industry"))
const cities = computed(() => uniqueValues("city"))

onMounted(loadJobs)

async function loadJobs() {
  loading.value = true
  try {
    jobs.value = await getJobsApi({
      keyword: filters.keyword,
      industry: filters.industry,
      city: filters.city,
      status: filters.status
    })
  } finally {
    loading.value = false
  }
}

function openJob(id) {
  router.push(`/student/jobs/${id}`)
}

function uniqueValues(field) {
  return [...new Set(jobs.value.map(job => job[field]).filter(Boolean))]
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

.filters {
  flex-wrap: wrap;
}

.filters .el-input {
  width: 260px;
}

.filters .el-select {
  width: 150px;
}

h2 {
  margin: 0;
}
</style>
