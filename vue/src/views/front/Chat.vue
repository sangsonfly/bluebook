<script setup>
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'
import {
  getConversation,
  getSessionList,
  getPrivateUnreadCount,
  markConversationRead,
  sendPrivateMessage
} from '@/api/privateMessage'

const router = useRouter()
const route = useRoute()

const account = computed(() => {
  const raw = localStorage.getItem('account')
  if (!raw) return null
  try {
    return JSON.parse(raw)
  } catch {
    return null
  }
})

const currentUserId = computed(() => account.value?.id || null)
const routeTargetId = computed(() => {
  const id = Number(route.params.targetUserId)
  return Number.isFinite(id) ? id : null
})

const loading = ref(false)
const sending = ref(false)
const sessionLoading = ref(false)
const sessions = ref([])
const currentSessionUserId = ref(null)
const messages = ref([])
const inputMessage = ref('')
const unreadTotal = ref(0)
const currentTargetUser = ref(null)

let pollTimer = null

const currentSession = computed(() =>
  sessions.value.find(item => Number(item.targetUserId) === Number(currentSessionUserId.value))
)

const formatTime = (timeText) => {
  if (!timeText) return ''
  const date = new Date(timeText)
  if (Number.isNaN(date.getTime())) return ''
  const now = new Date()
  const sameDay = now.toDateString() === date.toDateString()
  return sameDay
    ? `${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}`
    : `${date.getMonth() + 1}-${date.getDate()}`
}

const loadUnreadTotal = async () => {
  if (!currentUserId.value) return
  const res = await getPrivateUnreadCount(currentUserId.value)
  if (res.code === 200 || res.code === '200') {
    unreadTotal.value = Number(res.data || 0)
  }
}

const loadSessions = async () => {
  if (!currentUserId.value) return
  sessionLoading.value = true
  try {
    const res = await getSessionList(currentUserId.value)
    if (res.code === 200 || res.code === '200') {
      sessions.value = (res.data || []).map(item => ({
        ...item,
        unreadCount: Number(item.unreadCount || 0)
      }))
      if (!currentSessionUserId.value && sessions.value.length > 0) {
        currentSessionUserId.value = sessions.value[0].targetUserId
      }
    } else {
      ElMessage.error(res.msg || '加载会话失败')
    }
  } catch (e) {
    console.error(e)
    ElMessage.error('加载会话失败')
  } finally {
    sessionLoading.value = false
  }
}

const loadTargetUser = async (targetUserId) => {
  if (!targetUserId) return
  try {
    const res = await request.get(`/user/${targetUserId}`)
    if (res.code === 200 || res.code === '200') {
      currentTargetUser.value = res.data || null
    }
  } catch (e) {
    console.error('获取目标用户失败', e)
  }
}

const loadConversation = async ({ silent = false } = {}) => {
  if (!currentUserId.value || !currentSessionUserId.value) return
  if (!silent) loading.value = true
  try {
    const res = await getConversation(currentUserId.value, currentSessionUserId.value, 1, 100)
    if (res.code === 200 || res.code === '200') {
      messages.value = res.data?.records || []
      await markConversationRead(currentUserId.value, currentSessionUserId.value)
      await Promise.all([loadUnreadTotal(), loadSessions()])
      await loadTargetUser(currentSessionUserId.value)
    } else {
      ElMessage.error(res.msg || '加载聊天记录失败')
    }
  } catch (e) {
    console.error(e)
    ElMessage.error('加载聊天记录失败')
  } finally {
    if (!silent) loading.value = false
  }
}

const selectSession = async (targetUserId) => {
  if (!targetUserId) return
  currentSessionUserId.value = Number(targetUserId)
  await loadConversation()
}

const doSendMessage = async () => {
  const content = inputMessage.value.trim()
  if (!content) {
    ElMessage.warning('请输入消息内容')
    return
  }
  if (!currentUserId.value || !currentSessionUserId.value) return
  sending.value = true
  try {
    const res = await sendPrivateMessage(currentUserId.value, currentSessionUserId.value, content)
    if (res.code === 200 || res.code === '200') {
      inputMessage.value = ''
      await loadConversation({ silent: true })
    } else {
      ElMessage.error(res.msg || '发送失败')
    }
  } catch (e) {
    console.error(e)
    ElMessage.error('发送失败')
  } finally {
    sending.value = false
  }
}

const startPolling = () => {
  stopPolling()
  pollTimer = setInterval(async () => {
    if (!currentUserId.value) return
    await Promise.all([loadSessions(), loadUnreadTotal()])
    if (currentSessionUserId.value) {
      await loadConversation({ silent: true })
    }
  }, 4000)
}

const stopPolling = () => {
  if (pollTimer) {
    clearInterval(pollTimer)
    pollTimer = null
  }
}

watch(routeTargetId, async (newId) => {
  if (newId && newId !== currentUserId.value) {
    currentSessionUserId.value = newId
    await loadConversation()
  }
})

onMounted(async () => {
  if (!currentUserId.value) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  if (routeTargetId.value && routeTargetId.value === currentUserId.value) {
    ElMessage.warning('不能和自己聊天')
    router.push('/front/home')
    return
  }

  if (routeTargetId.value) {
    currentSessionUserId.value = routeTargetId.value
  }

  await Promise.all([loadSessions(), loadUnreadTotal()])
  if (currentSessionUserId.value) {
    await loadConversation()
  }
  startPolling()
})

