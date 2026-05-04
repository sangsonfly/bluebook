package com.example.springboot.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.springboot.entity.UserLike;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 用户点赞Mapper
 */
@Mapper
public interface UserLikeMapper extends BaseMapper<UserLike> {
    
    /**
     * 查询用户对某对象的点赞记录
     */
    @Select("SELECT * FROM user_like WHERE user_id = #{userId} AND target_type = #{targetType} AND target_id = #{targetId}")
    UserLike getLikeRecord(@Param("userId") Integer userId, @Param("targetType") Integer targetType, @Param("targetId") Integer targetId);
    
    /**
     * 统计某对象的点赞数
     */
    @Select("SELECT COUNT(*) FROM user_like WHERE target_type = #{targetType} AND target_id = #{targetId}")
    Integer countLikes(@Param("targetType") Integer targetType, @Param("targetId") Integer targetId);
    
    /**
     * 统计用户点赞的笔记数量
     */
    @Select("SELECT COUNT(*) FROM user_like WHERE user_id = #{userId} AND target_type = 1")
    Integer countUserLikedNotes(@Param("userId") Integer userId);
    
    /**
     * 获取用户点赞的笔记ID列表
     */
    @Select("SELECT target_id FROM user_like WHERE user_id = #{userId} AND target_type = 1 ORDER BY create_time DESC")
    List<Integer> getUserLikedNoteIds(@Param("userId") Integer userId);
    
    /**
     * 获取点赞某笔记的用户ID列表（推荐算法使用）
     */
    @Select("SELECT user_id FROM user_like WHERE target_type = 1 AND target_id = #{noteId}")
    List<Integer> getUsersWhoLikedNote(@Param("noteId") Integer noteId);
}

