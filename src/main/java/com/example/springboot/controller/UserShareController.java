package com.example.springboot.controller;

import com.example.springboot.common.Result;
import com.example.springboot.service.UserShareService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 用户分享Controller
 * 提供分享记录、统计等API
 */
@RestController
@RequestMapping("/api/userShare")
public class UserShareController {
    
    @Autowired
    private UserShareService userShareService;
    
    /**
     * 记录分享行为
     */
    @PostMapping("/share")
    public Result share(@RequestParam Integer userId,
                       @RequestParam Integer targetType,
                       @RequestParam Integer targetId,
                       @RequestParam(required = false) String platform) {
        try {
            boolean success = userShareService.recordShare(userId, targetType, targetId, platform);
            return success ? Result.success("分享成功") : Result.error("分享失败");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 统计某对象的分享数
     */
    @GetMapping("/count")
    public Result countShares(@RequestParam Integer targetType,
                             @RequestParam Integer targetId) {
        Integer count = userShareService.countShares(targetType, targetId);
        return Result.success(count);
    }
    
    /**
     * 统计用户的分享次数
     */
    @GetMapping("/countUserShares")
    public Result countUserShares(@RequestParam Integer userId) {
        Integer count = userShareService.countUserShares(userId);
        return Result.success(count);
    }
    
    /**
     * 统计各平台分享数量
     */
    @GetMapping("/countByPlatform")
    public Result countSharesByPlatform(@RequestParam Integer targetType,
                                       @RequestParam Integer targetId) {
        List<Map<String, Object>> result = userShareService.countSharesByPlatform(targetType, targetId);
        return Result.success(result);
    }
    
    /**
     * 获取用户分享过的笔记ID列表
     */
    @GetMapping("/mySharedNoteIds")
    public Result getMySharedNoteIds(@RequestParam Integer userId) {
        List<Integer> noteIds = userShareService.getUserSharedNoteIds(userId);
        return Result.success(noteIds);
    }
    
    /**
     * 检查用户是否分享过某内容
     */
    @GetMapping("/hasShared")
    public Result hasShared(@RequestParam Integer userId,
                           @RequestParam Integer targetType,
                           @RequestParam Integer targetId) {
        boolean hasShared = userShareService.hasShared(userId, targetType, targetId);
        return Result.success(hasShared);
    }
}

