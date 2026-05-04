package com.example.springboot.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.springboot.entity.PrivateMessage;
import com.example.springboot.entity.vo.PrivateSessionVO;

import java.util.List;

/**
 * 私聊服务接口
 */
public interface PrivateMessageService extends IService<PrivateMessage> {

    void sendMessage(Integer senderId, Integer receiverId, String content);

    IPage<PrivateMessage> getConversation(Integer userId, Integer targetUserId, Page<PrivateMessage> page);

    Integer markConversationRead(Integer receiverId, Integer senderId);

    List<PrivateSessionVO> getSessionList(Integer userId);

    Integer getUnreadCount(Integer userId);
}
