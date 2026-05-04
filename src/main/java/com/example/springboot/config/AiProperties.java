package com.example.springboot.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "ai")
public class AiProperties {
    private boolean enabled = false;
    private String baseUrl;
    private String apiKey;
    private String model = "deepseek-chat";
    private int timeoutMs = 10000;
    private int rateLimitPerMinute = 10;
}
