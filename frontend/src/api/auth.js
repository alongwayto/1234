import request from '@/utils/request'

export const login = (data) => request.post('/auth/login', data)
export const logout = () => request.post('/auth/logout')
export const getInfo = () => request.get('/auth/info')
export const changePassword = (data) => request.post('/auth/change-password', data)
export const updateProfile = (data) => request.put('/auth/profile', data)
export const uploadAvatar = (formData) => request.post('/auth/avatar', formData, { headers: { 'Content-Type': 'multipart/form-data' } })
