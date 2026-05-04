package com.example.springboot.entity.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 后台用户管理视图对象
 */
@Data
public class UserManageVO {

    private Integer id;
    private String username;
    private String nickname;
    private String avatarUrl;
    private String email;
    private String phone;

    private Integer status;
    private Integer accountType;
    private Integer isVerified;
    private LocalDateTime registerTime;

    private Integer clubCount;
    private String clubNames;
}
