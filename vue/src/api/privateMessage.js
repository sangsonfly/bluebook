import request from '@/utils/request'

export function sendPrivateMessage(senderId, receiverId, content) {
    return request({
        url: '/api/privateMessage/send',
        method: 'post',
        params: {
            senderId,
            receiverId,
            content
        }
    })
}

export function getConversation(userId, targetUserId, pageNum = 1, pageSize = 100) {
    return request({
        url: '/api/privateMessage/conversation',
        method: 'get',
        params: {
            userId,
            targetUserId,
            pageNum,
            pageSize
        }
    })
}

export function markConversationRead(receiverId, senderId) {
    return request({
        url: '/api/privateMessage/markConversationRead',
        method: 'post',
        params: {
            receiverId,
            senderId
        }
    })
}

export function getSessionList(userId) {
    return request({
        url: '/api/privateMessage/sessionList',
        method: 'get',
        params: {
            userId
        }
    })
}

export function getPrivateUnreadCount(userId) {
    return request({
        url: '/api/privateMessage/unreadCount',
        method: 'get',
        params: {
            userId
        }
    })
}
