package com.example.springboot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 社团成员表
 * 记录用户与社团的关系
 */
@Data
@TableName("club_member")
public class ClubMember implements Serializable {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /** 社团ID */
    private Integer clubId;
    
    /** 用户ID */
    private Integer userId;
    
    /** 
     * 角色
     * 1-普通成员 2-管理员 3-社长
     */
    private Integer role;
    
    /** 状态 1-正常 0-已退出 */
    private Integer status;
    
    /** 加入时间 */
    private LocalDateTime joinTime;
    
    // ============ 角色常量 ============
    
    /** 普通成员 */
    public static final int ROLE_MEMBER = 1;
    
    /** 管理员 */
    public static final int ROLE_ADMIN = 2;
    
    /** 社长 */
    public static final int ROLE_PRESIDENT = 3;
}

