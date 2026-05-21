<template>
  <div class="ai-chat-container">
    <!-- 侧边栏：会话列表 -->
    <div class="chat-sidebar" :class="{ 'is-collapsed': sidebarCollapsed }">
      <div class="sidebar-header">
        <h3>AI 智能助手</h3>
        <el-button text @click="sidebarCollapsed = !sidebarCollapsed">
          <el-icon><component :is="sidebarCollapsed ? 'Expand' : 'Fold'" /></el-icon>
        </el-button>
      </div>
      
      <div v-if="!sidebarCollapsed" class="session-list">
        <el-button type="primary" class="new-session-btn" @click="createNewSession">
          <el-icon><Plus /></el-icon>
          新建会话
        </el-button>
        
        <div class="session-items">
          <div
            v-for="session in sessions"
            :key="session.id"
            class="session-item"
            :class="{ 'is-active': currentSession?.id === session.id }"
            @click="selectSession(session)"
          >
            <div class="session-info">
              <span class="session-title">{{ session.title || '新会话' }}</span>
              <span class="session-time">{{ formatTime(session.lastActiveTime) }}</span>
            </div>
            <el-button text size="small" @click.stop="deleteSession(session)">
              <el-icon><Delete /></el-icon>
            </el-button>
          </div>
        </div>
      </div>
    </div>

    <!-- 主聊天区域 -->
    <div class="chat-main">
      <!-- 顶部工具栏 -->
      <div class="chat-header">
        <div class="header-info">
          <span class="session-type">{{ currentSessionTypeText }}</span>
          <span v-if="currentSession" class="message-count">{{ messages.length }} 条消息</span>
        </div>
        <div class="header-actions">
          <el-button text @click="clearMessages">
            <el-icon><Delete /></el-icon>
            清空
          </el-button>
        </div>
      </div>

      <!-- 消息列表 -->
      <div class="message-list" ref="messageListRef">
        <!-- 欢迎消息 -->
        <div v-if="messages.length === 0" class="welcome-message">
          <div class="welcome-icon">
            <el-icon :size="48"><ChatDotRound /></el-icon>
          </div>
          <h3>您好，我是智能设备助手</h3>
          <p>我可以帮您：</p>
          <ul>
            <li>解答设备相关问题</li>
            <li>提供故障排查指导</li>
            <li>生成维护建议</li>
            <li>引导报修流程</li>
          </ul>
          <div class="quick-actions">
            <el-tag v-for="q in quickQuestions" :key="q" @click="sendQuickQuestion(q)" class="quick-tag">
              {{ q }}
            </el-tag>
          </div>
        </div>

        <!-- 消息 -->
        <div v-else>
          <div
            v-for="(msg, index) in messages"
            :key="index"
            class="message-item"
            :class="{ 'is-user': msg.role === 'user', 'is-assistant': msg.role === 'assistant' }"
          >
            <div class="message-avatar">
              <el-avatar :size="36" :icon="msg.role === 'user' ? 'User' : 'ChatDotRound'" />
            </div>
            <div class="message-content">
              <div class="message-text" v-html="formatMessage(msg.content)"></div>
              <div class="message-meta">
                <span class="message-time">{{ formatTime(msg.createTime) }}</span>
                <span v-if="msg.costTime" class="message-cost">{{ msg.costTime }}ms</span>
              </div>
            </div>
          </div>
        </div>

        <!-- 加载中 -->
        <div v-if="loading" class="loading-indicator">
          <el-icon class="is-loading"><Loading /></el-icon>
          <span>AI 正在思考...</span>
        </div>
      </div>

      <!-- 输入区域 -->
      <div class="chat-input-area">
        <div class="input-container">
          <el-input
            v-model="inputMessage"
            type="textarea"
            :rows="2"
            placeholder="请输入您的问题..."
            resize="none"
            @keydown.enter.ctrl="sendMessage"
          />
          <div class="input-actions">
            <span class="hint">Ctrl + Enter 发送</span>
            <el-button type="primary" :loading="loading" @click="sendMessage">
              <el-icon><Promotion /></el-icon>
              发送
            </el-button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, nextTick, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ChatDotRound, Plus, Delete, Promotion, Loading, Expand, Fold } from '@element-plus/icons-vue'
