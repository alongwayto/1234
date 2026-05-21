/**
 * WebSocket 服务 - 设备状态实时监控
 * 基于 STOMP 协议的消息推送
 */

import SockJS from 'sockjs-client'
import { Stomp } from '@stomp/stompjs'
import { ElMessage } from 'element-plus'

class WebSocketService {
  constructor() {
    this.stompClient = null
    this.connected = false
    this.subscriptions = new Map()
    this.reconnectAttempts = 0
    this.maxReconnectAttempts = 5
    this.reconnectDelay = 3000
    this.heartbeatTimer = null
    this.listeners = {
      deviceStatus: [],
      alert: [],
      workOrder: [],
      fault: [],
      system: [],
      heartbeat: []
    }
  }

  /**
   * 建立 WebSocket 连接
   */
  connect(userId) {
    return new Promise((resolve, reject) => {
      if (this.connected && this.stompClient) {
        resolve()
        return
      }

      // 优先使用环境变量中的地址
      const wsUrl = import.meta.env.VITE_WS_URL || 'http://localhost:8080/ws/equipment'
      const socket = SockJS.create({
        url: wsUrl,
        headers: { userId: userId },
        transports: ['websocket', 'xhr-streaming', 'xhr-polling']
      })

      this.stompClient = Stomp.over(socket)

      // 关闭调试模式（生产环境）
      if (import.meta.env.PROD) {
        this.stompClient.debug = () => {}
      }

      this.stompClient.connect(
        { userId: userId },
        (frame) => {
          this.connected = true
          this.reconnectAttempts = 0
          console.log('WebSocket 连接成功:', frame)
          ElMessage.success('实时连接已建立')
          this.startHeartbeat()
          this.resubscribeAll()
          resolve()
        },
        (error) => {
          console.error('WebSocket 连接失败:', error)
          this.connected = false
          this.stopHeartbeat()
          this.handleReconnect(userId)
          reject(error)
        }
      )

      // 断开连接监听
      socket.onclose = () => {
        console.log('WebSocket 连接关闭')
        this.connected = false
        this.stopHeartbeat()
      }
    })
  }

  /**
   * 断开连接
   */
  disconnect() {
    this.stopHeartbeat()
    if (this.stompClient) {
      this.stompClient.disconnect()
      this.stompClient = null
    }
    this.connected = false
    this.subscriptions.clear()
    console.log('WebSocket 已断开')
  }

  /**
   * 重新连接
   */
  handleReconnect(userId) {
    if (this.reconnectAttempts < this.maxReconnectAttempts) {
      this.reconnectAttempts++
      console.log(`尝试重新连接 (${this.reconnectAttempts}/${this.maxReconnectAttempts})...`)
      setTimeout(() => {
        this.connect(userId)
      }, this.reconnectDelay)
    } else {
      ElMessage.error('实时连接断开，请刷新页面重试')
    }
  }

  /**
   * 启动心跳
   */
  startHeartbeat() {
    this.heartbeatTimer = setInterval(() => {
      if (this.connected && this.stompClient) {
        this.stompClient.send('/app/heartbeat', {}, JSON.stringify({ timestamp: Date.now() }))
      }
    }, 30000) // 每30秒发送一次心跳
  }

  /**
   * 停止心跳
   */
  stopHeartbeat() {
    if (this.heartbeatTimer) {
      clearInterval(this.heartbeatTimer)
      this.heartbeatTimer = null
    }
  }

  /**
   * 订阅设备状态更新
   */
  subscribeDeviceStatus(deviceId, callback) {
    const topic = deviceId ? `/topic/device/status/${deviceId}` : '/topic/device/status/all'
    return this.subscribe(topic, (message) => {
      this.notifyListeners('deviceStatus', message)
      callback && callback(message)
    })
  }

  /**
   * 订阅设备状态概览
   */
  subscribeDeviceOverview(callback) {
    return this.subscribe('/topic/device/overview', (message) => {
      this.notifyListeners('deviceStatus', message)
      callback && callback(message)
    })
  }

