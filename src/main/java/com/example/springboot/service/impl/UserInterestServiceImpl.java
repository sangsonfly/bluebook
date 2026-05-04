package com.example.springboot.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.springboot.entity.Tag;
import com.example.springboot.entity.UserInterest;
import com.example.springboot.mapper.NoteTagMapper;
import com.example.springboot.mapper.UserInterestMapper;
import com.example.springboot.service.IUserInterestService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.math.BigDecimal;
import java.util.List;

/**
 * 用户兴趣服务实现类
 */
@Slf4j
@Service
public class UserInterestServiceImpl extends ServiceImpl<UserInterestMapper, UserInterest> implements IUserInterestService {
    
    @Resource
    private UserInterestMapper userInterestMapper;
    
    @Resource
    private NoteTagMapper noteTagMapper;
    
    @Override
    @Transactional
    public void updateUserInterestFromBehavior(Integer userId) {
        // 这个方法可以用于批量更新用户兴趣
        // 暂时不实现，因为兴趣更新会在每次行为发生时实时更新
        log.debug("更新用户{}的兴趣标签（基于行为）", userId);
    }
    
    @Override
    @Transactional
    public void addInterestFromNote(Integer userId, Integer noteId, BigDecimal behaviorWeight) {
        try {
            // 获取笔记的所有标签
            List<Tag> tags = noteTagMapper.getTagsByNoteId(noteId);
            
            if (tags == null || tags.isEmpty()) {
                // 笔记没有标签，无法更新兴趣
                return;
            }
            
            // 为每个标签更新用户兴趣权重
            for (Tag tag : tags) {
                String tagName = tag.getName();
                if (tagName == null || tagName.trim().isEmpty()) {
                    continue;
                }
                
                // 使用行为权重的一半作为标签权重（避免权重过大）
                BigDecimal tagWeight = behaviorWeight.multiply(new BigDecimal("0.5"));
                
                // 更新或插入用户兴趣标签
                userInterestMapper.upsertUserInterest(userId, tagName, tagWeight);
            }
            
            log.debug("用户{}对笔记{}的操作已更新兴趣标签，标签数量：{}", userId, noteId, tags.size());
            
        } catch (Exception e) {
            log.error("更新用户{}的兴趣标签失败（笔记ID：{}）", userId, noteId, e);
            // 不抛出异常，避免影响主流程
        }
    }
    
    @Override
    public List<UserInterest> getUserInterestTags(Integer userId) {
        return userInterestMapper.getUserInterestTags(userId);
    }
}

