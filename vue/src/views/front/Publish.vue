<template>
  <div class="publish-page">
    <!-- 发布专用顶部栏 - 简洁版 -->
    <div class="publish-header">
      <div class="publish-header-content">
        <div class="header-left">
          <button class="btn-back" @click="router.push('/front/home')">
            <i class="fas fa-arrow-left"></i>
            <span>返回</span>
          </button>
          <span class="publish-toolbar-title">发布笔记</span>
        </div>
        <div class="header-actions">
          <button class="btn-draft" @click="saveDraft">
            <i class="fas fa-save"></i>
            <span>存草稿</span>
          </button>
          <button class="btn-publish-main" @click="publishNote">
            <i class="fas fa-paper-plane"></i>
            <span>发布</span>
          </button>
        </div>
      </div>
    </div>
    
    <!-- 发布内容区 -->
    <main class="publish-content">
      <div class="publish-wrapper">
        <!-- 左侧编辑区 -->
        <div class="publish-editor">
          <div class="editor-card">
            <div
              v-if="violationNotice.visible"
              ref="violationAlertRef"
              class="violation-alert-card"
              role="alert"
            >
              <div class="violation-alert-header">
                <div class="violation-alert-title">
                  <i class="fas fa-shield-exclamation"></i>
                  <span>{{ violationNotice.title }}</span>
                </div>
                <button class="violation-alert-close" type="button" @click="clearViolationNotice">
                  <i class="fas fa-times"></i>
                </button>
              </div>
              <p class="violation-alert-text">{{ violationNotice.detail }}</p>
              <ul class="violation-alert-list">
                <li><i class="fas fa-check-circle"></i> 请检查标题和正文是否包含敏感或违规表述</li>
                <li><i class="fas fa-check-circle"></i> 可适当调整措辞后再次发布</li>
              </ul>
            </div>

            <!-- 图片上传区 -->
            <div class="image-upload-section">
              <div class="upload-tips">
                <i class="fas fa-images"></i>
                <p>添加图片（最多9张）</p>
                <span>支持 JPG、PNG、GIF，单张不超过10MB</span>
              </div>
              
              <!-- 文字生成图片按钮 -->
              <div class="generate-image-section">
                <button 
                  class="btn-generate-image" 
                  @click="showGenerateDialog = true"
                  :disabled="form.images.length >= 9"
                >
                  <i class="fas fa-magic"></i>
                  <span>文字生成图片</span>
                </button>
              </div>
              
              <div class="image-list">
                <div
                  v-for="(image, index) in form.images"
                  :key="index"
                  class="image-preview-item"
                >
                  <img :src="image.preview" alt="预览图片" />
                  <button class="remove-image" @click="removeImage(index)">
                    <i class="fas fa-times"></i>
                  </button>
                </div>
                
                <div
                  v-if="form.images.length < 9"
                  class="upload-box"
                  @click="triggerFileInput"
                >
                  <i class="fas fa-plus"></i>
                  <span>点击上传</span>
                  <input
                    ref="fileInput"
                    type="file"
                    accept="image/*"
                    multiple
                    hidden
                    @change="handleFileChange"
                  />
                </div>
              </div>
            </div>

            <!-- 标题输入 -->
            <div class="title-input-section">
              <input
                v-model="form.title"
                type="text"
                placeholder="填写标题，让更多人看到（最多20字）"
                maxlength="20"
              />
              <span class="char-count">{{ form.title.length }}/20</span>
            </div>

            <!-- 内容输入 -->
            <div class="content-input-section">
              <textarea
                v-model="form.content"
                placeholder="分享你的校园生活、学习经验、美食探店...

