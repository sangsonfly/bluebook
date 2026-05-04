package com.example.springboot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 二手商品表
 * 实现校园二手交易功能
 */
@Data
@TableName("secondhand_item")
public class SecondhandItem implements Serializable {
    
    @TableId(type = IdType.AUTO)
    private Integer id;
    
    /** 发布用户ID */
    private Integer userId;
    
    /** 商品标题 */
    private String title;
    
    /** 商品描述 */
    private String description;
    
    /** 商品图片（JSON数组或逗号分隔） */
    private String images;
    
    /** 价格 */
    private BigDecimal price;
    
    /** 原价 */
    private BigDecimal originalPrice;
    
    /** 分类（教材/电子产品/日用品/运动器材/其他） */
    private String category;
    
    /** 
     * 新旧程度
     * 1-全新 2-几乎全新 3-轻微使用 4-明显使用
     */
    private Integer condition;
    
    /** 
     * 状态
     * 0-已下架 1-在售 2-已预订 3-已售出
     */
    private Integer status;
    
    /** 浏览量 */
    private Integer views;
    
    /** 交易地点 */
    private String location;
    
    /** 联系方式 */
    private String contactInfo;
    
    /** 发布时间 */
    private LocalDateTime createTime;
    
    /** 更新时间 */
    private LocalDateTime updateTime;
    
    // ============ 新旧程度常量 ============
    
    /** 全新 */
    public static final int CONDITION_NEW = 1;
    
    /** 几乎全新 */
    public static final int CONDITION_LIKE_NEW = 2;
    
    /** 轻微使用 */
    public static final int CONDITION_LIGHTLY_USED = 3;
    
    /** 明显使用 */
    public static final int CONDITION_HEAVILY_USED = 4;
    
    // ============ 状态常量 ============
    
    /** 已下架 */
    public static final int STATUS_OFFLINE = 0;
    
    /** 在售 */
    public static final int STATUS_SELLING = 1;
    
    /** 已预订 */
    public static final int STATUS_RESERVED = 2;
    
    /** 已售出 */
    public static final int STATUS_SOLD = 3;
}

