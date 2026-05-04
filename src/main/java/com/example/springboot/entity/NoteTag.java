package com.example.springboot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 笔记标签关联表
 * 实现笔记与标签的多对多关系
 */
@Data
@TableName("note_tag")
public class NoteTag implements Serializable {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /** 笔记ID */
    private Integer noteId;
    
    /** 标签ID */
    private Integer tagId;
    
    /** 创建时间 */
    private LocalDateTime createTime;
}

