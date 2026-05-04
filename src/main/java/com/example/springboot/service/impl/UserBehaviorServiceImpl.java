package com.example.springboot.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.springboot.entity.UserBehavior;
import com.example.springboot.mapper.UserBehaviorMapper;
import com.example.springboot.service.IRecommendationService;
import com.example.springboot.service.IUserBehaviorService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 用户行为Service实现类
 */
@Service
public class UserBehaviorServiceImpl extends ServiceImpl<UserBehaviorMapper, UserBehavior> implements IUserBehaviorService {
    
    @Resource
    private UserBehaviorMapper userBehaviorMapper;

    @Lazy
    @Resource
    private IRecommendationService recommendationService;
    
    @Override
    public void recordBehavior(Integer userId, Integer noteId, Integer behaviorType) {
        // 调用完整版方法，duration传null
        recordBehavior(userId, noteId, behaviorType, null);
    }
    
    @Override
    @Transactional
    public void recordBehavior(Integer userId, Integer noteId, Integer behaviorType, Integer duration) {
        // 检查是否已存在相同行为（避免重复记录点赞等）
        if (behaviorType != UserBehavior.BEHAVIOR_VIEW) {
            UserBehavior existing = userBehaviorMapper.selectByUserAndNote(userId, noteId, behaviorType);
            if (existing != null) {
                return; // 已存在，不重复记录
            }
        }
        
        // 创建新的行为记录
        UserBehavior behavior = new UserBehavior();
        behavior.setUserId(userId);
        behavior.setNoteId(noteId);
        behavior.setBehaviorType(behaviorType);
        behavior.setDuration(duration != null ? duration : 0);
        
        // 设置行为权重
        BigDecimal weight = getWeightByType(behaviorType);
        behavior.setWeight(weight);
        
        userBehaviorMapper.insert(behavior);

        // 点赞/收藏/评论/分享行为触发异步个人推荐重算，浏览行为因频率过高不触发
        if (behaviorType != UserBehavior.BEHAVIOR_VIEW) {
            recommendationService.precomputeRecommendationsForUser(userId);
        }
    }
    
    @Override
    public boolean hasBehavior(Integer userId, Integer noteId, Integer behaviorType) {
        UserBehavior behavior = userBehaviorMapper.selectByUserAndNote(userId, noteId, behaviorType);
        return behavior != null;
    }
    
    @Override
    @Transactional
    public boolean removeBehavior(Integer userId, Integer noteId, Integer behaviorType) {
        UserBehavior existing = userBehaviorMapper.selectByUserAndNote(userId, noteId, behaviorType);
        if (existing != null) {
            userBehaviorMapper.deleteById(existing.getId());
            return true;
        }
        return false;
    }
    
    @Override
    @Transactional
    public boolean toggleBehavior(Integer userId, Integer noteId, Integer behaviorType) {
        UserBehavior existing = userBehaviorMapper.selectByUserAndNote(userId, noteId, behaviorType);
        if (existing != null) {
            // 已存在，取消
            userBehaviorMapper.deleteById(existing.getId());
            return false;
        } else {
            // 不存在，添加
            UserBehavior behavior = new UserBehavior();
            behavior.setUserId(userId);
            behavior.setNoteId(noteId);
            behavior.setBehaviorType(behaviorType);
            behavior.setWeight(getWeightByType(behaviorType));
            behavior.setDuration(0);
            userBehaviorMapper.insert(behavior);
            return true;
        }
    }
    
    @Override
    public List<UserBehavior> getUserBehaviorHistory(Integer userId, Integer limit) {
        QueryWrapper<UserBehavior> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);
        queryWrapper.orderByDesc("create_time");
        queryWrapper.last("LIMIT " + limit);
        return userBehaviorMapper.selectList(queryWrapper);
    }
    
    @Override
    public Map<String, Object> getNoteBehaviorStats(Integer noteId) {
        QueryWrapper<UserBehavior> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("note_id", noteId);
        List<UserBehavior> behaviors = userBehaviorMapper.selectList(queryWrapper);
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalCount", behaviors.size());
        stats.put("viewCount", behaviors.stream().filter(b -> b.getBehaviorType() == UserBehavior.BEHAVIOR_VIEW).count());
        stats.put("likeCount", behaviors.stream().filter(b -> b.getBehaviorType() == UserBehavior.BEHAVIOR_LIKE).count());
        stats.put("collectCount", behaviors.stream().filter(b -> b.getBehaviorType() == UserBehavior.BEHAVIOR_COLLECT).count());
        stats.put("commentCount", behaviors.stream().filter(b -> b.getBehaviorType() == UserBehavior.BEHAVIOR_COMMENT).count());
        stats.put("shareCount", behaviors.stream().filter(b -> b.getBehaviorType() == UserBehavior.BEHAVIOR_SHARE).count());
        
        return stats;
    }
    
    @Override
    public List<Integer> getUserTopNoteIds(Integer userId, Integer limit) {
        List<Map<String, Object>> results = userBehaviorMapper.getUserTopNotes(userId, limit);
        return results.stream()
                .map(map -> (Integer) map.get("note_id"))
                .collect(Collectors.toList());
    }
    
    @Override
    public List<Integer> getSimilarUserIds(Integer userId, Integer limit) {
        List<Map<String, Object>> results = userBehaviorMapper.getSimilarUsers(userId, limit);
        return results.stream()
                .map(map -> (Integer) map.get("user_id"))
                .collect(Collectors.toList());
    }
    
    /**
     * 根据行为类型获取权重
     */
    private BigDecimal getWeightByType(Integer behaviorType) {
        switch (behaviorType) {
            case UserBehavior.BEHAVIOR_VIEW:
                return new BigDecimal("0.5");
            case UserBehavior.BEHAVIOR_LIKE:
                return new BigDecimal("1.0");
            case UserBehavior.BEHAVIOR_COLLECT:
                return new BigDecimal("1.5");
            case UserBehavior.BEHAVIOR_COMMENT:
                return new BigDecimal("2.0");
            case UserBehavior.BEHAVIOR_SHARE:
                return new BigDecimal("2.5");
            default:
                return new BigDecimal("1.0");
        }
    }
}

