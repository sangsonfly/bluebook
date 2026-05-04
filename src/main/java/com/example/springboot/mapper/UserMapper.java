package com.example.springboot.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.springboot.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * <p>
 *  Mapper 接口
 * </p>
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
    
    /**
     * 查询用户关注的人列表
     */
    @Select("SELECT u.* FROM sys_user u " +
            "INNER JOIN user_follow uf ON u.id = uf.followee_id " +
            "WHERE uf.follower_id = #{userId} AND uf.status = 1 " +
            "ORDER BY uf.create_time DESC")
    IPage<User> getFollowingUsers(Integer userId, Page<User> page);
    
    /**
     * 查询用户的粉丝列表
     */
    @Select("SELECT u.* FROM sys_user u " +
            "INNER JOIN user_follow uf ON u.id = uf.follower_id " +
            "WHERE uf.followee_id = #{userId} AND uf.status = 1 " +
            "ORDER BY uf.create_time DESC")
    IPage<User> getFollowerUsers(Integer userId, Page<User> page);

}
