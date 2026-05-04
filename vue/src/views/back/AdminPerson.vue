<script setup>
import { ref, reactive } from 'vue'
import { Plus, Check } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { serverHost } from '../../../config/config.default'
import request from '@/utils/request'

// 表单数据
const form = reactive({})

// 用户信息
const account = ref(
    localStorage.getItem('account') ? JSON.parse(localStorage.getItem('account')) : {}
)

// 获取用户信息
const getAccount = () => {
  request.get('/web/userInfo').then(res => {
    if (res.code === '200' && res.data) {
      Object.assign(form, res.data)
    } else {
      ElMessage.error(res.msg)
    }
  })
}
getAccount()

// 定义要发出的事件
const emit = defineEmits(['refreshUser'])

// 保存用户信息
const save = () => {
  request.post('/admin', form).then(res => {
    if (res.code === '200') {
      ElMessage.success('保存成功')

      // 只更新昵称和头像到 account 对象，其他属性保持不变
      if (form.nickname) account.value.nickname = form.nickname
      if (form.avatarUrl) account.value.avatarUrl = form.avatarUrl

      // 更新浏览器存储的用户信息
      localStorage.setItem('account', JSON.stringify(account.value))

      // 向父组件发送更新事件，传递更新后的用户信息
      emit('updateAccount', account.value)

    } else {
      ElMessage.error(res.msg || '保存失败')
    }
  })
}

// 头像上传成功处理
const handleAvatarSuccess = (res) => {
  form.avatarUrl = res
}

</script>

