<template>
  <div class="page-container">
    <el-card shadow="never">
      <div class="table-toolbar">
        <el-button v-if="canImport" type="primary" :icon="Upload" @click="openImport">导入历史工单</el-button>
        <el-button :icon="Refresh" @click="loadData">刷新</el-button>
      </div>

      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column prop="orderNo" label="工单号" width="180" />
        <el-table-column prop="assigneeName" label="维护员" width="100" />
        <el-table-column prop="assignerName" label="派单人" width="100" />
        <el-table-column prop="assignTime" label="派单时间" width="160" />
        <el-table-column prop="expectedTime" label="预计完成" width="160" />
        <el-table-column prop="actualEnd" label="完成时间" width="160" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="orderStatusType(row.status)">{{ orderStatusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="cost" label="维修费用" width="110">
          <template #default="{ row }">{{ row.cost ? '¥' + row.cost : '-' }}</template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" v-if="row.status === 0" @click="handleStart(row.id)">开始处理</el-button>
            <el-button link type="success" v-if="row.status === 1" @click="openComplete(row)">完成</el-button>
            <el-button link type="info" @click="viewDetail(row)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination v-model:current-page="query.pageNum" v-model:page-size="query.pageSize"
                     :total="total" layout="total, prev, pager, next" class="pagination" @change="loadData" />
    </el-card>

    <!-- 完成工单弹窗 -->
    <el-dialog v-model="completeVisible" title="完成工单" width="500px">
      <el-form :model="completeForm" label-width="90px">
        <el-form-item label="处理描述">
          <el-input v-model="completeForm.handleDesc" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="处理结果">
          <el-select v-model="completeForm.handleResult" style="width:100%">
            <el-option label="已修复" :value="1" />
            <el-option label="无法修复" :value="2" />
            <el-option label="需更换" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="维修费用">
          <el-input-number v-model="completeForm.cost" :min="0" :precision="2" style="width:100%" />
        </el-form-item>
        <el-form-item label="更换配件">
          <el-input v-model="completeForm.partsReplaced" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="completeVisible = false">取消</el-button>
        <el-button type="primary" @click="doComplete">提交</el-button>
      </template>
    </el-dialog>

    <!-- 历史工单导入弹窗 -->
    <el-dialog v-model="importVisible" title="导入历史工单" width="760px">
      <el-form ref="importFormRef" :model="importForm" :rules="importRules" label-width="100px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="工单号">
              <el-input v-model="importForm.orderNo" placeholder="留空自动生成" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="故障单号">
              <el-input v-model="importForm.faultNo" placeholder="留空自动生成" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="设备" prop="deviceId">
              <el-select v-model="importForm.deviceId" filterable style="width:100%" placeholder="选择设备">
                <el-option v-for="device in deviceOptions" :key="device.id" :label="device.deviceName" :value="device.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="故障类型">
              <el-input v-model="importForm.faultType" placeholder="如：硬件故障、网络故障" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="故障级别">
              <el-select v-model="importForm.faultLevel" style="width:100%">
                <el-option label="低" :value="1" />
                <el-option label="中" :value="2" />
                <el-option label="高" :value="3" />
                <el-option label="紧急" :value="4" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="维修费用">
              <el-input-number v-model="importForm.cost" :min="0" :precision="2" style="width:100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="上报人">
              <el-input v-model="importForm.reporterName" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="维护员">
              <el-input v-model="importForm.assigneeName" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="派单人">
              <el-input v-model="importForm.assignerName" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="上报时间">
              <el-date-picker v-model="importForm.reportTime" type="datetime" value-format="YYYY-MM-DDTHH:mm:ss" style="width:100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="派单时间">
              <el-date-picker v-model="importForm.assignTime" type="datetime" value-format="YYYY-MM-DDTHH:mm:ss" style="width:100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="预计完成">
              <el-date-picker v-model="importForm.expectedTime" type="datetime" value-format="YYYY-MM-DDTHH:mm:ss" style="width:100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="开始时间">
              <el-date-picker v-model="importForm.actualStart" type="datetime" value-format="YYYY-MM-DDTHH:mm:ss" style="width:100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="完成时间" prop="actualEnd">
              <el-date-picker v-model="importForm.actualEnd" type="datetime" value-format="YYYY-MM-DDTHH:mm:ss" style="width:100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="处理结果">
              <el-select v-model="importForm.handleResult" style="width:100%">
                <el-option label="已修复" :value="1" />
                <el-option label="无法修复" :value="2" />
                <el-option label="需更换" :value="3" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="故障描述">
              <el-input v-model="importForm.faultDesc" type="textarea" :rows="2" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="处理描述">
              <el-input v-model="importForm.handleDesc" type="textarea" :rows="2" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="更换配件">
              <el-input v-model="importForm.partsReplaced" placeholder="多个配件可用逗号分隔" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="importVisible = false">取消</el-button>
        <el-button type="primary" :loading="importSubmitting" @click="doImport">导入</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Refresh, Upload } from '@element-plus/icons-vue'
import dayjs from 'dayjs'
import { getWorkOrderPage, startHandle, completeHandle, importHistoricalWorkOrder } from '@/api/fault'
import { getDevicePage } from '@/api/device'
import { useUserStore } from '@/store/user'

const userStore = useUserStore()
const canImport = computed(() => userStore.roles.includes('ROLE_ADMIN'))
const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const completeVisible = ref(false)
const importVisible = ref(false)
const importSubmitting = ref(false)
const importFormRef = ref()
const deviceOptions = ref([])
const completeForm = reactive({ orderId: null, handleDesc: '', handleResult: 1, cost: 0, partsReplaced: '' })
const importForm = reactive(createImportForm())
const query = reactive({ pageNum: 1, pageSize: 10 })

const importRules = {
  deviceId: [{ required: true, message: '请选择设备', trigger: 'change' }],
  actualEnd: [{ required: true, message: '请选择完成时间', trigger: 'change' }],
}

onMounted(() => {
  loadData()
  loadDevices()
})

async function loadData() {
  loading.value = true
  try {
    const res = await getWorkOrderPage(query)
    tableData.value = res.data.records
    total.value = res.data.total
  } finally { loading.value = false }
}

async function loadDevices() {
  const res = await getDevicePage({ pageNum: 1, pageSize: 200 })
  deviceOptions.value = res.data.records || []
}

async function handleStart(id) {
  await startHandle(id)
  ElMessage.success('已开始处理')
  loadData()
}

function openComplete(row) {
  Object.assign(completeForm, { orderId: row.id, handleDesc: '', handleResult: 1, cost: 0, partsReplaced: '' })
  completeVisible.value = true
}

async function doComplete() {
  await completeHandle(completeForm)
  ElMessage.success('工单已完成')
  completeVisible.value = false
  loadData()
}

function openImport() {
  Object.assign(importForm, createImportForm())
  importVisible.value = true
  if (!deviceOptions.value.length) loadDevices()
}

async function doImport() {
  await importFormRef.value.validate()
  importSubmitting.value = true
  try {
    await importHistoricalWorkOrder(buildImportPayload())
    ElMessage.success('历史工单已导入')
    importVisible.value = false
    loadData()
  } finally {
    importSubmitting.value = false
  }
}

function buildImportPayload() {
  const payload = { ...importForm }
  ;['reportTime', 'assignTime', 'expectedTime', 'actualStart', 'actualEnd'].forEach((field) => {
    if (!payload[field]) payload[field] = null
  })
  return payload
}

function createImportForm() {
  const now = dayjs().format('YYYY-MM-DDTHH:mm:ss')
  return {
    faultNo: '',
    orderNo: '',
    deviceId: null,
    faultType: '',
    faultLevel: 2,
    faultDesc: '',
    reporterName: '',
    reportTime: now,
    assigneeName: '',
    assignerName: '',
    assignTime: now,
    expectedTime: '',
    actualStart: '',
    actualEnd: now,
    handleDesc: '',
    handleResult: 1,
    cost: 0,
    partsReplaced: '',
  }
}

function viewDetail(row) { /* 可扩展详情弹窗 */ }

const orderStatusLabel = (s) => ({ 0: '待处理', 1: '处理中', 2: '已完成', 3: '已关闭' }[s] || '-')
const orderStatusType = (s) => ({ 0: 'info', 1: 'warning', 2: 'success', 3: '' }[s] || '')
</script>

<style scoped>
.table-toolbar {
  display: flex;
  gap: 10px;
  margin-bottom: 14px;
}

.pagination { margin-top: 16px; justify-content: flex-end; }
</style>
