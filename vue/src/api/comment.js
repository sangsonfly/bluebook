import request from '@/utils/request'

// 根据笔记ID获取评论列表
export function getCommentsByNoteId(noteId, userId = null) {
    const params = {}
    if (userId) {
        params.userId = userId
    }
    return request({
        url: `/api/comment/note/${noteId}`,
        method: 'get',
        params: params
    })
}

// 发布评论
export function addComment(commentData) {
    return request({
        url: '/api/comment',
        method: 'post',
        data: commentData
    })
}

// 删除评论
export function deleteComment(commentId) {
    return request({
        url: `/api/comment/${commentId}`,
        method: 'delete'
    })
}

// 点赞/取消点赞评论
export function likeComment(commentId, userId) {
    return request({
        url: `/api/comment/${commentId}/like`,
        method: 'post',
        params: { userId }
    })
}