import { useUserStore } from '@/store/user'

const userStore = useUserStore()

// 状态
const sidebarCollapsed = ref(false)
const sessions = ref([])
const currentSession = ref(null)
const messages = ref([])
const inputMessage = ref('')
const loading = ref(false)
const messageListRef = ref(null)

// 快捷问题
const quickQuestions = [
  '设备故障了怎么办？',
  '如何申请设备维修？',
  '设备维护周期是多久？',
  '报修流程是什么？'
]

const currentSessionTypeText = computed(() => {
  if (!currentSession.value) return 'AI 智能助手'
  const types = {
    general: '通用问答',
    device: '设备咨询',
    fault: '故障咨询'
  }
  return types[currentSession.value.sessionType] || 'AI 助手'
})

onMounted(() => {
  loadSessions()
})

// 加载会话列表
async function loadSessions() {
  // 模拟加载会话
  // 实际调用: await getSessions(userStore.userInfo?.id)
}

// 创建新会话
async function createNewSession() {
  const session = {
    id: Date.now(),
    title: '新会话',
    sessionType: 'general',
    messageCount: 0,
    lastActiveTime: new Date()
  }
  sessions.value.unshift(session)
  currentSession.value = session
  messages.value = []
}

// 选择会话
async function selectSession(session) {
  currentSession.value = session
  // 加载该会话的消息
  // 实际调用: await getMessages(session.id)
  messages.value = []
}

