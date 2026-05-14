<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { Plus, Check, User as UserIcon, UserFilled, Star, Document, View, ChatDotRound, OfficeBuilding } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { updateNote, deleteNote } from '@/api/note'
import { getMyClubs } from '@/api/clubMember'
import { getClubById } from '@/api/club'
import { serverHost } from '../../../config/config.default'
import request from '@/utils/request'
import { getFollowingList, getFollowersList, unfollowUser, followUser } from '@/api/follow'
import { formatTime } from '@/utils/date'

const router = useRouter()

// 表单数据
const form = reactive({})
const verifyForm = reactive({
  studentId: '',
  realName: '',
  school: '',
  college: '',
  major: '',
  grade: ''
})

// 用户信息
const account = ref(
    localStorage.getItem('account') ? JSON.parse(localStorage.getItem('account')) : {}
)

// 当前激活的标签页
const activeTab = ref('info')

// 关注列表
const followingList = ref([])
const followingLoading = ref(false)
const followingPage = ref({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

// 粉丝列表
const followersList = ref([])
const followersLoading = ref(false)
const followersPage = ref({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

// 我的笔记列表
const notesList = ref([])
const notesLoading = ref(false)
const notesPage = ref({
  pageNum: 1,
  pageSize: 12,
  total: 0
})

// 我的收藏列表
const collectsList = ref([])
const collectsLoading = ref(false)
const collectsPage = ref({
  pageNum: 1,
  pageSize: 12,
  total: 0
})

const myClubsRows = ref([])
const clubsLoading = ref(false)
const clubNameCache = ref({})

// 获取用户信息
const getAccount = () => {
  request.get('/web/userInfo').then(res => {
    if (res.code === '200' && res.data) {
      Object.assign(form, res.data)
      verifyForm.studentId = res.data.studentId || ''
      verifyForm.realName = res.data.realName || ''
      verifyForm.school = res.data.school || ''
      verifyForm.college = res.data.college || ''
      verifyForm.major = res.data.major || ''
      verifyForm.grade = res.data.grade || ''
      // 同步认证状态到 localStorage，避免其他页面读到旧数据
      account.value = { ...account.value, ...res.data }
      localStorage.setItem('account', JSON.stringify(account.value))
    } else {
      ElMessage.error(res.msg)
    }
  })
}
getAccount()

// 定义要发出的事件
const emit = defineEmits(['refreshUser'])

// 保存用户信息
const save = () => {
  request.post('/user', form).then(res => {
    if (res.code === '200') {
      ElMessage.success('保存成功')

      // 只更新昵称和头像到 account 对象，其他属性保持不变
      if (form.nickname) account.value.nickname = form.nickname
      if (form.avatarUrl) account.value.avatarUrl = form.avatarUrl

      // 更新浏览器存储的用户信息
      localStorage.setItem('account', JSON.stringify(account.value))

      // 向父组件发送更新事件，传递更新后的用户信息
      emit('updateAccount', account.value)

    } else {
      ElMessage.error(res.msg || '保存失败')
    }
  })
}

const verifyStatusText = (status) => {
  if (status === 1) return '已认证'
  if (status === 2) return '待审核'
  return '未认证'
}

const verifyStatusType = (status) => {
  if (status === 1) return 'success'
  if (status === 2) return 'warning'
  return 'info'
}

const canSubmitVerification = computed(() => form.isVerified !== 1 && form.isVerified !== 2)

const verifySubmitButtonText = computed(() => {
  if (form.isVerified === 1) return '已认证'
  if (form.isVerified === 2) return '审核中'
  return '提交校园认证'
})

const submitVerification = () => {
  if (form.isVerified === 1) {
    ElMessage.warning('已认证，无需重复提交')
    return
  }
  if (form.isVerified === 2) {
    ElMessage.warning('认证申请正在审核中，请勿重复提交')
    return
  }
  if (!verifyForm.studentId || !verifyForm.realName || !verifyForm.school) {
    ElMessage.warning('请填写学号、真实姓名、学校')
    return
  }
  request.post('/user/verify/apply', { ...verifyForm }).then(res => {
    if (res.code === '200' || res.code === 200) {
      ElMessage.success('提交成功，等待管理员审核')
      getAccount()
    } else {
      ElMessage.error(res.msg || '提交失败')
    }
  })
}

// 头像上传成功处理
const handleAvatarSuccess = (res) => {
  form.avatarUrl = res
}

// 加载我的关注列表
const loadFollowingList = async () => {
  if (!account.value || !account.value.id) return
  
  followingLoading.value = true
  try {
    const res = await getFollowingList(
      account.value.id, 
      followingPage.value.pageNum, 
      followingPage.value.pageSize
    )
    if (res.code === 200 || res.code === '200') {
      followingList.value = res.data.records || []
      followingPage.value.total = res.data.total || 0
    }
  } catch (error) {
    console.error('加载关注列表失败', error)
    ElMessage.error('加载关注列表失败')
  } finally {
    followingLoading.value = false
  }
}

// 加载我的粉丝列表
const loadFollowersList = async () => {
  if (!account.value || !account.value.id) return
  
  followersLoading.value = true
  try {
    const res = await getFollowersList(
      account.value.id, 
      followersPage.value.pageNum, 
      followersPage.value.pageSize
    )
    if (res.code === 200 || res.code === '200') {
      followersList.value = res.data.records || []
      followersPage.value.total = res.data.total || 0
    }
  } catch (error) {
    console.error('加载粉丝列表失败', error)
    ElMessage.error('加载粉丝列表失败')
  } finally {
    followersLoading.value = false
  }
}

// 取消关注
const handleUnfollow = async (userId) => {
  if (!account.value || !account.value.id) return
  
  try {
    const res = await unfollowUser(account.value.id, userId)
    if (res.code === 200 || res.code === '200') {
      ElMessage.success('已取消关注')
      // 刷新列表
      loadFollowingList()
      // 更新用户统计信息
      getAccount()
    } else {
      ElMessage.error(res.msg || '操作失败')
    }
  } catch (error) {
    console.error('取消关注失败', error)
    ElMessage.error('操作失败')
  }
}

// 回关（关注粉丝）
const handleFollowBack = async (userId) => {
  if (!account.value || !account.value.id) return
  
  try {
    const res = await followUser(account.value.id, userId)
    if (res.code === 200 || res.code === '200') {
      ElMessage.success('关注成功')
      // 刷新列表
      loadFollowersList()
      // 更新用户统计信息
      getAccount()
    } else {
      ElMessage.error(res.msg || '操作失败')
    }
  } catch (error) {
    console.error('关注失败', error)
    ElMessage.error('操作失败')
  }
}

// 加载我的收藏列表
const loadCollectsList = async () => {
  if (!account.value || !account.value.id) return
  
  collectsLoading.value = true
  try {
    const res = await request.get('/api/userCollect/myCollectedNotes', {
      params: {
        userId: account.value.id,
        pageNum: collectsPage.value.pageNum,
        pageSize: collectsPage.value.pageSize
      }
    })
    if (res.code === 200 || res.code === '200') {
      collectsList.value = res.data.records || []
      collectsPage.value.total = res.data.total || 0
    }
  } catch (error) {
    console.error('加载收藏列表失败', error)
    ElMessage.error('加载收藏列表失败')
  } finally {
    collectsLoading.value = false
  }
}

// 标签页切换
const handleTabChange = (tabName) => {
  if (tabName === 'following') {
    loadFollowingList()
  } else if (tabName === 'followers') {
    loadFollowersList()
  } else if (tabName === 'notes') {
    loadNotesList()
  } else if (tabName === 'collects') {
    loadCollectsList()
  } else if (tabName === 'clubs') {
    loadMyClubs()
  }
}

const clubRoleLabel = (role) => {
  if (role === 3) return '社长'
  if (role === 2) return '管理员'
  return '成员'
}

const loadMyClubs = async () => {
  if (!account.value?.id) return
  clubsLoading.value = true
  try {
    const res = await getMyClubs()
    if (res.code !== 200 && res.code !== '200') {
      myClubsRows.value = []
      return
    }
    const rows = res.data || []
    const enriched = []
    for (const m of rows) {
      let clubName = `社团 #${m.clubId}`
      try {
        const cr = await getClubById(m.clubId)
        if ((cr.code === 200 || cr.code === '200') && cr.data?.name) {
          clubName = cr.data.name
        }
      } catch {
        /* ignore */
      }
      enriched.push({ ...m, clubName })
    }
    myClubsRows.value = enriched
  } catch (error) {
    console.error('加载社团失败', error)
    ElMessage.error('加载社团失败')
    myClubsRows.value = []
  } finally {
    clubsLoading.value = false
  }
}

const goToClub = (clubId) => {
  router.push(`/front/club/${clubId}`)
}

const resolveClubName = async (clubId) => {
  if (!clubId) return ''
  if (clubNameCache.value[clubId]) {
    return clubNameCache.value[clubId]
  }
  try {
    const res = await getClubById(clubId)
    if ((res.code === 200 || res.code === '200') && res.data?.name) {
      clubNameCache.value[clubId] = res.data.name
      return res.data.name
    }
  } catch {
    // ignore
  }
  const fallback = `社团 #${clubId}`
  clubNameCache.value[clubId] = fallback
  return fallback
}

// 加载我的笔记列表
const loadNotesList = async () => {
  if (!account.value || !account.value.id) return
  
  notesLoading.value = true
  try {
    const res = await request.get('/api/note/page', {
      params: {
        userId: account.value.id,
        pageNum: notesPage.value.pageNum,
        pageSize: notesPage.value.pageSize
      }
    })
    if (res.code === 200 || res.code === '200') {
      const rows = res.data.records || []
      const enriched = await Promise.all(rows.map(async (note) => {
        if (!note.clubId) return note
        const clubName = await resolveClubName(note.clubId)
        return { ...note, clubName }
      }))
      notesList.value = enriched
      notesPage.value.total = res.data.total || 0
    }
  } catch (error) {
    console.error('加载笔记列表失败', error)
    ElMessage.error('加载笔记列表失败')
  } finally {
    notesLoading.value = false
  }
}

// 笔记列表分页
const handleNotesPageChange = (page) => {
  notesPage.value.pageNum = page
  loadNotesList()
}

// 收藏列表分页
const handleCollectsPageChange = (page) => {
  collectsPage.value.pageNum = page
  loadCollectsList()
}

// 跳转到笔记详情
const goToNote = (noteId) => {
  router.push(`/front/note/${noteId}`)
}

const noteCornerLabel = (note) => {
  if (note.status === 0) return '草稿'
  if (note.status === 2) return '下架'
  return ''
}

const handleNotePublish = (row) => {
  ElMessageBox.confirm('确定上架发布该笔记？', '提示', { type: 'info' })
    .then(async () => {
      const res = await updateNote({ ...JSON.parse(JSON.stringify(row)), status: 1 })
      if (res.code === 200 || res.code === '200') {
        ElMessage.success('已发布')
        loadNotesList()
      } else {
        ElMessage.error(res.msg || '操作失败')
      }
    })
    .catch(() => {})
}

const handleNoteOffline = (row) => {
  ElMessageBox.confirm('确定下架该笔记？访客将无法看到。', '提示', { type: 'warning' })
    .then(async () => {
      const res = await updateNote({ ...JSON.parse(JSON.stringify(row)), status: 2 })
      if (res.code === 200 || res.code === '200') {
        ElMessage.success('已下架')
        loadNotesList()
      } else {
        ElMessage.error(res.msg || '操作失败')
      }
    })
    .catch(() => {})
}

const handleNoteDelete = (row) => {
  ElMessageBox.confirm('确定删除该笔记？', '警告', { type: 'warning' })
    .then(async () => {
      const res = await deleteNote(row.id)
      if (res.code === 200 || res.code === '200') {
        ElMessage.success('已删除')
        loadNotesList()
      } else {
        ElMessage.error(res.msg || '删除失败')
      }
    })
    .catch(() => {})
}

// 格式化时间

// 关注列表分页
const handleFollowingPageChange = (page) => {
  followingPage.value.pageNum = page
  loadFollowingList()
}

// 粉丝列表分页
const handleFollowersPageChange = (page) => {
  followersPage.value.pageNum = page
  loadFollowersList()
}

// 组件挂载时加载数据
onMounted(() => {
  if (activeTab.value === 'following') {
    loadFollowingList()
  } else if (activeTab.value === 'followers') {
    loadFollowersList()
  } else if (activeTab.value === 'clubs') {
    loadMyClubs()
  }
})

</script>

<template>
  <div class="person-container">
    <div class="person-card">
      <div class="person-layout">
        <aside class="person-sidebar">
          <div class="avatar-section">
            <div class="avatar-wrapper">
              <el-upload
                :action="`${serverHost}/web/upload`"
                :show-file-list="false"
                :on-success="handleAvatarSuccess"
                class="avatar-uploader"
              >
                <div class="avatar-container">
                  <img v-if="form.avatarUrl" :src="form.avatarUrl" class="avatar">
                  <div v-else class="avatar-placeholder">
                    <el-icon class="avatar-icon"><Plus /></el-icon>
                  </div>
                  <div class="avatar-overlay">
                    <el-icon class="upload-icon"><Plus /></el-icon>
                    <span>更换头像</span>
                  </div>
                </div>
              </el-upload>
            </div>
            <h2 class="user-name">{{ form.nickname || form.username || '未设置昵称' }}</h2>
            <p class="user-role">普通用户</p>

            <div class="user-stats">
              <div class="stat-item" @click="activeTab = 'notes'; handleTabChange('notes')">
                <div class="stat-number">{{ form.notesCount || 0 }}</div>
                <div class="stat-label">笔记</div>
              </div>
              <div class="stat-item" @click="activeTab = 'following'; handleTabChange('following')">
                <div class="stat-number">{{ form.followingCount || 0 }}</div>
                <div class="stat-label">关注</div>
              </div>
              <div class="stat-item" @click="activeTab = 'followers'; handleTabChange('followers')">
                <div class="stat-number">{{ form.followersCount || 0 }}</div>
                <div class="stat-label">粉丝</div>
              </div>
              <div class="stat-item">
                <div class="stat-number">{{ form.likesReceived || 0 }}</div>
                <div class="stat-label">获赞</div>
              </div>
            </div>
          </div>
        </aside>

        <div class="person-main">
          <el-tabs v-model="activeTab" @tab-change="handleTabChange" class="person-tabs">
        <!-- 个人信息标签 -->
        <el-tab-pane label="个人信息" name="info">
          <div class="tab-content">

        <el-form label-width="104px" label-position="left" size="default">
          <div class="form-group">
            <div class="form-group-title">
              <span class="title-icon">📝</span>
              <span>基本信息</span>
            </div>
            
            <el-form-item label="用户名">
              <el-input 
                v-model="form.username" 
                disabled 
                autocomplete="off"
                prefix-icon="User"
              ></el-input>
            </el-form-item>
            
            <el-form-item label="昵称">
              <el-input 
                v-model="form.nickname" 
                autocomplete="off"
                placeholder="请输入昵称"
                prefix-icon="EditPen"
              ></el-input>
            </el-form-item>
          </div>

          <div class="form-group">
            <div class="form-group-title">
              <span class="title-icon">📧</span>
              <span>联系方式</span>
            </div>
            
            <el-form-item label="邮箱">
              <el-input 
                v-model="form.email" 
                autocomplete="off"
                placeholder="请输入邮箱"
                prefix-icon="Message"
              ></el-input>
            </el-form-item>
            
            <el-form-item label="电话">
              <el-input 
                v-model="form.phone" 
                autocomplete="off"
                placeholder="请输入电话"
                prefix-icon="Phone"
              ></el-input>
            </el-form-item>
          </div>

          <div class="form-group">
            <div class="form-group-title">
              <span class="title-icon">🏫</span>
              <span>校园认证</span>
              <el-tag class="ml-10" :type="verifyStatusType(form.isVerified)">
                {{ verifyStatusText(form.isVerified) }}
              </el-tag>
            </div>

            <el-form-item label="学号" required>
              <el-input v-model="verifyForm.studentId" :disabled="!canSubmitVerification" autocomplete="off" placeholder="请输入学号" />
            </el-form-item>
            <el-form-item label="真实姓名" required>
              <el-input v-model="verifyForm.realName" :disabled="!canSubmitVerification" autocomplete="off" placeholder="请输入真实姓名" />
            </el-form-item>
            <el-form-item label="学校" required>
              <el-input v-model="verifyForm.school" :disabled="!canSubmitVerification" autocomplete="off" placeholder="请输入学校名称" />
            </el-form-item>
            <el-form-item label="学院">
              <el-input v-model="verifyForm.college" :disabled="!canSubmitVerification" autocomplete="off" placeholder="请输入学院" />
            </el-form-item>
            <el-form-item label="专业">
              <el-input v-model="verifyForm.major" :disabled="!canSubmitVerification" autocomplete="off" placeholder="请输入专业" />
            </el-form-item>
            <el-form-item label="年级">
              <el-input v-model="verifyForm.grade" :disabled="!canSubmitVerification" autocomplete="off" placeholder="请输入年级" />
            </el-form-item>
            <el-form-item>
              <el-button type="warning" :disabled="!canSubmitVerification" @click="submitVerification">{{ verifySubmitButtonText }}</el-button>
              <span v-if="form.verifyTime" class="verify-time-tip">认证时间：{{ formatTime(form.verifyTime) }}</span>
            </el-form-item>
          </div>

          <div class="form-actions">
            <el-button type="primary" size="large" @click="save" class="save-btn">
              <el-icon class="btn-icon"><Check /></el-icon>
              保存修改
            </el-button>
          </div>
        </el-form>
      </div>
        </el-tab-pane>

        <!-- 我的关注标签 -->
        <el-tab-pane name="following">
          <template #label>
            <span class="tab-label">
              <el-icon><UserFilled /></el-icon>
              我的关注
            </span>
          </template>
          <div class="tab-content">
            <div v-loading="followingLoading" class="user-list">
              <div v-if="followingList.length === 0" class="empty-state">
                <el-icon class="empty-icon"><UserIcon /></el-icon>
                <p>还没有关注任何人</p>
              </div>
              <div 
                v-for="user in followingList" 
                :key="user.id"
                class="user-item"
              >
                <el-avatar :src="user.avatarUrl" :size="50">
                  {{ user.nickname?.charAt(0) }}
                </el-avatar>
                <div class="user-info">
                  <h4>{{ user.nickname || user.username }}</h4>
                  <p>粉丝 {{ user.followersCount || 0 }} · 获赞 {{ user.likesReceived || 0 }}</p>
                </div>
                <el-button 
                  type="info" 
                  size="small"
                  @click="handleUnfollow(user.id)"
                >
                  已关注
                </el-button>
              </div>
            </div>
            <div v-if="followingPage.total > followingPage.pageSize" class="pagination">
              <el-pagination
                :current-page="followingPage.pageNum"
                :page-size="followingPage.pageSize"
                :total="followingPage.total"
                layout="prev, pager, next"
                @current-change="handleFollowingPageChange"
              />
            </div>
          </div>
        </el-tab-pane>

        <!-- 我的笔记标签 -->
        <el-tab-pane name="notes">
          <template #label>
            <span class="tab-label">
              <el-icon><Document /></el-icon>
              我的笔记
            </span>
          </template>
          <div class="tab-content notes-tab-content">
            <div v-loading="notesLoading" class="my-notes-waterfall">
              <div v-if="notesList.length === 0" class="empty-state">
                <el-icon class="empty-icon"><Document /></el-icon>
                <p>还没有笔记</p>
                <el-button type="primary" @click="router.push('/front/publish')">去发布</el-button>
              </div>
              <div 
                v-for="note in notesList" 
                :key="note.id"
                class="my-note-card"
                @click="goToNote(note.id)"
              >
                <!-- 封面图片 -->
                <div class="my-note-cover">
                  <span v-if="noteCornerLabel(note)" class="note-corner-badge">{{ noteCornerLabel(note) }}</span>
                  <el-image 
                    v-if="note.imageUrl"
                    :src="note.imageUrl.split(',')[0]" 
                    fit="cover"
                    lazy
                    class="cover-image"
                  >
                    <template #error>
                      <div class="image-error">
                        <el-icon><Document /></el-icon>
                      </div>
                    </template>
                  </el-image>
                  <div v-else class="no-cover">
                    <el-icon><Document /></el-icon>
                  </div>
                  <div class="hover-overlay" @click.stop>
                    <span class="view-text" @click="goToNote(note.id)">查看详情</span>
                    <div v-if="account?.id === note.userId" class="my-note-owner-actions">
                      <el-button v-if="note.status === 1" size="small" @click="handleNoteOffline(note)">下架</el-button>
                      <el-button v-if="note.status === 2 || note.status === 0" size="small" type="primary" @click="handleNotePublish(note)">上架</el-button>
                      <el-button size="small" type="danger" @click="handleNoteDelete(note)">删除</el-button>
                    </div>
                  </div>
                </div>
                <!-- 笔记内容 -->
                <div class="my-note-content">
                  <h3 class="my-note-title">{{ note.title }}</h3>
                  <div v-if="note.clubId" class="my-note-club">
                    {{ note.clubName || `社团 #${note.clubId}` }}
                  </div>
                  <div class="my-note-tags" v-if="note.tags">
                    <el-tag 
                      v-for="tag in note.tags.split(',').slice(0, 2)" 
                      :key="tag"
                      size="small"
                      type="info"
                      effect="plain"
                    >
                      #{{ tag }}
                    </el-tag>
                  </div>
                  <div class="my-note-footer">
                    <div class="my-note-stats">
                      <span class="stat-item">
                        <el-icon><View /></el-icon>
                        {{ note.views || 0 }}
                      </span>
                      <span class="stat-item">
                        <el-icon color="var(--el-color-danger)"><Star /></el-icon>
                        {{ note.likes || 0 }}
                      </span>
                      <span class="stat-item">
                        <el-icon><ChatDotRound /></el-icon>
                        {{ note.comments || 0 }}
                      </span>
                    </div>
                    <span class="my-note-time">{{ formatTime(note.createTime) }}</span>
                  </div>
                </div>
              </div>
            </div>
            <div v-if="notesPage.total > notesPage.pageSize" class="pagination">
              <el-pagination
                :current-page="notesPage.pageNum"
                :page-size="notesPage.pageSize"
                :total="notesPage.total"
                layout="prev, pager, next"
                @current-change="handleNotesPageChange"
              />
            </div>
          </div>
        </el-tab-pane>

        <!-- 我的收藏标签 -->
        <el-tab-pane name="collects">
          <template #label>
            <span class="tab-label">
              <el-icon><Star /></el-icon>
              我的收藏
            </span>
          </template>
          <div class="tab-content notes-tab-content">
            <div v-loading="collectsLoading" class="my-notes-waterfall">
              <div v-if="collectsList.length === 0" class="empty-state">
                <el-icon class="empty-icon"><Star /></el-icon>
                <p>还没有收藏任何笔记</p>
              </div>
              <div 
                v-for="note in collectsList" 
                :key="note.id"
                class="my-note-card"
                @click="goToNote(note.id)"
              >
                <!-- 封面图片 -->
                <div class="my-note-cover">
                  <el-image 
                    v-if="note.imageUrl"
                    :src="note.imageUrl.split(',')[0]" 
                    fit="cover"
                    lazy
                    class="cover-image"
                  >
                    <template #error>
                      <div class="image-error">
                        <el-icon><Document /></el-icon>
                      </div>
                    </template>
                  </el-image>
                  <div v-else class="no-cover">
                    <el-icon><Document /></el-icon>
                  </div>
                  <!-- 悬停操作 -->
                  <div class="hover-overlay">
                    <span class="view-text">查看详情</span>
                  </div>
                </div>
                <!-- 笔记内容 -->
                <div class="my-note-content">
                  <h3 class="my-note-title">{{ note.title }}</h3>
                  <div class="my-note-tags" v-if="note.tags">
                    <el-tag 
                      v-for="tag in note.tags.split(',').slice(0, 2)" 
                      :key="tag"
                      size="small"
                      type="info"
                      effect="plain"
                    >
                      #{{ tag }}
                    </el-tag>
                  </div>
                  <div class="my-note-footer">
                    <div class="my-note-stats">
                      <span class="stat-item">
                        <el-icon><View /></el-icon>
                        {{ note.views || 0 }}
                      </span>
                      <span class="stat-item">
                        <el-icon color="var(--el-color-danger)"><Star /></el-icon>
                        {{ note.collects || 0 }}
                      </span>
                      <span class="stat-item">
                        <el-icon><ChatDotRound /></el-icon>
                        {{ note.comments || 0 }}
                      </span>
                    </div>
                    <span class="my-note-time">{{ formatTime(note.createTime) }}</span>
                  </div>
                </div>
              </div>
            </div>
            <div v-if="collectsPage.total > collectsPage.pageSize" class="pagination">
              <el-pagination
                :current-page="collectsPage.pageNum"
                :page-size="collectsPage.pageSize"
                :total="collectsPage.total"
                layout="prev, pager, next"
                @current-change="handleCollectsPageChange"
              />
            </div>
          </div>
        </el-tab-pane>

        <el-tab-pane name="clubs">
          <template #label>
            <span class="tab-label">
              <el-icon><OfficeBuilding /></el-icon>
              我的社团
            </span>
          </template>
          <div class="tab-content">
            <div v-loading="clubsLoading" class="clubs-list">
              <div v-if="myClubsRows.length === 0" class="empty-state">
                <el-icon class="empty-icon"><OfficeBuilding /></el-icon>
                <p>尚未加入任何社团</p>
                <el-button type="primary" @click="router.push('/front/home')">去首页看看</el-button>
              </div>
              <div
                v-for="row in myClubsRows"
                :key="row.id"
                class="club-row"
                @click="goToClub(row.clubId)"
              >
                <div class="club-row-main">
                  <div class="club-row-title">{{ row.clubName }}</div>
                  <div class="club-row-sub">我在本社：{{ clubRoleLabel(row.role) }}</div>
                </div>
                <el-button type="primary" text @click.stop="goToClub(row.clubId)">进入</el-button>
              </div>
            </div>
          </div>
        </el-tab-pane>

        <!-- 我的粉丝标签 -->
        <el-tab-pane name="followers">
          <template #label>
            <span class="tab-label">
              <el-icon><Star /></el-icon>
              我的粉丝
            </span>
          </template>
          <div class="tab-content">
            <div v-loading="followersLoading" class="user-list">
              <div v-if="followersList.length === 0" class="empty-state">
                <el-icon class="empty-icon"><Star /></el-icon>
                <p>还没有粉丝</p>
              </div>
              <div 
                v-for="user in followersList" 
                :key="user.id"
                class="user-item"
              >
                <el-avatar :src="user.avatarUrl" :size="50">
                  {{ user.nickname?.charAt(0) }}
                </el-avatar>
                <div class="user-info">
                  <h4>{{ user.nickname || user.username }}</h4>
                  <p>粉丝 {{ user.followersCount || 0 }} · 获赞 {{ user.likesReceived || 0 }}</p>
                </div>
                <el-button 
                  type="primary" 
                  size="small"
                  @click="handleFollowBack(user.id)"
                >
                  回关
                </el-button>
              </div>
            </div>
            <div v-if="followersPage.total > followersPage.pageSize" class="pagination">
              <el-pagination
                :current-page="followersPage.pageNum"
                :page-size="followersPage.pageSize"
                :total="followersPage.total"
                layout="prev, pager, next"
                @current-change="handleFollowersPageChange"
              />
            </div>
          </div>
        </el-tab-pane>
          </el-tabs>
        </div>
      </div>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.person-container {
  min-height: calc(100vh - 60px);
  background: var(--bb-bg-page);

  :deep(.el-pagination),
  :deep(.el-pagination button),
  :deep(.el-pagination .el-pager li) {
    font-size: 18px;
  }

  :deep(.el-button--small) {
    font-size: 16px;
  }

  :deep(.el-button:not(.el-button--small):not(.save-btn)) {
    font-size: 18px;
  }
  display: flex;
  align-items: flex-start;
  justify-content: center;
  padding: 24px 32px 40px;
  box-sizing: border-box;

  .person-card {
    max-width: min(1280px, 100%);
    width: 100%;
    background: var(--bb-bg-card);
    border-radius: var(--bb-radius-card);
    border: 1px solid var(--bb-border);
    box-shadow: var(--bb-shadow-card);
    overflow: hidden;
    position: relative;
    animation: slideUp 0.5s ease-out;
    border-top: 3px solid var(--bb-brand);

    .person-layout {
      display: flex;
      align-items: stretch;
      min-height: 420px;
    }

    .person-sidebar {
      flex: 0 0 260px;
      border-right: 1px solid var(--bb-border);
      background: var(--bb-bg-page);
      padding: 20px 16px 24px;
      box-sizing: border-box;
    }

    .person-main {
      flex: 1;
      min-width: 0;
    }

    .avatar-section {
      text-align: center;
      padding: 0;
      position: relative;

      .avatar-wrapper {
        display: inline-block;
        margin-bottom: 12px;

        .avatar-uploader {
          cursor: pointer;

          .avatar-container {
            position: relative;
            width: 104px;
            height: 104px;
            border-radius: 50%;
            overflow: hidden;
            border: 4px solid var(--bb-bg-card);
            box-shadow: var(--bb-shadow-card);
            transition: box-shadow 0.2s ease, transform 0.2s ease;

            &:hover {
              transform: translateY(-2px);
              box-shadow: var(--bb-shadow-card-hover);

              .avatar-overlay {
                opacity: 1;
              }
            }

            .avatar {
              width: 100%;
              height: 100%;
              object-fit: cover;
              display: block;
            }

            .avatar-placeholder {
              width: 100%;
              height: 100%;
              background: linear-gradient(135deg, var(--bb-brand) 0%, var(--bb-brand-hover) 100%);
              display: flex;
              align-items: center;
              justify-content: center;

              .avatar-icon {
                font-size: 40px;
                color: white;
              }
            }

            .avatar-overlay {
              position: absolute;
              top: 0;
              left: 0;
              right: 0;
              bottom: 0;
              background: rgba(0, 0, 0, 0.55);
              display: flex;
              flex-direction: column;
              align-items: center;
              justify-content: center;
              opacity: 0;
              transition: opacity 0.2s ease;
              color: white;

              .upload-icon {
                font-size: 28px;
                margin-bottom: 4px;
              }

              span {
                font-size: 16px;
                font-weight: 500;
              }
            }
          }
        }
      }

      .user-name {
        font-size: 24px;
        font-weight: 600;
        letter-spacing: 0.02em;
        color: var(--bb-text-primary);
        margin: 0 0 6px 0;
        line-height: 1.35;
        word-break: break-word;
      }

      .user-role {
        font-size: 17px;
        color: var(--bb-text-secondary);
        margin: 0 0 16px 0;
        padding: 4px 14px;
        background: var(--bb-bg-card);
        border-radius: var(--bb-radius-pill);
        display: inline-block;
        border: 1px solid var(--bb-border);
      }

      .user-stats {
        display: grid;
        grid-template-columns: 1fr 1fr;
        gap: 8px 10px;
        margin-top: 0;
        padding-top: 16px;
        border-top: 1px solid var(--bb-border);

        .stat-item {
          text-align: center;
          cursor: pointer;
          transition: background 0.2s ease;
          padding: 8px 6px;
          border-radius: 8px;

          &:hover {
            background: var(--bb-brand-soft);
          }

          .stat-number {
            font-size: 22px;
            font-weight: 700;
            color: var(--bb-brand);
            margin-bottom: 2px;
            line-height: 1.2;
          }

          .stat-label {
            font-size: 16px;
            color: var(--bb-text-secondary);
            font-weight: 500;
          }
        }
      }
    }

    .person-main .person-tabs {
      padding: 0 24px 32px;

      :deep(.el-tabs__header) {
        margin-bottom: 18px;
      }

      :deep(.el-tabs__nav-wrap::after) {
        height: 1px;
        background-color: var(--bb-border);
      }

      :deep(.el-tabs__item) {
        font-size: 19px;
        font-weight: 500;
        color: var(--bb-text-secondary);
        padding: 0 18px;

        &.is-active {
          color: var(--bb-brand);
          font-weight: 600;
        }
      }

      :deep(.el-tabs__active-bar) {
        background-color: var(--bb-brand);
        height: 3px;
      }

      .tab-label {
        display: flex;
        align-items: center;
        gap: 6px;
      }

      .tab-content {
        min-height: 360px;

        .form-group {
          margin-bottom: 22px;

          &:last-of-type {
            margin-bottom: 0;
          }

          .form-group-title {
            font-size: 21px;
            font-weight: 600;
            color: var(--bb-text-primary);
            margin-bottom: 14px;
            padding-bottom: 10px;
            border-bottom: 2px solid var(--bb-border);
            display: flex;
            align-items: center;

            .title-icon {
              margin-right: 8px;
              font-size: 22px;
            }
          }

          :deep(.el-form-item) {
            margin-bottom: 18px;

            .el-form-item__label {
              font-weight: 500;
              color: var(--bb-text-secondary);
              font-size: 18px;
            }

            .el-input {
              .el-input__wrapper {
                font-size: 18px;
                border-radius: 8px;
                padding: 8px 14px;
                box-shadow: 0 0 0 1px var(--bb-border) inset;
                transition: box-shadow 0.2s ease;

                &:hover {
                  box-shadow: 0 0 0 1px var(--bb-brand-muted) inset;
                }

                &.is-focus {
                  box-shadow: 0 0 0 1px var(--bb-brand) inset, 0 0 0 3px var(--bb-brand-soft);
                }
              }

              &.is-disabled .el-input__wrapper {
                background-color: var(--bb-bg-page);
              }
            }
          }
        }

        .form-actions {
          margin-top: 24px;
          text-align: left;

          .save-btn {
            min-width: 200px;
            height: 48px;
            font-size: 20px;
            font-weight: 600;
            border-radius: 8px;
            background: linear-gradient(135deg, var(--bb-brand) 0%, var(--bb-brand-hover) 100%);
            border: none;
            box-shadow: 0 8px 20px rgba(37, 99, 235, 0.22);
            transition: transform 0.2s ease, box-shadow 0.2s ease;

            .btn-icon {
              margin-right: 8px;
            }

            &:hover {
              transform: translateY(-2px);
              box-shadow: 0 12px 28px rgba(37, 99, 235, 0.3);
            }

            &:active {
              transform: translateY(0);
            }
          }
        }

        .verify-time-tip {
          margin-left: 12px;
          color: var(--bb-text-secondary);
          font-size: 14px;
        }
      }
    }

    // 用户列表样式
    .user-list {
      .empty-state {
        text-align: center;
        padding: 60px 20px;
        color: var(--bb-text-secondary);

        .empty-icon {
          font-size: 68px;
          color: var(--bb-text-muted);
          margin-bottom: 15px;
        }

        p {
          font-size: 20px;
          margin: 0;
        }
      }

      .user-item {
        display: flex;
        align-items: center;
        gap: 15px;
        padding: 20px;
        border-radius: 8px;
        margin-bottom: 15px;
        background: var(--bb-bg-page);
        border: 1px solid var(--bb-border);
        transition: all 0.3s ease;

        &:hover {
          background: var(--bb-bg-card);
          transform: translateX(4px);
          box-shadow: var(--bb-shadow-card-hover);
        }

        .user-info {
          flex: 1;

          h4 {
            font-size: 20px;
            font-weight: 600;
            color: var(--bb-text-primary);
            margin: 0 0 6px 0;
          }

          p {
            font-size: 17px;
            color: var(--bb-text-secondary);
            margin: 0;
          }
        }

        .el-button {
          min-width: 80px;
        }
      }
    }

    .clubs-list {
      .empty-state {
        text-align: center;
        padding: 60px 20px;
        color: var(--bb-text-secondary);

        .empty-icon {
          font-size: 68px;
          color: var(--bb-text-muted);
          margin-bottom: 15px;
        }

        p {
          font-size: 20px;
          margin: 0 0 16px;
        }
      }

      .club-row {
        display: flex;
        align-items: center;
        justify-content: space-between;
        gap: 16px;
        padding: 18px 20px;
        margin-bottom: 12px;
        border-radius: 8px;
        background: var(--bb-bg-page);
        border: 1px solid var(--bb-border);
        cursor: pointer;
        transition: all 0.2s ease;

        &:hover {
          background: var(--bb-bg-card);
          box-shadow: var(--bb-shadow-card-hover);
        }

        .club-row-title {
          font-size: 18px;
          font-weight: 600;
          color: var(--bb-text-primary);
        }

        .club-row-sub {
          font-size: 14px;
          color: var(--bb-text-secondary);
          margin-top: 4px;
        }
      }
    }

    // 分页样式
    .pagination {
      display: flex;
      justify-content: center;
      margin-top: 30px;
      padding-top: 20px;
      border-top: 1px solid var(--bb-border);
    }
  }
}

// 我的笔记标签页内容
.notes-tab-content {
  padding: 20px 0;
}

// 小红书风格瀑布流
.my-notes-waterfall {
  column-count: 3;
  column-gap: 16px;

  @media (min-width: 1100px) {
    column-count: 4;
  }

  @media (max-width: 900px) {
    column-count: 2;
  }

  @media (max-width: 600px) {
    column-count: 1;
  }
}

// 小红书风格笔记卡片
.my-note-card {
  break-inside: avoid;
  margin-bottom: 16px;
  background: var(--bb-bg-card);
  border-radius: var(--bb-radius-card);
  border: 1px solid var(--bb-border);
  overflow: hidden;
  box-shadow: var(--bb-shadow-card);
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  
  &:hover {
    transform: translateY(-6px);
    box-shadow: var(--bb-shadow-card-hover);
    
    .hover-overlay {
      opacity: 1;
    }
    
    .cover-image {
      transform: scale(1.05);
    }
  }
  
  .my-note-cover {
    position: relative;
    width: 100%;
    min-height: 160px;
    overflow: hidden;
    background: var(--bb-bg-page);

    .note-corner-badge {
      position: absolute;
      top: 8px;
      right: 8px;
      z-index: 2;
      font-size: 12px;
      padding: 2px 8px;
      border-radius: 10px;
      background: rgba(0, 0, 0, 0.55);
      color: #fff;
    }
    
    .cover-image {
      width: 100%;
      height: 100%;
      min-height: 160px;
      max-height: 280px;
      object-fit: cover;
      transition: transform 0.4s ease;
      
      :deep(img) {
        width: 100%;
        height: 100%;
        object-fit: cover;
      }
    }
    
    .no-cover {
      width: 100%;
      height: 180px;
      display: flex;
      align-items: center;
      justify-content: center;
      background: linear-gradient(135deg, var(--bb-brand) 0%, var(--bb-brand-hover) 100%);
      color: white;
      font-size: 52px;
    }
    
    .image-error {
      width: 100%;
      height: 180px;
      display: flex;
      align-items: center;
      justify-content: center;
      background: var(--bb-bg-page);
      color: var(--bb-text-muted);
      font-size: 40px;
    }
    
    .hover-overlay {
      position: absolute;
      top: 0;
      left: 0;
      right: 0;
      bottom: 0;
      background: rgba(0, 0, 0, 0.4);
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      gap: 10px;
      opacity: 0;
      transition: opacity 0.3s ease;
      
      .view-text {
        color: white;
        font-size: 18px;
        font-weight: 500;
        padding: 8px 20px;
        background: rgba(255, 255, 255, 0.2);
        border-radius: 20px;
        backdrop-filter: blur(4px);
        cursor: pointer;
      }

      .my-note-owner-actions {
        display: flex;
        flex-wrap: wrap;
        gap: 6px;
        justify-content: center;
      }
    }
  }
  
  .my-note-content {
    padding: 14px 16px 16px;
    
    .my-note-title {
      font-size: 19px;
      font-weight: 600;
      color: var(--bb-text-primary);
      margin: 0 0 10px 0;
      line-height: 1.5;
      display: -webkit-box;
      line-clamp: 2;
      -webkit-line-clamp: 2;
      -webkit-box-orient: vertical;
      overflow: hidden;
      text-overflow: ellipsis;
    }

    .my-note-club {
      display: inline-block;
      margin-bottom: 10px;
      font-size: 13px;
      color: var(--bb-brand);
      background: var(--bb-brand-soft);
      border-radius: 10px;
      padding: 2px 10px;
    }
    
    .my-note-tags {
      display: flex;
      flex-wrap: wrap;
      gap: 6px;
      margin-bottom: 12px;
      
      .el-tag {
        border-radius: 12px;
        font-size: 15px;
        padding: 0 10px;
        height: 26px;
        line-height: 26px;
        background: var(--bb-bg-page);
        border: none;
        color: var(--bb-brand);
      }
    }
    
    .my-note-footer {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding-top: 10px;
      border-top: 1px solid var(--bb-border);
      
      .my-note-stats {
        display: flex;
        gap: 12px;
        
        .stat-item {
          display: flex;
          align-items: center;
          gap: 4px;
          font-size: 16px;
          color: var(--bb-text-secondary);
          
          .el-icon {
            font-size: 18px;
          }
        }
      }
      
      .my-note-time {
        font-size: 15px;
        color: var(--bb-text-muted);
      }
    }
  }
}

// 动画
@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

// 响应式设计
@media (max-width: 992px) {
  .person-container {
    padding: 20px 16px 32px;

    .person-card {
      .person-layout {
        flex-direction: column;
        min-height: 0;
      }

      .person-sidebar {
        flex: none;
        width: 100%;
        border-right: none;
        border-bottom: 1px solid var(--bb-border);
        padding: 20px 20px 16px;
      }

      .avatar-section .user-stats {
        grid-template-columns: repeat(4, minmax(0, 1fr));
        gap: 10px;
      }

      .person-main .person-tabs {
        padding: 0 20px 28px;

        :deep(.el-tabs__item) {
          padding: 0 12px;
          font-size: 18px;
        }
      }
    }
  }
}

@media (max-width: 576px) {
  .person-container .person-card .avatar-section .user-stats {
    grid-template-columns: 1fr 1fr;
  }
}

@media (max-width: 768px) {
  .person-container {
    padding: 16px 12px 24px;

    .person-card {
      .avatar-section {
        .avatar-wrapper .avatar-uploader .avatar-container {
          width: 96px;
          height: 96px;
        }

        .user-name {
          font-size: 22px;
        }
      }

      .person-main .person-tabs {
        padding: 0 16px 20px;

        .tab-content .form-group .form-group-title {
          font-size: 20px;
        }
      }
    }
  }
}
</style>
