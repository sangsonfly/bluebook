package com.example.springboot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 举报记录表
 * 用户举报违规内容
 */
@Data
@TableName("report")
public class Report implements Serializable {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /** 举报用户ID */
    private Integer userId;
    
    /** 
     * 举报对象类型
     * 1-笔记 2-评论 3-用户 4-二手商品 5-问题/答案
     */
    private Integer targetType;
    
    /** 举报对象ID */
    private Integer targetId;
    
    /** 
     * 举报原因类型
     * 1-违法违规 2-色情低俗 3-垃圾广告 4-侵权 5-其他
     */
    private Integer reasonType;
    
    /** 举报原因详细描述 */
    private String reason;
    
    /** 
     * 状态
     * 0-待处理 1-已处理 2-已驳回
     */
    private Integer status;
    
    /** 处理结果 */
    private String result;
    
    /** 处理人ID */
    private Integer handlerId;
    
    /** 举报时间 */
    private LocalDateTime createTime;
    
    /** 处理时间 */
    private LocalDateTime handleTime;
    
    // ============ 目标类型常量 ============
    
    /** 笔记 */
    public static final int TARGET_NOTE = 1;
    
    /** 评论 */
    public static final int TARGET_COMMENT = 2;
    
    /** 用户 */
    public static final int TARGET_USER = 3;
    
    /** 二手商品 */
    public static final int TARGET_SECONDHAND = 4;
    
    /** 问题/答案 */
    public static final int TARGET_QA = 5;
    
    // ============ 原因类型常量 ============
    
    /** 违法违规 */
    public static final int REASON_ILLEGAL = 1;
    
    /** 色情低俗 */
    public static final int REASON_VULGAR = 2;
    
    /** 垃圾广告 */
    public static final int REASON_SPAM = 3;
    
    /** 侵权 */
    public static final int REASON_INFRINGEMENT = 4;
    
    /** 其他 */
    public static final int REASON_OTHER = 5;
    
    // ============ 状态常量 ============
    
    /** 待处理 */
    public static final int STATUS_PENDING = 0;
    
    /** 已处理 */
    public static final int STATUS_HANDLED = 1;
    
    /** 已驳回 */
    public static final int STATUS_REJECTED = 2;
}

