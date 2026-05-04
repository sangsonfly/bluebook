package com.example.springboot.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.springboot.common.Constants;
import com.example.springboot.common.Result;
import com.example.springboot.entity.Account;
import com.example.springboot.entity.ClubMember;
import com.example.springboot.entity.User;
import com.example.springboot.entity.vo.ClubMemberVO;
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
 * 社团成员Controller
 * 提供社团成员管理API
 */
@RestController
@RequestMapping("/api/clubMember")
public class ClubMemberController {
    
    @Autowired
    private ClubMemberService clubMemberService;

    @Autowired
    private IUserService userService;
    
    /**
     * 加入社团
     */
    @PostMapping("/join")
    public Result joinClub(@RequestParam Integer clubId) {
        Account cur = TokenUtils.getCurrentUser();
        if (cur == null || cur.getId() == null || !"ROLE_USER".equals(cur.getRole())) {
            return Result.error(Constants.CODE_401, "请先登录");
        }
        Result verifyResult = ensureVerifiedUser(cur.getId());
        if (verifyResult != null) {
            return verifyResult;
        }
        try {
            boolean success = clubMemberService.joinClub(clubId, cur.getId(), null);
            return success ? Result.success() : Result.error("加入失败");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 退出社团
     */
    @PostMapping("/leave")
    public Result leaveClub(@RequestParam Integer clubId) {
        Account cur = TokenUtils.getCurrentUser();
        if (cur == null || cur.getId() == null || !"ROLE_USER".equals(cur.getRole())) {
            return Result.error(Constants.CODE_401, "请先登录");
        }
        try {
            boolean success = clubMemberService.leaveClub(clubId, cur.getId());
            return success ? Result.success() : Result.error("退出失败");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 设置成员角色
     */
    @PostMapping("/setRole")
    public Result setRole(
            @RequestParam Integer clubId,
            @RequestParam Integer userId,
            @RequestParam Integer role) {
        Account cur = TokenUtils.getCurrentUser();
        if (cur == null || cur.getId() == null) {
            return Result.error(Constants.CODE_401, "请先登录");
        }
        boolean platformAdmin = "ROLE_ADMIN".equals(cur.getRole());
        boolean president = "ROLE_USER".equals(cur.getRole())
                && clubMemberService.isPresident(clubId, cur.getId());
        if (!platformAdmin && !president) {
            return Result.error(Constants.CODE_401, "无权限");
        }
        if (role == null || (role != ClubMember.ROLE_MEMBER && role != ClubMember.ROLE_ADMIN)) {
            return Result.error("仅支持设置为成员或管理员，设置社长请使用专用接口");
        }
        try {
            boolean success = clubMemberService.setMemberRole(clubId, userId, role);
            return success ? Result.success() : Result.error("设置失败");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 设置社长（仅平台管理员）
     */
    @PostMapping("/setPresident")
    public Result setPresident(
            @RequestParam Integer clubId,
            @RequestParam Integer userId) {
        Account cur = TokenUtils.getCurrentUser();
        if (cur == null || cur.getId() == null) {
            return Result.error(Constants.CODE_401, "请先登录");
        }
        if (!"ROLE_ADMIN".equals(cur.getRole())) {
            return Result.error(Constants.CODE_401, "无权限");
        }
        try {
            boolean success = clubMemberService.setPresident(clubId, userId);
            return success ? Result.success() : Result.error("设置失败");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 获取社团成员列表
     */
    @GetMapping("/list/{clubId}")
    public Result getMembers(
            @PathVariable Integer clubId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        Page<ClubMember> page = new Page<>(pageNum, pageSize);
        IPage<ClubMember> result = clubMemberService.getClubMembers(clubId, page);
        return Result.success(toClubMemberVoPage(result));
    }

    /**
     * 将分页成员记录转为 VO，批量填充用户昵称、姓名、学号
     */
    private IPage<ClubMemberVO> toClubMemberVoPage(IPage<ClubMember> memberPage) {
        List<ClubMember> records = memberPage.getRecords();
        if (records == null || records.isEmpty()) {
            Page<ClubMemberVO> empty = new Page<>(memberPage.getCurrent(), memberPage.getSize(), memberPage.getTotal());
            empty.setRecords(Collections.emptyList());
            return empty;
        }
        List<Integer> userIds = records.stream()
                .map(ClubMember::getUserId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        Map<Integer, User> userMap = userIds.isEmpty()
                ? Collections.emptyMap()
                : userService.listByIds(userIds).stream()
                        .collect(Collectors.toMap(User::getId, u -> u, (a, b) -> a));

        List<ClubMemberVO> vos = records.stream().map(m -> {
            ClubMemberVO vo = new ClubMemberVO();
            BeanUtils.copyProperties(m, vo);
            User u = userMap.get(m.getUserId());
            if (u != null) {
                vo.setNickname(u.getNickname());
                vo.setRealName(u.getRealName());
                vo.setStudentId(u.getStudentId());
                vo.setAvatarUrl(u.getAvatarUrl());
            }
            return vo;
        }).collect(Collectors.toList());

        Page<ClubMemberVO> voPage = new Page<>(memberPage.getCurrent(), memberPage.getSize(), memberPage.getTotal());
        voPage.setRecords(vos);
        return voPage;
    }
    
    /**
     * 获取当前登录用户加入的社团列表
     */
    @GetMapping("/myClubs")
    public Result myClubsCurrent() {
        Account cur = TokenUtils.getCurrentUser();
        if (cur == null || cur.getId() == null || !"ROLE_USER".equals(cur.getRole())) {
            return Result.error(Constants.CODE_401, "请先登录");
        }
        List<ClubMember> clubs = clubMemberService.getUserClubs(cur.getId());
        return Result.success(clubs);
    }

    /**
     * 获取用户加入的社团列表（仅可查询本人）
     */
    @GetMapping("/myClubs/{userId}")
    public Result getUserClubs(@PathVariable Integer userId) {
        Account cur = TokenUtils.getCurrentUser();
        if (cur == null || cur.getId() == null || !"ROLE_USER".equals(cur.getRole())) {
            return Result.error(Constants.CODE_401, "请先登录");
        }
        if (!cur.getId().equals(userId)) {
            return Result.error(Constants.CODE_401, "无权限");
        }
        List<ClubMember> clubs = clubMemberService.getUserClubs(userId);
        return Result.success(clubs);
    }
    
    /**
     * 检查是否是社团成员
     */
    @GetMapping("/isMember")
    public Result isMember(@RequestParam Integer clubId, @RequestParam Integer userId) {
        Account cur = TokenUtils.getCurrentUser();
        if (cur == null || cur.getId() == null || !"ROLE_USER".equals(cur.getRole())) {
            return Result.error(Constants.CODE_401, "请先登录");
        }
        if (!cur.getId().equals(userId)) {
            return Result.error(Constants.CODE_401, "无权限");
        }
        boolean is = clubMemberService.isMember(clubId, userId);
        return Result.success(is);
    }
    
    /**
     * 检查是否是社团管理员
     */
    @GetMapping("/isAdmin")
    public Result isAdmin(@RequestParam Integer clubId, @RequestParam Integer userId) {
        Account cur = TokenUtils.getCurrentUser();
        if (cur == null || cur.getId() == null || !"ROLE_USER".equals(cur.getRole())) {
            return Result.error(Constants.CODE_401, "请先登录");
        }
        if (!cur.getId().equals(userId)) {
            return Result.error(Constants.CODE_401, "无权限");
        }
        boolean is = clubMemberService.isAdmin(clubId, userId);
        return Result.success(is);
    }
    
    /**
     * 获取社团成员数
     */
    @GetMapping("/count/{clubId}")
    public Result getMemberCount(@PathVariable Integer clubId) {
        Integer count = clubMemberService.getMemberCount(clubId);
        return Result.success(count);
    }

    private Result ensureVerifiedUser(Integer userId) {
        User user = userService.getById(userId);
        if (user == null) {
            return Result.error(Constants.CODE_401, "用户不存在");
        }
        if (user.getIsVerified() == null || user.getIsVerified() != User.VERIFY_STATUS_VERIFIED) {
            return Result.error("请先完成实名认证后再加入社团");
        }
        return null;
    }
}

