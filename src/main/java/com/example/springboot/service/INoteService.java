package com.example.springboot.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.springboot.entity.Note;

/**
 * 笔记Service接口
 */
public interface INoteService extends IService<Note> {
    
    /**
     * 增加浏览量
     */
    void incrementViews(Integer id);
    
    /**
     * 增加点赞数
     */
    void incrementLikes(Integer id);
    
    /**
     * 减少点赞数
     */
    void decrementLikes(Integer id);
    
    /**
     * 增加收藏数
     */
    void incrementCollects(Integer id);
    
    /**
     * 减少收藏数
     */
    void decrementCollects(Integer id);
    
    /**
     * 获取笔记详情（增强版，自动填充作者账号类型）
     */
    Note getNoteDetailById(Integer id);
    
    /**
     * 获取关注用户的笔记列表
     */
    Page<Note> getFollowingUserNotes(Integer userId, Integer pageNum, Integer pageSize);
}

