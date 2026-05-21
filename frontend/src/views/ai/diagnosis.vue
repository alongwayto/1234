<template>
  <div class="diagnosis-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>AI智能诊断</span>
          <div class="header-actions">
            <el-select v-model="selectedDevice" placeholder="选择设备" filterable clearable style="width: 250px">
              <el-option
                v-for="device in deviceList"
                :key="device.id"
                :label="`${device.name} (${device.code})`"
                :value="device.id"
              />
            </el-select>
            <el-button type="primary" @click="startDiagnosis" :loading="diagnosing">
              <el-icon><Cpu /></el-icon>
              开始诊断
            </el-button>
          </div>
        </div>
      </template>

      <el-row :gutter="20">
        <!-- 诊断参数 -->
        <el-col :span="8">
          <div class="param-section">
            <h4>诊断参数设置</h4>
            <el-form :model="diagnosisParams" label-width="100px">
              <el-form-item label="CPU使用率">
                <el-slider v-model="diagnosisParams.cpuUsage" :min="0" :max="100" show-input />
              </el-form-item>
              <el-form-item label="内存使用率">
                <el-slider v-model="diagnosisParams.memoryUsage" :min="0" :max="100" show-input />
              </el-form-item>
              <el-form-item label="温度">
                <el-input-number v-model="diagnosisParams.temperature" :min="20" :max="100" /> <span class="unit">℃</span>
              </el-form-item>
              <el-form-item label="运行时间">
                <el-input-number v-model="diagnosisParams.uptimeHours" :min="0" :max="8760" /> <span class="unit">小时</span>
              </el-form-item>
              <el-form-item label="故障次数">
                <el-input-number v-model="diagnosisParams.faultCount" :min="0" :max="100" />
              </el-form-item>
            </el-form>
          </div>

          <div class="quick-actions">
            <h4>快捷诊断</h4>
            <div class="action-grid">
              <el-button v-for="action in quickActions" :key="action.type" @click="quickDiagnosis(action.type)">
                <el-icon><component :is="action.icon" /></el-icon>
                {{ action.label }}
              </el-button>
            </div>
          </div>
        </el-col>

        <!-- 诊断结果 -->
        <el-col :span="16">
          <div v-if="!diagnosisResult" class="empty-result">
            <el-empty description="请选择设备并设置参数后开始诊断">
              <template #image>
                <el-icon :size="80" color="#c0c4cc"><Cpu /></el-icon>
              </template>
            </el-empty>
          </div>

          <div v-else class="diagnosis-result">
            <!-- 诊断概览 -->
            <el-card class="result-overview" :class="'level-' + diagnosisResult.level">
              <div class="overview-header">
                <div class="health-score">
                  <el-progress type="circle" :percentage="diagnosisResult.healthScore" :color="getScoreColor(diagnosisResult.healthScore)" :width="100">
                    <template #default>
                      <div class="score-text">
                        <span class="score-value">{{ diagnosisResult.healthScore }}</span>
                        <span class="score-label">健康分</span>
                      </div>
                    </template>
                  </el-progress>
                </div>
                <div class="overview-info">
                  <h3>{{ diagnosisResult.status }}</h3>
                  <p class="overview-desc">{{ diagnosisResult.summary }}</p>
                  <div class="overview-tags">
                    <el-tag :type="getLevelType(diagnosisResult.level)">{{ getLevelText(diagnosisResult.level) }}</el-tag>
                    <el-tag>诊断耗时: {{ diagnosisResult.costTime }}ms</el-tag>
                  </div>
                </div>
              </div>
            </el-card>

            <!-- 诊断详情 -->
            <el-card class="result-detail">
              <el-tabs v-model="activeTab">
                <el-tab-pane label="故障分析" name="fault">
                  <div class="fault-analysis">
                    <div v-for="(item, index) in diagnosisResult.analysis" :key="index" class="analysis-item">
                      <div class="analysis-header">
                        <span class="analysis-index">{{ index + 1 }}</span>
                        <span class="analysis-title">{{ item.title }}</span>
                        <el-tag size="small" :type="item.severity === 'high' ? 'danger' : item.severity === 'medium' ? 'warning' : 'info'">
                          {{ item.severity === 'high' ? '高' : item.severity === 'medium' ? '中' : '低' }}
                        </el-tag>
                      </div>
                      <div class="analysis-content">{{ item.description }}</div>
                      <div class="analysis-evidence">
                        <span class="evidence-label">依据:</span>
                        {{ item.evidence }}
                      </div>
                    </div>
                  </div>
                </el-tab-pane>

                <el-tab-pane label="维修建议" name="suggestion">
                  <div class="suggestion-list">
                    <el-timeline>
                      <el-timeline-item
                        v-for="(suggestion, index) in diagnosisResult.suggestions"
                        :key="index"
                        :color="suggestion.urgent ? '#f56c6c' : '#409eff'"
                        :hollow="!suggestion.urgent"
                      >
                        <div class="suggestion-item">
                          <div class="suggestion-header">
                            <span class="suggestion-title">{{ suggestion.title }}</span>
                            <el-tag v-if="suggestion.urgent" type="danger" size="small">紧急</el-tag>
                            <el-tag v-if="suggestion.urgent === false" type="success" size="small">常规</el-tag>
                          </div>
                          <div class="suggestion-content">{{ suggestion.content }}</div>
                          <div class="suggestion-action">
                            <el-button type="primary" size="small" v-if="suggestion.actionable" @click="createWorkorder(suggestion)">
                              创建工单
                            </el-button>
                          </div>
                        </div>
                      </el-timeline-item>
                    </el-timeline>
                  </div>
                </el-tab-pane>

                <el-tab-pane label="历史对比" name="history">
                  <div class="history-chart">
                    <v-chart :option="historyOption" autoresize />
                  </div>
                </el-tab-pane>
              </el-tabs>
            </el-card>
          </div>
        </el-col>
      </el-row>
    </el-card>

    <!-- 历史诊断记录 -->
    <el-card class="history-card">
      <template #header>
        <span>诊断历史</span>
      </template>
      <el-table :data="historyRecords" stripe>
        <el-table-column type="index" label="序号" width="60" />
        <el-table-column prop="deviceName" label="设备名称" width="150" />
        <el-table-column prop="diagnosisTime" label="诊断时间" width="160" />
        <el-table-column prop="healthScore" label="健康分" width="100">
          <template #default="{ row }">
            <el-progress :percentage="row.healthScore" :color="getScoreColor(row.healthScore)" :stroke-width="8" />
          </template>
        </el-table-column>
        <el-table-column prop="status" label="诊断状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getLevelType(row.level)">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="summary" label="诊断摘要" min-width="200" show-overflow-tooltip />
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="viewHistory(row)">查看</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { LineChart } from 'echarts/charts'
import { GridComponent, TooltipComponent } from 'echarts/components'
import VChart from 'vue-echarts'
import { Cpu, Monitor, Warning, Refresh } from '@element-plus/icons-vue'

