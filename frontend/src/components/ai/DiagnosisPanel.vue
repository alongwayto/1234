<template>
  <div class="ai-diagnosis-panel">
    <el-card class="diagnosis-card">
      <template #header>
        <div class="card-header">
          <span>AI 智能诊断</span>
          <el-tag :type="aiStatusType" size="small">{{ aiStatusText }}</el-tag>
        </div>
      </template>
      
      <!-- 诊断类型选择 -->
      <el-radio-group v-model="diagnosisType" class="type-selector">
        <el-radio-button label="diagnosis">故障诊断</el-radio-button>
        <el-radio-button label="advice">维护建议</el-radio-button>
        <el-radio-button label="risk">风险预测</el-radio-button>
      </el-radio-group>

      <!-- 设备选择 -->
      <el-select
        v-model="selectedDeviceId"
        placeholder="选择要诊断的设备"
        filterable
        class="device-selector"
        @change="onDeviceChange"
      >
        <el-option
          v-for="device in deviceList"
          :key="device.id"
          :label="`${device.deviceCode} - ${device.deviceName}`"
          :value="device.id"
        />
      </el-select>

      <!-- 诊断结果展示 -->
      <div v-if="diagnosisResult" class="diagnosis-result">
        <!-- 紧急程度 -->
        <div class="urgency-indicator">
          <el-tag :type="urgencyType" size="large">
            紧急程度：{{ urgencyText }}
          </el-tag>
        </div>

        <!-- 诊断详情 -->
        <div class="result-section">
          <h4>诊断结果</h4>
          <div class="result-content">
            <p v-for="(item, index) in diagnosisResult" :key="index">{{ item }}</p>
          </div>
        </div>

        <!-- 维护建议 -->
        <div v-if="maintenanceAdvice" class="result-section">
          <h4>维护建议</h4>
          <div class="advice-content">
            <p>{{ maintenanceAdvice }}</p>
          </div>
        </div>

        <!-- 风险预测 -->
        <div v-if="riskPrediction" class="result-section">
          <h4>风险预测</h4>
          <div class="risk-content">
            <el-progress
              :percentage="riskPrediction.failureProbability"
              :color="riskColor"
              :stroke-width="20"
            />
            <p class="risk-analysis">{{ riskPrediction.analysis }}</p>
          </div>
        </div>
      </div>

      <!-- 空状态 -->
      <el-empty v-else-if="!loading" description="请选择设备进行AI诊断" :image-size="80" />

      <!-- 加载状态 -->
      <div v-if="loading" class="loading-state">
        <el-icon class="is-loading"><Loading /></el-icon>
        <span>AI 正在分析中...</span>
      </div>

      <!-- 操作按钮 -->
      <div class="action-buttons">
        <el-button type="primary" :loading="loading" @click="runDiagnosis">
          开始诊断
        </el-button>
        <el-button @click="resetDiagnosis">重置</el-button>
      </div>
    </el-card>

    <!-- 诊断历史 -->
    <el-card v-if="diagnosisHistory.length > 0" class="history-card">
      <template #header>
        <span>诊断历史</span>
      </template>
      <el-timeline>
        <el-timeline-item
          v-for="(item, index) in diagnosisHistory"
          :key="index"
          :timestamp="item.timestamp"
          placement="top"
        >
          <el-card>
            <h4>{{ item.deviceName }}</h4>
            <p>紧急程度：{{ getUrgencyText(item.urgencyLevel) }}</p>
            <p class="preview-text">{{ item.suggestions?.[0] || '无建议' }}</p>
          </el-card>
        </el-timeline-item>
      </el-timeline>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Loading } from '@element-plus/icons-vue'
import { diagnoseDevice, generateAdvice, predictRisk } from '@/api/ai'
import { listSimple } from '@/api/device'

const props = defineProps({
  deviceId: {
    type: Number,
    default: null
  }
})

const emit = defineEmits(['diagnosis-complete'])

const diagnosisType = ref('diagnosis')
const selectedDeviceId = ref(props.deviceId)
const deviceList = ref([])
const loading = ref(false)
const diagnosisResult = ref(null)
const maintenanceAdvice = ref(null)
const riskPrediction = ref(null)
const diagnosisHistory = ref([])

const aiStatusType = computed(() => {
  return loading.value ? 'warning' : 'success'
})

const aiStatusText = computed(() => {
  return loading.value ? '诊断中' : '就绪'
})

