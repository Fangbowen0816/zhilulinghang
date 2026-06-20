<template>
  <div class="page">
    <div class="page-header">
      <h2>平台设置</h2>
      <div>
        <el-button @click="loadSettings">刷新</el-button>
        <el-button type="primary" @click="openCreateDialog">新增设置</el-button>
      </div>
    </div>

    <el-table :data="settings" border empty-text="暂无平台设置">
      <el-table-column prop="settingKey" label="键名" width="220" />
      <el-table-column prop="settingValue" label="值" min-width="220" show-overflow-tooltip />
      <el-table-column prop="description" label="说明" min-width="260" show-overflow-tooltip />
      <el-table-column prop="updateTime" label="更新时间" width="190" />
      <el-table-column label="操作" width="100">
        <template #default="{ row }">
          <el-button type="primary" size="small" @click="openEditDialog(row)">编辑</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="editingSetting ? '编辑设置' : '新增设置'" width="560px">
      <el-form label-width="84px">
        <el-form-item label="键名">
          <el-input
            v-model="form.settingKey"
            :disabled="Boolean(editingSetting)"
            placeholder="例如 resume_polish_enabled"
          />
        </el-form-item>
        <el-form-item label="值">
          <el-input v-model="form.settingValue" type="textarea" :rows="3" placeholder="请输入设置值" />
        </el-form-item>
        <el-form-item label="说明">
          <el-input v-model="form.description" placeholder="请输入设置说明" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveSetting">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from "vue"
import { ElMessage } from "element-plus"
import { createAdminSettingApi, getAdminSettingsApi, updateAdminSettingApi } from "../../api/admin"

const settings = ref([])
const dialogVisible = ref(false)
const editingSetting = ref(null)
const form = reactive({
  settingKey: "",
  settingValue: "",
  description: ""
})

onMounted(loadSettings)

async function loadSettings() {
  settings.value = await getAdminSettingsApi()
}

function openCreateDialog() {
  editingSetting.value = null
  form.settingKey = ""
  form.settingValue = ""
  form.description = ""
  dialogVisible.value = true
}

function openEditDialog(row) {
  editingSetting.value = row
  form.settingKey = row.settingKey
  form.settingValue = row.settingValue || ""
  form.description = row.description || ""
  dialogVisible.value = true
}

async function saveSetting() {
  if (!form.settingKey.trim()) {
    ElMessage.warning("请输入设置键名")
    return
  }
  const payload = {
    settingKey: form.settingKey,
    settingValue: form.settingValue,
    description: form.description
  }
  if (editingSetting.value) {
    await updateAdminSettingApi(editingSetting.value.id, payload)
    ElMessage.success("设置已更新")
  } else {
    await createAdminSettingApi(payload)
    ElMessage.success("设置已新增")
  }
  dialogVisible.value = false
  await loadSettings()
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
