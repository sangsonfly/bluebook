package com.example.springboot.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.springboot.entity.Note;
import com.example.springboot.entity.User;
import com.example.springboot.mapper.NoteMapper;
import com.example.springboot.mapper.UserFollowMapper;
import com.example.springboot.service.INoteService;
import com.example.springboot.service.IUserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.util.List;

/**
 * 笔记Service实现类
 */
@Service
public class NoteServiceImpl extends ServiceImpl<NoteMapper, Note> implements INoteService {
    
    @Resource
    private NoteMapper noteMapper;
    
    @Resource
    private IUserService userService;
    
    @Resource
    private UserFollowMapper userFollowMapper;
    
    @Override
    @Transactional
    public void incrementViews(Integer id) {
        Note note = noteMapper.selectById(id);
        if (note != null) {
            note.setViews(note.getViews() == null ? 1 : note.getViews() + 1);
            noteMapper.updateById(note);
        }
    }
    
    @Override
    @Transactional
    public void incrementLikes(Integer id) {
        Note note = noteMapper.selectById(id);
        if (note != null) {
            note.setLikes(note.getLikes() == null ? 1 : note.getLikes() + 1);
            noteMapper.updateById(note);
        }
    }
    
    @Override
    @Transactional
    public void decrementLikes(Integer id) {
        Note note = noteMapper.selectById(id);
        if (note != null && note.getLikes() != null && note.getLikes() > 0) {
            note.setLikes(note.getLikes() - 1);
            noteMapper.updateById(note);
        }
    }
    
    @Override
    @Transactional
    public void incrementCollects(Integer id) {
        Note note = noteMapper.selectById(id);
        if (note != null) {
            note.setCollects(note.getCollects() == null ? 1 : note.getCollects() + 1);
            noteMapper.updateById(note);
        }
    }
    
    @Override
    @Transactional
    public void decrementCollects(Integer id) {
        Note note = noteMapper.selectById(id);
        if (note != null && note.getCollects() != null && note.getCollects() > 0) {
            note.setCollects(note.getCollects() - 1);
            noteMapper.updateById(note);
        }
    }
    
    @Override
    public Note getNoteDetailById(Integer id) {
        Note note = noteMapper.selectById(id);
        if (note != null && note.getUserId() != null) {
            // 查询作者信息，补充账号类型
            User user = userService.getById(note.getUserId());
            if (user != null) {
                note.setAccountType(user.getAccountType());
                note.setIsClubAccount(user.getAccountType() != null && 
                                     user.getAccountType().equals(User.ACCOUNT_TYPE_CLUB));
            }
        }
        return note;
    }
    
    @Override
    public Page<Note> getFollowingUserNotes(Integer userId, Integer pageNum, Integer pageSize) {
        Page<Note> page = new Page<>(pageNum, pageSize);
        
        // 1. 获取用户关注的所有用户ID
        List<Integer> followingUserIds = userFollowMapper.getFollowingUserIds(userId);
        
        if (followingUserIds.isEmpty()) {
            return page; // 如果没有关注任何人，返回空列表
        }
        
        // 2. 查询这些用户发布的笔记
        QueryWrapper<Note> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("user_id", followingUserIds)
                   .eq("status", Note.STATUS_PUBLISHED)
                   .orderByDesc("create_time");
        
        return this.page(page, queryWrapper);
    }
}

