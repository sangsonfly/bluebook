package com.example.springboot.service.impl;

import com.example.springboot.config.AiProperties;
import com.example.springboot.entity.dto.AiOptimizeRequest;
import com.example.springboot.entity.dto.AiOptimizeResponse;
import com.example.springboot.service.AiCopyService;
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
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AiCopyServiceImpl implements AiCopyService {
    private static final Logger log = LoggerFactory.getLogger(AiCopyServiceImpl.class);

    private final HttpClient httpClient = HttpClient.newBuilder().build();
    private final Map<Integer, Deque<Long>> userRequestHistory = new ConcurrentHashMap<>();

    @Resource
    private AiProperties aiProperties;

    @Resource
    private ObjectMapper objectMapper;

    @Override
    public AiOptimizeResponse optimize(AiOptimizeRequest request, Integer userId) {
        validateAndLimit(request, userId);
        long start = System.currentTimeMillis();
        String style = resolveStyle(request == null ? null : request.getStyle());
        try {
            String endpoint = normalizeEndpoint(aiProperties.getBaseUrl());
            String payload = buildPayload(request);

            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(endpoint))
                    .timeout(Duration.ofMillis(aiProperties.getTimeoutMs()))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + aiProperties.getApiKey())
                    .POST(HttpRequest.BodyPublishers.ofString(payload))
                    .build();

            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            long cost = System.currentTimeMillis() - start;
            log.info("AI optimize completed, userId={}, style={}, status={}, cost={}ms", userId, style, response.statusCode(), cost);

            if (response.statusCode() >= 400) {
                throw new RuntimeException("AI provider returned status " + response.statusCode());
            }
            return parseProviderResponse(response.body(), request);
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            log.warn("AI optimize failed, userId={}, style={}, reason={}", userId, style, e.getMessage());
            throw new RuntimeException("AI 服务暂时不可用，请稍后重试");
        }
    }

    private void validateAndLimit(AiOptimizeRequest request, Integer userId) {
        if (!aiProperties.isEnabled()) {
            throw new IllegalArgumentException("当前环境未开启 AI 服务");
        }
        if (request == null) {
            throw new IllegalArgumentException("参数错误");
        }
        if (request.getContent() == null || request.getContent().trim().isEmpty()) {
            throw new IllegalArgumentException("请先输入内容");
        }
        if (request.getContent().length() > 1000) {
            throw new IllegalArgumentException("内容不能超过1000字");
        }
        if (request.getTitle() != null && request.getTitle().length() > 20) {
            throw new IllegalArgumentException("标题不能超过20字");
        }
        if (aiProperties.getApiKey() == null || aiProperties.getApiKey().trim().isEmpty()) {
            throw new IllegalArgumentException("AI 服务配置不完整，请联系管理员");
        }
        if (aiProperties.getBaseUrl() == null || aiProperties.getBaseUrl().trim().isEmpty()) {
            throw new IllegalArgumentException("AI 服务地址未配置，请联系管理员");
        }
        applyRateLimit(userId);
    }

    private void applyRateLimit(Integer userId) {
        if (userId == null || aiProperties.getRateLimitPerMinute() <= 0) {
            return;
        }
        long now = System.currentTimeMillis();
        long windowStart = now - 60_000L;
        Deque<Long> queue = userRequestHistory.computeIfAbsent(userId, key -> new ArrayDeque<>());
        synchronized (queue) {
            while (!queue.isEmpty() && queue.peekFirst() < windowStart) {
                queue.pollFirst();
            }
            if (queue.size() >= aiProperties.getRateLimitPerMinute()) {
                throw new IllegalArgumentException("请求过于频繁，请稍后再试");
            }
            queue.offerLast(now);
        }
    }

    private String buildPayload(AiOptimizeRequest request) throws Exception {
        Map<String, Object> payload = new HashMap<>();
        payload.put("model", aiProperties.getModel());
        payload.put("temperature", 0.7);
        payload.put("response_format", Map.of("type", "json_object"));

        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(Map.of(
                "role", "system",
                "content", "你是一名校园内容文案编辑助手。任务是优化用户文案表达，不改变事实，不新增用户未提供的信息。请输出JSON对象，包含 optimizedTitle(string), optimizedContent(string), highlights(array,string最多3条)。"
        ));
        messages.add(Map.of(
                "role", "user",
                "content", buildUserPrompt(request)
        ));
        payload.put("messages", messages);
        return objectMapper.writeValueAsString(payload);
    }

    private String buildUserPrompt(AiOptimizeRequest request) {
        String style = resolveStyle(request.getStyle());
        String title = request.getTitle() == null ? "" : request.getTitle().trim();
        return "请优化以下校园笔记文案。\n"
                + "改写强度: " + style + "\n"
                + buildStyleRules(style)
                + "输入标题: " + title + "\n"
                + "输入内容: " + request.getContent().trim();
    }

    private String resolveStyle(String rawStyle) {
        if (rawStyle == null || rawStyle.trim().isEmpty()) {
            return "medium";
        }
        String normalized = rawStyle.trim().toLowerCase();
        if ("campus-friendly".equals(normalized)) {
            return "medium";
        }
        if ("strong".equals(normalized) || "medium".equals(normalized) || "light".equals(normalized)) {
            return normalized;
        }
        return "medium";
    }

    private String buildStyleRules(String style) {
        if ("strong".equals(style)) {
            return "要求:\n"
                    + "1) 使用“结论句 + 分点展开 + 明确收束”结构，重构句序与段落\n"
                    + "2) 在不新增事实前提下，表达更有张力，避免机械同义词替换\n"
                    + "3) 标题必须重写为12-16字，避免“还行/一般/不错”等弱表达\n"
                    + "4) 不编造细节，不改变事实结论，不引入用户未提供信息\n"
                    + "5) 正文长度控制在原文100%-135%\n";
        }
        if ("light".equals(style)) {
            return "要求:\n"
                    + "1) 保留原有信息顺序与语气，只做轻度润色\n"
                    + "2) 优化语句通顺度、标点与节奏，不夸张表达\n"
                    + "3) 标题可微调，最多20字\n"
                    + "4) 不新增事实，不改变核心观点\n"
                    + "5) 正文长度控制在原文90%-115%\n";
        }
        return "要求:\n"
                + "1) 先给结论，再给2-3个支撑点，最后一句给出收尾态度\n"
                + "2) 在不新增事实前提下，提升可读性与表达力度，避免同义词替换\n"
                + "3) 标题建议重写为12-16字，避免“还行/一般/不错”等弱表达\n"
                + "4) 保留事实和核心观点，不编造细节\n"
                + "5) 正文长度控制在原文95%-125%\n";
    }

    private AiOptimizeResponse parseProviderResponse(String responseBody, AiOptimizeRequest request) throws Exception {
        JsonNode root = objectMapper.readTree(responseBody);
        JsonNode contentNode = root.path("choices").path(0).path("message").path("content");
        if (contentNode.isMissingNode() || contentNode.asText().trim().isEmpty()) {
            throw new RuntimeException("AI 返回为空");
        }
        String rawContent = contentNode.asText().trim();
        String jsonText = extractJson(rawContent);
        JsonNode aiJson = objectMapper.readTree(jsonText);

        AiOptimizeResponse response = new AiOptimizeResponse();
        String optimizedTitle = aiJson.path("optimizedTitle").asText("");
        String optimizedContent = aiJson.path("optimizedContent").asText("");

        response.setOptimizedTitle(optimizedTitle.isEmpty() ? safeTitleFallback(request.getTitle()) : optimizedTitle);
        response.setOptimizedContent(optimizedContent.isEmpty() ? request.getContent() : optimizedContent);

        JsonNode highlightsNode = aiJson.path("highlights");
        if (highlightsNode.isArray()) {
            List<String> highlights = new ArrayList<>();
            for (JsonNode item : highlightsNode) {
                if (item != null && !item.asText("").trim().isEmpty()) {
                    highlights.add(item.asText().trim());
                }
                if (highlights.size() >= 3) {
                    break;
                }
            }
            response.setHighlights(highlights);
        }
        return response;
    }

    private String safeTitleFallback(String title) {
        if (title == null) {
            return "";
        }
        return title.length() > 20 ? title.substring(0, 20) : title;
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
}
