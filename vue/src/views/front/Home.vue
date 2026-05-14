<script setup>
import { ref, onMounted, computed, watch } from 'vue'
import { getNoteList, getNotePage, likeNote, collectNote } from '@/api/note'
import { getRecommendedNotes, refreshMyRecommendations } from '@/api/recommendation'
import { getVerifiedClubs } from '@/api/club'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Icon } from '@iconify/vue'
import {
  Star,
  Reading,
  User,
  Calendar,
  Food,
  ShoppingBag,
  Basketball,
  Camera,
  StarFilled,
  Share,
  CircleCheck
} from '@element-plus/icons-vue'

defineOptions({
  name: 'FrontHome'
})

const router = useRouter()
const route = useRoute()
const notes = ref([])
const clubs = ref([])
const loading = ref(false)
const isRefreshing = ref(false)
const activeCategory = ref('all')
const activeTab = ref('推荐')

const searchKeyword = computed(() => {
  const q = route.query.q
  if (q == null || q === '') return ''
  return String(q).trim()
})

const isSearching = computed(() => searchKeyword.value.length > 0)

const categoryIcons = {
  all: Star,
  学习经验: Reading,
  社团活动: User,
  校园活动: Calendar,
  美食探店: Food,
  二手市场: ShoppingBag,
  运动健身: Basketball,
  校园风光: Camera,
  其他: Star
}

const categories = ref([
  { id: 'all', name: '热门内容' },
  { id: '学习经验', name: '学习经验' },
  { id: '社团活动', name: '社团活动' },
  { id: '校园活动', name: '校园活动' },
  { id: '美食探店', name: '美食探店' },
  { id: '二手市场', name: '二手市场' },
  { id: '运动健身', name: '运动健身' },
  { id: '校园风光', name: '校园风光' },
  { id: '其他', name: '其他' }
])

const loadNotes = async () => {
  loading.value = true
  try {
    let res
    const account = localStorage.getItem('account') ? JSON.parse(localStorage.getItem('account')) : null
    const userId = account?.id

    const params = {
      pageNum: 1,
      pageSize: 20
    }

    const hasSearchKeyword = searchKeyword.value.length > 0
    if (hasSearchKeyword) {
      params.keyword = searchKeyword.value
    }

    if (activeCategory.value !== 'all') {
      params.category = activeCategory.value
    }

    if (hasSearchKeyword) {
      res = await getNotePage(params)
    } else if (activeTab.value === '推荐') {
      if (activeCategory.value !== 'all') {
        res = await getNotePage(params)
      } else {
        res = await getRecommendedNotes({ limit: params.pageSize || 20 })
      }
    } else if (activeTab.value === '关注') {
      if (!userId) {
        ElMessage.warning('请先登录查看关注动态')
        notes.value = []
        loading.value = false
        return
      }
      params.userId = userId
      res = await getNoteList('following', params)
    } else {
      if (activeCategory.value !== 'all') {
        res = await getNotePage(params)
      } else {
        res = await getNoteList('latest', params)
      }
    }

    if (res.code === 200 || res.code === '200') {
      if (res.data && res.data.records) {
        notes.value = res.data.records || []
      } else {
        notes.value = res.data || []
      }

      if (hasSearchKeyword) {
        if (notes.value.length === 0) {
          ElMessage.info('未找到相关笔记')
        } else {
          ElMessage.success(`找到 ${notes.value.length} 条相关笔记`)
        }
      }
    }
  } catch (error) {
    ElMessage.error('加载笔记失败')
    console.error(error)
  } finally {
    loading.value = false
  }
}

// 模块级变量：不受组件 keep-alive/HMR 生命周期影响
let skipNextHomeLoad = false

watch(
  () => `${route.path}|${route.query.q ?? ''}`,
  () => {
    if (route.path === '/front/home') {
      if (skipNextHomeLoad) {
        skipNextHomeLoad = false
        return
      }
      loadNotes()
    }
  },
  { immediate: true }
)

const loadClubs = async () => {
  try {
    const res = await getVerifiedClubs()
    if (res.code === 200 || res.code === '200') {
      clubs.value = res.data || []
    }
  } catch (error) {
    console.error('加载社团失败', error)
  }
}

