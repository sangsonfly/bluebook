package com.example.springboot.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.springboot.entity.Activity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 活动Mapper
 */
@Mapper
public interface ActivityMapper extends BaseMapper<Activity> {
    
    /**
     * 查询社团的活动列表
     */
    @Select("SELECT * FROM activity WHERE club_id = #{clubId} ORDER BY start_time DESC")
    List<Activity> getByClubId(Integer clubId);
    
    /**
     * 查询进行中的活动
     */
    @Select("SELECT * FROM activity WHERE status = 1 OR status = 2 ORDER BY start_time ASC")
    List<Activity> getActiveActivities();
}

