package com.example.springboot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 活动表
 * 社团发布的各类活动信息
 */
@Data
@TableName("activity")
public class Activity implements Serializable {
    
    @TableId(type = IdType.AUTO)
    private Integer id;
    
    /** 发布社团ID */
    private Integer clubId;
    
    /** 活动标题 */
    private String title;
    
    /** 活动描述 */
    private String description;
    
    /** 封面图 */
    private String coverUrl;
    
    /** 活动地点 */
    private String location;
    
    /** 开始时间 */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime startTime;
    
    /** 结束时间 */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime endTime;
    
    /** 最大参与人数（NULL为不限） */
    private Integer maxParticipants;
    
    /** 当前报名人数 */
    private Integer currentParticipants;
    
    /** 是否需要审核 1-是 0-否 */
    private Integer needApproval;
    
    /** 活动标签 */
    private String tags;
    
    /** 
     * 状态
     * 0-已取消 1-报名中 2-进行中 3-已结束
     */
    private Integer status;
    
    /** 创建时间 */
    private LocalDateTime createTime;
    
    /** 更新时间 */
    private LocalDateTime updateTime;
    
    // ============ 状态常量 ============
    
    /** 已取消 */
    public static final int STATUS_CANCELLED = 0;
    
    /** 报名中 */
    public static final int STATUS_REGISTERING = 1;
    
    /** 进行中 */
    public static final int STATUS_ONGOING = 2;
    
    /** 已结束 */
    public static final int STATUS_FINISHED = 3;
}

