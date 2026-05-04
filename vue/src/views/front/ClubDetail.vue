<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { CircleCheck, Setting } from '@element-plus/icons-vue'
import { getClubById } from '@/api/club'
import { getNotePage } from '@/api/note'
import {
  joinClub,
  leaveClub,
  isClubMember,
  isClubAdmin,
  getClubMembersPage
} from '@/api/clubMember'
import { getClubActivities } from '@/api/activity'
import {
  registerActivity,
  cancelActivityRegistration,
  hasRegistered
} from '@/api/activityRegistration'

const route = useRoute()
const router = useRouter()

const clubId = computed(() => Number(route.params.id))

const club = ref(null)
const clubLoading = ref(true)
const activeMainTab = ref('notes')

const notes = ref([])
const notesTotal = ref(0)
const notesPage = ref({ pageNum: 1, pageSize: 12 })
const notesLoading = ref(false)

const activities = ref([])
const activitiesLoading = ref(false)
const activitySubmittingMap = ref({})
const activityRegisteredMap = ref({})

const members = ref([])
const membersTotal = ref(0)
const membersPage = ref({ pageNum: 1, pageSize: 15 })
const membersLoading = ref(false)

const account = ref(
  localStorage.getItem('account') ? JSON.parse(localStorage.getItem('account')) : {}
)

const isMember = ref(false)
const isAdmin = ref(false)

const roleLabel = (role) => {
  if (role === 3) return '社长'
  if (role === 2) return '管理员'
  return '成员'
}

const loadClub = async () => {
  clubLoading.value = true
  try {
    const res = await getClubById(clubId.value)
    if (res.code === 200 || res.code === '200') {
      club.value = res.data
    } else {
      club.value = null
      ElMessage.error(res.msg || '加载社团失败')
    }
  } catch (e) {
    club.value = null
    ElMessage.error('加载社团失败')
  } finally {
    clubLoading.value = false
  }
}

const loadMembershipFlags = async () => {
  if (!account.value?.id) {
    isMember.value = false
    isAdmin.value = false
    return
  }
  try {
    const [mRes, aRes] = await Promise.all([
      isClubMember(clubId.value, account.value.id),
      isClubAdmin(clubId.value, account.value.id)
    ])
    isMember.value =
      (mRes.code === 200 || mRes.code === '200') && (mRes.data === true || mRes.data === 'true')
    isAdmin.value =
      (aRes.code === 200 || aRes.code === '200') && (aRes.data === true || aRes.data === 'true')
  } catch {
    isMember.value = false
    isAdmin.value = false
  }
}

const loadNotes = async () => {
  notesLoading.value = true
  try {
    const res = await getNotePage({
      clubId: clubId.value,
      pageNum: notesPage.value.pageNum,
      pageSize: notesPage.value.pageSize
    })
    if ((res.code === 200 || res.code === '200') && res.data) {
      notes.value = res.data.records || []
      notesTotal.value = res.data.total || 0
    }
  } catch {
    ElMessage.error('加载笔记失败')
  } finally {
    notesLoading.value = false
  }
}

const loadActivities = async () => {
  activitiesLoading.value = true
  try {
    const res = await getClubActivities(clubId.value)
    if (res.code === 200 || res.code === '200') {
      activities.value = res.data || []
      await loadRegistrationStates()
    } else {
      activities.value = []
      activityRegisteredMap.value = {}
    }
  } catch {
    activities.value = []
    activityRegisteredMap.value = {}
  } finally {
    activitiesLoading.value = false
  }
}

const loadRegistrationStates = async () => {
  if (!account.value?.id || account.value.role !== 'ROLE_USER' || !activities.value.length) {
    activityRegisteredMap.value = {}
    return
  }
  const pairs = await Promise.all(
    activities.value.map(async (a) => {
      try {
        const res = await hasRegistered(a.id)
        const registered =
          (res.code === 200 || res.code === '200') && (res.data === true || res.data === 'true')
        return [a.id, registered]
      } catch {
        return [a.id, false]
      }
    })
  )
  activityRegisteredMap.value = Object.fromEntries(pairs)
}

