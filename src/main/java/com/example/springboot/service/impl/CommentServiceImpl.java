package com.example.springboot.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.springboot.entity.Comment;
import com.example.springboot.mapper.CommentMapper;
import com.example.springboot.service.ICommentService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;

/**
 * 评论Service实现类
 */
@Service
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment> implements ICommentService {
    
    @Resource
    private CommentMapper commentMapper;
    
    @Override
    @Transactional
    public void incrementLikes(Integer id) {
        Comment comment = commentMapper.selectById(id);
        if (comment != null) {
            comment.setLikes(comment.getLikes() == null ? 1 : comment.getLikes() + 1);
            commentMapper.updateById(comment);
        }
    }
    
    @Override
    @Transactional
    public void decrementLikes(Integer id) {
        Comment comment = commentMapper.selectById(id);
        if (comment != null && comment.getLikes() != null && comment.getLikes() > 0) {
            comment.setLikes(comment.getLikes() - 1);
            commentMapper.updateById(comment);
        }
    }
}

