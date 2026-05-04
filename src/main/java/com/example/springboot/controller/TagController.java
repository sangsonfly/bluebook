package com.example.springboot.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.springboot.common.Result;
import com.example.springboot.entity.Tag;
import com.example.springboot.service.TagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 标签Controller
 * 提供标签管理、热门标签查询等API
 */
@RestController
@RequestMapping("/api/tag")
public class TagController {
    
    @Autowired
    private TagService tagService;
    
    /**
     * 创建标签
     */
    @PostMapping("/create")
    public Result createTag(@RequestParam String name,
                           @RequestParam(required = false) String category) {
        try {
            Tag tag = tagService.createOrGetTag(name, category);
            return Result.success(tag);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 获取热门标签
     */
    @GetMapping("/hot")
    public Result getHotTags(@RequestParam(defaultValue = "20") Integer limit) {
        List<Tag> tags = tagService.getHotTags(limit);
        return Result.success(tags);
    }
    
    /**
     * 根据分类获取标签
     */
    @GetMapping("/byCategory")
    public Result getTagsByCategory(@RequestParam String category) {
        List<Tag> tags = tagService.getTagsByCategory(category);
        return Result.success(tags);
    }
    
    /**
     * 搜索标签
     */
    @GetMapping("/search")
    public Result searchTags(@RequestParam String keyword,
                            @RequestParam(defaultValue = "10") Integer limit) {
        List<Tag> tags = tagService.searchTags(keyword, limit);
        return Result.success(tags);
    }
    
    /**
     * 获取所有标签分类
     */
    @GetMapping("/categories")
    public Result getAllCategories() {
        List<String> categories = tagService.getAllCategories();
        return Result.success(categories);
    }
    
    /**
     * 分页查询标签
     */
    @GetMapping("/page")
    public Result getTagsPage(@RequestParam(defaultValue = "1") Integer pageNum,
                             @RequestParam(defaultValue = "20") Integer pageSize,
                             @RequestParam(required = false) String category,
                             @RequestParam(required = false) String keyword) {
        IPage<Tag> result = tagService.getTagsPage(pageNum, pageSize, category, keyword);
        return Result.success(result);
    }
    
    /**
     * 获取标签详情
     */
    @GetMapping("/{id}")
    public Result getTagById(@PathVariable Integer id) {
        Tag tag = tagService.getById(id);
        return tag != null ? Result.success(tag) : Result.error("标签不存在");
    }
    
    /**
     * 更新标签
     */
    @PutMapping("/update")
    public Result updateTag(@RequestParam Integer id,
                           @RequestParam(required = false) String name,
                           @RequestParam(required = false) String category) {
        try {
            boolean success = tagService.updateTag(id, name, category);
            return success ? Result.success("更新成功") : Result.error("更新失败");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 禁用标签
     */
    @PostMapping("/disable/{id}")
    public Result disableTag(@PathVariable Integer id) {
        try {
            boolean success = tagService.disableTag(id);
            return success ? Result.success("禁用成功") : Result.error("禁用失败");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 启用标签
     */
    @PostMapping("/enable/{id}")
    public Result enableTag(@PathVariable Integer id) {
        try {
            boolean success = tagService.enableTag(id);
            return success ? Result.success("启用成功") : Result.error("启用失败");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 删除标签
     */
    @DeleteMapping("/{id}")
    public Result deleteTag(@PathVariable Integer id) {
        boolean success = tagService.removeById(id);
        return success ? Result.success("删除成功") : Result.error("删除失败");
    }
}

