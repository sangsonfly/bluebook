<script setup>
import { ref, reactive} from 'vue'
import { Search, Plus, Delete, Edit, Check, Close, UploadFilled} from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { serverHost } from '../../../config/config.default'
import request from '../../utils/request'
import { formatDate } from '@/utils/date'
import { setPresident } from '@/api/clubMember'

// 表格数据
const tableData = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)

// 搜索条件
const searchForm = reactive({
  keyword: '',
  category: '',
  isVerified: ''
})

// 分类选项
const categoryOptions = [
  { label: '全部', value: '' },
  { label: '体育运动', value: '体育运动' },
  { label: '文化艺术', value: '文化艺术' },
  { label: '公益服务', value: '公益服务' },
  { label: '学术科技', value: '学术科技' },
]

// 认证状态选项
const verifiedOptions = [
  { label: '全部', value: '' },
  { label: '已认证', value: '1' },
  { label: '未认证', value: '0' },
]

// 表单数据
const form = ref({})
const dialogFormVisible = ref(false)
const multipleSelection = ref([])
const presidentDialogVisible = ref(false)
const presidentForm = reactive({
  clubId: null,
  clubName: '',
  userId: null
})

// 加载数据
const load = () => {
  request.get("/api/club/adminPage", {
    params: {
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      keyword: searchForm.keyword,
      category: searchForm.category,
      isVerified: searchForm.isVerified === '' ? undefined : Number(searchForm.isVerified),
    }
  }).then(res => {
    if (res.data) {
      // 如果返回的是数组，直接使用
      if (Array.isArray(res.data)) {
        tableData.value = res.data.map(item => ({
          ...item,
          adminCount: item.adminCount ?? 0,
          adminPreviewNames: Array.isArray(item.adminPreviewNames) ? item.adminPreviewNames : []
        }))
        total.value = res.data.length
      } else if (res.data.records) {
        // 如果是分页数据
        tableData.value = (res.data.records || []).map(item => ({
          ...item,
          adminCount: item.adminCount ?? 0,
          adminPreviewNames: Array.isArray(item.adminPreviewNames) ? item.adminPreviewNames : []
        }))
        total.value = res.data.total
      }
    }
  })
}
load()

// 保存
const save = () => {
  request.post("/api/club", form.value).then(res => {
    if (res.code === '200') {
      ElMessage.success("保存成功")
      dialogFormVisible.value = false
      load()
    } else {
      ElMessage.error("保存失败")
    }
  })
}

// 添加
const handleAdd = () => {
  form.value = { 
    isVerified: 0,
    status: 1,
    memberCount: 0
  }
  dialogFormVisible.value = true
}

// 编辑
const handleEdit = (row) => {
  form.value = JSON.parse(JSON.stringify(row))
  dialogFormVisible.value = true
}

// 删除
const del = (id) => {
  request.delete("/api/club/" + id).then(res => {
    if (res.code === '200') {
      ElMessage.success("删除成功")
      load()
    } else {
      ElMessage.error("删除失败")
    }
  })
}

// 批量删除
const delBatch = () => {
  if (multipleSelection.value.length === 0) {
    ElMessage.warning("请至少选择一条记录")
    return
  }

  const ids = multipleSelection.value.map(v => v.id)
  const promises = ids.map(id => request.delete("/api/club/" + id))
  
  Promise.all(promises).then(() => {
    ElMessage.success("批量删除成功")
    load()
  }).catch(() => {
    ElMessage.error("批量删除失败")
  })
}

// 修改认证状态
const changeVerified = (row) => {
  const newStatus = row.isVerified === 1 ? 0 : 1
  const statusText = newStatus === 1 ? '认证' : '取消认证'
  
  ElMessageBox.confirm(
    `确定要${statusText}这个社团吗？`,
    '提示',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    }
  ).then(() => {
    request.post('/api/club', { ...row, isVerified: newStatus }).then(res => {
      if (res.code === '200') {
        ElMessage.success(`${statusText}成功`)
        load()
      } else {
        ElMessage.error(`${statusText}失败`)
      }
    })
  })
}

// 修改状态（启用/禁用）
const changeStatus = (row) => {
  const newStatus = row.status === 1 ? 0 : 1
  const statusText = newStatus === 1 ? '启用' : '禁用'
  
  ElMessageBox.confirm(
    `确定要${statusText}这个社团吗？`,
    '提示',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    }
  ).then(() => {
    request.post('/api/club', { ...row, status: newStatus }).then(res => {
      if (res.code === '200') {
        ElMessage.success(`${statusText}成功`)
        load()
      } else {
        ElMessage.error(`${statusText}失败`)
      }
    })
  })
}

