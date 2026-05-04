package com.example.springboot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 评论点赞实体类
 */
@Data
@TableName("comment_like")
public class CommentLike implements Serializable {
    
    @TableId(type = IdType.AUTO)
    private Integer id;
    
    /** 用户ID */
    private Integer userId;
    
    /** 评论ID */
    private Integer commentId;
    
    /** 创建时间 */
    private LocalDateTime createTime;
}

