package com.example.springboot.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.springboot.entity.Notification;
import com.example.springboot.mapper.NotificationMapper;
import org.springframework.stereotype.Service;

/**
 * 消息通知服务
 * 实现各类消息通知功能
 */
@Service
public class NotificationService extends ServiceImpl<NotificationMapper, Notification> {
    
    /**
     * 发送点赞通知
     */
    public void sendLikeNotification(Integer senderId, Integer receiverId, Integer noteId) {
        Notification notification = new Notification();
        notification.setSenderId(senderId);
        notification.setReceiverId(receiverId);
        notification.setType(Notification.TYPE_LIKE);
        notification.setContent("点赞了你的笔记");
        notification.setRelatedType(Notification.RELATED_NOTE);
        notification.setRelatedId(noteId);
        notification.setIsRead(0);
        this.save(notification);
    }
    
    /**
     * 发送评论通知
     */
    public void sendCommentNotification(Integer senderId, Integer receiverId, Integer noteId, Integer commentId) {
        Notification notification = new Notification();
        notification.setSenderId(senderId);
        notification.setReceiverId(receiverId);
        notification.setType(Notification.TYPE_COMMENT);
        notification.setContent("评论了你的笔记");
        notification.setRelatedType(Notification.RELATED_NOTE);
        notification.setRelatedId(noteId);
        notification.setIsRead(0);
        this.save(notification);
    }
    
    /**
     * 发送回复通知
     */
    public void sendReplyNotification(Integer senderId, Integer receiverId, Integer commentId) {
        Notification notification = new Notification();
        notification.setSenderId(senderId);
        notification.setReceiverId(receiverId);
        notification.setType(Notification.TYPE_REPLY);
        notification.setContent("回复了你的评论");
        notification.setRelatedType(Notification.RELATED_COMMENT);
        notification.setRelatedId(commentId);
        notification.setIsRead(0);
        this.save(notification);
    }
    
    /**
     * 发送收藏通知
     */
    public void sendCollectNotification(Integer senderId, Integer receiverId, Integer noteId) {
        Notification notification = new Notification();
        notification.setSenderId(senderId);
        notification.setReceiverId(receiverId);
        notification.setType(Notification.TYPE_COLLECT);
        notification.setContent("收藏了你的笔记");
        notification.setRelatedType(Notification.RELATED_NOTE);
        notification.setRelatedId(noteId);
        notification.setIsRead(0);
        this.save(notification);
    }
    
    /**
     * 发送关注通知
     */
    public void sendFollowNotification(Integer senderId, Integer receiverId) {
        Notification notification = new Notification();
        notification.setSenderId(senderId);
        notification.setReceiverId(receiverId);
        notification.setType(Notification.TYPE_FOLLOW);
        notification.setContent("关注了你");
        notification.setIsRead(0);
        this.save(notification);
    }
    
    /**
     * 发送活动通知
     */
    public void sendActivityNotification(Integer receiverId, Integer activityId, String content) {
        Notification notification = new Notification();
        notification.setReceiverId(receiverId);
        notification.setType(Notification.TYPE_ACTIVITY);
        notification.setContent(content);
        notification.setRelatedType(Notification.RELATED_ACTIVITY);
        notification.setRelatedId(activityId);
        notification.setIsRead(0);
        this.save(notification);
    }
    
    /**
     * 发送系统通知
     */
    public void sendSystemNotification(Integer receiverId, String content) {
        Notification notification = new Notification();
        notification.setReceiverId(receiverId);
        notification.setType(Notification.TYPE_SYSTEM);
        notification.setContent(content);
        notification.setIsRead(0);
        this.save(notification);
    }
    
    /**
     * 获取用户的通知列表
     */
    public IPage<Notification> getUserNotifications(Integer userId, Page<Notification> page) {
        QueryWrapper<Notification> wrapper = new QueryWrapper<>();
        wrapper.eq("receiver_id", userId)
               .orderByDesc("create_time");
        return this.page(page, wrapper);
    }
    
    /**
     * 获取未读通知数
     */
    public Integer getUnreadCount(Integer userId) {
        return this.baseMapper.countUnread(userId);
    }
    
    /**
     * 标记消息为已读
     */
    public boolean markAsRead(Long notificationId) {
        Notification notification = this.getById(notificationId);
        if (notification != null) {
            notification.setIsRead(1);
            return this.updateById(notification);
        }
        return false;
    }
    
    /**
     * 标记所有消息为已读
     */
    public Integer markAllAsRead(Integer userId) {
        return this.baseMapper.markAllAsRead(userId);
    }
}

