import request from '@/utils/request'

export const getFaultPage = (params) => request.get('/fault', { params })
export const getFaultById = (id) => request.get(`/fault/${id}`)
export const createFault = (data) => request.post('/fault', data)
export const assignWorkOrder = (data) => request.post('/fault/assign', data)
export const importHistoricalWorkOrder = (data) => request.post('/fault/workorder/import', data)
export const startHandle = (id) => request.post(`/fault/workorder/${id}/start`)
export const completeHandle = (data) => request.post('/fault/workorder/complete', data)
export const archiveFault = (id) => request.post(`/fault/${id}/archive`)
export const evaluateWorkOrder = (id, data) => request.post(`/fault/workorder/${id}/evaluate`, data)
export const getWorkOrderPage = (params) => request.get('/fault/workorders', { params })
export const getWorkOrderByFaultId = (faultId) => request.get(`/fault/${faultId}/workorder`)
