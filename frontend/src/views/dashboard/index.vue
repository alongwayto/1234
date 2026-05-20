<template>
  <div class="ops-dashboard" v-loading="loading">
    <div class="dashboard-head">
      <div>
        <h2>运营驾驶舱</h2>
        <p>设备资产、健康风险、告警工单与维护成本总览</p>
      </div>
      <div class="head-actions">
        <span class="update-time">更新于 {{ lastUpdated }}</span>
        <el-button type="primary" plain @click="loadDashboard">
          <el-icon><Refresh /></el-icon>
          刷新
        </el-button>
      </div>
    </div>

    <el-row :gutter="16" class="metric-row">
      <el-col v-for="card in metricCards" :key="card.key" :xs="12" :sm="8" :lg="4">
        <el-card class="metric-card" shadow="never">
          <div class="metric-icon" :class="card.tone">
            <el-icon><component :is="card.icon" /></el-icon>
          </div>
          <div class="metric-main">
            <span class="metric-value">{{ card.value }}</span>
            <span class="metric-unit">{{ card.unit }}</span>
          </div>
          <div class="metric-label">{{ card.label }}</div>
          <div class="metric-sub">{{ card.sub }}</div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16" class="content-row">
      <el-col :xs="24" :lg="8">
        <el-card class="panel-card" shadow="never">
          <template #header>
            <div class="panel-title">
              <span>健康分布</span>
              <el-tag size="small" type="success">{{ formatPercent(dashboard.averageHealthScore) }}</el-tag>
            </div>
          </template>
          <div ref="healthChartRef" class="chart chart-md"></div>
        </el-card>
      </el-col>

      <el-col :xs="24" :lg="16">
        <el-card class="panel-card" shadow="never">
          <template #header>
            <div class="panel-title">
              <span>近 7 天故障趋势</span>
              <el-tag size="small" type="warning">本月 {{ overview.monthFaults || 0 }} 起</el-tag>
            </div>
          </template>
          <div ref="faultChartRef" class="chart chart-md"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16" class="content-row">
      <el-col :xs="24" :xl="8">
        <el-card class="panel-card" shadow="never">
          <template #header>
            <div class="panel-title">
              <span>待办建议</span>
              <el-tag size="small" type="danger">{{ alerts.unhandled || 0 }} 条告警</el-tag>
            </div>
          </template>
          <div class="action-list">
            <div v-for="(item, index) in dashboard.actionItems" :key="item" class="action-item">
              <div class="action-index">{{ index + 1 }}</div>
              <div class="action-text">{{ item }}</div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :xs="24" :xl="8">
        <el-card class="panel-card" shadow="never">
          <template #header>
            <div class="panel-title">
              <span>风险设备</span>
              <el-tag size="small">{{ dashboard.riskDevices.length }} 台</el-tag>
            </div>
          </template>
          <div class="risk-list">
            <div v-for="device in dashboard.riskDevices" :key="device.id" class="risk-item">
              <div class="risk-main">
                <div class="risk-name">{{ device.deviceName || device.deviceCode }}</div>
                <div class="risk-advice">{{ device.maintenanceAdvice || '暂无维护建议' }}</div>
              </div>
              <div class="risk-side">
                <div class="risk-score" :style="{ color: healthColor(device.healthScore) }">
                  {{ device.healthScore ?? 0 }}
                </div>
                <el-tag :type="riskTagType(device.riskLevel)" size="small">
                  {{ device.riskLabel || '未知' }}
                </el-tag>
              </div>
            </div>
            <el-empty v-if="!dashboard.riskDevices.length" description="暂无风险设备" :image-size="82" />
          </div>
        </el-card>
      </el-col>

      <el-col :xs="24" :xl="8">
        <el-card class="panel-card" shadow="never">
          <template #header>
            <div class="panel-title">
              <span>最新告警</span>
              <el-tag size="small" type="info">未处理</el-tag>
            </div>
          </template>
          <div class="alert-list">
            <div v-for="alert in dashboard.recentAlerts" :key="alert.id" class="alert-item">
              <div class="alert-head">
                <el-tag :type="alertType(alert.alertLevel)" size="small">
                  {{ alertLevelText(alert.alertLevel) }}
                </el-tag>
                <span>{{ formatTime(alert.createTime) }}</span>
              </div>
              <div class="alert-device">{{ alert.deviceName || alert.deviceCode }}</div>
              <div class="alert-message">{{ alert.alertMsg }}</div>
            </div>
            <el-empty v-if="!dashboard.recentAlerts.length" description="暂无未处理告警" :image-size="82" />
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16" class="content-row">
      <el-col :xs="24" :lg="8">
        <el-card class="panel-card" shadow="never">
          <template #header>
            <div class="panel-title">
              <span>工单处理</span>
              <el-tag size="small" type="success">{{ formatPercent(workOrders.completionRate) }}</el-tag>
            </div>
          </template>
          <div ref="orderChartRef" class="chart chart-sm"></div>
        </el-card>
      </el-col>

      <el-col :xs="24" :lg="16">
        <el-card class="panel-card" shadow="never">
          <template #header>
            <div class="panel-title">
              <span>近 6 月维护成本</span>
              <span class="cost-total">合计 {{ totalCost }} 元</span>
            </div>
          </template>
          <div ref="costChartRef" class="chart chart-sm"></div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { computed, nextTick, onMounted, onUnmounted, reactive, ref } from 'vue'
