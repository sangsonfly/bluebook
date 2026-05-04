package com.example.springboot.entity.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 后台社团管理列表视图对象
 */
@Data
public class ClubAdminVO {

    private Integer id;
    private String name;
    private String description;
    private String avatarUrl;
    private String coverUrl;
    private String category;
    private Integer memberCount;
    private Integer activityCount;
    private Integer isVerified;
    private LocalDateTime verifyTime;
    private Integer status;
    private LocalDateTime createTime;
    private String contactInfo;

    private Integer presidentUserId;
    private String presidentName;
    private String presidentAvatar;

    /** 仅统计角色为管理员（role=2） */
    private Integer adminCount;
    /** 管理员昵称预览，默认最多返回3个 */
    private List<String> adminPreviewNames;
}
