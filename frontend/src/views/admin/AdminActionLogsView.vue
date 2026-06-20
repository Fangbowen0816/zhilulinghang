<template>
  <div class="page">
    <div class="page-header">
      <h2>操作日志</h2>
      <el-button @click="loadLogs">刷新</el-button>
    </div>

    <div class="filters">
      <el-input v-model="keyword" placeholder="搜索管理员/动作/对象/详情" clearable />
    </div>

    <el-table :data="filteredLogs" border empty-text="暂无操作日志">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="adminUsername" label="管理员" width="140" />
      <el-table-column prop="action" label="动作" width="180" />
      <el-table-column prop="targetType" label="对象类型" width="150" />
      <el-table-column prop="targetId" label="对象ID" width="100" />
      <el-table-column prop="detail" label="详情" min-width="240" show-overflow-tooltip />
      <el-table-column prop="createTime" label="时间" width="190" />
    </el-table>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from "vue"
import { getAdminActionLogsApi } from "../../api/admin"

const logs = ref([])
const keyword = ref("")

const filteredLogs = computed(() => {
  return logs.value.filter(log => {
    const text = `${log.adminUsername || ""}${log.action || ""}${log.targetType || ""}${log.targetId || ""}${log.detail || ""}`
    return !keyword.value || text.includes(keyword.value)
  })
})

onMounted(loadLogs)

async function loadLogs() {
  logs.value = await getAdminActionLogsApi()
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
  width: 320px;
}

h2 {
  margin: 0;
}
</style>
