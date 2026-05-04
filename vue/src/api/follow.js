import request from '@/utils/request'

/**
 * 关注用户
 * @param {Number} followerId - 关注者ID（当前用户）
 * @param {Number} followeeId - 被关注者ID（目标用户）
 */
export function followUser(followerId, followeeId) {
    return request({
        url: '/api/userFollow/follow',
        method: 'post',
        params: {
            followerId,
            followeeId
        }
    })
}

/**
 * 取消关注
 * @param {Number} followerId - 关注者ID（当前用户）
 * @param {Number} followeeId - 被关注者ID（目标用户）
 */
export function unfollowUser(followerId, followeeId) {
    return request({
        url: '/api/userFollow/unfollow',
        method: 'post',
        params: {
            followerId,
            followeeId
        }
    })
}

/**
 * 检查是否已关注
 * @param {Number} followerId - 关注者ID（当前用户）
 * @param {Number} followeeId - 被关注者ID（目标用户）
 */
export function checkIsFollowing(followerId, followeeId) {
    return request({
        url: '/api/userFollow/isFollowing',
        method: 'get',
        params: {
            followerId,
            followeeId
        }
    })
}

/**
 * 获取关注列表（我关注的人）
 * @param {Number} userId - 用户ID
 * @param {Number} pageNum - 页码
 * @param {Number} pageSize - 每页数量
 */
export function getFollowingList(userId, pageNum = 1, pageSize = 10) {
    return request({
        url: '/api/userFollow/following',
        method: 'get',
        params: {
            userId,
            pageNum,
            pageSize
        }
    })
}

/**
 * 获取粉丝列表（关注我的人）
 * @param {Number} userId - 用户ID
 * @param {Number} pageNum - 页码
 * @param {Number} pageSize - 每页数量
 */
export function getFollowersList(userId, pageNum = 1, pageSize = 10) {
    return request({
        url: '/api/userFollow/followers',
        method: 'get',
        params: {
            userId,
            pageNum,
            pageSize
        }
    })
}

/**
 * 统计关注数
 * @param {Number} userId - 用户ID
 */
export function countFollowing(userId) {
    return request({
        url: '/api/userFollow/countFollowing',
        method: 'get',
        params: {
            userId
        }
    })
}

/**
 * 统计粉丝数
 * @param {Number} userId - 用户ID
 */
export function countFollowers(userId) {
    return request({
        url: '/api/userFollow/countFollowers',
        method: 'get',
        params: {
            userId
        }
    })
}

