package com.example.springboot.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.springboot.entity.Note;
import com.example.springboot.entity.NoteTag;
import com.example.springboot.entity.Tag;
import com.example.springboot.mapper.NoteMapper;
import com.example.springboot.mapper.NoteTagMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 笔记标签关联服务
 * 提供笔记标签管理功能
 */
@Slf4j
@Service
public class NoteTagService extends ServiceImpl<NoteTagMapper, NoteTag> {
    
    @Autowired
    private TagService tagService;
    
    @Autowired
    private NoteMapper noteMapper;
    
    /**
     * 为笔记添加标签
     */
    @Transactional
    public boolean addTagToNote(Integer noteId, Integer tagId) {
        // 检查关联是否已存在
        if (this.baseMapper.existsRelation(noteId, tagId) > 0) {
            throw new RuntimeException("标签已添加");
        }
        
        // 创建关联
        NoteTag noteTag = new NoteTag();
        noteTag.setNoteId(noteId);
        noteTag.setTagId(tagId);
        boolean success = this.save(noteTag);
        
        if (success) {
            // 增加标签使用次数
            tagService.incrementUseCount(tagId);
        }
        
        return success;
    }
    
    /**
     * 为笔记批量添加标签（通过标签名称）
     */
    @Transactional
    public void addTagsToNote(Integer noteId, List<String> tagNames, String defaultCategory) {
        if (tagNames == null || tagNames.isEmpty()) {
            return;
        }
        
        // 先删除笔记的所有标签
        removeAllTagsFromNote(noteId);
        
        // 创建或获取标签
        List<Tag> tags = tagService.createOrGetTags(tagNames, defaultCategory);
        
        // 添加新的标签关联
        for (Tag tag : tags) {
            NoteTag noteTag = new NoteTag();
            noteTag.setNoteId(noteId);
            noteTag.setTagId(tag.getId());
            this.save(noteTag);
            
            // 增加标签使用次数
            tagService.incrementUseCount(tag.getId());
        }
    }
    
    /**
     * 从笔记移除标签
     */
    @Transactional
    public boolean removeTagFromNote(Integer noteId, Integer tagId) {
        QueryWrapper<NoteTag> wrapper = new QueryWrapper<>();
        wrapper.eq("note_id", noteId).eq("tag_id", tagId);
        
        boolean success = this.remove(wrapper);
        
        if (success) {
            // 减少标签使用次数
            tagService.decrementUseCount(tagId);
        }
        
        return success;
    }
    
    /**
     * 移除笔记的所有标签
     */
    @Transactional
    public void removeAllTagsFromNote(Integer noteId) {
        // 先获取所有标签ID
        List<Tag> tags = this.baseMapper.getTagsByNoteId(noteId);
        
        // 删除关联
        this.baseMapper.deleteByNoteId(noteId);
        
        // 减少每个标签的使用次数
        for (Tag tag : tags) {
            tagService.decrementUseCount(tag.getId());
        }
    }
    
    /**
     * 获取笔记的所有标签
     */
    public List<Tag> getTagsByNoteId(Integer noteId) {
        return this.baseMapper.getTagsByNoteId(noteId);
    }
    
    /**
     * 获取使用某标签的笔记列表
     */
    public List<Note> getNotesByTagId(Integer tagId, Integer limit) {
        List<Integer> noteIds = this.baseMapper.getNoteIdsByTagId(tagId);
        
        if (noteIds.isEmpty()) {
            return new ArrayList<>();
        }
        
        QueryWrapper<Note> wrapper = new QueryWrapper<>();
        wrapper.in("id", noteIds)
               .eq("status", 1)
               .orderByDesc("create_time");
        
        if (limit != null && limit > 0) {
            wrapper.last("LIMIT " + limit);
        }
        
        return noteMapper.selectList(wrapper);
    }
    
    /**
     * 从笔记的tags字段（逗号分隔）同步到note_tag表
     */
    @Transactional
    public void syncTagsFromNoteField(Integer noteId) {
        Note note = noteMapper.selectById(noteId);
        if (note == null || note.getTags() == null || note.getTags().isEmpty()) {
            return;
        }
        
        // 解析标签字符串
        List<String> tagNames = Arrays.stream(note.getTags().split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
        
        // 添加标签
        addTagsToNote(noteId, tagNames, note.getCategory());
    }
    
    /**
     * 批量同步所有笔记的标签
     */
    @Transactional
    public void syncAllNoteTags() {
        QueryWrapper<Note> wrapper = new QueryWrapper<>();
        wrapper.isNotNull("tags")
               .ne("tags", "")
               .eq("status", 1);
        
        List<Note> notes = noteMapper.selectList(wrapper);
        
        for (Note note : notes) {
            try {
                syncTagsFromNoteField(note.getId());
            } catch (Exception e) {
                log.error("同步笔记{}标签失败: {}", note.getId(), e.getMessage());
            }
        }
    }
}

