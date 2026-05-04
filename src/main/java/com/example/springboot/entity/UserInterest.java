package com.example.springboot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 用户兴趣标签表
 * 用于推荐算法辅助，记录用户对各标签的兴趣权重
 */
@Data
@TableName("user_interest")
public class UserInterest implements Serializable {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /** 用户ID */
    private Integer userId;
    
    /** 兴趣标签 */
    private String tag;
    
    /** 权重（根据行为累计） */
    private BigDecimal weight;
    
    /** 更新时间 */
    private LocalDateTime updateTime;
}

