package com.example.springboot.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.springboot.entity.Note;
import com.example.springboot.entity.RecommendationResult;
import com.example.springboot.entity.UserBehavior;
import com.example.springboot.mapper.NoteMapper;
import com.example.springboot.mapper.RecommendationResultMapper;
import com.example.springboot.mapper.UserBehaviorMapper;
import com.example.springboot.service.IRecommendationService;
import com.example.springboot.utils.RecommendationUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 推荐服务实现类
 * 实现基于协同过滤的推荐算法
 */
@Slf4j
@Service
public class RecommendationServiceImpl implements IRecommendationService {
    
    @Resource
    private RecommendationResultMapper recommendationResultMapper;
    
    @Resource
    private UserBehaviorMapper userBehaviorMapper;
    
    @Resource
    private NoteMapper noteMapper;
    
    /** 相似用户数量 */
    private static final int SIMILAR_USER_COUNT = 20;
    
    /** 推荐结果保留的笔记数量 */
    private static final int PRECOMPUTE_LIMIT = 50;
    
    @Override
    public List<Note> getRecommendedNotes(Integer userId, Integer limit) {
        if (userId == null) {
            // 未登录用户返回热门推荐
            return getHotRecommendations(limit != null ? limit : 20);
        }
        
        // 尝试从预计算结果中获取
        RecommendationResult cachedResult = recommendationResultMapper.selectByUser(userId);
        
        List<Note> recommendedNotes = new ArrayList<>();
        
        if (cachedResult != null && cachedResult.getNoteIds() != null) {
            // 使用预计算的结果
            List<Integer> noteIds = parseNoteIds(cachedResult.getNoteIds());
            if (!noteIds.isEmpty()) {
                // 获取前16条个性推荐
                int personalLimit = (int) Math.floor(limit * 0.8);
                List<Integer> personalNoteIds = noteIds.stream()
                        .limit(personalLimit)
                        .collect(Collectors.toList());
                recommendedNotes.addAll(noteMapper.selectByIds(personalNoteIds));
            }
        } else {
            // 没有预计算结果，实时计算（性能较差，仅作为降级方案）
            log.warn("用户{}没有预计算的推荐结果，使用实时计算", userId);
            recommendedNotes = getCollaborativeFilteringRecommendations(userId, limit);
        }
        
        // 添加4条热门推荐
        int hotLimit = limit - recommendedNotes.size();
        if (hotLimit > 0) {
            List<Note> hotNotes = getHotRecommendations(hotLimit);
            // 过滤掉已经推荐的笔记
            Set<Integer> recommendedIds = recommendedNotes.stream()
                    .map(Note::getId)
                    .collect(Collectors.toSet());
            hotNotes = hotNotes.stream()
                    .filter(note -> !recommendedIds.contains(note.getId()))
                    .limit(hotLimit)
                    .collect(Collectors.toList());
            recommendedNotes.addAll(hotNotes);
        }
        
        return recommendedNotes;
    }
    
    @Override
    public List<Note> getCollaborativeFilteringRecommendations(Integer userId, Integer limit) {
        // 获取用户的行为向量
        List<Map<String, Object>> userBehaviors = userBehaviorMapper.getUserBehaviorVector(userId);
        if (userBehaviors == null || userBehaviors.isEmpty()) {
            // 新用户，返回热门推荐
            return getHotRecommendations(limit != null ? limit : 20);
        }
        
        Map<Integer, Double> userVector = RecommendationUtil.buildUserVector(userBehaviors);

        // 只排除用户明确互动过的笔记（点赞/收藏/评论/分享），浏览过的笔记仍可出现在推荐中
        Set<Integer> explicitInteractedNotes = new HashSet<>(userBehaviorMapper.getExplicitInteractionNoteIds(userId));

        // 获取相似用户
        List<Map<String, Object>> similarUsersData = userBehaviorMapper.getSimilarUsers(userId, SIMILAR_USER_COUNT);
        
        // 存储笔记推荐分数
        Map<Integer, Double> noteScores = new HashMap<>();
        
        // 为每个相似用户计算推荐分数
        for (Map<String, Object> similarUserData : similarUsersData) {
            Integer similarUserId = (Integer) similarUserData.get("user_id");
            
            // 获取相似用户的行为向量
            List<Map<String, Object>> similarUserBehaviors = userBehaviorMapper.getUserBehaviorVector(similarUserId);
            Map<Integer, Double> similarUserVector = RecommendationUtil.buildUserVector(similarUserBehaviors);
            
            // 计算相似度
            double similarity = RecommendationUtil.calculateCosineSimilarity(userVector, similarUserVector);
            
            if (similarity <= 0) {
                continue;
            }
            
            // 对相似用户喜欢的笔记进行推荐分数累加
            for (Map.Entry<Integer, Double> entry : similarUserVector.entrySet()) {
                Integer noteId = entry.getKey();
                Double rating = entry.getValue();
                
                // 跳过用户已明确互动过的笔记（点赞/收藏/评论/分享），仅浏览过的允许出现
                if (explicitInteractedNotes.contains(noteId)) {
                    continue;
                }
                
                // 计算推荐分数并累加
                double score = RecommendationUtil.calculateRecommendationScore(similarity, rating);
                noteScores.put(noteId, noteScores.getOrDefault(noteId, 0.0) + score);
            }
        }
        
        // 按分数排序，获取Top N
        List<Integer> recommendedNoteIds = noteScores.entrySet().stream()
                .sorted(Map.Entry.<Integer, Double>comparingByValue().reversed())
                .limit(limit != null ? limit : 20)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
        
        if (recommendedNoteIds.isEmpty()) {
            // 如果没有推荐结果，返回热门推荐
            return getHotRecommendations(limit != null ? limit : 20);
        }
        
        // 查询笔记详情
        return noteMapper.selectByIds(recommendedNoteIds);
    }
    
