package com.example.springboot.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.springboot.entity.ClubMember;
import com.example.springboot.mapper.ClubMemberMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 社团成员服务
 * 实现社团成员管理功能
 */
@Service
public class ClubMemberService extends ServiceImpl<ClubMemberMapper, ClubMember> {
    
    @Autowired
    private IClubService clubService;
    
    /**
     * 加入社团
     */
    @Transactional
    public boolean joinClub(Integer clubId, Integer userId, Integer role) {
        // 检查是否已经是成员
        ClubMember existing = this.baseMapper.getMember(clubId, userId);
        if (existing != null) {
            if (existing.getStatus() == 1) {
                throw new RuntimeException("已经是社团成员");
            } else {
                // 之前退出过，重新加入
                existing.setStatus(1);
                existing.setRole(role == null ? ClubMember.ROLE_MEMBER : role);
                boolean success = this.updateById(existing);
                if (success) {
                    clubService.incrementMemberCount(clubId);
                }
                return success;
            }
        }
        
        // 创建新成员记录
        ClubMember member = new ClubMember();
        member.setClubId(clubId);
        member.setUserId(userId);
        member.setRole(role == null ? ClubMember.ROLE_MEMBER : role);
        member.setStatus(1);
        
        boolean success = this.save(member);
        if (success) {
            // 增加社团成员数
            clubService.incrementMemberCount(clubId);
        }
        
        return success;
    }
    
    /**
     * 退出社团
     */
    @Transactional
    public boolean leaveClub(Integer clubId, Integer userId) {
        ClubMember member = this.baseMapper.getMember(clubId, userId);
        if (member == null || member.getStatus() == 0) {
            throw new RuntimeException("不是社团成员");
        }
        
        // 社长不能退出
        if (member.getRole() == ClubMember.ROLE_PRESIDENT) {
            throw new RuntimeException("社长不能退出社团");
        }
        
        member.setStatus(0);
        boolean success = this.updateById(member);
        
        if (success) {
            // 减少社团成员数
            clubService.decrementMemberCount(clubId);
        }
        
        return success;
    }
    
    /**
     * 设置成员角色
     */
    public boolean setMemberRole(Integer clubId, Integer userId, Integer role) {
        ClubMember member = this.baseMapper.getMember(clubId, userId);
        if (member == null) {
            throw new RuntimeException("不是社团成员");
        }

        if (role == null || (role != ClubMember.ROLE_MEMBER && role != ClubMember.ROLE_ADMIN)) {
            throw new RuntimeException("仅支持设置为成员或管理员");
        }

        // 社长角色变更必须走 setPresident 专用流程，避免社长被直接降级
        if (member.getRole() != null && member.getRole() == ClubMember.ROLE_PRESIDENT) {
            throw new RuntimeException("社长角色不可直接修改，请使用设置社长接口");
        }

        if (member.getRole() != null && member.getRole().equals(role)) {
            return true;
        }

        member.setRole(role);
        return this.updateById(member);
    }

    /**
     * 设置社长（平台管理员专用）
     * 保证单社唯一社长：旧社长自动降为管理员。
     */
    @Transactional
    public boolean setPresident(Integer clubId, Integer userId) {
        ClubMember target = this.baseMapper.getMember(clubId, userId);
        if (target == null) {
            throw new RuntimeException("目标用户不是该社团成员");
        }

        List<ClubMember> presidents = this.baseMapper.getPresidents(clubId);
        if (presidents.size() > 1) {
            throw new RuntimeException("社团角色数据异常，请联系管理员");
        }

        ClubMember currentPresident = presidents.isEmpty() ? null : presidents.get(0);
        if (currentPresident != null && currentPresident.getUserId().equals(userId)) {
            return true;
        }

        if (currentPresident != null) {
            currentPresident.setRole(ClubMember.ROLE_ADMIN);
            this.updateById(currentPresident);
        }

        target.setRole(ClubMember.ROLE_PRESIDENT);
        return this.updateById(target);
    }
    
    /**
     * 获取社团成员列表
     */
    public IPage<ClubMember> getClubMembers(Integer clubId, Page<ClubMember> page) {
        QueryWrapper<ClubMember> wrapper = new QueryWrapper<>();
        wrapper.eq("club_id", clubId)
               .eq("status", 1)
               .orderByDesc("role")
               .orderByAsc("join_time");
        return this.page(page, wrapper);
    }
    
    /**
     * 获取用户加入的社团列表
     */
    public List<ClubMember> getUserClubs(Integer userId) {
        return this.baseMapper.getByUserId(userId);
    }
    
    /**
     * 检查是否是社团成员
     */
    public boolean isMember(Integer clubId, Integer userId) {
        ClubMember member = this.baseMapper.getMember(clubId, userId);
        return member != null && member.getStatus() != null && member.getStatus() == 1;
    }
    
    /**
     * 检查是否是社团管理员（管理员或社长）
     */
    public boolean isAdmin(Integer clubId, Integer userId) {
        ClubMember member = this.baseMapper.getMember(clubId, userId);
        return member != null && (member.getRole() == ClubMember.ROLE_ADMIN || 
                                  member.getRole() == ClubMember.ROLE_PRESIDENT);
    }

    /**
     * 是否为该社社长
     */
    public boolean isPresident(Integer clubId, Integer userId) {
        ClubMember member = this.baseMapper.getMember(clubId, userId);
        return member != null && member.getRole() == ClubMember.ROLE_PRESIDENT;
    }
    
    /**
     * 获取社团成员数
     */
    public Integer getMemberCount(Integer clubId) {
        QueryWrapper<ClubMember> wrapper = new QueryWrapper<>();
        wrapper.eq("club_id", clubId).eq("status", 1);
        return Math.toIntExact(this.count(wrapper));
    }
}

