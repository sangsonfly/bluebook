package com.example.springboot.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.springboot.entity.NoteTag;
import com.example.springboot.entity.Tag;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 笔记标签关联Mapper
 */
@Mapper
public interface NoteTagMapper extends BaseMapper<NoteTag> {
    
    /**
     * 获取笔记的所有标签
     */
    @Select("SELECT t.* FROM tag t INNER JOIN note_tag nt ON t.id = nt.tag_id WHERE nt.note_id = #{noteId} AND t.status = 1")
    List<Tag> getTagsByNoteId(@Param("noteId") Integer noteId);
    
    /**
     * 获取使用某标签的笔记ID列表
     */
    @Select("SELECT note_id FROM note_tag WHERE tag_id = #{tagId}")
    List<Integer> getNoteIdsByTagId(@Param("tagId") Integer tagId);
    
    /**
     * 删除笔记的所有标签关联
     */
    @Delete("DELETE FROM note_tag WHERE note_id = #{noteId}")
    int deleteByNoteId(@Param("noteId") Integer noteId);
    
    /**
     * 删除某个标签的所有关联
     */
    @Delete("DELETE FROM note_tag WHERE tag_id = #{tagId}")
    int deleteByTagId(@Param("tagId") Integer tagId);
    
    /**
     * 检查笔记和标签的关联是否存在
     */
    @Select("SELECT COUNT(*) FROM note_tag WHERE note_id = #{noteId} AND tag_id = #{tagId}")
    int existsRelation(@Param("noteId") Integer noteId, @Param("tagId") Integer tagId);
    
    /**
     * 获取相似笔记（基于共同标签数量）
     */
    @Select("SELECT nt2.note_id, COUNT(*) as common_tags " +
            "FROM note_tag nt1 " +
            "INNER JOIN note_tag nt2 ON nt1.tag_id = nt2.tag_id " +
            "WHERE nt1.note_id = #{noteId} AND nt2.note_id != #{noteId} " +
            "GROUP BY nt2.note_id " +
            "ORDER BY common_tags DESC " +
            "LIMIT #{limit}")
    List<Integer> findSimilarNoteIds(@Param("noteId") Integer noteId, @Param("limit") Integer limit);
}

