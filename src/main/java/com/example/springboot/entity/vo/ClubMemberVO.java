package com.example.springboot.entity.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 社团成员列表视图（含用户昵称、认证姓名与学号）
 */
@Data
public class ClubMemberVO {

    private Long id;
    private Integer clubId;
    private Integer userId;

    /** 昵称 */
    private String nickname;
    /** 真实姓名（校园认证） */
    private String realName;
    /** 学号（校园认证） */
    private String studentId;
    /** 头像 */
    private String avatarUrl;

    /**
     * 角色 1-成员 2-管理员 3-社长
     */
    private Integer role;

    /** 状态 1-正常 0-已退出 */
    private Integer status;

    private LocalDateTime joinTime;
}
