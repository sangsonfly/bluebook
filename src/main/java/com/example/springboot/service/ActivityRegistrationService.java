package com.example.springboot.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.springboot.entity.Activity;
import com.example.springboot.entity.ActivityRegistration;
import com.example.springboot.mapper.ActivityRegistrationMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 活动报名服务
 * 实现活动报名、审核、签到等功能
 */
@Service
public class ActivityRegistrationService extends ServiceImpl<ActivityRegistrationMapper, ActivityRegistration> {
    
    @Autowired
    private ActivityService activityService;
    
    @Autowired
    private NotificationService notificationService;
    
    /**
     * 报名活动
     */
    @Transactional
    public boolean register(Integer activityId, Integer userId, String remark) {
        // 检查是否可以报名
        String blockedReason = activityService.getRegisterBlockedReason(activityId);
        if (blockedReason != null) {
            throw new RuntimeException(blockedReason);
        }

        // 检查是否已经报名过
        ActivityRegistration existing = this.baseMapper.getByActivityAndUser(activityId, userId);
        if (existing != null && isOccupyingRegistrationStatus(existing.getStatus())) {
            throw new RuntimeException("已经报名过该活动");
        }

        // 获取活动信息，判断是否需要审核
        Activity activity = activityService.getById(activityId);
        if (activity == null) {
            throw new RuntimeException("活动不存在");
        }

        ActivityRegistration registration = existing == null ? new ActivityRegistration() : existing;
        registration.setActivityId(activityId);
        registration.setUserId(userId);
        registration.setRemark(remark);
        registration.setReviewRemark(null);

        if (activity.getNeedApproval() == 1) {
            registration.setStatus(ActivityRegistration.STATUS_PENDING);
            registration.setReviewTime(null);
        } else {
            // 不需要审核，直接通过
            registration.setStatus(ActivityRegistration.STATUS_APPROVED);
            registration.setReviewTime(LocalDateTime.now());
            // 增加活动报名人数
            activityService.incrementParticipants(activityId);
        }

        boolean success = existing == null ? this.save(registration) : this.updateById(registration);

        if (success) {
            // 发送报名成功通知
            String message = activity.getNeedApproval() == 1 ? "报名成功，等待审核" : "报名成功";
            notificationService.sendActivityNotification(userId, activityId, message);
        }
        
        return success;
    }
    
    /**
     * 取消报名
     */
    @Transactional
    public boolean cancel(Integer activityId, Integer userId) {
        ActivityRegistration registration = this.baseMapper.getByActivityAndUser(activityId, userId);
        
        if (registration == null) {
            throw new RuntimeException("未找到报名记录");
        }
        
        if (registration.getStatus() == ActivityRegistration.STATUS_CANCELLED) {
            throw new RuntimeException("已经取消过了");
        }
        Integer oldStatus = registration.getStatus();
        // 更新状态
        registration.setStatus(ActivityRegistration.STATUS_CANCELLED);
        boolean success = this.updateById(registration);

        if (success && oldStatus == ActivityRegistration.STATUS_APPROVED) {
            // 如果之前是已通过状态，减少活动报名人数
            activityService.decrementParticipants(activityId);
        }
        
        return success;
    }
    
    /**
     * 审核报名（社团管理员）
     */
    @Transactional
    public boolean review(Long registrationId, Integer status, String reviewRemark) {
        ActivityRegistration registration = this.getById(registrationId);
        
        if (registration == null) {
            throw new RuntimeException("未找到报名记录");
        }
        
        if (registration.getStatus() != ActivityRegistration.STATUS_PENDING) {
            throw new RuntimeException("该报名记录不是待审核状态");
        }
        
        registration.setStatus(status);
        registration.setReviewRemark(reviewRemark);
        registration.setReviewTime(LocalDateTime.now());
        
        boolean success = this.updateById(registration);
        
        if (success) {
            if (status == ActivityRegistration.STATUS_APPROVED) {
                // 审核通过，增加报名人数
                activityService.incrementParticipants(registration.getActivityId());
                notificationService.sendActivityNotification(
                    registration.getUserId(), 
                    registration.getActivityId(), 
                    "报名审核通过"
                );
            } else if (status == ActivityRegistration.STATUS_REJECTED) {
                // 审核拒绝，发送通知
                notificationService.sendActivityNotification(
                    registration.getUserId(), 
                    registration.getActivityId(), 
                    "报名审核未通过：" + reviewRemark
                );
            }
        }
        
        return success;
    }
    
    /**
     * 签到
     */
    public boolean checkIn(Long registrationId) {
        ActivityRegistration registration = this.getById(registrationId);
        
        if (registration == null) {
            throw new RuntimeException("未找到报名记录");
        }
        
        if (registration.getStatus() != ActivityRegistration.STATUS_APPROVED) {
            throw new RuntimeException("只有审核通过的报名才能签到");
        }
        
        registration.setStatus(ActivityRegistration.STATUS_CHECKED_IN);
        return this.updateById(registration);
    }
    
    /**
     * 获取活动的报名列表
     */
    public IPage<ActivityRegistration> getActivityRegistrations(Integer activityId, Page<ActivityRegistration> page) {
        QueryWrapper<ActivityRegistration> wrapper = new QueryWrapper<>();
        wrapper.eq("activity_id", activityId)
               .orderByDesc("create_time");
        return this.page(page, wrapper);
    }
    
    /**
     * 获取用户的报名列表
     */
    public IPage<ActivityRegistration> getUserRegistrations(Integer userId, Page<ActivityRegistration> page) {
        QueryWrapper<ActivityRegistration> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId)
               .orderByDesc("create_time");
        return this.page(page, wrapper);
    }
    
    /**
     * 检查用户是否已报名
     */
    public boolean hasRegistered(Integer activityId, Integer userId) {
        ActivityRegistration registration = this.baseMapper.getByActivityAndUser(activityId, userId);
        return registration != null && isOccupyingRegistrationStatus(registration.getStatus());
    }
    
    /**
     * 获取待审核的报名列表
     */
    public IPage<ActivityRegistration> getPendingRegistrations(Integer activityId, Page<ActivityRegistration> page) {
        QueryWrapper<ActivityRegistration> wrapper = new QueryWrapper<>();
        wrapper.eq("activity_id", activityId)
               .eq("status", ActivityRegistration.STATUS_PENDING)
               .orderByAsc("create_time");
        return this.page(page, wrapper);
    }

    private boolean isOccupyingRegistrationStatus(Integer status) {
        return status != null && (
                status == ActivityRegistration.STATUS_PENDING
                        || status == ActivityRegistration.STATUS_APPROVED
                        || status == ActivityRegistration.STATUS_CHECKED_IN
        );
    }
}

