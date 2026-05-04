<script setup>
import { ref, onMounted } from 'vue'
import { TrendCharts, User, Document, ChatDotSquare, View } from '@element-plus/icons-vue'
import request from '../../utils/request'
import { formatDate } from '@/utils/date'

// 统计数据
const stats = ref({
  totalUsers: 0,
  totalNotes: 0,
  totalComments: 0,
  totalViews: 0,
  totalLikes: 0,
  totalCollects: 0,
})

// 分类统计
const categoryStats = ref([])

// 热门笔记
const hotNotes = ref([])

// 热门社团
const hotClubs = ref([])

// 加载统计数据
const loadStatistics = async () => {
  try {
    // 获取用户总数
    const userRes = await request.get('/user/page', {
      params: { pageNum: 1, pageSize: 1 }
    })
    stats.value.totalUsers = userRes.data?.total || 0

    // 获取笔记数据和统计
    const noteRes = await request.get('/api/note/page', {
      params: { pageNum: 1, pageSize: 1000, status: 1 }
    })
    if (noteRes.data && noteRes.data.records) {
      const notes = noteRes.data.records
      stats.value.totalNotes = notes.length
      
      // 计算总浏览量、点赞数、收藏数
      stats.value.totalViews = notes.reduce((sum, note) => sum + (note.views || 0), 0)
      stats.value.totalLikes = notes.reduce((sum, note) => sum + (note.likes || 0), 0)
      stats.value.totalCollects = notes.reduce((sum, note) => sum + (note.collects || 0), 0)
      
      // 统计分类数据
      const categoryMap = {}
      notes.forEach(note => {
        const category = note.category || '未分类'
        if (!categoryMap[category]) {
          categoryMap[category] = { name: category, count: 0, views: 0, likes: 0 }
        }
        categoryMap[category].count++
        categoryMap[category].views += note.views || 0
        categoryMap[category].likes += note.likes || 0
      })
      categoryStats.value = Object.values(categoryMap).sort((a, b) => b.count - a.count)
      
      // 获取热门笔记 TOP 10
      hotNotes.value = notes
        .sort((a, b) => (b.views || 0) - (a.views || 0))
        .slice(0, 10)
    }

    // 获取社团数据
    const clubRes = await request.get('/api/club')
    if (clubRes.data) {
      hotClubs.value = Array.isArray(clubRes.data) 
        ? clubRes.data.sort((a, b) => (b.memberCount || 0) - (a.memberCount || 0)).slice(0, 5)
        : []
    }

    // 统计评论数（遍历所有笔记的评论）
    let commentCount = 0
    if (noteRes.data && noteRes.data.records) {
      const promises = noteRes.data.records.map(note => 
        request.get(`/api/comment/note/${note.id}`)
          .then(res => {
            if (res.data) {
              commentCount += res.data.length
            }
          })
          .catch(() => {})
      )
      await Promise.all(promises)
      stats.value.totalComments = commentCount
    }

  } catch (error) {
    console.error('加载统计数据失败:', error)
  }
}

// 格式化数字
const formatNumber = (num) => {
  if (num >= 10000) {
    return (num / 10000).toFixed(1) + 'w'
  }
  if (num >= 1000) {
    return (num / 1000).toFixed(1) + 'k'
  }
  return num
}

// 格式化日期

onMounted(() => {
  loadStatistics()
})
</script>

