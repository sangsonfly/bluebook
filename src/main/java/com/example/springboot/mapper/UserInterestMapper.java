package com.example.springboot.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.springboot.entity.UserInterest;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.util.List;

/**
 * 用户兴趣标签Mapper
 */
@Mapper
public interface UserInterestMapper extends BaseMapper<UserInterest> {
    
    /**
     * 查询用户的所有兴趣标签
     */
    @Select("SELECT * FROM user_interest WHERE user_id = #{userId} ORDER BY weight DESC")
    List<UserInterest> getByUserId(Integer userId);
    
    /**
     * 查询用户对特定标签的兴趣
     */
    @Select("SELECT * FROM user_interest WHERE user_id = #{userId} AND tag = #{tag}")
    UserInterest getByUserIdAndTag(Integer userId, String tag);
    
    /**
     * 获取用户的兴趣标签（别名方法，与计划保持一致）
     */
    @Select("SELECT * FROM user_interest WHERE user_id = #{userId}")
    List<UserInterest> getUserInterestTags(@Param("userId") Integer userId);
    
    /**
     * 更新或插入用户兴趣标签（Upsert）
     */
    @Insert("INSERT INTO user_interest (user_id, tag, weight) VALUES (#{userId}, #{tag}, #{weight}) " +
            "ON DUPLICATE KEY UPDATE weight = weight + #{weight}, update_time = NOW()")
    void upsertUserInterest(@Param("userId") Integer userId, @Param("tag") String tag, @Param("weight") BigDecimal weight);
}

