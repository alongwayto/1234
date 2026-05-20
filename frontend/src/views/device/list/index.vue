<template>
  <div class="page-container">
    <!-- 搜索栏 -->
    <el-card shadow="never" class="search-card">
      <el-form :model="query" inline>
        <el-form-item label="设备编号">
          <el-input v-model="query.deviceCode" placeholder="请输入" clearable style="width:160px" />
        </el-form-item>
        <el-form-item label="设备名称">
          <el-input v-model="query.deviceName" placeholder="请输入" clearable style="width:160px" />
        </el-form-item>
        <el-form-item label="分类">
          <el-select v-model="query.categoryId" placeholder="全部" clearable style="width:140px">
            <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.status" placeholder="全部" clearable style="width:120px">
            <el-option label="正常" :value="1" />
            <el-option label="维修中" :value="2" />
            <el-option label="停用" :value="0" />
            <el-option label="报废" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="在线状态">
          <el-select v-model="query.onlineStatus" placeholder="全部" clearable style="width:100px">
            <el-option label="在线" :value="1" />
            <el-option label="离线" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 操作栏 -->
    <div class="card-header" style="margin-top:16px">
      <span class="title">设备列表</span>
      <div>
        <el-button type="primary" @click="openDialog()"><el-icon><Plus /></el-icon> 新增设备</el-button>
        <el-button @click="handleDownloadTemplate"><el-icon><Document /></el-icon> 模板</el-button>
        <el-button @click="handleImport"><el-icon><Upload /></el-icon> 导入</el-button>
        <el-button @click="handleExport"><el-icon><Download /></el-icon> 导出</el-button>
        <el-button type="danger" :disabled="!selectedIds.length" @click="handleBatchDelete">
          <el-icon><Delete /></el-icon> 批量删除
        </el-button>
      </div>
    </div>

    <!-- 表格 -->
    <el-card shadow="never">
      <el-table :data="tableData" v-loading="loading" @selection-change="selectedIds = $event.map(r => r.id)" border stripe>
        <el-table-column type="selection" width="50" />
        <el-table-column prop="deviceCode" label="设备编号" width="140" />
        <el-table-column prop="deviceName" label="设备名称" min-width="160" show-overflow-tooltip />
        <el-table-column prop="categoryName" label="分类" width="120" />
        <el-table-column prop="brand" label="品牌" width="100" />
        <el-table-column prop="model" label="型号" width="120" show-overflow-tooltip />
        <el-table-column prop="locationName" label="位置" min-width="160" show-overflow-tooltip />
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)">{{ statusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="在线" width="80">
          <template #default="{ row }">
            <el-badge :is-dot="true" :type="row.onlineStatus === 1 ? 'success' : 'info'" />
            {{ row.onlineStatus === 1 ? '在线' : '离线' }}
          </template>
        </el-table-column>
        <el-table-column prop="responsiblePerson" label="负责人" width="100" />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="$router.push(`/device/detail/${row.id}`)">详情</el-button>
            <el-button link type="primary" @click="openDialog(row)">编辑</el-button>
            <el-button link type="danger" @click="handleDelete(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination
        v-model:current-page="query.pageNum" v-model:page-size="query.pageSize"
        :total="total" :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next" class="pagination"
        @change="loadData" />
    </el-card>

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="editForm.id ? '编辑设备' : '新增设备'" width="700px" destroy-on-close>
      <el-form ref="editFormRef" :model="editForm" :rules="editRules" label-width="100px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="设备编号" prop="deviceCode">
              <el-input v-model="editForm.deviceCode" :disabled="!!editForm.id" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="设备名称" prop="deviceName">
              <el-input v-model="editForm.deviceName" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="设备分类" prop="categoryId">
              <el-select v-model="editForm.categoryId" style="width:100%">
                <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="位置">
              <el-select v-model="editForm.locationId" clearable style="width:100%">
                <el-option v-for="l in locations" :key="l.id" :label="l.fullAddress" :value="l.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="品牌"><el-input v-model="editForm.brand" /></el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="型号"><el-input v-model="editForm.model" /></el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="序列号"><el-input v-model="editForm.serialNumber" /></el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="购买日期">
              <el-date-picker v-model="editForm.purchaseDate" type="date" value-format="YYYY-MM-DD" style="width:100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="购买价格">
              <el-input-number v-model="editForm.purchasePrice" :min="0" :precision="2" style="width:100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="保修到期">
              <el-date-picker v-model="editForm.warrantyDate" type="date" value-format="YYYY-MM-DD" style="width:100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="负责人"><el-input v-model="editForm.responsiblePerson" /></el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="负责人电话"><el-input v-model="editForm.responsiblePhone" /></el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态">
              <el-select v-model="editForm.status" style="width:100%">
                <el-option label="正常" :value="1" />
                <el-option label="停用" :value="0" />
                <el-option label="维修中" :value="2" />
                <el-option label="报废" :value="3" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="标签"><el-input v-model="editForm.tags" placeholder="逗号分隔" /></el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="描述"><el-input v-model="editForm.description" type="textarea" :rows="2" /></el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>

    <!-- 导入弹窗 -->
    <el-dialog v-model="importVisible" title="导入设备" width="400px">
      <el-upload drag :auto-upload="false" :on-change="onFileChange" accept=".xlsx,.xls" :limit="1">
        <el-icon size="40"><UploadFilled /></el-icon>
        <div>拖拽或点击上传 Excel 文件</div>
        <template #tip><div style="color:#909399;font-size:12px">支持 .xlsx .xls 格式</div></template>
      </el-upload>
      <template #footer>
        <el-button @click="importVisible = false">取消</el-button>
        <el-button type="primary" :loading="importing" @click="doImport">开始导入</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getDevicePage, createDevice, updateDevice, deleteDevice, batchDeleteDevices,
         importDevices, downloadImportTemplate, exportDevices, getCategories, getLocations } from '@/api/device'

