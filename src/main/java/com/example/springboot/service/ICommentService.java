package com.example.springboot.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.springboot.entity.Comment;

/**
 * 评论Service接口
 */
public interface ICommentService extends IService<Comment> {
    
    /**
     * 增加点赞数
     */
    void incrementLikes(Integer id);
    
    /**
     * 减少点赞数
     */
    void decrementLikes(Integer id);
}