💡 写作小技巧：
• 用表情符号让内容更生动
• 分段阅读体验更好
• 添加相关话题增加曝光
• 真实分享更能打动人心"
                maxlength="1000"
              ></textarea>
              <span class="char-count">{{ form.content.length }}/1000</span>
            </div>

            <!-- 话题标签 -->
            <div class="tag-section">
              <div class="section-title">
                <i class="fas fa-hashtag"></i>
                <span>添加话题</span>
              </div>
              <div class="tag-input-box">
                <input
                  v-model="tagInput"
                  type="text"
                  placeholder="输入话题，按回车添加"
                  @keypress.enter="addTag"
                />
                <button class="btn-add-tag" @click="addTag">
                  <i class="fas fa-plus"></i>
                </button>
              </div>
              <div class="selected-tags">
                <span
                  v-for="(tag, index) in form.tags"
                  :key="index"
                  class="selected-tag"
                >
                  #{{ tag }}
                  <button class="remove-tag" @click="removeTag(index)">
                    <i class="fas fa-times"></i>
                  </button>
                </span>
              </div>
              <div class="hot-tags">
                <span class="hot-tag-label">热门话题：</span>
                <span
                  v-for="hotTag in hotTags"
                  :key="hotTag"
                  class="hot-tag"
                  @click="addHotTag(hotTag)"
                >
                  #{{ hotTag }}
                </span>
              </div>
            </div>

            <!-- 分类选择 -->
            <div class="category-section">
              <div class="section-title">
                <i class="fas fa-list"></i>
                <span>选择分类</span>
              </div>
              <div class="category-grid">
                <label
                  v-for="cat in categories"
                  :key="cat.value"
                  class="category-option"
                >
                  <input
                    v-model="form.category"
                    type="radio"
                    :value="cat.value"
                  />
                  <div class="category-card">
                    <i :class="cat.icon"></i>
                    <span>{{ cat.label }}</span>
                  </div>
                </label>
              </div>
            </div>

            <div class="club-section">
              <div class="section-title">
                <i class="fas fa-users"></i>
                <span>关联社团（可选）</span>
              </div>
              <div class="club-select-box">
                <el-select
                  v-model="form.clubId"
                  clearable
                  placeholder="不关联社团（默认）"
                  :loading="clubsLoading"
                  style="width: 100%"
                >
                  <el-option label="不关联社团" :value="null" />
                  <el-option
                    v-for="club in myClubs"
                    :key="club.clubId"
                    :label="club.clubName"
                    :value="club.clubId"
                  />
                </el-select>
                <div v-if="!clubsLoading && myClubs.length === 0" class="club-empty-tip">
                  当前未加入社团，发布后将作为普通笔记展示
                </div>
              </div>
            </div>

            <!-- AI 辅助功能 -->
            <div class="ai-section">
              <div class="section-title">
                <i class="fas fa-magic"></i>
                <span>AI 智能助手</span>
                <span class="beta-badge">Beta</span>
              </div>
              <div class="ai-tools">
                <button class="ai-tool-btn" @click="optimizeContent" :disabled="optimizingCopy">
                  <i class="fas fa-wand-magic-sparkles"></i>
                  <span>{{ optimizingCopy ? '优化中...' : '优化文案' }}</span>
                </button>
                <button class="ai-tool-btn" @click="showTemplateDialog = true">
                  <i class="fas fa-file-alt"></i>
                  <span>使用模板</span>
                </button>
              </div>
            </div>
          </div>
        </div>

        <!-- 右侧预览区 -->
        <aside class="publish-preview">
          <div class="preview-card">
            <div class="preview-header">
              <i class="fas fa-mobile-alt"></i>
              <span>预览效果</span>
            </div>
            
            <div class="preview-phone">
              <div class="phone-screen">
                <div class="preview-images">
                  <div v-if="form.images.length === 0" class="preview-placeholder">
                    <i class="fas fa-image"></i>
                    <p>图片预览</p>
                  </div>
                  <div v-else-if="form.images.length > 1" class="preview-carousel">
                    <div class="preview-carousel-wrapper" :style="{ transform: `translateX(-${previewIndex * 100}%)` }">
                      <div v-for="(img, index) in form.images" :key="index" class="preview-carousel-item">
                        <img :src="img.preview" alt="预览" />
                      </div>
                    </div>
                    <div class="preview-indicators">
                      <span 
                        v-for="(img, index) in form.images"
                        :key="index"
                        class="preview-indicator"
                        :class="{ active: previewIndex === index }"
                        @click="previewIndex = index"
                      ></span>
                    </div>
                  </div>
                  <img v-else :src="form.images[0].preview" alt="预览" />
                </div>
                <div class="preview-content">
                  <h3 class="preview-title-text">
                    {{ form.title || '标题将在这里显示' }}
                  </h3>
                  <p class="preview-content-text">
                    {{ form.content || '内容将在这里显示...' }}
                  </p>
                  <div class="preview-tags">
                    <span
                      v-for="tag in form.tags"
                      :key="tag"
                      class="preview-tag"
                    >
                      #{{ tag }}
                    </span>
                  </div>
                </div>
                <div class="preview-author">
                  <img :src="account.avatarUrl || defaultAvatar" alt="头像" />
                  <span>{{ account.nickname || account.username || '你的昵称' }}</span>
                </div>
              </div>
            </div>
          </div>

          <div class="tips-card">
            <div class="tips-header">
              <i class="fas fa-lightbulb"></i>
              <span>发布小贴士</span>
            </div>
            <ul class="tips-list">
              <li><i class="fas fa-check-circle"></i> 使用清晰的图片，突出重点</li>
              <li><i class="fas fa-check-circle"></i> 标题简洁有力，吸引眼球</li>
              <li><i class="fas fa-check-circle"></i> 内容真实有用，分享干货</li>
              <li><i class="fas fa-check-circle"></i> 添加相关话题，增加曝光</li>
              <li><i class="fas fa-check-circle"></i> 遵守社区规范，文明发言</li>
            </ul>
          </div>
        </aside>
      </div>
    </main>

    <!-- 模板选择对话框 -->
    <el-dialog
      v-model="showTemplateDialog"
      title="选择内容模板"
      width="600px"
      :close-on-click-modal="false"
    >
      <div class="template-list">
        <div
          v-for="template in templates"
          :key="template.type"
          class="template-item"
          @click="applyTemplate(template)"
        >
          <i :class="template.icon"></i>
          <h4>{{ template.title }}</h4>
          <p>{{ template.desc }}</p>
        </div>
      </div>
    </el-dialog>

    <!-- AI优化预览对话框 -->
    <el-dialog
      v-model="showOptimizePreview"
      title="文案优化预览"
      width="860px"
      :close-on-click-modal="false"
      @close="cancelOptimizedCopy"
    >
      <div class="optimize-preview-dialog">
        <div class="optimize-preview-grid">
          <div class="optimize-preview-panel">
            <div class="optimize-panel-header">
              <span>原文</span>
            </div>
            <div class="optimize-field">
              <p class="optimize-label">标题</p>
              <p class="optimize-title">
                {{ optimizeOriginal.title || '未填写标题' }}
              </p>
            </div>
            <div class="optimize-field">
              <p class="optimize-label">内容</p>
              <p class="optimize-content">
                {{ optimizeOriginal.content || '未填写内容' }}
              </p>
            </div>
          </div>
          <div class="optimize-preview-panel optimized">
            <div class="optimize-panel-header">
              <span>优化后</span>
              <span v-if="hasTitleDiff || hasContentDiff" class="optimize-badge">已优化</span>
            </div>
            <div class="optimize-field">
              <p class="optimize-label">标题</p>
              <p class="optimize-title">
                {{ optimizeCandidate.optimizedTitle || optimizeOriginal.title || '未填写标题' }}
              </p>
            </div>
            <div class="optimize-field">
              <p class="optimize-label">内容</p>
              <p class="optimize-content">
                {{ optimizeCandidate.optimizedContent || '未返回优化内容' }}
              </p>
            </div>
          </div>
        </div>

        <div v-if="optimizeCandidate.highlights.length" class="optimize-highlights">
          <p class="optimize-highlights-title">优化说明</p>
          <ul>
            <li v-for="(item, index) in optimizeCandidate.highlights" :key="index">
              {{ item }}
            </li>
          </ul>
        </div>
      </div>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="cancelOptimizedCopy">暂不应用</el-button>
          <el-button type="primary" @click="applyOptimizedCopy">应用优化</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 文字生成图片对话框 -->
    <el-dialog
      v-model="showGenerateDialog"
      title="文字生成图片"
      width="700px"
      :close-on-click-modal="false"
    >
      <div class="generate-image-dialog">
        <div class="generate-config">
          <div class="config-item">
            <label>图片文字：</label>
            <input 
              v-model="generateText" 
              type="text" 
              placeholder="输入要显示的文字（最多20字）"
              maxlength="20"
              @input="previewGeneratedImage"
            />
            <span class="char-count">{{ generateText.length }}/20</span>
          </div>
          <div class="config-item">
            <label>背景颜色：</label>
            <div class="color-picker">
              <input 
                type="color" 
                v-model="backgroundColor"
                @change="previewGeneratedImage"
              />
              <span>{{ backgroundColor }}</span>
            </div>
          </div>
          <div class="config-item">
            <label>文字颜色：</label>
            <div class="color-picker">
              <input 
                type="color" 
                v-model="textColor"
                @change="previewGeneratedImage"
              />
              <span>{{ textColor }}</span>
            </div>
          </div>
        </div>
        
        <!-- 预览区域 -->
        <div class="generated-preview">
          <p class="preview-label">预览效果：</p>
          <canvas ref="previewCanvas" width="400" height="400" style="display: none;"></canvas>
          <div class="preview-container">
            <img 
              v-if="generatedImagePreview" 
              :src="generatedImagePreview" 
              alt="生成的图片预览"
              class="preview-generated-img"
            />
            <div v-else class="preview-placeholder-text">
              <i class="fas fa-image"></i>
              <p>输入文字后显示预览</p>
            </div>
          </div>
        </div>
      </div>
      
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="showGenerateDialog = false">取消</el-button>
          <el-button 
            type="primary" 
            @click="generateAndAddImage"
            :disabled="!generateText.trim() || form.images.length >= 9"
          >
            <i class="fas fa-plus"></i>
            生成并添加
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Reading } from '@element-plus/icons-vue'
import { addNote } from '@/api/note'
import { optimizeCopy } from '@/api/ai'
import { getMyClubs } from '@/api/clubMember'
import { getClubById } from '@/api/club'
import { serverHost } from '../../../config/config.default'

