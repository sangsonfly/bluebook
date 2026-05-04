package com.example.springboot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 社团实体类（增强版）
 * 支持管理员、活动统计等功能
 */
@Data
@TableName("club")
public class Club implements Serializable {
    
    @TableId(type = IdType.AUTO)
    private Integer id;
    
    /** 社团名称 */
    private String name;
    
    /** 社团简介 */
    private String description;
    
    /** 社团头像 */
    private String avatarUrl;
    
    /** 封面图 */
    private String coverUrl;
    
    /** 社团类别 */
    private String category;
    
    // ============ 管理信息 ============
    
    /** 社团管理员用户ID */
    private Integer adminUserId;
    
    /** 联系方式（JSON格式） */
    private String contactInfo;
    
    // ============ 统计数据 ============
    
    /** 成员数量 */
    private Integer memberCount;
    
    /** 活动数量 */
    private Integer activityCount;
    
    // ============ 认证状态 ============
    
    /** 是否认证 1-是 0-否 */
    private Integer isVerified;
    
    /** 认证时间 */
    private LocalDateTime verifyTime;
    
    /** 状态 1-正常 0-禁用 */
    private Integer status;
    
    /** 创建时间 */
    private LocalDateTime createTime;
    
    /** 更新时间 */
    private LocalDateTime updateTime;
}

