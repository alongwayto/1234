import request from '@/utils/request'

// 获取所有会话
export function getChatSessions() {
  return request({
    url: '/api/ai/chat/sessions',
    method: 'get'
  })
}

// 创建新会话
export function createChatSession(data) {
  return request({
    url: '/api/ai/chat/sessions',
    method: 'post',
    data
  })
}

// 获取会话消息
export function getSessionMessages(sessionId) {
  return request({
    url: `/api/ai/chat/sessions/${sessionId}/messages`,
    method: 'get'
  })
}

// 删除会话
export function deleteChatSession(sessionId) {
  return request({
    url: `/api/ai/chat/sessions/${sessionId}`,
    method: 'delete'
  })
}

// 发送消息
export function sendMessage(data) {
  return request({
    url: '/api/ai/chat/send',
    method: 'post',
    data,
    timeout: 60000 // 1分钟超时
  })
}

// 流式发送消息
export function sendMessageStream(data, onMessage, onError, onComplete) {
  return request({
    url: '/api/ai/chat/send/stream',
    method: 'post',
    data,
    timeout: 60000,
    responseType: 'stream',
    onMessage,
    onError,
    onComplete
  })
}

// 获取AI配置
export function getAIConfig() {
  return request({
    url: '/api/ai/config',
    method: 'get'
  })
}

// 更新AI配置
export function updateAIConfig(data) {
  return request({
    url: '/api/ai/config',
    method: 'put',
    data
  })
}