import * as echarts from 'echarts'
import { getOperationsDashboard } from '@/api/statistics'

const loading = ref(false)
const lastUpdated = ref('--')
const healthChartRef = ref()
const faultChartRef = ref()
const orderChartRef = ref()
const costChartRef = ref()

const dashboard = reactive(createEmptyDashboard())
let healthChart
let faultChart
let orderChart
let costChart

const overview = computed(() => dashboard.overview || {})
const alerts = computed(() => dashboard.alerts || {})
const workOrders = computed(() => dashboard.workOrders || {})

const metricCards = computed(() => [
  {
    key: 'total',
    label: '设备总数',
    value: overview.value.totalDevices || 0,
    unit: '台',
    sub: `在线 ${overview.value.onlineDevices || 0} 台`,
    icon: 'Monitor',
    tone: 'blue',
  },
  {
    key: 'online',
    label: '在线率',
    value: formatNumber(dashboard.onlineRate),
    unit: '%',
    sub: `故障 ${overview.value.faultDevices || 0} 台`,
    icon: 'Connection',
    tone: 'green',
  },
  {
    key: 'health',
    label: '平均健康度',
    value: formatNumber(dashboard.averageHealthScore),
    unit: '分',
    sub: healthStateText.value,
    icon: 'DataAnalysis',
    tone: 'teal',
  },
  {
    key: 'alerts',
    label: '未处理告警',
    value: alerts.value.unhandled || 0,
    unit: '条',
    sub: `紧急 ${alerts.value.critical || 0} 条`,
    icon: 'Bell',
    tone: 'red',
  },
  {
    key: 'orders',
    label: '待处理工单',
    value: workOrders.value.pending || 0,
    unit: '单',
    sub: `处理中 ${workOrders.value.processing || 0} 单`,
    icon: 'Tickets',
    tone: 'purple',
  },
  {
    key: 'cost',
    label: '维护成本',
    value: totalCost.value,
    unit: '元',
    sub: '近 6 月累计',
    icon: 'Money',
    tone: 'orange',
  },
])

const healthStateText = computed(() => {
  const score = Number(dashboard.averageHealthScore || 0)
  if (score >= 85) return '整体健康'
  if (score >= 70) return '需要关注'
  if (score >= 55) return '建议巡检'
  return '优先处理'
})

const totalCost = computed(() => {
  const values = dashboard.costMonthly?.costs || []
  const total = values.reduce((sum, item) => sum + Number(item || 0), 0)
  return formatMoney(total)
})

onMounted(async () => {
  await nextTick()
  initCharts()
  await loadDashboard()
  window.addEventListener('resize', resizeCharts)
})

onUnmounted(() => {
  window.removeEventListener('resize', resizeCharts)
  ;[healthChart, faultChart, orderChart, costChart].forEach(chart => chart?.dispose())
})

async function loadDashboard() {
  loading.value = true
  try {
    const res = await getOperationsDashboard()
    Object.assign(dashboard, createEmptyDashboard(), res.data || {})
    lastUpdated.value = formatFullTime(new Date())
    await nextTick()
    renderCharts()
  } finally {
    loading.value = false
  }
}