const router = useRouter()
const fileInput = ref(null)
const tagInput = ref('')
const showTemplateDialog = ref(false)
const previewIndex = ref(0)
const myClubs = ref([])
const clubsLoading = ref(false)

// 文字生成图片相关变量
const showGenerateDialog = ref(false)
const generateText = ref('')
const backgroundColor = ref('#2563eb')
const textColor = ref('#ffffff')
const generatedImagePreview = ref('')
const previewCanvas = ref(null)
const violationAlertRef = ref(null)
const optimizingCopy = ref(false)
const showOptimizePreview = ref(false)

const optimizeOriginal = reactive({
  title: '',
  content: ''
})

const optimizeCandidate = reactive({
  optimizedTitle: '',
  optimizedContent: '',
  highlights: []
})

const violationNotice = reactive({
  visible: false,
  title: '内容提醒',
  detail: ''
})

const hasTitleDiff = computed(() => {
  const newTitle = (optimizeCandidate.optimizedTitle || '').trim()
  return !!newTitle && newTitle !== (optimizeOriginal.title || '')
})

const hasContentDiff = computed(() => {
  return (optimizeCandidate.optimizedContent || '') !== (optimizeOriginal.content || '')
})

// 获取当前登录用户信息
const account = JSON.parse(localStorage.getItem('account') || '{}')
console.log('Publish页面 - account数据:', account) // 调试
console.log('nickname:', account.nickname, 'username:', account.username) // 调试
const defaultAvatar = 'https://api.dicebear.com/7.x/initials/svg?seed=User&backgroundColor=2563eb'

// 表单数据
const form = reactive({
  title: '',
  content: '',
  images: [],
  tags: [],
  category: '',
  status: 1, // 1-已发布 0-草稿 2-下架（前台一般由个人中心/详情操作）
  clubId: null,
  userId: account.id || null,
  authorName: account.nickname || account.username || '匿名用户',
  authorAvatar: account.avatarUrl || defaultAvatar
})

// 热门标签
const hotTags = ref([
  '新生指南', '考研组队', '社团招新', '美食探店', '校园美景'
])

// 分类列表
const categories = [
  { value: '学习经验', label: '学习经验', icon: 'fas fa-graduation-cap' },
  { value: '社团活动', label: '社团活动', icon: 'fas fa-users' },
  { value: '校园活动', label: '校园活动', icon: 'fas fa-calendar-alt' },
  { value: '二手市场', label: '二手市场', icon: 'fas fa-shopping-bag' },
  { value: '美食探店', label: '美食探店', icon: 'fas fa-utensils' },
  { value: '运动健身', label: '运动健身', icon: 'fas fa-dumbbell' },
  { value: '校园风光', label: '校园风光', icon: 'fas fa-camera' },
  { value: '宿舍生活', label: '宿舍生活', icon: 'fas fa-home' },
  { value: '其他', label: '其他', icon: 'fas fa-ellipsis-h' }
]

// 模板列表
const templates = [
  {
    type: 'study',
    icon: 'fas fa-graduation-cap',
    title: '学习经验分享',
    desc: '分享学习心得、备考经验等',
    content: {
      title: '高效学习方法分享 📚',
      content: '📖 学习心得分享\n\n今天给大家分享一些我的学习方法和经验...\n\n💡 核心要点：\n1. 制定合理的学习计划\n2. 保持专注和效率\n3. 及时复习巩固\n\n希望对大家有帮助！'
    }
  },
  {
    type: 'food',
    icon: 'fas fa-utensils',
    title: '美食探店',
    desc: '推荐好吃的餐厅、食堂',
    content: {
      title: '校园美食探店 🍜',
      content: '🍽️ 今日探店\n\n店铺名称：\n地址位置：\n人均消费：\n\n✨ 推荐菜品：\n\n📝 总体评价：\n味道、环境、服务都很不错，值得一试！'
    }
  },
  {
    type: 'activity',
    icon: 'fas fa-calendar-alt',
    title: '活动通知',
    desc: '发布社团或校园活动信息',
    content: {
      title: '活动通知 📢',
      content: '🎉 活动名称：\n📅 时间：\n📍 地点：\n\n活动内容：\n\n报名方式：\n\n期待大家的参与！'
    }
  },
  {
    type: 'secondhand',
    icon: 'fas fa-shopping-bag',
    title: '二手交易',
    desc: '出售闲置物品',
    content: {
      title: '闲置物品出售 🛍️',
      content: '📦 物品名称：\n💰 价格：\n📍 交易地点：\n\n物品描述：\n• 购买时间：\n• 使用情况：\n• 出售原因：\n\n联系方式：\n\n诚心出售，欢迎联系！'
    }
  }
]

// 触发文件选择
const triggerFileInput = () => {
  fileInput.value.click()
}

const clearViolationNotice = () => {
  violationNotice.visible = false
  violationNotice.title = '内容提醒'
  violationNotice.detail = ''
}

const showViolationNotice = (message) => {
  const detail = message && message !== '发布失败'
    ? message
    : '系统检测到当前内容可能涉及违规，请调整后重新发布。'

  violationNotice.visible = true
  violationNotice.title = '内容未通过发布校验'
  violationNotice.detail = detail

  nextTick(() => {
    violationAlertRef.value?.scrollIntoView({
      behavior: 'smooth',
      block: 'center'
    })
  })
}

// 处理文件选择 - 只做预览，不上传（发布时才上传）
const handleFileChange = (event) => {
  const files = Array.from(event.target.files)
  
  files.forEach(file => {
    if (form.images.length >= 9) {
      ElMessage.warning('最多只能上传9张图片')
      return
    }
    
    if (file.size > 5 * 1024 * 1024) {
      ElMessage.error('图片大小不能超过5MB')
      return
    }
    
    if (!file.type.startsWith('image/')) {
      ElMessage.warning('只能上传图片文件')
      return
    }
    
    const reader = new FileReader()
    reader.onload = (e) => {
      // 存储文件对象和预览Base64
      form.images.push({
        file: file,              // 原始文件，发布时上传
        preview: e.target.result // Base64，用于预览
      })
    }
    reader.readAsDataURL(file)
  })
  
  event.target.value = ''
}

// 删除图片
const removeImage = (index) => {
  form.images.splice(index, 1)
}

