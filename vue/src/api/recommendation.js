import request from '@/utils/request'

/**
 * 获取推荐笔记（16条个性推荐 + 4条热门推荐）
 */
export function getRecommendedNotes(params) {
  return request({
    url: '/api/recommendation/notes',
    method: 'get',
    params: params
  })
}

/**
 * 获取热门笔记（用于新用户冷启动）
 */
export function getHotNotes(params) {
  return request({
    url: '/api/recommendation/hot',
    method: 'get',
    params: params
  })
}

/**
 * 手动刷新推荐（管理员功能）
 */
export function refreshRecommendations() {
  return request({
    url: '/api/recommendation/refresh',
    method: 'post'
  })
}

/**
 * 刷新当前登录用户自己的推荐结果
 */
export function refreshMyRecommendations() {
  return request({
    url: '/api/recommendation/refresh/me',
    method: 'post'
  })
}

