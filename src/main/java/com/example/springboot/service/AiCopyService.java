package com.example.springboot.service;

import com.example.springboot.entity.dto.AiOptimizeRequest;
import com.example.springboot.entity.dto.AiOptimizeResponse;

public interface AiCopyService {
    AiOptimizeResponse optimize(AiOptimizeRequest request, Integer userId);
}
