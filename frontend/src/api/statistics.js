import request from '@/utils/request'

export const getOverview = () => request.get('/statistics/overview')
export const getDeviceStatusDist = () => request.get('/statistics/device-status-dist')
export const getFaultTrend = () => request.get('/statistics/fault-trend')
export const getFaultTypeDist = () => request.get('/statistics/fault-type-dist')
export const getCostMonthly = (options = 6) => {
  const params = typeof options === 'number' ? { months: options } : options
  return request.get('/statistics/cost-monthly', { params })
}
export const getDeviceUsage = () => request.get('/statistics/device-usage')
export const getFaultTopDevices = () => request.get('/statistics/fault-top-devices')
export const getOperationsDashboard = () => request.get('/statistics/operations')
