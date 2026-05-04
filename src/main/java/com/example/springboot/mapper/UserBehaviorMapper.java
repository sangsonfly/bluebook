package com.example.springboot.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.springboot.entity.UserBehavior;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 用户行为Mapper
 * 推荐算法的核心数据来源
 */
@Mapper
public interface UserBehaviorMapper extends BaseMapper<UserBehavior> {
    
    /**
     * 查询用户对笔记的行为记录
     */
    @Select("SELECT * FROM user_behavior WHERE user_id = #{userId} AND note_id = #{noteId} AND behavior_type = #{behaviorType}")
    UserBehavior selectByUserAndNote(Integer userId, Integer noteId, Integer behaviorType);
    
    /**
     * 获取用户行为最多的笔记ID列表（用于推荐）
     */
    @Select("SELECT note_id, SUM(weight) as total_weight FROM user_behavior " +
            "WHERE user_id = #{userId} " +
            "GROUP BY note_id " +
            "ORDER BY total_weight DESC " +
            "LIMIT #{limit}")
    List<Map<String, Object>> getUserTopNotes(Integer userId, Integer limit);
    
    /**
     * 获取与指定用户行为相似的其他用户（协同过滤基础）
     */
    @Select("SELECT ub2.user_id, COUNT(*) as common_count " +
            "FROM user_behavior ub1 " +
            "JOIN user_behavior ub2 ON ub1.note_id = ub2.note_id " +
            "WHERE ub1.user_id = #{userId} AND ub2.user_id != #{userId} " +
            "GROUP BY ub2.user_id " +
            "ORDER BY common_count DESC " +
            "LIMIT #{limit}")
    List<Map<String, Object>> getSimilarUsers(Integer userId, Integer limit);
    
    /**
     * 获取用户的行为向量（笔记ID和权重）
     */
    @Select("SELECT note_id, SUM(weight) as total_weight " +
            "FROM user_behavior WHERE user_id = #{userId} " +
            "GROUP BY note_id")
    List<Map<String, Object>> getUserBehaviorVector(@Param("userId") Integer userId);
    
    /**
     * 获取活跃用户列表（最近30天）
     */
    @Select("SELECT DISTINCT user_id FROM user_behavior " +
            "WHERE create_time >= DATE_SUB(NOW(), INTERVAL 30 DAY) " +
            "LIMIT #{limit}")
    List<Integer> getActiveUserIds(@Param("limit") Integer limit);
}