use([CanvasRenderer, LineChart, GridComponent, TooltipComponent])

const deviceList = ref([
  { id: 1, name: '核心交换机', code: 'DEV001' },
  { id: 2, name: '服务器集群', code: 'DEV002' },
  { id: 3, name: '监控摄像头', code: 'DEV003' }
])

const selectedDevice = ref(null)
const diagnosing = ref(false)
const diagnosisResult = ref(null)
const activeTab = ref('fault')

const diagnosisParams = reactive({
  cpuUsage: 50,
  memoryUsage: 60,
  temperature: 45,
  uptimeHours: 720,
  faultCount: 2
})

const quickActions = [
  { type: 'high_load', label: '高负载检测', icon: 'TrendCharts' },
  { type: 'temperature', label: '温度异常', icon: 'Warning' },
  { type: 'aging', label: '老化分析', icon: 'Timer' }
]

const historyRecords = ref([
  {
    id: 1,
    deviceName: '核心交换机',
    diagnosisTime: '2024-03-15 14:30:00',
    healthScore: 75,
    level: 2,
    status: '需要注意',
    summary: 'CPU使用率偏高，建议关注'
  },
  {
    id: 2,
    deviceName: '服务器集群',
    diagnosisTime: '2024-03-14 10:15:00',
    healthScore: 92,
    level: 1,
    status: '运行正常',
    summary: '各项指标正常，继续保持'
  }
])

