package com.example.springboot.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.springboot.entity.UserInterest;

import java.util.List;

/**
 * 用户兴趣服务接口
 */
public interface IUserInterestService extends IService<UserInterest> {
    
    /**
     * 根据用户行为更新兴趣标签
     * 
     * @param userId 用户ID
     */
    void updateUserInterestFromBehavior(Integer userId);
    
    /**
     * 当用户对笔记进行操作时，提取笔记标签并更新用户兴趣
     * 
     * @param userId 用户ID
     * @param noteId 笔记ID
     * @param behaviorWeight 行为权重
     */
    void addInterestFromNote(Integer userId, Integer noteId, java.math.BigDecimal behaviorWeight);
    
    /**
     * 获取用户的兴趣标签列表
     * 
     * @param userId 用户ID
     * @return 兴趣标签列表
     */
    List<UserInterest> getUserInterestTags(Integer userId);
}

