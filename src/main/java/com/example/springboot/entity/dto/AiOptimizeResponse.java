package com.example.springboot.entity.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class AiOptimizeResponse {
    private String optimizedTitle;
    private String optimizedContent;
    private List<String> highlights = new ArrayList<>();
}
