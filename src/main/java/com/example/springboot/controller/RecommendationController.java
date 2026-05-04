package com.example.springboot.controller;

import com.example.springboot.common.Result;
import com.example.springboot.config.interceptor.AuthAccess;
import com.example.springboot.entity.Account;
import com.example.springboot.entity.Note;
import com.example.springboot.service.IRecommendationService;
import com.example.springboot.utils.TokenUtils;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import java.util.List;

/**
 * 推荐Controller
 */
@RestController
@RequestMapping("/api/recommendation")
public class RecommendationController {
    
    @Resource
    private IRecommendationService recommendationService;
    
    /**
     * 获取推荐笔记（16条个性推荐 + 4条热门推荐）
     */
    @GetMapping("/notes")
    public Result getRecommendedNotes(@RequestParam(defaultValue = "20") Integer limit) {
        Account currentUser = TokenUtils.getCurrentUser();
        Integer userId = null;
        
        if (currentUser != null && currentUser.getId() != null) {
            userId = currentUser.getId();
        }
        
        List<Note> notes = recommendationService.getRecommendedNotes(userId, limit);
        return Result.success(notes);
    }
    
    /**
     * 获取热门笔记（用于新用户冷启动）
     */
    @AuthAccess
    @GetMapping("/hot")
    public Result getHotNotes(@RequestParam(defaultValue = "20") Integer limit) {
        List<Note> notes = recommendationService.getHotRecommendations(limit);
        return Result.success(notes);
    }
    
    /**
     * 手动刷新推荐（管理员功能，也可用于调试）
     */
    @PostMapping("/refresh")
    public Result refreshRecommendations() {
        recommendationService.precomputeRecommendationsForAllActiveUsers();
        return Result.success("推荐结果已刷新");
    }

    /**
     * 刷新当前登录用户自己的推荐结果
     */
    @PostMapping("/refresh/me")
    public Result refreshMyRecommendations() {
        Account currentUser = TokenUtils.getCurrentUser();
        if (currentUser == null) {
            return Result.error("请先登录");
        }
        recommendationService.precomputeRecommendationsForUser(currentUser.getId());
        return Result.success("推荐已刷新");
    }
}