    @Override
    public List<Note> getHotRecommendations(Integer limit) {
        return noteMapper.getHotNotes(limit != null ? limit : 20);
    }
    
    @Override
    @Async
    @Transactional
    public void precomputeRecommendationsForUser(Integer userId) {
        try {
            log.info("开始为用户{}预计算推荐结果", userId);
            
            // 计算推荐结果
            List<Note> recommendations = getCollaborativeFilteringRecommendations(userId, PRECOMPUTE_LIMIT);
            
            if (recommendations.isEmpty()) {
                log.warn("用户{}没有可推荐的笔记", userId);
                return;
            }
            
            // 将笔记ID列表转换为字符串
            String noteIdsStr = recommendations.stream()
                    .map(note -> String.valueOf(note.getId()))
                    .collect(Collectors.joining(","));
            
            // 计算推荐质量评分（简单的评分：基于推荐笔记的平均互动数）
            double avgScore = recommendations.stream()
                    .mapToDouble(note -> {
                        double score = (note.getLikes() != null ? note.getLikes() * 2 : 0) +
                                      (note.getCollects() != null ? note.getCollects() * 3 : 0) +
                                      (note.getComments() != null ? note.getComments() * 1.5 : 0) +
                                      (note.getViews() != null ? note.getViews() * 0.01 : 0);
                        return score;
                    })
                    .average()
                    .orElse(0.0);
            
            // 保存或更新推荐结果
            RecommendationResult existing = recommendationResultMapper.selectByUser(userId);
            
            if (existing != null) {
                // 更新现有记录
                existing.setNoteIds(noteIdsStr);
                existing.setScore(BigDecimal.valueOf(avgScore));
                existing.setUpdateTime(LocalDateTime.now());
                recommendationResultMapper.updateById(existing);
            } else {
                // 创建新记录
                RecommendationResult result = new RecommendationResult();
                result.setUserId(userId);
                result.setNoteIds(noteIdsStr);
                result.setScore(BigDecimal.valueOf(avgScore));
                result.setCreateTime(LocalDateTime.now());
                result.setUpdateTime(LocalDateTime.now());
                recommendationResultMapper.insert(result);
            }
            
            log.info("用户{}推荐结果预计算完成，推荐{}条笔记", userId, recommendations.size());
            
        } catch (Exception e) {
            log.error("为用户{}预计算推荐结果失败", userId, e);
            throw e;
        }
    }
    
    @Override
    public void precomputeRecommendationsForAllActiveUsers() {
        log.info("开始为所有活跃用户预计算推荐结果");
        
        // 获取活跃用户列表（最多1000个）
        List<Integer> activeUserIds = userBehaviorMapper.getActiveUserIds(1000);
        
        if (activeUserIds == null || activeUserIds.isEmpty()) {
            log.warn("没有找到活跃用户");
            return;
        }
        
        log.info("找到{}个活跃用户，开始预计算", activeUserIds.size());
        
        int successCount = 0;
        int failCount = 0;
        
        for (Integer userId : activeUserIds) {
            try {
                precomputeRecommendationsForUser(userId);
                successCount++;
            } catch (Exception e) {
                log.error("为用户{}预计算推荐结果失败", userId, e);
                failCount++;
            }
        }
        
        log.info("推荐结果预计算完成，成功：{}，失败：{}", successCount, failCount);
    }
    
    /**
     * 解析笔记ID字符串为列表
     */
    private List<Integer> parseNoteIds(String noteIdsStr) {
        if (noteIdsStr == null || noteIdsStr.trim().isEmpty()) {
            return new ArrayList<>();
        }
        
        return Arrays.stream(noteIdsStr.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(Integer::parseInt)
                .collect(Collectors.toList());
    }
}

