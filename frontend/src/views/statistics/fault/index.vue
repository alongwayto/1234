<template>
  <div class="page-container">
    <el-row :gutter="16">
      <el-col :span="12">
        <el-card header="故障类型分布" shadow="hover">
          <div ref="typeChartRef" style="height:320px"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card header="近7天故障趋势" shadow="hover">
          <div ref="trendChartRef" style="height:320px"></div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import * as echarts from 'echarts'
import { getFaultTypeDist, getFaultTrend } from '@/api/statistics'

const typeChartRef = ref()
const trendChartRef = ref()

onMounted(async () => {
  const [typeRes, trendRes] = await Promise.all([getFaultTypeDist(), getFaultTrend()])

  const typeChart = echarts.init(typeChartRef.value)
  typeChart.setOption({
    tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
    legend: { bottom: 0 },
    series: [{ type: 'pie', radius: ['35%', '65%'], data: typeRes.data }]
  })

  const trendChart = echarts.init(trendChartRef.value)
  trendChart.setOption({
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'category', data: trendRes.data.dates },
    yAxis: { type: 'value', minInterval: 1 },
    series: [{ name: '故障数', type: 'line', smooth: true, data: trendRes.data.counts,
      areaStyle: { opacity: 0.2 }, itemStyle: { color: '#f56c6c' } }]
  })
})
</script>