const urgencyType = computed(() => {
  const level = diagnosisResult.value?.urgencyLevel
  if (level >= 4) return 'danger'
  if (level >= 3) return 'warning'
  if (level >= 2) return 'info'
  return 'success'
})

const urgencyText = computed(() => {
  const level = diagnosisResult.value?.urgencyLevel
  if (level >= 4) return '紧急'
  if (level >= 3) return '高'
  if (level >= 2) return '中'
  return '低'
})

const riskColor = computed(() => {
  const prob = riskPrediction.value?.failureProbability || 0
  if (prob >= 70) return '#f56c6c'
  if (prob >= 40) return '#e6a23c'
  return '#67c23a'
})

onMounted(() => {
  loadDevices()
})

async function loadDevices() {
  try {
    const res = await listSimple()
    deviceList.value = res.data || []
  } catch (error) {
    console.error('加载设备列表失败:', error)
  }
}

function onDeviceChange(deviceId) {
  resetDiagnosis()
}

async function runDiagnosis() {
  if (!selectedDeviceId.value) {
    ElMessage.warning('请先选择设备')
    return
  }

  loading.value = true
  diagnosisResult.value = null
  maintenanceAdvice.value = null
  riskPrediction.value = null

  try {
    switch (diagnosisType.value) {
      case 'diagnosis':
        await runDeviceDiagnosis()
        break
      case 'advice':
        await runAdviceGeneration()
        break
      case 'risk':
        await runRiskPrediction()
        break
    }
  } catch (error) {
    console.error('诊断失败:', error)
    ElMessage.error('诊断失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

async function runDeviceDiagnosis() {
  const res = await diagnoseDevice(selectedDeviceId.value)
  if (res.data) {
    diagnosisResult.value = res.data.suggestions || []
    
    // 添加到历史
    const device = deviceList.value.find(d => d.id === selectedDeviceId.value)
    diagnosisHistory.value.unshift({
      deviceName: device?.deviceName || res.data.deviceName,
      deviceCode: res.data.deviceCode,
      urgencyLevel: res.data.urgencyLevel,
      suggestions: res.data.suggestions,
      timestamp: new Date().toLocaleString()
    })
    
    emit('diagnosis-complete', res.data)
    ElMessage.success('诊断完成')
  }
}

async function runAdviceGeneration() {
  const res = await generateAdvice(selectedDeviceId.value)
  if (res.data) {
    maintenanceAdvice.value = res.data
    ElMessage.success('建议生成完成')
  }
}

async function runRiskPrediction() {
  const res = await predictRisk(selectedDeviceId.value)
  if (res.data) {
    riskPrediction.value = res.data
    ElMessage.success('风险预测完成')
  }
}

function resetDiagnosis() {
  diagnosisResult.value = null
  maintenanceAdvice.value = null
  riskPrediction.value = null
}

function getUrgencyText(level) {
  if (level >= 4) return '紧急'
  if (level >= 3) return '高'
  if (level >= 2) return '中'
  return '低'
}
</script>

<style scoped>
.ai-diagnosis-panel {
  padding: 16px;
}

.diagnosis-card {
  margin-bottom: 16px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.type-selector {
  margin-bottom: 16px;
  display: block;
}

.device-selector {
  width: 100%;
  margin-bottom: 16px;
}

.diagnosis-result {
  margin: 20px 0;
}

.urgency-indicator {
  margin-bottom: 16px;
}

.result-section {
  margin-bottom: 16px;
  padding: 12px;
  background: #f5f7fa;
  border-radius: 8px;
}

.result-section h4 {
  margin: 0 0 12px 0;
  color: #303133;
  font-size: 14px;
}

.result-content p {
  margin: 8px 0;
  color: #606266;
  line-height: 1.6;
}

.advice-content p {
  color: #606266;
  line-height: 1.8;
  white-space: pre-wrap;
}

.risk-content {
  text-align: center;
}

.risk-analysis {
  margin-top: 12px;
  color: #606266;
}

.loading-state {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 40px;
  color: #909399;
}

.action-buttons {
  margin-top: 16px;
  text-align: center;
}

.history-card {
  margin-top: 16px;
}

.history-card h4 {
  margin: 0 0 8px 0;
  color: #303133;
}

.history-card p {
  margin: 4px 0;
  color: #606266;
}

.preview-text {
  font-size: 12px;
  color: #909399 !important;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
</style>
