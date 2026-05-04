package com.example.springboot.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.springboot.entity.Note;
import com.example.springboot.entity.UserBehavior;
import com.example.springboot.entity.UserCollect;
import com.example.springboot.mapper.NoteMapper;
import com.example.springboot.mapper.UserCollectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用户收藏服务
 * 提供收藏/取消收藏、收藏夹管理等功能
 */
@Service
public class UserCollectService extends ServiceImpl<UserCollectMapper, UserCollect> {
    
    @Autowired
    private NotificationService notificationService;
    
    @Autowired
    private NoteMapper noteMapper;
    
    @Autowired
    private IUserBehaviorService userBehaviorService;
    
    /**
     * 收藏
     */
    @Transactional
    public boolean collect(Integer userId, Integer targetType, Integer targetId, String folderName) {
        // 检查是否已经收藏
        UserCollect existing = this.baseMapper.getCollectRecord(userId, targetType, targetId);
        if (existing != null) {
            throw new RuntimeException("已经收藏过了");
        }
        
        // 创建收藏记录
        UserCollect collect = new UserCollect();
        collect.setUserId(userId);
        collect.setTargetType(targetType);
        collect.setTargetId(targetId);
        collect.setFolderName(folderName != null ? folderName : UserCollect.DEFAULT_FOLDER);
        boolean success = this.save(collect);
        
        if (success) {
            // 更新对象的收藏数
            updateCollectCount(targetType, targetId, 1);
            
            // 记录用户行为（用于推荐算法）
            if (targetType == UserCollect.TYPE_NOTE) {
                userBehaviorService.recordBehavior(userId, targetId, 3); // 行为类型3=收藏
                
                // 发送收藏通知（不给自己发）
                Note note = noteMapper.selectById(targetId);
                if (note != null && note.getUserId() != null && !note.getUserId().equals(userId)) {
                    notificationService.sendCollectNotification(userId, note.getUserId(), targetId);
                }
            }
        }
        
        return success;
    }
    
    /**
     * 取消收藏
     */
    @Transactional
    public boolean uncollect(Integer userId, Integer targetType, Integer targetId) {
        QueryWrapper<UserCollect> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId)
               .eq("target_type", targetType)
               .eq("target_id", targetId);
        
        boolean success = this.remove(wrapper);
        
        if (success) {
            // 更新对象的收藏数
            updateCollectCount(targetType, targetId, -1);
            
            // 同步删除 user_behavior 表中的收藏记录（用于协同过滤）
            if (targetType == UserCollect.TYPE_NOTE) {
                userBehaviorService.removeBehavior(userId, targetId, UserBehavior.BEHAVIOR_COLLECT);
            }
        }
        
        return success;
    }
    
    /**
     * 检查是否已收藏
     */
    public boolean isCollected(Integer userId, Integer targetType, Integer targetId) {
        return this.baseMapper.getCollectRecord(userId, targetType, targetId) != null;
    }
    
    /**
     * 统计某对象的收藏数
     */
    public Integer countCollects(Integer targetType, Integer targetId) {
        return this.baseMapper.countCollects(targetType, targetId);
    }
    
    /**
     * 获取用户的收藏夹列表（带统计）
     */
    public List<Map<String, Object>> getUserFoldersWithCount(Integer userId) {
        List<String> folders = this.baseMapper.getUserFolders(userId);
        return folders.stream().map(folder -> {
            Map<String, Object> map = new HashMap<>();
            map.put("folderName", folder);
            map.put("count", this.baseMapper.countFolderItems(userId, folder));
            return map;
        }).toList();
    }
    
    /**
     * 获取用户收藏的笔记列表（按收藏夹）
     */
    public IPage<Note> getUserCollectedNotes(Integer userId, String folderName, Page<Note> page) {
        // 构建查询条件
        QueryWrapper<UserCollect> collectWrapper = new QueryWrapper<>();
        collectWrapper.eq("user_id", userId)
                     .eq("target_type", UserCollect.TYPE_NOTE);
        
        if (folderName != null && !folderName.isEmpty()) {
            collectWrapper.eq("folder_name", folderName);
        }
        
        collectWrapper.orderByDesc("create_time");
        
        // 获取收藏的笔记ID列表
        List<UserCollect> collects = this.list(collectWrapper);
        if (collects.isEmpty()) {
            return page;
        }
        
        List<Integer> noteIds = collects.stream()
                .map(UserCollect::getTargetId)
                .toList();
        
        // 根据ID列表查询笔记详情
        QueryWrapper<Note> noteWrapper = new QueryWrapper<>();
        noteWrapper.in("id", noteIds)
                   .eq("status", 1);
        
        return noteMapper.selectPage(page, noteWrapper);
    }
    
    /**
     * 移动收藏到其他收藏夹
     */
    @Transactional
    public boolean moveToFolder(Integer userId, Integer targetType, Integer targetId, String newFolderName) {
        UserCollect collect = this.baseMapper.getCollectRecord(userId, targetType, targetId);
        if (collect == null) {
            throw new RuntimeException("未找到收藏记录");
        }
        
        collect.setFolderName(newFolderName);
        return this.updateById(collect);
    }
    
    /**
     * 批量检查收藏状态
     */
    public List<Integer> batchCheckCollected(Integer userId, Integer targetType, List<Integer> targetIds) {
        QueryWrapper<UserCollect> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId)
               .eq("target_type", targetType)
               .in("target_id", targetIds);
        
        List<UserCollect> collects = this.list(wrapper);
        return collects.stream().map(UserCollect::getTargetId).toList();
    }
    
    /**
     * 更新对象的收藏数
     */
    private void updateCollectCount(Integer targetType, Integer targetId, int delta) {
        if (targetType == UserCollect.TYPE_NOTE) {
            // 更新笔记收藏数
            Note note = noteMapper.selectById(targetId);
            if (note != null) {
                note.setCollects((note.getCollects() == null ? 0 : note.getCollects()) + delta);
                noteMapper.updateById(note);
            }
        }
        // 二手商品和问题的收藏数更新可以在对应的Service中实现
    }
}

