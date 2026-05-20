<template>
  <div class="page-container">
    <div class="card-header">
      <span class="title">角色管理</span>
      <el-button type="primary" @click="openDialog()"><el-icon><Plus /></el-icon> 新增角色</el-button>
    </div>
    <el-card shadow="never">
      <el-table :data="roles" v-loading="loading" border stripe>
        <el-table-column prop="roleName" label="角色名称" width="150" />
        <el-table-column prop="roleCode" label="角色编码" width="150" />
        <el-table-column prop="description" label="描述" />
        <el-table-column label="操作" width="150">
          <template #default="{ row }">
            <el-button link type="primary" @click="openDialog(row)">编辑</el-button>
            <el-button link type="danger" @click="handleDelete(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="form.role.id ? '编辑角色' : '新增角色'" width="700px" destroy-on-close>
      <el-form :model="form.role" label-width="90px">
        <el-form-item label="角色名称" :rules="[{required:true}]">
          <el-input v-model="form.role.roleName" />
        </el-form-item>
        <el-form-item label="角色编码" :rules="[{required:true}]">
          <el-input v-model="form.role.roleCode" :disabled="!!form.role.id" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.role.description" />
        </el-form-item>
        <el-form-item label="权限配置">
          <el-tree ref="treeRef" :data="permTree" :props="{ label: 'permName', children: 'children' }"
                   show-checkbox node-key="id" :default-checked-keys="form.permIds"
                   @check="onTreeCheck" style="width:100%;max-height:300px;overflow-y:auto" />
        </el-form-item>
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
import { getRoles, getPermTree, getRolePerms, createRole, updateRole, deleteRole } from '@/api/system'

const loading = ref(false)
const roles = ref([])
const permTree = ref([])
const dialogVisible = ref(false)
const treeRef = ref()
const form = reactive({ role: { id: null, roleName: '', roleCode: '', description: '' }, permIds: [] })

onMounted(async () => {
  permTree.value = (await getPermTree()).data
  loadData()
})

async function loadData() {
  loading.value = true
  try { roles.value = (await getRoles()).data }
  finally { loading.value = false }
}

async function openDialog(row = null) {
  Object.assign(form.role, { id: null, roleName: '', roleCode: '', description: '' })
  form.permIds = []
  if (row) {
    Object.assign(form.role, row)
    form.permIds = (await getRolePerms(row.id)).data
  }
  dialogVisible.value = true
}

function onTreeCheck() {
  const checked = treeRef.value.getCheckedKeys()
  const halfChecked = treeRef.value.getHalfCheckedKeys()
  form.permIds = [...checked, ...halfChecked]
}

async function handleSave() {
  if (form.role.id) await updateRole(form.role.id, form)
  else await createRole(form)
  ElMessage.success('保存成功')
  dialogVisible.value = false
  loadData()
}

async function handleDelete(id) {
  await ElMessageBox.confirm('确定删除该角色？', '提示', { type: 'warning' })
  await deleteRole(id)
  ElMessage.success('删除成功')
  loadData()
}
</script>
