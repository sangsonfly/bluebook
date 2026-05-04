package com.example.springboot.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.springboot.entity.CommentLike;
import org.apache.ibatis.annotations.Mapper;

/**
 * 评论点赞Mapper
 */
@Mapper
public interface CommentLikeMapper extends BaseMapper<CommentLike> {
}

