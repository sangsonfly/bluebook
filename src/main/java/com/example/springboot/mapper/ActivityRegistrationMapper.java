package com.example.springboot.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.springboot.entity.ActivityRegistration;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 活动报名Mapper
 */
@Mapper
public interface ActivityRegistrationMapper extends BaseMapper<ActivityRegistration> {
    
    /**
     * 查询用户的报名记录
     */
    @Select("SELECT * FROM activity_registration WHERE activity_id = #{activityId} AND user_id = #{userId}")
    ActivityRegistration getByActivityAndUser(Integer activityId, Integer userId);
    
    /**
     * 查询活动的所有报名记录
     */
    @Select("SELECT * FROM activity_registration WHERE activity_id = #{activityId} ORDER BY create_time DESC")
    List<ActivityRegistration> getByActivityId(Integer activityId);
    
    /**
     * 统计活动的报名人数（已通过的）
     */
    @Select("SELECT COUNT(*) FROM activity_registration WHERE activity_id = #{activityId} AND status IN (1, 3)")
    Integer countApprovedRegistrations(Integer activityId);
}

