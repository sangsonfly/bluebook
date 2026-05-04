package com.example.springboot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 消息通知表
 * 实现站内消息通知功能
 */
@Data
@TableName("notification")
public class Notification implements Serializable {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /** 接收者ID */
    private Integer receiverId;
    
    /** 发送者ID（系统通知为NULL） */
    private Integer senderId;
    
    /** 
     * 通知类型
     * 1-点赞 2-评论 3-关注 4-系统通知 5-活动通知 6-回复 7-收藏
     */
    private Integer type;
    
    /** 通知内容 */
    private String content;
    
    /** 
     * 关联类型
     * 1-笔记 2-评论 3-活动
     */
    private Integer relatedType;
    
    /** 关联对象ID */
    private Integer relatedId;
    
    /** 是否已读 1-是 0-否 */
    private Integer isRead;
    
    /** 创建时间 */
    private LocalDateTime createTime;
    
    // ============ 通知类型常量 ============
    
    /** 点赞通知 */
    public static final int TYPE_LIKE = 1;
    
    /** 评论通知 */
    public static final int TYPE_COMMENT = 2;
    
    /** 关注通知 */
    public static final int TYPE_FOLLOW = 3;
    
    /** 系统通知 */
    public static final int TYPE_SYSTEM = 4;
    
    /** 活动通知 */
    public static final int TYPE_ACTIVITY = 5;
    
    /** 回复通知 */
    public static final int TYPE_REPLY = 6;
    
    /** 收藏通知 */
    public static final int TYPE_COLLECT = 7;
    
    // ============ 关联类型常量 ============
    
    /** 笔记 */
    public static final int RELATED_NOTE = 1;
    
    /** 评论 */
    public static final int RELATED_COMMENT = 2;
    
    /** 活动 */
    public static final int RELATED_ACTIVITY = 3;
}

