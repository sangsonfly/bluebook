package com.example.springboot.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.springboot.entity.Notification;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * 消息通知Mapper
 */
@Mapper
public interface NotificationMapper extends BaseMapper<Notification> {
    
    /**
     * 统计用户未读消息数
     */
    @Select("SELECT COUNT(*) FROM notification WHERE receiver_id = #{userId} AND is_read = 0")
    Integer countUnread(Integer userId);
    
    /**
     * 标记所有消息为已读
     */
    @Update("UPDATE notification SET is_read = 1 WHERE receiver_id = #{userId} AND is_read = 0")
    Integer markAllAsRead(Integer userId);
}

