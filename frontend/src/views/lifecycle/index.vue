<template>
  <div class="lifecycle-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>设备生命周期管理</span>
          <div class="header-actions">
            <el-select v-model="queryParams.lifecycleType" placeholder="生命周期类型" clearable style="width: 150px">
              <el-option label="采购" value="purchase" />
              <el-option label="维修" value="maintenance" />
              <el-option label="更换" value="replacement" />
              <el-option label="巡检" value="inspection" />
              <el-option label="报废" value="retirement" />
            </el-select>
            <el-button type="primary" @click="handleQuery">
              <el-icon><Search /></el-icon>
              查询
            </el-button>
            <el-button type="primary" @click="handleAdd">
              <el-icon><Plus /></el-icon>
              添加记录
            </el-button>
          </div>
        </div>
      </template>

      <el-table :data="tableData" stripe v-loading="loading">
        <el-table-column type="index" label="序号" width="60" />
        <el-table-column prop="deviceName" label="设备名称" min-width="150" />
        <el-table-column prop="deviceCode" label="设备编号" width="120" />
        <el-table-column prop="lifecycleType" label="生命周期类型" width="100">
          <template #default="{ row }">
            <el-tag :type="getTypeTag(row.lifecycleType)">
              {{ getTypeText(row.lifecycleType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="eventDate" label="事件日期" width="120" />
        <el-table-column prop="description" label="事件描述" min-width="200" show-overflow-tooltip />
        <el-table-column prop="cost" label="费用" width="100" align="right">
          <template #default="{ row }">
            {{ row.cost ? `¥${row.cost.toFixed(2)}` : '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="vendor" label="供应商/维修商" width="150" show-overflow-tooltip />
        <el-table-column prop="operator" label="操作人" width="100" />
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleView(row)">查看</el-button>
            <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="pagination.current"
        v-model:page-size="pagination.size"
        :total="pagination.total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next"
        @size-change="handleQuery"
        @current-change="handleQuery"
        style="margin-top: 20px; justify-content: flex-end"
      />
    </el-card>

    <!-- 添加/编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px" @close="resetForm">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="120px">
        <el-form-item label="设备" prop="deviceId">
          <el-select v-model="form.deviceId" filterable placeholder="请选择设备" @change="onDeviceChange">
            <el-option
              v-for="device in deviceList"
              :key="device.id"
              :label="`${device.name} (${device.code})`"
              :value="device.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="生命周期类型" prop="lifecycleType">
          <el-select v-model="form.lifecycleType" placeholder="请选择类型">
            <el-option label="采购" value="purchase" />
            <el-option label="维修" value="maintenance" />
            <el-option label="更换" value="replacement" />
            <el-option label="巡检" value="inspection" />
            <el-option label="报废" value="retirement" />
          </el-select>
        </el-form-item>
        <el-form-item label="事件日期" prop="eventDate">
          <el-date-picker v-model="form.eventDate" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" />
        </el-form-item>
        <el-form-item label="事件描述" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="请输入事件描述" />
        </el-form-item>
        <el-form-item label="费用">
          <el-input-number v-model="form.cost" :precision="2" :min="0" :step="100" style="width: 100%" />
        </el-form-item>
        <el-form-item label="供应商/维修商">
          <el-input v-model="form.vendor" placeholder="请输入供应商或维修商" />
        </el-form-item>
        <el-form-item label="联系人">
          <el-input v-model="form.contactPerson" placeholder="请输入联系人" />
        </el-form-item>
        <el-form-item label="联系电话">
          <el-input v-model="form.contactPhone" placeholder="请输入联系电话" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="2" placeholder="备注信息" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm" :loading="submitting">确定</el-button>
      </template>
    </el-dialog>

    <!-- 查看详情对话框 -->
    <el-dialog v-model="viewVisible" title="记录详情" width="700px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="设备名称">{{ viewData.deviceName }}</el-descriptions-item>
        <el-descriptions-item label="设备编号">{{ viewData.deviceCode }}</el-descriptions-item>
        <el-descriptions-item label="生命周期类型">
          <el-tag :type="getTypeTag(viewData.lifecycleType)">
            {{ getTypeText(viewData.lifecycleType) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="事件日期">{{ viewData.eventDate }}</el-descriptions-item>
        <el-descriptions-item label="费用">{{ viewData.cost ? `¥${viewData.cost.toFixed(2)}` : '-' }}</el-descriptions-item>
        <el-descriptions-item label="供应商/维修商">{{ viewData.vendor || '-' }}</el-descriptions-item>
        <el-descriptions-item label="联系人">{{ viewData.contactPerson || '-' }}</el-descriptions-item>
        <el-descriptions-item label="联系电话">{{ viewData.contactPhone || '-' }}</el-descriptions-item>
        <el-descriptions-item label="操作人">{{ viewData.operator || '-' }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ viewData.createTime }}</el-descriptions-item>
        <el-descriptions-item label="事件描述" :span="2">{{ viewData.description }}</el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ viewData.remark || '-' }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Plus } from '@element-plus/icons-vue'

const loading = ref(false)
const tableData = ref([])
const deviceList = ref([])
const dialogVisible = ref(false)
const viewVisible = ref(false)
const dialogTitle = ref('')
const submitting = ref(false)
const formRef = ref(null)

const queryParams = reactive({
  lifecycleType: '',
  deviceId: null,
  startDate: '',
  endDate: ''
})

const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

const form = reactive({
  id: null,
  deviceId: null,
  lifecycleType: '',
  eventDate: '',
  description: '',
  cost: 0,
  vendor: '',
  contactPerson: '',
  contactPhone: '',
  remark: ''
})

const viewData = ref({})

const rules = {
  deviceId: [{ required: true, message: '请选择设备', trigger: 'change' }],
  lifecycleType: [{ required: true, message: '请选择生命周期类型', trigger: 'change' }],
  eventDate: [{ required: true, message: '请选择事件日期', trigger: 'change' }],
  description: [{ required: true, message: '请输入事件描述', trigger: 'blur' }]
}

const typeMap = {
  purchase: { text: '采购', type: 'success' },
  maintenance: { text: '维修', type: 'warning' },
  replacement: { text: '更换', type: 'primary' },
  inspection: { text: '巡检', type: 'info' },
  retirement: { text: '报废', type: 'danger' }
}

function getTypeText(type) {
  return typeMap[type]?.text || type
}

function getTypeTag(type) {
  return typeMap[type]?.type || 'info'
}

async function loadDevices() {
  // 模拟加载设备列表
  deviceList.value = [
    { id: 1, name: '核心交换机', code: 'DEV001' },
    { id: 2, name: '服务器集群', code: 'DEV002' },
    { id: 3, name: '监控摄像头', code: 'DEV003' }
  ]
}

async function loadData() {
  loading.value = true
  try {
    // 模拟数据
    tableData.value = [
      {
        id: 1,
        deviceId: 1,
        deviceName: '核心交换机',
        deviceCode: 'DEV001',
        lifecycleType: 'purchase',
        eventDate: '2023-01-15',
        description: '采购华为S5735S-L48T4S-A1核心交换机',
        cost: 15800.00,
        vendor: '华为官方授权经销商',
        operator: '系统管理员',
        createTime: '2023-01-15 10:30:00'
      },
      {
        id: 2,
        deviceId: 1,
        deviceName: '核心交换机',
        deviceCode: 'DEV001',
        lifecycleType: 'maintenance',
        eventDate: '2023-06-20',
        description: '例行维护，清理灰尘，更换散热风扇',
        cost: 800.00,
        vendor: '校园网络维护组',
        operator: '维护员张三',
        createTime: '2023-06-20 14:20:00'
      }
    ]
    pagination.total = 2
  } finally {
    loading.value = false
  }
}

function handleQuery() {
  pagination.current = 1
  loadData()
}

function handleAdd() {
  dialogTitle.value = '添加记录'
  dialogVisible.value = true
}

function handleEdit(row) {
  dialogTitle.value = '编辑记录'
  Object.assign(form, row)
  dialogVisible.value = true
}

function handleView(row) {
  viewData.value = { ...row }
  viewVisible.value = true
}

function onDeviceChange(deviceId) {
  const device = deviceList.value.find(d => d.id === deviceId)
  if (device) {
    form.deviceName = device.name
    form.deviceCode = device.code
  }
}

async function submitForm() {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      submitting.value = true
      try {
        // 实际调用API保存
        ElMessage.success('保存成功')
        dialogVisible.value = false
        loadData()
      } finally {
        submitting.value = false
      }
    }
  })
}

function resetForm() {
  formRef.value?.resetFields()
  Object.assign(form, {
    id: null,
    deviceId: null,
    lifecycleType: '',
    eventDate: '',
    description: '',
    cost: 0,
    vendor: '',
    contactPerson: '',
    contactPhone: '',
    remark: ''
  })
}

onMounted(() => {
  loadDevices()
  loadData()
})
</script>

<style scoped>
.lifecycle-container {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-actions {
  display: flex;
  gap: 10px;
}
</style>
