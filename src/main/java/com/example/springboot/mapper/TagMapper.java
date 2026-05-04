package com.example.springboot.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.springboot.entity.Tag;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 标签Mapper
 */
@Mapper
public interface TagMapper extends BaseMapper<Tag> {
    
    /**
     * 根据标签名称查询
     */
    @Select("SELECT * FROM tag WHERE name = #{name}")
    Tag getByName(@Param("name") String name);
    
    /**
     * 获取热门标签（按使用次数排序）
     */
    @Select("SELECT * FROM tag WHERE status = 1 ORDER BY use_count DESC LIMIT #{limit}")
    List<Tag> getHotTags(@Param("limit") Integer limit);
    
    /**
     * 根据分类获取标签
     */
    @Select("SELECT * FROM tag WHERE category = #{category} AND status = 1 ORDER BY use_count DESC")
    List<Tag> getTagsByCategory(@Param("category") String category);
    
    /**
     * 搜索标签（模糊匹配）
     */
    @Select("SELECT * FROM tag WHERE name LIKE CONCAT('%', #{keyword}, '%') AND status = 1 ORDER BY use_count DESC LIMIT #{limit}")
    List<Tag> searchTags(@Param("keyword") String keyword, @Param("limit") Integer limit);
    
    /**
     * 增加标签使用次数
     */
    @Update("UPDATE tag SET use_count = use_count + 1 WHERE id = #{tagId}")
    int incrementUseCount(@Param("tagId") Integer tagId);
    
    /**
     * 减少标签使用次数
     */
    @Update("UPDATE tag SET use_count = use_count - 1 WHERE id = #{tagId} AND use_count > 0")
    int decrementUseCount(@Param("tagId") Integer tagId);
    
    /**
     * 获取所有标签分类
     */
    @Select("SELECT DISTINCT category FROM tag WHERE status = 1 ORDER BY category")
    List<String> getAllCategories();
}

