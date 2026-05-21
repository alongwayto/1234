<template>
  <div class="predict-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>预测性维护</span>
          <div class="header-actions">
            <el-select v-model="queryParams.riskLevel" placeholder="风险等级" clearable style="width: 120px">
              <el-option label="低风险" :value="1" />
              <el-option label="中风险" :value="2" />
              <el-option label="高风险" :value="3" />
              <el-option label="紧急" :value="4" />
            </el-select>
            <el-select v-model="queryParams.status" placeholder="处理状态" clearable style="width: 120px">
              <el-option label="未处理" :value="0" />
              <el-option label="已处理" :value="1" />
              <el-option label="已忽略" :value="2" />
            </el-select>
            <el-button type="primary" @click="handleQuery">
              <el-icon><Search /></el-icon>
              查询
            </el-button>
            <el-button type="primary" @click="runPrediction">
              <el-icon><Refresh /></el-icon>
              重新预测
            </el-button>
          </div>
        </div>
      </template>

      <!-- 风险统计 -->
      <el-row :gutter="20" class="risk-stats">
        <el-col :span="6">
          <div class="risk-card level-1">
            <div class="risk-count">{{ riskStats.low }}</div>
            <div class="risk-label">低风险</div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="risk-card level-2">
            <div class="risk-count">{{ riskStats.medium }}</div>
            <div class="risk-label">中风险</div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="risk-card level-3">
            <div class="risk-count">{{ riskStats.high }}</div>
            <div class="risk-label">高风险</div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="risk-card level-4">
            <div class="risk-count">{{ riskStats.critical }}</div>
            <div class="risk-label">紧急</div>
          </div>
        </el-col>
      </el-row>

      <!-- 预测列表 -->
      <el-table :data="tableData" stripe v-loading="loading">
        <el-table-column type="index" label="序号" width="60" />
        <el-table-column prop="deviceName" label="设备名称" min-width="150" />
        <el-table-column prop="deviceCode" label="设备编号" width="120" />
        <el-table-column prop="predictType" label="预测类型" width="100">
          <template #default="{ row }">
            <el-tag>{{ getPredictTypeText(row.predictType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="riskLevel" label="风险等级" width="100">
          <template #default="{ row }">
            <el-tag :type="getRiskType(row.riskLevel)">
              {{ getRiskText(row.riskLevel) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="probability" label="发生概率" width="120">
          <template #default="{ row }">
            <el-progress :percentage="row.probability" :color="getProgressColor(row.probability)" :stroke-width="10" />
          </template>
        </el-table-column>
        <el-table-column prop="predictTime" label="预测时间" width="120" />
        <el-table-column prop="description" label="预测描述" min-width="200" show-overflow-tooltip />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.status === 0" type="warning">未处理</el-tag>
            <el-tag v-else-if="row.status === 1" type="success">已处理</el-tag>
            <el-tag v-else type="info">已忽略</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleView(row)">详情</el-button>
            <el-button link type="success" v-if="row.status === 0" @click="handleProcess(row)">处理</el-button>
            <el-dropdown v-if="row.status === 0">
              <el-button link type="primary">
                更多 <el-icon class="el-icon--right"><ArrowDown /></el-icon>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item @click="handleIgnore(row)">忽略</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="pagination.current"
        v-model:page-size="pagination.size"
        :total="pagination.total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        @size-change="handleQuery"
        @current-change="handleQuery"
        style="margin-top: 20px; justify-content: flex-end"
      />
    </el-card>

    <!-- 详情对话框 -->
    <el-dialog v-model="viewVisible" title="预测详情" width="700px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="设备名称">{{ viewData.deviceName }}</el-descriptions-item>
        <el-descriptions-item label="设备编号">{{ viewData.deviceCode }}</el-descriptions-item>
        <el-descriptions-item label="预测类型">{{ getPredictTypeText(viewData.predictType) }}</el-descriptions-item>
        <el-descriptions-item label="风险等级">
          <el-tag :type="getRiskType(viewData.riskLevel)">{{ getRiskText(viewData.riskLevel) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="发生概率">{{ viewData.probability }}%</el-descriptions-item>
        <el-descriptions-item label="预测时间">{{ viewData.predictTime }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag v-if="viewData.status === 0" type="warning">未处理</el-tag>
          <el-tag v-else-if="viewData.status === 1" type="success">已处理</el-tag>
          <el-tag v-else type="info">已忽略</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ viewData.createTime }}</el-descriptions-item>
        <el-descriptions-item label="预测描述" :span="2">{{ viewData.description }}</el-descriptions-item>
        <el-descriptions-item label="建议措施" :span="2">
          <div style="white-space: pre-wrap">{{ viewData.suggestion }}</div>
        </el-descriptions-item>
        <el-descriptions-item label="AI分析依据" :span="2">
          <div style="white-space: pre-wrap; color: #909399">{{ viewData.analysisBasis }}</div>
        </el-descriptions-item>
        <el-descriptions-item label="处理人" v-if="viewData.handler">{{ viewData.handler }}</el-descriptions-item>
        <el-descriptions-item label="处理时间" v-if="viewData.handleTime">{{ viewData.handleTime }}</el-descriptions-item>
        <el-descriptions-item label="处理备注" :span="2" v-if="viewData.handleRemark">{{ viewData.handleRemark }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>

    <!-- 处理对话框 -->
    <el-dialog v-model="processVisible" title="处理预测" width="500px">
      <el-form :model="processForm" label-width="100px">
        <el-form-item label="处理方式">
          <el-radio-group v-model="processForm.action">
            <el-radio label="process">处理完成</el-radio>
            <el-radio label="ignore">忽略</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="处理备注" v-if="processForm.action === 'process'">
          <el-input v-model="processForm.remark" type="textarea" :rows="3" placeholder="请输入处理备注" />
        </el-form-item>
        <el-form-item label="忽略原因" v-else>
          <el-input v-model="processForm.remark" type="textarea" :rows="3" placeholder="请输入忽略原因" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="processVisible = false">取消</el-button>
        <el-button type="primary" @click="submitProcess" :loading="submitting">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, ArrowDown } from '@element-plus/icons-vue'

const loading = ref(false)
const tableData = ref([])
const viewVisible = ref(false)
const processVisible = ref(false)
const submitting = ref(false)
const viewData = ref({})

const queryParams = reactive({
  riskLevel: null,
  status: 0
})

const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

const riskStats = reactive({
  low: 15,
  medium: 8,
  high: 3,
  critical: 1
})

const processForm = reactive({
  id: null,
  action: 'process',
  remark: ''
})

const predictTypeMap = {
  failure: '故障预测',
  wear: '磨损预测',
  performance: '性能预测'
}

function getPredictTypeText(type) {
  return predictTypeMap[type] || type
}

function getRiskText(level) {
  const map = { 1: '低风险', 2: '中风险', 3: '高风险', 4: '紧急' }
  return map[level] || '未知'
}

function getRiskType(level) {
  const map = { 1: 'success', 2: 'warning', 3: 'danger', 4: 'danger' }
  return map[level] || 'info'
}

function getProgressColor(percentage) {
  if (percentage >= 70) return '#f56c6c'
  if (percentage >= 40) return '#e6a23c'
  return '#67c23a'
}

async function loadData() {
  loading.value = true
  try {
    // 模拟数据
    tableData.value = [
      {
        id: 1,
        deviceId: 1,
        deviceName: '核心交换机',
        deviceCode: 'DEV001',
        predictType: 'failure',
        riskLevel: 3,
        probability: 65,
        predictTime: '2024-04-15',
        description: '基于最近30天的CPU使用率和温度数据分析，设备可能在30天内出现性能下降',
        suggestion: '1. 建议本周内进行一次全面清理和固件升级\n2. 准备备用设备以应对突发故障\n3. 加强监控频率',
        analysisBasis: 'CPU平均使用率从45%上升至62%，温度从38℃上升至45℃，呈上升趋势',
        status: 0,
        createTime: '2024-03-15 10:30:00'
      },
      {
        id: 2,
        deviceId: 2,
        deviceName: '服务器集群',
        deviceCode: 'DEV002',
        predictType: 'wear',
        riskLevel: 2,
        probability: 35,
        predictTime: '2024-06-15',
        description: '硬盘写入量接近设计寿命，建议关注',
        suggestion: '1. 建议在60天内进行硬盘健康检查\n2. 考虑添加新硬盘扩容\n3. 备份重要数据',
        analysisBasis: '硬盘累计写入量已达设计寿命的78%',
        status: 0,
        createTime: '2024-03-10 14:20:00'
      }
    ]
    pagination.total = 2
  } finally {
    loading.value = false
  }
}

function handleQuery() {
  pagination.current = 1
  loadData()
}

async function runPrediction() {
  try {
    await ElMessageBox.confirm('确定要重新运行预测分析吗？这将基于最新的设备数据生成新的预测结果。', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'info'
    })
    ElMessage.success('预测分析已触发，请稍后查看结果')
  } catch {
    // 取消
  }
}

function handleView(row) {
  viewData.value = { ...row }
  viewVisible.value = true
}

function handleProcess(row) {
  processForm.id = row.id
  processForm.action = 'process'
  processForm.remark = ''
  processVisible.value = true
}

async function handleIgnore(row) {
  try {
    await ElMessageBox.confirm('确定要忽略此预测吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    // 调用忽略API
    ElMessage.success('已忽略')
    loadData()
  } catch {
    // 取消
  }
}

async function submitProcess() {
  if (!processForm.remark.trim()) {
    ElMessage.warning('请输入备注')
    return
  }
  submitting.value = true
  try {
    // 调用处理API
    ElMessage.success(processForm.action === 'process' ? '处理成功' : '已忽略')
    processVisible.value = false
    loadData()
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.predict-container {
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

.risk-stats {
  margin-bottom: 20px;
}

.risk-card {
  padding: 20px;
  border-radius: 8px;
  text-align: center;
  color: #fff;
}

.risk-card.level-1 { background: linear-gradient(135deg, #67c23a, #85ce61); }
.risk-card.level-2 { background: linear-gradient(135deg, #e6a23c, #ebb563); }
.risk-card.level-3 { background: linear-gradient(135deg, #f56c6c, #f78989); }
.risk-card.level-4 { background: linear-gradient(135deg, #c45656, #a83232); }

.risk-count {
  font-size: 32px;
  font-weight: 700;
}

.risk-label {
  font-size: 14px;
  margin-top: 4px;
  opacity: 0.9;
}
</style>
