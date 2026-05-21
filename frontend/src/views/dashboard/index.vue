<template>
  <div class="dashboard-container">
    <!-- 顶部统计卡片 -->
    <el-row :gutter="20" class="stat-cards">
      <el-col :span="6">
        <div class="stat-card stat-primary">
          <div class="stat-icon">
            <el-icon :size="40"><Monitor /></el-icon>
          </div>
          <div class="stat-info">
            <span class="stat-value">{{ stats.totalDevices }}</span>
            <span class="stat-label">设备总数</span>
          </div>
          <div class="stat-trend up">
            <el-icon><Top /></el-icon>
            <span>+12%</span>
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card stat-success">
          <div class="stat-icon">
            <el-icon :size="40"><CircleCheck /></el-icon>
          </div>
          <div class="stat-info">
            <span class="stat-value">{{ stats.onlineDevices }}</span>
            <span class="stat-label">在线设备</span>
          </div>
          <div class="stat-trend up">
            <el-icon><Top /></el-icon>
            <span>+8%</span>
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card stat-warning">
          <div class="stat-icon">
            <el-icon :size="40"><Warning /></el-icon>
          </div>
          <div class="stat-info">
            <span class="stat-value">{{ stats.alerts }}</span>
            <span class="stat-label">待处理预警</span>
          </div>
          <div class="stat-trend down">
            <el-icon><Bottom /></el-icon>
            <span>-5%</span>
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card stat-danger">
          <div class="stat-icon">
            <el-icon :size="40"><WarningFilled /></el-icon>
          </div>
          <div class="stat-info">
            <span class="stat-value">{{ stats.faults }}</span>
            <span class="stat-label">故障工单</span>
          </div>
          <div class="stat-trend">
            <span>进行中</span>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- 主要内容区 -->
    <el-row :gutter="20" class="main-content">
      <!-- 左侧图表区域 -->
      <el-col :span="16">
        <!-- 设备状态分布 -->
        <el-card class="chart-card">
          <template #header>
            <div class="card-header">
              <span>设备状态分布</span>
              <el-radio-group v-model="statusPeriod" size="small">
                <el-radio-button label="today">今日</el-radio-button>
                <el-radio-button label="week">本周</el-radio-button>
                <el-radio-button label="month">本月</el-radio-button>
              </el-radio-group>
            </div>
          </template>
          <div class="chart-container">
            <v-chart :option="statusPieOption" autoresize />
          </div>
        </el-card>

        <!-- 设备运行趋势 -->
        <el-card class="chart-card">
          <template #header>
            <div class="card-header">
              <span>设备运行趋势</span>
              <el-select v-model="trendType" size="small" style="width: 120px">
                <el-option label="在线率" value="online" />
                <el-option label="CPU使用率" value="cpu" />
                <el-option label="内存使用率" value="memory" />
              </el-select>
            </div>
          </template>
          <div class="chart-container">
            <v-chart :option="trendLineOption" autoresize />
          </div>
        </el-card>
      </el-col>

      <!-- 右侧信息区域 -->
      <el-col :span="8">
        <!-- 实时设备状态 -->
        <el-card class="info-card">
          <template #header>
            <div class="card-header">
              <span>实时设备状态</span>
              <el-tag type="success" size="small">实时</el-tag>
            </div>
          </template>
          <div class="device-list">
            <div v-for="device in realTimeDevices" :key="device.id" class="device-item">
              <div class="device-info">
                <span class="device-name">{{ device.name }}</span>
                <span class="device-location">{{ device.location }}</span>
              </div>
              <div class="device-status">
                <span class="status-dot" :class="device.status"></span>
                <span class="status-text">{{ device.statusText }}</span>
              </div>
            </div>
          </div>
        </el-card>

        <!-- 预警信息 -->
        <el-card class="info-card">
          <template #header>
            <div class="card-header">
              <span>最新预警</span>
              <el-button type="primary" link size="small" @click="$router.push('/monitor/alerts')">
                查看全部
              </el-button>
            </div>
          </template>
          <div class="alert-list">
            <div v-for="alert in alerts" :key="alert.id" class="alert-item" :class="'level-' + alert.level">
              <div class="alert-level">
                <el-tag :type="getAlertType(alert.level)" size="small">
                  {{ getAlertLevelText(alert.level) }}
                </el-tag>
              </div>
              <div class="alert-content">
                <span class="alert-title">{{ alert.title }}</span>
                <span class="alert-time">{{ alert.createTime }}</span>
              </div>
            </div>
          </div>
        </el-card>

        <!-- 维护提醒 -->
        <el-card class="info-card">
          <template #header>
            <div class="card-header">
              <span>预测性维护</span>
              <el-button type="primary" link size="small" @click="$router.push('/lifecycle/predict')">
                查看全部
              </el-button>
            </div>
          </template>
          <div class="maintenance-list">
            <div v-for="item in maintenanceList" :key="item.id" class="maintenance-item">
              <el-progress :percentage="item.probability" :color="getProgressColor(item.probability)" />
              <div class="maintenance-info">
                <span class="device-name">{{ item.deviceName }}</span>
                <span class="maintenance-desc">{{ item.description }}</span>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { PieChart, LineChart } from 'echarts/charts'
