<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Document, ArrowLeft } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'
import { followUser, unfollowUser, checkIsFollowing } from '@/api/follow'

const route = useRoute()
const router = useRouter()

// 用户ID
const userId = computed(() => route.params.id)

// 当前登录用户
const account = computed(() => {
  const str = localStorage.getItem('account')
  if (!str) return null
  try {
    return JSON.parse(str)
  } catch {
    return null
  }
})

// 用户信息
const userInfo = ref({})
const loading = ref(false)

// 笔记列表
const notesList = ref([])
const notesLoading = ref(false)
const notesPage = ref({
  pageNum: 1,
  pageSize: 12,
  total: 0
})

// 关注状态
const isFollowing = ref(false)
const followLoading = ref(false)

// 获取用户信息
const getUserInfo = async () => {
  loading.value = true
  try {
    const res = await request.get(`/user/${userId.value}`)
    if (res.code === '200' || res.code === 200) {
      userInfo.value = res.data || {}
    } else {
      ElMessage.error(res.msg || '获取用户信息失败')
    }
  } catch (error) {
    console.error('获取用户信息失败', error)
    ElMessage.error('获取用户信息失败')
  } finally {
    loading.value = false
  }
}

// 获取用户笔记
const loadNotesList = async () => {
  notesLoading.value = true
  try {
    const res = await request.get('/api/note/page', {
      params: {
        userId: userId.value,
        pageNum: notesPage.value.pageNum,
        pageSize: notesPage.value.pageSize
      }
    })
    if (res.code === 200 || res.code === '200') {
      notesList.value = res.data.records || []
      notesPage.value.total = res.data.total || 0
    }
  } catch (error) {
    console.error('加载笔记列表失败', error)
  } finally {
    notesLoading.value = false
  }
}

// 检查是否已关注
const checkFollowStatus = async () => {
  if (!account.value || !account.value.id || account.value.id == userId.value) return
  
  try {
    const res = await checkIsFollowing(account.value.id, userId.value)
    if (res.code === '200' || res.code === 200) {
      isFollowing.value = res.data
    }
  } catch (error) {
    console.error('检查关注状态失败', error)
  }
}

// 关注/取消关注
const handleFollow = async () => {
  if (!account.value || !account.value.id) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  
  if (account.value.id == userId.value) {
    ElMessage.warning('不能关注自己')
    return
  }
  
  followLoading.value = true
  try {
    let res
    if (isFollowing.value) {
      res = await unfollowUser(account.value.id, userId.value)
    } else {
      res = await followUser(account.value.id, userId.value)
    }
    
    if (res.code === '200' || res.code === 200) {
      isFollowing.value = !isFollowing.value
      ElMessage.success(isFollowing.value ? '关注成功' : '已取消关注')
      getUserInfo() // 刷新用户信息
    } else {
      ElMessage.error(res.msg || '操作失败')
    }
  } catch (error) {
    console.error('关注操作失败', error)
    ElMessage.error('操作失败')
  } finally {
    followLoading.value = false
  }
}

// 发私信
const goPrivateChat = () => {
  if (!account.value || !account.value.id) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  if (account.value.id == userId.value) {
    ElMessage.warning('不能和自己私聊')
    return
  }
  router.push(`/front/chat/${userId.value}`)
}

// 笔记分页
const handleNotesPageChange = (page) => {
  notesPage.value.pageNum = page
  loadNotesList()
}

// 跳转到笔记详情
const goToNote = (noteId) => {
  router.push(`/front/note/${noteId}`)
}

// 返回
const goBack = () => {
  router.back()
}

// 是否是自己
const isSelf = computed(() => {
  return account.value && account.value.id == userId.value
})

onMounted(() => {
  getUserInfo()
  loadNotesList()
  checkFollowStatus()
})
</script>