const loadMembers = async () => {
  membersLoading.value = true
  try {
    const res = await getClubMembersPage(
      clubId.value,
      membersPage.value.pageNum,
      membersPage.value.pageSize
    )
    if ((res.code === 200 || res.code === '200') && res.data) {
      members.value = res.data.records || []
      membersTotal.value = res.data.total || 0
    }
  } catch {
    members.value = []
  } finally {
    membersLoading.value = false
  }
}

const goNote = (id) => {
  router.push(`/front/note/${id}`)
}

const handleJoin = async () => {
  if (!account.value?.id) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  if (Number(account.value?.isVerified) !== 1) {
    ElMessage.warning('请先完成实名认证后再加入社团')
    router.push('/front/person')
    return
  }
  try {
    const res = await joinClub(clubId.value)
    if (res.code === 200 || res.code === '200') {
      ElMessage.success('加入成功')
      await loadClub()
      await loadMembershipFlags()
    } else {
      ElMessage.error(res.msg || '加入失败')
    }
  } catch (e) {
    ElMessage.error(e?.message || '加入失败')
  }
}

const handleLeave = () => {
  if (!account.value?.id) return
  ElMessageBox.confirm('确定退出该社团？', '提示', { type: 'warning' })
    .then(async () => {
      try {
        const res = await leaveClub(clubId.value)
        if (res.code === 200 || res.code === '200') {
          ElMessage.success('已退出')
          await loadClub()
          await loadMembershipFlags()
        } else {
          ElMessage.error(res.msg || '退出失败')
        }
      } catch (e) {
        ElMessage.error(e?.message || '退出失败')
      }
    })
    .catch(() => {})
}

const goManage = () => {
  router.push(`/front/club/${clubId.value}/manage`)
}

const handleNotesPage = (p) => {
  notesPage.value.pageNum = p
  loadNotes()
}

const handleMembersPage = (p) => {
  membersPage.value.pageNum = p
  loadMembers()
}

const onMainTab = (name) => {
  if (name === 'activities' && activities.value.length === 0 && !activitiesLoading.value) {
    loadActivities()
  }
  if (name === 'members' && members.value.length === 0 && !membersLoading.value) {
    loadMembers()
  }
}

const getNoteCover = (imageUrl) => {
  if (!imageUrl) return ''
  const first = imageUrl.split(',').map((s) => s.trim()).filter(Boolean)[0]
  return first || ''
}

const formatDateTime = (value) => {
  if (!value) return '-'
  return String(value).replace('T', ' ')
}

const activityStatusLabel = (status) => {
  if (status === 0) return '已取消'
  if (status === 1) return '报名中'
  if (status === 2) return '进行中'
  if (status === 3) return '已结束'
  return '未知'
}

const activityStatusType = (status) => {
  if (status === 1) return 'success'
  if (status === 2) return 'warning'
  if (status === 3) return 'info'
  return 'danger'
}

const toTime = (value) => {
  if (!value) return null
  const t = new Date(value).getTime()
  return Number.isNaN(t) ? null : t
}

const getActivityDisplayStatus = (activity) => {
  if (!activity || activity.status === 0) return activity?.status ?? 0
  const now = Date.now()
  const startAt = toTime(activity.startTime)
  const endAt = toTime(activity.endTime)
  if (endAt != null && now >= endAt) return 3
  if (startAt != null && now >= startAt) return 2
  return activity.status
}

const isActivityFull = (activity) => {
  if (!activity.maxParticipants) return false
  return (activity.currentParticipants || 0) >= activity.maxParticipants
}

const isRegisterTimeOpen = (activity) => {
  const startAt = toTime(activity.startTime)
  return startAt != null && Date.now() < startAt
}

const canOperateRegistration = (activity) => {
  return (
    account.value?.id &&
    account.value.role === 'ROLE_USER' &&
    isMember.value &&
    getActivityDisplayStatus(activity) === 1 &&
    isRegisterTimeOpen(activity)
  )
}