// 实时预览生成的图片
const previewGeneratedImage = () => {
  if (!generateText.value.trim()) {
    generatedImagePreview.value = ''
    return
  }
  
  const canvas = previewCanvas.value
  if (!canvas) return
  
  const ctx = canvas.getContext('2d')
  const width = 400
  const height = 400
  
  // 清空画布
  ctx.clearRect(0, 0, width, height)
  
  // 绘制背景
  ctx.fillStyle = backgroundColor.value
  ctx.fillRect(0, 0, width, height)
  
  // 绘制文字
  ctx.fillStyle = textColor.value
  ctx.textAlign = 'center'
  ctx.textBaseline = 'middle'
  
  // 计算合适的字体大小
  const text = generateText.value.trim()
  const maxWidth = width - 40
  const minFontSize = 24
  const maxFontSize = 48
  let fontSize = maxFontSize
  
  // 先尝试最大字体
  ctx.font = `bold ${fontSize}px Arial, sans-serif`
  const metrics = ctx.measureText(text)
  
  // 如果文字太宽，减小字体
  if (metrics.width > maxWidth) {
    fontSize = Math.max(minFontSize, (maxWidth / metrics.width) * fontSize)
    ctx.font = `bold ${fontSize}px Arial, sans-serif`
  }
  
  // 处理文字换行
  const words = text.split('')
  const lines = []
  let currentLine = ''
  
  for (let i = 0; i < words.length; i++) {
    const testLine = currentLine + words[i]
    const testMetrics = ctx.measureText(testLine)
    
    if (testMetrics.width > maxWidth && currentLine !== '') {
      lines.push(currentLine)
      currentLine = words[i]
    } else {
      currentLine = testLine
    }
  }
  if (currentLine) {
    lines.push(currentLine)
  }
  
  // 绘制多行文字（居中）
  const lineHeight = fontSize * 1.2
  const totalHeight = lines.length * lineHeight
  const startY = (height - totalHeight) / 2 + lineHeight / 2
  
  lines.forEach((line, index) => {
    ctx.fillText(line, width / 2, startY + index * lineHeight)
  })
  
  // 转换为预览图片
  canvas.toBlob((blob) => {
    const reader = new FileReader()
    reader.onload = (e) => {
      generatedImagePreview.value = e.target.result
    }
    reader.readAsDataURL(blob)
  }, 'image/png', 0.95)
}

// 文字生成图片并添加到列表
const generateAndAddImage = () => {
  if (!generateText.value.trim()) {
    ElMessage.warning('请输入要显示的文字')
    return
  }
  
  if (form.images.length >= 9) {
    ElMessage.warning('最多只能添加9张图片')
    return
  }
  
  const canvas = previewCanvas.value
  if (!canvas) return
  
  // 使用预览Canvas生成最终图片
  canvas.toBlob((blob) => {
    const file = new File([blob], `generated-${Date.now()}.png`, { type: 'image/png' })
    
    const reader = new FileReader()
    reader.onload = (e) => {
      // 添加到图片列表
      form.images.push({
        file: file,
        preview: e.target.result,
        isGenerated: true
      })
      
      // 重置对话框
      generateText.value = ''
      generatedImagePreview.value = ''
      backgroundColor.value = '#2563eb'
      textColor.value = '#ffffff'
      showGenerateDialog.value = false
      
      ElMessage.success('图片已生成并添加')
    }
    reader.readAsDataURL(blob)
  }, 'image/png', 0.95)
}

// 添加标签
const addTag = () => {
  const tag = tagInput.value.trim().replace(/^#/, '')
  if (!tag) return
  
  if (form.tags.length >= 5) {
    ElMessage.warning('最多只能添加5个话题')
    return
  }
  
  if (form.tags.includes(tag)) {
    ElMessage.warning('该话题已添加')
    return
  }
  
  form.tags.push(tag)
  tagInput.value = ''
}

// 添加热门标签
const addHotTag = (tag) => {
  if (form.tags.length >= 5) {
    ElMessage.warning('最多只能添加5个话题')
    return
  }
  
  if (form.tags.includes(tag)) {
    ElMessage.warning('该话题已添加')
    return
  }
  
  form.tags.push(tag)
}

// 删除标签
const removeTag = (index) => {
  form.tags.splice(index, 1)
}

// 应用模板
const applyTemplate = (template) => {
  form.title = template.content.title
  form.content = template.content.content
  showTemplateDialog.value = false
  ElMessage.success('模板已应用')
}

// AI优化文案
const resetOptimizePreview = () => {
  showOptimizePreview.value = false
  optimizeOriginal.title = ''
  optimizeOriginal.content = ''
  optimizeCandidate.optimizedTitle = ''
  optimizeCandidate.optimizedContent = ''
  optimizeCandidate.highlights = []
}

const applyOptimizedCopy = () => {
  const nextTitle = optimizeCandidate.optimizedTitle || ''
  const nextContent = optimizeCandidate.optimizedContent || ''

  if (!nextContent.trim()) {
    ElMessage.warning('优化结果为空，无法应用')
    return
  }

  if (hasTitleDiff.value) {
    form.title = nextTitle
  }
  if (hasContentDiff.value) {
    form.content = nextContent
  }

  resetOptimizePreview()
  ElMessage.success('文案优化已应用')
}

const cancelOptimizedCopy = () => {
  resetOptimizePreview()
}

const optimizeContent = async () => {
  if (!form.content || !form.content.trim()) {
    ElMessage.warning('请先输入内容')
    return
  }

  if (optimizingCopy.value) {
    return
  }

  optimizingCopy.value = true
  resetOptimizePreview()
  const loading = ElMessage({
    message: 'AI正在优化文案...',
    duration: 0,
    type: 'info'
  })
  try {
    const res = await optimizeCopy({
      title: form.title,
      content: form.content,
      style: 'strong'
    })
    loading.close()
    if (res.code !== 200 && res.code !== '200') {
      ElMessage.error(res.msg || '文案优化失败，请稍后重试')
      return
    }

    const optimized = res.data || {}
    const optimizedContent = optimized.optimizedContent || ''
    const optimizedTitle = optimized.optimizedTitle || ''
    if (!optimizedContent.trim()) {
      ElMessage.warning('AI 未返回有效优化结果')
      return
    }

    const titleChanged = optimizedTitle && optimizedTitle !== form.title
    const contentChanged = optimizedContent !== form.content
    if (!titleChanged && !contentChanged) {
      ElMessage.success('文案已是较优表达，无需调整')
      return
    }

    optimizeOriginal.title = form.title || ''
    optimizeOriginal.content = form.content || ''
    optimizeCandidate.optimizedTitle = optimizedTitle || ''
    optimizeCandidate.optimizedContent = optimizedContent
    optimizeCandidate.highlights = Array.isArray(optimized.highlights) ? optimized.highlights : []
    showOptimizePreview.value = true
  } catch (error) {
    loading.close()
    if (error?.code === 'ECONNABORTED' || String(error?.message || '').includes('timeout')) {
      ElMessage.error('请求超时，请稍后重试')
    } else {
      ElMessage.error(error?.msg || error?.message || '文案优化失败，请稍后重试')
    }
  } finally {
    optimizingCopy.value = false
  }
}

const loadMyClubs = async () => {
  clubsLoading.value = true
  try {
    const res = await getMyClubs()
    if (res.code !== 200 && res.code !== '200') {
      myClubs.value = []
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
        // ignore single club resolve failure
      }
      enriched.push({ ...m, clubName })
    }
    myClubs.value = enriched
  } catch (error) {
    console.error('加载我的社团失败:', error)
    myClubs.value = []
  } finally {
    clubsLoading.value = false
  }
}

