package com.example.springboot.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.springboot.entity.User;
import com.example.springboot.entity.UserFollow;
import com.example.springboot.mapper.UserFollowMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 用户关注服务
 * 实现关注/取关、查询粉丝/关注列表等功能
 */
@Service
public class UserFollowService extends ServiceImpl<UserFollowMapper, UserFollow> {
    
    @Autowired
    private IUserService userService;
    
    @Autowired
    private NotificationService notificationService;
    
    /**
     * 关注用户
     */
    @Transactional
    public boolean follow(Integer followerId, Integer followeeId) {
        if (followerId.equals(followeeId)) {
            throw new RuntimeException("不能关注自己");
        }
        
        // 检查是否存在关注记录（包括已取消的）
        UserFollow existing = this.baseMapper.getFollowRecord(followerId, followeeId);
        
        if (existing != null) {
            // 已存在记录
            if (existing.getStatus().equals(UserFollow.STATUS_ACTIVE)) {
                throw new RuntimeException("已经关注过了");
            }
            // 之前取消过，重新激活
            existing.setStatus(UserFollow.STATUS_ACTIVE);
            boolean success = this.updateById(existing);
            if (success) {
                userService.incrementFollowingCount(followerId);
                userService.incrementFollowersCount(followeeId);
                notificationService.sendFollowNotification(followerId, followeeId);
            }
            return success;
        }
        
        // 创建新的关注记录
        UserFollow follow = new UserFollow();
        follow.setFollowerId(followerId);
        follow.setFolloweeId(followeeId);
        follow.setStatus(UserFollow.STATUS_ACTIVE);
        boolean success = this.save(follow);
        
        if (success) {
            userService.incrementFollowingCount(followerId);
            userService.incrementFollowersCount(followeeId);
            notificationService.sendFollowNotification(followerId, followeeId);
        }
        
        return success;
    }
    
    /**
     * 取消关注
     */
    @Transactional
    public boolean unfollow(Integer followerId, Integer followeeId) {
        QueryWrapper<UserFollow> wrapper = new QueryWrapper<>();
        wrapper.eq("follower_id", followerId)
               .eq("followee_id", followeeId)
               .eq("status", UserFollow.STATUS_ACTIVE);
        
        UserFollow follow = this.getOne(wrapper);
        if (follow == null) {
            throw new RuntimeException("未关注该用户");
        }
        
        // 更新状态为已取消
        follow.setStatus(UserFollow.STATUS_CANCELLED);
        boolean success = this.updateById(follow);
        
        if (success) {
            // 更新双方的关注数和粉丝数
            userService.decrementFollowingCount(followerId);
            userService.decrementFollowersCount(followeeId);
        }
        
        return success;
    }
    
    /**
     * 检查是否已关注
     */
    public boolean isFollowing(Integer followerId, Integer followeeId) {
        return this.baseMapper.getFollowRelation(followerId, followeeId) != null;
    }
    
    /**
     * 获取关注列表（我关注的人）
     */
    public IPage<User> getFollowingList(Integer userId, Page<User> page) {
        // 通过JOIN查询获取关注的用户信息
        return userService.getFollowingUsers(userId, page);
    }
    
    /**
     * 获取粉丝列表（关注我的人）
     */
    public IPage<User> getFollowersList(Integer userId, Page<User> page) {
        // 通过JOIN查询获取粉丝用户信息
        return userService.getFollowerUsers(userId, page);
    }
    
    /**
     * 统计关注数
     */
    public Integer countFollowing(Integer userId) {
        return this.baseMapper.countFollowing(userId);
    }
    
    /**
     * 统计粉丝数
     */
    public Integer countFollowers(Integer userId) {
        return this.baseMapper.countFollowers(userId);
    }
}

