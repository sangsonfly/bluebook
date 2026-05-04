package com.example.springboot.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 推荐算法工具类
 * 提供协同过滤算法所需的各种计算方法
 */
public class RecommendationUtil {
    
    /**
     * 计算两个用户的余弦相似度
     * 
     * @param vector1 用户1的行为向量（笔记ID -> 权重）
     * @param vector2 用户2的行为向量（笔记ID -> 权重）
     * @return 相似度值，范围[0, 1]，值越大越相似
     */
    public static double calculateCosineSimilarity(
            Map<Integer, Double> vector1, 
            Map<Integer, Double> vector2) {
        
        if (vector1 == null || vector2 == null || vector1.isEmpty() || vector2.isEmpty()) {
            return 0.0;
        }
        
        // 获取两个向量的交集（共同操作的笔记）
        Set<Integer> commonItems = new java.util.HashSet<>(vector1.keySet());
        commonItems.retainAll(vector2.keySet());
        
        if (commonItems.isEmpty()) {
            return 0.0;
        }
        
        // 计算点积（内积）
        double dotProduct = 0.0;
        for (Integer itemId : commonItems) {
            dotProduct += vector1.get(itemId) * vector2.get(itemId);
        }
        
        // 计算向量1的模长
        double norm1 = 0.0;
        for (Double value : vector1.values()) {
            norm1 += value * value;
        }
        norm1 = Math.sqrt(norm1);
        
        // 计算向量2的模长
        double norm2 = 0.0;
        for (Double value : vector2.values()) {
            norm2 += value * value;
        }
        norm2 = Math.sqrt(norm2);
        
        // 避免除零
        if (norm1 == 0.0 || norm2 == 0.0) {
            return 0.0;
        }
        
        // 余弦相似度 = (A·B) / (||A|| * ||B||)
        return dotProduct / (norm1 * norm2);
    }
    
    /**
     * 构建用户行为向量
     * 将用户的行为记录转换为向量表示（笔记ID -> 权重）
     * 
     * @param behaviors 行为记录列表，每个元素包含noteId和weight
     * @return 用户行为向量
     */
    public static Map<Integer, Double> buildUserVector(java.util.List<Map<String, Object>> behaviors) {
        Map<Integer, Double> vector = new HashMap<>();
        
        if (behaviors == null || behaviors.isEmpty()) {
            return vector;
        }
        
        for (Map<String, Object> behavior : behaviors) {
            Integer noteId = (Integer) behavior.get("note_id");
            Object weightObj = behavior.get("total_weight");
            
            double weight = 0.0;
            if (weightObj instanceof Number) {
                weight = ((Number) weightObj).doubleValue();
            } else if (weightObj instanceof java.math.BigDecimal) {
                weight = ((java.math.BigDecimal) weightObj).doubleValue();
            }
            
            if (noteId != null) {
                vector.put(noteId, weight);
            }
        }
        
        return vector;
    }
    
    /**
     * 计算推荐分数
     * 基于用户相似度和相似用户对笔记的评分
     * 
     * @param similarity 用户相似度
     * @param userRating 相似用户对笔记的评分（权重）
     * @return 推荐分数
     */
    public static double calculateRecommendationScore(double similarity, double userRating) {
        return similarity * userRating;
    }
    
    /**
     * 应用时间衰减因子
     * 新内容的权重更高
     * 
     * @param baseScore 基础分数
     * @param daysAgo 多少天前的内容
     * @param decayFactor 衰减因子（默认0.9，每天衰减10%）
     * @return 衰减后的分数
     */
    public static double applyTimeDecay(double baseScore, long daysAgo, double decayFactor) {
        if (daysAgo <= 0) {
            return baseScore;
        }
        return baseScore * Math.pow(decayFactor, daysAgo);
    }
    
    /**
     * 应用时间衰减因子（使用默认衰减因子0.9）
     */
    public static double applyTimeDecay(double baseScore, long daysAgo) {
        return applyTimeDecay(baseScore, daysAgo, 0.9);
    }
}