  /**
   * 订阅新预警
   */
  subscribeNewAlert(callback) {
    return this.subscribe('/topic/alert/new', (message) => {
      this.notifyListeners('alert', message)
      ElMessage.warning({
        message: `收到新预警: ${message.alertMsg || message.alert_type}`,
        duration: 5000
      })
      callback && callback(message)
    })
  }

  /**
   * 订阅预警数量更新
   */
  subscribeAlertCount(callback) {
    return this.subscribe('/topic/alert/count', (count) => {
      this.notifyListeners('alert', { type: 'count', count })
      callback && callback(count)
    })
  }

  /**
   * 订阅新工单
   */
  subscribeNewWorkOrder(callback) {
    return this.subscribe('/topic/workorder/new', (message) => {
      this.notifyListeners('workOrder', message)
      ElMessage.info({
        message: `收到新工单: ${message.orderNo}`,
        duration: 5000
      })
      callback && callback(message)
    })
  }

  /**
   * 订阅新故障
   */
  subscribeNewFault(callback) {
    return this.subscribe('/topic/fault/new', (message) => {
      this.notifyListeners('fault', message)
      ElMessage.warning({
        message: `收到故障上报: ${message.faultNo}`,
        duration: 5000
      })
      callback && callback(message)
    })
  }

  /**
   * 订阅系统广播
   */
  subscribeSystemBroadcast(callback) {
    return this.subscribe('/topic/system/broadcast', (message) => {
      this.notifyListeners('system', message)
      callback && callback(message)
    })
  }

  /**
   * 订阅在线人数
   */
  subscribeOnlineCount(callback) {
    return this.subscribe('/topic/online/count', (count) => {
      callback && callback(count)
    })
  }

  /**
   * 订阅 AI 诊断结果
   */
  subscribeAIDiagnosis(sessionId, callback) {
    return this.subscribe(`/topic/ai/diagnosis/${sessionId}`, (message) => {
      callback && callback(message)
    })
  }

  /**
   * 通用订阅方法
   */
  subscribe(topic, callback) {
    if (!this.connected || !this.stompClient) {
      console.warn('WebSocket 未连接，无法订阅:', topic)
      return null
    }

    // 如果已订阅，先取消
    if (this.subscriptions.has(topic)) {
      this.unsubscribe(topic)
    }

    const subscription = this.stompClient.subscribe(topic, (response) => {
      try {
        const message = JSON.parse(response.body)
        callback(message)
      } catch (e) {
        callback(response.body)
      }
    })

    this.subscriptions.set(topic, subscription)
    console.log('已订阅:', topic)
    return subscription
  }

  /**
   * 取消订阅
   */
  unsubscribe(topic) {
    if (this.subscriptions.has(topic)) {
      this.subscriptions.get(topic).unsubscribe()
      this.subscriptions.delete(topic)
      console.log('已取消订阅:', topic)
    }
  }

  /**
   * 重新订阅所有主题
   */
  resubscribeAll() {
    this.subscriptions.forEach((sub, topic) => {
      // 重新订阅（通过回调）
    })
  }

  /**
   * 发送设备状态上报
   */
  sendDeviceReport(deviceData) {
    if (this.connected && this.stompClient) {
      this.stompClient.send('/app/device/report', {}, JSON.stringify(deviceData))
    }
  }

  /**
   * 注册通用监听器
   */
  addListener(type, callback) {
    if (this.listeners[type]) {
      this.listeners[type].push(callback)
    }
  }

  /**
   * 移除监听器
   */
  removeListener(type, callback) {
    if (this.listeners[type]) {
      this.listeners[type] = this.listeners[type].filter(cb => cb !== callback)
    }
  }

  /**
   * 通知所有监听器
   */
  notifyListeners(type, message) {
    if (this.listeners[type]) {
      this.listeners[type].forEach(callback => {
        try {
          callback(message)
        } catch (e) {
          console.error('监听器执行错误:', e)
        }
      })
    }
  }

  /**
   * 获取连接状态
   */
  isConnected() {
    return this.connected
  }
}

// 导出单例
export const wsService = new WebSocketService()

export default wsService