function initCharts() {
  healthChart = echarts.init(healthChartRef.value)
  faultChart = echarts.init(faultChartRef.value)
  orderChart = echarts.init(orderChartRef.value)
  costChart = echarts.init(costChartRef.value)
}

function renderCharts() {
  renderHealthChart()
  renderFaultChart()
  renderOrderChart()
  renderCostChart()
}

function renderHealthChart() {
  healthChart?.setOption({
    tooltip: { trigger: 'item', formatter: '{b}: {c} 台 ({d}%)' },
    legend: { bottom: 0 },
    series: [
      {
        type: 'pie',
        radius: ['50%', '72%'],
        center: ['50%', '43%'],
        avoidLabelOverlap: true,
        data: dashboard.healthDistribution,
        color: ['#2f8f6f', '#3478c7', '#c7792f', '#cf4b4b'],
      },
    ],
  })
}

function renderFaultChart() {
  const trend = dashboard.faultTrend || {}
  faultChart?.setOption({
    tooltip: { trigger: 'axis' },
    grid: { top: 28, right: 24, bottom: 34, left: 42 },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: trend.dates || [],
      axisLabel: { formatter: value => String(value).slice(5) },
    },
    yAxis: { type: 'value', minInterval: 1 },
    series: [
      {
        name: '故障数',
        type: 'line',
        smooth: true,
        data: trend.counts || [],
        color: '#cf4b4b',
        symbolSize: 8,
        areaStyle: { color: 'rgba(207, 75, 75, 0.14)' },
      },
    ],
  })
}

function renderOrderChart() {
  const orders = workOrders.value
  orderChart?.setOption({
    tooltip: { trigger: 'axis' },
    grid: { top: 24, right: 18, bottom: 32, left: 36 },
    xAxis: { type: 'category', data: ['待处理', '处理中', '已完成'] },
    yAxis: { type: 'value', minInterval: 1 },
    series: [
      {
        name: '工单数',
        type: 'bar',
        barWidth: 34,
        data: [orders.pending || 0, orders.processing || 0, orders.finished || 0],
        itemStyle: {
          color: params => ['#c7792f', '#3478c7', '#2f8f6f'][params.dataIndex],
          borderRadius: [5, 5, 0, 0],
        },
      },
    ],
  })
}

function renderCostChart() {
  const cost = dashboard.costMonthly || {}
  costChart?.setOption({
    tooltip: {
      trigger: 'axis',
      valueFormatter: value => `${formatMoney(value)} 元`,
    },
    grid: { top: 24, right: 24, bottom: 34, left: 58 },
    xAxis: { type: 'category', data: cost.labels || [] },
    yAxis: { type: 'value' },
    series: [
      {
        name: '维护成本',
        type: 'bar',
        barWidth: 28,
        data: cost.costs || [],
        itemStyle: { color: '#6f5fbd', borderRadius: [5, 5, 0, 0] },
      },
    ],
  })
}

function resizeCharts() {
  ;[healthChart, faultChart, orderChart, costChart].forEach(chart => chart?.resize())
}

function createEmptyDashboard() {
  return {
    overview: {},
    onlineRate: 0,
    averageHealthScore: 0,
    healthDistribution: [],
    riskDevices: [],
    recentAlerts: [],
    workOrders: {},
    alerts: {},
    actionItems: [],
    faultTrend: { dates: [], counts: [] },
    costMonthly: { labels: [], costs: [] },
  }
}

function formatNumber(value) {
  const num = Number(value || 0)
  return Number.isInteger(num) ? num : num.toFixed(1)
}

function formatPercent(value) {
  return `${formatNumber(value)}%`
}

function formatMoney(value) {
  return Number(value || 0).toLocaleString('zh-CN', { maximumFractionDigits: 0 })
}

function formatTime(value) {
  if (!value) return '--'
  return String(value).replace('T', ' ').slice(5, 16)
}

function formatFullTime(value) {
  const date = value instanceof Date ? value : new Date(value)
  const pad = num => String(num).padStart(2, '0')
  return `${pad(date.getHours())}:${pad(date.getMinutes())}:${pad(date.getSeconds())}`
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
  if (level >= 2) return 'info'
  return 'success'
}

