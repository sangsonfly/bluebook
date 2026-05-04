package com.example.springboot.service;

import com.example.springboot.entity.Note;

import java.util.List;

/**
 * 推荐服务接口
 */
public interface IRecommendationService {
    
    /**
     * 获取推荐笔记（16条个性推荐 + 4条热门推荐）
     * 
     * @param userId 用户ID
     * @param limit 推荐数量（默认20）
     * @return 推荐笔记列表
     */
    List<Note> getRecommendedNotes(Integer userId, Integer limit);
    
    /**
     * 基于协同过滤的推荐
     * 
     * @param userId 用户ID
     * @param limit 推荐数量
     * @return 推荐笔记列表
     */
    List<Note> getCollaborativeFilteringRecommendations(Integer userId, Integer limit);
    
    /**
     * 获取热门笔记（用于新用户冷启动）
     * 
     * @param limit 推荐数量
     * @return 热门笔记列表
     */
    List<Note> getHotRecommendations(Integer limit);
    
    /**
     * 为单个用户预计算推荐结果
     * 
     * @param userId 用户ID
     */
    void precomputeRecommendationsForUser(Integer userId);
    
    /**
     * 为所有活跃用户预计算推荐结果
     */
    void precomputeRecommendationsForAllActiveUsers();
}

