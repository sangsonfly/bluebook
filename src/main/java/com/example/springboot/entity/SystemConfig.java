package com.example.springboot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 系统配置表
 * 存储系统各项配置参数
 */
@Data
@TableName("system_config")
public class SystemConfig implements Serializable {
    
    @TableId(type = IdType.AUTO)
    private Integer id;
    
    /** 配置键 */
    private String configKey;
    
    /** 配置值 */
    private String configValue;
    
    /** 配置说明 */
    private String description;
    
    /** 更新时间 */
    private LocalDateTime updateTime;
}

