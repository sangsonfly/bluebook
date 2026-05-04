package com.example.springboot.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.springboot.entity.UserCollect;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 用户收藏Mapper
 */
@Mapper
public interface UserCollectMapper extends BaseMapper<UserCollect> {
    
    /**
     * 查询用户对某对象的收藏记录
     */
    @Select("SELECT * FROM user_collect WHERE user_id = #{userId} AND target_type = #{targetType} AND target_id = #{targetId}")
    UserCollect getCollectRecord(@Param("userId") Integer userId, @Param("targetType") Integer targetType, @Param("targetId") Integer targetId);
    
    /**
     * 统计某对象的收藏数
     */
    @Select("SELECT COUNT(*) FROM user_collect WHERE target_type = #{targetType} AND target_id = #{targetId}")
    Integer countCollects(@Param("targetType") Integer targetType, @Param("targetId") Integer targetId);
    
    /**
     * 获取用户的收藏夹列表
     */
    @Select("SELECT DISTINCT folder_name FROM user_collect WHERE user_id = #{userId} ORDER BY folder_name")
    List<String> getUserFolders(@Param("userId") Integer userId);
    
    /**
     * 统计收藏夹内的内容数量
     */
    @Select("SELECT COUNT(*) FROM user_collect WHERE user_id = #{userId} AND folder_name = #{folderName}")
    Integer countFolderItems(@Param("userId") Integer userId, @Param("folderName") String folderName);
    
    /**
     * 获取用户收藏的笔记ID列表
     */
    @Select("SELECT target_id FROM user_collect WHERE user_id = #{userId} AND target_type = 1 ORDER BY create_time DESC")
    List<Integer> getUserCollectedNoteIds(@Param("userId") Integer userId);
    
    /**
     * 获取收藏某笔记的用户ID列表（推荐算法使用）
     */
    @Select("SELECT user_id FROM user_collect WHERE target_type = 1 AND target_id = #{noteId}")
    List<Integer> getUsersWhoCollectedNote(@Param("noteId") Integer noteId);
}