<template>
  <div style="padding: 20px">
    <!-- 顶部统计卡片 -->
    <el-row :gutter="20" style="margin-bottom: 20px">
      <el-col :span="8">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon-wrapper" style="background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);">
              <el-icon class="stat-icon" color="#fff"><User /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ formatNumber(stats.totalUsers) }}</div>
              <div class="stat-label">总用户数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="8">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon-wrapper" style="background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);">
              <el-icon class="stat-icon" color="#fff"><Document /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ formatNumber(stats.totalNotes) }}</div>
              <div class="stat-label">总笔记数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="8">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon-wrapper" style="background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);">
              <el-icon class="stat-icon" color="#fff"><ChatDotSquare /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ formatNumber(stats.totalComments) }}</div>
              <div class="stat-label">总评论数</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 互动数据统计 -->
    <el-row :gutter="20" style="margin-bottom: 20px">
      <el-col :span="8">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon-wrapper" style="background: linear-gradient(135deg, #fa709a 0%, #fee140 100%);">
              <el-icon class="stat-icon" color="#fff"><View /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ formatNumber(stats.totalViews) }}</div>
              <div class="stat-label">总浏览量</div>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="8">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon-wrapper" style="background: linear-gradient(135deg, #30cfd0 0%, #330867 100%);">
              <span class="stat-icon" style="font-size: 28px;">👍</span>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ formatNumber(stats.totalLikes) }}</div>
              <div class="stat-label">总点赞数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="8">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon-wrapper" style="background: linear-gradient(135deg, #a8edea 0%, #fed6e3 100%);">
              <span class="stat-icon" style="font-size: 28px;">⭐</span>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ formatNumber(stats.totalCollects) }}</div>
              <div class="stat-label">总收藏数</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 分类统计和热门数据 -->
    <el-row :gutter="20" style="margin-bottom: 20px">
      <el-col :span="12">
        <el-card>
          <template #header>
            <div class="card-header">
              <span><el-icon><TrendCharts /></el-icon> 内容分类统计</span>
            </div>
          </template>
          <el-table :data="categoryStats" style="width: 100%" max-height="400">
            <el-table-column prop="name" label="分类" width="120" />
            <el-table-column prop="count" label="笔记数" align="center">
              <template #default="scope">
                <el-tag type="primary">{{ scope.row.count }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="views" label="总浏览" align="center">
              <template #default="scope">
                <span>{{ formatNumber(scope.row.views) }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="likes" label="总点赞" align="center">
              <template #default="scope">
                <span>{{ formatNumber(scope.row.likes) }}</span>
              </template>
            </el-table-column>
            <el-table-column label="占比" align="center">
              <template #default="scope">
                <el-progress 
                  :percentage="Math.round((scope.row.count / stats.totalNotes) * 100)" 
                  :stroke-width="8"
                />
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
      
      <el-col :span="12">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>🏆 热门社团 TOP 5</span>
            </div>
          </template>
          <el-table :data="hotClubs" style="width: 100%">
            <el-table-column type="index" label="排名" width="80" align="center">
              <template #default="scope">
                <el-tag 
                  :type="scope.$index === 0 ? 'danger' : scope.$index === 1 ? 'warning' : 'info'"
                  effect="dark"
                >
                  {{ scope.$index + 1 }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="社团">
              <template #default="scope">
                <div style="display: flex; align-items: center; gap: 10px;">
                  <el-avatar :src="scope.row.avatarUrl" :size="40" />
                  <div>
                    <div>{{ scope.row.name }}</div>
                    <el-tag v-if="scope.row.isVerified === 1" type="success" size="small">已认证</el-tag>
                  </div>
                </div>
              </template>
            </el-table-column>
            <el-table-column prop="category" label="分类" width="100" align="center" />
            <el-table-column label="成员数" width="100" align="center">
              <template #default="scope">
                <el-tag type="info">{{ scope.row.memberCount || 0 }} 人</el-tag>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>

    <!-- 热门笔记 -->
    <el-row :gutter="20">
      <el-col :span="24">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>🔥 热门笔记 TOP 10</span>
            </div>
          </template>
          <el-table :data="hotNotes" style="width: 100%">
            <el-table-column type="index" label="排名" width="80" align="center">
              <template #default="scope">
                <el-tag 
                  :type="scope.$index < 3 ? 'danger' : scope.$index < 5 ? 'warning' : 'info'"
                  effect="dark"
                >
                  {{ scope.$index + 1 }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="封面" width="100" align="center">
              <template #default="scope">
                <el-image 
                  v-if="scope.row.imageUrl"
                  :src="scope.row.imageUrl" 
                  style="width: 60px; height: 60px; border-radius: 8px;"
                  fit="cover"
                />
              </template>
            </el-table-column>
            <el-table-column prop="title" label="标题" min-width="250" show-overflow-tooltip />
            <el-table-column prop="authorName" label="作者" width="120" />
            <el-table-column prop="category" label="分类" width="100" align="center" />
            <el-table-column label="浏览量" width="120" align="center" sortable>
              <template #default="scope">
                <el-tag type="danger">👁️ {{ formatNumber(scope.row.views) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="点赞数" width="120" align="center" sortable>
              <template #default="scope">
                <el-tag type="warning">👍 {{ formatNumber(scope.row.likes) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="收藏数" width="120" align="center" sortable>
              <template #default="scope">
                <el-tag type="success">⭐ {{ formatNumber(scope.row.collects) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createTime" label="发布时间" width="120" align="center">
              <template #default="scope">
                {{ formatDate(scope.row.createTime) }}
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<style scoped>
.stat-card {
  cursor: pointer;
  transition: all 0.3s;
  border-radius: 12px;
}

.stat-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
}

.stat-content {
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 10px;
}

.stat-icon-wrapper {
  width: 64px;
  height: 64px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.stat-icon {
  font-size: 32px;
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 32px;
  font-weight: bold;
  color: #303133;
  line-height: 1;
  margin-bottom: 8px;
}

.stat-label {
  font-size: 14px;
  color: #909399;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: bold;
  font-size: 16px;
}
</style>