import { TitleComponent, TooltipComponent, LegendComponent, GridComponent } from 'echarts/components'
import VChart from 'vue-echarts'
import { Monitor, CircleCheck, Warning, WarningFilled, Top, Bottom } from '@element-plus/icons-vue'

use([CanvasRenderer, PieChart, LineChart, TitleComponent, TooltipComponent, LegendComponent, GridComponent])

// 统计数据
const stats = ref({
  totalDevices: 256,
  onlineDevices: 228,
  alerts: 12,
  faults: 8
})

// 时间选择
const statusPeriod = ref('week')
const trendType = ref('online')

// 实时设备
const realTimeDevices = ref([
  { id: 1, name: '核心交换机-01', location: 'A栋机房', status: 'online', statusText: '在线' },
  { id: 2, name: '服务器集群', location: 'B栋机房', status: 'online', statusText: '在线' },
  { id: 3, name: '监控摄像头-03', location: '教学楼', status: 'warning', statusText: '异常' },
  { id: 4, name: '空调控制器', location: '图书馆', status: 'online', statusText: '在线' },
  { id: 5, name: '门禁系统', location: '行政楼', status: 'offline', statusText: '离线' }
])

// 预警信息
const alerts = ref([
  { id: 1, level: 3, title: '服务器CPU温度过高', createTime: '10分钟前' },
  { id: 2, level: 2, title: '摄像头网络延迟', createTime: '30分钟前' },
  { id: 3, level: 1, title: '存储空间不足预警', createTime: '1小时前' }
])

// 维护提醒
const maintenanceList = ref([
  { id: 1, deviceName: '核心交换机', probability: 65, description: '可能在30天内出现性能下降' },
  { id: 2, deviceName: '服务器集群', probability: 35, description: '硬盘接近设计寿命' },
  { id: 3, deviceName: '监控存储', probability: 85, description: '存储空间即将满' }
])

// 设备状态饼图配置
const statusPieOption = computed(() => ({
  tooltip: {
    trigger: 'item',
    formatter: '{b}: {c} ({d}%)'
  },
  legend: {
    orient: 'vertical',
    left: 'left'
  },
  series: [{
    type: 'pie',
    radius: ['40%', '70%'],
    avoidLabelOverlap: false,
    itemStyle: {
      borderRadius: 10,
      borderColor: '#fff',
      borderWidth: 2
    },
    label: {
      show: true,
      formatter: '{b}: {d}%'
    },
    data: [
      { value: 228, name: '在线', itemStyle: { color: '#67c23a' } },
      { value: 15, name: '离线', itemStyle: { color: '#909399' } },
      { value: 8, name: '故障', itemStyle: { color: '#f56c6c' } },
      { value: 5, name: '维护中', itemStyle: { color: '#e6a23c' } }
    ]
  }]
}))

