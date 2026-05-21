<template>
  <el-container class="app-layout">
    <!-- 侧边栏 -->
    <el-aside :width="isCollapsed ? '64px' : '220px'" class="sidebar">
      <div class="logo" :class="{ collapsed: isCollapsed }">
        <el-icon size="24" color="#fff"><Monitor /></el-icon>
        <span v-if="!isCollapsed" class="logo-text">设备管理系统</span>
      </div>
      <el-scrollbar>
        <el-menu
          :default-active="activeMenu"
          :collapse="isCollapsed"
          background-color="#001529"
          text-color="#ffffffa0"
          active-text-color="#ffffff"
          router
          class="sidebar-menu"
        >
          <el-menu-item index="/dashboard">
            <el-icon><Odometer /></el-icon>
            <template #title>首页仪表盘</template>
          </el-menu-item>

          <el-sub-menu index="device">
            <template #title>
              <el-icon><Monitor /></el-icon>
              <span>设备管理</span>
            </template>
            <el-menu-item index="/device/list">设备列表</el-menu-item>
            <el-menu-item index="/device/category">设备分类</el-menu-item>
            <el-menu-item index="/device/location">位置管理</el-menu-item>
          </el-sub-menu>

          <el-sub-menu index="monitor">
            <template #title>
              <el-icon><DataAnalysis /></el-icon>
              <span>状态监控</span>
            </template>
            <el-menu-item index="/monitor/status">
              实时监控
              <el-badge v-if="alertCount > 0" :value="alertCount" class="menu-badge" />
            </el-menu-item>
            <el-menu-item index="/monitor/alert">预警管理</el-menu-item>
          </el-sub-menu>

          <el-sub-menu index="fault">
            <template #title>
              <el-icon><Warning /></el-icon>
              <span>故障管理</span>
            </template>
            <el-menu-item index="/fault/report">故障上报</el-menu-item>
            <el-menu-item index="/fault/workorder">工单管理</el-menu-item>
            <el-menu-item index="/fault/archive">故障归档</el-menu-item>
          </el-sub-menu>

          <el-sub-menu index="statistics">
            <template #title>
              <el-icon><TrendCharts /></el-icon>
              <span>数据统计</span>
            </template>
            <el-menu-item index="/statistics/usage">使用率统计</el-menu-item>
            <el-menu-item index="/statistics/cost">维护成本</el-menu-item>
            <el-menu-item index="/statistics/fault">故障报表</el-menu-item>
          </el-sub-menu>

          <el-sub-menu index="ai">
            <template #title>
              <el-icon><ChatDotRound /></el-icon>
              <span>AI智能体</span>
            </template>
            <el-menu-item index="/ai/chat">AI助手</el-menu-item>
            <el-menu-item index="/ai/diagnosis">智能诊断</el-menu-item>
          </el-sub-menu>

          <el-sub-menu index="lifecycle">
            <template #title>
              <el-icon><Timer /></el-icon>
              <span>生命周期</span>
            </template>
            <el-menu-item index="/lifecycle/record">生命周期记录</el-menu-item>
            <el-menu-item index="/lifecycle/predict">预测性维护</el-menu-item>
          </el-sub-menu>

          <el-sub-menu index="system" v-if="userStore.roles.includes('ROLE_ADMIN')">
            <template #title>
              <el-icon><Setting /></el-icon>
              <span>系统管理</span>
            </template>
            <el-menu-item index="/system/user">用户管理</el-menu-item>
            <el-menu-item index="/system/role">角色管理</el-menu-item>
            <el-menu-item index="/system/log">操作日志</el-menu-item>
            <el-menu-item index="/system/backup">数据备份</el-menu-item>
          </el-sub-menu>
        </el-menu>
      </el-scrollbar>
    </el-aside>

    <el-container>
      <!-- 顶部导航 -->
      <el-header class="header">
        <div class="header-left">
          <el-icon class="collapse-btn" @click="isCollapsed = !isCollapsed">
            <component :is="isCollapsed ? 'Expand' : 'Fold'" />
          </el-icon>
          <!-- 面包屑 -->
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item v-if="route.meta.title">{{ route.meta.title }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div class="header-right">
          <el-tooltip content="预警通知">
            <el-badge :value="alertCount" :hidden="alertCount === 0" class="header-icon">
              <el-icon size="20" @click="$router.push('/monitor/alert')"><Bell /></el-icon>
            </el-badge>
          </el-tooltip>
          <el-dropdown @command="handleCommand">
            <div class="user-info">
              <el-avatar :size="32" :src="userStore.avatar || undefined">
                {{ userStore.username?.charAt(0)?.toUpperCase() }}
              </el-avatar>
              <span class="username">{{ userStore.userInfo?.realName || userStore.username }}</span>
              <el-icon><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile"><el-icon><User /></el-icon> 个人信息</el-dropdown-item>
                <el-dropdown-item command="logout" divided><el-icon><SwitchButton /></el-icon> 退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <!-- 主内容区 -->
      <el-main class="main-content">
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessageBox, ElMessage } from 'element-plus'
import { useUserStore } from '@/store/user'
import { getUnhandledCount } from '@/api/monitor'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const isCollapsed = ref(false)
const alertCount = ref(0)

const activeMenu = computed(() => route.path)

onMounted(async () => {
  try {
    const res = await getUnhandledCount()
    alertCount.value = res.data || 0
  } catch {}
})

async function handleCommand(cmd) {
  if (cmd === 'profile') {
    router.push('/profile')
  } else if (cmd === 'logout') {
    await ElMessageBox.confirm('确定要退出登录吗？', '提示', { type: 'warning' })
    userStore.logout()
    ElMessage.success('已退出登录')
    router.push('/login')
  }
}
</script>

<style scoped>
.app-layout { height: 100vh; }
.sidebar {
  background: #001529;
  transition: width 0.3s;
  overflow: hidden;
}
.logo {
  height: 60px;
  display: flex;
  align-items: center;
  padding: 0 20px;
  gap: 10px;
  border-bottom: 1px solid rgba(255,255,255,0.1);
  white-space: nowrap;
  overflow: hidden;
}
.logo.collapsed { padding: 0 20px; justify-content: center; }
.logo-text { color: #fff; font-size: 15px; font-weight: 600; }
.sidebar-menu { border-right: none; }
.menu-badge { margin-left: 8px; }
.header {
  background: #fff;
  border-bottom: 1px solid #e8e8e8;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  height: 60px;
  box-shadow: 0 1px 4px rgba(0,21,41,0.08);
}
.header-left { display: flex; align-items: center; gap: 16px; }
.collapse-btn { font-size: 20px; cursor: pointer; color: #606266; }
.collapse-btn:hover { color: #409eff; }
.header-right { display: flex; align-items: center; gap: 20px; }
.header-icon { cursor: pointer; }
.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 6px;
  transition: background 0.2s;
}
.user-info:hover { background: #f5f7fa; }
.username { font-size: 14px; color: #303133; }
.main-content {
  background: #f5f7fa;
  padding: 20px;
  overflow-y: auto;
}
</style>
