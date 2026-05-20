<template>
  <div class="page-container">
    <div class="card-header">
      <span class="title">位置管理</span>
      <el-button type="primary" @click="openDialog()"><el-icon><Plus /></el-icon> 新增位置</el-button>
    </div>
    <el-card shadow="never">
      <el-table :data="locations" v-loading="loading" border stripe>
        <el-table-column prop="building" label="楼栋" width="150" />
        <el-table-column prop="floor" label="楼层" width="100" />
        <el-table-column prop="room" label="房间/区域" width="160" />
        <el-table-column prop="fullAddress" label="完整地址" show-overflow-tooltip />
        <el-table-column label="操作" width="150">
          <template #default="{ row }">
            <el-button link type="primary" @click="openDialog(row)">编辑</el-button>
            <el-button link type="danger" @click="handleDelete(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑位置' : '新增位置'" width="500px">
      <el-form ref="formRef" :model="form" label-width="90px">
        <el-form-item label="楼栋" :rules="[{required:true}]"><el-input v-model="form.building" /></el-form-item>
        <el-form-item label="楼层"><el-input v-model="form.floor" /></el-form-item>
        <el-form-item label="房间/区域"><el-input v-model="form.room" /></el-form-item>
        <el-form-item label="完整地址"><el-input v-model="form.fullAddress" /></el-form-item>
        <el-form-item label="备注"><el-input v-model="form.remark" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getLocations, createLocation, updateLocation, deleteLocation } from '@/api/device'

const loading = ref(false)
const locations = ref([])
const dialogVisible = ref(false)
const form = reactive({ id: null, building: '', floor: '', room: '', fullAddress: '', remark: '' })

onMounted(loadData)

async function loadData() {
  loading.value = true
  try { locations.value = (await getLocations()).data }
  finally { loading.value = false }
}

function openDialog(row = null) {
  Object.assign(form, { id: null, building: '', floor: '', room: '', fullAddress: '', remark: '' })
  if (row) Object.assign(form, row)
  dialogVisible.value = true
}

async function handleSave() {
  if (form.id) await updateLocation(form.id, form)
  else await createLocation(form)
  ElMessage.success('保存成功')
  dialogVisible.value = false
  loadData()
}

async function handleDelete(id) {
  await ElMessageBox.confirm('确定删除该位置？', '提示', { type: 'warning' })
  await deleteLocation(id)
  ElMessage.success('删除成功')
  loadData()
}
</script>
