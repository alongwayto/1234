import request from '@/utils/request'

export const reportStatus = (data) => request.post('/monitor/status/report', data)
export const getRecentStatus = (deviceId, limit = 20) => request.get(`/monitor/status/${deviceId}`, { params: { limit } })
export const getRealtimeMonitor = () => request.get('/monitor/realtime')
export const getAlertPage = (params) => request.get('/monitor/alerts', { params })
export const handleAlert = (id, data) => request.post(`/monitor/alerts/${id}/handle`, data)
export const getUnhandledCount = () => request.get('/monitor/alerts/unhandled-count')
