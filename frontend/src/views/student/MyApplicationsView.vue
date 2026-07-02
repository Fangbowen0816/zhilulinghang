<template>
  <div class="page">
    <div class="page-header">
      <h2>投递记录</h2>
      <el-button @click="loadApplications">刷新</el-button>
    </div>

    <div class="filters">
      <el-input v-model="keyword" placeholder="搜索岗位/公司/简历" clearable />
      <el-select v-model="statusFilter" placeholder="状态" clearable>
        <el-option v-for="(label, value) in statusMap" :key="value" :label="label" :value="value" />
      </el-select>
    </div>

    <el-table v-loading="loading" :data="filteredApplications" border empty-text="暂无投递记录">
      <el-table-column prop="jobTitle" label="岗位" min-width="180" />
      <el-table-column prop="company" label="公司" min-width="140" />
      <el-table-column prop="city" label="城市" width="100" />
      <el-table-column prop="resumeTitle" label="使用简历" min-width="170" />
      <el-table-column label="状态" width="150">
        <template #default="{ row }">
          <el-select :model-value="row.status" size="small" @change="value => changeStatus(row, value)">
            <el-option v-for="(label, value) in statusMap" :key="value" :label="label" :value="value" />
          </el-select>
        </template>
      </el-table-column>
      <el-table-column prop="applyTime" label="投递时间" width="170">
        <template #default="{ row }">{{ formatTime(row.applyTime) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="120">
        <template #default="{ row }">
          <el-button type="primary" size="small" @click="openDetail(row)">管理进度</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-drawer v-model="detailVisible" :title="selectedApplication?.jobTitle || '投递详情'" size="680px">
      <template v-if="selectedApplication">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="公司">{{ selectedApplication.company }}</el-descriptions-item>
          <el-descriptions-item label="使用简历">{{ selectedApplication.resumeTitle }}</el-descriptions-item>
          <el-descriptions-item label="当前状态">
            <el-tag>{{ statusMap[selectedApplication.status] || selectedApplication.status }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="投递时间">{{ formatTime(selectedApplication.applyTime) }}</el-descriptions-item>
        </el-descriptions>

        <section class="drawer-section">
          <div class="section-header">
            <h3>状态时间线</h3>
          </div>
          <el-timeline>
            <el-timeline-item :timestamp="formatTime(selectedApplication.applyTime)" type="primary">
              已投递
            </el-timeline-item>
            <el-timeline-item :timestamp="formatTime(selectedApplication.updateTime)" type="success">
              当前状态：{{ statusMap[selectedApplication.status] || selectedApplication.status }}
            </el-timeline-item>
          </el-timeline>
        </section>

        <section class="drawer-section">
          <div class="section-header">
            <h3>经验记录</h3>
            <el-button type="primary" size="small" @click="openExperienceDialog()">新增记录</el-button>
          </div>
          <el-empty v-if="experiences.length === 0" description="暂无经验记录" />
          <div v-else class="item-list">
            <div v-for="item in experiences" :key="item.id" class="list-item">
              <div>
                <el-tag size="small">{{ stageMap[item.stage] || item.stage }}</el-tag>
                <p>{{ item.content }}</p>
                <span>{{ formatTime(item.updateTime) }}</span>
              </div>
              <div class="item-actions">
                <el-button size="small" @click="openExperienceDialog(item)">编辑</el-button>
                <el-button type="danger" size="small" @click="deleteExperience(item)">删除</el-button>
              </div>
            </div>
          </div>
        </section>

        <section class="drawer-section">
          <div class="section-header">
            <h3>提醒事项</h3>
            <el-button type="primary" size="small" @click="openReminderDialog">新增提醒</el-button>
          </div>
          <el-empty v-if="reminders.length === 0" description="暂无提醒事项" />
          <div v-else class="item-list">
            <div v-for="item in reminders" :key="item.id" class="list-item">
              <div>
                <el-tag :type="item.status === 'DONE' ? 'success' : 'warning'" size="small">
                  {{ reminderStatusMap[item.status] || item.status }}
                </el-tag>
                <strong>{{ reminderTypeMap[item.remindType] || item.remindType }}</strong>
                <p>{{ item.content }}</p>
                <span>{{ formatTime(item.remindTime) }}</span>
              </div>
              <div class="item-actions">
                <el-button
                  type="success"
                  size="small"
                  :disabled="item.status === 'DONE'"
                  @click="markReminderDone(item)"
                >
                  完成
                </el-button>
                <el-button type="danger" size="small" @click="deleteReminder(item)">删除</el-button>
              </div>
            </div>
          </div>
        </section>
      </template>
    </el-drawer>

    <el-dialog v-model="experienceDialogVisible" :title="editingExperience ? '编辑经验记录' : '新增经验记录'" width="520px">
      <el-form label-width="84px">
        <el-form-item label="阶段">
          <el-select v-model="experienceForm.stage" class="full-width">
            <el-option v-for="(label, value) in stageMap" :key="value" :label="label" :value="value" />
          </el-select>
        </el-form-item>
        <el-form-item label="内容">
          <el-input v-model="experienceForm.content" type="textarea" :rows="4" placeholder="记录笔试、面试或准备心得" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="experienceDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveExperience">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="reminderDialogVisible" title="新增提醒" width="520px">
      <el-form label-width="84px">
        <el-form-item label="类型">
          <el-select v-model="reminderForm.remindType" class="full-width">
            <el-option v-for="(label, value) in reminderTypeMap" :key="value" :label="label" :value="value" />
          </el-select>
        </el-form-item>
        <el-form-item label="时间">
          <el-date-picker
            v-model="reminderForm.remindTime"
            type="datetime"
            value-format="YYYY-MM-DDTHH:mm:ss"
            placeholder="选择提醒时间"
            class="full-width"
          />
        </el-form-item>
        <el-form-item label="内容">
          <el-input v-model="reminderForm.content" placeholder="例如：准备一轮面试项目介绍" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="reminderDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveReminder">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from "vue"
import { ElMessage, ElMessageBox } from "element-plus"
import {
  createApplicationExperienceApi,
  createApplicationReminderApi,
  deleteApplicationExperienceApi,
  deleteApplicationReminderApi,
  getApplicationExperiencesApi,
  getApplicationRemindersApi,
  getMyApplicationsApi,
  markApplicationReminderDoneApi,
  updateApplicationExperienceApi,
  updateApplicationStatusApi
} from "../../api/application"

const loading = ref(false)
const applications = ref([])
const keyword = ref("")
const statusFilter = ref("")
const detailVisible = ref(false)
const selectedApplication = ref(null)
const experiences = ref([])
const reminders = ref([])
const experienceDialogVisible = ref(false)
const reminderDialogVisible = ref(false)
const editingExperience = ref(null)

const experienceForm = reactive({
  stage: "GENERAL",
  content: ""
})

const reminderForm = reactive({
  remindType: "FOLLOW_UP",
  remindTime: "",
  content: ""
})

const statusMap = {
  APPLIED: "已投递",
  SCREENING: "筛选中",
  WRITTEN_TEST: "笔试",
  INTERVIEW: "面试",
  OFFER: "Offer",
  REJECTED: "已拒绝",
  CLOSED: "已结束"
}

const stageMap = {
  GENERAL: "通用",
  WRITTEN_TEST: "笔试",
  INTERVIEW: "面试",
  OFFER: "Offer"
}

const reminderTypeMap = {
  FOLLOW_UP: "跟进",
  WRITTEN_TEST: "笔试",
  INTERVIEW: "面试",
  GENERAL: "通用"
}

const reminderStatusMap = {
  PENDING: "待完成",
  DONE: "已完成",
  CANCELLED: "已取消"
}

const filteredApplications = computed(() => {
  return applications.value.filter(application => {
    const text = `${application.jobTitle || ""}${application.company || ""}${application.resumeTitle || ""}`
    const matchesKeyword = !keyword.value || text.includes(keyword.value)
    const matchesStatus = !statusFilter.value || application.status === statusFilter.value
    return matchesKeyword && matchesStatus
  })
})

onMounted(loadApplications)

async function loadApplications() {
  loading.value = true
  try {
    applications.value = await getMyApplicationsApi()
  } finally {
    loading.value = false
  }
}

async function changeStatus(row, status) {
  await updateApplicationStatusApi(row.id, status)
  ElMessage.success("投递状态已更新")
  await loadApplications()
  if (selectedApplication.value?.id === row.id) {
    selectedApplication.value = applications.value.find(item => item.id === row.id)
  }
}

async function openDetail(row) {
  selectedApplication.value = row
  detailVisible.value = true
  await loadProgress(row.id)
}

async function loadProgress(applicationId) {
  const [experienceData, reminderData] = await Promise.all([
    getApplicationExperiencesApi(applicationId),
    getApplicationRemindersApi(applicationId)
  ])
  experiences.value = experienceData
  reminders.value = reminderData
}

function openExperienceDialog(item = null) {
  editingExperience.value = item
  experienceForm.stage = item?.stage || "GENERAL"
  experienceForm.content = item?.content || ""
  experienceDialogVisible.value = true
}

async function saveExperience() {
  if (!experienceForm.content.trim()) {
    ElMessage.warning("请填写经验记录内容")
    return
  }
  const payload = {
    stage: experienceForm.stage,
    content: experienceForm.content
  }
  if (editingExperience.value) {
    await updateApplicationExperienceApi(editingExperience.value.id, payload)
    ElMessage.success("经验记录已更新")
  } else {
    await createApplicationExperienceApi(selectedApplication.value.id, payload)
    ElMessage.success("经验记录已新增")
  }
  experienceDialogVisible.value = false
  await loadProgress(selectedApplication.value.id)
}

async function deleteExperience(item) {
  await ElMessageBox.confirm("确认删除这条经验记录吗？", "删除经验记录", {
    type: "warning",
    confirmButtonText: "确认删除",
    cancelButtonText: "取消"
  })
  await deleteApplicationExperienceApi(item.id)
  ElMessage.success("经验记录已删除")
  await loadProgress(selectedApplication.value.id)
}

function openReminderDialog() {
  reminderForm.remindType = "FOLLOW_UP"
  reminderForm.remindTime = ""
  reminderForm.content = ""
  reminderDialogVisible.value = true
}

async function saveReminder() {
  if (!reminderForm.remindTime) {
    ElMessage.warning("请选择提醒时间")
    return
  }
  if (!reminderForm.content.trim()) {
    ElMessage.warning("请填写提醒内容")
    return
  }
  await createApplicationReminderApi(selectedApplication.value.id, {
    remindType: reminderForm.remindType,
    remindTime: reminderForm.remindTime,
    content: reminderForm.content
  })
  ElMessage.success("提醒已新增")
  reminderDialogVisible.value = false
  await loadProgress(selectedApplication.value.id)
}

async function markReminderDone(item) {
  await markApplicationReminderDoneApi(item.id)
  ElMessage.success("提醒已完成")
  await loadProgress(selectedApplication.value.id)
}

async function deleteReminder(item) {
  await ElMessageBox.confirm("确认删除这条提醒吗？", "删除提醒", {
    type: "warning",
    confirmButtonText: "确认删除",
    cancelButtonText: "取消"
  })
  await deleteApplicationReminderApi(item.id)
  ElMessage.success("提醒已删除")
  await loadProgress(selectedApplication.value.id)
}

function formatTime(value) {
  if (!value) return "暂无"
  return String(value).replace("T", " ").slice(0, 16)
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
  width: 150px;
}

.drawer-section {
  margin-top: 22px;
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}

.section-header h3 {
  margin: 0;
}

.item-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.list-item {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  padding: 14px;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  background: #fff;
}

.list-item p {
  margin: 8px 0;
  color: #344054;
  line-height: 1.6;
}

.list-item span {
  color: #667085;
  font-size: 13px;
}

.item-actions {
  display: flex;
  flex-shrink: 0;
  align-items: flex-start;
  gap: 8px;
}

.full-width {
  width: 100%;
}

h2 {
  margin: 0;
}
</style>
