<template>
  <div class="page-container">
    <el-row :gutter="24">
      <!-- 左侧头像卡片 -->
      <el-col :span="8">
        <el-card shadow="never" class="avatar-card">
          <div class="avatar-section">
            <el-avatar :size="100" :src="userInfo.avatar || undefined" class="avatar">
              {{ userInfo.username?.charAt(0)?.toUpperCase() }}
            </el-avatar>
            <el-upload :show-file-list="false" :before-upload="beforeAvatarUpload" :http-request="uploadAvatar" accept="image/*">
              <el-button size="small" style="margin-top:12px">更换头像</el-button>
            </el-upload>
            <div class="user-name">{{ userInfo.realName || userInfo.username }}</div>
            <div class="user-dept">{{ userInfo.department }}</div>
            <div class="user-roles">
              <el-tag v-for="r in userInfo.roles" :key="r" size="small" style="margin:2px">{{ roleLabel(r) }}</el-tag>
            </div>
          </div>
          <el-divider />
          <div class="info-item"><el-icon><User /></el-icon> {{ userInfo.username }}</div>
          <div class="info-item"><el-icon><Message /></el-icon> {{ userInfo.email || '未设置' }}</div>
          <div class="info-item"><el-icon><Phone /></el-icon> {{ userInfo.phone || '未设置' }}</div>
          <div class="info-item"><el-icon><Clock /></el-icon> 最后登录：{{ userInfo.lastLogin || '-' }}</div>
        </el-card>
      </el-col>

      <!-- 右侧信息编辑 -->
      <el-col :span="16">
        <el-tabs v-model="activeTab">
          <el-tab-pane label="基本信息" name="info">
            <el-card shadow="never">
              <el-form ref="infoFormRef" :model="infoForm" label-width="90px">
                <el-row :gutter="16">
                  <el-col :span="12">
                    <el-form-item label="真实姓名"><el-input v-model="infoForm.realName" /></el-form-item>
                  </el-col>
                  <el-col :span="12">
                    <el-form-item label="性别">
                      <el-radio-group v-model="infoForm.gender">
                        <el-radio :label="0">未知</el-radio>
                        <el-radio :label="1">男</el-radio>
                        <el-radio :label="2">女</el-radio>
                      </el-radio-group>
                    </el-form-item>
                  </el-col>
                  <el-col :span="12">
                    <el-form-item label="邮箱">
                      <el-input v-model="infoForm.email" type="email" />
                    </el-form-item>
                  </el-col>
                  <el-col :span="12">
                    <el-form-item label="手机号"><el-input v-model="infoForm.phone" /></el-form-item>
                  </el-col>
                  <el-col :span="12">
                    <el-form-item label="所属部门"><el-input v-model="infoForm.department" /></el-form-item>
                  </el-col>
                  <el-col :span="24">
                    <el-form-item label="备注"><el-input v-model="infoForm.remark" type="textarea" :rows="2" /></el-form-item>
                  </el-col>
                </el-row>
                <el-form-item>
                  <el-button type="primary" :loading="saving" @click="saveInfo">保存信息</el-button>
                </el-form-item>
              </el-form>
            </el-card>
          </el-tab-pane>

          <el-tab-pane label="修改密码" name="password">
            <el-card shadow="never">
              <el-form ref="pwdFormRef" :model="pwdForm" :rules="pwdRules" label-width="100px" style="max-width:400px">
                <el-form-item label="当前密码" prop="oldPassword">
                  <el-input v-model="pwdForm.oldPassword" type="password" show-password />
                </el-form-item>
                <el-form-item label="新密码" prop="newPassword">
                  <el-input v-model="pwdForm.newPassword" type="password" show-password />
                </el-form-item>
                <el-form-item label="确认新密码" prop="confirmPassword">
                  <el-input v-model="pwdForm.confirmPassword" type="password" show-password />
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" :loading="changingPwd" @click="changePassword">修改密码</el-button>
                </el-form-item>
              </el-form>
            </el-card>
          </el-tab-pane>
        </el-tabs>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/store/user'
import { updateProfile, changePassword as changePwd, uploadAvatar as uploadAvatarApi } from '@/api/auth'

const userStore = useUserStore()
const userInfo = computed(() => userStore.userInfo || {})
const activeTab = ref('info')
const saving = ref(false)
const changingPwd = ref(false)
const infoFormRef = ref()
const pwdFormRef = ref()

const infoForm = reactive({ realName: '', gender: 0, email: '', phone: '', department: '', remark: '' })
const pwdForm = reactive({ oldPassword: '', newPassword: '', confirmPassword: '' })

const pwdRules = {
  oldPassword: [{ required: true, message: '请输入当前密码' }],
  newPassword: [{ required: true, message: '请输入新密码' }, { min: 6, message: '密码至少6位' }],
  confirmPassword: [
    { required: true, message: '请确认新密码' },
    { validator: (rule, val, cb) => val === pwdForm.newPassword ? cb() : cb(new Error('两次密码不一致')), trigger: 'blur' }
  ],
}

onMounted(() => {
  const u = userStore.userInfo
  if (u) Object.assign(infoForm, { realName: u.realName, gender: u.gender || 0, email: u.email, phone: u.phone, department: u.department, remark: u.remark })
})

async function saveInfo() {
  saving.value = true
  try {
    await updateProfile(infoForm)
    await userStore.fetchUserInfo()
    ElMessage.success('信息更新成功')
  } finally { saving.value = false }
}

async function changePassword() {
  await pwdFormRef.value.validate()
  changingPwd.value = true
  try {
    await changePwd({ oldPassword: pwdForm.oldPassword, newPassword: pwdForm.newPassword })
    ElMessage.success('密码修改成功，请重新登录')
    userStore.logout()
    window.location.href = '/login'
  } finally { changingPwd.value = false }
}

function beforeAvatarUpload(file) {
  const isImage = file.type.startsWith('image/')
  const isLt2M = file.size / 1024 / 1024 < 2
  if (!isImage) ElMessage.error('只能上传图片文件')
  if (!isLt2M) ElMessage.error('图片大小不能超过 2MB')
  return isImage && isLt2M
}

async function uploadAvatar({ file }) {
  const fd = new FormData()
  fd.append('file', file)
  const res = await uploadAvatarApi(fd)
  await userStore.fetchUserInfo()
  ElMessage.success('头像更新成功')
}

const roleLabel = (code) => ({ ROLE_ADMIN: '管理员', ROLE_MAINTAINER: '维护员', ROLE_USER: '普通用户' }[code] || code)
</script>

<style scoped>
.avatar-card { text-align: center; }
.avatar-section { display: flex; flex-direction: column; align-items: center; padding: 20px 0; }
.avatar { font-size: 36px; background: #409eff; }
.user-name { font-size: 20px; font-weight: 600; margin-top: 12px; color: #303133; }
.user-dept { font-size: 13px; color: #909399; margin-top: 4px; }
.user-roles { margin-top: 8px; }
.info-item { display: flex; align-items: center; gap: 8px; padding: 8px 0; font-size: 14px; color: #606266; border-bottom: 1px solid #f5f5f5; }
</style>
