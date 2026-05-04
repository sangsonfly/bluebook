package com.example.springboot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * <p>
 * 用户实体类（增强版）
 * 支持校园认证、社交统计等功能
 * </p>
 */

@Data
@TableName(value = "sys_user")
public class User extends Account {

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    private String username;

    private String password;

    private String nickname;

    private String avatarUrl;

    private String email;

    private String phone;
    
    // ============ 校园认证相关 ============
    
    /** 学号 */
    private String studentId;
    
    /** 真实姓名 */
    private String realName;
    
    /** 是否认证 1-是 0-否 */
    private Integer isVerified;
    
    /** 认证时间 */
    private LocalDateTime verifyTime;
    
    /** 学校 */
    private String school;
    
    /** 学院 */
    private String college;
    
    /** 专业 */
    private String major;
    
    /** 年级 */
    private String grade;
    
    // ============ 社交数据统计 ============
    
    /** 关注数 */
    private Integer followingCount;
    
    /** 粉丝数 */
    private Integer followersCount;
    
    /** 笔记数 */
    private Integer notesCount;
    
    /** 获赞总数 */
    private Integer likesReceived;
    
    // ============ 个人信息 ============
    
    /** 个人简介 */
    private String bio;
    
    /** 性别 0-未知 1-男 2-女 */
    private Integer gender;
    
    // ============ 账号状态 ============
    
    /** 状态 1-正常 0-禁用 */
    private Integer status;
    
    /** 账号类型 1-普通用户 2-社团账号 3-机构账号 4-企业账号 */
    private Integer accountType;
    
    /** 注册时间 */
    private LocalDateTime registerTime;
    
    /** 最后登录时间 */
    private LocalDateTime lastLoginTime;
    
    // ============ 账号类型常量 ============
    
    /** 普通用户 */
    public static final int ACCOUNT_TYPE_USER = 1;
    
    /** 社团账号 */
    public static final int ACCOUNT_TYPE_CLUB = 2;
    
    /** 机构账号 */
    public static final int ACCOUNT_TYPE_ORGANIZATION = 3;
    
    /** 企业账号 */
    public static final int ACCOUNT_TYPE_ENTERPRISE = 4;

    // ============ 校园认证状态常量 ============

    /** 未认证 */
    public static final int VERIFY_STATUS_UNVERIFIED = 0;

    /** 已认证 */
    public static final int VERIFY_STATUS_VERIFIED = 1;

    /** 待审核 */
    public static final int VERIFY_STATUS_PENDING = 2;

}
