package com.example.springboot.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.springboot.common.Result;
import com.example.springboot.entity.Note;
import com.example.springboot.service.UserLikeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户点赞Controller
 * 提供点赞/取消点赞、查询点赞列表等API
 */
@RestController
@RequestMapping("/api/userLike")
public class UserLikeController {
    
    @Autowired
    private UserLikeService userLikeService;
    
    /**
     * 点赞
     */
    @PostMapping("/like")
    public Result like(@RequestParam Integer userId, 
                      @RequestParam Integer targetType, 
                      @RequestParam Integer targetId) {
        try {
            boolean success = userLikeService.like(userId, targetType, targetId);
            return success ? Result.success("点赞成功") : Result.error("点赞失败");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 取消点赞
     */
    @PostMapping("/unlike")
    public Result unlike(@RequestParam Integer userId, 
                        @RequestParam Integer targetType, 
                        @RequestParam Integer targetId) {
        try {
            boolean success = userLikeService.unlike(userId, targetType, targetId);
            return success ? Result.success("取消点赞成功") : Result.error("取消点赞失败");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 检查是否已点赞
     */
    @GetMapping("/isLiked")
    public Result isLiked(@RequestParam Integer userId, 
                         @RequestParam Integer targetType, 
                         @RequestParam Integer targetId) {
        boolean isLiked = userLikeService.isLiked(userId, targetType, targetId);
        return Result.success(isLiked);
    }
    
    /**
     * 统计某对象的点赞数
     */
    @GetMapping("/count")
    public Result countLikes(@RequestParam Integer targetType, 
                            @RequestParam Integer targetId) {
        Integer count = userLikeService.countLikes(targetType, targetId);
        return Result.success(count);
    }
    
    /**
     * 获取用户点赞的笔记列表
     */
    @GetMapping("/myLikedNotes")
    public Result getMyLikedNotes(@RequestParam Integer userId,
                                 @RequestParam(defaultValue = "1") Integer pageNum,
                                 @RequestParam(defaultValue = "10") Integer pageSize) {
        Page<Note> page = new Page<>(pageNum, pageSize);
        IPage<Note> result = userLikeService.getUserLikedNotes(userId, page);
        return Result.success(result);
    }
    
    /**
     * 批量检查点赞状态
     */
    @PostMapping("/batchCheck")
    public Result batchCheckLiked(@RequestParam Integer userId,
                                 @RequestParam Integer targetType,
                                 @RequestBody List<Integer> targetIds) {
        List<Integer> likedIds = userLikeService.batchCheckLiked(userId, targetType, targetIds);
        return Result.success(likedIds);
    }
}

