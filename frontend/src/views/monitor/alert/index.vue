<template>
  <div class="page-container">
    <el-card shadow="never" class="search-card">
      <el-form :model="query" inline>
        <el-form-item label="预警级别">
          <el-select v-model="query.alertLevel" clearable style="width:120px">
            <el-option label="低" :value="1" /><el-option label="中" :value="2" />
            <el-option label="高" :value="3" /><el-option label="紧急" :value="4" />
          </el-select>
        </el-form-item>
        <el-form-item label="处理状态">
          <el-select v-model="query.status" clearable style="width:120px">
            <el-option label="未处理" :value="0" /><el-option label="已处理" :value="1" /><el-option label="已忽略" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData">查询</el-button>
          <el-button @click="() => { query.alertLevel = null; query.status = null; loadData() }">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
    <el-card shadow="never" style="margin-top:16px">
      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column prop="deviceName" label="设备名称" min-width="160" />
        <el-table-column prop="alertType" label="预警类型" width="100" />
        <el-table-column label="预警级别" width="90">
          <template #default="{ row }">
            <el-tag :type="levelType(row.alertLevel)">{{ levelLabel(row.alertLevel) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="alertMsg" label="预警信息" min-width="200" show-overflow-tooltip />
        <el-table-column prop="alertValue" label="触发值" width="100" />
        <el-table-column prop="threshold" label="阈值" width="100" />
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.status === 0 ? 'danger' : row.status === 1 ? 'success' : 'info'">
              {{ ['未处理','已处理','已忽略'][row.status] }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="预警时间" width="160" />
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <template v-if="row.status === 0">
              <el-button link type="primary" @click="openHandle(row, 1)">处理</el-button>
              <el-button link type="info" @click="openHandle(row, 2)">忽略</el-button>
            </template>
            <span v-else style="color:#909399">{{ row.handler }}</span>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination v-model:current-page="query.pageNum" v-model:page-size="query.pageSize"
                     :total="total" layout="total, prev, pager, next" class="pagination" @change="loadData" />
    </el-card>

    <el-dialog v-model="handleVisible" title="处理预警" width="400px">
      <el-form :model="handleForm" label-width="80px">
        <el-form-item label="处理备注">
          <el-input v-model="handleForm.remark" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="handleVisible = false">取消</el-button>
        <el-button type="primary" @click="doHandle">确认</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getAlertPage, handleAlert } from '@/api/monitor'
import { useUserStore } from '@/store/user'

const userStore = useUserStore()
const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const handleVisible = ref(false)
const handleForm = reactive({ alertId: null, status: 1, remark: '' })
const query = reactive({ pageNum: 1, pageSize: 10, alertLevel: null, status: null })

onMounted(loadData)

async function loadData() {
  loading.value = true
  try {
    const res = await getAlertPage(query)
    tableData.value = res.data.records
    total.value = res.data.total
  } finally { loading.value = false }
}

function openHandle(row, status) {
  handleForm.alertId = row.id
  handleForm.status = status
  handleForm.remark = ''
  handleVisible.value = true
}

async function doHandle() {
  await handleAlert(handleForm.alertId, { handler: userStore.username, remark: handleForm.remark, status: handleForm.status })
  ElMessage.success('操作成功')
  handleVisible.value = false
  loadData()
}

const levelLabel = (l) => ({ 1: '低', 2: '中', 3: '高', 4: '紧急' }[l] || '-')
const levelType = (l) => ({ 1: 'info', 2: 'warning', 3: 'danger', 4: 'danger' }[l] || '')
</script>

<style scoped>
.search-card { margin-bottom: 0; }
.pagination { margin-top: 16px; justify-content: flex-end; }
</style>