<template>
  <div class="user-profile-container">
    <div class="profile-card" v-loading="loading">
      <!-- 返回按钮 -->
      <div class="back-btn" @click="goBack">
        <el-icon><ArrowLeft /></el-icon>
        返回
      </div>
      
      <!-- 头部背景 -->
      <div class="card-header-bg"></div>
      
      <!-- 用户信息 -->
      <div class="user-section">
        <div class="avatar-wrapper">
          <el-avatar :src="userInfo.avatarUrl" :size="120">
            {{ userInfo.nickname?.charAt(0) || userInfo.username?.charAt(0) || '?' }}
          </el-avatar>
        </div>
        
        <h2 class="user-name">{{ userInfo.nickname || userInfo.username || '未知用户' }}</h2>
        
        <!-- 统计信息 -->
        <div class="user-stats">
          <div class="stat-item">
            <div class="stat-number">{{ userInfo.notesCount || 0 }}</div>
            <div class="stat-label">笔记</div>
          </div>
          <div class="stat-item">
            <div class="stat-number">{{ userInfo.followingCount || 0 }}</div>
            <div class="stat-label">关注</div>
          </div>
          <div class="stat-item">
            <div class="stat-number">{{ userInfo.followersCount || 0 }}</div>
            <div class="stat-label">粉丝</div>
          </div>
          <div class="stat-item">
            <div class="stat-number">{{ userInfo.likesReceived || 0 }}</div>
            <div class="stat-label">获赞</div>
          </div>
        </div>
        
        <!-- 关注按钮 -->
        <el-button 
          v-if="!isSelf"
          :type="isFollowing ? 'info' : 'primary'" 
          :loading="followLoading"
          size="large"
          class="follow-btn"
          @click="handleFollow"
        >
          {{ isFollowing ? '✓ 已关注' : '+ 关注' }}
        </el-button>
        <el-button
          v-if="!isSelf"
          type="default"
          size="large"
          class="chat-btn"
          @click="goPrivateChat"
        >
          发私信
        </el-button>
        <el-button 
          v-else
          type="default"
          size="large"
          class="follow-btn"
          @click="router.push('/front/person')"
        >
          编辑资料
        </el-button>
      </div>
      
      <!-- 笔记列表 -->
      <div class="notes-section">
        <h3 class="section-title">
          <el-icon><Document /></el-icon>
          TA的笔记
        </h3>
        
        <div v-loading="notesLoading" class="notes-grid">
          <div v-if="notesList.length === 0" class="empty-state">
            <el-icon class="empty-icon"><Document /></el-icon>
            <p>还没有发布笔记</p>
          </div>
          <div 
            v-for="note in notesList" 
            :key="note.id"
            class="note-card"
            @click="goToNote(note.id)"
          >
            <div class="note-cover">
              <img v-if="note.imageUrl" :src="note.imageUrl.split(',')[0]" alt="封面" />
              <div v-else class="no-cover">
                <el-icon><Document /></el-icon>
              </div>
            </div>
            <div class="note-info">
              <h4>{{ note.title }}</h4>
              <p class="note-stats">
                <span>👁 {{ note.views || 0 }}</span>
                <span>❤ {{ note.likes || 0 }}</span>
              </p>
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
    </div>
  </div>
</template>

<style scoped lang="scss">
.user-profile-container {
  min-height: calc(100vh - 60px);
  padding: 24px 20px 40px;
  background: var(--bb-bg-page);
}

.profile-card {
  max-width: 900px;
  margin: 0 auto;
  background: var(--bb-bg-card);
  border-radius: var(--bb-radius-card);
  border: 1px solid var(--bb-border);
  overflow: visible;
  box-shadow: var(--bb-shadow-card);
  position: relative;
}

.back-btn {
  position: absolute;
  top: 20px;
  left: 20px;
  z-index: 10;
  display: flex;
  align-items: center;
  gap: 5px;
  padding: 8px 16px;
  background: rgba(255, 255, 255, 0.9);
  border-radius: 20px;
  cursor: pointer;
  font-size: 14px;
  color: var(--bb-text-secondary);
  transition: all 0.3s;
  
  &:hover {
    background: var(--bb-bg-card);
    color: var(--bb-brand);
  }
}

