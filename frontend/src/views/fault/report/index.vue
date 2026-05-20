<template>
  <div class="page-container">
    <div class="card-header">
      <span class="title">故障上报</span>
      <el-button type="primary" @click="openDialog()"><el-icon><Plus /></el-icon> 上报故障</el-button>
    </div>
    <el-card shadow="never">
      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column prop="faultNo" label="故障单号" width="180" />
        <el-table-column prop="deviceName" label="设备名称" min-width="160" show-overflow-tooltip />
        <el-table-column prop="faultType" label="故障类型" width="120" />
        <el-table-column label="故障级别" width="90">
          <template #default="{ row }">
            <el-tag :type="levelType(row.faultLevel)">{{ levelLabel(row.faultLevel) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="faultDesc" label="故障描述" min-width="200" show-overflow-tooltip />
        <el-table-column prop="reporterName" label="上报人" width="100" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="faultStatusType(row.status)">{{ faultStatusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="上报时间" width="160" />
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="viewDetail(row)">详情</el-button>
            <el-button link type="warning" v-if="row.status === 0 && isAdmin" @click="openAssign(row)">派单</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination v-model:current-page="query.pageNum" v-model:page-size="query.pageSize"
                     :total="total" layout="total, prev, pager, next" class="pagination" @change="loadData" />
    </el-card>

    <!-- 上报故障弹窗 -->
    <el-dialog v-model="dialogVisible" title="上报故障" width="600px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="设备" prop="deviceId">
          <el-select v-model="form.deviceId" filterable style="width:100%" placeholder="请选择设备">
            <el-option v-for="d in devices" :key="d.id" :label="`${d.deviceCode} - ${d.deviceName}`" :value="d.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="故障类型">
          <el-select v-model="form.faultType" style="width:100%">
            <el-option label="硬件故障" value="硬件故障" /><el-option label="软件故障" value="软件故障" />
            <el-option label="网络故障" value="网络故障" /><el-option label="显示异常" value="显示异常" />
            <el-option label="电源故障" value="电源故障" /><el-option label="其他" value="其他" />
          </el-select>
        </el-form-item>
        <el-form-item label="故障级别">
          <el-radio-group v-model="form.faultLevel">
            <el-radio :label="1">低</el-radio><el-radio :label="2">中</el-radio>
            <el-radio :label="3">高</el-radio><el-radio :label="4">紧急</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="故障描述" prop="faultDesc">
          <el-input v-model="form.faultDesc" type="textarea" :rows="4" placeholder="请详细描述故障现象" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSubmit">提交</el-button>
      </template>
    </el-dialog>

    <!-- 派单弹窗 -->
    <el-dialog v-model="assignVisible" title="派单" width="500px">
      <el-form :model="assignForm" label-width="90px">
        <el-form-item label="维护员">
          <el-select v-model="assignForm.assigneeId" style="width:100%">
            <el-option v-for="u in maintainers" :key="u.id" :label="u.realName || u.username" :value="u.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="预计完成">
          <el-date-picker v-model="assignForm.expectedTime" type="datetime" style="width:100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="assignVisible = false">取消</el-button>
        <el-button type="primary" @click="doAssign">确认派单</el-button>
      </template>
    </el-dialog>

    <!-- 详情弹窗 -->
    <el-dialog v-model="detailVisible" title="故障详情" width="600px">
      <el-descriptions :column="2" border v-if="currentFault">
        <el-descriptions-item label="故障单号">{{ currentFault.faultNo }}</el-descriptions-item>
        <el-descriptions-item label="设备名称">{{ currentFault.deviceName }}</el-descriptions-item>
        <el-descriptions-item label="故障类型">{{ currentFault.faultType }}</el-descriptions-item>
        <el-descriptions-item label="故障级别">{{ levelLabel(currentFault.faultLevel) }}</el-descriptions-item>
        <el-descriptions-item label="上报人">{{ currentFault.reporterName }}</el-descriptions-item>
        <el-descriptions-item label="状态">{{ faultStatusLabel(currentFault.status) }}</el-descriptions-item>
        <el-descriptions-item label="故障描述" :span="2">{{ currentFault.faultDesc }}</el-descriptions-item>
        <el-descriptions-item label="AI诊断建议" :span="2" v-if="currentFault.aiDiagnosis">
          <div style="white-space:pre-wrap">{{ currentFault.aiDiagnosis }}</div>
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getFaultPage, createFault, assignWorkOrder } from '@/api/fault'
import { getDevicePage } from '@/api/device'
import { getUserPage } from '@/api/system'
import { useUserStore } from '@/store/user'

const userStore = useUserStore()
const isAdmin = computed(() => userStore.roles.includes('ROLE_ADMIN'))
const loading = ref(false)
const saving = ref(false)
const tableData = ref([])
const total = ref(0)
const dialogVisible = ref(false)
const assignVisible = ref(false)
const detailVisible = ref(false)
const currentFault = ref(null)
const devices = ref([])
const maintainers = ref([])
const formRef = ref()

const query = reactive({ pageNum: 1, pageSize: 10 })
const form = reactive({ deviceId: null, faultType: '硬件故障', faultLevel: 2, faultDesc: '' })
const assignForm = reactive({ faultId: null, assigneeId: null, expectedTime: null })
const rules = {
  deviceId: [{ required: true, message: '请选择设备' }],
  faultDesc: [{ required: true, message: '请填写故障描述' }],
}

onMounted(async () => {
  const [devRes, userRes] = await Promise.all([
    getDevicePage({ pageNum: 1, pageSize: 100 }),
    getUserPage({ pageNum: 1, pageSize: 100 })
  ])
  devices.value = devRes.data.records
  maintainers.value = userRes.data.records.filter(u => u.roles?.includes('ROLE_MAINTAINER'))
  loadData()
})

async function loadData() {
  loading.value = true
  try {
    const res = await getFaultPage(query)
    tableData.value = res.data.records
    total.value = res.data.total
  } finally { loading.value = false }
}

function openDialog() {
  Object.assign(form, { deviceId: null, faultType: '硬件故障', faultLevel: 2, faultDesc: '' })
  dialogVisible.value = true
}

async function handleSubmit() {
  await formRef.value.validate()
  saving.value = true
  try {
    await createFault(form)
    ElMessage.success('故障上报成功')
    dialogVisible.value = false
    loadData()
  } finally { saving.value = false }
}

function openAssign(row) {
  assignForm.faultId = row.id
  assignForm.assigneeId = null
  assignForm.expectedTime = null
  assignVisible.value = true
}

async function doAssign() {
  await assignWorkOrder(assignForm)
  ElMessage.success('派单成功')
  assignVisible.value = false
  loadData()
}

function viewDetail(row) { currentFault.value = row; detailVisible.value = true }

const levelLabel = (l) => ({ 1: '低', 2: '中', 3: '高', 4: '紧急' }[l] || '-')
const levelType = (l) => ({ 1: 'info', 2: 'warning', 3: 'danger', 4: 'danger' }[l] || '')
const faultStatusLabel = (s) => ({ 0: '待派单', 1: '已派单', 2: '处理中', 3: '已完成', 4: '已归档' }[s] || '-')
const faultStatusType = (s) => ({ 0: 'info', 1: 'warning', 2: 'primary', 3: 'success', 4: '' }[s] || '')
</script>

<style scoped>
.pagination { margin-top: 16px; justify-content: flex-end; }
</style>
