package com.example.springboot.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.springboot.entity.Club;
import org.apache.ibatis.annotations.Mapper;

/**
 * 社团Mapper
 */
@Mapper
public interface ClubMapper extends BaseMapper<Club> {
}