// 删除会话
async function deleteSession(session) {
  try {
    await ElMessageBox.confirm('确定要删除该会话吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    sessions.value = sessions.value.filter(s => s.id !== session.id)
    if (currentSession.value?.id === session.id) {
      currentSession.value = null
      messages.value = []
    }
    ElMessage.success('删除成功')
  } catch {
    // 取消删除
  }
}

// 发送消息
async function sendMessage() {
  if (!inputMessage.value.trim()) return
  if (!currentSession.value) {
    createNewSession()
  }

  const content = inputMessage.value.trim()
  inputMessage.value = ''
  
  // 添加用户消息
  messages.value.push({
    role: 'user',
    content: content,
    createTime: new Date()
  })
  
  scrollToBottom()
  loading.value = true

  try {
    // 模拟AI回复
    await new Promise(resolve => setTimeout(resolve, 1000))
    
    const responses = [
      '根据您的问题，我来为您解答。首先请确认设备当前的状态指示灯是否正常。',
      '这是一个常见的问题。让我分析一下可能的原因：\n1. 电源连接是否正常\n2. 网络是否畅通\n3. 设备是否在保修期内\n\n建议您先检查这些基本项。',
      '好的，我已经了解了您的情况。建议您按以下步骤操作：\n1. 重启设备\n2. 检查连接线\n3. 如仍有问题，请提交维修工单。'
    ]
    
    const randomResponse = responses[Math.floor(Math.random() * responses.length)]
    
    messages.value.push({
      role: 'assistant',
      content: randomResponse,
      createTime: new Date(),
      costTime: Math.floor(Math.random() * 500) + 200
    })
    
    // 更新会话标题
    if (currentSession.value) {
      currentSession.value.title = content.substring(0, 20) + (content.length > 20 ? '...' : '')
      currentSession.value.messageCount++
    }
    
    scrollToBottom()
  } catch (error) {
    console.error('发送消息失败:', error)
    ElMessage.error('发送失败，请重试')
  } finally {
    loading.value = false
  }
}

// 发送快捷问题
function sendQuickQuestion(question) {
  if (!currentSession.value) {
    createNewSession()
  }
  inputMessage.value = question
  sendMessage()
}

// 清空消息
function clearMessages() {
  messages.value = []
  if (currentSession.value) {
    currentSession.value.messageCount = 0
  }
}

// 格式化消息（支持简单的Markdown）
function formatMessage(content) {
  if (!content) return ''
  return content
    .replace(/\n/g, '<br>')
    .replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')
    .replace(/\*(.*?)\*/g, '<em>$1</em>')
    .replace(/`(.*?)`/g, '<code>$1</code>')
}

// 格式化时间
function formatTime(time) {
  if (!time) return ''
  const date = new Date(time)
  const now = new Date()
  const diff = now - date
  
  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return `${Math.floor(diff / 60000)}分钟前`
  if (diff < 86400000) return `${Math.floor(diff / 3600000)}小时前`
  
  return date.toLocaleString('zh-CN', {
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

// 滚动到底部
function scrollToBottom() {
  nextTick(() => {
    if (messageListRef.value) {
      messageListRef.value.scrollTop = messageListRef.value.scrollHeight
    }
  })
}
</script>

<style scoped>
.ai-chat-container {
  display: flex;
  height: calc(100vh - 84px);
  background: #f5f7fa;
}

.chat-sidebar {
  width: 280px;
  background: #fff;
  border-right: 1px solid #e4e7ed;
  display: flex;
  flex-direction: column;
  transition: width 0.3s;
}

.chat-sidebar.is-collapsed {
  width: 60px;
}

.sidebar-header {
  padding: 16px;
  border-bottom: 1px solid #e4e7ed;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.sidebar-header h3 {
  margin: 0;
  font-size: 16px;
  color: #303133;
}

.session-list {
  flex: 1;
  overflow-y: auto;
  padding: 12px;
}

.new-session-btn {
  width: 100%;
  margin-bottom: 12px;
}

.session-items {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.session-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 12px;
  border-radius: 8px;
  cursor: pointer;
  transition: background 0.2s;
}

.session-item:hover {
  background: #f5f7fa;
}

.session-item.is-active {
  background: #ecf5ff;
  color: #409eff;
}

.session-info {
  flex: 1;
  min-width: 0;
}

.session-title {
  display: block;
  font-size: 14px;
  color: #303133;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.session-time {
  font-size: 12px;
  color: #909399;
}

.chat-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.chat-header {
  padding: 12px 20px;
  background: #fff;
  border-bottom: 1px solid #e4e7ed;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.session-type {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
}

.message-count {
  font-size: 12px;
  color: #909399;
}

.message-list {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
}

.welcome-message {
  text-align: center;
  padding: 60px 20px;
  color: #606266;
}

.welcome-icon {
  margin-bottom: 20px;
  color: #409eff;
}

.welcome-message h3 {
  margin: 0 0 16px 0;
  color: #303133;
}

.welcome-message ul {
  text-align: left;
  max-width: 300px;
  margin: 16px auto;
  padding-left: 20px;
}

.welcome-message li {
  margin: 8px 0;
}

.quick-actions {
  margin-top: 24px;
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  justify-content: center;
}

.quick-tag {
  cursor: pointer;
}

.message-item {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;
}

.message-item.is-user {
  flex-direction: row-reverse;
}

.message-content {
  max-width: 70%;
}

.message-text {
  padding: 12px 16px;
  border-radius: 12px;
  line-height: 1.6;
  white-space: pre-wrap;
}

.is-user .message-text {
  background: #409eff;
  color: #fff;
  border-bottom-right-radius: 4px;
}

.is-assistant .message-text {
  background: #fff;
  color: #303133;
  border-bottom-left-radius: 4px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.message-meta {
  display: flex;
  gap: 8px;
  margin-top: 4px;
  font-size: 12px;
  color: #909399;
}

.is-user .message-meta {
  justify-content: flex-end;
}

.loading-indicator {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 20px;
  color: #909399;
}

.chat-input-area {
  padding: 16px 20px;
  background: #fff;
  border-top: 1px solid #e4e7ed;
}

.input-container {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.input-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.hint {
  font-size: 12px;
  color: #909399;
}
</style>
