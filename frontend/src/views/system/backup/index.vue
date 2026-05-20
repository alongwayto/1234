<template>
  <div class="page-container">
    <div class="card-header">
      <span class="title">数据备份</span>
      <el-button type="primary" :loading="backing" @click="handleManualBackup">
        <el-icon><Upload /></el-icon> 立即备份
      </el-button>
    </div>
    <el-card shadow="never">
      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column prop="fileName" label="备份文件名" min-width="220" show-overflow-tooltip />
        <el-table-column label="文件大小" width="120">
          <template #default="{ row }">{{ formatSize(row.fileSize) }}</template>
        </el-table-column>
        <el-table-column label="备份类型" width="100">
          <template #default="{ row }">
            <el-tag :type="row.backupType === 1 ? 'primary' : 'success'" size="small">
              {{ row.backupType === 1 ? '手动' : '自动' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
              {{ row.status === 1 ? '成功' : '失败' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="operator" label="操作人" width="100" />
        <el-table-column prop="remark" label="备注" min-width="150" show-overflow-tooltip />
        <el-table-column prop="createTime" label="备份时间" width="160" />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" :disabled="row.status !== 1" @click="handleDownload(row)">下载</el-button>
            <el-button link type="danger" :disabled="row.status !== 1" @click="handleRestore(row)">恢复</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination v-model:current-page="query.pageNum" v-model:page-size="query.pageSize"
                     :total="total" layout="total, prev, pager, next" class="pagination" @change="loadData" />
    </el-card>
    <el-alert type="info" :closable="false" style="margin-top:16px">
      系统每天凌晨 2:00 自动备份数据库。恢复会覆盖当前数据库状态，请先确认备份文件来源可靠。
    </el-alert>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getBackupList, manualBackup, downloadBackup, restoreBackup } from '@/api/system'

const loading = ref(false)
const backing = ref(false)
const tableData = ref([])
const total = ref(0)
const query = reactive({ pageNum: 1, pageSize: 10 })

onMounted(loadData)

async function loadData() {
  loading.value = true
  try {
    const res = await getBackupList(query)
    tableData.value = res.data.records
    total.value = res.data.total
  } finally { loading.value = false }
}

async function handleManualBackup() {
  backing.value = true
  try {
    await manualBackup()
    ElMessage.success('备份成功')
    loadData()
  } finally { backing.value = false }
}

async function handleDownload(row) {
  const res = await downloadBackup(row.id)
  const url = URL.createObjectURL(new Blob([res.data]))
  const a = document.createElement('a')
  a.href = url
  a.download = getDownloadName(res, row.fileName || 'backup.sql')
  a.click()
  URL.revokeObjectURL(url)
}

async function handleRestore(row) {
  await ElMessageBox.confirm(`确定恢复备份 ${row.fileName}？当前数据库状态会被备份内容覆盖。`, '恢复确认', {
    type: 'warning',
    confirmButtonText: '确认恢复',
  })
  await restoreBackup(row.id)
  ElMessage.success('恢复成功')
  loadData()
}

function getDownloadName(res, fallbackName) {
  const disposition = res.headers?.['content-disposition'] || ''
  const match = disposition.match(/filename\*?=(?:UTF-8''|")?([^";]+)/i)
  return match ? decodeURIComponent(match[1]) : fallbackName
}

function formatSize(bytes) {
  if (!bytes) return '-'
  if (bytes < 1024) return bytes + ' B'
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB'
  return (bytes / 1024 / 1024).toFixed(1) + ' MB'
}
</script>

<style scoped>
.pagination { margin-top: 16px; justify-content: flex-end; }
</style>