// 重置搜索
const reset = () => {
  searchForm.keyword = ""
  searchForm.category = ""
  searchForm.isVerified = ""
  load()
}

// 表格选择变化
const handleSelectionChange = (val) => {
  multipleSelection.value = val
}

// 分页大小变化
const handleSizeChange = (size) => {
  pageSize.value = size
  load()
}

// 页码变化
const handleCurrentChange = (current) => {
  pageNum.value = current
  load()
}

// 确认删除
const confirmDelete = (id) => {
  ElMessageBox.confirm(
      '确定要删除这个社团吗？删除后将无法恢复！',
      '警告',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }
  )
      .then(() => {
        del(id)
      })
}

// 确认批量删除
const confirmBatchDelete = () => {
  if (multipleSelection.value.length === 0) {
    ElMessage.warning("请至少选择一条记录")
    return
  }

  ElMessageBox.confirm(
      '确定要批量删除这些社团吗？',
      '警告',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }
  )
      .then(() => {
        delBatch()
      })
}

const openSetPresidentDialog = (row) => {
  presidentForm.clubId = row.id
  presidentForm.clubName = row.name
  presidentForm.userId = null
  presidentDialogVisible.value = true
}

const submitSetPresident = () => {
  if (!presidentForm.userId) {
    ElMessage.warning('请输入要设置为社长的用户ID')
    return
  }

  ElMessageBox.confirm(
    `确定将用户 ${presidentForm.userId} 设为【${presidentForm.clubName}】社长吗？`,
    '确认设置社长',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    }
  ).then(async () => {
    const res = await setPresident(presidentForm.clubId, presidentForm.userId)
    if (res.code === 200 || res.code === '200') {
      ElMessage.success('设置社长成功')
      presidentDialogVisible.value = false
      load()
    } else {
      ElMessage.error(res.msg || '设置社长失败')
    }
  }).catch(() => {})
}

// 头像上传成功
const handleAvatarUploadSuccess = (res) => {
  form.value.avatarUrl = res
}

// 封面上传成功
const handleCoverUploadSuccess = (res) => {
  form.value.coverUrl = res
}

const formatContactSummary = (contactInfo) => {
  if (!contactInfo) return '未填写'
  if (typeof contactInfo !== 'string') return '未填写'

  try {
    const contact = JSON.parse(contactInfo)
    const fields = [contact.phone, contact.wechat, contact.email]
      .filter(v => typeof v === 'string' && v.trim())
      .map(v => v.trim())
    if (!fields.length) return '未填写'
    const summary = fields.slice(0, 2).join(' / ')
    return summary.length > 24 ? `${summary.slice(0, 24)}...` : summary
  } catch (e) {
    return contactInfo.length > 24 ? `${contactInfo.slice(0, 24)}...` : contactInfo
  }
}

</script>

