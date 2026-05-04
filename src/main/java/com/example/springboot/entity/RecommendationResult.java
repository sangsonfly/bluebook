package com.example.springboot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 推荐结果缓存表
 * 用于存储预计算的推荐结果
 */
@Data
@TableName("recommendation_result")
public class RecommendationResult implements Serializable {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /** 用户ID */
    private Integer userId;
    
    /** 推荐的笔记ID列表（逗号分隔） */
    private String noteIds;
    
    /** 
     * 算法类型 
     * 1-协同过滤 2-基于内容
     */
    private Integer algorithmType;
    
    /** 推荐质量评分 */
    private BigDecimal score;
    
    /** 创建时间 */
    private LocalDateTime createTime;
    
    /** 更新时间 */
    private LocalDateTime updateTime;
}

