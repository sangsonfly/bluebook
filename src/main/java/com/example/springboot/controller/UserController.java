package com.example.springboot.controller;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.springboot.common.Constants;
import com.example.springboot.common.Result;
import com.example.springboot.entity.Account;
import com.example.springboot.entity.User;
import com.example.springboot.entity.dto.UserVerifyApplyDTO;
import com.example.springboot.entity.dto.UserVerifyReviewDTO;
import com.example.springboot.exception.ServiceException;
import com.example.springboot.service.IUserService;
import com.example.springboot.utils.TokenUtils;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 前端控制器
 * </p>
 */
@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    private IUserService userService;

    @PostMapping
    public Result save(@RequestBody User user) {
        Account current = TokenUtils.getCurrentUser();
        boolean admin = current != null && "ROLE_ADMIN".equals(current.getRole());
        if (!admin) {
            user.setIsVerified(null);
            user.setVerifyTime(null);
            user.setStudentId(null);
            user.setRealName(null);
            user.setSchool(null);
            user.setCollege(null);
            user.setMajor(null);
            user.setGrade(null);
        }
        return Result.success(userService.saveOrUpdate(user));
    }

    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Integer id) {
        return Result.success(userService.removeById(id));
    }

    @PostMapping("/del/batch")
    public Result deleteBatch(@RequestBody List<Integer> ids) {
        return Result.success(userService.removeByIds(ids));
    }

    @GetMapping
    public Result findAll() {
        return Result.success(userService.list());
    }

    @GetMapping("/{id}")
    public Result findOne(@PathVariable Integer id) {
        User user = userService.getUserWithUpdatedStats(id);
        return Result.success(user);
    }

    @GetMapping("/page")
    public Result findPage(@RequestParam Integer pageNum,
                           @RequestParam Integer pageSize,
                           @RequestParam(defaultValue = "") String keyword) {

        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByDesc(User::getId);

        if (StrUtil.isNotBlank(keyword)) {
            queryWrapper.like(User::getNickname, keyword);
        }

        return Result.success(userService.page(new Page<>(pageNum, pageSize), queryWrapper));
    }

    @GetMapping("/manage/page")
    public Result findManagePage(@RequestParam Integer pageNum,
                                 @RequestParam Integer pageSize,
                                 @RequestParam(defaultValue = "") String keyword,
                                 @RequestParam(required = false) Integer status) {
        return Result.success(userService.getManagePage(pageNum, pageSize, keyword, status));
    }

    @PostMapping("/reset-password")
    public Result resetPassword(@RequestBody Map<String, Object> params) {
        Integer userId = params.get("userId") == null ? null : Integer.valueOf(params.get("userId").toString());
        String newPassword = params.get("newPassword") == null ? "" : params.get("newPassword").toString();
        userService.resetPassword(userId, newPassword);
        return Result.success();
    }

    @PostMapping("/verify/apply")
    public Result applyVerification(@RequestBody UserVerifyApplyDTO dto) {
        Account current = TokenUtils.getCurrentUser();
        if (current == null || current.getId() == null || !"ROLE_USER".equals(current.getRole())) {
            throw new ServiceException(Constants.CODE_401, "仅登录用户可提交认证");
        }
        userService.applyVerification(current.getId(), dto);
        return Result.success();
    }

    @GetMapping("/verify/page")
    public Result getVerifyPage(@RequestParam Integer pageNum,
                                @RequestParam Integer pageSize,
                                @RequestParam(defaultValue = "") String keyword,
                                @RequestParam(required = false) Integer status) {
        Account current = TokenUtils.getCurrentUser();
        if (current == null || current.getId() == null || !"ROLE_ADMIN".equals(current.getRole())) {
            throw new ServiceException(Constants.CODE_401, "仅管理员可查看认证审核列表");
        }
        return Result.success(userService.getVerifyPage(pageNum, pageSize, keyword, status));
    }

    @PostMapping("/verify/review")
    public Result reviewVerification(@RequestBody UserVerifyReviewDTO dto) {
        Account current = TokenUtils.getCurrentUser();
        if (current == null || current.getId() == null || !"ROLE_ADMIN".equals(current.getRole())) {
            throw new ServiceException(Constants.CODE_401, "仅管理员可审核认证");
        }
        if (dto == null || dto.getUserId() == null || dto.getApproved() == null) {
            throw new ServiceException(Constants.CODE_400, "参数错误");
        }
        userService.reviewVerification(dto.getUserId(), dto.getApproved());
        return Result.success();
    }

}

