package com.example.springboot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 评论实体类（增强版）
 * 支持回复、@功能
 */
@Data
@TableName("comment")
public class Comment implements Serializable {
    
    @TableId(type = IdType.AUTO)
    private Integer id;
    
    /** 笔记ID */
    private Integer noteId;
    
    /** 评论用户ID */
    private Integer userId;
    
    /** 评论内容 */
    private String content;
    
    /** 父评论ID（回复功能） */
    private Integer parentId;
    
    /** 回复给谁（@功能） */
    private Integer replyToUserId;
    
    /** 点赞数 */
    private Integer likes;
    
    /** 创建时间 */
    private LocalDateTime createTime;
}

