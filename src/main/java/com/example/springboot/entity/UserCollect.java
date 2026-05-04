package com.example.springboot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户收藏表
 * 推荐算法高权重数据源
 */
@Data
@TableName("user_collect")
public class UserCollect implements Serializable {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /** 用户ID */
    private Integer userId;
    
    /** 收藏对象类型 1-笔记 2-二手商品 3-问题 */
    private Integer targetType;
    
    /** 收藏对象ID */
    private Integer targetId;
    
    /** 收藏夹名称 */
    private String folderName;
    
    /** 收藏时间 */
    private LocalDateTime createTime;
    
    // ============ 类型常量 ============
    
    /** 笔记 */
    public static final int TYPE_NOTE = 1;
    
    /** 二手商品 */
    public static final int TYPE_SECONDHAND_ITEM = 2;
    
    /** 问题 */
    public static final int TYPE_QUESTION = 3;
    
    /** 默认收藏夹 */
    public static final String DEFAULT_FOLDER = "默认收藏夹";
}

