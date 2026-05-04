package com.example.springboot.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.springboot.entity.Tag;
import com.example.springboot.mapper.TagMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 标签服务
 * 提供标签管理、热门标签查询等功能
 */
@Service
public class TagService extends ServiceImpl<TagMapper, Tag> {
    
    /**
     * 创建或获取标签
     * 如果标签已存在则返回现有标签，否则创建新标签
     */
    @Transactional
    public Tag createOrGetTag(String name, String category) {
        // 检查标签是否已存在
        Tag existingTag = this.baseMapper.getByName(name);
        if (existingTag != null) {
            return existingTag;
        }
        
        // 创建新标签
        Tag tag = new Tag();
        tag.setName(name);
        tag.setCategory(category != null ? category : Tag.CATEGORY_GENERAL);
        tag.setUseCount(0);
        tag.setStatus(Tag.STATUS_ACTIVE);
        this.save(tag);
        
        return tag;
    }
    
    /**
     * 批量创建或获取标签
     */
    @Transactional
    public List<Tag> createOrGetTags(List<String> tagNames, String category) {
        List<Tag> tags = new ArrayList<>();
        for (String name : tagNames) {
            if (name != null && !name.trim().isEmpty()) {
                tags.add(createOrGetTag(name.trim(), category));
            }
        }
        return tags;
    }
    
    /**
     * 获取热门标签
     */
    public List<Tag> getHotTags(Integer limit) {
        return this.baseMapper.getHotTags(limit != null ? limit : 20);
    }
    
    /**
     * 根据分类获取标签
     */
    public List<Tag> getTagsByCategory(String category) {
        return this.baseMapper.getTagsByCategory(category);
    }
    
    /**
     * 搜索标签
     */
    public List<Tag> searchTags(String keyword, Integer limit) {
        return this.baseMapper.searchTags(keyword, limit != null ? limit : 10);
    }
    
    /**
     * 增加标签使用次数
     */
    @Transactional
    public void incrementUseCount(Integer tagId) {
        this.baseMapper.incrementUseCount(tagId);
    }
    
    /**
     * 减少标签使用次数
     */
    @Transactional
    public void decrementUseCount(Integer tagId) {
        this.baseMapper.decrementUseCount(tagId);
    }
    
    /**
     * 获取所有标签分类
     */
    public List<String> getAllCategories() {
        return this.baseMapper.getAllCategories();
    }
    
    /**
     * 分页查询标签
     */
    public IPage<Tag> getTagsPage(Integer pageNum, Integer pageSize, String category, String keyword) {
        Page<Tag> page = new Page<>(pageNum, pageSize);
        QueryWrapper<Tag> wrapper = new QueryWrapper<>();
        
        wrapper.eq("status", Tag.STATUS_ACTIVE);
        
        if (category != null && !category.isEmpty()) {
            wrapper.eq("category", category);
        }
        
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like("name", keyword);
        }
        
        wrapper.orderByDesc("use_count");
        
        return this.page(page, wrapper);
    }
    
    /**
     * 禁用标签
     */
    @Transactional
    public boolean disableTag(Integer tagId) {
        Tag tag = this.getById(tagId);
        if (tag == null) {
            throw new RuntimeException("标签不存在");
        }
        
        tag.setStatus(Tag.STATUS_DISABLED);
        return this.updateById(tag);
    }
    
    /**
     * 启用标签
     */
    @Transactional
    public boolean enableTag(Integer tagId) {
        Tag tag = this.getById(tagId);
        if (tag == null) {
            throw new RuntimeException("标签不存在");
        }
        
        tag.setStatus(Tag.STATUS_ACTIVE);
        return this.updateById(tag);
    }
    
    /**
     * 更新标签信息
     */
    @Transactional
    public boolean updateTag(Integer tagId, String name, String category) {
        Tag tag = this.getById(tagId);
        if (tag == null) {
            throw new RuntimeException("标签不存在");
        }
        
        // 如果修改了名称，检查新名称是否已存在
        if (name != null && !name.equals(tag.getName())) {
            Tag existingTag = this.baseMapper.getByName(name);
            if (existingTag != null && !existingTag.getId().equals(tagId)) {
                throw new RuntimeException("标签名称已存在");
            }
            tag.setName(name);
        }
        
        if (category != null && !category.isEmpty()) {
            tag.setCategory(category);
        }
        
        return this.updateById(tag);
    }
}

