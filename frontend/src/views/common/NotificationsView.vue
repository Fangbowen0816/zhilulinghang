<template>
  <div class="page">
    <div class="page-header">
      <h2>站内通知</h2>
      <div>
        <el-button @click="markAllRead">全部已读</el-button>
        <el-button @click="loadNotifications">刷新</el-button>
      </div>
    </div>

    <el-table :data="notifications" border empty-text="暂无通知">
      <el-table-column label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="row.readStatus === 'UNREAD' ? 'warning' : 'info'">
            {{ row.readStatus === "UNREAD" ? "未读" : "已读" }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="title" label="标题" width="180" />
      <el-table-column prop="content" label="内容" />
      <el-table-column prop="type" label="类型" width="170" />
      <el-table-column prop="createTime" label="时间" width="190" />
      <el-table-column label="操作" width="120">
        <template #default="{ row }">
          <el-button
            size="small"
            :disabled="row.readStatus === 'READ'"
            @click="markRead(row)"
          >
            标为已读
          </el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup>
import { onMounted, ref } from "vue"
import { ElMessage } from "element-plus"
import {
  getNotificationsApi,
  markAllNotificationsReadApi,
  markNotificationReadApi
} from "../../api/notification"

const notifications = ref([])

onMounted(loadNotifications)

async function loadNotifications() {
  notifications.value = await getNotificationsApi()
}

async function markRead(row) {
  await markNotificationReadApi(row.id)
  ElMessage.success("已标记为已读")
  await loadNotifications()
}

async function markAllRead() {
  await markAllNotificationsReadApi()
  ElMessage.success("已全部标记为已读")
  await loadNotifications()
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
