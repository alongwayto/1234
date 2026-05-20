<template>
  <div class="login-container">
    <div class="login-box">
      <div class="login-header">
        <el-icon size="40" color="#409eff"><Monitor /></el-icon>
        <h1>智能校园设备管理系统</h1>
        <p>Campus Equipment Management System</p>
      </div>
      <el-form ref="formRef" :model="form" :rules="rules" size="large" @keyup.enter="handleLogin">
        <el-form-item prop="username">
          <el-input v-model="form.username" placeholder="请输入用户名" prefix-icon="User" clearable />
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="form.password" type="password" placeholder="请输入密码"
                    prefix-icon="Lock" show-password clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" class="login-btn" @click="handleLogin">
            {{ loading ? '登录中...' : '登 录' }}
          </el-button>
        </el-form-item>
      </el-form>
      <div class="login-tips">
        <span>默认账号：admin / admin123</span>
      </div>
    </div>
    <div class="login-bg">
      <div class="bg-circle c1"></div>
      <div class="bg-circle c2"></div>
      <div class="bg-circle c3"></div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/store/user'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref()
const loading = ref(false)

const form = reactive({ username: '', password: '' })
const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
}

async function handleLogin() {
  await formRef.value.validate()
  loading.value = true
  try {
    await userStore.login(form.username, form.password)
    ElMessage.success('登录成功')
    router.push('/')
  } catch (e) {
    // error handled by interceptor
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #1a237e 0%, #0d47a1 40%, #1565c0 100%);
  position: relative;
  overflow: hidden;
}
.login-box {
  width: 420px;
  background: rgba(255,255,255,0.95);
  border-radius: 16px;
  padding: 48px 40px;
  box-shadow: 0 20px 60px rgba(0,0,0,0.3);
  position: relative;
  z-index: 10;
}
.login-header {
  text-align: center;
  margin-bottom: 36px;
}
.login-header h1 {
  font-size: 22px;
  font-weight: 700;
  color: #1a237e;
  margin: 12px 0 6px;
}
.login-header p {
  font-size: 13px;
  color: #909399;
}
.login-btn {
  width: 100%;
  height: 44px;
  font-size: 16px;
  letter-spacing: 4px;
}
.login-tips {
  text-align: center;
  font-size: 12px;
  color: #c0c4cc;
  margin-top: 16px;
}
.bg-circle {
  position: absolute;
  border-radius: 50%;
  background: rgba(255,255,255,0.05);
}
.c1 { width: 400px; height: 400px; top: -100px; right: -100px; }
.c2 { width: 300px; height: 300px; bottom: -80px; left: -80px; }
.c3 { width: 200px; height: 200px; top: 50%; left: 10%; }
</style>
