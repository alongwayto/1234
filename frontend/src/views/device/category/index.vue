<template>
  <div class="page-container">
    <div class="card-header">
      <span class="title">设备分类管理</span>
      <el-button type="primary" @click="openDialog()"><el-icon><Plus /></el-icon> 新增分类</el-button>
    </div>
    <el-card shadow="never">
      <el-table :data="categories" v-loading="loading" row-key="id" border default-expand-all>
        <el-table-column prop="name" label="分类名称" />
        <el-table-column prop="code" label="分类编码" width="150" />
        <el-table-column prop="description" label="描述" show-overflow-tooltip />
        <el-table-column prop="sortOrder" label="排序" width="80" />
        <el-table-column label="操作" width="150">
          <template #default="{ row }">
            <el-button link type="primary" @click="openDialog(row)">编辑</el-button>
            <el-button link type="danger" @click="handleDelete(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑分类' : '新增分类'" width="500px">
      <el-form ref="formRef" :model="form" label-width="90px">
        <el-form-item label="父分类">
          <el-select v-model="form.parentId" clearable style="width:100%">
            <el-option label="顶级分类" :value="0" />
            <el-option v-for="c in topCategories" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="分类名称" :rules="[{required:true}]">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="分类编码" :rules="[{required:true}]">
          <el-input v-model="form.code" />
        </el-form-item>
        <el-form-item label="描述"><el-input v-model="form.description" /></el-form-item>
        <el-form-item label="排序"><el-input-number v-model="form.sortOrder" :min="0" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getCategories, createCategory, updateCategory, deleteCategory } from '@/api/device'

const loading = ref(false)
const categories = ref([])
const dialogVisible = ref(false)
const formRef = ref()
const form = reactive({ id: null, parentId: 0, name: '', code: '', description: '', sortOrder: 0 })

const topCategories = computed(() => categories.value.filter(c => c.parentId === 0))

onMounted(loadData)

async function loadData() {
  loading.value = true
  try {
    const res = await getCategories()
    // 构建树形结构
    const all = res.data
    const map = {}
    all.forEach(c => { map[c.id] = { ...c, children: [] } })
    const tree = []
    all.forEach(c => {
      if (c.parentId && map[c.parentId]) map[c.parentId].children.push(map[c.id])
      else tree.push(map[c.id])
    })
    categories.value = tree
  } finally { loading.value = false }
}

function openDialog(row = null) {
  Object.assign(form, { id: null, parentId: 0, name: '', code: '', description: '', sortOrder: 0 })
  if (row) Object.assign(form, row)
  dialogVisible.value = true
}

async function handleSave() {
  if (form.id) await updateCategory(form.id, form)
  else await createCategory(form)
  ElMessage.success('保存成功')
  dialogVisible.value = false
  loadData()
}

async function handleDelete(id) {
  await ElMessageBox.confirm('确定删除该分类？', '提示', { type: 'warning' })
  await deleteCategory(id)
  ElMessage.success('删除成功')
  loadData()
}
</script>
