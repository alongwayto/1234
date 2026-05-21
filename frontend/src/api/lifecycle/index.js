import request from '@/utils/request'

// ==================== 设备生命周期管理 ====================

/**
 * 分页查询生命周期记录
 */
export function getLifecycleList(params) {
  return request({
    url: '/api/device/lifecycle/list',
    method: 'get',
    params
  })
}

/**
 * 获取生命周期记录详情
 */
export function getLifecycleDetail(id) {
  return request({
    url: `/api/device/lifecycle/${id}`,
    method: 'get'
  })
}

/**
 * 添加生命周期记录
 */
export function addLifecycle(data) {
  return request({
    url: '/api/device/lifecycle',
    method: 'post',
    data
  })
}

/**
 * 更新生命周期记录
 */
export function updateLifecycle(data) {
  return request({
    url: '/api/device/lifecycle',
    method: 'put',
    data
  })
}

/**
 * 删除生命周期记录
 */
export function deleteLifecycle(id) {
  return request({
    url: `/api/device/lifecycle/${id}`,
    method: 'delete'
  })
}

/**
 * 获取设备的生命周期时间线
 */
export function getDeviceLifecycleTimeline(deviceId) {
  return request({
    url: `/api/device/lifecycle/timeline/${deviceId}`,
    method: 'get'
  })
}

/**
 * 导出生命周期记录
 */
export function exportLifecycle(params) {
  return request({
    url: '/api/device/lifecycle/export',
    method: 'get',
    params,
    responseType: 'blob'
  })
}

// ==================== 预测性维护 ====================

/**
 * 分页查询预测性维护记录
 */
export function getPredictiveMaintenanceList(params) {
  return request({
    url: '/api/predictive-maintenance/list',
    method: 'get',
    params
  })
}

/**
 * 获取预测性维护详情
 */
export function getPredictiveMaintenanceDetail(id) {
  return request({
    url: `/api/predictive-maintenance/${id}`,
    method: 'get'
  })
}

/**
 * 触发预测分析
 */
export function triggerPrediction(deviceId) {
  return request({
    url: `/api/predictive-maintenance/predict/${deviceId}`,
    method: 'post'
  })
}

/**
 * 批量触发预测分析
 */
export function triggerBatchPrediction() {
  return request({
    url: '/api/predictive-maintenance/predict/batch',
    method: 'post'
  })
}

/**
 * 处理预测（标记为已处理或忽略）
 */
export function handlePrediction(data) {
  return request({
    url: '/api/predictive-maintenance/handle',
    method: 'post',
    data
  })
}

/**
 * 获取预测统计
 */
export function getPredictionStatistics() {
  return request({
    url: '/api/predictive-maintenance/statistics',
    method: 'get'
  })
}

/**
 * 获取预测趋势
 */
export function getPredictionTrend(params) {
  return request({
    url: '/api/predictive-maintenance/trend',
    method: 'get',
    params
  })
}

/**
 * 创建预测相关的工单
 */
export function createWorkorderFromPrediction(predictiveId, data) {
  return request({
    url: `/api/predictive-maintenance/${predictiveId}/workorder`,
    method: 'post',
    data
  })
}

/**
 * 删除预测记录
 */
export function deletePrediction(id) {
  return request({
    url: `/api/predictive-maintenance/${id}`,
    method: 'delete'
  })
}
