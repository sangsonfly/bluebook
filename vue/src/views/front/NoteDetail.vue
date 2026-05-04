<script setup>
import { ref, onMounted, onUnmounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getNoteById, likeNote, collectNote, getNoteBehavior, updateNote, deleteNote } from '@/api/note'
import { followUser, unfollowUser, checkIsFollowing } from '@/api/follow'
import { getCommentsByNoteId, addComment, deleteComment, likeComment } from '@/api/comment'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ChatDotRound, ArrowLeft, ArrowRight } from '@element-plus/icons-vue'
import { formatTime } from '@/utils/date'

const route = useRoute()
const router = useRouter()
const note = ref({})
const loading = ref(false)
const loadError = ref('')
const isLiked = ref(false)
const isCollected = ref(false)
const isFollowing = ref(false)
const followLoading = ref(false)

// 评论相关状态
const comments = ref([])
const commentContent = ref('')
const replyingTo = ref(null)
const replyContent = ref('')
const commentLoading = ref(false)
const submitting = ref(false)

// 获取当前登录用户
const account = computed(() => {
  const accountStr = localStorage.getItem('account')
  return accountStr ? JSON.parse(accountStr) : null
})

const isNoteOwner = computed(
  () => !!(account.value?.id && note.value?.userId && account.value.id === note.value.userId)
)

const canInteract = computed(() => note.value?.status === 1)

// 图片轮播相关
const currentIndex = ref(0)
const imageList = computed(() => {
  if (!note.value?.imageUrl) return []
  return note.value.imageUrl.split(',').filter(url => url.trim())
})

// 触摸事件处理
let touchStartX = 0
let touchStartY = 0

const handleTouchStart = (e) => {
  touchStartX = e.touches[0].clientX
  touchStartY = e.touches[0].clientY
}

const handleTouchMove = (e) => {
  if (!touchStartX) return
  const touchEndX = e.touches[0].clientX
  const touchEndY = e.touches[0].clientY
  const diffX = touchStartX - touchEndX
  const diffY = touchStartY - touchEndY
  
  // 判断是水平滑动还是垂直滑动
  if (Math.abs(diffX) > Math.abs(diffY)) {
    e.preventDefault() // 阻止垂直滚动
  }
}

const handleTouchEnd = (e) => {
  if (!touchStartX) return
  const touchEndX = e.changedTouches[0].clientX
  const diffX = touchStartX - touchEndX
  
  if (Math.abs(diffX) > 50) { // 滑动距离超过50px才切换
    if (diffX > 0) {
      nextImage()
    } else {
      prevImage()
    }
  }
  
  touchStartX = 0
  touchStartY = 0
}

// 鼠标拖拽事件处理
let isDragging = false
let dragStartX = 0

const handleMouseDown = (e) => {
  isDragging = true
  dragStartX = e.clientX
  e.preventDefault()
}

const handleMouseMove = (e) => {
  if (!isDragging) return
  e.preventDefault()
}

const handleMouseUp = (e) => {
  if (!isDragging) return
  const dragEndX = e.clientX
  const diffX = dragStartX - dragEndX
  
  if (Math.abs(diffX) > 50) {
    if (diffX > 0) {
      nextImage()
    } else {
      prevImage()
    }
  }
  
  isDragging = false
  dragStartX = 0
}

// 切换图片
const prevImage = () => {
  if (currentIndex.value > 0) {
    currentIndex.value--
  } else {
    currentIndex.value = imageList.value.length - 1 // 循环
  }
}

const nextImage = () => {
  if (currentIndex.value < imageList.value.length - 1) {
    currentIndex.value++
  } else {
    currentIndex.value = 0 // 循环
  }
}

const goToImage = (index) => {
  currentIndex.value = index
}