const goToDetail = (id) => {
  skipNextHomeLoad = true
  router.push(`/front/note/${id}`)
}

const goToUserProfile = (userId) => {
  if (!userId) return
  skipNextHomeLoad = true
  router.push(`/front/user/${userId}`)
}

const goToClub = (clubId) => {
  if (!clubId) return
  skipNextHomeLoad = true
  router.push(`/front/club/${clubId}`)
}

const handleLike = async (id, event) => {
  event.stopPropagation()
  const account = JSON.parse(localStorage.getItem('account') || 'null')
  if (!account || !account.id) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  try {
    await likeNote(id, account.id)
    ElMessage.success('点赞成功')
  } catch {
    ElMessage.error('点赞失败')
  }
}

const handleCollect = async (id, event) => {
  event.stopPropagation()
  const account = JSON.parse(localStorage.getItem('account') || 'null')
  if (!account || !account.id) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  try {
    const res = await collectNote(id, account.id)
    if (res.code === 200 || res.code === '200') {
      ElMessage.success(res.data ? '收藏成功' : '已取消收藏')
    } else {
      ElMessage.error(res.msg || '操作失败')
    }
  } catch {
    ElMessage.error('收藏失败')
  }
}

const formatNumber = (num) => {
  if (!num) return 0
  if (num >= 10000) {
    return (num / 10000).toFixed(1) + 'w'
  } else if (num >= 1000) {
    return (num / 1000).toFixed(1) + 'k'
  }
  return num
}

const getNoteImage = (imageUrl) => {
  if (!imageUrl) return ''
  const images = imageUrl.split(',').filter((url) => url.trim())
  return images[0] || ''
}

const getImageCount = (imageUrl) => {
  if (!imageUrl) return 0
  return imageUrl.split(',').filter((url) => url.trim()).length
}

const coverToneClass = (id) => {
  const n = Math.abs(Number(id) || 0) % 6
  return `cover-tone-${n}`
}

const switchTab = (tab) => {
  activeTab.value = tab
  loadNotes()
}

const switchCategory = (categoryId) => {
  activeCategory.value = categoryId
  loadNotes()
}

const handleRefreshRecommend = async () => {
  isRefreshing.value = true
  try {
    await refreshMyRecommendations()
    await loadNotes()
    ElMessage.success('推荐已更新')
  } catch {
    ElMessage.error('刷新失败')
  } finally {
    isRefreshing.value = false
  }
}

const clearSearch = () => {
  const next = { ...route.query }
  delete next.q
  router.replace({ path: '/front/home', query: next })
}

onMounted(() => {
  loadClubs()
})
</script>

