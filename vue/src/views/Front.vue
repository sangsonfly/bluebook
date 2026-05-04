<script setup>
import { ref, watch, computed, onMounted, onBeforeUnmount } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { projectName } from '../../config/config.default'
import { ElMessage } from 'element-plus'
import { getPrivateUnreadCount } from '@/api/privateMessage'

const router = useRouter()
const route = useRoute()

const account = ref(
  localStorage.getItem('account') ? JSON.parse(localStorage.getItem('account')) : {}
)

const searchKeyword = ref('')
const privateUnread = ref(0)
const isSearchPanelVisible = ref(false)
const searchBoxRef = ref(null)
const concernTopics = ref([
  '新生指南',
  '考研组队',
  '社团招新',
  '宿舍日常',
  '校园美景',
  '二手闲置'
])

watch(
  () => [route.path, route.query.q],
  () => {
    if (route.path === '/front/home') {
      const q = route.query.q
      searchKeyword.value = q != null && q !== '' ? String(q) : ''
    }
  },
  { immediate: true }
)

const activePath = computed(() => route.path)
const isChatActive = computed(
  () => activePath.value === '/front/chat' || activePath.value.startsWith('/front/chat/')
)
const privateUnreadDisplay = computed(() => {
  if (privateUnread.value <= 0) return ''
  return privateUnread.value > 99 ? '99+' : String(privateUnread.value)
})

const applyHomeSearchQuery = () => {
  const q = searchKeyword.value.trim()
  router.push({ path: '/front/home', query: q ? { q } : {} })
}

const handleSearch = () => {
  if (!searchKeyword.value.trim()) {
    if (route.path === '/front/home') {
      router.replace({ path: '/front/home', query: {} })
    }
    return
  }
  applyHomeSearchQuery()
  isSearchPanelVisible.value = false
}

const handleSearchFocus = () => {
  isSearchPanelVisible.value = true
}

const handleTopicClick = (topic) => {
  searchKeyword.value = topic
  handleSearch()
}

const handleGlobalPointerDown = (event) => {
  if (!searchBoxRef.value) return
  if (!searchBoxRef.value.contains(event.target)) {
    isSearchPanelVisible.value = false
  }
}

const logout = () => {
  localStorage.removeItem('account')
  ElMessage.success('退出成功')
  router.push('/login')
}

const loadPrivateUnread = async () => {
  if (!account.value?.id) {
    privateUnread.value = 0
    return
  }
  try {
    const res = await getPrivateUnreadCount(account.value.id)
    if (res.code === 200 || res.code === '200') {
      privateUnread.value = Number(res.data || 0)
    }
  } catch (error) {
    console.error('加载私聊未读数失败', error)
  }
}

const handleUpdateAccount = (updatedAccount) => {
  account.value = updatedAccount
  loadPrivateUnread()
}

watch(
  () => route.path,
  (newPath, oldPath) => {
    const isChatPath = (path) => path === '/front/chat' || path.startsWith('/front/chat/')
    if (isChatPath(newPath) || isChatPath(oldPath || '')) {
      loadPrivateUnread()
    }
  }
)

onMounted(() => {
  loadPrivateUnread()
  document.addEventListener('mousedown', handleGlobalPointerDown)
})

onBeforeUnmount(() => {
  document.removeEventListener('mousedown', handleGlobalPointerDown)
})
</script>

<template>
  <div class="front-container">
    <header class="header-nav">
      <div class="header-content">
        <div class="logo" @click="router.push('/front/home')">
          <i class="fas fa-book-open"></i>
          <span>{{ projectName }}</span>
        </div>

        <div ref="searchBoxRef" class="search-box">
          <i class="fas fa-search search-icon" aria-hidden="true" @click="handleSearch" />
          <input
            v-model="searchKeyword"
            type="text"
            placeholder="搜索校园笔记、活动、二手交易..."
            @keydown.enter.prevent="handleSearch"
            @focus="handleSearchFocus"
          />
          <div v-if="isSearchPanelVisible" class="search-suggest-panel">
            <p class="suggest-title">大家关心</p>
            <div class="suggest-list">
              <button
                v-for="topic in concernTopics"
                :key="topic"
                type="button"
                class="suggest-pill"
                @click="handleTopicClick(topic)"
              >
                #{{ topic }}
              </button>
            </div>
          </div>
        </div>

        <nav class="nav-menu">
          <a
            href="javascript:;"
            class="nav-item"
            :class="{ active: activePath === '/front/home' }"
            @click="router.push('/front/home')"
          >
            <i class="fas fa-home"></i>
            <span>首页</span>
          </a>

          <a
            v-if="account.id"
            href="javascript:;"
            class="nav-item"
            :class="{ active: activePath === '/front/publish' }"
            @click="router.push('/front/publish')"
          >
            <i class="fas fa-plus-circle"></i>
            <span>发布</span>
          </a>

          <a
            v-if="account.id"
            href="javascript:;"
            class="nav-item"
            :class="{ active: isChatActive }"
            @click="router.push('/front/chat')"
          >
            <i class="fas fa-bell"></i>
            <span>消息</span>
            <span v-if="privateUnread > 0" class="badge">{{ privateUnreadDisplay }}</span>
          </a>

          <a
            v-if="account.id"
            href="javascript:;"
            class="nav-item"
            :class="{
              active:
                activePath.includes('/front/person') || activePath.includes('/front/password')
            }"
            @click="router.push('/front/person')"
          >
            <i class="fas fa-user"></i>
            <span>我的</span>
          </a>

          <a v-if="account.id" href="javascript:;" class="nav-item" @click="logout">
            <i class="fas fa-right-from-bracket"></i>
            <span>退出</span>
          </a>
          <template v-if="!account.id">
            <el-button size="small" @click="router.push('/login')">登录</el-button>
            <el-button size="small" type="primary" @click="router.push('/register')">注册</el-button>
          </template>
        </nav>
      </div>
    </header>

    <div class="main-content">
      <router-view v-slot="{ Component }">
        <keep-alive include="FrontHome">
          <component :is="Component" @update-account="handleUpdateAccount" />
        </keep-alive>
      </router-view>
    </div>

    <footer class="front-footer">
      <p>© {{ new Date().getFullYear() }} {{ projectName }}. 保留所有权利</p>
    </footer>
  </div>
