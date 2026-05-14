package com.example.springboot.service.impl;

import com.example.springboot.config.AiProperties;
import com.example.springboot.entity.dto.AiModerationResult;
import com.example.springboot.service.AiModerationService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AiModerationServiceImpl implements AiModerationService {
    private static final Logger log = LoggerFactory.getLogger(AiModerationServiceImpl.class);

    private final HttpClient httpClient = HttpClient.newBuilder().build();

    @Resource
    private AiProperties aiProperties;

    @Resource
    private ObjectMapper objectMapper;

    @Override
    public AiModerationResult check(String title, String content) {
        validateInput(title, content);
        validateConfig();
        long start = System.currentTimeMillis();
        try {
            String endpoint = normalizeEndpoint(aiProperties.getBaseUrl());
            String payload = buildPayload(title, content);
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(endpoint))
                    .timeout(Duration.ofMillis(aiProperties.getTimeoutMs()))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + aiProperties.getApiKey())
                    .POST(HttpRequest.BodyPublishers.ofString(payload))
                    .build();

            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            long cost = System.currentTimeMillis() - start;
            log.info("AI moderation completed, status={}, cost={}ms", response.statusCode(), cost);
            if (response.statusCode() >= 400) {
                throw new RuntimeException("AI provider returned status " + response.statusCode());
            }
            return parseResponse(response.body());
        } catch (Exception e) {
            log.warn("AI moderation failed: {}", e.getMessage());
            throw new RuntimeException("AI 审核服务暂不可用");
        }
    }

    private void validateInput(String title, String content) {
        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("请先输入内容");
        }
        if (content.length() > 1000) {
            throw new IllegalArgumentException("内容不能超过1000字");
        }
        if (title != null && title.length() > 20) {
            throw new IllegalArgumentException("标题不能超过20字");
        }
    }

    private void validateConfig() {
        if (!aiProperties.isEnabled()) {
            throw new IllegalArgumentException("当前环境未开启 AI 服务");
        }
        if (aiProperties.getApiKey() == null || aiProperties.getApiKey().trim().isEmpty()) {
            throw new IllegalArgumentException("AI 服务配置不完整");
        }
        if (aiProperties.getBaseUrl() == null || aiProperties.getBaseUrl().trim().isEmpty()) {
            throw new IllegalArgumentException("AI 服务地址未配置");
        }
    }

    private String buildPayload(String title, String content) throws Exception {
        Map<String, Object> payload = new HashMap<>();
        payload.put("model", aiProperties.getModel());
        payload.put("temperature", 0.2);
        payload.put("response_format", Map.of("type", "json_object"));

        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(Map.of(
                "role", "system",
                "content", "你是校园社区内容安全审核助手。请严格根据用户输入判断是否可发布，只输出JSON对象，格式为：{\"result\":\"PASS|BLOCK\",\"reason\":\"简短原因\"}。【拦截标准】仅当内容涉嫌违法违规、色情低俗、垃圾广告（商业推广/刷屏广告）、诈骗导流（引导加微信/QQ/群/外链进行诈骗）、人身攻击（辱骂/骚扰）或明显不适合校园社区时返回BLOCK。【重要豁免】1. 校园二手物品买卖（出闲置/求购/转让/免费赠送/拼单等）属于正常校园行为，即使包含价格数字、交易方式（面交/自取）、“私聊”“私信”等，也必须判定为PASS。2. 正常语境下提到“私信”“私聊”“站内联系”不违规，仅当明确引导加微信/QQ/群或打开外部链接进行站外导流时才需关注。3. “金钱交易诱导”特指刷单/传销/非法集资/赌博/代购牟利等违法行为，不包括同学间正常二手物品买卖。"
        ));
        messages.add(Map.of(
                "role", "user",
                "content", "请审核以下内容是否可在校园社区发布。\n标题: " + safe(title) + "\n正文: " + safe(content)
        ));
        payload.put("messages", messages);
        return objectMapper.writeValueAsString(payload);
    }

    private AiModerationResult parseResponse(String responseBody) throws Exception {
        JsonNode root = objectMapper.readTree(responseBody);
        JsonNode contentNode = root.path("choices").path(0).path("message").path("content");
        if (contentNode.isMissingNode() || contentNode.asText().trim().isEmpty()) {
            return AiModerationResult.block("内容审核结果异常");
        }
        String raw = contentNode.asText().trim();
        String jsonText = extractJson(raw);
        JsonNode aiJson = objectMapper.readTree(jsonText);
        String result = aiJson.path("result").asText("").trim().toUpperCase();
        String reason = aiJson.path("reason").asText("内容可能涉及违规").trim();
        if ("PASS".equals(result)) {
            return AiModerationResult.pass();
        }
        return AiModerationResult.block(reason.isEmpty() ? "内容可能涉及违规" : reason);
    }

    private String extractJson(String text) {
        String trimmed = text.trim();
        if (trimmed.startsWith("```")) {
            int firstBrace = trimmed.indexOf('{');
            int lastBrace = trimmed.lastIndexOf('}');
            if (firstBrace >= 0 && lastBrace > firstBrace) {
                return trimmed.substring(firstBrace, lastBrace + 1);
            }
        }
        return trimmed;
    }

    private String normalizeEndpoint(String baseUrl) {
        String normalized = baseUrl.trim();
        if (normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        if (!normalized.endsWith("/chat/completions")) {
            normalized = normalized + "/chat/completions";
        }
        return normalized;
    }

    private String safe(String text) {
        return text == null ? "" : text.trim();
    }
}
