package com.example.springboot.entity.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 私聊会话摘要
 */
@Data
public class PrivateSessionVO {

    private Integer targetUserId;
    private String targetNickname;
    private String targetAvatarUrl;
    private String lastContent;
    private LocalDateTime lastTime;
    private Integer unreadCount;
}
