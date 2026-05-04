import request from '@/utils/request'

// 查询所有笔记（支持不同类型）
export function getNoteList(type = 'latest', params = {}) {
    if (type === 'hot') {
        // 热门笔记
        return request({
            url: '/api/note/hot',
            method: 'get',
            params: params
        })
    } else if (type === 'following') {
        // 关注用户的笔记
        return request({
            url: '/api/note/following',
            method: 'get',
            params: params
        })
    } else {
        // 默认最新
        return request({
            url: '/api/note/list',
            method: 'get',
            params: params
        })
    }
}

// 分页查询笔记
export function getNotePage(params) {
    return request({
        url: '/api/note/page',
        method: 'get',
        params: params
    })
}

// 根据ID查询笔记详情
export function getNoteById(id) {
    return request({
        url: `/api/note/${id}`,
        method: 'get'
    })
}

// 新增笔记
export function addNote(data) {
    return request({
        url: '/api/note',
        method: 'post',
        data: data
    })
}

// 更新笔记
export function updateNote(data) {
    return request({
        url: '/api/note',
        method: 'put',
        data: data
    })
}

// 删除笔记
export function deleteNote(id) {
    return request({
        url: `/api/note/${id}`,
        method: 'delete'
    })
}

// 点赞笔记
export function likeNote(id, userId) {
    return request({
        url: `/api/note/${id}/like`,
        method: 'post',
        params: { userId }
    })
}

// 收藏笔记
export function collectNote(id, userId) {
    return request({
        url: `/api/note/${id}/collect`,
        method: 'post',
        params: { userId }
    })
}

// 获取用户对笔记的行为状态（是否点赞/收藏）
export function getNoteBehavior(id, userId) {
    return request({
        url: `/api/note/${id}/behavior`,
        method: 'get',
        params: { userId }
    })
}

// 根据作者查询笔记
export function getNoteByAuthor(authorName) {
    return request({
        url: `/api/note/author/${authorName}`,
        method: 'get'
    })
}

// 获取所有分类列表
export function getCategories() {
    return request({
        url: '/api/note/categories',
        method: 'get'
    })
}

