<script setup>
import { ref, reactive } from 'vue'
import { Search, Plus, Delete, Edit, UploadFilled, Key, Check, Close } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { serverHost } from '../../../config/config.default'
import request from '../../utils/request'

// 表格数据
const tableData = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)

// 搜索条件
const searchForm = reactive({
  keyword: '',
  isVerified: '',
})

// 表单数据
const form = ref({})
const dialogFormVisible = ref(false)
const multipleSelection = ref([])
const resetPasswordDialogVisible = ref(false)
const resetPasswordForm = reactive({
  userId: null,
  username: '',
  newPassword: '',
})

// 加载数据
const load = () => {
  request.get("/user/manage/page", {
    params: {
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      keyword: searchForm.keyword,
      status: searchForm.isVerified === '' ? undefined : Number(searchForm.isVerified),
    }
  }).then(res => {
    if (res.data) {
      tableData.value = res.data.records
      total.value = res.data.total
    }
  })
}
load()

// 保存
const save = () => {
  const payload = { ...form.value }
  if (payload.id && !payload.password) {
    delete payload.password
  }
  request.post("/user", payload).then(res => {
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
    password: ''
  }
  dialogFormVisible.value = true
}

// 编辑
const handleEdit = (row) => {
  form.value = JSON.parse(JSON.stringify(row))
  form.value.password = ''
  dialogFormVisible.value = true
}

// 删除
const del = (id) => {
  request.delete("/user/" + id).then(res => {
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
  request.post("/user/del/batch", ids).then(res => {
    if (res.code === '200') {
      ElMessage.success("批量删除成功")
      load()
    } else {
      ElMessage.error("批量删除失败")
    }
  })
}

// 重置搜索
const reset = () => {
  searchForm.keyword = ""
  searchForm.isVerified = ""
  load()
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

const reviewVerification = (row, approved) => {
  request.post('/user/verify/review', {
    userId: row.id,
    approved
  }).then(res => {
    if (res.code === '200' || res.code === 200) {
      ElMessage.success(approved ? '审核通过' : '已驳回')
      load()
    } else {
      ElMessage.error(res.msg || '审核失败')
    }
  })
}

const confirmReview = (row, approved) => {
  ElMessageBox.confirm(
      approved ? '确认通过该用户认证申请？' : '确认驳回该用户认证申请？',
      '认证审核',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: approved ? 'success' : 'warning',
      }
  ).then(() => reviewVerification(row, approved))
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
      '确定要删除这条数据吗？',
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
      '确定要批量删除这些数据吗？',
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

// 头像上传
const handleAvatarUrlUploadSuccess = (res) => {
  form.value.avatarUrl = res;
};

const openResetPassword = (row) => {
  resetPasswordForm.userId = row.id
  resetPasswordForm.username = row.username
  resetPasswordForm.newPassword = ''
  resetPasswordDialogVisible.value = true
}

const submitResetPassword = () => {
  if (!resetPasswordForm.newPassword || resetPasswordForm.newPassword.trim().length < 6) {
    ElMessage.warning('新密码至少 6 位')
    return
  }
  request.post('/user/reset-password', {
    userId: resetPasswordForm.userId,
    newPassword: resetPasswordForm.newPassword.trim()
  }).then(res => {
    if (res.code === '200') {
      ElMessage.success('重置密码成功')
      resetPasswordDialogVisible.value = false
    } else {
      ElMessage.error(res.msg || '重置密码失败')
    }
  })
}

const accountTypeText = (value) => {
  const map = {
    1: '普通用户',
    2: '社团账号',
    3: '机构账号',
    4: '企业账号'
  }
  return map[value] || '未知'
}

const accountTypeTagType = (value) => {
  const map = {
    1: '',
    2: 'success',
    3: 'warning',
    4: 'danger'
  }
  return map[value] ?? 'info'
}

const formatDate = (value) => {
  if (!value) return '-'
  if (Array.isArray(value)) {
    const [y, m, d, hh = 0, mm = 0, ss = 0] = value
    const pad = (n) => String(n).padStart(2, '0')
    return `${y}-${pad(m)}-${pad(d)} ${pad(hh)}:${pad(mm)}:${pad(ss)}`
  }
  if (typeof value === 'string') {
    return value.replace('T', ' ').slice(0, 19)
  }
  return String(value)
}


</script>

<template>
  <div class="content-container">

    <!-- 搜索区域 -->
    <div class="header-section">
      <el-input v-model="searchForm.keyword" placeholder="昵称/用户名/手机号/邮箱" class="filter-input" :prefix-icon="Search" clearable/>
      <el-select v-model="searchForm.isVerified" class="ml-10" placeholder="认证状态" clearable style="width: 130px">
        <el-option label="未认证" :value="0" />
        <el-option label="待审核" :value="2" />
        <el-option label="已认证" :value="1" />
      </el-select>
      <el-button class="ml-10" plain type="primary" @click="load">搜索</el-button>
      <el-button plain type="info" @click="reset">重置</el-button>
    </div>

    <!-- 操作按钮区域 -->
    <div class="toolbar-section">
      <el-button plain type="primary" @click="handleAdd" :icon="Plus">新增</el-button>
      <el-button plain type="danger" @click="confirmBatchDelete" :icon="Delete">批量删除</el-button>
    </div>

    <!-- 表格区域 -->
    <el-card>
      <el-table :data="tableData" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="60" align="center" />
        <el-table-column prop="id" label="ID" width="80" align="center" />
        <el-table-column prop="username" label="用户名" min-width="120" show-overflow-tooltip />
        <el-table-column prop="nickname" label="昵称" min-width="120" show-overflow-tooltip />
        <el-table-column prop="email" label="邮箱" min-width="160" show-overflow-tooltip />
        <el-table-column prop="phone" label="电话" width="130" />

        <el-table-column label="账号类型" width="110" align="center">
          <template #default="scope">
            <el-tag :type="accountTypeTagType(scope.row.accountType)">
              {{ accountTypeText(scope.row.accountType) }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="认证状态" width="100" align="center">
          <template #default="scope">
            <el-tag :type="verifyStatusType(scope.row.isVerified)">
              {{ verifyStatusText(scope.row.isVerified) }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="状态" width="90" align="center">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">
              {{ scope.row.status === 1 ? '正常' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="clubCount" label="社团数" width="90" align="center">
          <template #default="scope">
            {{ scope.row.clubCount || 0 }}
          </template>
        </el-table-column>

        <el-table-column prop="clubNames" label="所属社团" min-width="180" show-overflow-tooltip>
          <template #default="scope">
            {{ scope.row.clubNames || '-' }}
          </template>
        </el-table-column>

        <el-table-column prop="registerTime" label="注册时间" width="170" align="center">
          <template #default="scope">
            {{ formatDate(scope.row.registerTime) }}
          </template>
        </el-table-column>

        <el-table-column label="头像" width="100" align="center">
          <template #default="scope">
            <el-avatar :size="60" :src="scope.row.avatarUrl"/>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="230" align="center" fixed="right">
          <template #default="scope">
            <el-tooltip v-if="scope.row.isVerified === 2" content="通过认证" placement="top" :effect="'light'">
              <el-button circle type="success" :icon="Check" @click="confirmReview(scope.row, true)"/>
            </el-tooltip>
            <el-tooltip v-if="scope.row.isVerified === 2" content="驳回认证" placement="top" :effect="'light'">
              <el-button circle type="warning" :icon="Close" @click="confirmReview(scope.row, false)"/>
            </el-tooltip>
            <el-tooltip content="编辑" placement="top" :effect="'light'">
              <el-button circle type="primary" :icon="Edit" @click="handleEdit(scope.row)"/>
            </el-tooltip>
            <el-tooltip content="重置密码" placement="top" :effect="'light'">
              <el-button circle type="warning" :icon="Key" @click="openResetPassword(scope.row)"/>
            </el-tooltip>
            <el-tooltip content="删除" placement="top" :effect="'light'">
              <el-button circle type="danger" :icon="Delete" @click="confirmDelete(scope.row.id)"/>
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
    <el-dialog v-model="dialogFormVisible" :title="form.id ? '编辑' : '新增'" width="30%" destroy-on-close center>
      <el-form :model="form" label-width="100px">
        <el-form-item label="头像" required>
          <div class="upload-container">
            <el-avatar v-if="form.avatarUrl" :src="form.avatarUrl" :size="80" />
            <el-upload :action="`${serverHost}/web/upload`" :on-success="handleAvatarUrlUploadSuccess" :show-file-list="false">
              <el-button type="primary" :icon="UploadFilled">{{ form.avatarUrl ? '更换图片' : '上传图片' }}</el-button>
            </el-upload>
          </div>
        </el-form-item>
        <el-form-item label="用户名" required>
          <el-input v-model="form.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item :label="form.id ? '密码(留空不修改)' : '密码'" :required="!form.id">
          <el-input v-model="form.password" show-password :placeholder="form.id ? '留空则不修改密码' : '请输入密码'"></el-input>
        </el-form-item>
        <el-form-item label="昵称" required>
          <el-input v-model="form.nickname" placeholder="请输入昵称" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="form.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="电话">
          <el-input v-model="form.phone" placeholder="请输入电话" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogFormVisible = false">取消</el-button>
          <el-button type="primary" @click="save">确定</el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog v-model="resetPasswordDialogVisible" title="重置密码" width="400px" destroy-on-close center>
      <el-form label-width="90px">
        <el-form-item label="用户名">
          <el-input v-model="resetPasswordForm.username" disabled />
        </el-form-item>
        <el-form-item label="新密码" required>
          <el-input v-model="resetPasswordForm.newPassword" show-password placeholder="请输入新密码（至少 6 位）" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="resetPasswordDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitResetPassword">确定</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>

</style>