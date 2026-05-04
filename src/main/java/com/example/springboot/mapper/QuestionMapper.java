package com.example.springboot.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.springboot.entity.Question;
import org.apache.ibatis.annotations.Mapper;

/**
 * 问题Mapper
 */
@Mapper
public interface QuestionMapper extends BaseMapper<Question> {
}

