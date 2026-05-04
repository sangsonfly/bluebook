<script setup>
import { ref, reactive, computed } from 'vue'
import { Search, Plus, Delete, Edit, View, ArrowDown } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '../../utils/request'
import { formatDate } from '@/utils/date'

const tableData = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)

const searchForm = reactive({
  keyword: '',
  category: '',
  status: ''
})

const categoryOptions = [
  { label: '全部', value: '' },
  { label: '学习经验', value: '学习经验' },
  { label: '美食探店', value: '美食探店' },
  { label: '社团活动', value: '社团活动' },
  { label: '校园活动', value: '校园活动' },
]

const statusOptions = [
  { label: '全部', value: '' },
  { label: '已发布', value: '1' },
  { label: '草稿', value: '0' },
  { label: '下架', value: '2' },
  { label: '已删除', value: '-1' },
]

const form = ref({})
const dialogFormVisible = ref(false)
const dialogDetailVisible = ref(false)
const detailData = ref({})
const multipleSelection = ref([])

const isFormDeleted = computed(() => form.value?.status === -1)
const isFormNew = computed(() => !form.value?.id)

const firstImageUrl = (url) => {
  if (!url || typeof url !== 'string') return ''
  return url.split(',')[0]?.trim() || ''
}

const imagePreviewList = (url) => {
  if (!url || typeof url !== 'string') return []
  return url.split(',').map((s) => s.trim()).filter(Boolean)
}

const load = () => {
  const params = {
    pageNum: pageNum.value,
    pageSize: pageSize.value,
    keyword: searchForm.keyword || undefined,
    category: searchForm.category || undefined,
  }
  if (searchForm.status !== '' && searchForm.status != null) {
    params.status = Number(searchForm.status)
  }
  request.get('/api/note/page', { params }).then((res) => {
    if (res.data) {
      tableData.value = res.data.records
      total.value = res.data.total
    }
  }).catch(() => {
    ElMessage.error('加载列表失败')
    tableData.value = []
    total.value = 0
  })
}
load()

const save = () => {
  if (isFormNew.value) {
    if (form.value.userId == null || form.value.userId === '' || Number(form.value.userId) <= 0) {
      ElMessage.warning('请填写有效的发布用户 ID')
      return
    }
  }
  if (isFormDeleted.value) {
    ElMessage.warning('已删除笔记请使用「恢复为已发布」')
    return
  }

  const method = form.value.id ? 'put' : 'post'
  request[method]('/api/note', form.value).then((res) => {
    if (res.code === '200') {
      ElMessage.success('保存成功')
      dialogFormVisible.value = false
      load()
    } else {
      ElMessage.error(res.msg || '保存失败')
    }
  })
}

const handleAdd = () => {
  form.value = { status: 1, userId: undefined }
  dialogFormVisible.value = true
}

const handleEdit = (row) => {
  form.value = JSON.parse(JSON.stringify(row))
  dialogFormVisible.value = true
}

const handleView = (row) => {
  detailData.value = row
  dialogDetailVisible.value = true
}

const del = (id) => {
  request.delete('/api/note/' + id).then((res) => {
    if (res.code === '200') {
      ElMessage.success('删除成功')
      load()
    } else {
      ElMessage.error(res.msg || '删除失败')
    }
  })
}

const delBatch = async () => {
  if (multipleSelection.value.length === 0) {
    ElMessage.warning('请至少选择一条记录')
    return
  }

  const ids = multipleSelection.value.map((v) => v.id)
  try {
    const results = await Promise.all(ids.map((id) => request.delete('/api/note/' + id)))
    let ok = 0
    let firstFailMsg = ''
    for (const res of results) {
      if (res.code === '200') ok++
      else if (!firstFailMsg && res.msg) firstFailMsg = res.msg
    }
    const n = results.length
    const fail = n - ok
    if (ok === n) {
      ElMessage.success(`批量删除成功，共 ${ok} 条`)
    } else if (ok > 0) {
      ElMessage.warning(
        `成功 ${ok} 条，失败 ${fail} 条${firstFailMsg ? `（${firstFailMsg}）` : ''}`
      )
    } else {
      ElMessage.error(firstFailMsg || '批量删除失败')
    }
    load()
  } catch {
    ElMessage.error('批量删除请求异常')
    load()
  }
}

const statusLabel = (s) => {
  if (s === 1) return '已发布'
  if (s === 0) return '草稿'
  if (s === 2) return '下架'
  if (s === -1) return '已删除'
  return '未知'
}

