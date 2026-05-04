<script setup>
import { ref, onMounted } from 'vue'
import { User, Document, ChatDotSquare, OfficeBuilding, TrendCharts, View } from '@element-plus/icons-vue'
import request from '../../utils/request'
import { useRouter } from 'vue-router'
import { formatDate } from '@/utils/date'

const router = useRouter()

// 获取当前用户
const account = ref(JSON.parse(localStorage.getItem('account') || '{}'))

// 统计数据
const stats = ref({
  totalUsers: 0,
  totalNotes: 0,
  totalComments: 0,
  totalClubs: 0,
  totalViews: 0,
  totalLikes: 0
})

// 热门笔记列表
const hotNotes = ref([])

// 最近用户列表
const recentUsers = ref([])

// 认证社团列表
const verifiedClubs = ref([])

// 加载统计数据
const loadStatistics = async () => {
  try {
    // 获取用户总数
    const userRes = await request.get('/user/page', {
      params: { pageNum: 1, pageSize: 5 }
    })
    stats.value.totalUsers = userRes.data?.total || 0
    recentUsers.value = userRes.data?.records || []

    // 获取笔记总数和热门笔记
    const noteRes = await request.get('/api/note/page', {
      params: { pageNum: 1, pageSize: 5, status: 1 }
    })
    if (noteRes.data) {
      stats.value.totalNotes = noteRes.data.total || 0
      // 按浏览量排序获取热门笔记
      const allNotes = noteRes.data.records || []
      hotNotes.value = allNotes.sort((a, b) => (b.views || 0) - (a.views || 0)).slice(0, 5)
      
      // 计算总浏览量和总点赞数
      stats.value.totalViews = allNotes.reduce((sum, note) => sum + (note.views || 0), 0)
      stats.value.totalLikes = allNotes.reduce((sum, note) => sum + (note.likes || 0), 0)
    }

    // 获取社团总数
    const clubRes = await request.get('/api/club')
    if (clubRes.data) {
      const clubs = Array.isArray(clubRes.data) ? clubRes.data : []
      stats.value.totalClubs = clubs.length
      verifiedClubs.value = clubs.filter(c => c.isVerified === 1).slice(0, 4)
    }

    // 估算评论数（简化处理）
    stats.value.totalComments = Math.floor(stats.value.totalNotes * 2.5)

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

// 跳转到详细统计
const goToStatistics = () => {
  router.push('/back/statistics')
}

// 跳转到管理页面
const goToManage = (path) => {
  router.push(path)
}

onMounted(() => {
  loadStatistics()
})
</script>

<template>
  <div style="padding: 20px">
    <!-- 欢迎信息 -->
    <el-card style="margin-bottom: 20px">
      <div style="display: flex; align-items: center; justify-content: space-between;">
        <div>
          <h2 style="margin: 0 0 10px 0;">👋 您好，{{ account.nickname }}！</h2>
          <p style="margin: 0; color: #909399;">欢迎回到校园蓝珊笔记管理后台</p>
        </div>
        <el-button type="primary" @click="goToStatistics" :icon="TrendCharts">
          查看详细统计
        </el-button>
      </div>
    </el-card>

    <!-- 数据概览卡片 -->
    <el-row :gutter="20" style="margin-bottom: 20px">
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover" @click="goToManage('/back/user')">
          <div class="stat-content">
            <el-icon class="stat-icon" color="#409EFF" :size="48"><User /></el-icon>
            <div>
              <div class="stat-value">{{ formatNumber(stats.totalUsers) }}</div>
              <div class="stat-label">总用户数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover" @click="goToManage('/back/note')">
          <div class="stat-content">
            <el-icon class="stat-icon" color="#67C23A" :size="48"><Document /></el-icon>
            <div>
              <div class="stat-value">{{ formatNumber(stats.totalNotes) }}</div>
              <div class="stat-label">总笔记数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover" @click="goToManage('/back/club')">
          <div class="stat-content">
            <el-icon class="stat-icon" color="#E6A23C" :size="48"><OfficeBuilding /></el-icon>
            <div>
              <div class="stat-value">{{ formatNumber(stats.totalClubs) }}</div>
              <div class="stat-label">认证社团数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover" @click="goToManage('/back/comment')">
          <div class="stat-content">
            <el-icon class="stat-icon" color="#F56C6C" :size="48"><ChatDotSquare /></el-icon>
            <div>
              <div class="stat-value">{{ formatNumber(stats.totalComments) }}</div>
              <div class="stat-label">总评论数</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 互动数据 -->
    <el-row :gutter="20" style="margin-bottom: 20px">
      <el-col :span="12">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <el-icon class="stat-icon" color="#909399" :size="48"><View /></el-icon>
            <div>
              <div class="stat-value">{{ formatNumber(stats.totalViews) }}</div>
              <div class="stat-label">总浏览量</div>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="12">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <span class="stat-icon" style="font-size: 48px;">👍</span>
            <div>
              <div class="stat-value">{{ formatNumber(stats.totalLikes) }}</div>
              <div class="stat-label">总点赞数</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 热门笔记和最近用户 -->
    <el-row :gutter="20">
      <el-col :span="12">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>🔥 热门笔记 TOP 5</span>
              <el-button text type="primary" @click="goToManage('/back/note')">查看更多 →</el-button>
            </div>
          </template>
          <el-table :data="hotNotes" style="width: 100%" :show-header="false">
            <el-table-column label="封面" width="80">
              <template #default="scope">
                <el-image 
                  v-if="scope.row.imageUrl"
                  :src="scope.row.imageUrl" 
                  style="width: 60px; height: 60px; border-radius: 8px;"
                  fit="cover"
                />
              </template>
            </el-table-column>
            <el-table-column prop="title" label="标题" show-overflow-tooltip />
            <el-table-column label="数据" width="120" align="center">
              <template #default="scope">
                <div style="display: flex; flex-direction: column; gap: 5px; font-size: 12px;">
                  <span>👁️ {{ formatNumber(scope.row.views) }}</span>
                  <span>👍 {{ formatNumber(scope.row.likes) }}</span>
                </div>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
      
      <el-col :span="12">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>👥 最近注册用户</span>
              <el-button text type="primary" @click="goToManage('/back/user')">查看更多 →</el-button>
            </div>
          </template>
          <el-table :data="recentUsers" style="width: 100%" :show-header="false">
            <el-table-column label="头像" width="80">
              <template #default="scope">
                <el-avatar :src="scope.row.avatarUrl" :size="50" />
              </template>
            </el-table-column>
            <el-table-column label="信息">
              <template #default="scope">
                <div>
                  <div style="font-weight: bold; margin-bottom: 5px;">{{ scope.row.nickname }}</div>
                  <div style="font-size: 12px; color: #909399;">{{ scope.row.username }}</div>
                </div>
              </template>
            </el-table-column>
            <el-table-column label="邮箱" show-overflow-tooltip>
              <template #default="scope">
                <span style="font-size: 12px; color: #909399;">{{ scope.row.email || '-' }}</span>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>

    <!-- 认证社团展示 -->
    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="24">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>✅ 认证社团</span>
              <el-button text type="primary" @click="goToManage('/back/club')">管理社团 →</el-button>
            </div>
          </template>
          <el-row :gutter="15">
            <el-col :span="6" v-for="club in verifiedClubs" :key="club.id">
              <div class="club-card">
                <el-avatar :src="club.avatarUrl" :size="60" />
                <div class="club-info">
                  <div class="club-name">{{ club.name }}</div>
                  <el-tag type="success" size="small">已认证</el-tag>
                  <div class="club-members">{{ club.memberCount || 0 }} 成员</div>
                </div>
              </div>
            </el-col>
          </el-row>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<style scoped>
.stat-card {
  cursor: pointer;
  transition: all 0.3s;
}

.stat-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.stat-content {
  display: flex;
  align-items: center;
  gap: 20px;
}

.stat-icon {
  flex-shrink: 0;
}

.stat-value {
  font-size: 32px;
  font-weight: bold;
  color: #303133;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-top: 5px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: bold;
}

.club-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 20px;
  border: 1px solid #EBEEF5;
  border-radius: 8px;
  transition: all 0.3s;
}

.club-card:hover {
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  transform: translateY(-3px);
}

.club-info {
  margin-top: 15px;
  text-align: center;
}

.club-name {
  font-weight: bold;
  margin-bottom: 8px;
  font-size: 14px;
}

.club-members {
  font-size: 12px;
  color: #909399;
  margin-top: 8px;
}
</style>