<template>
  <div class="page-container">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span class="title">故障归档</span>
        </div>
      </template>
      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column prop="faultNo" label="故障单号" width="180" />
        <el-table-column prop="deviceName" label="设备名称" min-width="160" show-overflow-tooltip />
        <el-table-column prop="faultType" label="故障类型" width="120" />
        <el-table-column prop="faultDesc" label="故障描述" min-width="200" show-overflow-tooltip />
        <el-table-column prop="reporterName" label="上报人" width="100" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 4 ? '' : 'success'">{{ row.status === 4 ? '已归档' : '已完成' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="上报时间" width="160" />
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" v-if="row.status === 3" @click="handleArchive(row.id)">归档</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination v-model:current-page="query.pageNum" v-model:page-size="query.pageSize"
                     :total="total" layout="total, prev, pager, next" class="pagination" @change="loadData" />
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getFaultPage, archiveFault } from '@/api/fault'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const query = reactive({ pageNum: 1, pageSize: 10 })

onMounted(loadData)

async function loadData() {
  loading.value = true
  try {
    // 查询已完成和已归档的故障
    const res = await getFaultPage({ ...query })
    tableData.value = res.data.records.filter(r => r.status >= 3)
    total.value = res.data.total
  } finally { loading.value = false }
}

async function handleArchive(id) {
  await ElMessageBox.confirm('确定归档该故障记录？', '提示', { type: 'warning' })
  await archiveFault(id)
  ElMessage.success('归档成功')
  loadData()
}
</script>

<style scoped>
.pagination { margin-top: 16px; justify-content: flex-end; }
</style>
