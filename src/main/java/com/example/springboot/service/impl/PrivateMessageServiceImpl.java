package com.example.springboot.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.springboot.entity.PrivateMessage;
import com.example.springboot.entity.vo.PrivateSessionVO;
import com.example.springboot.mapper.PrivateMessageMapper;
import com.example.springboot.service.PrivateMessageService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 私聊服务实现
 */
@Service
public class PrivateMessageServiceImpl extends ServiceImpl<PrivateMessageMapper, PrivateMessage> implements PrivateMessageService {

    private static final int CONTENT_MAX_LENGTH = 1000;

    @Override
    @Transactional
    public void sendMessage(Integer senderId, Integer receiverId, String content) {
        if (senderId == null || receiverId == null) {
            throw new RuntimeException("发送方或接收方不能为空");
        }
        if (senderId.equals(receiverId)) {
            throw new RuntimeException("不能给自己发送私信");
        }
        if (content == null || content.trim().isEmpty()) {
            throw new RuntimeException("消息内容不能为空");
        }
        String normalized = content.trim();
        if (normalized.length() > CONTENT_MAX_LENGTH) {
            throw new RuntimeException("消息长度不能超过1000字");
        }

        PrivateMessage message = new PrivateMessage();
        message.setSenderId(senderId);
        message.setReceiverId(receiverId);
        message.setContent(normalized);
        message.setIsRead(0);
        this.save(message);
    }

    @Override
    public IPage<PrivateMessage> getConversation(Integer userId, Integer targetUserId, Page<PrivateMessage> page) {
        if (userId == null || targetUserId == null) {
            throw new RuntimeException("用户参数不能为空");
        }
        QueryWrapper<PrivateMessage> wrapper = new QueryWrapper<>();
        wrapper.and(w -> w.eq("sender_id", userId).eq("receiver_id", targetUserId)
                        .or()
                        .eq("sender_id", targetUserId).eq("receiver_id", userId))
                .orderByAsc("create_time", "id");
        return this.page(page, wrapper);
    }

    @Override
    @Transactional
    public Integer markConversationRead(Integer receiverId, Integer senderId) {
        if (receiverId == null || senderId == null) {
            throw new RuntimeException("用户参数不能为空");
        }
        return this.baseMapper.markConversationRead(receiverId, senderId);
    }

    @Override
    public List<PrivateSessionVO> getSessionList(Integer userId) {
        if (userId == null) {
            throw new RuntimeException("用户ID不能为空");
        }
        return this.baseMapper.getSessionList(userId);
    }

    @Override
    public Integer getUnreadCount(Integer userId) {
        if (userId == null) {
            throw new RuntimeException("用户ID不能为空");
        }
        return this.baseMapper.countUnread(userId);
    }
}
