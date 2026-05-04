package com.example.springboot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 私信表
 * 实现用户间私信功能
 */
@Data
@TableName("private_message")
public class PrivateMessage implements Serializable {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /** 发送者ID */
    private Integer senderId;
    
    /** 接收者ID */
    private Integer receiverId;
    
    /** 消息内容 */
    private String content;
    
    /** 是否已读 1-是 0-否 */
    private Integer isRead;
    
    /** 创建时间 */
    private LocalDateTime createTime;
}

