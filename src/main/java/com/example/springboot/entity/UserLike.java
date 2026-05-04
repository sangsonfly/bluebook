package com.example.springboot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户点赞表
 * 推荐算法核心数据源
 */
@Data
@TableName("user_like")
public class UserLike implements Serializable {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /** 用户ID */
    private Integer userId;
    
    /** 点赞对象类型 1-笔记 2-评论 3-答案 */
    private Integer targetType;
    
    /** 点赞对象ID */
    private Integer targetId;
    
    /** 点赞时间 */
    private LocalDateTime createTime;
    
    // ============ 类型常量 ============
    
    /** 笔记 */
    public static final int TYPE_NOTE = 1;
    
    /** 评论 */
    public static final int TYPE_COMMENT = 2;
    
    /** 答案 */
    public static final int TYPE_ANSWER = 3;
}

