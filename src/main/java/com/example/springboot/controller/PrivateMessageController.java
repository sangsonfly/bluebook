package com.example.springboot.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.springboot.common.Result;
import com.example.springboot.entity.PrivateMessage;
import com.example.springboot.entity.vo.PrivateSessionVO;
import com.example.springboot.service.PrivateMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 私聊消息 Controller
 */
@RestController
@RequestMapping("/api/privateMessage")
public class PrivateMessageController {

    @Autowired
    private PrivateMessageService privateMessageService;

    @PostMapping("/send")
    public Result send(@RequestParam Integer senderId,
                       @RequestParam Integer receiverId,
                       @RequestParam String content) {
        try {
            privateMessageService.sendMessage(senderId, receiverId, content);
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/conversation")
    public Result getConversation(@RequestParam Integer userId,
                                  @RequestParam Integer targetUserId,
                                  @RequestParam(defaultValue = "1") Integer pageNum,
                                  @RequestParam(defaultValue = "20") Integer pageSize) {
        try {
            Page<PrivateMessage> page = new Page<>(pageNum, pageSize);
            IPage<PrivateMessage> result = privateMessageService.getConversation(userId, targetUserId, page);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/markConversationRead")
    public Result markConversationRead(@RequestParam Integer receiverId,
                                       @RequestParam Integer senderId) {
        try {
            Integer updatedCount = privateMessageService.markConversationRead(receiverId, senderId);
            return Result.success(updatedCount);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/sessionList")
    public Result getSessionList(@RequestParam Integer userId) {
        try {
            List<PrivateSessionVO> data = privateMessageService.getSessionList(userId);
            return Result.success(data);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/unreadCount")
    public Result unreadCount(@RequestParam Integer userId) {
        try {
            Integer unread = privateMessageService.getUnreadCount(userId);
            return Result.success(unread);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}
