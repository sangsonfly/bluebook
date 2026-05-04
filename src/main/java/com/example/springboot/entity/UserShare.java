package com.example.springboot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户分享表
 * 最高权重行为数据
 */
@Data
@TableName("user_share")
public class UserShare implements Serializable {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /** 分享用户ID */
    private Integer userId;
    
    /** 分享对象类型 1-笔记 2-活动 3-二手商品 */
    private Integer targetType;
    
    /** 分享对象ID */
    private Integer targetId;
    
    /** 分享平台 */
    private String platform;
    
    /** 分享时间 */
    private LocalDateTime createTime;
    
    // ============ 类型常量 ============
    
    /** 笔记 */
    public static final int TYPE_NOTE = 1;
    
    /** 活动 */
    public static final int TYPE_ACTIVITY = 2;
    
    /** 二手商品 */
    public static final int TYPE_SECONDHAND_ITEM = 3;
    
    // ============ 平台常量 ============
    
    /** 站内分享 */
    public static final String PLATFORM_INTERNAL = "internal";
    
    /** 微信 */
    public static final String PLATFORM_WECHAT = "wechat";
    
    /** QQ */
    public static final String PLATFORM_QQ = "qq";
    
    /** 微博 */
    public static final String PLATFORM_WEIBO = "weibo";
}

