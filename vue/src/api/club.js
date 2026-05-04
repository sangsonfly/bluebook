import request from '@/utils/request'

// 查询所有社团
export function getClubList() {
    return request({
        url: '/api/club/list',
        method: 'get'
    })
}

// 查询认证社团
export function getVerifiedClubs() {
    return request({
        url: '/api/club/verified',
        method: 'get'
    })
}

// 分页查询社团
export function getClubPage(params) {
    return request({
        url: '/api/club/page',
        method: 'get',
        params: params
    })
}

// 根据ID查询社团详情
export function getClubById(id) {
    return request({
        url: `/api/club/${id}`,
        method: 'get'
    })
}