// 保存草稿 - 只保存预览Base64，不保存File对象
const saveDraft = () => {
  const draft = {
    title: form.title,
    content: form.content,
    images: form.images.map(img => img.preview), // 只保存预览
    tags: form.tags,
    category: form.category,
    clubId: form.clubId,
    time: new Date().toISOString()
  }
  localStorage.setItem('noteDraft', JSON.stringify(draft))
  ElMessage.success('草稿已保存')
}

// 加载草稿
const loadDraft = () => {
  const draft = localStorage.getItem('noteDraft')
  if (!draft) return
  
  try {
    // 先验证草稿数据是否有效
    const data = JSON.parse(draft)
    if (!data || typeof data !== 'object') {
      localStorage.removeItem('noteDraft')
      return
    }
    
    ElMessageBox.confirm('检测到未发布的草稿，是否继续编辑？', '提示', {
      confirmButtonText: '继续编辑',
      cancelButtonText: '放弃草稿',
      type: 'info',
      closeOnClickModal: true,
      distinguishCancelAndClose: true
    }).then(() => {
      // 继续编辑 - 恢复草稿
      // 恢复时 images 只有 preview，没有 file（需要重新选择图片才能发布）
      const restoredImages = (data.images || []).map(preview => ({
        file: null,      // 草稿恢复后没有原始文件
        preview: preview
      }))
      
      Object.assign(form, {
        title: data.title || '',
        content: data.content || '',
        images: restoredImages,
        tags: data.tags || [],
        category: data.category || '',
        clubId: data.clubId ?? null
      })
      
      if (restoredImages.length > 0) {
        ElMessage.info('草稿中的图片需要重新选择才能发布')
      }
    }).catch(() => {
      // 放弃草稿或关闭弹窗
      localStorage.removeItem('noteDraft')
    })
  } catch (e) {
    // JSON 解析失败，清除无效草稿
    console.error('草稿数据无效:', e)
    localStorage.removeItem('noteDraft')
  }
}

// 发布笔记
const publishNote = async () => {
  clearViolationNotice()

  // 验证必填项
  if (form.images.length === 0) {
    ElMessage.warning('请至少上传一张图片或生成一张图片')
    return
  }
  
  if (!form.title.trim()) {
    ElMessage.warning('请填写标题')
    return
  }
  
  if (!form.content.trim()) {
    ElMessage.warning('请填写内容')
    return
  }
  
  if (!form.category) {
    ElMessage.warning('请选择分类')
    return
  }
  
  const loading = ElMessage({
    message: '正在上传图片...',
    duration: 0,
    type: 'info'
  })
  
  try {
    // ========== 第一步：上传所有图片 ==========
    const imageUrls = []
    
    console.log('开始上传，图片数量:', form.images.length) // 调试
    console.log('图片数据:', form.images) // 调试
    
    for (let i = 0; i < form.images.length; i++) {
      const img = form.images[i]
      
      console.log(`处理第${i + 1}张图片:`, img) // 调试
      console.log(`img.file 是否存在:`, !!img.file) // 调试
      
      // 如果没有 file（草稿恢复的情况），跳过上传使用占位图
      if (!img.file) {
        const placeholderUrl = `https://picsum.photos/seed/${Date.now() + i}/400/500`
        console.log('使用占位图:', placeholderUrl) // 调试
        imageUrls.push(placeholderUrl)
        continue
      }
      
      const formData = new FormData()
      formData.append('file', img.file)
      
      // 使用相对路径，通过Nginx代理访问后端
      const uploadUrl = serverHost ? `${serverHost}/api/file/upload` : '/api/file/upload'
      const res = await fetch(uploadUrl, {
        method: 'POST',
        headers: {
          'token': account.token || ''
        },
        body: formData
      })
      
      const result = await res.json()
      console.log('上传返回结果:', result) // 调试日志
      
      if (result.code === '200' || result.code === 200) {
        // 兼容处理：URL 可能在 data 或 msg 中
        const url = result.data || result.msg
        console.log('上传成功，URL:', url) // 调试日志
        imageUrls.push(url)
      } else {
        loading.close()
        ElMessage.error(`第${i + 1}张图片上传失败: ${result.msg}`)
        return
      }
    }
    
    // ========== 第二步：提交笔记数据 ==========
    loading.message = '正在发布...'
    
    const data = {
      title: form.title,
      content: form.content,
      imageUrl: imageUrls.join(','),  // 多图用逗号分隔
      tags: form.tags.join(','),
      category: form.category,
      clubId: form.clubId || null,
      status: 1, // 新发布为已上架；下架用 status=2
      userId: account.id || form.userId,
      authorName: account.nickname || account.username || form.authorName || '匿名用户',
      authorAvatar: account.avatarUrl || form.authorAvatar || defaultAvatar
    }
    
    console.log('提交的数据:', data) // 调试日志
    console.log('图片URL列表:', imageUrls) // 调试日志
    
    const res = await addNote(data)
    loading.close()
    
    if (res.code === 200 || res.code === '200') {
      clearViolationNotice()
      localStorage.removeItem('noteDraft')
      
      ElMessageBox.confirm('发布成功！', '提示', {
        confirmButtonText: '返回首页',
        cancelButtonText: '继续发布',
        type: 'success',
        customClass: 'publish-success-dialog',
        appendTo: 'body'
      }).then(() => {
        router.push('/front/home')
      }).catch(() => {
        // 重置表单
        Object.assign(form, {
          title: '',
          content: '',
          images: [],
          tags: [],
          category: '',
          clubId: null
        })
      })
    } else {
      const message = res.msg || '发布失败'
      if (message.includes('违规') || message.includes('审核')) {
        showViolationNotice(message)
        ElMessage.warning('内容可能涉及违规，请按页面提示修改后重试')
      } else {
        ElMessage.error(message)
      }
    }
  } catch (error) {
    loading.close()
    console.error('发布失败:', error)
    const message = error?.response?.data?.msg || error?.message || '发布失败，请重试'
    if (message.includes('违规') || message.includes('审核')) {
      showViolationNotice(message)
      ElMessage.warning('内容可能涉及违规，请按页面提示修改后重试')
      return
    }
    ElMessage.error('发布失败，请重试')
  }
}

// 监听图片变化，重置预览索引
watch(() => form.images.length, () => {
  previewIndex.value = 0
})

watch(
  [
    () => form.title,
    () => form.content,
    () => form.category,
    () => form.tags.join(','),
    () => form.images.length
  ],
  () => {
    if (violationNotice.visible) {
      clearViolationNotice()
    }
  }
)

// 监听对话框打开，初始化预览
watch(showGenerateDialog, (newVal) => {
  if (newVal) {
    // 对话框打开时，如果有文字则立即预览
    if (generateText.value.trim()) {
      // 使用setTimeout确保Canvas已渲染
      setTimeout(() => {
        previewGeneratedImage()
      }, 100)
    }
  } else {
    // 对话框关闭时，如果未文字生成图片则重置表单
    if (!generatedImagePreview.value) {
      generateText.value = ''
      backgroundColor.value = '#2563eb'
      textColor.value = '#ffffff'
    }
  }
})

