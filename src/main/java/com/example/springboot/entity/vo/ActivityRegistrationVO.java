package com.example.springboot.entity.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 活动报名列表视图（含用户昵称、认证姓名与学号）
 */
@Data
public class ActivityRegistrationVO {

    private Long id;
    private Integer activityId;
    private Integer userId;

    /** 昵称 */
    private String nickname;
    /** 真实姓名（校园认证） */
    private String realName;
    /** 学号（校园认证） */
    private String studentId;

    /**
     * 状态 0-待审核 1-已通过 2-已拒绝 3-已签到 4-已取消
     */
    private Integer status;

    private String remark;
    private String reviewRemark;
    private LocalDateTime createTime;
    private LocalDateTime reviewTime;
}
