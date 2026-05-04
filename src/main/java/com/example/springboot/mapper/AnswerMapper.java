package com.example.springboot.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.springboot.entity.Answer;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 答案Mapper
 */
@Mapper
public interface AnswerMapper extends BaseMapper<Answer> {
    
    /**
     * 查询问题的所有答案
     */
    @Select("SELECT * FROM answer WHERE question_id = #{questionId} ORDER BY likes DESC, create_time DESC")
    List<Answer> getByQuestionId(Integer questionId);
}

