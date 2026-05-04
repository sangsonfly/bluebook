package com.example.springboot.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.springboot.common.Result;
import com.example.springboot.entity.Notification;
import com.example.springboot.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 消息通知Controller
 * 提供查询通知、标记已读等API
 */
@RestController
@RequestMapping("/notification")
public class NotificationController {
    
    @Autowired
    private NotificationService notificationService;
    
    /**
     * 获取用户的通知列表
     */
    @GetMapping("/list")
    public Result getNotifications(
            @RequestParam Integer userId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        Page<Notification> page = new Page<>(pageNum, pageSize);
        IPage<Notification> result = notificationService.getUserNotifications(userId, page);
        return Result.success(result);
    }
    
    /**
     * 获取未读消息数
     */
    @GetMapping("/unreadCount")
    public Result getUnreadCount(@RequestParam Integer userId) {
        Integer count = notificationService.getUnreadCount(userId);
        return Result.success(count);
    }
    
    /**
     * 标记消息为已读
     */
    @PostMapping("/markAsRead")
    public Result markAsRead(@RequestParam Long notificationId) {
        boolean success = notificationService.markAsRead(notificationId);
        return success ? Result.success() : Result.error("操作失败");
    }
    
    /**
     * 标记所有消息为已读
     */
    @PostMapping("/markAllAsRead")
    public Result markAllAsRead(@RequestParam Integer userId) {
        Integer count = notificationService.markAllAsRead(userId);
        return Result.success(count);
    }
}

