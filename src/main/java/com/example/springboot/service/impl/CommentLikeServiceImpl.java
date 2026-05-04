package com.example.springboot.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.springboot.entity.CommentLike;
import com.example.springboot.mapper.CommentLikeMapper;
import com.example.springboot.service.ICommentLikeService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 评论点赞Service实现类
 */
@Service
public class CommentLikeServiceImpl extends ServiceImpl<CommentLikeMapper, CommentLike> implements ICommentLikeService {
    
    @Resource
    private CommentLikeMapper commentLikeMapper;
    
    @Override
    @Transactional
    public boolean toggleLike(Integer userId, Integer commentId) {
        // 查询是否已存在
        LambdaQueryWrapper<CommentLike> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CommentLike::getUserId, userId)
                   .eq(CommentLike::getCommentId, commentId);
        CommentLike existing = commentLikeMapper.selectOne(queryWrapper);
        
        if (existing != null) {
            // 已存在，删除（取消点赞）
            commentLikeMapper.deleteById(existing.getId());
            return false;
        } else {
            // 不存在，添加（点赞）
            CommentLike commentLike = new CommentLike();
            commentLike.setUserId(userId);
            commentLike.setCommentId(commentId);
            commentLikeMapper.insert(commentLike);
            return true;
        }
    }
    
    @Override
    public boolean hasLiked(Integer userId, Integer commentId) {
        LambdaQueryWrapper<CommentLike> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CommentLike::getUserId, userId)
                   .eq(CommentLike::getCommentId, commentId);
        return commentLikeMapper.selectCount(queryWrapper) > 0;
    }
    
    @Override
    public Set<Integer> getLikedCommentIds(Integer userId, List<Integer> commentIds) {
        if (commentIds == null || commentIds.isEmpty()) {
            return Set.of();
        }
        
        LambdaQueryWrapper<CommentLike> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CommentLike::getUserId, userId)
                   .in(CommentLike::getCommentId, commentIds);
        List<CommentLike> likes = commentLikeMapper.selectList(queryWrapper);
        
        return likes.stream()
                   .map(CommentLike::getCommentId)
                   .collect(Collectors.toSet());
    }
}

