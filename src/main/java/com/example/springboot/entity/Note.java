package com.example.springboot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 笔记实体类（增强版）
 * 支持社团发布、官方标识等功能
 */
@Data
@TableName("note")
public class Note implements Serializable {
    
    @TableId(type = IdType.AUTO)
    private Integer id;
    
    /** 标题 */
    private String title;
    
    /** 内容 */
    private String content;
    
    /** 图片URL（多图用逗号分隔） */
    private String imageUrl;
    
    // ============ 发布者信息 ============
    
    /** 发布用户ID */
    private Integer userId;
    
    /** 社团ID（社团发布时填写） */
    private Integer clubId;
    
    /** 发布类型 1-个人 2-社团 */
    private Integer publishType;
    
    /** 作者名称 */
    private String authorName;
    
    /** 作者头像 */
    private String authorAvatar;
    
    // ============ 互动数据 ============
    
    /** 点赞数 */
    private Integer likes;
    
    /** 浏览数 */
    private Integer views;
    
    /** 收藏数 */
    private Integer collects;
    
    /** 评论数 */
    private Integer comments;
    
    // ============ 分类与标签 ============
    
    /** 标签（逗号分隔） */
    private String tags;
    
    /** 分类 */
    private String category;
    
    // ============ 状态 ============
    
    /** 是否官方内容 1-是 0-否 */
    private Integer isOfficial;
    
    /** 状态：0-草稿 1-已发布 2-下架 -1-已删除(软删) */
    private Integer status;
    
    /** 创建时间 */
    private LocalDateTime createTime;
    
    /** 更新时间 */
    private LocalDateTime updateTime;
    
    // ============ 扩展字段（不存在于数据库，用于前端展示） ============
    
    /** 作者账号类型（从sys_user表关联查询） */
    @TableField(exist = false)
    private Integer accountType;
    
    /** 是否社团账号 */
    @TableField(exist = false)
    private Boolean isClubAccount;
    
    // ============ 发布类型常量 ============
    
    /** 个人发布 */
    public static final int PUBLISH_TYPE_PERSONAL = 1;
    
    /** 社团发布 */
    public static final int PUBLISH_TYPE_CLUB = 2;

    public static final int STATUS_DRAFT = 0;
    public static final int STATUS_PUBLISHED = 1;
    public static final int STATUS_OFFLINE = 2;
    public static final int STATUS_DELETED = -1;
}