onBeforeUnmount(() => {
  stopPolling()
})
</script>

<template>
  <div class="chat-page">
    <div class="chat-header">
      <div class="title">私聊</div>
      <div class="unread">未读：{{ unreadTotal }}</div>
    </div>

    <div class="chat-content">
      <aside class="session-panel" v-loading="sessionLoading">
        <div
          v-for="item in sessions"
          :key="item.targetUserId"
          class="session-item"
          :class="{ active: Number(item.targetUserId) === Number(currentSessionUserId) }"
          @click="selectSession(item.targetUserId)"
        >
          <el-avatar :src="item.targetAvatarUrl" :size="42">
            {{ (item.targetNickname || '?').charAt(0) }}
          </el-avatar>
          <div class="session-main">
            <div class="line1">
              <span class="name">{{ item.targetNickname || `用户${item.targetUserId}` }}</span>
              <span class="time">{{ formatTime(item.lastTime) }}</span>
            </div>
            <div class="line2">
              <span class="content">{{ item.lastContent || '' }}</span>
              <el-badge v-if="item.unreadCount > 0" :value="item.unreadCount" />
            </div>
          </div>
        </div>
        <div v-if="!sessionLoading && sessions.length === 0" class="empty">
          暂无会话
        </div>
      </aside>

      <section class="message-panel">
        <div class="message-header">
          {{ currentTargetUser?.nickname || currentSession?.targetNickname || '请选择会话' }}
        </div>

        <div class="message-list" v-loading="loading">
          <div v-if="!currentSessionUserId" class="empty">请选择左侧会话开始聊天</div>
          <div v-else-if="messages.length === 0" class="empty">还没有消息，发一条试试</div>
          <div
            v-for="msg in messages"
            :key="msg.id"
            class="message-item"
            :class="{ mine: Number(msg.senderId) === Number(currentUserId) }"
          >
            <div class="bubble">{{ msg.content }}</div>
            <div class="meta">{{ formatTime(msg.createTime) }}</div>
          </div>
        </div>

        <div class="message-input">
          <el-input
            v-model="inputMessage"
            type="textarea"
            :rows="3"
            resize="none"
            maxlength="1000"
            show-word-limit
            placeholder="输入私信内容..."
            @keyup.enter.exact.prevent="doSendMessage"
          />
          <el-button type="primary" :loading="sending" @click="doSendMessage">发送</el-button>
        </div>
      </section>
    </div>
  </div>
</template>

<style scoped lang="scss">
.chat-page {
  min-height: calc(100vh - 60px);
  padding: 20px;
  background: var(--bb-bg-page);
}

.chat-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
  color: var(--bb-text-primary);
}

.chat-content {
  display: grid;
  grid-template-columns: 320px 1fr;
  gap: 12px;
  height: calc(100vh - 150px);
}

.session-panel {
  background: var(--bb-bg-card);
  border: 1px solid var(--bb-border);
  border-radius: var(--bb-radius-card);
  overflow-y: auto;
}

.session-item {
  display: flex;
  gap: 10px;
  padding: 10px;
  border-bottom: 1px solid var(--bb-border);
  cursor: pointer;

  &.active {
    background: var(--bb-brand-soft);
  }

  .session-main {
    flex: 1;
    min-width: 0;
  }

  .line1,
  .line2 {
    display: flex;
    justify-content: space-between;
    align-items: center;
    gap: 8px;
  }

  .name {
    font-weight: 600;
    color: var(--bb-text-primary);
  }

  .time,
  .content {
    color: var(--bb-text-secondary);
    font-size: 12px;
  }

  .content {
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    max-width: 170px;
  }
}

.message-panel {
  background: var(--bb-bg-card);
  border: 1px solid var(--bb-border);
  border-radius: var(--bb-radius-card);
  display: grid;
  grid-template-rows: 48px 1fr 130px;
}

.message-header {
  border-bottom: 1px solid var(--bb-border);
  display: flex;
  align-items: center;
  padding: 0 14px;
  font-weight: 600;
}

.message-list {
  overflow-y: auto;
  padding: 12px;
  background: var(--bb-bg-page);
}

.message-item {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  margin-bottom: 10px;

  &.mine {
    align-items: flex-end;
  }

  .bubble {
    max-width: 70%;
    padding: 8px 10px;
    background: #fff;
    border: 1px solid var(--bb-border);
    border-radius: 10px;
    color: var(--bb-text-primary);
    white-space: pre-wrap;
    word-break: break-word;
  }

  &.mine .bubble {
    background: var(--bb-brand-soft);
  }

  .meta {
    font-size: 12px;
    color: var(--bb-text-secondary);
    margin-top: 4px;
  }
}

.message-input {
  padding: 10px;
  border-top: 1px solid var(--bb-border);
  display: grid;
  grid-template-columns: 1fr 88px;
  gap: 8px;
  align-items: end;
}

.empty {
  text-align: center;
  color: var(--bb-text-secondary);
  padding: 24px;
}

@media (max-width: 900px) {
  .chat-content {
    grid-template-columns: 1fr;
    height: auto;
  }

  .session-panel {
    max-height: 280px;
  }

  .message-panel {
    height: calc(100vh - 470px);
    min-height: 420px;
  }
}
</style>
