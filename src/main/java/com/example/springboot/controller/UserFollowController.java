package com.example.springboot.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.springboot.common.Result;
import com.example.springboot.entity.User;
import com.example.springboot.service.UserFollowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 用户关注Controller
 * 提供关注/取关、查询关注列表等API
 */
@RestController
@RequestMapping("/api/userFollow")
public class UserFollowController {
    
    @Autowired
    private UserFollowService userFollowService;
    
    /**
     * 关注用户
     */
    @PostMapping("/follow")
    public Result follow(@RequestParam Integer followerId, @RequestParam Integer followeeId) {
        try {
            boolean success = userFollowService.follow(followerId, followeeId);
            return success ? Result.success() : Result.error("关注失败");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 取消关注
     */
    @PostMapping("/unfollow")
    public Result unfollow(@RequestParam Integer followerId, @RequestParam Integer followeeId) {
        try {
            boolean success = userFollowService.unfollow(followerId, followeeId);
            return success ? Result.success() : Result.error("取消关注失败");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 检查是否已关注
     */
    @GetMapping("/isFollowing")
    public Result isFollowing(@RequestParam Integer followerId, @RequestParam Integer followeeId) {
        boolean isFollowing = userFollowService.isFollowing(followerId, followeeId);
        return Result.success(isFollowing);
    }
    
    /**
     * 获取关注列表（我关注的人）
     */
    @GetMapping("/following")
    public Result getFollowingList(
            @RequestParam Integer userId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        Page<User> page = new Page<>(pageNum, pageSize);
        IPage<User> result = userFollowService.getFollowingList(userId, page);
        return Result.success(result);
    }
    
    /**
     * 获取粉丝列表（关注我的人）
     */
    @GetMapping("/followers")
    public Result getFollowersList(
            @RequestParam Integer userId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        Page<User> page = new Page<>(pageNum, pageSize);
        IPage<User> result = userFollowService.getFollowersList(userId, page);
        return Result.success(result);
    }
    
    /**
     * 统计关注数
     */
    @GetMapping("/countFollowing")
    public Result countFollowing(@RequestParam Integer userId) {
        Integer count = userFollowService.countFollowing(userId);
        return Result.success(count);
    }
    
    /**
     * 统计粉丝数
     */
    @GetMapping("/countFollowers")
    public Result countFollowers(@RequestParam Integer userId) {
        Integer count = userFollowService.countFollowers(userId);
        return Result.success(count);
    }
}

