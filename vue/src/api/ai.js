import request from '@/utils/request'

export function optimizeCopy(data) {
  return request({
    url: '/api/ai/copy/optimize',
    method: 'post',
    data
  })
}