// 加载笔记详情
const loadNoteDetail = async () => {
  loading.value = true
  loadError.value = ''
  try {
    const id = route.params.id
    const res = await getNoteById(id)
    if (res.code === 200 || res.code === '200') {
      note.value = res.data || {}
      if (!note.value?.id) {
        loadError.value = '笔记不存在或已下架'
        note.value = {}
        return
      }
      currentIndex.value = 0
      if (account.value && note.value.userId && account.value.id !== note.value.userId) {
        checkFollowStatus()
      }
      if (account.value && canInteract.value) {
        checkBehaviorStatus()
      } else {
        isLiked.value = false
        isCollected.value = false
      }
      loadComments()
    } else {
      loadError.value = res.msg || '笔记不存在或已下架'
      note.value = {}
    }
  } catch (error) {
    loadError.value = error?.response?.data?.msg || '加载笔记详情失败'
    note.value = {}
    console.error(error)
  } finally {
    loading.value = false
  }
}

const notePayload = () => JSON.parse(JSON.stringify(note.value))

const handleSetNoteStatus = async (status) => {
  try {
    const res = await updateNote({ ...notePayload(), status })
    if (res.code === 200 || res.code === '200') {
      ElMessage.success('已更新')
      await loadNoteDetail()
    } else {
      ElMessage.error(res.msg || '操作失败')
    }
  } catch (e) {
    ElMessage.error('操作失败')
  }
}

const handleDeleteNote = async () => {
  try {
    await ElMessageBox.confirm('确定删除该笔记吗？', '提示', {
      type: 'warning',
      confirmButtonText: '删除',
      cancelButtonText: '取消'
    })
    const res = await deleteNote(note.value.id)
    if (res.code === 200 || res.code === '200') {
      ElMessage.success('已删除')
      handleClose()
    } else {
      ElMessage.error(res.msg || '删除失败')
    }
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('删除失败')
  }
}

// 检查点赞/收藏状态
const checkBehaviorStatus = async () => {
  if (!account.value || !note.value.id) return
  try {
    const res = await getNoteBehavior(note.value.id, account.value.id)
    if (res.code === 200 || res.code === '200') {
      isLiked.value = res.data?.liked || false
      isCollected.value = res.data?.collected || false
    }
  } catch (error) {
    console.error('获取行为状态失败', error)
  }
}

// 检查关注状态
const checkFollowStatus = async () => {
  if (!account.value || !note.value.userId) return
  try {
    const res = await checkIsFollowing(account.value.id, note.value.userId)
    if (res.code === 200 || res.code === '200') {
      isFollowing.value = res.data || false
    }
  } catch (error) {
    console.error('检查关注状态失败', error)
  }
}

// 跳转到用户主页
const goToUserProfile = (userId) => {
  if (!userId) return
  router.push(`/front/user/${userId}`)
}

// 关注/取消关注
const handleFollow = async () => {
  // 检查是否登录
  if (!account.value) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  
  // 不能关注自己
  if (account.value.id === note.value.userId) {
    ElMessage.warning('不能关注自己')
    return
  }
  
  followLoading.value = true
  try {
    if (isFollowing.value) {
      // 取消关注
      const res = await unfollowUser(account.value.id, note.value.userId)
      if (res.code === 200 || res.code === '200') {
        isFollowing.value = false
        ElMessage.success('已取消关注')
      } else {
        ElMessage.error(res.msg || '取消关注失败')
      }
    } else {
      // 关注
      const res = await followUser(account.value.id, note.value.userId)
      if (res.code === 200 || res.code === '200') {
        isFollowing.value = true
        ElMessage.success('关注成功')
      } else {
        ElMessage.error(res.msg || '关注失败')
      }
    }
  } catch (error) {
    console.error('关注操作失败', error)
    ElMessage.error('操作失败，请稍后重试')
  } finally {
    followLoading.value = false
  }
}

