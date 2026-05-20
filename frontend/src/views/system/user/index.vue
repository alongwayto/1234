<template>
  <div class="page-container">
    <div class="card-header">
      <span class="title">用户管理</span>
      <div class="header-actions">
        <el-button plain @click="openDoc('deployment', '部署文件')">
          <el-icon><Document /></el-icon>
          部署文件
        </el-button>
        <el-button plain @click="openDoc('user-guide', '用户手册')">
          <el-icon><Reading /></el-icon>
          用户手册
        </el-button>
        <el-button type="primary" @click="openDialog()"><el-icon><Plus /></el-icon> 新增用户</el-button>
      </div>
    </div>
    <el-card shadow="never">
      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column prop="username" label="用户名" width="120" />
        <el-table-column prop="realName" label="姓名" width="100" />
        <el-table-column prop="email" label="邮箱" min-width="160" />
        <el-table-column prop="phone" label="手机号" width="130" />
        <el-table-column prop="department" label="部门" width="130" />
        <el-table-column label="角色" width="130">
          <template #default="{ row }">
            <el-tag v-for="r in row.roles" :key="r" size="small" style="margin:2px">{{ roleLabel(r) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-switch :model-value="row.status === 1" @change="toggleStatus(row)" />
          </template>
        </el-table-column>
        <el-table-column prop="lastLogin" label="最后登录" width="160" />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openDialog(row)">编辑</el-button>
            <el-button link type="warning" @click="openReset(row)">重置密码</el-button>
            <el-button link type="danger" @click="handleDelete(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination v-model:current-page="query.pageNum" v-model:page-size="query.pageSize"
                     :total="total" layout="total, prev, pager, next" class="pagination" @change="loadData" />
    </el-card>

    <el-dialog v-model="dialogVisible" :title="form.user.id ? '编辑用户' : '新增用户'" width="600px" destroy-on-close>
      <el-form ref="formRef" :model="form.user" label-width="90px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="用户名" :rules="[{required:true}]">
              <el-input v-model="form.user.username" :disabled="!!form.user.id" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="密码" :rules="form.user.id ? [] : [{required:true}]">
              <el-input v-model="form.user.password" type="password" :placeholder="form.user.id ? '不填则不修改' : '请输入密码'" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="姓名"><el-input v-model="form.user.realName" /></el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="邮箱"><el-input v-model="form.user.email" /></el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="手机号"><el-input v-model="form.user.phone" /></el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="部门"><el-input v-model="form.user.department" /></el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="角色">
              <el-checkbox-group v-model="form.roleIds">
                <el-checkbox v-for="r in roles" :key="r.id" :label="r.id">{{ r.roleName }}</el-checkbox>
              </el-checkbox-group>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="resetVisible" title="重置密码" width="400px">
      <el-form label-width="90px">
        <el-form-item label="新密码">
          <el-input v-model="resetPassword" type="password" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="resetVisible = false">取消</el-button>
        <el-button type="primary" @click="doReset">确认重置</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getUserPage,
  createUser,
  updateUser,
  deleteUser,
  resetPassword as resetPwd,
  toggleUserStatus,
  getRoles,
  getSystemDoc,
} from '@/api/system'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const dialogVisible = ref(false)
const resetVisible = ref(false)
const resetUserId = ref(null)
const resetPassword = ref('')
const roles = ref([])
const formRef = ref()
const query = reactive({ pageNum: 1, pageSize: 10 })
const form = reactive({ user: { id: null, username: '', password: '', realName: '', email: '', phone: '', department: '' }, roleIds: [] })

onMounted(async () => {
  roles.value = (await getRoles()).data
  loadData()
})

async function loadData() {
  loading.value = true
  try {
    const res = await getUserPage(query)
    tableData.value = res.data.records
    total.value = res.data.total
  } finally { loading.value = false }
}

function openDialog(row = null) {
  Object.assign(form.user, { id: null, username: '', password: '', realName: '', email: '', phone: '', department: '' })
  form.roleIds = []
  if (row) {
    Object.assign(form.user, row)
    form.roleIds = roles.value.filter(r => row.roles?.includes(r.roleCode)).map(r => r.id)
  }
  dialogVisible.value = true
}

async function handleSave() {
  if (form.user.id) await updateUser(form.user.id, form)
  else await createUser(form)
  ElMessage.success('保存成功')
  dialogVisible.value = false
  loadData()
}

async function handleDelete(id) {
  await ElMessageBox.confirm('确定删除该用户？', '提示', { type: 'warning' })
  await deleteUser(id)
  ElMessage.success('删除成功')
  loadData()
}

async function toggleStatus(row) {
  await toggleUserStatus(row.id)
  loadData()
}

function openReset(row) { resetUserId.value = row.id; resetPassword.value = ''; resetVisible.value = true }
async function doReset() {
  await resetPwd(resetUserId.value, resetPassword.value)
  ElMessage.success('密码重置成功')
  resetVisible.value = false
}

async function openDoc(type, title) {
  const docWindow = window.open('', '_blank')
  if (!docWindow) {
    ElMessage.warning('浏览器阻止了新窗口，请允许弹窗后重试')
    return
  }
  writeDocWindow(docWindow, title, '正在加载文档...')
  try {
    const res = await getSystemDoc(type)
    writeDocWindow(docWindow, title, res.data || '')
  } catch (error) {
    docWindow.close()
    throw error
  }
}

function writeDocWindow(docWindow, title, content) {
  docWindow.document.open()
  docWindow.document.write(`<!doctype html>
<html lang="zh-CN">
<head>
  <meta charset="UTF-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1.0" />
  <title>${escapeHtml(title)}</title>
  <style>
    body {
      margin: 0;
      background: #f5f7fa;
      color: #1f2a37;
      font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", "Microsoft YaHei", sans-serif;
    }
    main {
      max-width: 980px;
      margin: 0 auto;
      padding: 32px 24px 48px;
    }
    h1 {
      margin: 0 0 18px;
      font-size: 24px;
      line-height: 1.3;
    }
    pre {
      margin: 0;
      padding: 24px;
      border: 1px solid #e5e7eb;
      border-radius: 8px;
      background: #fff;
      color: #263445;
      white-space: pre-wrap;
      word-break: break-word;
      line-height: 1.75;
      font-size: 14px;
      box-sizing: border-box;
    }
  </style>
</head>
<body>
  <main>
    <h1>${escapeHtml(title)}</h1>
    <pre>${escapeHtml(content)}</pre>
  </main>
</body>
</html>`)
  docWindow.document.close()
}

function escapeHtml(value) {
  return String(value)
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#39;')
}

const roleLabel = (code) => ({ ROLE_ADMIN: '管理员', ROLE_MAINTAINER: '维护员', ROLE_USER: '普通用户' }[code] || code)
</script>

<style scoped>
.header-actions {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
  justify-content: flex-end;
}

.pagination { margin-top: 16px; justify-content: flex-end; }

@media (max-width: 768px) {
  .header-actions {
    width: 100%;
    justify-content: flex-start;
  }
}
</style>