const setNoteStatus = (row, newStatus) => {
  const label = statusLabel(newStatus)
  ElMessageBox.confirm(`确定将笔记设为「${label}」吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
  }).then(() => {
    request.put('/api/note', { ...row, status: newStatus }).then((res) => {
      if (res.code === '200') {
        ElMessage.success('操作成功')
        if (dialogFormVisible.value && form.value?.id === row.id) {
          form.value = { ...form.value, status: newStatus }
        }
        load()
      } else {
        ElMessage.error(res.msg || '操作失败')
      }
    })
  })
}

const handleStatusCommand = (row, cmd) => {
  const newStatus = Number(cmd)
  if (row.status === newStatus) return
  setNoteStatus(row, newStatus)
}

const handleRestoreFromDialog = () => {
  ElMessageBox.confirm('确定将笔记恢复为「已发布」？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
  }).then(() => {
    const body = { ...JSON.parse(JSON.stringify(form.value)), status: 1 }
    request.put('/api/note', body).then((res) => {
      if (res.code === '200') {
        ElMessage.success('已恢复为已发布')
        dialogFormVisible.value = false
        load()
      } else {
        ElMessage.error(res.msg || '操作失败')
      }
    })
  })
}

const reset = () => {
  searchForm.keyword = ''
  searchForm.category = ''
  searchForm.status = ''
  load()
}

const handleSelectionChange = (val) => {
  multipleSelection.value = val
}

const handleSizeChange = (size) => {
  pageSize.value = size
  load()
}

const handleCurrentChange = (current) => {
  pageNum.value = current
  load()
}

const confirmDelete = (id) => {
  ElMessageBox.confirm('确定要删除这条数据吗？（软删）', '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
  }).then(() => {
    del(id)
  })
}

const confirmBatchDelete = () => {
  if (multipleSelection.value.length === 0) {
    ElMessage.warning('请至少选择一条记录')
    return
  }

  ElMessageBox.confirm('确定要批量删除这些数据吗？（软删）', '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
  }).then(() => {
    delBatch()
  })
}

const formatStatus = (status) => statusLabel(status)

const statusTagType = (status) => {
  if (status === 1) return 'success'
  if (status === 2) return 'warning'
  if (status === -1) return 'danger'
  return 'info'
}
</script>

<template>
  <div class="content-container">
    <div class="header-section">
      <el-input
        v-model="searchForm.keyword"
        placeholder="请输入标题或内容关键词"
        class="filter-input"
        :prefix-icon="Search"
        clearable
      />

      <el-select v-model="searchForm.category" placeholder="选择分类" class="filter-select" clearable>
        <el-option
          v-for="item in categoryOptions"
          :key="item.value"
          :label="item.label"
          :value="item.value"
        />
      </el-select>

      <el-select v-model="searchForm.status" placeholder="选择状态" class="filter-select" clearable>
        <el-option
          v-for="item in statusOptions"
          :key="item.value"
          :label="item.label"
          :value="item.value"
        />
      </el-select>

      <el-button class="ml-10" plain type="primary" @click="load">搜索</el-button>
      <el-button plain type="info" @click="reset">重置</el-button>
    </div>

    <div class="toolbar-section">
      <el-button plain type="primary" @click="handleAdd" :icon="Plus">新增</el-button>
      <el-button plain type="danger" @click="confirmBatchDelete" :icon="Delete">批量删除</el-button>
    </div>

    <el-card>
      <el-table :data="tableData" @selection-change="handleSelectionChange" style="width: 100%">
        <el-table-column type="selection" width="60" align="center" />
        <el-table-column prop="id" label="ID" width="80" align="center" />

        <el-table-column prop="title" label="标题" min-width="200" show-overflow-tooltip />

        <el-table-column prop="authorName" label="作者" width="120" />

        <el-table-column prop="category" label="分类" width="100" align="center" />

        <el-table-column label="封面" width="100" align="center">
          <template #default="scope">
            <el-image
              v-if="firstImageUrl(scope.row.imageUrl)"
              :src="firstImageUrl(scope.row.imageUrl)"
              :preview-src-list="imagePreviewList(scope.row.imageUrl)"
              style="width: 60px; height: 60px; border-radius: 4px;"
              fit="cover"
            />
            <span v-else>-</span>
          </template>
        </el-table-column>

        <el-table-column label="数据统计" width="200" align="center">
          <template #default="scope">
            <div style="display: flex; flex-direction: column; gap: 5px;">
              <div style="display: flex; justify-content: space-around;">
                <span>👁️ {{ scope.row.views || 0 }}</span>
                <span>👍 {{ scope.row.likes || 0 }}</span>
              </div>
              <div style="display: flex; justify-content: space-around;">
                <span>⭐ {{ scope.row.collects || 0 }}</span>
                <span>💬 {{ scope.row.comments || 0 }}</span>
              </div>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="状态" width="100" align="center">
          <template #default="scope">
            <el-tag :type="statusTagType(scope.row.status)">
              {{ formatStatus(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="createTime" label="创建时间" width="160" align="center">
          <template #default="scope">
            {{ formatDate(scope.row.createTime) }}
          </template>
        </el-table-column>

        <el-table-column label="操作" width="280" align="center" fixed="right">
          <template #default="scope">
            <el-tooltip content="查看" placement="top" :effect="'light'">
              <el-button circle type="info" :icon="View" @click="handleView(scope.row)" size="small" />
            </el-tooltip>
            <el-tooltip content="编辑" placement="top" :effect="'light'">
              <el-button circle type="primary" :icon="Edit" @click="handleEdit(scope.row)" size="small" />
            </el-tooltip>
            <el-dropdown trigger="click" @command="(cmd) => handleStatusCommand(scope.row, cmd)">
              <el-button circle type="warning" size="small">
                <el-icon><ArrowDown /></el-icon>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu v-if="scope.row.status === -1">
                  <el-dropdown-item command="1">恢复为已发布</el-dropdown-item>
                </el-dropdown-menu>
                <el-dropdown-menu v-else>
                  <el-dropdown-item command="1" :disabled="scope.row.status === 1">设为已发布</el-dropdown-item>
                  <el-dropdown-item command="2" :disabled="scope.row.status === 2">设为下架</el-dropdown-item>
                  <el-dropdown-item command="0" :disabled="scope.row.status === 0">设为草稿</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
            <el-tooltip content="删除(软删)" placement="top" :effect="'light'">
              <el-button
                circle
                type="danger"
                :icon="Delete"
                :disabled="scope.row.status === -1"
                @click="confirmDelete(scope.row.id)"
                size="small"
              />
            </el-tooltip>
          </template>
        </el-table-column>
      </el-table>

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

    <el-dialog v-model="dialogDetailVisible" title="笔记详情" width="60%" destroy-on-close>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="标题" :span="2">{{ detailData.title }}</el-descriptions-item>
        <el-descriptions-item label="作者">{{ detailData.authorName }}</el-descriptions-item>
        <el-descriptions-item label="分类">{{ detailData.category }}</el-descriptions-item>
        <el-descriptions-item label="标签" :span="2">{{ detailData.tags }}</el-descriptions-item>
        <el-descriptions-item label="浏览量">{{ detailData.views || 0 }}</el-descriptions-item>
        <el-descriptions-item label="点赞数">{{ detailData.likes || 0 }}</el-descriptions-item>
        <el-descriptions-item label="收藏数">{{ detailData.collects || 0 }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="statusTagType(detailData.status)">
            {{ formatStatus(detailData.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="创建时间" :span="2">{{ formatDate(detailData.createTime) }}</el-descriptions-item>
        <el-descriptions-item label="内容" :span="2">
          <div style="max-height: 300px; overflow-y: auto;">{{ detailData.content }}</div>
        </el-descriptions-item>
        <el-descriptions-item label="封面图" :span="2">
          <el-image
            v-if="firstImageUrl(detailData.imageUrl)"
            :src="firstImageUrl(detailData.imageUrl)"
            :preview-src-list="imagePreviewList(detailData.imageUrl)"
            style="width: 200px; border-radius: 8px;"
            fit="cover"
          />
          <span v-else>-</span>
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>

    <el-dialog v-model="dialogFormVisible" :title="form.id ? '编辑笔记' : '新增笔记'" width="50%" destroy-on-close center>
      <el-alert
        v-if="isFormDeleted"
        type="warning"
        :closable="false"
        show-icon
        class="form-deleted-alert"
        title="该笔记已软删除"
        description="可修改下方内容后点击「恢复为已发布」；或使用行内下拉「恢复为已发布」。请勿使用「确定」保存（已删除状态不能直接保存）。"
      />
      <el-form :model="form" label-width="110px">
        <el-form-item v-if="isFormNew" label="发布用户ID" required>
          <el-input-number
            v-model="form.userId"
            :min="1"
            :controls="true"
            placeholder="必填，对应前台用户主键"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item v-else label="发布用户ID">
          <span>{{ form.userId ?? '-' }}</span>
        </el-form-item>
        <el-form-item label="标题" required>
          <el-input v-model="form.title" placeholder="请输入标题" />
        </el-form-item>
        <el-form-item label="内容" required>
          <el-input v-model="form.content" type="textarea" :rows="6" placeholder="请输入内容" />
        </el-form-item>
        <el-form-item label="作者">
          <el-input v-model="form.authorName" placeholder="请输入作者名称" />
        </el-form-item>
        <el-form-item label="分类">
          <el-select v-model="form.category" placeholder="请选择分类">
            <el-option label="学习经验" value="学习经验" />
            <el-option label="美食探店" value="美食探店" />
            <el-option label="社团活动" value="社团活动" />
            <el-option label="校园活动" value="校园活动" />
          </el-select>
        </el-form-item>
        <el-form-item label="标签">
          <el-input v-model="form.tags" placeholder="多个标签用逗号分隔" />
        </el-form-item>
        <el-form-item label="封面图URL">
          <el-input v-model="form.imageUrl" placeholder="多图用英文逗号分隔" />
        </el-form-item>
        <el-form-item v-if="isFormDeleted" label="状态">
          <el-tag type="danger">已删除</el-tag>
        </el-form-item>
        <el-form-item v-else label="状态">
          <el-radio-group v-model="form.status">
            <el-radio :label="1">已发布</el-radio>
            <el-radio :label="0">草稿</el-radio>
            <el-radio :label="2">下架</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogFormVisible = false">{{ isFormDeleted ? '关闭' : '取消' }}</el-button>
          <template v-if="isFormDeleted">
            <el-button type="primary" @click="handleRestoreFromDialog">恢复为已发布</el-button>
          </template>
          <template v-else>
            <el-button type="primary" @click="save">确定</el-button>
          </template>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.content-container {
  padding: 20px;
}

.form-deleted-alert {
  margin-bottom: 16px;
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
</style>
