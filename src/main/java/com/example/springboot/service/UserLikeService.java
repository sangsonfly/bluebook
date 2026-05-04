package com.example.springboot.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.springboot.entity.Note;
import com.example.springboot.entity.UserLike;
import com.example.springboot.mapper.NoteMapper;
import com.example.springboot.mapper.UserLikeMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 用户点赞服务
 * 提供点赞/取消点赞、查询点赞列表等功能
 */
@Service
public class UserLikeService extends ServiceImpl<UserLikeMapper, UserLike> {
    
    @Autowired
    private NotificationService notificationService;
    
    @Autowired
    private NoteMapper noteMapper;
    
    @Autowired
    private IUserBehaviorService userBehaviorService;
    
    /**
     * 点赞
     */
    @Transactional
    public boolean like(Integer userId, Integer targetType, Integer targetId) {
        // 检查是否已经点赞
        UserLike existing = this.baseMapper.getLikeRecord(userId, targetType, targetId);
        if (existing != null) {
            throw new RuntimeException("已经点赞过了");
        }
        
        // 创建点赞记录
        UserLike like = new UserLike();
        like.setUserId(userId);
        like.setTargetType(targetType);
        like.setTargetId(targetId);
        boolean success = this.save(like);
        
        if (success) {
            // 更新对象的点赞数
            updateLikeCount(targetType, targetId, 1);
            
            // 记录用户行为（用于推荐算法）
            if (targetType == UserLike.TYPE_NOTE) {
                userBehaviorService.recordBehavior(userId, targetId, 2); // 行为类型2=点赞
                
                // 发送点赞通知（不给自己发）
                Note note = noteMapper.selectById(targetId);
                if (note != null && note.getUserId() != null && !note.getUserId().equals(userId)) {
                    notificationService.sendLikeNotification(userId, note.getUserId(), targetId);
                }
            }
        }
        
        return success;
    }
    
    /**
     * 取消点赞
     */
    @Transactional
    public boolean unlike(Integer userId, Integer targetType, Integer targetId) {
        QueryWrapper<UserLike> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId)
               .eq("target_type", targetType)
               .eq("target_id", targetId);
        
        boolean success = this.remove(wrapper);
        
        if (success) {
            // 更新对象的点赞数
            updateLikeCount(targetType, targetId, -1);
        }
        
        return success;
    }
    
    /**
     * 检查是否已点赞
     */
    public boolean isLiked(Integer userId, Integer targetType, Integer targetId) {
        return this.baseMapper.getLikeRecord(userId, targetType, targetId) != null;
    }
    
    /**
     * 统计某对象的点赞数
     */
    public Integer countLikes(Integer targetType, Integer targetId) {
        return this.baseMapper.countLikes(targetType, targetId);
    }
    
    /**
     * 获取用户点赞的笔记列表
     */
    public IPage<Note> getUserLikedNotes(Integer userId, Page<Note> page) {
        // 先获取用户点赞的笔记ID列表
        List<Integer> noteIds = this.baseMapper.getUserLikedNoteIds(userId);
        
        if (noteIds.isEmpty()) {
            return page;
        }
        
        // 根据ID列表查询笔记详情
        QueryWrapper<Note> wrapper = new QueryWrapper<>();
        wrapper.in("id", noteIds)
               .eq("status", 1)
               .orderByDesc("create_time");
        
        return noteMapper.selectPage(page, wrapper);
    }
    
    /**
     * 批量检查点赞状态
     */
    public List<Integer> batchCheckLiked(Integer userId, Integer targetType, List<Integer> targetIds) {
        QueryWrapper<UserLike> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId)
               .eq("target_type", targetType)
               .in("target_id", targetIds);
        
        List<UserLike> likes = this.list(wrapper);
        return likes.stream().map(UserLike::getTargetId).toList();
    }
    
    /**
     * 更新对象的点赞数
     */
    private void updateLikeCount(Integer targetType, Integer targetId, int delta) {
        if (targetType == UserLike.TYPE_NOTE) {
            // 更新笔记点赞数
            Note note = noteMapper.selectById(targetId);
            if (note != null) {
                note.setLikes((note.getLikes() == null ? 0 : note.getLikes()) + delta);
                noteMapper.updateById(note);
            }
        }
        // 评论和答案的点赞数更新可以在对应的Service中实现
    }
}

