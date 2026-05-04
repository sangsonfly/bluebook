package com.example.springboot.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.springboot.entity.RecommendationResult;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * 推荐结果Mapper
 */
@Mapper
public interface RecommendationResultMapper extends BaseMapper<RecommendationResult> {
    
    /**
     * 根据用户ID和算法类型查询推荐结果
     */
    @Select("SELECT * FROM recommendation_result WHERE user_id = #{userId} AND algorithm_type = #{algorithmType}")
    RecommendationResult selectByUserAndType(Integer userId, Integer algorithmType);
}

