package com.example.springboot.controller;

import com.example.springboot.common.Result;
import com.example.springboot.service.IUserService;
import com.example.springboot.service.INoteService;
import com.example.springboot.service.IClubService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 统计数据控制器
 */
@RestController
@RequestMapping("/statistics")
public class StatisticsController {

    @Resource
    private IUserService userService;
    @Resource
    private INoteService noteService;
    @Resource
    private IClubService clubService;

    /**
     * 获取系统概览统计
     */
    @GetMapping("/overview")
    public Result getOverview() {
        Map<String, Object> data = new HashMap<>();
        
        // 统计总数
        data.put("totalUsers", userService.count());
        data.put("totalNotes", noteService.count());
        data.put("totalClubs", clubService.count());
        
        // 可以添加更多统计逻辑
        // data.put("todayUsers", userService.getTodayCount());
        // data.put("todayNotes", noteService.getTodayCount());
        
        return Result.success(data);
    }

    /**
     * 获取用户增长趋势
     * @param days 天数（默认7天）
     */
    @GetMapping("/user-trend")
    public Result getUserTrend(@RequestParam(defaultValue = "7") Integer days) {
        // 返回最近N天的用户增长数据
        // 这里需要根据实际数据库查询实现
        // 示例数据
        int[] trendData = new int[days];
        for (int i = 0; i < days; i++) {
            trendData[i] = (int) (Math.random() * 50) + 20;
        }
        return Result.success(trendData);
    }

    /**
     * 获取内容分类统计
     */
    @GetMapping("/category-stats")
    public Result getCategoryStats() {
        // 统计各分类的笔记数量
        // 这里需要根据实际业务逻辑实现
        Map<String, Integer> categoryStats = new HashMap<>();
        categoryStats.put("学习经验", 120);
        categoryStats.put("美食探店", 85);
        categoryStats.put("社团活动", 95);
        categoryStats.put("校园活动", 60);
        
        return Result.success(categoryStats);
    }
}

