package com.example.springboot.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.springboot.entity.SecondhandItem;
import org.apache.ibatis.annotations.Mapper;

/**
 * 二手商品Mapper
 */
@Mapper
public interface SecondhandItemMapper extends BaseMapper<SecondhandItem> {
}

