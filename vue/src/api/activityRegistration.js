import request from '@/utils/request'

export function registerActivity(activityId, remark = '') {
  return request({
    url: '/api/activityRegistration/register',
    method: 'post',
    params: { activityId, remark }
  })
}

export function cancelActivityRegistration(activityId) {
  return request({
    url: '/api/activityRegistration/cancel',
    method: 'post',
    params: { activityId }
  })
}

export function hasRegistered(activityId) {
  return request({
    url: '/api/activityRegistration/hasRegistered',
    method: 'get',
    params: { activityId }
  })
}

export function getPendingRegistrations(activityId, pageNum = 1, pageSize = 20) {
  return request({
    url: `/api/activityRegistration/pending/${activityId}`,
    method: 'get',
    params: { pageNum, pageSize }
  })
}

export function getActivityRegistrations(activityId, pageNum = 1, pageSize = 20) {
  return request({
    url: `/api/activityRegistration/activity/${activityId}`,
    method: 'get',
    params: { pageNum, pageSize }
  })
}

export function reviewRegistration(registrationId, status, reviewRemark = '') {
  return request({
    url: '/api/activityRegistration/review',
    method: 'post',
    params: { registrationId, status, reviewRemark }
  })
}

export function checkInRegistration(registrationId) {
  return request({
    url: '/api/activityRegistration/checkIn',
    method: 'post',
    params: { registrationId }
  })
}