const historyOption = computed(() => ({
  tooltip: { trigger: 'axis' },
  legend: { data: ['CPU使用率', '内存使用率', '健康分'], bottom: 0 },
  grid: { left: '3%', right: '4%', bottom: '15%', top: '10%', containLabel: true },
  xAxis: {
    type: 'category',
    data: ['03-10', '03-11', '03-12', '03-13', '03-14', '03-15']
  },
  yAxis: [
    { type: 'value', min: 0, max: 100, axisLabel: { formatter: '{value}%' } }
  ],
  series: [
    { name: 'CPU使用率', type: 'line', data: [45, 52, 48, 55, 58, 62] },
    { name: '内存使用率', type: 'line', data: [55, 58, 56, 60, 62, 65] },
    { name: '健康分', type: 'line', yAxisIndex: 0, data: [90, 88, 85, 82, 78, 75] }
  ]
}))

function getScoreColor(score) {
  if (score >= 80) return '#67c23a'
  if (score >= 60) return '#e6a23c'
  return '#f56c6c'
}

function getLevelText(level) {
  const map = { 1: '健康', 2: '需要注意', 3: '亚健康', 4: '危险' }
  return map[level] || '未知'
}

function getLevelType(level) {
  const map = { 1: 'success', 2: 'warning', 3: 'warning', 4: 'danger' }
  return map[level] || 'info'
}

async function startDiagnosis() {
  if (!selectedDevice.value) {
    ElMessage.warning('请先选择设备')
    return
  }
  
  diagnosing.value = true
  diagnosisResult.value = null
  
  try {
    // 模拟AI诊断过程
    await new Promise(resolve => setTimeout(resolve, 2000))
    
    const level = diagnosisParams.cpuUsage > 80 || diagnosisParams.temperature > 60 ? 3 : 
                  diagnosisParams.cpuUsage > 60 || diagnosisParams.temperature > 50 ? 2 : 1
    
    const healthScore = Math.max(30, 100 - diagnosisParams.cpuUsage * 0.3 - diagnosisParams.memoryUsage * 0.2 - 
                          (diagnosisParams.temperature - 40) * 0.5 - diagnosisParams.faultCount * 5)
    
    diagnosisResult.value = {
      healthScore: Math.round(healthScore),
      level,
      status: getLevelText(level),
      summary: level === 1 ? '设备运行状态良好，各项指标正常' :
               level === 2 ? '设备运行基本正常，部分指标偏高，建议关注' :
               '设备存在潜在风险，建议尽快处理',
      costTime: Math.floor(Math.random() * 500) + 200,
      analysis: [
        {
          title: 'CPU使用率分析',
          severity: diagnosisParams.cpuUsage > 70 ? 'high' : diagnosisParams.cpuUsage > 50 ? 'medium' : 'low',
          description: `当前CPU使用率为${diagnosisParams.cpuUsage}%，${
            diagnosisParams.cpuUsage > 70 ? '处于较高水平，可能影响性能' :
            diagnosisParams.cpuUsage > 50 ? '处于中等水平，建议监控' : '处于正常范围'
          }`,
          evidence: `基于最近7天的监控数据，CPU使用率呈上升趋势，峰值达到${diagnosisParams.cpuUsage + 10}%`
        },
        {
          title: '温度分析',
          severity: diagnosisParams.temperature > 60 ? 'high' : diagnosisParams.temperature > 50 ? 'medium' : 'low',
          description: `当前设备温度为${diagnosisParams.temperature}℃，${
            diagnosisParams.temperature > 60 ? '温度过高，可能导致硬件损伤' :
            diagnosisParams.temperature > 50 ? '温度偏高，需要关注散热' : '温度正常'
          }`,
          evidence: `环境温度35℃，设备表面温度与正常运行温度范围(30-50℃)的偏差为${diagnosisParams.temperature - 40}℃`
        }
      ],
      suggestions: [
        {
          title: '清理设备散热片和风扇',
          urgent: diagnosisParams.temperature > 60,
          actionable: true,
          content: '建议使用专业工具清理散热片和风扇，确保散热通道畅通'
        },
        {
          title: '检查并优化服务配置',
          urgent: false,
          actionable: true,
          content: '检查当前运行的服务，关闭不必要的进程以降低CPU负载'
        },
        {
          title: '增加监控频率',
          urgent: false,
          actionable: false,
          content: '建议将监控频率从每5分钟调整为每1分钟，及时发现异常'
        }
      ]
    }
  } catch (error) {
    ElMessage.error('诊断失败，请重试')
  } finally {
    diagnosing.value = false
  }
}