.card-header-bg {
  height: 132px;
  background: linear-gradient(
    135deg,
    var(--bb-brand-soft) 0%,
    var(--bb-bg-card) 72%
  );
  position: relative;

  &::before {
    content: '';
    position: absolute;
    inset: 0;
    background: radial-gradient(
      circle at 18% 28%,
      rgba(37, 99, 235, 0.1) 0%,
      transparent 52%
    );
    pointer-events: none;
  }
}

.user-section {
  text-align: center;
  padding: 0 40px 30px;
  margin-top: -44px;
  
  .avatar-wrapper {
    display: inline-block;
    margin-bottom: 15px;
    position: relative;
    z-index: 2;
    
    :deep(.el-avatar) {
      border: 5px solid var(--bb-bg-card);
      box-shadow: var(--bb-shadow-card);
    }
  }
  
  .user-name {
    font-size: 24px;
    font-weight: 600;
    letter-spacing: 0.02em;
    color: var(--bb-text-primary);
    margin: 0 0 20px 0;
  }
  
  .user-stats {
    display: flex;
    justify-content: center;
    gap: 40px;
    margin-bottom: 25px;
    
    .stat-item {
      text-align: center;
      
      .stat-number {
        font-size: 22px;
        font-weight: 700;
        color: var(--bb-brand);
      }
      
      .stat-label {
        font-size: 13px;
        color: var(--bb-text-secondary);
        margin-top: 4px;
      }
    }
  }
  
  .follow-btn {
    min-width: 120px;
    border-radius: 20px;
  }

  .chat-btn {
    min-width: 120px;
    border-radius: 20px;
    margin-left: 10px;
  }
}

.notes-section {
  padding: 30px 40px 40px;
  border-top: 1px solid var(--bb-border);
  
  .section-title {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 18px;
    font-weight: 600;
    color: var(--bb-text-primary);
    margin: 0 0 20px 0;
  }
}

.notes-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
  gap: 20px;
  
  .note-card {
    background: var(--bb-bg-page);
    border-radius: var(--bb-radius-card);
    border: 1px solid var(--bb-border);
    overflow: hidden;
    cursor: pointer;
    transition: all 0.3s ease;
    
    &:hover {
      transform: translateY(-5px);
      box-shadow: var(--bb-shadow-card-hover);
    }
    
    .note-cover {
      width: 100%;
      height: 140px;
      overflow: hidden;
      background: var(--bb-bg-page);
      
      img {
        width: 100%;
        height: 100%;
        object-fit: cover;
      }
      
      .no-cover {
        width: 100%;
        height: 100%;
        display: flex;
        align-items: center;
        justify-content: center;
        background: linear-gradient(135deg, var(--bb-brand) 0%, var(--bb-brand-hover) 100%);
        color: white;
        font-size: 36px;
      }
    }
    
    .note-info {
      padding: 12px;
      
      h4 {
        margin: 0 0 8px 0;
        font-size: 14px;
        color: var(--bb-text-primary);
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }
      
      .note-stats {
        margin: 0;
        font-size: 12px;
        color: var(--bb-text-secondary);
        
        span {
          margin-right: 10px;
        }
      }
    }
  }
}

.empty-state {
  grid-column: 1 / -1;
  text-align: center;
  padding: 60px 20px;
  color: var(--bb-text-secondary);
  
  .empty-icon {
    font-size: 48px;
    margin-bottom: 15px;
  }
  
  p {
    margin: 0;
    font-size: 14px;
  }
}

.pagination {
  margin-top: 30px;
  display: flex;
  justify-content: center;
}

@media (max-width: 768px) {
  .user-profile-container {
    padding: 20px 10px;
  }
  
  .profile-card {
    border-radius: var(--bb-radius-card);
  }
  
  .user-section {
    padding: 0 20px 20px;
    
    .user-stats {
      gap: 25px;
    }
  }
  
  .notes-section {
    padding: 20px;
  }
  
  .notes-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: 12px;
  }
}
</style>

