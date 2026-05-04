package com.example.springboot.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.springboot.entity.Activity;
import com.example.springboot.mapper.ActivityMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 活动服务
 * 实现活动发布、查询、管理等功能
 */
@Service
public class ActivityService extends ServiceImpl<ActivityMapper, Activity> {
    
    @Autowired
    private IClubService clubService;
    
    /**
     * 创建活动
     */
    @Transactional
    public boolean createActivity(Activity activity) {
        // 设置初始状态
        activity.setStatus(Activity.STATUS_REGISTERING);
        activity.setCurrentParticipants(0);
        
        boolean success = this.save(activity);
        
        if (success) {
            // 更新社团的活动数
            clubService.incrementActivityCount(activity.getClubId());
        }
        
        return success;
    }
    
    /**
     * 获取社团的活动列表
     */
    public List<Activity> getClubActivities(Integer clubId) {
        return this.baseMapper.getByClubId(clubId);
    }
    
    /**
     * 获取进行中的活动
     */
    public List<Activity> getActiveActivities() {
        return this.baseMapper.getActiveActivities();
    }
    
    /**
     * 分页查询活动
     */
    public IPage<Activity> getActivitiesPage(Page<Activity> page, String category, Integer status) {
        QueryWrapper<Activity> wrapper = new QueryWrapper<>();
        
        if (category != null && !category.isEmpty()) {
            wrapper.like("tags", category);
        }
        
        if (status != null) {
            wrapper.eq("status", status);
        }
        
        wrapper.orderByDesc("create_time");
        return this.page(page, wrapper);
    }
    
    /**
     * 更新活动状态
     */
    public boolean updateActivityStatus(Integer activityId, Integer status) {
        Activity activity = this.getById(activityId);
        if (activity != null) {
            activity.setStatus(status);
            return this.updateById(activity);
        }
        return false;
    }
    
    /**
     * 增加报名人数
     */
    public void incrementParticipants(Integer activityId) {
        Activity activity = this.getById(activityId);
        if (activity != null) {
            activity.setCurrentParticipants(activity.getCurrentParticipants() + 1);
            this.updateById(activity);
        }
    }
    
    /**
     * 减少报名人数
     */
    public void decrementParticipants(Integer activityId) {
        Activity activity = this.getById(activityId);
        if (activity != null && activity.getCurrentParticipants() > 0) {
            activity.setCurrentParticipants(activity.getCurrentParticipants() - 1);
            this.updateById(activity);
        }
    }
    
    /**
     * 检查活动是否已满员
     */
    public boolean isFull(Integer activityId) {
        Activity activity = this.getById(activityId);
        if (activity == null || activity.getMaxParticipants() == null) {
            return false;
        }
        return activity.getCurrentParticipants() >= activity.getMaxParticipants();
    }
    
    /**
     * 检查活动是否可以报名
     */
    public boolean canRegister(Integer activityId) {
        return getRegisterBlockedReason(activityId) == null;
    }

    /**
     * 获取活动不可报名原因，返回 null 表示可报名
     */
    public String getRegisterBlockedReason(Integer activityId) {
        Activity activity = this.getById(activityId);
        if (activity == null) {
            return "活动不存在";
        }

        // 状态必须是报名中
        if (activity.getStatus() != Activity.STATUS_REGISTERING) {
            return "该活动当前不在报名阶段";
        }

        // 检查时间
        if (activity.getStartTime() == null) {
            return "活动开始时间未设置";
        }
        if (!activity.getStartTime().isAfter(LocalDateTime.now())) {
            return "活动已开始，无法报名";
        }

        // 检查人数
        if (isFull(activityId)) {
            return "活动名额已满";
        }
        return null;
    }
}

