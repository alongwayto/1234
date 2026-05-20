import request from '@/utils/request'

export const getDevicePage = (params) => request.get('/device', { params })
export const getDeviceById = (id) => request.get(`/device/${id}`)
export const createDevice = (data) => request.post('/device', data)
export const updateDevice = (id, data) => request.put(`/device/${id}`, data)
export const deleteDevice = (id) => request.delete(`/device/${id}`)
export const batchDeleteDevices = (ids) => request.delete('/device/batch', { data: ids })
export const importDevices = (formData) => request.post('/device/import', formData, { headers: { 'Content-Type': 'multipart/form-data' } })
export const downloadImportTemplate = () => request.get('/device/import-template', { responseType: 'blob' })
export const exportDevices = (params) => request.get('/device/export', { params, responseType: 'blob' })
export const updateOnlineStatus = (id, onlineStatus) => request.put(`/device/${id}/online-status`, null, { params: { onlineStatus } })

// 分类
export const getCategories = () => request.get('/device/categories')
export const createCategory = (data) => request.post('/device/categories', data)
export const updateCategory = (id, data) => request.put(`/device/categories/${id}`, data)
export const deleteCategory = (id) => request.delete(`/device/categories/${id}`)

// 位置
export const getLocations = () => request.get('/device/locations')
export const createLocation = (data) => request.post('/device/locations', data)
export const updateLocation = (id, data) => request.put(`/device/locations/${id}`, data)
export const deleteLocation = (id) => request.delete(`/device/locations/${id}`)
