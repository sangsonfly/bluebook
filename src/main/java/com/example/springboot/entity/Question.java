package com.example.springboot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 问题表
 * 实现问答社区功能
 */
@Data
@TableName("question")
public class Question implements Serializable {
    
    @TableId(type = IdType.AUTO)
    private Integer id;
    
    /** 提问用户ID */
    private Integer userId;
    
    /** 问题标题 */
    private String title;
    
    /** 问题描述 */
    private String content;
    
    /** 图片URL */
    private String images;
    
    /** 标签（逗号分隔） */
    private String tags;
    
    /** 分类 */
    private String category;
    
    /** 浏览数 */
    private Integer views;
    
    /** 回答数 */
    private Integer answerCount;
    
    /** 最佳答案ID */
    private Integer bestAnswerId;
    
    /** 悬赏积分 */
    private Integer rewardPoints;
    
    /** 
     * 状态
     * 1-待解决 2-已解决
     */
    private Integer status;
    
    /** 创建时间 */
    private LocalDateTime createTime;
    
    /** 更新时间 */
    private LocalDateTime updateTime;
    
    // ============ 状态常量 ============
    
    /** 待解决 */
    public static final int STATUS_PENDING = 1;
    
    /** 已解决 */
    public static final int STATUS_SOLVED = 2;
}