const canRegisterActivity = (activity) => {
  return canOperateRegistration(activity) && !isActivityFull(activity) && !activityRegisteredMap.value[activity.id]
}

const canCancelRegistration = (activity) => {
  return canOperateRegistration(activity) && !!activityRegisteredMap.value[activity.id]
}

const getCannotRegisterReason = (activity) => {
  const displayStatus = getActivityDisplayStatus(activity)
  if (displayStatus === 0) return '活动已取消'
  if (displayStatus === 2) return '活动进行中'
  if (displayStatus === 3) return '活动已结束'
  if (displayStatus !== 1) return '不可报名'
  if (!activity.startTime || !isRegisterTimeOpen(activity)) return '活动已开始'
  if (isActivityFull(activity)) return '名额已满'
  if (!isMember.value) return '加入社团后可报名'
  return '不可报名'
}

const submitRegister = async (activity) => {
  activitySubmittingMap.value[activity.id] = true
  try {
    const res = await registerActivity(activity.id)
    if (res.code === 200 || res.code === '200') {
      ElMessage.success(activity.needApproval === 1 ? '报名成功，等待审核' : '报名成功')
      await loadActivities()
    } else {
      ElMessage.error(res.msg || '报名失败')
    }
  } catch {
    ElMessage.error('报名失败')
  } finally {
    activitySubmittingMap.value[activity.id] = false
  }
}

const submitCancelRegistration = async (activity) => {
  activitySubmittingMap.value[activity.id] = true
  try {
    const res = await cancelActivityRegistration(activity.id)
    if (res.code === 200 || res.code === '200') {
      ElMessage.success('已取消报名')
      await loadActivities()
    } else {
      ElMessage.error(res.msg || '取消失败')
    }
  } catch {
    ElMessage.error('取消失败')
  } finally {
    activitySubmittingMap.value[activity.id] = false
  }
}

watch(
  () => route.params.id,
  () => {
    account.value = localStorage.getItem('account') ? JSON.parse(localStorage.getItem('account')) : {}
    notesPage.value.pageNum = 1
    membersPage.value.pageNum = 1
    loadClub()
    loadNotes()
    loadMembershipFlags()
    activities.value = []
    activityRegisteredMap.value = {}
    members.value = []
    activeMainTab.value = 'notes'
    loadActivities()
    loadMembers()
  }
)

onMounted(async () => {
  await loadClub()
  await loadNotes()
  await loadMembershipFlags()
  await loadActivities()
  await loadMembers()
})
</script>

