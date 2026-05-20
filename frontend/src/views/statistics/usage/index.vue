<template>
  <div class="page-container">
    <el-row :gutter="16">
      <el-col :span="12">
        <el-card header="设备在线率" shadow="hover">
          <div ref="usageChartRef" style="height:320px"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card header="故障高频设备 TOP10" shadow="hover">
          <div ref="topChartRef" style="height:320px"></div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import * as echarts from 'echarts'
import { getDeviceUsage, getFaultTopDevices } from '@/api/statistics'

const usageChartRef = ref()
const topChartRef = ref()

onMounted(async () => {
  const [usageRes, topRes] = await Promise.all([getDeviceUsage(), getFaultTopDevices()])

  const usageChart = echarts.init(usageChartRef.value)
  usageChart.setOption({
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'category', data: usageRes.data.map(d => d.deviceName), axisLabel: { rotate: 30, interval: 0 } },
    yAxis: { type: 'value', max: 100, axisLabel: { formatter: '{value}%' } },
    series: [{ name: '使用率', type: 'bar', data: usageRes.data.map(d => d.usageRate),
      itemStyle: { color: '#409eff', borderRadius: [4,4,0,0] } }]
  })

  const topChart = echarts.init(topChartRef.value)
  const topData = topRes.data.reverse()
  topChart.setOption({
    tooltip: { trigger: 'axis' },
    yAxis: { type: 'category', data: topData.map(d => d.name) },
    xAxis: { type: 'value', minInterval: 1 },
    series: [{ name: '故障次数', type: 'bar', data: topData.map(d => d.count),
      itemStyle: { color: '#f56c6c', borderRadius: [0,4,4,0] } }]
  })
})
</script>
