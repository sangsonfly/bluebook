package com.example.springboot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 答案表
 * 问题的回答记录
 */
@Data
@TableName("answer")
public class Answer implements Serializable {
    
    @TableId(type = IdType.AUTO)
    private Integer id;
    
    /** 问题ID */
    private Integer questionId;
    
    /** 回答用户ID */
    private Integer userId;
    
    /** 回答内容 */
    private String content;
    
    /** 图片URL */
    private String images;
    
    /** 点赞数 */
    private Integer likes;
    
    /** 是否最佳答案 1-是 0-否 */
    private Integer isBest;
    
    /** 创建时间 */
    private LocalDateTime createTime;
}

