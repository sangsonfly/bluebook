package com.example.springboot.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.springboot.entity.CommentLike;

import java.util.List;
import java.util.Set;

/**
 * 评论点赞Service接口
 */
public interface ICommentLikeService extends IService<CommentLike> {
    
    /**
     * 切换点赞状态（已有则删除，没有则添加）
     * @param userId 用户ID
     * @param commentId 评论ID
     * @return true-添加了点赞，false-取消了点赞
     */
    boolean toggleLike(Integer userId, Integer commentId);
    
    /**
     * 检查用户是否已点赞某个评论
     * @param userId 用户ID
     * @param commentId 评论ID
     * @return true-已点赞，false-未点赞
     */
    boolean hasLiked(Integer userId, Integer commentId);
    
    /**
     * 批量查询用户点赞的评论ID集合
     * @param userId 用户ID
     * @param commentIds 评论ID列表
     * @return 已点赞的评论ID集合
     */
    Set<Integer> getLikedCommentIds(Integer userId, List<Integer> commentIds);
}

