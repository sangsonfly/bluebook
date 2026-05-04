package com.example.springboot.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.springboot.entity.UserFollow;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 用户关注Mapper
 */
@Mapper
public interface UserFollowMapper extends BaseMapper<UserFollow> {
    
    /**
     * 查询两个用户之间的关注关系（仅活跃状态）
     */
    @Select("SELECT * FROM user_follow WHERE follower_id = #{followerId} AND followee_id = #{followeeId} AND status = 1")
    UserFollow getFollowRelation(Integer followerId, Integer followeeId);
    
    /**
     * 查询两个用户之间的关注记录（包括已取消的）
     */
    @Select("SELECT * FROM user_follow WHERE follower_id = #{followerId} AND followee_id = #{followeeId}")
    UserFollow getFollowRecord(Integer followerId, Integer followeeId);
    
    /**
     * 统计用户的关注数
     */
    @Select("SELECT COUNT(*) FROM user_follow WHERE follower_id = #{userId} AND status = 1")
    Integer countFollowing(Integer userId);
    
    /**
     * 统计用户的粉丝数
     */
    @Select("SELECT COUNT(*) FROM user_follow WHERE followee_id = #{userId} AND status = 1")
    Integer countFollowers(Integer userId);
    
    /**
     * 获取用户关注的所有用户ID列表
     */
    @Select("SELECT followee_id FROM user_follow WHERE follower_id = #{userId} AND status = 1")
    List<Integer> getFollowingUserIds(@Param("userId") Integer userId);
}