onMounted(() => {
  loadMyClubs()
  loadDraft()
})
</script>

<style scoped>
@import url('https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css');

/* 发布页局部变量（对齐前台蓝色令牌） */
.publish-page {
  --primary-color: var(--bb-brand);
  --secondary-color: #3b82f6;
  --primary-dark: var(--bb-brand-hover);
  --success-color: #4caf50;
  --text-primary: var(--bb-text-primary);
  --text-secondary: var(--bb-text-secondary);
  --text-light: var(--bb-text-muted);
  --background-color: var(--bb-bg-page);
  --card-background: var(--bb-bg-card);
  --border-color: var(--bb-border);
  --hover-color: var(--bb-brand-soft);
  --shadow-sm: var(--bb-shadow-card);
  --shadow-md: 0 4px 16px rgba(37, 99, 235, 0.12);
}

/* 嵌入 Front.vue 的 main-content：不占满视口，不盖住全局顶栏 */
.publish-page {
  width: 100%;
  min-height: calc(100vh - 90px);
  background: var(--background-color);
  box-sizing: border-box;
}

/* 发布页工具栏：滚动时贴在 Front 固定顶栏下方（顶栏高度 90px） */
.publish-header {
  position: sticky;
  top: 90px;
  background: white;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  z-index: 100;
}

.publish-header-content {
  max-width: 1400px;
  margin: 0 auto;
  padding: 15px 40px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 20px;
}

.btn-back {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  border: none;
  background: transparent;
  color: var(--text-secondary);
  font-size: 20px;
  cursor: pointer;
  border-radius: 8px;
  transition: all 0.3s;
  font-weight: 500;
}

.btn-back:hover {
  background: var(--hover-color);
  color: var(--primary-color);
}

.publish-toolbar-title {
  font-size: 20px;
  font-weight: 600;
  color: var(--text-primary);
  user-select: none;
}

.header-actions {
  display: flex;
  gap: 12px;
}

/* 发布内容区 */
.publish-content {
  padding: 30px 40px 50px;
}

.btn-draft,
.btn-publish-main {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 24px;
  border: none;
  border-radius: 20px;
  font-size: 18px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s;
}

.btn-draft {
  background: white;
  color: var(--text-secondary);
  border: 2px solid var(--border-color);
}

.btn-draft:hover {
  background: var(--hover-color);
  color: var(--primary-color);
  border-color: var(--primary-color);
}

.btn-publish-main {
  background: linear-gradient(135deg, #1e88e5, #42a5f5);
  color: white;
  box-shadow: 0 4px 12px rgba(30, 136, 229, 0.3);
}

.btn-publish-main:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(30, 136, 229, 0.4);
}

.publish-wrapper {
  display: grid;
  grid-template-columns: 1fr 400px;
  gap: 30px;
  max-width: 1400px;
  margin: 0 auto;
}

/* 左侧编辑区 */
.editor-card {
  background: var(--card-background);
  border-radius: 16px;
  padding: 30px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
}

/* 图片上传区 */
.image-upload-section {
  margin-bottom: 30px;
}

.upload-tips {
  text-align: center;
  padding: 30px;
  background: linear-gradient(135deg, var(--bb-brand-soft) 0%, #e3f2fd 100%);
  border-radius: 12px;
  margin-bottom: 20px;
}

.upload-tips i {
  font-size: 48px;
  color: var(--primary-color);
  margin-bottom: 15px;
  display: block;
}

.upload-tips p {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0 0 5px 0;
}

.upload-tips span {
  font-size: 13px;
  color: var(--text-light);
}

/* 文字生成图片区域 */
.generate-image-section {
  margin-bottom: 20px;
  text-align: center;
}

.btn-generate-image {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 12px 24px;
  background: linear-gradient(135deg, var(--bb-brand) 0%, var(--bb-brand-hover) 100%);
  color: white;
  border: none;
  border-radius: 20px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s;
  box-shadow: 0 2px 8px rgba(102, 126, 234, 0.3);
}

.btn-generate-image:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
}

.btn-generate-image:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.btn-generate-image i {
  font-size: 16px;
}

.image-list {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 15px;
}

.upload-box {
  aspect-ratio: 1;
  border: 2px dashed var(--border-color);
  border-radius: 12px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.3s;
  background: white;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.upload-box:hover {
  border-color: var(--primary-color);
  background: white;
  box-shadow: 0 4px 12px rgba(30, 136, 229, 0.15);
  transform: translateY(-2px);
}

.upload-box i {
  font-size: 32px;
  color: var(--primary-color);
  margin-bottom: 10px;
}

.upload-box span {
  font-size: 13px;
  color: var(--text-secondary);
}

.image-preview-item {
  position: relative;
  aspect-ratio: 1;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  transition: all 0.3s;
}

.image-preview-item:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.12);
  transform: translateY(-2px);
}

.image-preview-item img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.remove-image {
  position: absolute;
  top: 8px;
  right: 8px;
  width: 28px;
  height: 28px;
  border-radius: 50%;
  background: rgba(0, 0, 0, 0.6);
  color: white;
  border: none;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s;
}

.remove-image:hover {
  background: rgba(0, 0, 0, 0.8);
  transform: scale(1.1);
}

/* 标题输入 */
.title-input-section {
  position: relative;
  margin-bottom: 25px;
}

.title-input-section input {
  width: 100%;
  padding: 15px 60px 15px 15px;
  border: 1px solid var(--border-color);
  border-radius: 8px;
  font-size: 16px;
  font-weight: 600;
  outline: none;
  transition: all 0.3s;
  box-sizing: border-box;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
}

.title-input-section input:focus {
  border-color: var(--primary-color);
  box-shadow: 0 0 0 3px rgba(30, 136, 229, 0.1), 0 2px 8px rgba(0, 0, 0, 0.08);
}

.char-count {
  position: absolute;
  right: 15px;
  top: 50%;
  transform: translateY(-50%);
  font-size: 12px;
  color: var(--text-light);
}

/* 内容输入 */
.content-input-section {
  position: relative;
  margin-bottom: 30px;
}

.content-input-section textarea {
  width: 100%;
  min-height: 300px;
  padding: 15px;
  border: 1px solid var(--border-color);
  border-radius: 8px;
  font-size: 15px;
  line-height: 1.8;
  outline: none;
  resize: vertical;
  font-family: inherit;
  transition: all 0.3s;
  box-sizing: border-box;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
}

.content-input-section textarea:focus {
  border-color: var(--primary-color);
  box-shadow: 0 0 0 3px rgba(30, 136, 229, 0.1), 0 2px 8px rgba(0, 0, 0, 0.08);
}

.content-input-section .char-count {
  position: absolute;
  right: 15px;
  bottom: 15px;
  transform: none;
}

/* 标签、分类、AI区域 */
.tag-section,
.category-section,
.club-section,
.ai-section {
  padding: 25px;
  background: #fafbfc;
  border-radius: 12px;
  margin-bottom: 20px;
  border: 1px solid rgba(0, 0, 0, 0.04);
}