<template>
  <div class="home-container">
    <div class="main-content">
      <div class="content-wrapper">
        <aside class="sidebar">
          <div class="sidebar-section sidebar-section--nav">
            <h3 class="sidebar-title">内容分类</h3>
            <ul class="category-list">
              <li
                v-for="cat in categories"
                :key="cat.id"
                :class="['category-item', { active: activeCategory === cat.id }]"
                @click="switchCategory(cat.id)"
              >
                <el-icon class="cat-icon">
                  <component :is="categoryIcons[cat.id] || Star" />
                </el-icon>
                <span>{{ cat.name }}</span>
              </li>
            </ul>
          </div>

          <div class="sidebar-section sidebar-section--orgs">
            <h3 class="sidebar-title">校园组织&协会</h3>
            <div class="club-list">
              <div
                v-for="club in clubs"
                :key="club.id"
                class="club-item"
                role="button"
                tabindex="0"
                @click="goToClub(club.id)"
                @keydown.enter.prevent="goToClub(club.id)"
              >
                <el-avatar :src="club.avatarUrl" :size="40">
                  {{ club.name?.charAt(0) }}
                </el-avatar>
                <div class="club-info">
                  <div class="club-name">
                    {{ club.name }}
                    <el-icon v-if="club.isVerified" class="verified"><CircleCheck /></el-icon>
                  </div>
                  <span class="club-member">{{ club.memberCount }} 成员</span>
                </div>
              </div>
            </div>
          </div>
        </aside>

        <div class="notes-container">
          <div v-if="isSearching" class="search-result-header">
            <span>搜索"{{ searchKeyword }}"的结果</span>
            <el-button text type="primary" @click="clearSearch">清除搜索</el-button>
          </div>

          <div class="filter-bar">
            <button
              type="button"
              class="feed-tab"
              :class="{ 'feed-tab--active': activeTab === '推荐' }"
              @click="switchTab('推荐')"
            >
              推荐
            </button>
            <button
              type="button"
              class="feed-tab"
              :class="{ 'feed-tab--active': activeTab === '最新' }"
              @click="switchTab('最新')"
            >
              最新
            </button>
            <button
              type="button"
              class="feed-tab"
              :class="{ 'feed-tab--active': activeTab === '关注' }"
              @click="switchTab('关注')"
            >
              关注
            </button>
            <el-button
              v-if="activeTab === '推荐' && !isSearching"
              :loading="isRefreshing"
              size="small"
              text
              type="primary"
              style="margin-left: auto"
              @click="handleRefreshRecommend"
            >
              刷新
            </el-button>
          </div>

          <div v-if="activeTab === '推荐' && !isSearching" class="recommend-hint">
            根据你的兴趣为你推荐
          </div>

          <div v-loading="loading" class="waterfall">
            <div
              v-for="note in notes"
              :key="note.id"
              class="note-card"
              @click="goToDetail(note.id)"
            >
              <template v-if="getNoteImage(note.imageUrl)">
                <div class="note-image-wrapper">
                  <el-image
                    :src="getNoteImage(note.imageUrl)"
                    :alt="note.title"
                    class="note-image"
                    fit="cover"
                    lazy
                  >
                    <template #error>
                      <div class="note-text-cover" :class="coverToneClass(note.id)">
                        <span class="cover-title">{{ note.title }}</span>
                      </div>
                    </template>
                  </el-image>
                  <div v-if="getImageCount(note.imageUrl) > 1" class="image-count-indicator">
                    <span class="indicator-text">{{ getImageCount(note.imageUrl) }}</span>
                  </div>
                  <div class="hover-actions">
                    <el-button circle size="small" @click.stop="handleLike(note.id, $event)">
                      <Icon icon="mdi:heart-outline" class="hover-action-icon" aria-hidden="true" />
                    </el-button>
                    <el-button circle size="small" @click.stop="handleCollect(note.id, $event)">
                      <el-icon><StarFilled /></el-icon>
                    </el-button>
                    <el-button circle size="small" @click.stop>
                      <el-icon><Share /></el-icon>
                    </el-button>
                  </div>
                </div>
              </template>
              <div v-else class="note-text-cover" :class="coverToneClass(note.id)">
                <span class="cover-title">{{ note.title }}</span>
              </div>

              <div class="note-content">
                <h3 class="note-title">{{ note.title }}</h3>
                <div v-if="note.tags" class="note-tags">
                  <span
                    v-for="tag in note.tags.split(',').filter(Boolean)"
                    :key="tag"
                    class="tag-pill"
                  >
                    #{{ tag.trim() }}
                  </span>
                </div>
                <div class="note-footer">
                  <div class="author-info" @click.stop="goToUserProfile(note.userId)">
                    <el-avatar :src="note.authorAvatar" :size="28">
                      {{ note.authorName?.charAt(0) }}
                    </el-avatar>
                    <span class="author-name">{{ note.authorName }}</span>
                  </div>
                  <div class="note-stats">
                    <Icon icon="mdi:heart-outline" class="stat-icon" aria-hidden="true" />
                    <span>{{ formatNumber(note.likes) }}</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped lang="scss">
.home-container {
  min-height: calc(100vh - 90px);
  background: var(--bb-bg-page);
}

.main-content {
  max-width: 1920px;
  margin: 0 auto;
  padding: 20px 24px 40px;
}

@media (min-width: 1400px) {
  .main-content {
    padding-left: 18px;
    padding-right: 18px;
  }
}

.content-wrapper {
  display: grid;
  grid-template-columns: 200px 1fr;
  gap: 20px;
  align-items: start;
}

.sidebar {
  position: sticky;
  top: 90px;
  height: fit-content;
}

.sidebar-section {
  background: transparent;
  border: none;
  box-shadow: none;
  border-radius: 0;
  padding: 0;
  margin: 0;
}

.sidebar .sidebar-section:not(:last-child) {
  padding-bottom: 20px;
  margin-bottom: 20px;
  border-bottom: 1px solid var(--bb-border);
}

