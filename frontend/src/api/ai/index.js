/**
 * AI 智能诊断 API
 */
import request from '@/utils/request'

/**
 * 诊断设备状态
 */
export function diagnoseDevice(deviceId) {
  return request({
    url: `/ai/diagnose/${deviceId}`,
    method: 'post'
  })
}

/**
 * 批量诊断设备
 */
export function batchDiagnose(deviceIds) {
  return request({
    url: '/ai/diagnose/batch',
    method: 'post',
    data: deviceIds
  })
}

/**
 * 生成维护建议
 */
export function generateAdvice(deviceId) {
  return request({
    url: `/ai/advice/${deviceId}`,
    method: 'post'
  })
}

/**
 * 预测故障风险
 */
export function predictRisk(deviceId) {
  return request({
    url: `/ai/risk/${deviceId}`,
    method: 'get'
  })
}

/**
 * AI 智能问答
 */
export function aiChat(question, context) {
  return request({
    url: '/ai/chat',
    method: 'post',
    data: { question, context }
  })
}

/**
 * 分析故障报告
 */
export function analyzeFault(data) {
  return request({
    url: '/ai/analyze-fault',
    method: 'post',
    data
  })
}
