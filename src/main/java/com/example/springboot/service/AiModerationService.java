package com.example.springboot.service;

import com.example.springboot.entity.dto.AiModerationResult;

public interface AiModerationService {
    AiModerationResult check(String title, String content);
}
