import request from '@/utils/request'

/** 加入社团（当前登录用户） */
export function joinClub(clubId) {
  return request({
    url: '/api/clubMember/join',
    method: 'post',
    params: { clubId }
  })
}

/** 退出社团（当前登录用户） */
export function leaveClub(clubId) {
  return request({
    url: '/api/clubMember/leave',
    method: 'post',
    params: { clubId }
  })
}

/** 当前用户加入的社团列表 */
export function getMyClubs() {
  return request({
    url: '/api/clubMember/myClubs',
    method: 'get'
  })
}

/** 是否为社团成员（仅可查本人） */
export function isClubMember(clubId, userId) {
  return request({
    url: '/api/clubMember/isMember',
    method: 'get',
    params: { clubId, userId }
  })
}

/** 是否为社团管理员或社长（仅可查本人） */
export function isClubAdmin(clubId, userId) {
  return request({
    url: '/api/clubMember/isAdmin',
    method: 'get',
    params: { clubId, userId }
  })
}

/** 社团成员分页 */
export function getClubMembersPage(clubId, pageNum = 1, pageSize = 20) {
  return request({
    url: `/api/clubMember/list/${clubId}`,
    method: 'get',
    params: { pageNum, pageSize }
  })
}

/** 设置社团社长（仅平台管理员） */
export function setPresident(clubId, userId) {
  return request({
    url: '/api/clubMember/setPresident',
    method: 'post',
    params: { clubId, userId }
  })
}

/** 设置社团成员角色（仅社长或平台管理员） */
export function setClubMemberRole(clubId, userId, role) {
  return request({
    url: '/api/clubMember/setRole',
    method: 'post',
    params: { clubId, userId, role }
  })
}
