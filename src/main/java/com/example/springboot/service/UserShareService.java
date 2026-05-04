package com.example.springboot.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.springboot.entity.UserShare;
import com.example.springboot.mapper.UserShareMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * 用户分享服务
 * 提供分享记录、统计等功能
 */
@Service
public class UserShareService extends ServiceImpl<UserShareMapper, UserShare> {
    
    @Autowired
    private IUserBehaviorService userBehaviorService;
    
    /**
     * 记录分享行为
     */
    @Transactional
    public boolean recordShare(Integer userId, Integer targetType, Integer targetId, String platform) {
        // 创建分享记录
        UserShare share = new UserShare();
        share.setUserId(userId);
        share.setTargetType(targetType);
        share.setTargetId(targetId);
        share.setPlatform(platform != null ? platform : UserShare.PLATFORM_INTERNAL);
        boolean success = this.save(share);
        
        if (success) {
            // 记录用户行为（用于推荐算法）
            if (targetType == UserShare.TYPE_NOTE) {
                userBehaviorService.recordBehavior(userId, targetId, 5); // 行为类型5=分享
            }
        }
        
        return success;
    }
    
    /**
     * 统计某对象的分享数
     */
    public Integer countShares(Integer targetType, Integer targetId) {
        return this.baseMapper.countShares(targetType, targetId);
    }
    
    /**
     * 统计用户的分享次数
     */
    public Integer countUserShares(Integer userId) {
        return this.baseMapper.countUserShares(userId);
    }
    
    /**
     * 统计各平台分享数量
     */
    public List<Map<String, Object>> countSharesByPlatform(Integer targetType, Integer targetId) {
        return this.baseMapper.countSharesByPlatform(targetType, targetId);
    }
    
    /**
     * 获取用户分享过的笔记ID列表
     */
    public List<Integer> getUserSharedNoteIds(Integer userId) {
        return this.baseMapper.getUserSharedNoteIds(userId);
    }
    
    /**
     * 检查用户是否分享过某内容
     */
    public boolean hasShared(Integer userId, Integer targetType, Integer targetId) {
        return this.lambdaQuery()
                .eq(UserShare::getUserId, userId)
                .eq(UserShare::getTargetType, targetType)
                .eq(UserShare::getTargetId, targetId)
                .count() > 0;
    }
}

