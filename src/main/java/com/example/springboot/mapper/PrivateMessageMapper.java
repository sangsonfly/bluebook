package com.example.springboot.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.springboot.entity.PrivateMessage;
import com.example.springboot.entity.vo.PrivateSessionVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 私聊消息 Mapper
 */
@Mapper
public interface PrivateMessageMapper extends BaseMapper<PrivateMessage> {

    /**
     * 将 senderId 发给 receiverId 的消息批量标记已读
     */
    @Update("UPDATE private_message SET is_read = 1 WHERE receiver_id = #{receiverId} AND sender_id = #{senderId} AND is_read = 0")
    Integer markConversationRead(@Param("receiverId") Integer receiverId, @Param("senderId") Integer senderId);

    /**
     * 统计用户总未读私聊数
     */
    @Select("SELECT COUNT(*) FROM private_message WHERE receiver_id = #{userId} AND is_read = 0")
    Integer countUnread(@Param("userId") Integer userId);

    /**
     * 查询用户会话列表摘要（按最后消息时间倒序）
     */
    @Select("""
            SELECT
              t.target_user_id AS targetUserId,
              u.nickname AS targetNickname,
              u.avatar_url AS targetAvatarUrl,
              t.last_content AS lastContent,
              t.last_time AS lastTime,
              (
                SELECT COUNT(*)
                FROM private_message pm2
                WHERE pm2.receiver_id = #{userId}
                  AND pm2.sender_id = t.target_user_id
                  AND pm2.is_read = 0
              ) AS unreadCount
            FROM (
              SELECT
                CASE
                  WHEN sender_id = #{userId} THEN receiver_id
                  ELSE sender_id
                END AS target_user_id,
                content AS last_content,
                create_time AS last_time,
                ROW_NUMBER() OVER (
                  PARTITION BY CASE
                    WHEN sender_id = #{userId} THEN receiver_id
                    ELSE sender_id
                  END
                  ORDER BY create_time DESC, id DESC
                ) AS rn
              FROM private_message
              WHERE sender_id = #{userId} OR receiver_id = #{userId}
            ) t
            LEFT JOIN sys_user u ON u.id = t.target_user_id
            WHERE t.rn = 1
            ORDER BY t.last_time DESC
            """)
    List<PrivateSessionVO> getSessionList(@Param("userId") Integer userId);
}