// 点赞
const handleLike = async () => {
  if (!account.value) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  if (!canInteract.value) {
    ElMessage.warning('仅已发布的笔记可点赞')
    return
  }
  try {
    const res = await likeNote(note.value.id, account.value.id)
    if (res.code === 200 || res.code === '200') {
      isLiked.value = res.data // true-点赞成功，false-取消点赞
      ElMessage.success(isLiked.value ? '点赞成功' : '已取消点赞')
      loadNoteDetail()
    } else {
      ElMessage.error(res.msg || '操作失败')
    }
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

// 收藏
const handleCollect = async () => {
  if (!account.value) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  if (!canInteract.value) {
    ElMessage.warning('仅已发布的笔记可收藏')
    return
  }
  try {
    const res = await collectNote(note.value.id, account.value.id)
    if (res.code === 200 || res.code === '200') {
      isCollected.value = res.data // true-收藏成功，false-取消收藏
      ElMessage.success(isCollected.value ? '收藏成功' : '已取消收藏')
      loadNoteDetail()
    } else {
      ElMessage.error(res.msg || '操作失败')
    }
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

// 显示提示消息
const showToast = (message) => {
  // 移除现有toast
  const existingToast = document.querySelector('.toast-message')
  if (existingToast) {
    existingToast.remove()
  }
  
  const toast = document.createElement('div')
  toast.className = 'toast-message'
  toast.textContent = message
  document.body.appendChild(toast)
  
  setTimeout(() => toast.classList.add('show'), 10)
  setTimeout(() => {
    toast.classList.remove('show')
    setTimeout(() => toast.remove(), 300)
  }, 2000)
}

// 分享
const handleShare = () => {
  showToast('该模块正在开发中')
}

// 格式化数字
const formatNumber = (num) => {
  if (!num) return 0
  if (num >= 10000) {
    return (num / 10000).toFixed(1) + 'w'
  } else if (num >= 1000) {
    return (num / 1000).toFixed(1) + 'k'
  }
  return num
}

// 关闭详情弹层（路由页伪弹层）
const handleClose = () => {
  if (window.history.length > 1) {
    router.back()
  } else {
    router.replace('/front/home')
  }
}

const handleOverlayClick = (event) => {
  if (event.target === event.currentTarget) {
    handleClose()
  }
}

const handleEscClose = (event) => {
  if (event.key === 'Escape') {
    handleClose()
  }
}

// 加载评论列表
const loadComments = async () => {
  if (!note.value.id) return
  commentLoading.value = true
  try {
    const userId = account.value ? account.value.id : null
    const res = await getCommentsByNoteId(note.value.id, userId)
    if (res.code === 200 || res.code === '200') {
      // 处理评论数据，添加用户信息和点赞状态
      comments.value = (res.data || []).map(comment => ({
        ...comment,
        userName: comment.userName || `用户${comment.userId}`,
        userAvatar: comment.userAvatar || null,
        isLiked: comment.isLiked || false
      }))
    }
  } catch (error) {
    console.error('加载评论失败', error)
  } finally {
    commentLoading.value = false
  }
}

// 提交评论
const handleSubmitComment = async () => {
  if (!account.value) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  
  if (!commentContent.value.trim()) {
    ElMessage.warning('请输入评论内容')
    return
  }
  
  submitting.value = true
  try {
    const commentData = {
      noteId: note.value.id,
      userId: account.value.id,
      content: commentContent.value.trim(),
      parentId: null,
      replyToUserId: null
    }
    const res = await addComment(commentData)
    if (res.code === 200 || res.code === '200') {
      ElMessage.success('评论发布成功')
      commentContent.value = ''
      // 重新加载评论列表
      loadComments()
    } else {
      ElMessage.error(res.msg || '评论发布失败')
    }
  } catch (error) {
    ElMessage.error('评论发布失败')
    console.error(error)
  } finally {
    submitting.value = false
  }
}

// 处理回复操作
const handleReply = (comment) => {
  if (!account.value) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  replyingTo.value = comment.id
  replyContent.value = ''
}

// 提交回复
const handleSubmitReply = async (parentId) => {
  if (!replyContent.value.trim()) {
    ElMessage.warning('请输入回复内容')
    return
  }
  
  submitting.value = true
  try {
    const parentComment = comments.value.find(c => c.id === parentId)
    const commentData = {
      noteId: note.value.id,
      userId: account.value.id,
      content: replyContent.value.trim(),
      parentId: parentId,
      replyToUserId: parentComment?.userId || null
    }
    const res = await addComment(commentData)
    if (res.code === 200 || res.code === '200') {
      ElMessage.success('回复成功')
      replyContent.value = ''
      replyingTo.value = null
      // 重新加载评论列表
      loadComments()
    } else {
      ElMessage.error(res.msg || '回复失败')
    }
  } catch (error) {
    ElMessage.error('回复失败')
    console.error(error)
  } finally {
    submitting.value = false
  }
}

// 删除评论
const handleDeleteComment = async (commentId) => {
  try {
    await ElMessageBox.confirm(
      '确定要删除这条评论吗？删除后将无法恢复！',
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    const res = await deleteComment(commentId)
    if (res.code === 200 || res.code === '200') {
      ElMessage.success('删除成功')
      // 重新加载评论列表
      loadComments()
    } else {
      ElMessage.error(res.msg || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
      console.error(error)
    }
  }
}

// 点赞/取消点赞评论
const handleLikeComment = async (comment) => {
  if (!account.value) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  
  // 乐观更新：先更新UI
  const oldIsLiked = comment.isLiked
  const oldLikes = comment.likes || 0
  comment.isLiked = !oldIsLiked
  comment.likes = oldIsLiked ? oldLikes - 1 : oldLikes + 1
  
  try {
    const res = await likeComment(comment.id, account.value.id)
    if (res.code === 200 || res.code === '200') {
      // 确保状态与后端一致
      comment.isLiked = res.data
      // 如果后端返回了新的点赞数，使用后端数据
      if (res.data !== oldIsLiked) {
        comment.likes = res.data ? oldLikes + 1 : oldLikes - 1
      }
    } else {
      // 失败时回滚
      comment.isLiked = oldIsLiked
      comment.likes = oldLikes
      ElMessage.error(res.msg || '操作失败')
    }
  } catch (error) {
    // 失败时回滚
    comment.isLiked = oldIsLiked
    comment.likes = oldLikes
    ElMessage.error('操作失败')
    console.error(error)
  }
}


// 获取默认头像
const getDefaultAvatar = (userId) => {
  return null
}

onMounted(() => {
  window.addEventListener('keydown', handleEscClose)
  loadNoteDetail()
})

onUnmounted(() => {
  window.removeEventListener('keydown', handleEscClose)
})
</script>

<template>
  <div class="note-detail-overlay" @click="handleOverlayClick">
    <div v-loading="loading" class="note-detail-modal">
      <div class="modal-header">
        <span class="header-placeholder"></span>
        <el-button class="header-close-btn" circle plain @click="handleClose">×</el-button>
      </div>

      <div v-if="loadError && !loading" class="modal-body load-error-body">
        <el-empty :description="loadError">
          <el-button type="primary" @click="handleClose">返回</el-button>
        </el-empty>
      </div>

      <div v-else-if="!loading" class="modal-body two-column">
        <section class="image-panel">
          <div v-if="note.imageUrl" class="detail-images">
            <div v-if="imageList.length > 1" class="image-carousel">
              <div
                class="carousel-container"
                @touchstart="handleTouchStart"
                @touchmove="handleTouchMove"
                @touchend="handleTouchEnd"
                @mousedown="handleMouseDown"
                @mousemove="handleMouseMove"
                @mouseup="handleMouseUp"
                @mouseleave="handleMouseUp"
              >
                <div class="carousel-viewport">
                  <el-image
                    :key="currentIndex"
                    :src="imageList[currentIndex]"
                    :preview-src-list="imageList"
                    :initial-index="currentIndex"
                    fit="contain"
                    class="carousel-image"
                  />
                </div>
                <button class="carousel-btn carousel-btn-prev" @click.stop="prevImage">
                  <el-icon><ArrowLeft /></el-icon>
                </button>
                <button class="carousel-btn carousel-btn-next" @click.stop="nextImage">
                  <el-icon><ArrowRight /></el-icon>
                </button>
                <div class="carousel-indicators">
                  <span
                    v-for="(img, index) in imageList"
                    :key="index"
                    class="indicator"
                    :class="{ active: currentIndex === index }"
                    @click.stop="goToImage(index)"
                  ></span>
                </div>
              </div>
            </div>
            <div v-else class="single-image">
              <el-image :src="imageList[0]" :preview-src-list="imageList" fit="contain" class="detail-single-image" />
            </div>
          </div>
        </section>

        <section class="info-panel">
          <div class="author-row">
            <div class="author-card">
              <el-avatar :src="note.authorAvatar" :size="44" class="clickable-avatar" @click="goToUserProfile(note.userId)">
                {{ note.authorName?.charAt(0) }}
              </el-avatar>
              <div class="author-details clickable" @click="goToUserProfile(note.userId)">
                <h3>{{ note.authorName }}</h3>
                <span class="publish-time">{{ formatTime(note.createTime) }}</span>
              </div>
            </div>
            <el-button
              v-if="account && account.id !== note.userId && canInteract"
              :type="isFollowing ? 'info' : 'primary'"
              :loading="followLoading"
              class="follow-btn"
              @click="handleFollow"
            >
              {{ isFollowing ? '已关注' : '+ 关注' }}
            </el-button>
          </div>

          <div v-if="isNoteOwner" class="owner-status-bar">
            <el-tag v-if="note.status === 1" type="success" size="small">已发布</el-tag>
            <el-tag v-else-if="note.status === 0" type="info" size="small">草稿</el-tag>
            <el-tag v-else-if="note.status === 2" type="warning" size="small">已下架</el-tag>
            <el-tag v-else-if="note.status === -1" type="danger" size="small">已删除</el-tag>
            <span class="owner-actions">
              <el-button v-if="note.status === 1" size="small" @click="handleSetNoteStatus(2)">下架</el-button>
              <el-button v-if="note.status === 2 || note.status === 0" size="small" type="primary" @click="handleSetNoteStatus(1)">上架发布</el-button>
              <el-button v-if="note.status === -1" size="small" type="primary" @click="handleSetNoteStatus(1)">恢复发布</el-button>
              <el-button v-if="note.status !== -1" size="small" type="danger" @click="handleDeleteNote">删除</el-button>
            </span>
          </div>

          <h1 class="detail-title">{{ note.title }}</h1>
          <div class="detail-text">
            <p>{{ note.content }}</p>
          </div>
          <div v-if="note.tags" class="detail-tags">
            <el-tag v-for="tag in note.tags?.split(',')" :key="tag" type="primary" effect="plain">#{{ tag }}</el-tag>
          </div>

          <div v-if="canInteract" class="note-actions">
            <el-button :type="isLiked ? 'danger' : 'default'" @click="handleLike">点赞 {{ formatNumber(note.likes) }}</el-button>
            <el-button :type="isCollected ? 'warning' : 'default'" @click="handleCollect">收藏 {{ formatNumber(note.collects) }}</el-button>
            <el-button @click="handleShare">分享</el-button>
            <span class="views-text">浏览 {{ formatNumber(note.views) }}</span>
          </div>
          <div v-else-if="isNoteOwner" class="note-actions note-actions-muted">
            <span class="views-text">浏览 {{ formatNumber(note.views) }}（非发布状态，访客不可见）</span>
          </div>

          <div class="comment-panel">
            <div class="comments-header">
              <h3>评论 <span class="comment-count">{{ comments.length }}</span></h3>
            </div>
            <div class="comment-input-box">
              <el-avatar :src="account?.avatarUrl" :size="34">
                {{ account?.nickname?.charAt(0) || 'U' }}
              </el-avatar>
              <el-input
                v-model="commentContent"
                type="textarea"
                :rows="2"
                placeholder="说点什么..."
                maxlength="500"
                show-word-limit
                class="comment-input"
                @keydown.ctrl.enter="handleSubmitComment"
              />
              <el-button type="primary" :loading="submitting" @click="handleSubmitComment">发布</el-button>
            </div>
            <div v-loading="commentLoading" class="comments-list">
              <div v-for="comment in comments" :key="comment.id" class="comment-item">
                <el-avatar
                  :src="comment.userAvatar || getDefaultAvatar(comment.userId)"
                  :size="34"
                  class="clickable-avatar"
                  @click="goToUserProfile(comment.userId)"
                >
                  {{ comment.userName?.charAt(0) || 'U' }}
                </el-avatar>
                <div class="comment-content">
                  <div class="comment-header">
                    <span class="comment-author clickable" @click="goToUserProfile(comment.userId)">
                      {{ comment.userName || `用户${comment.userId}` }}
                    </span>
                    <span class="comment-time">{{ formatTime(comment.createTime) }}</span>
                    <el-button
                      v-if="account && (account.id === comment.userId || account.id === note.userId)"
                      text
                      type="danger"
                      size="small"
                      @click="handleDeleteComment(comment.id)"
                    >
                      删除
                    </el-button>
                  </div>
                  <p class="comment-text">{{ comment.content }}</p>
                  <div class="comment-actions">
                    <el-button text size="small" :class="{ liked: comment.isLiked }" @click="handleLikeComment(comment)">
                      <svg v-if="comment.isLiked" viewBox="0 0 24 24" width="16" height="16" fill="currentColor" style="margin-right: 4px;">
                        <path d="M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z"/>
                      </svg>
                      <svg v-else viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2" style="margin-right: 4px;">
                        <path d="M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z"/>
                      </svg>
                      {{ comment.likes || 0 }}
                    </el-button>
                    <el-button text size="small" @click="handleReply(comment)">
                      <el-icon><ChatDotRound /></el-icon>
                      回复
                    </el-button>
                  </div>
                  <div v-if="replyingTo === comment.id" class="reply-input-box">
                    <el-input
                      v-model="replyContent"
                      type="textarea"
                      :rows="2"
                      placeholder="回复..."
                      maxlength="300"
                      show-word-limit
                      class="reply-input"
                    />
                    <div class="reply-actions">
                      <el-button size="small" @click="replyingTo = null">取消</el-button>
                      <el-button type="primary" size="small" :loading="submitting" @click="handleSubmitReply(comment.id)">
                        回复
                      </el-button>
                    </div>
                  </div>
                </div>
              </div>
              <el-empty v-if="!commentLoading && comments.length === 0" description="暂无评论，快来抢沙发吧~" />
            </div>
          </div>
        </section>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* 覆盖层 */
.note-detail-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  background: rgba(15, 23, 42, 0.45);
  backdrop-filter: blur(2px);
  z-index: 1500;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
  box-sizing: border-box;
}

.note-detail-modal {
  width: min(1440px, 100%);
  height: min(88vh, 920px);
  background: #f9fbff;
  border-radius: 20px;
  box-shadow: 0 24px 60px rgba(15, 23, 42, 0.28);
  display: flex;
  flex-direction: column;
  overflow: hidden;
  position: relative;
}

.load-error-body {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding-top: 48px;
}

.owner-status-bar {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
}

.owner-actions {
  display: inline-flex;
  flex-wrap: wrap;
  gap: 6px;
  align-items: center;
}

.note-actions-muted {
  opacity: 0.88;
}

.modal-header {
  position: absolute;
  top: 12px;
  left: 14px;
  right: 14px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  z-index: 20;
  pointer-events: none;
}

.header-placeholder,
.header-close-btn {
  pointer-events: auto;
}

.header-placeholder {
  width: 1px;
  height: 1px;
  opacity: 0;
}

.header-close-btn {
  width: 34px;
  height: 34px;
  background: rgba(255, 255, 255, 0.9);
  border: 1px solid rgba(15, 23, 42, 0.08);
  color: #334155;
  opacity: 0.95;
  font-size: 20px;
  font-weight: 500;
}

.header-close-btn:hover {
  opacity: 1;
  background: rgba(255, 255, 255, 0.9);
}

.modal-body {
  flex: 1;
  padding: 18px;
  display: grid;
  grid-template-columns: minmax(0, 1.15fr) minmax(340px, 0.85fr);
  gap: 16px;
  min-height: 0;
}

.image-panel,
.info-panel {
  background: #fff;
  border: 1px solid #eaf0fa;
  border-radius: 16px;
  min-height: 0;
}

.image-panel {
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0;
}

.info-panel {
  overflow-y: auto;
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.author-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.author-card {
  display: flex;
  align-items: center;
  gap: 12px;
}

.author-details {
  flex: 1;
}

.author-details.clickable,
.clickable-avatar {
  cursor: pointer;
}

.author-details.clickable:hover h3 {
  color: var(--bb-brand);
}

.author-details h3 {
  font-size: 15px;
  margin: 0 0 3px 0;
  transition: color 0.2s;
}

.publish-time {
  font-size: 13px;
  color: #94a3b8;
}

.follow-btn {
  min-width: 88px;
}

.detail-images {
  margin: 0;
  text-align: center;
  width: 100%;
  height: 100%;
}

.image-carousel {
  position: relative;
  width: 100%;
  height: 100%;
  max-width: none;
  margin: 0 auto;
  border-radius: 0;
  overflow: hidden;
  background: #f4f7fc;
  border: none;
}

.carousel-container {
  position: relative;
  width: 100%;
  height: 100%;
  overflow: hidden;
  user-select: none;
  cursor: grab;
}

.carousel-container:active {
  cursor: grabbing;
}

.carousel-viewport {
  width: 100%;
  height: 100%;
  min-height: 0;
  display: flex;
  align-items: center;
  justify-content: center;
}

.carousel-image {
  width: 100%;
  height: 100%;
  max-height: none;
}

.carousel-image :deep(.el-image__inner) {
  width: 100%;
  height: 100%;
  max-height: none;
  object-fit: contain;
}

.carousel-btn {
  position: absolute;
  top: 50%;
  transform: translateY(-50%);
  width: 44px;
  height: 44px;
  border-radius: 50%;
  background: rgba(17, 24, 39, 0.56);
  border: none;
  color: white;
  font-size: 18px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
  z-index: 10;
  backdrop-filter: blur(10px);
}

.carousel-btn:hover {
  background: rgba(17, 24, 39, 0.72);
  transform: translateY(-50%) scale(1.1);
}

.carousel-btn-prev {
  left: 15px;
}

.carousel-btn-next {
  right: 15px;
}

.carousel-indicators {
  position: absolute;
  bottom: 15px;
  left: 50%;
  transform: translateX(-50%);
  display: flex;
  gap: 8px;
  z-index: 10;
}

.indicator {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.5);
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.25, 0.46, 0.45, 0.94);
}

.indicator.active {
  background: white;
  width: 24px;
  border-radius: 4px;
}

.indicator:hover {
  background: rgba(255, 255, 255, 0.8);
}

.single-image {
  width: 100%;
  height: 100%;
  max-width: none;
  margin: 0 auto;
  border-radius: 0;
  overflow: hidden;
  background: #f4f7fc;
  border: none;
}

.detail-single-image {
  width: 100%;
  height: 100%;
  max-height: none;
}

.detail-single-image :deep(.el-image__inner) {
  width: 100%;
  height: 100%;
  object-fit: contain;
}

.detail-text {
  font-size: 16px;
  line-height: 1.8;
  margin: 4px 0;
  color: #2b3448;
  white-space: pre-wrap;
}

.detail-text p {
  margin: 0 0 0.75em;
}

.detail-text p:last-child {
  margin-bottom: 0;
}

.detail-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 14px;
}

.detail-tags :deep(.el-tag) {
  font-size: 14px;
  padding: 6px 12px;
}

.detail-title {
  font-size: 22px;
  font-weight: 700;
  line-height: 1.4;
  margin: 0 0 8px;
  color: #1f2b3d;
}

.note-actions {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
  padding-top: 6px;
}

.note-actions :deep(.el-button) {
  font-size: 14px;
}

.views-text {
  margin-left: auto;
  font-size: 13px;
  color: #94a3b8;
}

.comment-panel {
  display: flex;
  flex-direction: column;
  gap: 10px;
  flex: none;
  min-height: auto;
  overflow: visible;
  border-top: 1px solid #edf2fb;
  padding-top: 10px;
}

.comments-header {
  padding-bottom: 4px;
}

.comments-header h3 {
  font-size: 17px;
  font-weight: 600;
  color: #1f2b3d;
  margin: 0;
}

.comment-count {
  color: #94a3b8;
  font-weight: normal;
}

.comment-input-box {
  display: flex;
  gap: 8px;
  padding: 8px;
  background: #fafcff;
  border-radius: 8px;
  border: 1px solid #eef3fb;
}

.comment-input {
  flex: 1;
}

.comment-input :deep(.el-textarea__inner) {
  font-size: 16px;
  line-height: 1.6;
}

.comments-list {
  margin-top: 0;
  flex: none;
  overflow: visible;
  padding-right: 4px;
  min-height: auto;
}

.comment-item {
  display: flex;
  gap: 8px;
  padding: 10px 0;
  border-bottom: 1px solid #edf2fb;
}

.comment-item:last-child {
  border-bottom: none;
}

.comment-content {
  flex: 1;
}

.comment-header {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 8px;
}

.comment-author {
  font-size: 14px;
  font-weight: 600;
  color: #22314a;
}

.comment-author.clickable {
  cursor: pointer;
  transition: color 0.3s;
}

.comment-author.clickable:hover {
  color: var(--bb-brand);
}

.comment-time {
  font-size: 13px;
  color: #94a3b8;
}

.comment-text {
  font-size: 14px;
  color: #2b3448;
  line-height: 1.65;
  margin-bottom: 8px;
  white-space: pre-wrap;
}

.comment-actions {
  display: flex;
  gap: 10px;
}

.comment-actions .el-button.liked {
  color: #f56c6c;
}

.comment-actions .el-button.liked svg {
  color: #f56c6c;
  fill: #f56c6c;
}

.comment-actions .el-button svg {
  display: inline-block;
  vertical-align: middle;
}

.reply-input-box {
  margin-top: 10px;
  padding: 10px;
  background: #fafcff;
  border-radius: 8px;
  border: 1px solid #eef3fb;
}

.reply-input {
  margin-bottom: 10px;
}

.reply-input :deep(.el-textarea__inner) {
  font-size: 16px;
  line-height: 1.6;
}

.reply-actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}

@media (max-width: 1360px) {
  .modal-body {
    grid-template-columns: minmax(0, 1fr) minmax(320px, 0.9fr);
  }

  .carousel-viewport {
    min-height: 0;
  }
}

@media (max-width: 1024px) {
  .note-detail-overlay {
    padding: 16px;
  }

  .note-detail-modal {
    height: 92vh;
  }

  .modal-body {
    grid-template-columns: 1fr;
    grid-template-rows: auto auto;
  }

  .image-panel {
    min-height: 320px;
  }

  .carousel-viewport {
    min-height: 0;
  }
}

@media (max-width: 768px) {
  .note-detail-overlay {
    padding: 0;
  }

  .note-detail-modal {
    width: 100%;
    height: 100vh;
    border-radius: 0;
  }

  .modal-body {
    grid-template-columns: 1fr;
    gap: 12px;
    padding: 12px;
    overflow-y: auto;
  }

  .image-panel,
  .info-panel {
    overflow: visible;
    max-height: none;
  }

  .note-actions .el-button {
    flex: 1;
    min-width: 96px;
  }

  .comment-input-box {
    flex-direction: column;
  }

  .views-text {
    width: 100%;
    margin-left: 0;
  }
}
</style>

<style>
/* Toast 提示样式 - 全局样式 */
.toast-message {
  position: fixed;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%) scale(0.8);
  background: rgba(0, 0, 0, 0.75);
  color: white;
  padding: 12px 24px;
  border-radius: 8px;
  font-size: 14px;
  z-index: 10000;
  opacity: 0;
  transition: all 0.3s ease;
  pointer-events: none;
  white-space: nowrap;
}

.toast-message.show {
  opacity: 1;
  transform: translate(-50%, -50%) scale(1);
}
</style>