.section-title {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 15px;
}

.section-title i {
  color: var(--primary-color);
  font-size: 18px;
}

.beta-badge {
  padding: 2px 8px;
  background: linear-gradient(135deg, var(--primary-color), var(--secondary-color));
  color: white;
  border-radius: 10px;
  font-size: 11px;
  font-weight: 600;
  margin-left: auto;
}

.tag-input-box {
  display: flex;
  gap: 10px;
  margin-bottom: 15px;
}

.tag-input-box input {
  flex: 1;
  padding: 10px 15px;
  border: 1px solid var(--border-color);
  border-radius: 20px;
  font-size: 14px;
  outline: none;
  transition: all 0.3s;
}

.tag-input-box input:focus {
  border-color: var(--primary-color);
}

.btn-add-tag {
  width: 40px;
  height: 40px;
  border: none;
  background: linear-gradient(135deg, var(--primary-color), var(--secondary-color));
  color: white;
  border-radius: 50%;
  cursor: pointer;
  transition: all 0.3s;
  box-shadow: 0 2px 8px rgba(30, 136, 229, 0.3);
}

.btn-add-tag:hover {
  background: linear-gradient(135deg, var(--primary-dark), var(--primary-color));
  transform: rotate(90deg);
  box-shadow: 0 4px 12px rgba(30, 136, 229, 0.4);
}

.selected-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-bottom: 15px;
  min-height: 40px;
}

.selected-tag {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 6px 12px;
  background: linear-gradient(135deg, var(--primary-color), var(--secondary-color));
  color: white;
  border-radius: 15px;
  font-size: 13px;
  box-shadow: 0 2px 6px rgba(30, 136, 229, 0.3);
}

.remove-tag {
  background: none;
  border: none;
  color: white;
  cursor: pointer;
  padding: 0;
  width: 16px;
  height: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  transition: background 0.3s;
}

.remove-tag:hover {
  background: rgba(255, 255, 255, 0.2);
}

.hot-tags {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 8px;
}

.hot-tag-label {
  font-size: 12px;
  color: var(--text-light);
}

.hot-tag {
  display: inline-block;
  padding: 5px 12px;
  background: white;
  color: var(--primary-color);
  border: 1px solid var(--border-color);
  border-radius: 12px;
  font-size: 12px;
  cursor: pointer;
  transition: all 0.3s;
}

.hot-tag:hover {
  border-color: var(--primary-color);
  background: var(--hover-color);
}

/* 分类选择 */
.category-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
}

.category-option {
  cursor: pointer;
}

.category-option input {
  display: none;
}

.category-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 15px;
  background: white;
  border: 2px solid var(--border-color);
  border-radius: 12px;
  transition: all 0.3s;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.04);
}

.category-card i {
  font-size: 24px;
  color: var(--primary-color);
}

.category-card span {
  font-size: 13px;
  color: var(--text-secondary);
}

.category-option input:checked + .category-card {
  border-color: var(--primary-color);
  background: linear-gradient(135deg, var(--bb-brand-soft) 0%, #e3f2fd 100%);
  box-shadow: 0 4px 12px rgba(30, 136, 229, 0.15);
}

.category-card:hover {
  border-color: var(--primary-color);
  transform: translateY(-3px);
  box-shadow: 0 6px 16px rgba(30, 136, 229, 0.12);
}

/* 社团关联 */
.club-select-box {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.club-empty-tip {
  font-size: 12px;
  color: var(--text-light);
}

/* AI工具 */
.ai-tools {
  display: flex;
  gap: 12px;
}

.ai-tool-btn {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 15px;
  border: 1px solid var(--border-color);
  background: white;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.04);
}

.ai-tool-btn:hover {
  border-color: var(--primary-color);
  background: white;
  transform: translateY(-3px);
  box-shadow: 0 6px 16px rgba(30, 136, 229, 0.12);
}

.ai-tool-btn i {
  font-size: 24px;
  color: var(--primary-color);
}

.ai-tool-btn span {
  font-size: 13px;
  color: var(--text-secondary);
}

/* 右侧预览区 */
.publish-preview {
  position: sticky;
  top: 100px;
  height: fit-content;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.preview-card,
.tips-card {
  background: var(--card-background);
  border-radius: 16px;
  padding: 20px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
}

.preview-header,
.tips-header {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 15px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 15px;
  padding-bottom: 15px;
  border-bottom: 1px solid var(--border-color);
}

.preview-header i,
.tips-header i {
  color: var(--primary-color);
}

/* 手机预览 */
.preview-phone {
  background: linear-gradient(135deg, var(--bb-brand) 0%, var(--bb-brand-hover) 100%);
  border-radius: 30px;
  padding: 40px 20px;
}

.phone-screen {
  background: white;
  border-radius: 20px;
  overflow: hidden;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.3);
}

.preview-images {
  aspect-ratio: 1;
  background: var(--background-color);
  display: flex;
  align-items: center;
  justify-content: center;
}

.preview-placeholder {
  text-align: center;
  color: var(--text-light);
}

.preview-placeholder i {
  font-size: 48px;
  margin-bottom: 10px;
  display: block;
}

.preview-placeholder p {
  margin: 0;
}

.preview-images img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

/* 预览轮播 */
.preview-carousel {
  width: 100%;
  height: 100%;
  position: relative;
  overflow: hidden;
}

.preview-carousel-wrapper {
  display: flex;
  width: 100%;
  height: 100%;
  transition: transform 0.3s ease;
}

.preview-carousel-item {
  flex: 0 0 100%;
  width: 100%;
  height: 100%;
}

.preview-carousel-item img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.preview-indicators {
  position: absolute;
  bottom: 10px;
  left: 50%;
  transform: translateX(-50%);
  display: flex;
  gap: 6px;
  z-index: 10;
}

.preview-indicator {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.5);
  cursor: pointer;
  transition: all 0.3s;
}

.preview-indicator.active {
  background: white;
  width: 20px;
  border-radius: 3px;
}

.preview-indicator:hover {
  background: rgba(255, 255, 255, 0.8);
}

.preview-content {
  padding: 15px;
}

.preview-title-text {
  font-size: 15px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0 0 10px 0;
  min-height: 20px;
}

.preview-content-text {
  font-size: 13px;
  color: var(--text-secondary);
  line-height: 1.6;
  margin: 0 0 10px 0;
  min-height: 40px;
  display: -webkit-box;
  line-clamp: 3;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.preview-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 5px;
  min-height: 20px;
}