const loading = ref(false)
const saving = ref(false)
const importing = ref(false)
const tableData = ref([])
const total = ref(0)
const selectedIds = ref([])
const dialogVisible = ref(false)
const importVisible = ref(false)
const importFile = ref(null)
const categories = ref([])
const locations = ref([])
const editFormRef = ref()

const query = reactive({ pageNum: 1, pageSize: 10, deviceCode: '', deviceName: '', categoryId: null, status: null, onlineStatus: null })
const editForm = reactive({ id: null, deviceCode: '', deviceName: '', categoryId: null, locationId: null,
  brand: '', model: '', serialNumber: '', purchaseDate: null, purchasePrice: null, warrantyDate: null,
  status: 1, tags: '', description: '', responsiblePerson: '', responsiblePhone: '' })
const editRules = {
  deviceCode: [{ required: true, message: '请输入设备编号' }],
  deviceName: [{ required: true, message: '请输入设备名称' }],
  categoryId: [{ required: true, message: '请选择分类' }],
}

onMounted(async () => {
  const [catRes, locRes] = await Promise.all([getCategories(), getLocations()])
  categories.value = catRes.data
  locations.value = locRes.data
  loadData()
})

async function loadData() {
  loading.value = true
  try {
    const res = await getDevicePage(query)
    tableData.value = res.data.records
    total.value = res.data.total
  } finally { loading.value = false }
}

function resetQuery() {
  Object.assign(query, { pageNum: 1, deviceCode: '', deviceName: '', categoryId: null, status: null, onlineStatus: null })
  loadData()
}

function openDialog(row = null) {
  Object.assign(editForm, { id: null, deviceCode: '', deviceName: '', categoryId: null, locationId: null,
    brand: '', model: '', serialNumber: '', purchaseDate: null, purchasePrice: null, warrantyDate: null,
    status: 1, tags: '', description: '', responsiblePerson: '', responsiblePhone: '' })
  if (row) Object.assign(editForm, row)
  dialogVisible.value = true
}

async function handleSave() {
  await editFormRef.value.validate()
  saving.value = true
  try {
    if (editForm.id) {
      await updateDevice(editForm.id, editForm)
    } else {
      await createDevice(editForm)
    }
    ElMessage.success('保存成功')
    dialogVisible.value = false
    loadData()
  } finally { saving.value = false }
}

async function handleDelete(id) {
  await ElMessageBox.confirm('确定删除该设备？', '提示', { type: 'warning' })
  await deleteDevice(id)
  ElMessage.success('删除成功')
  loadData()
}

async function handleBatchDelete() {
  await ElMessageBox.confirm(`确定删除选中的 ${selectedIds.value.length} 台设备？`, '提示', { type: 'warning' })
  await batchDeleteDevices(selectedIds.value)
  ElMessage.success('删除成功')
  loadData()
}

function handleImport() { importVisible.value = true; importFile.value = null }
function onFileChange(file) { importFile.value = file.raw }
async function doImport() {
  if (!importFile.value) return ElMessage.warning('请选择文件')
  importing.value = true
  try {
    const fd = new FormData()
    fd.append('file', importFile.value)
    await importDevices(fd)
    ElMessage.success('导入成功')
    importVisible.value = false
    loadData()
  } finally { importing.value = false }
}

async function handleExport() {
  const res = await exportDevices({ deviceCode: query.deviceCode, deviceName: query.deviceName,
    categoryId: query.categoryId, status: query.status })
  downloadBlob(res, '设备列表.xlsx')
}

async function handleDownloadTemplate() {
  const res = await downloadImportTemplate()
  downloadBlob(res, '设备导入模板.xlsx')
}

function downloadBlob(res, fallbackName) {
  const url = URL.createObjectURL(new Blob([res.data]))
  const a = document.createElement('a')
  a.href = url
  a.download = getDownloadName(res, fallbackName)
  a.click()
  URL.revokeObjectURL(url)
}

function getDownloadName(res, fallbackName) {
  const disposition = res.headers?.['content-disposition'] || ''
  const match = disposition.match(/filename\*?=(?:UTF-8''|")?([^";]+)/i)
  return match ? decodeURIComponent(match[1]) : fallbackName
}

const statusLabel = (s) => ({ 0: '停用', 1: '正常', 2: '维修中', 3: '报废' }[s] || '-')
const statusType = (s) => ({ 0: 'info', 1: 'success', 2: 'warning', 3: 'danger' }[s] || '')
</script>

<style scoped>
.search-card { margin-bottom: 0; }
.pagination { margin-top: 16px; justify-content: flex-end; }
</style>
