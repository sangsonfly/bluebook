package com.example.springboot.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.springboot.common.Constants;
import com.example.springboot.common.Result;
import com.example.springboot.entity.Account;
import com.example.springboot.entity.Activity;
import com.example.springboot.entity.ActivityRegistration;
import com.example.springboot.entity.User;
import com.example.springboot.entity.vo.ActivityRegistrationVO;
import com.example.springboot.service.ActivityService;
import com.example.springboot.service.ActivityRegistrationService;
import com.example.springboot.service.ClubMemberService;
import com.example.springboot.service.IUserService;
import com.example.springboot.utils.TokenUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 活动报名Controller
 * 提供报名、取消报名、审核等API
 */
@RestController
@RequestMapping("/api/activityRegistration")
public class ActivityRegistrationController {
    
    @Autowired
    private ActivityRegistrationService registrationService;

    @Autowired
    private ActivityService activityService;

    @Autowired
    private ClubMemberService clubMemberService;

    @Autowired
    private IUserService userService;
    
    /**
     * 报名活动
     */
    @PostMapping("/register")
    public Result register(
            @RequestParam Integer activityId,
            @RequestParam(required = false) String remark) {
        Account cur = TokenUtils.getCurrentUser();
        if (cur == null || cur.getId() == null || !"ROLE_USER".equals(cur.getRole())) {
            return Result.error(Constants.CODE_401, "请先登录");
        }
        Activity activity = activityService.getById(activityId);
        if (activity == null) {
            return Result.error("活动不存在");
        }
        if (!clubMemberService.isMember(activity.getClubId(), cur.getId())) {
            return Result.error("尚未加入该社团，无法报名");
        }
        try {
            boolean success = registrationService.register(activityId, cur.getId(), remark);
            return success ? Result.success() : Result.error("报名失败");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 取消报名
     */
    @PostMapping("/cancel")
    public Result cancel(@RequestParam Integer activityId) {
        Account cur = TokenUtils.getCurrentUser();
        if (cur == null || cur.getId() == null || !"ROLE_USER".equals(cur.getRole())) {
            return Result.error(Constants.CODE_401, "请先登录");
        }
        try {
            boolean success = registrationService.cancel(activityId, cur.getId());
            return success ? Result.success() : Result.error("取消失败");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 审核报名（社团管理员）
     */
    @PostMapping("/review")
    public Result review(
            @RequestParam Long registrationId,
            @RequestParam Integer status,
            @RequestParam(required = false) String reviewRemark) {
        Account cur = TokenUtils.getCurrentUser();
        if (cur == null || cur.getId() == null) {
            return Result.error(Constants.CODE_401, "请先登录");
        }
        ActivityRegistration registration = registrationService.getById(registrationId);
        if (registration == null) {
            return Result.error("未找到报名记录");
        }
        Activity activity = activityService.getById(registration.getActivityId());
        if (activity == null) {
            return Result.error("活动不存在");
        }
        if (!hasManagePermission(cur, activity.getClubId())) {
            return Result.error(Constants.CODE_401, "无权限");
        }
        try {
            boolean success = registrationService.review(registrationId, status, reviewRemark);
            return success ? Result.success() : Result.error("审核失败");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 签到
     */
    @PostMapping("/checkIn")
    public Result checkIn(@RequestParam Long registrationId) {
        Account cur = TokenUtils.getCurrentUser();
        if (cur == null || cur.getId() == null) {
            return Result.error(Constants.CODE_401, "请先登录");
        }
        ActivityRegistration registration = registrationService.getById(registrationId);
        if (registration == null) {
            return Result.error("未找到报名记录");
        }
        Activity activity = activityService.getById(registration.getActivityId());
        if (activity == null) {
            return Result.error("活动不存在");
        }
        if (!hasManagePermission(cur, activity.getClubId())) {
            return Result.error(Constants.CODE_401, "无权限");
        }
        try {
            boolean success = registrationService.checkIn(registrationId);
            return success ? Result.success() : Result.error("签到失败");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 获取活动的报名列表
     */
    @GetMapping("/activity/{activityId}")
    public Result getActivityRegistrations(
            @PathVariable Integer activityId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        Account cur = TokenUtils.getCurrentUser();
        if (cur == null || cur.getId() == null) {
            return Result.error(Constants.CODE_401, "请先登录");
        }
        Activity activity = activityService.getById(activityId);
        if (activity == null) {
            return Result.error("活动不存在");
        }
        if (!hasManagePermission(cur, activity.getClubId())) {
            return Result.error(Constants.CODE_401, "无权限");
        }
        Page<ActivityRegistration> page = new Page<>(pageNum, pageSize);
        IPage<ActivityRegistration> result = registrationService.getActivityRegistrations(activityId, page);
        return Result.success(toRegistrationVoPage(result));
    }

    /**
     * 将分页报名记录转为 VO，批量填充用户昵称、姓名、学号
     */
    private IPage<ActivityRegistrationVO> toRegistrationVoPage(IPage<ActivityRegistration> registrationPage) {
        List<ActivityRegistration> records = registrationPage.getRecords();
        if (records == null || records.isEmpty()) {
            Page<ActivityRegistrationVO> empty = new Page<>(
                    registrationPage.getCurrent(), registrationPage.getSize(), registrationPage.getTotal());
            empty.setRecords(Collections.emptyList());
            return empty;
        }
        List<Integer> userIds = records.stream()
                .map(ActivityRegistration::getUserId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        Map<Integer, User> userMap = userIds.isEmpty()
                ? Collections.emptyMap()
                : userService.listByIds(userIds).stream()
                        .collect(Collectors.toMap(User::getId, u -> u, (a, b) -> a));

        List<ActivityRegistrationVO> vos = records.stream().map(r -> {
            ActivityRegistrationVO vo = new ActivityRegistrationVO();
            BeanUtils.copyProperties(r, vo);
            User u = userMap.get(r.getUserId());
            if (u != null) {
                vo.setNickname(u.getNickname());
                vo.setRealName(u.getRealName());
                vo.setStudentId(u.getStudentId());
            }
            return vo;
        }).collect(Collectors.toList());

        Page<ActivityRegistrationVO> voPage = new Page<>(
                registrationPage.getCurrent(), registrationPage.getSize(), registrationPage.getTotal());
        voPage.setRecords(vos);
        return voPage;
    }
    
    /**
     * 获取用户的报名列表
     */
    @GetMapping("/user/{userId}")
    public Result getUserRegistrations(
            @PathVariable Integer userId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        Page<ActivityRegistration> page = new Page<>(pageNum, pageSize);
        IPage<ActivityRegistration> result = registrationService.getUserRegistrations(userId, page);
        return Result.success(result);
    }
    
    /**
     * 检查用户是否已报名
     */
    @GetMapping("/hasRegistered")
    public Result hasRegistered(@RequestParam Integer activityId) {
        Account cur = TokenUtils.getCurrentUser();
        if (cur == null || cur.getId() == null || !"ROLE_USER".equals(cur.getRole())) {
            return Result.error(Constants.CODE_401, "请先登录");
        }
        boolean has = registrationService.hasRegistered(activityId, cur.getId());
        return Result.success(has);
    }
    
    /**
     * 获取待审核的报名列表
     */
    @GetMapping("/pending/{activityId}")
    public Result getPendingRegistrations(
            @PathVariable Integer activityId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        Account cur = TokenUtils.getCurrentUser();
        if (cur == null || cur.getId() == null) {
            return Result.error(Constants.CODE_401, "请先登录");
        }
        Activity activity = activityService.getById(activityId);
        if (activity == null) {
            return Result.error("活动不存在");
        }
        if (!hasManagePermission(cur, activity.getClubId())) {
            return Result.error(Constants.CODE_401, "无权限");
        }
        Page<ActivityRegistration> page = new Page<>(pageNum, pageSize);
        IPage<ActivityRegistration> result = registrationService.getPendingRegistrations(activityId, page);
        return Result.success(result);
    }

    private boolean hasManagePermission(Account cur, Integer clubId) {
        if ("ROLE_ADMIN".equals(cur.getRole())) {
            return true;
        }
        if (!"ROLE_USER".equals(cur.getRole())) {
            return false;
        }
        return clubMemberService.isAdmin(clubId, cur.getId());
    }
}