<template>
  <div class="club-page" v-loading="clubLoading">
    <template v-if="club">
      <header class="club-header">
        <div class="club-header-bg" :style="club.coverUrl ? { backgroundImage: `url(${club.coverUrl})` } : {}" />
        <div class="club-header-inner">
          <el-avatar :src="club.avatarUrl" :size="72" class="club-avatar">
            {{ club.name?.charAt(0) }}
          </el-avatar>
          <div class="club-title-block">
            <h1 class="club-name">
              {{ club.name }}
              <el-icon v-if="club.isVerified === 1" class="verified"><CircleCheck /></el-icon>
            </h1>
            <p class="club-meta">
              <span v-if="club.category">{{ club.category }}</span>
              <span>{{ club.memberCount ?? 0 }} 成员</span>
            </p>
            <p v-if="club.description" class="club-desc">{{ club.description }}</p>
            <div class="club-actions">
              <template v-if="account?.id">
                <el-button v-if="!isMember" type="primary" @click="handleJoin">加入社团</el-button>
                <el-button v-else type="warning" plain @click="handleLeave">退出社团</el-button>
                <el-button v-if="isAdmin" type="primary" plain :icon="Setting" @click="goManage">
                  管理
                </el-button>
              </template>
              <el-button v-else @click="router.push('/login')">登录后加入</el-button>
            </div>
          </div>
        </div>
      </header>

      <div class="club-body">
        <el-tabs v-model="activeMainTab" class="club-tabs" @tab-change="onMainTab">
          <el-tab-pane label="动态" name="notes">
            <div v-loading="notesLoading" class="notes-grid">
              <div v-if="!notes.length" class="empty">暂无笔记</div>
              <div
                v-for="n in notes"
                :key="n.id"
                class="note-tile"
                @click="goNote(n.id)"
              >
                <el-image
                  v-if="getNoteCover(n.imageUrl)"
                  :src="getNoteCover(n.imageUrl)"
                  fit="cover"
                  class="tile-img"
                  lazy
                />
                <div v-else class="tile-text">{{ n.title }}</div>
                <div class="tile-title">{{ n.title }}</div>
              </div>
            </div>
            <div v-if="notesTotal > notesPage.pageSize" class="pager">
              <el-pagination
                background
                layout="prev, pager, next"
                :total="notesTotal"
                :page-size="notesPage.pageSize"
                :current-page="notesPage.pageNum"
                @current-change="handleNotesPage"
              />
            </div>
          </el-tab-pane>

          <el-tab-pane label="活动" name="activities">
            <div v-loading="activitiesLoading" class="act-list">
              <div v-if="!activities.length" class="empty">暂无活动</div>
              <el-card v-for="a in activities" :key="a.id" class="act-card" shadow="hover">
                <div class="act-cover-wrap" v-if="a.coverUrl">
                  <el-image :src="a.coverUrl" fit="cover" class="act-cover" />
                </div>
                <div class="act-top">
                  <div class="act-title">{{ a.title }}</div>
                  <el-tag :type="activityStatusType(getActivityDisplayStatus(a))" effect="light">
                    {{ activityStatusLabel(getActivityDisplayStatus(a)) }}
                  </el-tag>
                </div>
                <div class="act-sub">
                  {{ a.location || '地点待定' }} · {{ formatDateTime(a.startTime) }} — {{ formatDateTime(a.endTime) }}
                </div>
                <div class="act-meta">
                  <span>人数：{{ a.currentParticipants || 0 }}/{{ a.maxParticipants || '不限' }}</span>
                  <span>报名审核：{{ a.needApproval === 1 ? '需要' : '无需' }}</span>
                  <span v-if="a.tags">标签：{{ a.tags }}</span>
                </div>
                <p class="act-desc">{{ a.description }}</p>
                <div class="act-actions" v-if="account?.id && account.role === 'ROLE_USER'">
                  <el-button
                    v-if="canRegisterActivity(a)"
                    type="primary"
                    size="small"
                    :loading="activitySubmittingMap[a.id]"
                    @click="submitRegister(a)"
                  >
                    报名
                  </el-button>
                  <el-button
                    v-else-if="canCancelRegistration(a)"
                    type="warning"
                    plain
                    size="small"
                    :loading="activitySubmittingMap[a.id]"
                    @click="submitCancelRegistration(a)"
                  >
                    取消报名
                  </el-button>
                  <el-tag v-else-if="activityRegisteredMap[a.id]" type="info">已报名</el-tag>
                  <el-tag v-else :type="!isMember ? 'warning' : 'info'">
                    {{ getCannotRegisterReason(a) }}
                  </el-tag>
                </div>
              </el-card>
            </div>
          </el-tab-pane>

          <el-tab-pane label="成员" name="members">
            <div v-loading="membersLoading">
              <el-table v-if="members.length" :data="members" stripe style="width: 100%">
                <el-table-column prop="userId" label="用户ID" width="120" />
                <el-table-column label="角色" width="120">
                  <template #default="{ row }">{{ roleLabel(row.role) }}</template>
                </el-table-column>
                <el-table-column prop="joinTime" label="加入时间" min-width="180" />
              </el-table>
              <div v-else class="empty">暂无成员数据</div>
              <div v-if="membersTotal > membersPage.pageSize" class="pager">
                <el-pagination
                  background
                  layout="prev, pager, next"
                  :total="membersTotal"
                  :page-size="membersPage.pageSize"
                  :current-page="membersPage.pageNum"
                  @current-change="handleMembersPage"
                />
              </div>
            </div>
          </el-tab-pane>
        </el-tabs>
      </div>
    </template>
    <div v-else-if="!clubLoading" class="empty-page">社团不存在或已下线</div>
  </div>
