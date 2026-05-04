package com.example.springboot.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.springboot.common.Result;
import com.example.springboot.config.interceptor.AuthAccess;
import com.example.springboot.entity.Comment;
import com.example.springboot.entity.User;
import com.example.springboot.service.ICommentService;
import com.example.springboot.service.ICommentLikeService;
import com.example.springboot.service.IUserService;
import com.example.springboot.service.IUserBehaviorService;
import com.example.springboot.service.IUserInterestService;
import com.example.springboot.entity.UserBehavior;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 评论Controller
 */
@RestController
@RequestMapping("/api/comment")
public class CommentController {
    
    @Resource
    private ICommentService commentService;
    
    @Resource
    private ICommentLikeService commentLikeService;
    
    @Resource
    private IUserService userService;
    
    @Resource
    private IUserBehaviorService userBehaviorService;
    
    @Resource
    private IUserInterestService userInterestService;
    
    /**
     * 根据笔记ID查询评论列表（包含用户信息和点赞状态）
     */
    @AuthAccess
    @GetMapping("/note/{noteId}")
    public Result getByNoteId(@PathVariable Integer noteId, 
                              @RequestParam(required = false) Integer userId) {
        // 1. 查询评论列表
        QueryWrapper<Comment> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("note_id", noteId);
        queryWrapper.orderByDesc("create_time");
        List<Comment> list = commentService.list(queryWrapper);
        
        // 2. 如果提供了userId，批量查询点赞状态
        Set<Integer> likedCommentIds = null;
        if (userId != null && !list.isEmpty()) {
            List<Integer> commentIds = list.stream()
                    .map(Comment::getId)
                    .collect(Collectors.toList());
            likedCommentIds = commentLikeService.getLikedCommentIds(userId, commentIds);
        }
        
        // 3. 关联查询用户信息并构建返回数据
        final Set<Integer> finalLikedCommentIds = likedCommentIds;
        List<Map<String, Object>> result = list.stream().map(comment -> {
            Map<String, Object> map = new HashMap<>();
            // 添加评论基本信息
            map.put("id", comment.getId());
            map.put("noteId", comment.getNoteId());
            map.put("userId", comment.getUserId());
            map.put("content", comment.getContent());
            map.put("parentId", comment.getParentId());
            map.put("replyToUserId", comment.getReplyToUserId());
            map.put("likes", comment.getLikes());
            map.put("createTime", comment.getCreateTime());
            
            // 添加点赞状态
            if (finalLikedCommentIds != null) {
                map.put("isLiked", finalLikedCommentIds.contains(comment.getId()));
            } else {
                map.put("isLiked", false);
            }
            
            // 查询并添加用户信息
            if (comment.getUserId() != null) {
                User user = userService.getById(comment.getUserId());
                if (user != null) {
                    map.put("userName", user.getNickname());
                    map.put("userAvatar", user.getAvatarUrl());
                } else {
                    // 用户不存在时的默认值
                    map.put("userName", "用户" + comment.getUserId());
                    map.put("userAvatar", null);
                }
            } else {
                map.put("userName", null);
                map.put("userAvatar", null);
            }
            
            return map;
        }).collect(Collectors.toList());
        
        return Result.success(result);
    }
    
    /**
     * 新增评论
     */
    @PostMapping
    public Result save(@RequestBody Comment comment) {
        commentService.save(comment);
        
        // 记录评论行为（用于推荐算法）
        if (comment.getUserId() != null && comment.getNoteId() != null) {
            userBehaviorService.recordBehavior(comment.getUserId(), comment.getNoteId(), UserBehavior.BEHAVIOR_COMMENT);
            // 更新用户兴趣标签（评论权重2.0的一半）
            userInterestService.addInterestFromNote(comment.getUserId(), comment.getNoteId(), new java.math.BigDecimal("1.0"));
        }
        
        return Result.success();
    }
    
    /**
     * 删除评论
     */
    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Integer id) {
        commentService.removeById(id);
        return Result.success();
    }
    
    /**
     * 点赞评论（切换状态）
     */
    @PostMapping("/{id}/like")
    public Result like(@PathVariable Integer id, @RequestParam Integer userId) {
        boolean added = commentLikeService.toggleLike(userId, id);
        if (added) {
            commentService.incrementLikes(id);
        } else {
            commentService.decrementLikes(id);
        }
        return Result.success(added); // true-点赞成功，false-取消点赞
    }
}

