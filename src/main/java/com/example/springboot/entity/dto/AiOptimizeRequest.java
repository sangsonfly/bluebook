package com.example.springboot.entity.dto;

import lombok.Data;

@Data
public class AiOptimizeRequest {
    private String title;
    private String content;
    private String style;
}
