<template>
  <div class="realtime-screen">
    <div class="screen-header">
      <div>
        <h2>实时监控大屏</h2>
        <p>设备运行状态、性能指标、健康评分与维护建议</p>
      </div>
      <div class="refresh-state">
        <span class="pulse"></span>
        <span>{{ refreshText }}</span>
      </div>
    </div>

    <el-row :gutter="16" class="stat-row">
      <el-col v-for="card in statCards" :key="card.key" :xs="12" :sm="8" :md="4">
        <el-card class="stat-card" shadow="never">
          <div class="stat-icon" :class="card.className">
            <el-icon><component :is="card.icon" /></el-icon>
          </div>
          <div class="stat-value">{{ card.value }}</div>
          <div class="stat-label">{{ card.label }}</div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16" class="content-row">
      <el-col :xs="24" :lg="8">
        <el-card class="panel-card" shadow="never">
          <template #header>
            <div class="panel-title">状态分布</div>
          </template>
          <div ref="statusChartRef" class="chart chart-sm"></div>
        </el-card>
      </el-col>

      <el-col :xs="24" :lg="16">
        <el-card class="panel-card" shadow="never">
          <template #header>
            <div class="panel-title">实时性能趋势</div>
          </template>
          <div ref="trendChartRef" class="chart chart-sm"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16" class="content-row">
      <el-col :xs="24" :lg="16">
        <el-card class="panel-card" shadow="never">
          <template #header>
            <div class="panel-title">设备健康状态</div>
          </template>
          <el-table :data="monitor.devices" height="470" class="device-table">
            <el-table-column prop="deviceCode" label="编号" width="136" />
            <el-table-column prop="deviceName" label="设备名称" min-width="160" show-overflow-tooltip />
            <el-table-column label="在线" width="86">
              <template #default="{ row }">
                <el-tag :type="row.onlineStatus === 1 ? 'success' : 'info'" size="small">
                  {{ row.onlineStatus === 1 ? '在线' : '离线' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="健康度" width="156">
              <template #default="{ row }">
                <div class="health-cell">
                  <el-progress
                    :percentage="toPercent(row.healthScore)"
                    :color="healthColor(row.healthScore)"
                    :stroke-width="8"
                  />
                </div>
              </template>
            </el-table-column>
            <el-table-column label="风险" width="86">
              <template #default="{ row }">
                <el-tag :type="riskTagType(row.riskLevel)" size="small">
                  {{ row.riskLabel || '未知' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="CPU" width="130">
              <template #default="{ row }">
                <el-progress :percentage="toPercent(row.cpuUsage)" :color="progressColor(row.cpuUsage)" />
              </template>
            </el-table-column>
            <el-table-column label="内存" width="130">
              <template #default="{ row }">
                <el-progress :percentage="toPercent(row.memoryUsage)" :color="progressColor(row.memoryUsage)" />
              </template>
            </el-table-column>
            <el-table-column label="温度" width="96">
              <template #default="{ row }">
                <span :class="['temp-value', Number(row.temperature || 0) >= 70 ? 'is-hot' : '']">
                  {{ row.temperature ?? '-' }} C
                </span>
              </template>
            </el-table-column>
            <el-table-column label="维护建议" min-width="230" show-overflow-tooltip>
              <template #default="{ row }">
                {{ row.maintenanceAdvice || '暂无建议' }}
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>

      <el-col :xs="24" :lg="8">
        <el-card class="panel-card risk-panel" shadow="never">
          <template #header>
            <div class="panel-title">风险设备排行</div>
          </template>
          <div class="risk-list">
            <div v-for="device in monitor.riskDevices" :key="device.id" class="risk-item">
              <div class="risk-main">
                <div class="risk-name">{{ device.deviceName }}</div>
                <div class="risk-advice">{{ device.maintenanceAdvice }}</div>
              </div>
              <div class="risk-score" :style="{ color: healthColor(device.healthScore) }">
                {{ device.healthScore }}
              </div>
            </div>
            <el-empty v-if="!monitor.riskDevices.length" description="暂无风险设备" :image-size="80" />
          </div>
        </el-card>

        <el-card class="panel-card alert-panel" shadow="never">
          <template #header>
            <div class="panel-title">未处理告警</div>
          </template>
          <div class="alert-list">
            <div v-for="alert in monitor.alerts" :key="alert.id" class="alert-item">
              <div class="alert-head">
                <el-tag :type="alertType(alert.alertLevel)" size="small">
                  {{ alertLevelText(alert.alertLevel) }}
                </el-tag>
                <span>{{ formatTime(alert.createTime) }}</span>
              </div>
              <div class="alert-device">{{ alert.deviceName || alert.deviceCode }}</div>
              <div class="alert-message">{{ alert.alertMsg }}</div>
            </div>
            <el-empty v-if="!monitor.alerts.length" description="暂无未处理告警" :image-size="80" />
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { computed, nextTick, onMounted, onUnmounted, reactive, ref } from 'vue'
import * as echarts from 'echarts'
import { getRealtimeMonitor } from '@/api/monitor'

const statusChartRef = ref()
const trendChartRef = ref()
const refreshText = ref('正在连接')
const monitor = reactive({
  totalDevices: 0,
  onlineDevices: 0,
  offlineDevices: 0,
  faultDevices: 0,
  unhandledAlerts: 0,
  onlineRate: 0,
  averageHealthScore: 0,
  devices: [],
  riskDevices: [],
  alerts: [],
  trend: [],
})

let statusChart
let trendChart
let timer

const statCards = computed(() => [
  { key: 'total', label: '设备总数', value: monitor.totalDevices, icon: 'Monitor', className: 'blue' },
  { key: 'online', label: '在线设备', value: monitor.onlineDevices, icon: 'Connection', className: 'green' },
  { key: 'offline', label: '离线设备', value: monitor.offlineDevices, icon: 'Close', className: 'gray' },
  { key: 'health', label: '平均健康度', value: `${monitor.averageHealthScore || 0}%`, icon: 'DataAnalysis', className: 'teal' },
  { key: 'alerts', label: '未处理告警', value: monitor.unhandledAlerts, icon: 'Bell', className: 'red' },
  { key: 'rate', label: '在线率', value: `${monitor.onlineRate || 0}%`, icon: 'DataLine', className: 'purple' },
])

onMounted(async () => {
  await nextTick()
  statusChart = echarts.init(statusChartRef.value)
  trendChart = echarts.init(trendChartRef.value)
  await loadRealtime()
  timer = setInterval(loadRealtime, 5000)
  window.addEventListener('resize', resizeCharts)
})

onUnmounted(() => {
  if (timer) clearInterval(timer)
  window.removeEventListener('resize', resizeCharts)
  statusChart?.dispose()
  trendChart?.dispose()
})

async function loadRealtime() {
  const res = await getRealtimeMonitor()
  Object.assign(monitor, res.data)
  refreshText.value = `刷新于 ${formatTime(res.data.refreshTime)}`
  renderCharts()
}

function renderCharts() {
  statusChart?.setOption({
    tooltip: { trigger: 'item' },
    legend: { bottom: 0 },
    series: [{
      type: 'pie',
      radius: ['48%', '72%'],
      center: ['50%', '44%'],
      data: [
        { name: '在线', value: monitor.onlineDevices },
        { name: '离线', value: monitor.offlineDevices },
        { name: '异常', value: monitor.faultDevices },
        { name: '告警', value: monitor.unhandledAlerts },
      ],
      color: ['#2f8f6f', '#8b95a1', '#c7792f', '#cf4b4b'],
    }],
  })

  const trend = monitor.trend || []
  trendChart?.setOption({
    tooltip: { trigger: 'axis' },
    legend: { top: 0 },
    grid: { left: 42, right: 24, top: 42, bottom: 36 },
    xAxis: { type: 'category', data: trend.map(item => item.label), boundaryGap: false },
    yAxis: { type: 'value', min: 0, max: 100 },
    series: [
      { name: 'CPU', type: 'line', smooth: true, data: trend.map(item => item.cpuUsage), color: '#3478c7' },
      { name: '内存', type: 'line', smooth: true, data: trend.map(item => item.memoryUsage), color: '#2f8f6f' },
      { name: '温度', type: 'line', smooth: true, data: trend.map(item => item.temperature), color: '#c7792f' },
    ],
  })
}

function resizeCharts() {
  statusChart?.resize()
  trendChart?.resize()
}

function toPercent(value) {
  return Math.max(0, Math.min(100, Math.round(Number(value || 0))))
}

function progressColor(value) {
  const num = Number(value || 0)
  if (num >= 90) return '#cf4b4b'
  if (num >= 75) return '#c7792f'
  return '#2f8f6f'
}

function healthColor(value) {
  const num = Number(value || 0)
  if (num >= 85) return '#2f8f6f'
  if (num >= 70) return '#3478c7'
  if (num >= 55) return '#c7792f'
  return '#cf4b4b'
}

function riskTagType(level) {
  if (level === 'critical') return 'danger'
  if (level === 'warning') return 'warning'
  if (level === 'watch') return 'primary'
  return 'success'
}

function alertType(level) {
  if (level >= 4) return 'danger'
  if (level >= 3) return 'warning'
  return 'info'
}

function alertLevelText(level) {
  if (level >= 4) return '紧急'
  if (level >= 3) return '高'
  if (level >= 2) return '中'
  return '低'
}

function formatTime(value) {
  if (!value) return '--'
  return String(value).replace('T', ' ').slice(11, 19)
}
</script>

<style scoped>
.realtime-screen {
  padding: 16px;
  background: #f5f7fa;
  min-height: calc(100vh - 84px);
}

.screen-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  gap: 16px;
  margin-bottom: 16px;
}

.screen-header h2 {
  margin: 0;
  font-size: 24px;
  color: #1f2a37;
  font-weight: 700;
}

.screen-header p {
  margin: 6px 0 0;
  color: #6b7280;
  font-size: 14px;
}

.refresh-state {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #4b5563;
  font-size: 13px;
  white-space: nowrap;
}

.pulse {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #2f8f6f;
  box-shadow: 0 0 0 4px rgba(47, 143, 111, 0.12);
}

.stat-row,
.content-row {
  margin-bottom: 16px;
}

.stat-card,
.panel-card {
  border-radius: 8px;
  border: 1px solid #e5e7eb;
}

.stat-card {
  height: 138px;
}

.stat-icon {
  width: 38px;
  height: 38px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 8px;
  color: #fff;
  font-size: 20px;
  margin-bottom: 14px;
}

.stat-icon.blue { background: #3478c7; }
.stat-icon.green { background: #2f8f6f; }
.stat-icon.gray { background: #8b95a1; }
.stat-icon.teal { background: #178a93; }
.stat-icon.red { background: #cf4b4b; }
.stat-icon.purple { background: #6f5fbd; }

.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: #111827;
  line-height: 1;
}

.stat-label {
  margin-top: 8px;
  color: #6b7280;
  font-size: 13px;
}

.panel-title {
  font-weight: 600;
  color: #1f2a37;
}

.chart {
  width: 100%;
}

.chart-sm {
  height: 300px;
}

.device-table :deep(.el-progress__text) {
  min-width: 34px;
  font-size: 12px;
}

.health-cell {
  min-width: 120px;
}

.temp-value {
  font-weight: 600;
  color: #2f8f6f;
}

.temp-value.is-hot {
  color: #cf4b4b;
}

.risk-panel {
  margin-bottom: 16px;
}

.risk-list {
  min-height: 184px;
}

.risk-item {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  padding: 12px 0;
  border-bottom: 1px solid #edf0f5;
}

.risk-item:first-child {
  padding-top: 0;
}

.risk-main {
  min-width: 0;
}

.risk-name {
  color: #1f2a37;
  font-weight: 600;
  margin-bottom: 6px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.risk-advice {
  color: #6b7280;
  font-size: 12px;
  line-height: 1.5;
}

.risk-score {
  flex: 0 0 auto;
  font-size: 28px;
  font-weight: 700;
  line-height: 1;
}

.alert-list {
  height: 252px;
  overflow-y: auto;
  padding-right: 4px;
}

.alert-item {
  padding: 12px 0;
  border-bottom: 1px solid #edf0f5;
}

.alert-item:first-child {
  padding-top: 0;
}

.alert-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  color: #8b95a1;
  font-size: 12px;
  margin-bottom: 8px;
}

.alert-device {
  font-weight: 600;
  color: #1f2a37;
  margin-bottom: 6px;
}

.alert-message {
  color: #4b5563;
  font-size: 13px;
  line-height: 1.5;
}

@media (max-width: 768px) {
  .screen-header {
    align-items: flex-start;
    flex-direction: column;
  }

  .stat-card {
    height: 126px;
    margin-bottom: 12px;
  }
}
</style>