.sidebar-section--orgs {
  margin-top: 30px;
}

.sidebar-title {
  position: relative;
  font-size:20px;
  font-weight: 600;
  margin: 0 0 12px;
  padding-left: 10px;
  color: var(--bb-text-muted);
  letter-spacing: 0.02em;
}

.sidebar-title::before {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 2px;
  height: 14px;
  border-radius: 2px;
  background: var(--bb-brand);
}

.category-list {
  list-style: none;
  padding: 0;
  margin: 0;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.category-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 11px 14px;
  border-radius: 8px;
  cursor: pointer;
  transition: background 0.2s, color 0.2s;
  font-weight: 500;
  font-size: 19px;
  color: var(--bb-text-secondary);
}

.category-item .cat-icon {
  font-size: 22px;
  color: var(--bb-text-muted);
}

.category-item:hover {
  background: var(--bb-bg-page);
  color: var(--bb-text-primary);
}

.category-item:hover .cat-icon {
  color: var(--bb-brand);
}

.category-item.active {
  background: var(--bb-brand-soft);
  color: var(--bb-brand);
  font-weight: 600;
}

.category-item.active .cat-icon {
  color: var(--bb-brand);
}

.hot-topics {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.topic-pill {
  font-size: 18px;
  padding: 7px 13px;
  border-radius: var(--bb-radius-pill);
  background: var(--bb-bg-card);
  color: var(--bb-text-secondary);
  border: 1px solid transparent;
  cursor: default;
}

.topic-pill:hover {
  border-color: var(--bb-brand-muted);
  color: var(--bb-brand);
}

.notes-container {
  min-height: 400px;
}

.recommend-hint {
  font-size: 13px;
  color: var(--bb-text-muted);
  margin-bottom: 12px;
}

.search-result-header {
  background: var(--bb-bg-card);
  border-radius: var(--bb-radius-card);
  padding: 14px 18px;
  margin-bottom: 16px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  border: 1px solid var(--bb-border);
  box-shadow: var(--bb-shadow-card);
}

.search-result-header span {
  font-size: 14px;
  font-weight: 600;
  color: var(--bb-brand);
}

.filter-bar {
  background: transparent;
  border-radius: 0;
  padding: 8px 0 12px;
  margin-bottom: 20px;
  display: flex;
  gap: 8px;
  align-items: center;
  border: none;
  border-bottom: 1px solid var(--bb-border);
  box-shadow: none;
}

.feed-tab {
  border: none;
  background: transparent;
  padding: 8px 16px;
  font-size: 25px;
  font-weight: 500;
  color: var(--bb-text-secondary);
  cursor: pointer;
  border-radius: 8px;
  transition: color 0.2s, background 0.2s;
}

.feed-tab:hover {
  color: var(--bb-text-primary);
  background: var(--bb-bg-page);
}

.feed-tab--active {
  color: var(--bb-brand);
  font-weight: 700;
  box-shadow: inset 0 -2px 0 var(--bb-brand);
  border-radius: 8px 8px 0 0;
}

.waterfall {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 40px;
  max-width: 1200px;
  margin-inline: auto;
}

.note-card {
  background: var(--bb-bg-card);
  border-radius: var(--bb-radius-card);
  overflow: hidden;
  cursor: pointer;
  transition: transform 0.25s ease, box-shadow 0.25s ease;
  border: 1px solid var(--bb-border);
  box-shadow: var(--bb-shadow-card);
}

.note-card:hover {
  transform: translateY(-4px);
  box-shadow: var(--bb-shadow-card-hover);
}

.note-image-wrapper {
  position: relative;
  overflow: hidden;
  aspect-ratio: 4 / 5;
  background: var(--bb-bg-page);
}

.note-image {
  width: 100%;
  height: 100%;
  display: block;
}

.note-image :deep(.el-image__inner) {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.image-count-indicator {
  position: absolute;
  top: 8px;
  right: 8px;
  background: rgba(0, 0, 0, 0.45);
  color: white;
  padding: 3px 8px;
  border-radius: 10px;
  font-size: 12px;
  font-weight: 500;
  z-index: 5;
}

.hover-actions {
  position: absolute;
  bottom: 10px;
  right: 10px;
  display: flex;
  gap: 6px;
  opacity: 0;
  transform: translateY(6px);
  transition: opacity 0.2s, transform 0.2s;
  z-index: 6;
}

.note-card:hover .hover-actions {
  opacity: 1;
  transform: translateY(0);
}

.hover-actions .el-button {
  background: rgba(255, 255, 255, 0.95);
  border: none;
  box-shadow: var(--bb-shadow-card);
}

.hover-action-icon {
  display: block;
  width: 14px;
  height: 14px;
  color: var(--el-text-color-regular);
}

.note-text-cover {
  aspect-ratio: 3 / 4;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
  text-align: center;
}

.cover-title {
  font-size: 30px;
  font-weight: 400;
  line-height: 1.4;
  color: #fff;
  display: -webkit-box;
  -webkit-line-clamp: 4;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.cover-tone-0 {
  background: linear-gradient(145deg, #1d4ed8 0%, #2563eb 50%, #3b82f6 100%);
}
.cover-tone-1 {
  background: linear-gradient(145deg, #1e40af 0%, #2563eb 100%);
}
.cover-tone-2 {
  background: linear-gradient(145deg, #0369a1 0%, #0ea5e9 100%);
}
.cover-tone-3 {
  background: linear-gradient(145deg, #312e81 0%, #4f46e5 100%);
}
.cover-tone-4 {
  background: linear-gradient(145deg, #0f766e 0%, #14b8a6 100%);
}
.cover-tone-5 {
  background: linear-gradient(145deg, #1e3a5f 0%, #2563eb 90%);
}

.note-content {
  padding: 10px 12px 14px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.note-title {
  font-size: 16px;
  font-weight: 600;
  margin: 0;
  line-height: 1.45;
  color: var(--bb-text-primary);
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  word-break: break-word;
}

.note-card:hover .note-title {
  color: var(--bb-brand);
}

.note-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  min-height: 0;
}

.tag-pill {
  font-size: 12px;
  padding: 3px 8px;
  border-radius: 4px;
  background: var(--bb-bg-page);
  color: var(--bb-text-muted);
  border: 1px solid var(--bb-border);
}

.note-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 8px;
  margin-top: 2px;
  border-top: 1px solid var(--bb-border);
}

.author-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
}

.author-info:hover .author-name {
  color: var(--bb-brand);
}

.author-name {
  font-size: 13px;
  font-weight: 500;
  color: var(--bb-text-secondary);
  transition: color 0.2s;
  max-width: 120px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.note-stats {
  display: flex;
  align-items: center;
  gap: 4px;
  color: var(--bb-text-muted);
  font-size: 13px;
  font-weight: 600;
}

.stat-icon {
  display: block;
  flex-shrink: 0;
  width: 15px;
  height: 15px;
  color: var(--bb-brand-muted);
}

.club-list {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.club-item {
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 12px;
  border-radius: 8px;
  transition: background 0.2s;
  cursor: default;
}

.club-item:hover {
  background: var(--bb-bg-page);
}

.club-info {
  flex: 1;
  min-width: 0;
}

.club-name {
  font-size: 17px;
  font-weight: 600;
  margin-bottom: 2px;
  color: var(--bb-text-primary);
  display: flex;
  align-items: center;
  gap: 4px;
}

.club-member {
  font-size: 15px;
  color: var(--bb-text-muted);
  font-weight: 500;
}

.verified {
  color: var(--bb-brand);
  font-size: 20px;
}

@media (max-width: 1200px) {
  .content-wrapper {
    grid-template-columns: 1fr;
  }

  .sidebar {
    display: none;
  }

  .waterfall {
    grid-template-columns: repeat(4, minmax(0, 1fr));
    gap: 20px;
  }
}

@media (max-width: 900px) {
  .waterfall {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }
}

@media (max-width: 768px) {
  .main-content {
    padding: 12px 12px 32px;
  }

  .waterfall {
    grid-template-columns: repeat(2, 1fr);
    gap: 14px;
  }

  .note-content {
    padding: 12px 12px 14px;
    gap: 10px;
  }

  .note-title {
    font-size: 16px;
    line-height: 1.45;
  }

  .note-card {
    border-radius: 10px;
  }
}
</style>
