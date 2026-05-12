package com.example.springboot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 标签表
 * 支持内容分类和标签管理
 */
@Data
@TableName("tag")
public class Tag implements Serializable {
    
    @TableId(type = IdType.AUTO)
    private Integer id;
    
    /** 标签名称 */
    private String name;
    
    /** 标签分类 */
    private String category;
    
    /** 使用次数 */
    private Integer useCount;
    
    /** 状态 1-正常 0-禁用 */
    private Integer status;
    
    /** 创建时间 */
    private LocalDateTime createTime;
    
    /** 更新时间 */
    private LocalDateTime updateTime;
    
    // ============ 状态常量 ============
    
    /** 禁用 */
    public static final int STATUS_DISABLED = 0;
    
    /** 正常 */
    public static final int STATUS_ACTIVE = 1;
    
    // ============ 分类常量 ============
    
    /** 学习 */
    public static final String CATEGORY_STUDY = "学习";
    
    /** 生活 */
    public static final String CATEGORY_LIFE = "生活";
    
    /** 社团 */
    public static final String CATEGORY_CLUB = "社团";
    
    /** 运动 */
    public static final String CATEGORY_SPORT = "运动";
    
    /** 通用 */
    public static final String CATEGORY_GENERAL = "general";
}

