<script setup>
import { ref, reactive} from 'vue'
import { Search, Delete, View} from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '../../utils/request'
import { formatDate } from '@/utils/date'

// 表格数据
const tableData = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)

// 搜索条件
const searchForm = reactive({
  keyword: '',
  noteId: '',
})

// 详情数据
const detailData = ref({})
const dialogDetailVisible = ref(false)
const multipleSelection = ref([])

// 加载所有评论
const load = () => {
  // 因为CommentController没有分页查询接口，我们需要先获取所有笔记，然后获取评论
  // 这里简化处理，直接请求所有评论
  request.get("/api/note/page", {
    params: {
      pageNum: 1,
      pageSize: 1000, // 获取所有笔记
    }
  }).then(noteRes => {
    if (noteRes.data && noteRes.data.records) {
      const allComments = []
      const promises = noteRes.data.records.map(note => 
        request.get(`/api/comment/note/${note.id}`)
          .then(commentRes => {
            if (commentRes.data && commentRes.data.length > 0) {
              commentRes.data.forEach(comment => {
                allComments.push({
                  ...comment,
                  noteTitle: note.title,
                  noteId: note.id
                })
              })
            }
          })
          .catch(() => {})
      )
      
      Promise.all(promises).then(() => {
        // 根据搜索条件过滤
        let filteredComments = allComments
        if (searchForm.keyword) {
          filteredComments = filteredComments.filter(c => 
            c.content?.includes(searchForm.keyword) || 
            c.noteTitle?.includes(searchForm.keyword)
          )
        }
        if (searchForm.noteId) {
          filteredComments = filteredComments.filter(c => c.noteId == searchForm.noteId)
        }

        // 排序（按创建时间倒序）
        filteredComments.sort((a, b) => new Date(b.createTime) - new Date(a.createTime))
        
        // 手动分页
        total.value = filteredComments.length
        const start = (pageNum.value - 1) * pageSize.value
        const end = start + pageSize.value
        tableData.value = filteredComments.slice(start, end)
      })
    }
  })
}
load()

// 查看详情
const handleView = (row) => {
  detailData.value = row
  dialogDetailVisible.value = true
}

// 删除
const del = (id) => {
  request.delete("/api/comment/" + id).then(res => {
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
  const promises = ids.map(id => request.delete("/api/comment/" + id))
  
  Promise.all(promises).then(() => {
    ElMessage.success("批量删除成功")
    load()
  }).catch(() => {
    ElMessage.error("批量删除失败")
  })
}

// 重置搜索
const reset = () => {
  searchForm.keyword = ""
  searchForm.noteId = ""
  pageNum.value = 1
  load()
}

// 表格选择变化
const handleSelectionChange = (val) => {
  multipleSelection.value = val
}

// 分页大小变化
const handleSizeChange = (size) => {
  pageSize.value = size
  pageNum.value = 1
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
      '确定要删除这条评论吗？删除后将无法恢复！',
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
      '确定要批量删除这些评论吗？',
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

// 格式化日期

// 跳转到笔记详情
const goToNote = (noteId) => {
  window.open(`/front/note/${noteId}`, '_blank')
}

</script>

<template>
  <div class="content-container">

    <!-- 搜索区域 -->
    <div class="header-section">
      <el-input 
        v-model="searchForm.keyword" 
        placeholder="请输入评论内容或笔记标题" 
        class="filter-input" 
        :prefix-icon="Search" 
        clearable
      />
      
      <el-input 
        v-model="searchForm.noteId" 
        placeholder="笔记ID" 
        class="filter-select" 
        clearable
        type="number"
      />

      <el-button class="ml-10" plain type="primary" @click="load">搜索</el-button>
      <el-button plain type="info" @click="reset">重置</el-button>
    </div>

    <!-- 操作按钮区域 -->
    <div class="toolbar-section">
      <el-button plain type="danger" @click="confirmBatchDelete" :icon="Delete">批量删除</el-button>
      <el-alert 
        title="提示：删除评论将无法恢复，请谨慎操作！" 
        type="warning" 
        :closable="false"
        style="flex: 1; margin-left: 20px;"
      />
    </div>

    <!-- 表格区域 -->
    <el-card>
      <el-table :data="tableData" @selection-change="handleSelectionChange" style="width: 100%">
        <el-table-column type="selection" width="60" align="center" />
        <el-table-column prop="id" label="ID" width="80" align="center" />
        
        <el-table-column label="所属笔记" min-width="200">
          <template #default="scope">
            <el-link 
              type="primary" 
              @click="goToNote(scope.row.noteId)"
              :underline="false"
            >
              {{ scope.row.noteTitle }}
            </el-link>
            <el-tag size="small" style="margin-left: 10px;">ID: {{ scope.row.noteId }}</el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="content" label="评论内容" min-width="300" show-overflow-tooltip />
        
        <el-table-column prop="userId" label="用户ID" width="100" align="center" />

        <el-table-column prop="parentId" label="父评论ID" width="120" align="center">
          <template #default="scope">
            <el-tag v-if="scope.row.parentId" type="info">{{ scope.row.parentId }}</el-tag>
            <span v-else>-</span>
          </template>
        </el-table-column>

        <el-table-column prop="likes" label="点赞数" width="100" align="center">
          <template #default="scope">
            <el-tag type="success">👍 {{ scope.row.likes || 0 }}</el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="createTime" label="评论时间" width="160" align="center">
          <template #default="scope">
            {{ formatDate(scope.row.createTime) }}
          </template>
        </el-table-column>

        <el-table-column label="操作" width="150" align="center" fixed="right">
          <template #default="scope">
            <el-tooltip content="查看详情" placement="top" :effect="'light'">
              <el-button circle type="info" :icon="View" @click="handleView(scope.row)" size="small"/>
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

    <!-- 详情对话框 -->
    <el-dialog v-model="dialogDetailVisible" title="评论详情" width="50%" destroy-on-close>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="评论ID">{{ detailData.id }}</el-descriptions-item>
        <el-descriptions-item label="用户ID">{{ detailData.userId }}</el-descriptions-item>
        <el-descriptions-item label="笔记ID">
          <el-link type="primary" @click="goToNote(detailData.noteId)">
            {{ detailData.noteId }}
          </el-link>
        </el-descriptions-item>
        <el-descriptions-item label="点赞数">{{ detailData.likes || 0 }}</el-descriptions-item>
        <el-descriptions-item label="笔记标题" :span="2">{{ detailData.noteTitle }}</el-descriptions-item>
        <el-descriptions-item label="父评论ID" :span="2">
          <el-tag v-if="detailData.parentId" type="info">{{ detailData.parentId }}</el-tag>
          <span v-else>无（主评论）</span>
        </el-descriptions-item>
        <el-descriptions-item label="评论时间" :span="2">{{ formatDate(detailData.createTime) }}</el-descriptions-item>
        <el-descriptions-item label="评论内容" :span="2">
          <div style="max-height: 300px; overflow-y: auto; white-space: pre-wrap;">
            {{ detailData.content }}
          </div>
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<style scoped>
.content-container {
  padding: 20px;
}

.header-section {
  display: flex;
  gap: 10px;
  margin-bottom: 20px;
}

.filter-input {
  width: 350px;
}

.filter-select {
  width: 120px;
}

.toolbar-section {
  margin-bottom: 20px;
  display: flex;
  align-items: center;
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


