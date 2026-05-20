<template>
  <div class="page-container">
    <el-card shadow="hover">
      <template #header>
        <div class="chart-header">
          <span>维护成本月度趋势</span>
          <div class="chart-controls">
            <el-radio-group v-model="quickMonths" size="small" @change="handleQuickChange">
              <el-radio-button :label="12">近12个月</el-radio-button>
              <el-radio-button :label="24">近24个月</el-radio-button>
              <el-radio-button :label="36">近36个月</el-radio-button>
              <el-radio-button :label="60">近60个月</el-radio-button>
            </el-radio-group>
            <el-date-picker
              v-model="monthRange"
              type="monthrange"
              value-format="YYYY-MM"
              range-separator="至"
              start-placeholder="开始月份"
              end-placeholder="结束月份"
              size="small"
              class="month-picker"
              @change="handleRangeChange"
            />
          </div>
        </div>
      </template>
      <div v-loading="loading" ref="costChartRef" class="cost-chart"></div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount } from 'vue'
import * as echarts from 'echarts'
import { getCostMonthly } from '@/api/statistics'

const costChartRef = ref()
const loading = ref(false)
const quickMonths = ref(12)
const monthRange = ref([])
let chart

onMounted(() => {
  loadChart()
  window.addEventListener('resize', resizeChart)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', resizeChart)
  chart?.dispose()
})

async function loadChart() {
  loading.value = true
  try {
    const params = monthRange.value?.length === 2
      ? { startMonth: monthRange.value[0], endMonth: monthRange.value[1] }
      : quickMonths.value
    const res = await getCostMonthly(params)
    renderChart(res.data)
  } finally {
    loading.value = false
  }
}

function renderChart(data) {
  if (!chart) chart = echarts.init(costChartRef.value)
  const labels = data.labels || []
  const costs = data.costs || []
  chart.setOption({
    tooltip: {
      trigger: 'axis',
      formatter: (items) => items.map((item) => `${item.marker}${item.seriesName}: ¥${item.value}`).join('<br/>'),
    },
    legend: { top: 8 },
    grid: { left: 56, right: 24, top: 56, bottom: labels.length > 24 ? 70 : 36 },
    xAxis: { type: 'category', data: labels, axisLabel: { rotate: labels.length > 12 ? 35 : 0 } },
    yAxis: { type: 'value', axisLabel: { formatter: '¥{value}' } },
    dataZoom: labels.length > 24 ? [{ type: 'inside' }, { type: 'slider', bottom: 12, height: 18 }] : [],
    series: [
      {
        name: '维护费用',
        type: 'bar',
        data: costs,
        itemStyle: { color: '#2f80ed', borderRadius: [4, 4, 0, 0] },
        label: { show: labels.length <= 24, position: 'top', formatter: '¥{c}' },
      },
      {
        name: '趋势',
        type: 'line',
        data: costs,
        smooth: true,
        symbolSize: 6,
        lineStyle: { width: 3, color: '#f59e0b' },
        itemStyle: { color: '#f59e0b' },
      },
    ],
  })
}

function handleQuickChange() {
  monthRange.value = []
  loadChart()
}

function handleRangeChange(value) {
  quickMonths.value = value?.length === 2 ? 0 : 12
  loadChart()
}

function resizeChart() {
  chart?.resize()
}
</script>

<style scoped>
.chart-header {
  align-items: center;
  display: flex;
  gap: 16px;
  justify-content: space-between;
}

.chart-controls {
  align-items: center;
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  justify-content: flex-end;
}

.month-picker {
  width: 240px;
}

.cost-chart {
  height: 380px;
}

@media (max-width: 900px) {
  .chart-header {
    align-items: flex-start;
    flex-direction: column;
  }

  .chart-controls {
    justify-content: flex-start;
  }
}
</style>
