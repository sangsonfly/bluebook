package com.example.springboot.entity.dto;

import lombok.Data;

@Data
public class AiModerationResult {
    private boolean pass;
    private String reason;

    public static AiModerationResult pass() {
        AiModerationResult result = new AiModerationResult();
        result.setPass(true);
        result.setReason("");
        return result;
    }

    public static AiModerationResult block(String reason) {
        AiModerationResult result = new AiModerationResult();
        result.setPass(false);
        result.setReason(reason == null ? "" : reason);
        return result;
    }
}
