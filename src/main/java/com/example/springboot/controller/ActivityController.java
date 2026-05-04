package com.example.springboot.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.springboot.common.Constants;
import com.example.springboot.common.Result;
import com.example.springboot.config.interceptor.AuthAccess;
import com.example.springboot.entity.Account;
import com.example.springboot.entity.Activity;
import com.example.springboot.service.ActivityService;
import com.example.springboot.service.ClubMemberService;
import com.example.springboot.utils.TokenUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 活动Controller
 * 提供活动发布、查询、管理等API
 */
@RestController
@RequestMapping("/api/activity")
public class ActivityController {
    
    @Autowired
    private ActivityService activityService;

    @Autowired
    private ClubMemberService clubMemberService;
    
    /**
     * 创建活动
     */
    @PostMapping("/create")
    public Result create(@RequestBody Activity activity) {
        Account cur = TokenUtils.getCurrentUser();
        if (cur == null || cur.getId() == null) {
            return Result.error(Constants.CODE_401, "请先登录");
        }
        if (activity.getClubId() == null) {
            return Result.error("社团ID不能为空");
        }
        if (!hasClubManagePermission(cur, activity.getClubId())) {
            return Result.error(Constants.CODE_401, "无权限");
        }
        try {
            boolean success = activityService.createActivity(activity);
            return success ? Result.success(activity) : Result.error("创建失败");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 更新活动
     */
    @PutMapping("/update")
    public Result update(@RequestBody Activity activity) {
        Account cur = TokenUtils.getCurrentUser();
        if (cur == null || cur.getId() == null) {
            return Result.error(Constants.CODE_401, "请先登录");
        }
        if (activity.getId() == null) {
            return Result.error("活动ID不能为空");
        }
        Activity existing = activityService.getById(activity.getId());
        if (existing == null) {
            return Result.error("活动不存在");
        }
        if (!hasClubManagePermission(cur, existing.getClubId())) {
            return Result.error(Constants.CODE_401, "无权限");
        }
        activity.setClubId(existing.getClubId());
        boolean success = activityService.updateById(activity);
        return success ? Result.success() : Result.error("更新失败");
    }
    
    /**
     * 删除活动
     */
    @DeleteMapping("/delete/{id}")
    public Result delete(@PathVariable Integer id) {
        Account cur = TokenUtils.getCurrentUser();
        if (cur == null || cur.getId() == null) {
            return Result.error(Constants.CODE_401, "请先登录");
        }
        Activity activity = activityService.getById(id);
        if (activity == null) {
            return Result.error("活动不存在");
        }
        if (!hasClubManagePermission(cur, activity.getClubId())) {
            return Result.error(Constants.CODE_401, "无权限");
        }
        boolean success = activityService.removeById(id);
        return success ? Result.success() : Result.error("删除失败");
    }
    
    /**
     * 获取活动详情
     */
    @GetMapping("/detail/{id}")
    public Result getDetail(@PathVariable Integer id) {
        Activity activity = activityService.getById(id);
        return Result.success(activity);
    }
    
    /**
     * 获取社团的活动列表
     */
    @GetMapping("/club/{clubId}")
    @AuthAccess
    public Result getClubActivities(@PathVariable Integer clubId) {
        List<Activity> activities = activityService.getClubActivities(clubId);
        return Result.success(activities);
    }
    
    /**
     * 获取进行中的活动
     */
    @GetMapping("/active")
    public Result getActiveActivities() {
        List<Activity> activities = activityService.getActiveActivities();
        return Result.success(activities);
    }
    
    /**
     * 分页查询活动
     */
    @GetMapping("/page")
    public Result getPage(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Integer status) {
        Page<Activity> page = new Page<>(pageNum, pageSize);
        IPage<Activity> result = activityService.getActivitiesPage(page, category, status);
        return Result.success(result);
    }
    
    /**
     * 更新活动状态
     */
    @PostMapping("/updateStatus")
    public Result updateStatus(@RequestParam Integer activityId, @RequestParam Integer status) {
        Account cur = TokenUtils.getCurrentUser();
        if (cur == null || cur.getId() == null) {
            return Result.error(Constants.CODE_401, "请先登录");
        }
        Activity activity = activityService.getById(activityId);
        if (activity == null) {
            return Result.error("活动不存在");
        }
        if (!hasClubManagePermission(cur, activity.getClubId())) {
            return Result.error(Constants.CODE_401, "无权限");
        }
        boolean success = activityService.updateActivityStatus(activityId, status);
        return success ? Result.success() : Result.error("更新失败");
    }
    
    /**
     * 检查活动是否可以报名
     */
    @GetMapping("/canRegister/{activityId}")
    public Result canRegister(@PathVariable Integer activityId) {
        boolean can = activityService.canRegister(activityId);
        return Result.success(can);
    }

    private boolean hasClubManagePermission(Account cur, Integer clubId) {
        if ("ROLE_ADMIN".equals(cur.getRole())) {
            return true;
        }
        if (!"ROLE_USER".equals(cur.getRole())) {
            return false;
        }
        return clubMemberService.isAdmin(clubId, cur.getId());
    }
}

