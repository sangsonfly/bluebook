package com.example.springboot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 活动报名表
 * 记录用户的活动报名和审核状态
 */
@Data
@TableName("activity_registration")
public class ActivityRegistration implements Serializable {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /** 活动ID */
    private Integer activityId;
    
    /** 用户ID */
    private Integer userId;
    
    /** 
     * 状态
     * 0-待审核 1-已通过 2-已拒绝 3-已签到 4-已取消
     */
    private Integer status;
    
    /** 报名备注 */
    private String remark;
    
    /** 审核备注 */
    private String reviewRemark;
    
    /** 报名时间 */
    private LocalDateTime createTime;
    
    /** 审核时间 */
    private LocalDateTime reviewTime;
    
    // ============ 状态常量 ============
    
    /** 待审核 */
    public static final int STATUS_PENDING = 0;
    
    /** 已通过 */
    public static final int STATUS_APPROVED = 1;
    
    /** 已拒绝 */
    public static final int STATUS_REJECTED = 2;
    
    /** 已签到 */
    public static final int STATUS_CHECKED_IN = 3;
    
    /** 已取消 */
    public static final int STATUS_CANCELLED = 4;
}

