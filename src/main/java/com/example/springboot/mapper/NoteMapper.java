package com.example.springboot.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.springboot.entity.Note;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 笔记Mapper
 */
@Mapper
public interface NoteMapper extends BaseMapper<Note> {
    
    /**
     * 批量查询笔记
     */
    @Select("<script>" +
            "SELECT * FROM note WHERE id IN " +
            "<foreach collection='ids' item='id' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>" +
            " AND status = 1 ORDER BY FIELD(id, " +
            "<foreach collection='ids' item='id' separator=','>" +
            "#{id}" +
            "</foreach>" +
            ")" +
            "</script>")
    List<Note> selectByIds(@Param("ids") List<Integer> ids);
    
    /**
     * 获取热门笔记（综合评分）
     */
    @Select("SELECT * FROM note WHERE status = 1 " +
            "ORDER BY (likes * 2 + collects * 3 + comments * 1.5 + views * 0.01) DESC " +
            "LIMIT #{limit}")
    List<Note> getHotNotes(@Param("limit") Integer limit);
    
    /**
     * 根据标签获取相似笔记
     */
    @Select("<script>" +
            "SELECT DISTINCT n.* FROM note n " +
            "JOIN note_tag nt ON n.id = nt.note_id " +
            "JOIN tag t ON nt.tag_id = t.id " +
            "WHERE t.name IN " +
            "<foreach collection='tags' item='tag' open='(' separator=',' close=')'>" +
            "#{tag}" +
            "</foreach>" +
            " AND n.status = 1 AND n.id != #{excludeNoteId} " +
            "ORDER BY (n.likes * 2 + n.collects * 3 + n.comments * 1.5 + n.views * 0.01) DESC " +
            "LIMIT #{limit}" +
            "</script>")
    List<Note> getNotesByTags(@Param("tags") List<String> tags, @Param("excludeNoteId") Integer excludeNoteId, @Param("limit") Integer limit);
}