<template>
  <div class="person-container">
    <div class="person-card">
      <!-- 头部装饰背景 -->
      <div class="card-header-bg"></div>
      
      <!-- 头像区域 -->
      <div class="avatar-section">
        <div class="avatar-wrapper">
          <el-upload 
            :action="`${serverHost}/web/upload`" 
            :show-file-list="false" 
            :on-success="handleAvatarSuccess"
            class="avatar-uploader"
          >
            <div class="avatar-container">
              <img v-if="form.avatarUrl" :src="form.avatarUrl" class="avatar">
              <div v-else class="avatar-placeholder">
                <el-icon class="avatar-icon"><Plus /></el-icon>
              </div>
              <div class="avatar-overlay">
                <el-icon class="upload-icon"><Plus /></el-icon>
                <span>更换头像</span>
              </div>
            </div>
          </el-upload>
        </div>
        <h2 class="user-name">{{ form.nickname || form.username || '未设置昵称' }}</h2>
        <p class="user-role">管理员</p>
      </div>

      <!-- 表单区域 -->
      <div class="form-section">
        <el-form label-width="90px" label-position="left">
          <div class="form-group">
            <div class="form-group-title">
              <span class="title-icon">📝</span>
              <span>基本信息</span>
            </div>
            
            <el-form-item label="用户名">
              <el-input 
                v-model="form.username" 
                disabled 
                autocomplete="off"
                prefix-icon="User"
              ></el-input>
            </el-form-item>
            
            <el-form-item label="昵称">
              <el-input 
                v-model="form.nickname" 
                autocomplete="off"
                placeholder="请输入昵称"
                prefix-icon="EditPen"
              ></el-input>
            </el-form-item>
          </div>

          <div class="form-group">
            <div class="form-group-title">
              <span class="title-icon">📧</span>
              <span>联系方式</span>
            </div>
            
            <el-form-item label="邮箱">
              <el-input 
                v-model="form.email" 
                autocomplete="off"
                placeholder="请输入邮箱"
                prefix-icon="Message"
              ></el-input>
            </el-form-item>
            
            <el-form-item label="电话">
              <el-input 
                v-model="form.phone" 
                autocomplete="off"
                placeholder="请输入电话"
                prefix-icon="Phone"
              ></el-input>
            </el-form-item>
          </div>

          <div class="form-actions">
            <el-button type="primary" size="large" @click="save" class="save-btn">
              <el-icon class="btn-icon"><Check /></el-icon>
              保存修改
            </el-button>
          </div>
        </el-form>
      </div>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.person-container {
  min-height: calc(100vh - 60px);
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px 20px;

  .person-card {
    max-width: 700px;
    width: 100%;
    background: white;
    border-radius: 24px;
    box-shadow: 0 20px 60px rgba(0, 0, 0, 0.15);
    overflow: hidden;
    position: relative;
    animation: slideUp 0.5s ease-out;

    // 头部装饰背景
    .card-header-bg {
      height: 180px;
      background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
      position: relative;
      overflow: hidden;

      &::before {
        content: '';
        position: absolute;
        top: -50%;
        left: -50%;
        width: 200%;
        height: 200%;
        background: radial-gradient(circle, rgba(255, 255, 255, 0.1) 1px, transparent 1px);
        background-size: 30px 30px;
        animation: backgroundMove 20s linear infinite;
      }
    }

    // 头像区域
    .avatar-section {
      text-align: center;
      padding: 0 40px 30px;
      margin-top: -80px;
      position: relative;

      .avatar-wrapper {
        display: inline-block;
        margin-bottom: 20px;

        .avatar-uploader {
          cursor: pointer;

          .avatar-container {
            position: relative;
            width: 160px;
            height: 160px;
            border-radius: 50%;
            overflow: hidden;
            border: 6px solid white;
            box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
            transition: all 0.3s ease;

            &:hover {
              transform: translateY(-5px);
              box-shadow: 0 12px 32px rgba(0, 0, 0, 0.2);

              .avatar-overlay {
                opacity: 1;
              }
            }

            .avatar {
              width: 100%;
              height: 100%;
              object-fit: cover;
              display: block;
            }

            .avatar-placeholder {
              width: 100%;
              height: 100%;
              background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
              display: flex;
              align-items: center;
              justify-content: center;

              .avatar-icon {
                font-size: 48px;
                color: white;
              }
            }

            .avatar-overlay {
              position: absolute;
              top: 0;
              left: 0;
              right: 0;
              bottom: 0;
              background: rgba(0, 0, 0, 0.6);
              display: flex;
              flex-direction: column;
              align-items: center;
              justify-content: center;
              opacity: 0;
              transition: opacity 0.3s ease;
              color: white;

              .upload-icon {
                font-size: 32px;
                margin-bottom: 8px;
              }

              span {
                font-size: 14px;
                font-weight: 500;
              }
            }
          }
        }
      }

      .user-name {
        font-size: 28px;
        font-weight: 600;
        color: #2c3e50;
        margin: 0 0 8px 0;
      }

      .user-role {
        font-size: 14px;
        color: #909399;
        margin: 0;
        padding: 6px 20px;
        background: linear-gradient(135deg, #ffecd2 0%, #fcb69f 100%);
        border-radius: 20px;
        display: inline-block;
        font-weight: 500;
      }
    }

    // 表单区域
    .form-section {
      padding: 30px 40px 40px;

      .form-group {
        margin-bottom: 32px;

        &:last-of-type {
          margin-bottom: 0;
        }

        .form-group-title {
          font-size: 18px;
          font-weight: 600;
          color: #2c3e50;
          margin-bottom: 20px;
          padding-bottom: 12px;
          border-bottom: 2px solid #f0f2f5;
          display: flex;
          align-items: center;

          .title-icon {
            margin-right: 8px;
            font-size: 20px;
          }
        }

        :deep(.el-form-item) {
          margin-bottom: 24px;

          .el-form-item__label {
            font-weight: 500;
            color: #606266;
          }

          .el-input {
            .el-input__wrapper {
              border-radius: 12px;
              padding: 8px 16px;
              box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
              transition: all 0.3s ease;

              &:hover {
                box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
              }

              &.is-focus {
                box-shadow: 0 4px 16px rgba(245, 87, 108, 0.2);
              }
            }

            &.is-disabled .el-input__wrapper {
              background-color: #f5f7fa;
            }
          }
        }
      }

      .form-actions {
        margin-top: 40px;
        text-align: center;

        .save-btn {
          width: 100%;
          height: 50px;
          font-size: 16px;
          font-weight: 600;
          border-radius: 12px;
          background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
          border: none;
          box-shadow: 0 8px 20px rgba(245, 87, 108, 0.3);
          transition: all 0.3s ease;

          .btn-icon {
            margin-right: 8px;
          }

          &:hover {
            transform: translateY(-2px);
            box-shadow: 0 12px 28px rgba(245, 87, 108, 0.4);
          }

          &:active {
            transform: translateY(0);
          }
        }
      }
    }
  }
}

// 动画
@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes backgroundMove {
  0% {
    transform: translate(0, 0);
  }
  100% {
    transform: translate(30px, 30px);
  }
}

// 响应式设计
@media (max-width: 768px) {
  .person-container {
    padding: 20px 10px;

    .person-card {
      .avatar-section {
        padding: 0 20px 20px;

        .avatar-wrapper .avatar-uploader .avatar-container {
          width: 120px;
          height: 120px;
        }

        .user-name {
          font-size: 24px;
        }
      }

      .form-section {
        padding: 20px;

        .form-group .form-group-title {
          font-size: 16px;
        }
      }
    }
  }
}
</style>
