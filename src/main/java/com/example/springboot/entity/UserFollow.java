package com.example.springboot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户关注关系表
 * 实现关注/粉丝功能
 */
@Data
@TableName("user_follow")
public class UserFollow implements Serializable {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /** 关注者ID（粉丝） */
    private Integer followerId;
    
    /** 被关注者ID */
    private Integer followeeId;
    
    /** 状态 1-正常 0-取消 */
    private Integer status;
    
    /** 关注时间 */
    private LocalDateTime createTime;
    
    // ============ 状态常量 ============
    
    /** 已取消 */
    public static final int STATUS_CANCELLED = 0;
    
    /** 正常关注 */
    public static final int STATUS_ACTIVE = 1;
}