function alertLevelText(level) {
  if (level >= 4) return '紧急'
  if (level >= 3) return '高'
  if (level >= 2) return '中'
  return '低'
}
</script>

<style scoped>
.ops-dashboard {
  min-height: calc(100vh - 84px);
  padding: 16px;
  background: #f5f7fa;
}

.dashboard-head {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  gap: 16px;
  margin-bottom: 16px;
}

.dashboard-head h2 {
  margin: 0;
  color: #1f2a37;
  font-size: 24px;
  font-weight: 700;
}

.dashboard-head p {
  margin: 6px 0 0;
  color: #6b7280;
  font-size: 14px;
}

.head-actions {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
  justify-content: flex-end;
}

.update-time {
  color: #6b7280;
  font-size: 13px;
  white-space: nowrap;
}

.metric-row,
.content-row {
  margin-bottom: 16px;
}

.metric-card,
.panel-card {
  border: 1px solid #e5e7eb;
  border-radius: 8px;
}

.metric-card {
  height: 154px;
}

.metric-icon {
  width: 38px;
  height: 38px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  border-radius: 8px;
  font-size: 19px;
  margin-bottom: 14px;
}

.metric-icon.blue { background: #3478c7; }
.metric-icon.green { background: #2f8f6f; }
.metric-icon.teal { background: #178a93; }
.metric-icon.red { background: #cf4b4b; }
.metric-icon.purple { background: #6f5fbd; }
.metric-icon.orange { background: #c7792f; }

.metric-main {
  display: flex;
  align-items: baseline;
  gap: 4px;
  min-width: 0;
}

.metric-value {
  color: #111827;
  font-size: 27px;
  font-weight: 700;
  line-height: 1;
  overflow-wrap: anywhere;
}

.metric-unit {
  color: #4b5563;
  font-size: 13px;
  white-space: nowrap;
}

.metric-label {
  margin-top: 8px;
  color: #1f2a37;
  font-size: 13px;
  font-weight: 600;
}

.metric-sub {
  margin-top: 5px;
  color: #6b7280;
  font-size: 12px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.panel-title {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  color: #1f2a37;
  font-weight: 600;
}

.cost-total {
  color: #6b7280;
  font-size: 13px;
  font-weight: 400;
  white-space: nowrap;
}

.chart {
  width: 100%;
}

.chart-md {
  height: 310px;
}

.chart-sm {
  height: 260px;
}

.action-list,
.risk-list,
.alert-list {
  height: 294px;
  overflow-y: auto;
  padding-right: 4px;
}

.action-item,
.risk-item,
.alert-item {
  border-bottom: 1px solid #edf0f5;
}

.action-item {
  display: flex;
  gap: 12px;
  padding: 14px 0;
}

.action-item:first-child,
.risk-item:first-child,
.alert-item:first-child {
  padding-top: 0;
}

.action-index {
  flex: 0 0 auto;
  width: 24px;
  height: 24px;
  border-radius: 8px;
  background: #eaf2fb;
  color: #3478c7;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 13px;
  font-weight: 700;
}

.action-text {
  color: #374151;
  font-size: 13px;
  line-height: 1.6;
}

.risk-item {
  display: flex;
  justify-content: space-between;
  gap: 14px;
  padding: 13px 0;
}

.risk-main {
  min-width: 0;
}

.risk-name,
.alert-device {
  color: #1f2a37;
  font-weight: 600;
}

.risk-name {
  margin-bottom: 6px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.risk-advice,
.alert-message {
  color: #6b7280;
  font-size: 12px;
  line-height: 1.5;
}

.risk-side {
  flex: 0 0 auto;
  min-width: 54px;
  text-align: right;
}

.risk-score {
  margin-bottom: 8px;
  font-size: 25px;
  font-weight: 700;
  line-height: 1;
}

.alert-item {
  padding: 13px 0;
}

.alert-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
  color: #8b95a1;
  font-size: 12px;
}

.alert-device {
  margin-bottom: 6px;
}

@media (max-width: 768px) {
  .dashboard-head {
    align-items: flex-start;
    flex-direction: column;
  }

  .head-actions {
    width: 100%;
    justify-content: space-between;
  }

  .metric-card {
    height: 146px;
    margin-bottom: 12px;
  }

  .chart-md,
  .chart-sm {
    height: 260px;
  }
}
</style>
