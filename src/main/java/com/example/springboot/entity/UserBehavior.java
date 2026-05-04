package com.example.springboot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 用户行为记录表（推荐算法核心数据）
 * 用于协同过滤推荐算法的数据基础
 */
@Data
@TableName("user_behavior")
public class UserBehavior implements Serializable {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /** 用户ID */
    private Integer userId;
    
    /** 笔记ID */
    private Integer noteId;
    
    /** 
     * 行为类型
     * 1-浏览 2-点赞 3-收藏 4-评论 5-分享
     */
    private Integer behaviorType;
    
    /** 
     * 行为权重
     * 浏览0.5，点赞1.0，收藏1.5，评论2.0，分享2.5
     */
    private BigDecimal weight;
    
    /** 停留时长（秒，仅浏览时有效） */
    private Integer duration;
    
    /** 创建时间 */
    private LocalDateTime createTime;
    
    // ============ 行为类型常量 ============
    
    /** 浏览 */
    public static final int BEHAVIOR_VIEW = 1;
    
    /** 点赞 */
    public static final int BEHAVIOR_LIKE = 2;
    
    /** 收藏 */
    public static final int BEHAVIOR_COLLECT = 3;
    
    /** 评论 */
    public static final int BEHAVIOR_COMMENT = 4;
    
    /** 分享 */
    public static final int BEHAVIOR_SHARE = 5;
}