</template>

<style lang="scss" scoped>
@import url('https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css');

.front-container {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.header-nav {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  background: var(--bb-bg-card);
  border-bottom: 1px solid var(--bb-border);
  box-shadow: var(--bb-shadow-card);
  z-index: 1000;
}

.header-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 90px;
  max-width: 1920px;
  margin: 0 auto;
  padding: 0 32px;
}

.logo {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 25px;
  font-weight: 600;
  color: var(--bb-brand);
  cursor: pointer;
  transition: transform 0.2s;
  user-select: none;
}

.logo:hover {
  transform: scale(1.02);
}

.logo i {
  font-size: 26px;
}

.search-box {
  flex: 1;
  max-width: 520px;
  margin: 0 10px;
  position: relative;
}

.search-box .search-icon {
  position: absolute;
  left: 16px;
  top: 50%;
  transform: translateY(-50%);
  color: var(--bb-text-muted);
  font-size: 20px;
  cursor: pointer;
  z-index: 1;
}

.search-box .search-icon:hover {
  color: var(--bb-brand);
}

.search-box input {
  width: 100%;
  padding: 11px 16px 11px 44px;
  border: 1px solid var(--bb-border);
  border-radius: var(--bb-radius-pill);
  font-size: 20px;
  outline: none;
  transition: border-color 0.2s, box-shadow 0.2s;
  background: var(--bb-bg-page);
  box-sizing: border-box;
}

.search-box input:focus {
  border-color: var(--bb-brand);
  background: var(--bb-bg-card);
  box-shadow: 0 0 0 3px var(--bb-brand-soft);
}

.search-box input::placeholder {
  color: var(--bb-text-muted);
}

.search-suggest-panel {
  position: absolute;
  top: calc(100% + 10px);
  left: 0;
  width: 100%;
  background: var(--bb-bg-card);
  border: 1px solid var(--bb-border);
  border-radius: 14px;
  box-shadow: var(--bb-shadow-card);
  padding: 12px 14px;
  z-index: 20;
}

.suggest-title {
  margin: 0 0 10px;
  font-size: 14px;
  font-weight: 600;
  color: var(--bb-text-secondary);
}

.suggest-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.suggest-pill {
  border: 1px solid var(--bb-border);
  background: var(--bb-bg-page);
  color: var(--bb-text-secondary);
  border-radius: 999px;
  padding: 5px 10px;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s;
}

.suggest-pill:hover {
  color: var(--bb-brand);
  border-color: var(--bb-brand-muted);
  background: var(--bb-brand-soft);
}

.nav-menu {
  display: flex;
  align-items: center;
  gap: 8px;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 5px;
  color: var(--bb-text-primary);
  text-decoration: none;
  font-size: 20px;
  padding: 9px 16px;
  border-radius: 8px;
  transition: background 0.2s, color 0.2s;
  position: relative;
  cursor: pointer;
  user-select: none;
}

.nav-item:hover {
  background: var(--bb-brand-soft);
  color: var(--bb-brand);
}

.nav-item.active {
  color: var(--bb-brand);
  font-weight: 600;
}

.nav-item i {
  font-size: 20px;
}

.badge {
  position: absolute;
  top: 4px;
  right: 8px;
  background: #f97316;
  color: white;
  font-size: 11px;
  padding: 2px 5px;
  border-radius: 10px;
  min-width: 16px;
  text-align: center;
  font-weight: 600;
  line-height: 1;
}

.main-content {
  flex: 1;
  background-color: var(--bb-bg-page);
  margin-top: 90px;
}

.front-footer {
  padding: 16px 24px;
  text-align: center;
  background-color: var(--bb-bg-card);
  color: var(--bb-text-secondary);
  font-size: 12px;
  border-top: 1px solid var(--bb-border);
}

@media (max-width: 1200px) {
  .search-box {
    max-width: 360px;
    margin: 0 20px;
  }
}

@media (max-width: 992px) {
  .header-content {
    padding: 0 16px;
  }

  .search-box {
    max-width: 280px;
    margin: 0 12px;
  }

  .nav-item span {
    display: none;
  }

  .nav-item {
    padding: 8px 12px;
  }
}

@media (max-width: 768px) {
  .search-box {
    display: none;
  }

  .logo span {
    display: none;
  }
}
</style>
