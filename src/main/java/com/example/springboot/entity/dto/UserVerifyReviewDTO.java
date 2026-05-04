package com.example.springboot.entity.dto;

import lombok.Data;

@Data
public class UserVerifyReviewDTO {
    private Integer userId;
    private Boolean approved;
}
