<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getNotePage, deleteNote } from '@/api/note'
import { isClubAdmin, getClubMembersPage, setClubMemberRole } from '@/api/clubMember'
import {
  getClubActivities,
  createActivity,
  updateActivity,
  updateActivityStatus
} from '@/api/activity'
import { getActivityRegistrations, reviewRegistration } from '@/api/activityRegistration'

const route = useRoute()
const router = useRouter()

const clubId = computed(() => Number(route.params.id))

const account = ref(
  localStorage.getItem('account') ? JSON.parse(localStorage.getItem('account')) : {}
)

const allowed = ref(false)
const checking = ref(true)
const activeTab = ref('notes')

const tableData = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const loading = ref(false)

const members = ref([])
const membersTotal = ref(0)
const membersPageNum = ref(1)
const membersPageSize = ref(10)
const membersLoading = ref(false)
const activities = ref([])
const activitiesLoading = ref(false)
const activityDialogVisible = ref(false)
const activitySubmitting = ref(false)
const editingActivityId = ref(null)
const activityFormRef = ref()
const activityForm = ref({
  title: '',
  description: '',
  coverUrl: '',
  location: '',
  startTime: '',
  endTime: '',
  maxParticipants: null,
  needApproval: 0,
  tags: ''
})
const reviewDrawerVisible = ref(false)
const reviewLoading = ref(false)
const reviewSubmitting = ref(false)
const reviewingActivityId = ref(null)
const registrationFilter = ref('pending')
const registrationRows = ref([])
const registrationTotal = ref(0)
const registrationPageNum = ref(1)
const registrationPageSize = ref(10)
const rejectRemark = ref('')

const isCurrentUserPresident = computed(() =>
  members.value.some((item) => item.userId === account.value?.id && item.role === 3)
)

const roleLabel = (role) => {
  if (role === 3) return '社长'
  if (role === 2) return '管理员'
  return '成员'
}

const roleTagType = (role) => {
  if (role === 3) return 'danger'
  if (role === 2) return 'warning'
  return 'info'
}

const activityStatusLabel = (status) => {
  if (status === 0) return '已取消'
  if (status === 1) return '报名中'
  if (status === 2) return '进行中'
  if (status === 3) return '已结束'
  return '未知'
}

