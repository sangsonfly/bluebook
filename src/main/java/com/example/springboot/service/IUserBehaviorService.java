package com.example.springboot.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.springboot.entity.UserBehavior;

import java.util.List;
import java.util.Map;

/**
 * 用户行为Service接口
 * 推荐算法的核心服务
 */
public interface IUserBehaviorService extends IService<UserBehavior> {
    
    /**
     * 记录用户行为（完整版，带停留时长）
     * @param userId 用户ID
     * @param noteId 笔记ID
     * @param behaviorType 行为类型（1-浏览 2-点赞 3-收藏 4-评论 5-分享）
     * @param duration 停留时长（秒），仅浏览行为时使用
     */
    void recordBehavior(Integer userId, Integer noteId, Integer behaviorType, Integer duration);
    
    /**
     * 记录用户行为（简化版，不需要停留时长）
     * 适用于点赞、收藏、评论、分享等即时行为
     * @param userId 用户ID
     * @param noteId 笔记ID
     * @param behaviorType 行为类型（2-点赞 3-收藏 4-评论 5-分享）
     */
    void recordBehavior(Integer userId, Integer noteId, Integer behaviorType);
    
    /**
     * 检查用户是否对笔记有某种行为
     */
    boolean hasBehavior(Integer userId, Integer noteId, Integer behaviorType);
    
    /**
     * 取消用户行为（点赞/收藏）
     * @return true-取消成功，false-不存在该行为
     */
    boolean removeBehavior(Integer userId, Integer noteId, Integer behaviorType);
    
    /**
     * 切换用户行为（点赞/收藏），已有则取消，没有则添加
     * @return true-添加了行为，false-取消了行为
     */
    boolean toggleBehavior(Integer userId, Integer noteId, Integer behaviorType);
    
    /**
     * 获取用户的行为历史
     */
    List<UserBehavior> getUserBehaviorHistory(Integer userId, Integer limit);
    
    /**
     * 获取笔记的行为统计
     */
    Map<String, Object> getNoteBehaviorStats(Integer noteId);
    
    /**
     * 获取用户最感兴趣的笔记（根据行为权重）
     */
    List<Integer> getUserTopNoteIds(Integer userId, Integer limit);
    
    /**
     * 获取与用户行为相似的其他用户ID
     */
    List<Integer> getSimilarUserIds(Integer userId, Integer limit);
}

