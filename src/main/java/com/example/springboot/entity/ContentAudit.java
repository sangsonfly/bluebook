package com.example.springboot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 内容审核记录表
 * AI和人工审核记录
 */
@Data
@TableName("content_audit")
public class ContentAudit implements Serializable {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /** 
     * 内容类型
     * 1-笔记 2-评论 3-二手商品 4-问题/答案
     */
    private Integer contentType;
    
    /** 内容ID */
    private Integer contentId;
    
    /** 
     * 审核类型
     * 1-AI自动 2-人工审核
     */
    private Integer auditType;
    
    /** 
     * 审核结果
     * 1-通过 2-疑似违规 3-违规
     */
    private Integer auditResult;
    
    /** 风险评分（0-100） */
    private BigDecimal riskScore;
    
    /** 命中的敏感关键词 */
    private String keywords;
    
    /** 审核人ID（人工审核时） */
    private Integer auditorId;
    
    /** 审核备注 */
    private String remark;
    
    /** 审核时间 */
    private LocalDateTime createTime;
    
    // ============ 内容类型常量 ============
    
    /** 笔记 */
    public static final int CONTENT_NOTE = 1;
    
    /** 评论 */
    public static final int CONTENT_COMMENT = 2;
    
    /** 二手商品 */
    public static final int CONTENT_SECONDHAND = 3;
    
    /** 问题/答案 */
    public static final int CONTENT_QA = 4;
    
    // ============ 审核类型常量 ============
    
    /** AI自动审核 */
    public static final int AUDIT_AI = 1;
    
    /** 人工审核 */
    public static final int AUDIT_MANUAL = 2;
    
    // ============ 审核结果常量 ============
    
    /** 通过 */
    public static final int RESULT_PASS = 1;
    
    /** 疑似违规 */
    public static final int RESULT_SUSPICIOUS = 2;
    
    /** 违规 */
    public static final int RESULT_VIOLATION = 3;
}

