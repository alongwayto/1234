import request from '@/utils/request'

export const getUserPage = (params) => request.get('/system/users', { params })
export const getUserById = (id) => request.get(`/system/users/${id}`)
export const createUser = (data) => request.post('/system/users', data)
export const updateUser = (id, data) => request.put(`/system/users/${id}`, data)
export const deleteUser = (id) => request.delete(`/system/users/${id}`)
export const resetPassword = (id, password) => request.post(`/system/users/${id}/reset-password`, { password })
export const toggleUserStatus = (id) => request.post(`/system/users/${id}/toggle-status`)

export const getRoles = () => request.get('/system/roles')
export const getPermTree = () => request.get('/system/roles/permissions/tree')
export const getRolePerms = (id) => request.get(`/system/roles/${id}/permissions`)
export const createRole = (data) => request.post('/system/roles', data)
export const updateRole = (id, data) => request.put(`/system/roles/${id}`, data)
export const deleteRole = (id) => request.delete(`/system/roles/${id}`)

export const getLogPage = (params) => request.get('/system/logs', { params })
export const clearLogs = () => request.delete('/system/logs/clear')

export const getBackupList = (params) => request.get('/system/backup', { params })
export const manualBackup = () => request.post('/system/backup/manual')
export const downloadBackup = (id) => request.get(`/system/backup/${id}/download`, { responseType: 'blob' })
export const restoreBackup = (id) => request.post(`/system/backup/${id}/restore`)

export const getSystemDoc = (type) => request.get(`/system/docs/${type}`, { responseType: 'text' })
