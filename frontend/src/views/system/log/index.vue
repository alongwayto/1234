<template>
  <div class="page-container">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span class="title">操作日志</span>
          <el-button type="danger" @click="handleClear">清空日志</el-button>
        </div>
      </template>
      <el-form :model="query" inline style="margin-bottom:16px">
        <el-form-item label="用户名">
          <el-input v-model="query.username" clearable style="width:140px" />
        </el-form-item>
        <el-form-item label="模块">
          <el-input v-model="query.module" clearable style="width:140px" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.status" clearable style="width:100px">
            <el-option label="成功" :value="1" /><el-option label="失败" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData">查询</el-button>
        </el-form-item>
      </el-form>
      <el-table :data="tableData" v-loading="loading" border stripe size="small">
        <el-table-column prop="username" label="操作用户" width="100" />
        <el-table-column prop="module" label="模块" width="100" />
        <el-table-column prop="operation" label="操作" width="120" />
        <el-table-column prop="requestMethod" label="方法" width="70" />
        <el-table-column prop="requestUrl" label="URL" min-width="200" show-overflow-tooltip />
        <el-table-column prop="ip" label="IP" width="130" />
        <el-table-column label="状态" width="70">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
              {{ row.status === 1 ? '成功' : '失败' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="costTime" label="耗时(ms)" width="90" />
        <el-table-column prop="createTime" label="操作时间" width="160" />
      </el-table>
      <el-pagination v-model:current-page="query.pageNum" v-model:page-size="query.pageSize"
                     :total="total" layout="total, prev, pager, next" class="pagination" @change="loadData" />
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getLogPage, clearLogs } from '@/api/system'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const query = reactive({ pageNum: 1, pageSize: 20, username: '', module: '', status: null })

onMounted(loadData)

async function loadData() {
  loading.value = true
  try {
    const res = await getLogPage(query)
    tableData.value = res.data.records
    total.value = res.data.total
  } finally { loading.value = false }
}

async function handleClear() {
  await ElMessageBox.confirm('确定清空所有操作日志？此操作不可恢复！', '警告', { type: 'warning' })
  await clearLogs()
  ElMessage.success('清空成功')
  loadData()
}
</script>

<style scoped>
.pagination { margin-top: 16px; justify-content: flex-end; }
</style>
