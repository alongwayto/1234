import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { login as loginApi, getInfo } from '@/api/auth'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const userInfo = ref(JSON.parse(localStorage.getItem('userInfo') || 'null'))

  const isLoggedIn = computed(() => !!token.value)
  const roles = computed(() => userInfo.value?.roles || [])
  const permissions = computed(() => userInfo.value?.permissions || [])
  const username = computed(() => userInfo.value?.username || '')
  const avatar = computed(() => userInfo.value?.avatar || '')

  async function login(username, password) {
    const res = await loginApi({ username, password })
    token.value = res.data.token
    userInfo.value = res.data.user
    localStorage.setItem('token', token.value)
    localStorage.setItem('userInfo', JSON.stringify(userInfo.value))
  }

  async function fetchUserInfo() {
    const res = await getInfo()
    userInfo.value = res.data
    localStorage.setItem('userInfo', JSON.stringify(userInfo.value))
  }

  function logout() {
    token.value = ''
    userInfo.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
  }

  function hasPermission(perm) {
    return roles.value.includes('ROLE_ADMIN') || permissions.value.includes(perm)
  }

  return { token, userInfo, isLoggedIn, roles, permissions, username, avatar, login, fetchUserInfo, logout, hasPermission }
})
