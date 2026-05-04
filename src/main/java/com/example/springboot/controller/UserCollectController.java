package com.example.springboot.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.springboot.common.Result;
import com.example.springboot.entity.Note;
import com.example.springboot.service.UserCollectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 用户收藏Controller
 * 提供收藏/取消收藏、收藏夹管理等API
 */
@RestController
@RequestMapping("/api/userCollect")
public class UserCollectController {
    
    @Autowired
    private UserCollectService userCollectService;
    
    /**
     * 收藏
     */
    @PostMapping("/collect")
    public Result collect(@RequestParam Integer userId,
                         @RequestParam Integer targetType,
                         @RequestParam Integer targetId,
                         @RequestParam(required = false) String folderName) {
        try {
            boolean success = userCollectService.collect(userId, targetType, targetId, folderName);
            return success ? Result.success("收藏成功") : Result.error("收藏失败");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 取消收藏
     */
    @PostMapping("/uncollect")
    public Result uncollect(@RequestParam Integer userId,
                           @RequestParam Integer targetType,
                           @RequestParam Integer targetId) {
        try {
            boolean success = userCollectService.uncollect(userId, targetType, targetId);
            return success ? Result.success("取消收藏成功") : Result.error("取消收藏失败");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 检查是否已收藏
     */
    @GetMapping("/isCollected")
    public Result isCollected(@RequestParam Integer userId,
                             @RequestParam Integer targetType,
                             @RequestParam Integer targetId) {
        boolean isCollected = userCollectService.isCollected(userId, targetType, targetId);
        return Result.success(isCollected);
    }
    
    /**
     * 统计某对象的收藏数
     */
    @GetMapping("/count")
    public Result countCollects(@RequestParam Integer targetType,
                               @RequestParam Integer targetId) {
        Integer count = userCollectService.countCollects(targetType, targetId);
        return Result.success(count);
    }
    
    /**
     * 获取用户的收藏夹列表（带统计）
     */
    @GetMapping("/myFolders")
    public Result getMyFolders(@RequestParam Integer userId) {
        List<Map<String, Object>> folders = userCollectService.getUserFoldersWithCount(userId);
        return Result.success(folders);
    }
    
    /**
     * 获取用户收藏的笔记列表
     */
    @GetMapping("/myCollectedNotes")
    public Result getMyCollectedNotes(@RequestParam Integer userId,
                                     @RequestParam(required = false) String folderName,
                                     @RequestParam(defaultValue = "1") Integer pageNum,
                                     @RequestParam(defaultValue = "10") Integer pageSize) {
        Page<Note> page = new Page<>(pageNum, pageSize);
        IPage<Note> result = userCollectService.getUserCollectedNotes(userId, folderName, page);
        return Result.success(result);
    }
    
    /**
     * 移动收藏到其他收藏夹
     */
    @PostMapping("/moveToFolder")
    public Result moveToFolder(@RequestParam Integer userId,
                              @RequestParam Integer targetType,
                              @RequestParam Integer targetId,
                              @RequestParam String newFolderName) {
        try {
            boolean success = userCollectService.moveToFolder(userId, targetType, targetId, newFolderName);
            return success ? Result.success("移动成功") : Result.error("移动失败");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 批量检查收藏状态
     */
    @PostMapping("/batchCheck")
    public Result batchCheckCollected(@RequestParam Integer userId,
                                     @RequestParam Integer targetType,
                                     @RequestBody List<Integer> targetIds) {
        List<Integer> collectedIds = userCollectService.batchCheckCollected(userId, targetType, targetIds);
        return Result.success(collectedIds);
    }
}

