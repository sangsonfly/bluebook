package com.example.springboot.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.springboot.entity.ClubMember;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 社团成员Mapper
 */
@Mapper
public interface ClubMemberMapper extends BaseMapper<ClubMember> {
    
    /**
     * 查询用户在社团中的成员记录
     */
    @Select("SELECT * FROM club_member WHERE club_id = #{clubId} AND user_id = #{userId} AND status = 1")
    ClubMember getMember(Integer clubId, Integer userId);
    
    /**
     * 查询社团的所有成员
     */
    @Select("SELECT * FROM club_member WHERE club_id = #{clubId} AND status = 1")
    List<ClubMember> getByClubId(Integer clubId);
    
    /**
     * 查询用户加入的所有社团
     */
    @Select("SELECT * FROM club_member WHERE user_id = #{userId} AND status = 1")
    List<ClubMember> getByUserId(Integer userId);

    /**
     * 查询社团当前社长（活跃）
     */
    @Select("SELECT * FROM club_member WHERE club_id = #{clubId} AND role = 3 AND status = 1 LIMIT 1")
    ClubMember getPresident(Integer clubId);

    /**
     * 查询社团所有社长（用于数据异常校验）
     */
    @Select("SELECT * FROM club_member WHERE club_id = #{clubId} AND role = 3 AND status = 1")
    List<ClubMember> getPresidents(Integer clubId);
}

