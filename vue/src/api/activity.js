import request from '@/utils/request'

export function getClubActivities(clubId) {
  return request({
    url: `/api/activity/club/${clubId}`,
    method: 'get'
  })
}

export function createActivity(data) {
  return request({
    url: '/api/activity/create',
    method: 'post',
    data
  })
}

export function updateActivity(data) {
  return request({
    url: '/api/activity/update',
    method: 'put',
    data
  })
}

export function deleteActivity(id) {
  return request({
    url: `/api/activity/delete/${id}`,
    method: 'delete'
  })
}

export function updateActivityStatus(activityId, status) {
  return request({
    url: '/api/activity/updateStatus',
    method: 'post',
    params: { activityId, status }
  })
}

export function getActivityDetail(id) {
  return request({
    url: `/api/activity/detail/${id}`,
    method: 'get'
  })
}
