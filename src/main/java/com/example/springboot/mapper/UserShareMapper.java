package com.example.springboot.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.springboot.entity.UserShare;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 用户分享Mapper
 */
@Mapper
public interface UserShareMapper extends BaseMapper<UserShare> {
    
    /**
     * 统计某对象的分享数
     */
    @Select("SELECT COUNT(*) FROM user_share WHERE target_type = #{targetType} AND target_id = #{targetId}")
    Integer countShares(@Param("targetType") Integer targetType, @Param("targetId") Integer targetId);
    
    /**
     * 统计用户的分享次数
     */
    @Select("SELECT COUNT(*) FROM user_share WHERE user_id = #{userId}")
    Integer countUserShares(@Param("userId") Integer userId);
    
    /**
     * 统计各平台分享数量
     */
    @Select("SELECT platform, COUNT(*) as count FROM user_share WHERE target_type = #{targetType} AND target_id = #{targetId} GROUP BY platform")
    List<Map<String, Object>> countSharesByPlatform(@Param("targetType") Integer targetType, @Param("targetId") Integer targetId);
    
    /**
     * 获取用户分享过的笔记ID列表
     */
    @Select("SELECT DISTINCT target_id FROM user_share WHERE user_id = #{userId} AND target_type = 1 ORDER BY create_time DESC")
    List<Integer> getUserSharedNoteIds(@Param("userId") Integer userId);
    
    /**
     * 获取分享某笔记的用户ID列表（推荐算法使用）
     */
    @Select("SELECT DISTINCT user_id FROM user_share WHERE target_type = 1 AND target_id = #{noteId}")
    List<Integer> getUsersWhoSharedNote(@Param("noteId") Integer noteId);
}