const activityStatusTagType = (status) => {
  if (status === 0) return 'info'
  if (status === 1) return 'success'
  if (status === 2) return 'warning'
  if (status === 3) return 'danger'
  return 'info'
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

const registrationStatusLabel = (status) => {
  if (status === 0) return '待审核'
  if (status === 1) return '已通过'
  if (status === 2) return '已拒绝'
  if (status === 3) return '已签到'
  if (status === 4) return '已取消'
  return '未知'
}

const displayRegistrations = computed(() => {
  if (registrationFilter.value === 'pending') {
    return registrationRows.value.filter((item) => item.status === 0)
  }
  if (registrationFilter.value === 'approved') {
    return registrationRows.value.filter((item) => item.status === 1 || item.status === 3)
  }
  return registrationRows.value
})

const formatDateTime = (value) => {
  if (!value) return '-'
  return String(value).replace('T', ' ')
}

/** 表格单元格：空值显示为「-」 */
const formatCell = (value) => {
  if (value == null || String(value).trim() === '') return '-'
  return String(value).trim()
}

/** 确认框等场景：优先实名，其次昵称，再次用户ID */
const displayUserLabel = (row) => {
  const name = row?.realName?.trim()
  if (name) return name
  const nick = row?.nickname?.trim()
  if (nick) return nick
  if (row?.userId != null && row?.userId !== '') return `用户 #${row.userId}`
  return '-'
}

const getErrorMessage = (error, fallback = '请求失败') => {
  const serverMsg = error?.response?.data?.msg
  if (serverMsg) return serverMsg
  const status = error?.response?.status
  if (status) return `${fallback}（HTTP ${status}）`
  return error?.message || fallback
}

const activityRules = {
  title: [{ required: true, message: '请输入活动标题', trigger: 'blur' }],
  startTime: [{ required: true, message: '请选择开始时间', trigger: 'change' }],
  endTime: [{ required: true, message: '请选择结束时间', trigger: 'change' }]
}

const verifyAccess = async () => {
  checking.value = true
  if (!account.value?.id) {
    allowed.value = false
    checking.value = false
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  try {
    const res = await isClubAdmin(clubId.value, account.value.id)
    allowed.value =
      (res.code === 200 || res.code === '200') && (res.data === true || res.data === 'true')
    if (!allowed.value) {
      ElMessage.error('无权限管理该社团')
      router.replace(`/front/club/${clubId.value}`)
    }
  } catch {
    allowed.value = false
    ElMessage.error('权限校验失败，请稍后重试')
    router.replace(`/front/club/${clubId.value}`)
  } finally {
    checking.value = false
  }
}

const loadList = async () => {
  if (!allowed.value) return
  loading.value = true
  try {
    const res = await getNotePage({
      clubId: clubId.value,
      pageNum: pageNum.value,
      pageSize: pageSize.value
    })
    if ((res.code === 200 || res.code === '200') && res.data) {
      tableData.value = res.data.records || []
      total.value = res.data.total || 0
    }
  } catch {
    ElMessage.error('加载失败')
  } finally {
    loading.value = false
  }
}

const loadMembers = async () => {
  if (!allowed.value) return
  membersLoading.value = true
  try {
    const res = await getClubMembersPage(clubId.value, membersPageNum.value, membersPageSize.value)
    if ((res.code === 200 || res.code === '200') && res.data) {
      members.value = res.data.records || []
      membersTotal.value = res.data.total || 0
    }
  } catch {
    ElMessage.error('成员加载失败')
  } finally {
    membersLoading.value = false
  }
}

const loadActivities = async () => {
  if (!allowed.value) return
  activitiesLoading.value = true
  try {
    const res = await getClubActivities(clubId.value)
    if (res.code === 200 || res.code === '200') {
      activities.value = res.data || []
    }
  } catch (error) {
    ElMessage.error(getErrorMessage(error, '活动加载失败'))
  } finally {
    activitiesLoading.value = false
  }
}

const resetActivityForm = () => {
  editingActivityId.value = null
  activityForm.value = {
    title: '',
    description: '',
    coverUrl: '',
    location: '',
    startTime: '',
    endTime: '',
    maxParticipants: null,
    needApproval: 0,
    tags: ''
  }
}

const openCreateActivity = () => {
  resetActivityForm()
  activityDialogVisible.value = true
}

const openEditActivity = (row) => {
  editingActivityId.value = row.id
  activityForm.value = {
    title: row.title || '',
    description: row.description || '',
    coverUrl: row.coverUrl || '',
    location: row.location || '',
    startTime: row.startTime || '',
    endTime: row.endTime || '',
    maxParticipants: row.maxParticipants,
    needApproval: row.needApproval ?? 0,
    tags: row.tags || ''
  }
  activityDialogVisible.value = true
}

const submitActivity = async () => {
  if (!activityFormRef.value) return
  const valid = await activityFormRef.value.validate().catch(() => false)
  if (!valid) return
  if (new Date(activityForm.value.endTime).getTime() <= new Date(activityForm.value.startTime).getTime()) {
    ElMessage.warning('结束时间必须晚于开始时间')
    return
  }
  activitySubmitting.value = true
  const payload = {
    ...activityForm.value,
    clubId: clubId.value,
    startTime: activityForm.value.startTime || null,
    endTime: activityForm.value.endTime || null,
    maxParticipants:
      activityForm.value.maxParticipants === '' || activityForm.value.maxParticipants == null
        ? null
        : Number(activityForm.value.maxParticipants)
  }
  try {
    let res
    if (editingActivityId.value) {
      res = await updateActivity({ ...payload, id: editingActivityId.value })
    } else {
      res = await createActivity(payload)
    }
    if (res.code === 200 || res.code === '200') {
      ElMessage.success(editingActivityId.value ? '活动更新成功' : '活动创建成功')
      activityDialogVisible.value = false
      await loadActivities()
    } else {
      ElMessage.error(res.msg || '活动提交失败')
    }
  } catch (error) {
    ElMessage.error(getErrorMessage(error, '活动提交失败'))
  } finally {
    activitySubmitting.value = false
  }
}

const updateStatus = async (row, status) => {
  const actionText = status === 0 ? '取消活动' : '恢复报名'
  ElMessageBox.confirm(`确定${actionText}吗？`, '提示', { type: 'warning' })
    .then(async () => {
      try {
        const res = await updateActivityStatus(row.id, status)
        if (res.code === 200 || res.code === '200') {
          ElMessage.success('操作成功')
          loadActivities()
        } else {
          ElMessage.error(res.msg || '状态更新失败')
        }
      } catch (error) {
        ElMessage.error(getErrorMessage(error, '状态更新失败'))
      }
    })
    .catch(() => {})
}

const openReviewDrawer = async (row) => {
  reviewingActivityId.value = row.id
  registrationFilter.value = 'pending'
  registrationPageNum.value = 1
  rejectRemark.value = ''
  reviewDrawerVisible.value = true
  await loadActivityRegistrations()
}

const loadActivityRegistrations = async () => {
  if (!reviewingActivityId.value) return
  reviewLoading.value = true
  try {
    const res = await getActivityRegistrations(
      reviewingActivityId.value,
      registrationPageNum.value,
      registrationPageSize.value
    )
    if ((res.code === 200 || res.code === '200') && res.data) {
      registrationRows.value = res.data.records || []
      registrationTotal.value = res.data.total || 0
    } else {
      registrationRows.value = []
      registrationTotal.value = 0
    }
  } catch (error) {
    ElMessage.error(getErrorMessage(error, '报名记录加载失败'))
  } finally {
    reviewLoading.value = false
  }
}

const handleRegistrationPage = (p) => {
  registrationPageNum.value = p
  loadActivityRegistrations()
}

const handleRegistrationFilterChange = (value) => {
  registrationFilter.value = value
  registrationPageNum.value = 1
  loadActivityRegistrations()
}

const doReview = async (row, status) => {
  if (status === 2 && !rejectRemark.value.trim()) {
    ElMessage.warning('拒绝时请先填写统一拒绝说明')
    return
  }
  reviewSubmitting.value = true
  try {
    const res = await reviewRegistration(
      row.id,
      status,
      status === 2 ? rejectRemark.value.trim() : ''
    )
    if (res.code === 200 || res.code === '200') {
      ElMessage.success('审核成功')
      await loadActivityRegistrations()
      await loadActivities()
    } else {
      ElMessage.error(res.msg || '审核失败')
    }
  } catch (error) {
    ElMessage.error(getErrorMessage(error, '审核失败'))
  } finally {
    reviewSubmitting.value = false
  }
}

const handleOffShelf = (row) => {
  ElMessageBox.confirm('确定下架该笔记？访客将无法查看。', '提示', { type: 'warning' })
    .then(async () => {
      const res = await deleteNote(row.id)
      if (res.code === 200 || res.code === '200') {
        ElMessage.success('已下架')
        loadList()
      } else {
        ElMessage.error(res.msg || '操作失败')
      }
    })
    .catch(() => {})
}

const changeMemberRole = (row, targetRole) => {
  const actionText = targetRole === 2 ? '设为管理员' : '取消管理员'
  ElMessageBox.confirm(`确定要将「${displayUserLabel(row)}」${actionText}吗？`, '提示', { type: 'warning' })
    .then(async () => {
      const res = await setClubMemberRole(clubId.value, row.userId, targetRole)
      if (res.code === 200 || res.code === '200') {
        ElMessage.success('操作成功')
        loadMembers()
      } else {
        ElMessage.error(res.msg || '操作失败，请稍后重试')
      }
    })
    .catch(() => {})
}

const handlePage = (p) => {
  pageNum.value = p
  loadList()
}

const handleMembersPage = (p) => {
  membersPageNum.value = p
  loadMembers()
}

onMounted(async () => {
  await verifyAccess()
  if (allowed.value) {
    await Promise.all([loadList(), loadMembers(), loadActivities()])
  }
})
</script>

<template>
  <div class="manage-wrap" v-loading="checking">
    <template v-if="allowed && !checking">
      <div class="toolbar">
        <el-button text type="primary" @click="router.push(`/front/club/${clubId}`)">
          返回社团主页
        </el-button>
      </div>
      <h2 class="title">社团管理</h2>
      <el-tabs v-model="activeTab">
        <el-tab-pane label="活动管理" name="activities">
          <div class="tab-toolbar">
            <el-button type="primary" @click="openCreateActivity">发布活动</el-button>
          </div>
          <el-table v-loading="activitiesLoading" :data="activities" stripe style="width: 100%">
            <el-table-column prop="title" label="标题" min-width="180" show-overflow-tooltip />
            <el-table-column label="状态" width="110">
              <template #default="{ row }">
                <el-tag :type="activityStatusTagType(getActivityDisplayStatus(row))">
                  {{ activityStatusLabel(getActivityDisplayStatus(row)) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="时间" min-width="240">
              <template #default="{ row }">
                {{ formatDateTime(row.startTime) }} - {{ formatDateTime(row.endTime) }}
              </template>
            </el-table-column>
            <el-table-column prop="location" label="地点" min-width="130" show-overflow-tooltip />
            <el-table-column label="人数" width="120">
              <template #default="{ row }">
                {{ row.currentParticipants || 0 }}/{{ row.maxParticipants || '不限' }}
              </template>
            </el-table-column>
            <el-table-column label="审核" width="100">
              <template #default="{ row }">
                <el-tag :type="row.needApproval === 1 ? 'warning' : 'success'">
                  {{ row.needApproval === 1 ? '需要' : '无需' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" min-width="240" fixed="right">
              <template #default="{ row }">
                <el-button type="primary" link @click="openEditActivity(row)">编辑</el-button>
                <el-button
                  v-if="row.status !== 0"
                  type="warning"
                  link
                  @click="updateStatus(row, 0)"
                >
                  取消
                </el-button>
                <el-button v-else type="success" link @click="updateStatus(row, 1)">恢复</el-button>
                <el-button
                  v-if="row.needApproval === 1"
                  type="info"
                  link
                  @click="openReviewDrawer(row)"
                >
                  报名审核
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <el-tab-pane label="笔记管理" name="notes">
          <el-table v-loading="loading" :data="tableData" stripe style="width: 100%">
            <el-table-column prop="id" label="ID" width="72" />
            <el-table-column prop="title" label="标题" min-width="160" show-overflow-tooltip />
            <el-table-column prop="authorName" label="作者" width="120" show-overflow-tooltip />
            <el-table-column prop="createTime" label="发布时间" width="170" />
            <el-table-column label="操作" width="120" fixed="right">
              <template #default="{ row }">
                <el-button type="danger" link @click="handleOffShelf(row)">下架</el-button>
              </template>
            </el-table-column>
          </el-table>
          <div class="pager" v-if="total > pageSize">
            <el-pagination
              background
              layout="prev, pager, next"
              :total="total"
              :page-size="pageSize"
              :current-page="pageNum"
              @current-change="handlePage"
            />
          </div>
        </el-tab-pane>

        <el-tab-pane label="成员管理" name="members">
          <el-table v-loading="membersLoading" :data="members" stripe style="width: 100%">
            <el-table-column label="昵称" min-width="120" show-overflow-tooltip>
              <template #default="{ row }">{{ formatCell(row.nickname) }}</template>
            </el-table-column>
            <el-table-column label="姓名" min-width="100" show-overflow-tooltip>
              <template #default="{ row }">{{ formatCell(row.realName) }}</template>
            </el-table-column>
            <el-table-column label="学号" min-width="130" show-overflow-tooltip>
              <template #default="{ row }">{{ formatCell(row.studentId) }}</template>
            </el-table-column>
            <el-table-column label="角色" width="120">
              <template #default="{ row }">
                <el-tag :type="roleTagType(row.role)">{{ roleLabel(row.role) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="joinTime" label="加入时间" min-width="180" />
            <el-table-column label="操作" width="180" fixed="right">
              <template #default="{ row }">
                <template v-if="isCurrentUserPresident">
                  <el-button
                    v-if="row.role === 1"
                    type="primary"
                    link
                    @click="changeMemberRole(row, 2)"
                  >
                    设为管理员
                  </el-button>
                  <el-button
                    v-else-if="row.role === 2"
                    type="warning"
                    link
                    @click="changeMemberRole(row, 1)"
                  >
                    取消管理员
                  </el-button>
                  <el-tag v-else type="danger">社长不可操作</el-tag>
                </template>
                <el-tag v-else type="info">仅社长可操作</el-tag>
              </template>
            </el-table-column>
          </el-table>
          <div class="pager" v-if="membersTotal > membersPageSize">
            <el-pagination
              background
              layout="prev, pager, next"
              :total="membersTotal"
              :page-size="membersPageSize"
              :current-page="membersPageNum"
              @current-change="handleMembersPage"
            />
          </div>
        </el-tab-pane>
      </el-tabs>

      <el-dialog
        v-model="activityDialogVisible"
        :title="editingActivityId ? '编辑活动' : '发布活动'"
        width="700px"
      >
        <el-form ref="activityFormRef" :model="activityForm" :rules="activityRules" label-width="90px">
          <el-form-item label="活动标题" prop="title">
            <el-input v-model="activityForm.title" maxlength="100" show-word-limit />
          </el-form-item>
          <el-form-item label="活动描述">
            <el-input v-model="activityForm.description" type="textarea" :rows="4" />
          </el-form-item>
          <el-form-item label="活动地点">
            <el-input v-model="activityForm.location" />
          </el-form-item>
          <el-form-item label="开始时间" prop="startTime">
            <el-date-picker
              v-model="activityForm.startTime"
              type="datetime"
              value-format="YYYY-MM-DDTHH:mm:ss"
              placeholder="选择开始时间"
              style="width: 100%"
            />
          </el-form-item>
          <el-form-item label="结束时间" prop="endTime">
            <el-date-picker
              v-model="activityForm.endTime"
              type="datetime"
              value-format="YYYY-MM-DDTHH:mm:ss"
              placeholder="选择结束时间"
              style="width: 100%"
            />
          </el-form-item>
          <el-form-item label="人数上限">
            <el-input-number v-model="activityForm.maxParticipants" :min="1" :step="1" />
            <span class="field-tip">留空代表不限人数</span>
          </el-form-item>
          <el-form-item label="审核报名">
            <el-switch
              v-model="activityForm.needApproval"
              :active-value="1"
              :inactive-value="0"
              active-text="需要"
              inactive-text="无需"
            />
          </el-form-item>
          <el-form-item label="活动标签">
            <el-input v-model="activityForm.tags" placeholder="多个标签用英文逗号分隔" />
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="activityDialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="activitySubmitting" @click="submitActivity">
            提交
          </el-button>
        </template>
      </el-dialog>

      <el-drawer v-model="reviewDrawerVisible" title="报名审核" size="62%">
        <el-alert
          title="拒绝报名时将使用下方统一拒绝说明"
          type="info"
          show-icon
          :closable="false"
          class="review-alert"
        />
        <el-input
          v-model="rejectRemark"
          type="textarea"
          :rows="2"
          placeholder="填写拒绝说明（仅拒绝时必填）"
          class="review-remark"
        />
        <div class="review-filter">
          <el-radio-group
            v-model="registrationFilter"
            size="small"
            @change="handleRegistrationFilterChange"
          >
            <el-radio-button label="pending">待审核</el-radio-button>
            <el-radio-button label="approved">已通过</el-radio-button>
            <el-radio-button label="all">全部</el-radio-button>
          </el-radio-group>
        </div>
        <el-table v-loading="reviewLoading" :data="displayRegistrations" stripe>
          <el-table-column label="昵称" min-width="110" show-overflow-tooltip>
            <template #default="{ row }">{{ formatCell(row.nickname) }}</template>
          </el-table-column>
          <el-table-column label="姓名" min-width="90" show-overflow-tooltip>
            <template #default="{ row }">{{ formatCell(row.realName) }}</template>
          </el-table-column>
          <el-table-column label="学号" min-width="120" show-overflow-tooltip>
            <template #default="{ row }">{{ formatCell(row.studentId) }}</template>
          </el-table-column>
          <el-table-column label="报名时间" min-width="170">
            <template #default="{ row }">{{ formatDateTime(row.createTime) }}</template>
          </el-table-column>
          <el-table-column label="备注" min-width="180" show-overflow-tooltip>
            <template #default="{ row }">{{ row.remark || '-' }}</template>
          </el-table-column>
          <el-table-column label="状态" width="100">
            <template #default="{ row }">{{ registrationStatusLabel(row.status) }}</template>
          </el-table-column>
          <el-table-column label="操作" width="160" fixed="right">
            <template #default="{ row }">
              <el-button
                v-if="row.status === 0"
                type="success"
                link
                :loading="reviewSubmitting"
                @click="doReview(row, 1)"
              >
                通过
              </el-button>
              <el-button
                v-if="row.status === 0"
                type="danger"
                link
                :loading="reviewSubmitting"
                @click="doReview(row, 2)"
              >
                拒绝
              </el-button>
              <el-tag v-if="row.status !== 0" size="small" type="info">不可操作</el-tag>
            </template>
          </el-table-column>
        </el-table>
        <div class="pager" v-if="registrationTotal > registrationPageSize">
          <el-pagination
            background
            layout="prev, pager, next"
            :total="registrationTotal"
            :page-size="registrationPageSize"
            :current-page="registrationPageNum"
            @current-change="handleRegistrationPage"
          />
        </div>
      </el-drawer>
    </template>
  </div>
</template>

<style scoped lang="scss">
.manage-wrap {
  max-width: 960px;
  margin: 0 auto;
  padding: 24px 16px 48px;
  min-height: calc(100vh - 90px);
  font-size: 18px;
}

.manage-wrap :deep(.el-table) {
  font-size: 18px;
}

.manage-wrap :deep(.el-button) {
  font-size: 18px;
}

.manage-wrap :deep(.el-pagination) {
  font-size: 18px;
}

.toolbar {
  margin-bottom: 12px;
}

.tab-toolbar {
  margin-bottom: 12px;
  display: flex;
  justify-content: flex-end;
}

.title {
  margin: 0 0 16px;
  font-size: 24px;
  font-weight: 600;
  color: var(--bb-text-primary);
}

.pager {
  margin-top: 16px;
  display: flex;
  justify-content: center;
}

.field-tip {
  margin-left: 10px;
  color: var(--bb-text-muted);
}

.review-alert {
  margin-bottom: 10px;
}

.review-remark {
  margin-bottom: 12px;
}

.review-filter {
  margin-bottom: 12px;
}
</style>
