import { createRouter, createWebHistory } from 'vue-router'
import NProgress from 'nprogress'
import 'nprogress/nprogress.css'
import { useUserStore } from '@/store/user'

NProgress.configure({ showSpinner: false })

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/index.vue'),
    meta: { title: '登录', noAuth: true },
  },
  {
    path: '/',
    component: () => import('@/components/layout/AppLayout.vue'),
    redirect: '/dashboard',
    children: [
      { path: 'dashboard', name: 'Dashboard', component: () => import('@/views/dashboard/index.vue'), meta: { title: '首页仪表盘', icon: 'Odometer' } },
      // 设备管理
      { path: 'device/list', name: 'DeviceList', component: () => import('@/views/device/list/index.vue'), meta: { title: '设备列表', icon: 'Monitor' } },
      { path: 'device/detail/:id', name: 'DeviceDetail', component: () => import('@/views/device/detail/index.vue'), meta: { title: '设备详情', hidden: true } },
      { path: 'device/category', name: 'DeviceCategory', component: () => import('@/views/device/category/index.vue'), meta: { title: '设备分类', icon: 'Grid' } },
      { path: 'device/location', name: 'DeviceLocation', component: () => import('@/views/device/location/index.vue'), meta: { title: '位置管理', icon: 'Location' } },
      // 状态监控
      { path: 'monitor/status', name: 'MonitorStatus', component: () => import('@/views/monitor/status/index.vue'), meta: { title: '实时监控', icon: 'DataAnalysis' } },
      { path: 'monitor/alert', name: 'MonitorAlert', component: () => import('@/views/monitor/alert/index.vue'), meta: { title: '预警管理', icon: 'Bell' } },
      // 故障管理
      { path: 'fault/report', name: 'FaultReport', component: () => import('@/views/fault/report/index.vue'), meta: { title: '故障上报', icon: 'Warning' } },
      { path: 'fault/workorder', name: 'FaultWorkorder', component: () => import('@/views/fault/workorder/index.vue'), meta: { title: '工单管理', icon: 'Tickets' } },
      { path: 'fault/archive', name: 'FaultArchive', component: () => import('@/views/fault/archive/index.vue'), meta: { title: '故障归档', icon: 'FolderChecked' } },
      // 统计分析
      { path: 'statistics/usage', name: 'StatUsage', component: () => import('@/views/statistics/usage/index.vue'), meta: { title: '使用率统计', icon: 'TrendCharts' } },
      { path: 'statistics/cost', name: 'StatCost', component: () => import('@/views/statistics/cost/index.vue'), meta: { title: '维护成本', icon: 'Money' } },
      { path: 'statistics/fault', name: 'StatFault', component: () => import('@/views/statistics/fault/index.vue'), meta: { title: '故障报表', icon: 'PieChart' } },
      // 系统管理
      { path: 'system/user', name: 'SystemUser', component: () => import('@/views/system/user/index.vue'), meta: { title: '用户管理', icon: 'User' } },
      { path: 'system/role', name: 'SystemRole', component: () => import('@/views/system/role/index.vue'), meta: { title: '角色管理', icon: 'UserFilled' } },
      { path: 'system/log', name: 'SystemLog', component: () => import('@/views/system/log/index.vue'), meta: { title: '操作日志', icon: 'Document' } },
      { path: 'system/backup', name: 'SystemBackup', component: () => import('@/views/system/backup/index.vue'), meta: { title: '数据备份', icon: 'Upload' } },
      // 个人信息
      { path: 'profile', name: 'Profile', component: () => import('@/views/profile/index.vue'), meta: { title: '个人信息', hidden: true } },
    ],
  },
  { path: '/:pathMatch(.*)*', redirect: '/dashboard' },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

router.beforeEach((to, from, next) => {
  NProgress.start()
  document.title = (to.meta.title ? to.meta.title + ' - ' : '') + '智能校园设备管理系统'
  const userStore = useUserStore()
  if (to.meta.noAuth) {
    next()
  } else if (!userStore.isLoggedIn) {
    next('/login')
  } else {
    next()
  }
})

router.afterEach(() => NProgress.done())

export default router
