package com.example.springboot.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.springboot.entity.Club;
import com.example.springboot.entity.vo.ClubAdminVO;
import com.baomidou.mybatisplus.core.metadata.IPage;

/**
 * 社团Service接口
 */
public interface IClubService extends IService<Club> {
    
    /**
     * 增加社团活动数量
     */
    void incrementActivityCount(Integer clubId);
    
    /**
     * 减少社团活动数量
     */
    void decrementActivityCount(Integer clubId);
    
    /**
     * 增加社团成员数量
     */
    void incrementMemberCount(Integer clubId);
    
    /**
     * 减少社团成员数量
     */
    void decrementMemberCount(Integer clubId);
    
    /**
     * 申请社团认证
     */
    boolean applyVerification(Integer clubId);
    
    /**
     * 审核社团认证
     */
    boolean reviewVerification(Integer clubId, boolean approved);

    /**
     * 后台社团管理分页（聚合社长和管理员信息）
     */
    IPage<ClubAdminVO> getAdminPage(Integer pageNum, Integer pageSize, String keyword, String category, Integer isVerified);
}