<template>
  <div class="content-container">

    <!-- 搜索区域 -->
    <div class="header-section">
      <el-input v-model="searchForm.keyword" placeholder="请输入社团名称" class="filter-input" :prefix-icon="Search" clearable/>
      
      <el-select v-model="searchForm.category" placeholder="选择分类" class="filter-select" clearable>
        <el-option
          v-for="item in categoryOptions"
          :key="item.value"
          :label="item.label"
          :value="item.value"
        />
      </el-select>

      <el-select v-model="searchForm.isVerified" placeholder="认证状态" class="filter-select" clearable>
        <el-option
          v-for="item in verifiedOptions"
          :key="item.value"
          :label="item.label"
          :value="item.value"
        />
      </el-select>

      <el-button class="ml-10" plain type="primary" @click="load">搜索</el-button>
      <el-button plain type="info" @click="reset">重置</el-button>
    </div>

    <!-- 操作按钮区域 -->
    <div class="toolbar-section">
      <el-button plain type="primary" @click="handleAdd" :icon="Plus">新增社团</el-button>
      <el-button plain type="danger" @click="confirmBatchDelete" :icon="Delete">批量删除</el-button>
    </div>

    <!-- 表格区域 -->
    <el-card>
      <el-table :data="tableData" @selection-change="handleSelectionChange" style="width: 100%">
        <el-table-column type="selection" width="60" align="center" />
        <el-table-column prop="id" label="ID" width="80" align="center" />
        
        <el-table-column label="头像" width="100" align="center">
          <template #default="scope">
            <el-avatar :size="60" :src="scope.row.avatarUrl"/>
          </template>
        </el-table-column>

        <el-table-column prop="name" label="社团名称" min-width="150" show-overflow-tooltip />
        
        <el-table-column prop="description" label="简介" min-width="200" show-overflow-tooltip />
        
        <el-table-column prop="category" label="分类" width="120" align="center" />

        <el-table-column label="社长" min-width="180">
          <template #default="scope">
            <div v-if="scope.row.presidentUserId" class="president-cell">
              <el-avatar :size="26" :src="scope.row.presidentAvatar" />
              <span class="president-name">
                {{ scope.row.presidentName || `用户${scope.row.presidentUserId}` }}
              </span>
              <span class="president-id">ID: {{ scope.row.presidentUserId }}</span>
            </div>
            <el-tag v-else type="warning">未设置</el-tag>
          </template>
        </el-table-column>

        <el-table-column label="管理员" width="170" align="center">
          <template #default="scope">
            <el-tooltip
              v-if="scope.row.adminCount > 0"
              :content="scope.row.adminPreviewNames.length ? `${scope.row.adminPreviewNames.join('、')}${scope.row.adminCount > scope.row.adminPreviewNames.length ? ' 等' : ''}` : '管理员信息未命名'"
              placement="top"
            >
              <el-tag type="primary">
                {{ scope.row.adminCount }} 人
              </el-tag>
            </el-tooltip>
            <el-tag v-else type="info">0 人</el-tag>
          </template>
        </el-table-column>
        
        <el-table-column prop="memberCount" label="成员数" width="100" align="center">
          <template #default="scope">
            <el-tag type="info">{{ scope.row.memberCount || 0 }} 人</el-tag>
          </template>
        </el-table-column>

        <el-table-column label="联系方式" min-width="170" show-overflow-tooltip>
          <template #default="scope">
            {{ formatContactSummary(scope.row.contactInfo) }}
          </template>
        </el-table-column>

        <el-table-column label="认证状态" width="170" align="center">
          <template #default="scope">
            <div class="verify-cell">
              <el-tag :type="scope.row.isVerified === 1 ? 'success' : 'warning'">
                {{ scope.row.isVerified === 1 ? '已认证' : '未认证' }}
              </el-tag>
              <span v-if="scope.row.isVerified === 1 && scope.row.verifyTime" class="verify-time">
                {{ formatDate(scope.row.verifyTime) }}
              </span>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="状态" width="100" align="center">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">
              {{ scope.row.status === 1 ? '正常' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="createTime" label="创建时间" width="160" align="center">
          <template #default="scope">
            {{ formatDate(scope.row.createTime) }}
          </template>
        </el-table-column>

        <el-table-column label="操作" width="320" align="center" fixed="right">
          <template #default="scope">
            <el-tooltip content="编辑" placement="top" :effect="'light'">
              <el-button circle type="primary" :icon="Edit" @click="handleEdit(scope.row)" size="small"/>
            </el-tooltip>
            <el-tooltip content="设置社长" placement="top" :effect="'light'">
              <el-button circle type="warning" @click="openSetPresidentDialog(scope.row)" size="small">
                社
              </el-button>
            </el-tooltip>
            <el-tooltip :content="scope.row.isVerified === 1 ? '取消认证' : '认证'" placement="top" :effect="'light'">
              <el-button 
                circle 
                :type="scope.row.isVerified === 1 ? 'warning' : 'success'" 
                :icon="scope.row.isVerified === 1 ? Close : Check" 
                @click="changeVerified(scope.row)" 
                size="small"
              />
            </el-tooltip>
            <el-tooltip :content="scope.row.status === 1 ? '禁用' : '启用'" placement="top" :effect="'light'">
              <el-button 
                circle 
                :type="scope.row.status === 1 ? 'info' : 'success'" 
                @click="changeStatus(scope.row)" 
                size="small"
              >
                {{ scope.row.status === 1 ? '禁' : '启' }}
              </el-button>
            </el-tooltip>
            <el-tooltip content="删除" placement="top" :effect="'light'">
              <el-button circle type="danger" :icon="Delete" @click="confirmDelete(scope.row.id)" size="small"/>
            </el-tooltip>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页区域 -->
      <div class="pagination-section">
        <el-pagination
            v-model:current-page="pageNum"
            v-model:page-size="pageSize"
            :page-sizes="[10, 20, 50, 100]"
            layout="total, sizes, prev, pager, next, jumper"
            :total="total"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 表单对话框 -->
    <el-dialog
      v-model="dialogFormVisible"
      :title="form.id ? '编辑社团' : '新增社团'"
      width="50%"
      destroy-on-close
      center
      class="club-admin-dialog"
    >
      <el-form :model="form" label-width="100px">
        <el-form-item label="社团名称" required>
          <el-input v-model="form.name" placeholder="请输入社团名称" />
        </el-form-item>
        
        <el-form-item label="社团简介" required>
          <el-input v-model="form.description" type="textarea" :rows="4" placeholder="请输入社团简介" />
        </el-form-item>

        <el-form-item label="社团头像" required>
          <div class="upload-container">
            <el-avatar v-if="form.avatarUrl" :src="form.avatarUrl" :size="80" />
            <el-upload 
              :action="`${serverHost}/web/upload`" 
              :on-success="handleAvatarUploadSuccess" 
              :show-file-list="false"
            >
              <el-button type="primary" :icon="UploadFilled">{{ form.avatarUrl ? '更换头像' : '上传头像' }}</el-button>
            </el-upload>
          </div>
        </el-form-item>

        <el-form-item label="封面图">
          <div class="upload-container">
            <el-image 
              v-if="form.coverUrl" 
              :src="form.coverUrl" 
              style="width: 200px; height: 100px; border-radius: 8px;"
              fit="cover"
            />
            <el-upload 
              :action="`${serverHost}/web/upload`" 
              :on-success="handleCoverUploadSuccess" 
              :show-file-list="false"
            >
              <el-button type="primary" :icon="UploadFilled">{{ form.coverUrl ? '更换封面' : '上传封面' }}</el-button>
            </el-upload>
          </div>
        </el-form-item>

        <el-form-item label="社团分类" required>
          <el-select v-model="form.category" placeholder="请选择社团分类" style="width: 100%">
            <el-option label="体育运动" value="体育运动" />
            <el-option label="文化艺术" value="文化艺术" />
            <el-option label="公益服务" value="公益服务" />
            <el-option label="学术科技" value="学术科技" />
          </el-select>
        </el-form-item>

        <el-form-item label="成员数量">
          <el-input-number v-model="form.memberCount" :min="0" />
        </el-form-item>

        <el-form-item label="认证状态">
          <el-radio-group v-model="form.isVerified">
            <el-radio :label="1">已认证</el-radio>
            <el-radio :label="0">未认证</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio :label="1">正常</el-radio>
            <el-radio :label="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogFormVisible = false">取消</el-button>
          <el-button type="primary" @click="save">确定</el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog
      v-model="presidentDialogVisible"
      title="设置社长"
      width="420px"
      destroy-on-close
      center
      class="club-admin-dialog"
    >
      <el-form :model="presidentForm" label-width="110px">
        <el-form-item label="社团名称">
          <el-input :model-value="presidentForm.clubName" disabled />
        </el-form-item>
        <el-form-item label="目标用户ID" required>
          <el-input-number v-model="presidentForm.userId" :min="1" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="presidentDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitSetPresident">确定</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.content-container {
  padding: 20px;
  font-size: 18px;
}

.content-container :deep(.el-table),
.content-container :deep(.el-table th.el-table__cell),
.content-container :deep(.el-table td.el-table__cell) {
  font-size: 18px;
}

.content-container :deep(.el-button),
.content-container :deep(.el-input__inner),
.content-container :deep(.el-input__wrapper),
.content-container :deep(.el-select .el-input__wrapper),
.content-container :deep(.el-pagination),
.content-container :deep(.el-pagination .el-input__wrapper) {
  font-size: 18px;
}

.header-section {
  display: flex;
  gap: 10px;
  margin-bottom: 20px;
}

.filter-input {
  width: 300px;
}

.filter-select {
  width: 150px;
}

.toolbar-section {
  margin-bottom: 20px;
}

.pagination-section {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.ml-10 {
  margin-left: 10px;
}

.upload-container {
  display: flex;
  align-items: center;
  gap: 15px;
}

.president-cell {
  display: flex;
  align-items: center;
  gap: 8px;
}

.president-name {
  max-width: 80px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.president-id {
  font-size: 13px;
  color: #909399;
}

.verify-cell {
  display: flex;
  flex-direction: column;
  gap: 4px;
  align-items: center;
}

.verify-time {
  font-size: 12px;
  color: #909399;
  line-height: 1.2;
}
</style>

<style lang="scss">
.club-admin-dialog.el-dialog .el-dialog__title,
.club-admin-dialog .el-form-item__label,
.club-admin-dialog .el-input__inner,
.club-admin-dialog .el-input__wrapper,
.club-admin-dialog .el-select .el-input__wrapper,
.club-admin-dialog .el-radio__label,
.club-admin-dialog .el-button {
  font-size: 18px;
}
</style>