function quickDiagnosis(type) {
  switch (type) {
    case 'high_load':
      diagnosisParams.cpuUsage = 85
      diagnosisParams.memoryUsage = 80
      break
    case 'temperature':
      diagnosisParams.temperature = 75
      diagnosisParams.cpuUsage = 70
      break
    case 'aging':
      diagnosisParams.uptimeHours = 8000
      diagnosisParams.faultCount = 5
      break
  }
  startDiagnosis()
}

function createWorkorder(suggestion) {
  ElMessage.success(`已创建工单: ${suggestion.title}`)
}

function viewHistory(row) {
  selectedDevice.value = row.deviceId || 1
  diagnosisResult.value = {
    healthScore: row.healthScore,
    level: row.level,
    status: row.status,
    summary: row.summary,
    costTime: 350,
    analysis: [
      {
        title: '历史诊断分析',
        severity: row.level === 4 || row.level === 3 ? 'high' : row.level === 2 ? 'medium' : 'low',
        description: row.summary,
        evidence: '基于历史数据综合分析'
      }
    ],
    suggestions: [
      {
        title: '定期巡检',
        urgent: false,
        actionable: false,
        content: '建议每周进行一次设备巡检'
      }
    ]
  }
}
</script>

<style scoped>
.diagnosis-container {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-actions {
  display: flex;
  gap: 10px;
}

.param-section {
  padding: 16px;
  background: #f5f7fa;
  border-radius: 8px;
  margin-bottom: 20px;
}

.param-section h4, .quick-actions h4 {
  margin: 0 0 16px 0;
  font-size: 14px;
  color: #303133;
}

.unit {
  margin-left: 8px;
  color: #909399;
}

.quick-actions {
  padding: 16px;
  background: #f5f7fa;
  border-radius: 8px;
}

.action-grid {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.empty-result {
  padding: 60px;
  text-align: center;
}

.diagnosis-result {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.result-overview {
  border-left: 4px solid #409eff;
}

.result-overview.level-1 { border-left-color: #67c23a; }
.result-overview.level-2 { border-left-color: #e6a23c; }
.result-overview.level-3 { border-left-color: #f56c6c; }
.result-overview.level-4 { border-left-color: #c45656; }

.overview-header {
  display: flex;
  align-items: center;
  gap: 24px;
}

.score-text {
  text-align: center;
}

.score-value {
  display: block;
  font-size: 28px;
  font-weight: 700;
  color: #303133;
}

.score-label {
  font-size: 12px;
  color: #909399;
}

.overview-info h3 {
  margin: 0 0 8px 0;
  font-size: 20px;
}

.overview-desc {
  margin: 0 0 12px 0;
  color: #606266;
}

.overview-tags {
  display: flex;
  gap: 8px;
}

.analysis-item {
  padding: 16px;
  background: #f5f7fa;
  border-radius: 8px;
  margin-bottom: 12px;
}

.analysis-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 8px;
}

.analysis-index {
  width: 24px;
  height: 24px;
  background: #409eff;
  color: #fff;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
}

.analysis-title {
  flex: 1;
  font-weight: 500;
  color: #303133;
}

.analysis-content {
  margin-bottom: 8px;
  line-height: 1.6;
  color: #606266;
}

.analysis-evidence {
  font-size: 12px;
  color: #909399;
}

.evidence-label {
  font-weight: 500;
}

.suggestion-item {
  padding-bottom: 8px;
}

.suggestion-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 8px;
}

.suggestion-title {
  font-weight: 500;
  color: #303133;
}

.suggestion-content {
  margin-bottom: 12px;
  line-height: 1.6;
  color: #606266;
}

.history-chart {
  height: 300px;
}

.history-card {
  margin-top: 20px;
}
</style>