// 趋势图配置
const trendLineOption = computed(() => ({
  tooltip: {
    trigger: 'axis'
  },
  legend: {
    data: ['在线率', 'CPU使用率', '内存使用率'],
    bottom: 0
  },
  grid: {
    left: '3%',
    right: '4%',
    bottom: '15%',
    top: '10%',
    containLabel: true
  },
  xAxis: {
    type: 'category',
    boundaryGap: false,
    data: ['00:00', '04:00', '08:00', '12:00', '16:00', '20:00', '24:00']
  },
  yAxis: {
    type: 'value',
    min: 0,
    max: 100,
    axisLabel: {
      formatter: '{value}%'
    }
  },
  series: [
    {
      name: '在线率',
      type: 'line',
      smooth: true,
      data: [95, 92, 98, 99, 97, 96, 94],
      lineStyle: { color: '#67c23a' },
      areaStyle: { color: 'rgba(103, 194, 58, 0.2)' }
    },
    {
      name: 'CPU使用率',
      type: 'line',
      smooth: true,
      data: [30, 25, 45, 62, 58, 48, 35],
      lineStyle: { color: '#409eff' }
    },
    {
      name: '内存使用率',
      type: 'line',
      smooth: true,
      data: [55, 52, 58, 65, 62, 58, 56],
      lineStyle: { color: '#e6a23c' }
    }
  ]
}))

function getAlertType(level) {
  const types = { 1: 'info', 2: 'warning', 3: 'danger', 4: 'danger' }
  return types[level] || 'info'
}

function getAlertLevelText(level) {
  const texts = { 1: '提示', 2: '警告', 3: '严重', 4: '紧急' }
  return texts[level] || '未知'
}

function getProgressColor(probability) {
  if (probability >= 70) return '#f56c6c'
  if (probability >= 40) return '#e6a23c'
  return '#67c23a'
}

onMounted(() => {
  // 模拟实时数据更新
  // 实际项目中应通过WebSocket接收实时数据
})

onUnmounted(() => {
  // 清理
})
</script>

<style scoped>
.dashboard-container {
  padding: 20px;
  background: #f5f7fa;
  min-height: calc(100vh - 84px);
}

.stat-cards {
  margin-bottom: 20px;
}

.stat-card {
  display: flex;
  align-items: center;
  padding: 20px;
  border-radius: 12px;
  background: #fff;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
  position: relative;
  overflow: hidden;
}

.stat-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 4px;
  height: 100%;
}

.stat-primary::before { background: #409eff; }
.stat-success::before { background: #67c23a; }
.stat-warning::before { background: #e6a23c; }
.stat-danger::before { background: #f56c6c; }

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 16px;
}

.stat-primary .stat-icon { background: #ecf5ff; color: #409eff; }
.stat-success .stat-icon { background: #f0f9eb; color: #67c23a; }
.stat-warning .stat-icon { background: #fdf6ec; color: #e6a23c; }
.stat-danger .stat-icon { background: #fef0f0; color: #f56c6c; }

.stat-info {
  flex: 1;
}

.stat-value {
  display: block;
  font-size: 28px;
  font-weight: 700;
  color: #303133;
}

.stat-label {
  display: block;
  font-size: 14px;
  color: #909399;
  margin-top: 4px;
}

.stat-trend {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 14px;
  color: #67c23a;
}

.stat-trend.down {
  color: #f56c6c;
}

.main-content {
  display: flex;
}

.chart-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.chart-container {
  height: 300px;
}

.info-card {
  margin-bottom: 20px;
}

.device-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.device-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px;
  background: #f5f7fa;
  border-radius: 8px;
}

.device-info {
  display: flex;
  flex-direction: column;
}

.device-name {
  font-size: 14px;
  color: #303133;
  font-weight: 500;
}

.device-location {
  font-size: 12px;
  color: #909399;
}

.device-status {
  display: flex;
  align-items: center;
  gap: 6px;
}

.status-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
}

.status-dot.online { background: #67c23a; }
.status-dot.offline { background: #909399; }
.status-dot.warning { background: #e6a23c; }
.status-dot.error { background: #f56c6c; }

.status-text {
  font-size: 12px;
  color: #606266;
}

.alert-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.alert-item {
  display: flex;
  gap: 10px;
  padding: 10px;
  background: #f5f7fa;
  border-radius: 8px;
}

.alert-content {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.alert-title {
  font-size: 13px;
  color: #303133;
}

.alert-time {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}

.maintenance-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.maintenance-item {
  padding: 10px;
  background: #f5f7fa;
  border-radius: 8px;
}

.maintenance-info {
  margin-top: 8px;
  display: flex;
  flex-direction: column;
}

.maintenance-info .device-name {
  font-size: 13px;
  font-weight: 500;
}

.maintenance-desc {
  font-size: 12px;
  color: #909399;
  margin-top: 2px;
}
</style>