</template>

<style scoped lang="scss">
.club-page {
  max-width: 960px;
  margin: 0 auto;
  padding: 24px 16px 48px;
  min-height: calc(100vh - 90px);
  font-size: 18px;
}

.club-page :deep(.el-tabs__item) {
  font-size: 18px;
}

.club-page :deep(.el-button) {
  font-size: 18px;
}

.club-page :deep(.el-table) {
  font-size: 18px;
}

.club-page :deep(.el-pagination) {
  font-size: 18px;
}

.club-page :deep(.el-card__body) {
  font-size: 18px;
}

.club-header {
  position: relative;
  border-radius: var(--bb-radius-lg, 12px);
  overflow: hidden;
  margin-bottom: 24px;
  border: 1px solid var(--bb-border);
  background: var(--bb-bg-card);
}

.club-header-bg {
  height: 120px;
  background-size: cover;
  background-position: center;
  background-color: var(--bb-brand-soft);
}

.club-header-inner {
  display: flex;
  gap: 20px;
  padding: 20px 24px 24px;
  align-items: flex-start;
}

.club-avatar {
  margin-top: -48px;
  border: 3px solid var(--bb-bg-card);
  flex-shrink: 0;
}

.club-title-block {
  flex: 1;
  min-width: 0;
}

.club-name {
  margin: 0 0 8px;
  font-size: 26px;
  font-weight: 700;
  display: flex;
  align-items: center;
  gap: 8px;
  color: var(--bb-text-primary);
}

.verified {
  color: var(--bb-brand);
}

.club-meta {
  margin: 0 0 8px;
  font-size: 17px;
  color: var(--bb-text-muted);
  display: flex;
  gap: 12px;
}

.club-desc {
  margin: 0 0 12px;
  font-size: 18px;
  color: var(--bb-text-secondary);
  line-height: 1.5;
}

.club-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.club-body {
  background: var(--bb-bg-card);
  border: 1px solid var(--bb-border);
  border-radius: var(--bb-radius-lg, 12px);
  padding: 8px 16px 20px;
}

.notes-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 16px;
  min-height: 120px;
}

.note-tile {
  border-radius: 10px;
  overflow: hidden;
  border: 1px solid var(--bb-border);
  cursor: pointer;
  background: var(--bb-bg-page);
  transition: box-shadow 0.2s;
}

.note-tile:hover {
  box-shadow: var(--bb-shadow-card);
}

.tile-img {
  width: 100%;
  height: 240px;
  display: block;
}

.tile-text {
  height: 140px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 12px;
  font-size: 18px;
  color: var(--bb-text-secondary);
  background: var(--bb-brand-soft);
}

.tile-title {
  padding: 10px 12px;
  font-size: 18px;
  font-weight: 600;
  color: var(--bb-text-primary);
  line-height: 1.35;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.act-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
  min-height: 80px;
}

.act-card {
  border-radius: 10px;
}

.act-cover-wrap {
  margin: -6px -6px 10px;
}

.act-cover {
  width: 100%;
  height: 180px;
  display: block;
  border-radius: 8px;
}

.act-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
}

.act-title {
  font-weight: 600;
  margin-bottom: 6px;
  font-size: 18px;
}

.act-sub {
  font-size: 16px;
  color: var(--bb-text-muted);
  margin-bottom: 8px;
}

.act-desc {
  margin: 0;
  font-size: 17px;
  color: var(--bb-text-secondary);
  line-height: 1.45;
}

.act-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 8px 14px;
  font-size: 15px;
  color: var(--bb-text-muted);
  margin-bottom: 8px;
}

.act-actions {
  margin-top: 10px;
}

.pager {
  margin-top: 16px;
  display: flex;
  justify-content: center;
}

.empty,
.empty-page {
  text-align: center;
  padding: 40px 16px;
  color: var(--bb-text-muted);
  font-size: 18px;
}
</style>
