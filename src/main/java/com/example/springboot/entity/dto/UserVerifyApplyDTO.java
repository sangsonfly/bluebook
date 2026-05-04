package com.example.springboot.entity.dto;

import lombok.Data;

@Data
public class UserVerifyApplyDTO {
    private String studentId;
    private String realName;
    private String school;
    private String college;
    private String major;
    private String grade;
}
