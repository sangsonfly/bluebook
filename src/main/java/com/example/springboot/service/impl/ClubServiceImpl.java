package com.example.springboot.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.springboot.entity.Club;
import com.example.springboot.entity.ClubMember;
import com.example.springboot.entity.User;
import com.example.springboot.entity.vo.ClubAdminVO;
import com.example.springboot.mapper.ClubMemberMapper;
import com.example.springboot.mapper.ClubMapper;
import com.example.springboot.service.IClubService;
import com.example.springboot.service.IUserService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 社团Service实现类
 */
@Service
public class ClubServiceImpl extends ServiceImpl<ClubMapper, Club> implements IClubService {

    @Resource
    private ClubMemberMapper clubMemberMapper;

    @Resource
    private IUserService userService;

    @Override
    public void incrementActivityCount(Integer clubId) {
        Club club = getById(clubId);
        if (club != null) {
            club.setActivityCount(club.getActivityCount() == null ? 1 : club.getActivityCount() + 1);
            updateById(club);
        }
    }
    
    @Override
    public void decrementActivityCount(Integer clubId) {
        Club club = getById(clubId);
        if (club != null && club.getActivityCount() != null && club.getActivityCount() > 0) {
            club.setActivityCount(club.getActivityCount() - 1);
            updateById(club);
        }
    }
    
    @Override
    public void incrementMemberCount(Integer clubId) {
        Club club = getById(clubId);
        if (club != null) {
            club.setMemberCount(club.getMemberCount() == null ? 1 : club.getMemberCount() + 1);
            updateById(club);
        }
    }
    
    @Override
    public void decrementMemberCount(Integer clubId) {
        Club club = getById(clubId);
        if (club != null && club.getMemberCount() != null && club.getMemberCount() > 0) {
            club.setMemberCount(club.getMemberCount() - 1);
            updateById(club);
        }
    }
    
    @Override
    public boolean applyVerification(Integer clubId) {
        Club club = getById(clubId);
        if (club == null) {
            throw new RuntimeException("社团不存在");
        }
        
        if (club.getIsVerified() == 1) {
            throw new RuntimeException("社团已认证");
        }
        
        // 这里可以添加申请认证的逻辑，比如提交认证材料等
        // 当前简化处理：直接更新状态等待管理员审核
        
        return true;
    }
    
    @Override
    public boolean reviewVerification(Integer clubId, boolean approved) {
        Club club = getById(clubId);
        if (club == null) {
            throw new RuntimeException("社团不存在");
        }
        
        if (approved) {
            club.setIsVerified(1);
            club.setVerifyTime(LocalDateTime.now());
        } else {
            club.setIsVerified(0);
            club.setVerifyTime(null);
        }
        
        return updateById(club);
    }

    @Override
    public IPage<ClubAdminVO> getAdminPage(Integer pageNum, Integer pageSize, String keyword, String category, Integer isVerified) {
        Page<Club> page = new Page<>(pageNum, pageSize);
        QueryWrapper<Club> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc("id");

        if (StrUtil.isNotBlank(keyword)) {
            queryWrapper.and(wrapper -> wrapper.like("name", keyword).or().like("description", keyword));
        }
        if (StrUtil.isNotBlank(category)) {
            queryWrapper.eq("category", category);
        }
        if (isVerified != null) {
            queryWrapper.eq("is_verified", isVerified);
        }

        Page<Club> clubPage = page(page, queryWrapper);
        List<Club> clubs = clubPage.getRecords();
        if (clubs == null || clubs.isEmpty()) {
            return new Page<>(pageNum, pageSize, 0);
        }

        List<Integer> clubIds = clubs.stream().map(Club::getId).collect(Collectors.toList());
        QueryWrapper<ClubMember> memberWrapper = new QueryWrapper<>();
        memberWrapper.in("club_id", clubIds)
                .eq("status", 1)
                .in("role", ClubMember.ROLE_ADMIN, ClubMember.ROLE_PRESIDENT);
        List<ClubMember> memberRoles = clubMemberMapper.selectList(memberWrapper);

        Set<Integer> userIds = memberRoles.stream().map(ClubMember::getUserId).collect(Collectors.toSet());
        Map<Integer, User> userMap;
        if (userIds.isEmpty()) {
            userMap = Collections.emptyMap();
        } else {
            userMap = userService.listByIds(userIds).stream()
                    .collect(Collectors.toMap(User::getId, user -> user));
        }

        Map<Integer, ClubMember> presidentMap = new HashMap<>();
        Map<Integer, List<ClubMember>> adminsMap = new HashMap<>();

        for (ClubMember member : memberRoles) {
            if (member.getRole() == ClubMember.ROLE_PRESIDENT) {
                presidentMap.put(member.getClubId(), member);
            } else if (member.getRole() == ClubMember.ROLE_ADMIN) {
                adminsMap.computeIfAbsent(member.getClubId(), k -> new ArrayList<>()).add(member);
            }
        }

        List<ClubAdminVO> records = clubs.stream().map(club -> {
            ClubAdminVO vo = new ClubAdminVO();
            BeanUtils.copyProperties(club, vo);

            ClubMember president = presidentMap.get(club.getId());
            if (president != null) {
                vo.setPresidentUserId(president.getUserId());
                User presidentUser = userMap.get(president.getUserId());
                if (presidentUser != null) {
                    vo.setPresidentName(presidentUser.getNickname());
                    vo.setPresidentAvatar(presidentUser.getAvatarUrl());
                }
            }

            List<ClubMember> admins = adminsMap.getOrDefault(club.getId(), Collections.emptyList());
            vo.setAdminCount(admins.size());
            List<String> adminPreview = admins.stream()
                    .map(ClubMember::getUserId)
                    .map(userMap::get)
                    .filter(Objects::nonNull)
                    .map(User::getNickname)
                    .filter(StrUtil::isNotBlank)
                    .limit(3)
                    .collect(Collectors.toList());
            vo.setAdminPreviewNames(adminPreview);
            return vo;
        }).collect(Collectors.toList());

        Page<ClubAdminVO> result = new Page<>(pageNum, pageSize, clubPage.getTotal());
        result.setRecords(records);
        return result;
    }
}

