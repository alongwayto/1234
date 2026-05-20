<template>
  <div class="page-container">
    <el-card v-loading="loading">
      <template #header>
        <div class="card-header">
          <span class="title">设备详情 - {{ device.deviceName }}</span>
          <div>
            <el-button @click="$router.back()">返回</el-button>
            <el-button type="primary" @click="$router.push('/fault/report')">上报故障</el-button>
          </div>
        </div>
      </template>
      <el-descriptions :column="3" border>
        <el-descriptions-item label="设备编号">{{ device.deviceCode }}</el-descriptions-item>
        <el-descriptions-item label="设备名称">{{ device.deviceName }}</el-descriptions-item>
        <el-descriptions-item label="设备分类">{{ device.categoryName }}</el-descriptions-item>
        <el-descriptions-item label="品牌">{{ device.brand }}</el-descriptions-item>
        <el-descriptions-item label="型号">{{ device.model }}</el-descriptions-item>
        <el-descriptions-item label="序列号">{{ device.serialNumber }}</el-descriptions-item>
        <el-descriptions-item label="位置">{{ device.locationName }}</el-descriptions-item>
        <el-descriptions-item label="购买日期">{{ device.purchaseDate }}</el-descriptions-item>
        <el-descriptions-item label="购买价格">¥{{ device.purchasePrice }}</el-descriptions-item>
        <el-descriptions-item label="保修到期">{{ device.warrantyDate }}</el-descriptions-item>
        <el-descriptions-item label="负责人">{{ device.responsiblePerson }}</el-descriptions-item>
        <el-descriptions-item label="负责人电话">{{ device.responsiblePhone }}</el-descriptions-item>
        <el-descriptions-item label="设备状态">
          <el-tag :type="statusType(device.status)">{{ statusLabel(device.status) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="在线状态">
          <el-tag :type="device.onlineStatus === 1 ? 'success' : 'info'">
            {{ device.onlineStatus === 1 ? '在线' : '离线' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="标签">{{ device.tags }}</el-descriptions-item>
        <el-descriptions-item label="描述" :span="3">{{ device.description }}</el-descriptions-item>
      </el-descriptions>

      <!-- 最近状态记录 -->
      <div style="margin-top:24px">
        <h4 style="margin-bottom:12px">最近状态记录</h4>
        <div ref="statusChartRef" style="height:200px"></div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import * as echarts from 'echarts'
import { getDeviceById } from '@/api/device'
import { getRecentStatus } from '@/api/monitor'

const route = useRoute()
const loading = ref(false)
const device = ref({})
const statusChartRef = ref()

onMounted(async () => {
  loading.value = true
  try {
    const [devRes, statusRes] = await Promise.all([
      getDeviceById(route.params.id),
      getRecentStatus(route.params.id, 20)
    ])
    device.value = devRes.data
    const records = statusRes.data.reverse()
    const chart = echarts.init(statusChartRef.value)
    chart.setOption({
      tooltip: { trigger: 'axis' },
      legend: { data: ['CPU%', '内存%', '磁盘%', '温度℃'] },
      xAxis: { type: 'category', data: records.map(r => r.recordTime?.substring(11, 16) || '') },
      yAxis: { type: 'value' },
      series: [
        { name: 'CPU%', type: 'line', smooth: true, data: records.map(r => r.cpuUsage) },
        { name: '内存%', type: 'line', smooth: true, data: records.map(r => r.memoryUsage) },
        { name: '磁盘%', type: 'line', smooth: true, data: records.map(r => r.diskUsage) },
        { name: '温度℃', type: 'line', smooth: true, data: records.map(r => r.temperature) },
      ]
    })
  } finally { loading.value = false }
})

const statusLabel = (s) => ({ 0: '停用', 1: '正常', 2: '维修中', 3: '报废' }[s] || '-')
const statusType = (s) => ({ 0: 'info', 1: 'success', 2: 'warning', 3: 'danger' }[s] || '')
</script>