.preview-tag {
  display: inline-block;
  padding: 3px 8px;
  background: linear-gradient(135deg, var(--bb-brand-soft), #e3f2fd);
  color: var(--primary-color);
  border-radius: 10px;
  font-size: 11px;
  font-weight: 600;
}

.preview-author {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 15px;
  border-top: 1px solid var(--border-color);
}

.preview-author img {
  width: 30px;
  height: 30px;
  border-radius: 50%;
}

.preview-author span {
  font-size: 13px;
  color: var(--text-secondary);
}

/* 贴士列表 */
.tips-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.tips-list li {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  padding: 10px 0;
  font-size: 13px;
  color: var(--text-secondary);
  line-height: 1.6;
}

.tips-list i {
  color: var(--success-color);
  margin-top: 3px;
}

.violation-alert-card {
  margin-bottom: 20px;
  padding: 18px 20px;
  border-radius: 16px;
  border: 1px solid rgba(245, 158, 11, 0.28);
  background: linear-gradient(135deg, #fff7ed 0%, #fffbeb 100%);
  box-shadow: 0 8px 24px rgba(245, 158, 11, 0.12);
}

.violation-alert-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 10px;
}

.violation-alert-title {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 15px;
  font-weight: 700;
  color: #c2410c;
}

.violation-alert-title i {
  color: #ea580c;
}

.violation-alert-close {
  width: 28px;
  height: 28px;
  border: none;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.72);
  color: #9a3412;
  cursor: pointer;
  transition: all 0.2s ease;
}

.violation-alert-close:hover {
  background: rgba(255, 255, 255, 0.95);
  transform: rotate(90deg);
}

.violation-alert-text {
  margin: 0 0 10px;
  font-size: 14px;
  line-height: 1.7;
  color: #7c2d12;
}

.violation-alert-list {
  list-style: none;
  padding: 0;
  margin: 0;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.violation-alert-list li {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  font-size: 13px;
  line-height: 1.6;
  color: #9a3412;
}

.violation-alert-list i {
  margin-top: 3px;
  color: #f59e0b;
}

/* 模板列表 */
.template-list {
  display: grid;
  gap: 15px;
}

.template-item {
  padding: 20px;
  border: 2px solid var(--border-color);
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s;
}

.template-item:hover {
  border-color: var(--primary-color);
  background: var(--hover-color);
  transform: translateY(-2px);
}

.template-item i {
  font-size: 32px;
  color: var(--primary-color);
  margin-bottom: 10px;
  display: block;
}

.template-item h4 {
  font-size: 16px;
  margin: 0 0 5px 0;
}

.template-item p {
  font-size: 13px;
  color: var(--text-secondary);
  margin: 0;
}

/* 文字生成图片对话框 */
.generate-image-dialog {
  padding: 10px 0;
}

.generate-config {
  display: flex;
  flex-direction: column;
  gap: 20px;
  margin-bottom: 30px;
}

.config-item {
  display: flex;
  align-items: center;
  gap: 15px;
  position: relative;
}

.config-item label {
  min-width: 90px;
  font-size: 14px;
  font-weight: 500;
  color: var(--text-primary);
}

.config-item input[type="text"] {
  flex: 1;
  padding: 10px 15px;
  border: 1px solid var(--border-color);
  border-radius: 8px;
  font-size: 14px;
  outline: none;
  transition: all 0.3s;
}

.config-item input[type="text"]:focus {
  border-color: var(--primary-color);
  box-shadow: 0 0 0 3px rgba(30, 136, 229, 0.1);
}

.config-item .char-count {
  position: absolute;
  right: 15px;
  font-size: 12px;
  color: var(--text-light);
}

.color-picker {
  display: flex;
  align-items: center;
  gap: 10px;
}

.color-picker input[type="color"] {
  width: 60px;
  height: 40px;
  border: 1px solid var(--border-color);
  border-radius: 8px;
  cursor: pointer;
  padding: 0;
}

.color-picker span {
  font-size: 13px;
  color: var(--text-secondary);
  font-family: monospace;
  min-width: 80px;
}

.generated-preview {
  text-align: center;
  padding: 20px;
  background: #f8f9fa;
  border-radius: 12px;
}

.preview-label {
  margin: 0 0 15px 0;
  font-size: 14px;
  font-weight: 500;
  color: var(--text-primary);
}

.preview-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 200px;
  background: white;
  border-radius: 8px;
  padding: 20px;
}

.preview-generated-img {
  max-width: 100%;
  max-height: 300px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.preview-placeholder-text {
  color: var(--text-light);
  text-align: center;
}

.preview-placeholder-text i {
  font-size: 48px;
  margin-bottom: 10px;
  display: block;
  color: var(--text-light);
}

.preview-placeholder-text p {
  margin: 0;
  font-size: 14px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

/* AI优化预览 */
.optimize-preview-dialog {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.optimize-preview-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 14px;
}

.optimize-preview-panel {
  border: 1px solid var(--border-color);
  border-radius: 12px;
  background: #fafbfc;
  padding: 14px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.optimize-preview-panel.optimized {
  border-color: rgba(30, 136, 229, 0.3);
  background: #f4f8ff;
}

.optimize-panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 14px;
  font-weight: 600;
  color: var(--text-primary);
}

.optimize-badge {
  padding: 2px 8px;
  border-radius: 999px;
  font-size: 11px;
  color: white;
  background: linear-gradient(135deg, var(--primary-color), var(--secondary-color));
}

.optimize-field {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.optimize-label {
  margin: 0;
  font-size: 12px;
  color: var(--text-light);
}

.optimize-title,
.optimize-content {
  margin: 0;
  border: 1px solid var(--border-color);
  border-radius: 8px;
  background: white;
  padding: 10px 12px;
  white-space: pre-wrap;
  line-height: 1.65;
  color: var(--text-secondary);
}

.optimize-title {
  min-height: 44px;
  font-weight: 600;
  color: var(--text-primary);
}

.optimize-content {
  min-height: 180px;
  max-height: 260px;
  overflow: auto;
}

.optimize-highlights {
  border: 1px solid var(--border-color);
  border-radius: 10px;
  background: #fff;
  padding: 12px;
}

.optimize-highlights-title {
  margin: 0 0 8px;
  font-size: 13px;
  font-weight: 600;
  color: var(--text-primary);
}

.optimize-highlights ul {
  margin: 0;
  padding-left: 18px;
}

.optimize-highlights li {
  margin: 0 0 4px;
  color: var(--text-secondary);
  line-height: 1.6;
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .publish-wrapper {
    grid-template-columns: 1fr 350px;
  }
  
  .category-grid {
    grid-template-columns: repeat(3, 1fr);
  }
}

@media (max-width: 992px) {
  .publish-wrapper {
    grid-template-columns: 1fr;
  }
  
  .publish-preview {
    position: static;
  }
}

@media (max-width: 768px) {
  .publish-header {
    padding: 0 20px;
  }
  
  .publish-content {
    padding: 20px;
  }
  
  .editor-card {
    padding: 20px;
  }
  
  .image-list {
    grid-template-columns: repeat(3, 1fr);
  }
  
  .category-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .ai-tools {
    flex-direction: column;
  }
  
  .btn-draft span,
  .btn-publish-main span {
    display: none;
  }

  .optimize-preview-grid {
    grid-template-columns: 1fr;
  }
}
</style>

<style>
/* 发布成功弹窗样式 - 全局样式确保层级正确 */
.publish-success-dialog {
  z-index: 2001 !important;
}
.el-overlay {
  z-index: 2000 !important;
}
</style>
