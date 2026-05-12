package com.example.springboot.controller;

import com.example.springboot.common.Result;
import com.example.springboot.entity.Note;
import com.example.springboot.entity.Tag;
import com.example.springboot.service.NoteTagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 笔记标签关联Controller
 * 提供笔记标签管理等API
 */
@RestController
@RequestMapping("/api/noteTag")
public class NoteTagController {
    
    @Autowired
    private NoteTagService noteTagService;
    
    /**
     * 为笔记添加标签
     */
    @PostMapping("/add")
    public Result addTagToNote(@RequestParam Integer noteId,
                              @RequestParam Integer tagId) {
        try {
            boolean success = noteTagService.addTagToNote(noteId, tagId);
            return success ? Result.success("添加成功") : Result.error("添加失败");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 为笔记批量添加标签（通过标签名称）
     */
    @PostMapping("/addBatch")
    public Result addTagsToNote(@RequestParam Integer noteId,
                               @RequestBody List<String> tagNames,
                               @RequestParam(required = false) String category) {
        try {
            noteTagService.addTagsToNote(noteId, tagNames, category);
            return Result.success("添加成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 从笔记移除标签
     */
    @PostMapping("/remove")
    public Result removeTagFromNote(@RequestParam Integer noteId,
                                   @RequestParam Integer tagId) {
        try {
            boolean success = noteTagService.removeTagFromNote(noteId, tagId);
            return success ? Result.success("移除成功") : Result.error("移除失败");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 移除笔记的所有标签
     */
    @PostMapping("/removeAll")
    public Result removeAllTagsFromNote(@RequestParam Integer noteId) {
        try {
            noteTagService.removeAllTagsFromNote(noteId);
            return Result.success("移除成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 获取笔记的所有标签
     */
    @GetMapping("/byNote")
    public Result getTagsByNoteId(@RequestParam Integer noteId) {
        List<Tag> tags = noteTagService.getTagsByNoteId(noteId);
        return Result.success(tags);
    }
    
    /**
     * 获取使用某标签的笔记列表
     */
    @GetMapping("/byTag")
    public Result getNotesByTagId(@RequestParam Integer tagId,
                                 @RequestParam(required = false) Integer limit) {
        List<Note> notes = noteTagService.getNotesByTagId(tagId, limit);
        return Result.success(notes);
    }
    
    /**
     * 从笔记的tags字段同步到note_tag表
     */
    @PostMapping("/sync/{noteId}")
    public Result syncTagsFromNoteField(@PathVariable Integer noteId) {
        try {
            noteTagService.syncTagsFromNoteField(noteId);
            return Result.success("同步成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 批量同步所有笔记的标签（管理员功能）
     */
    @PostMapping("/syncAll")
    public Result syncAllNoteTags() {
        try {
            noteTagService.syncAllNoteTags();
            return Result.success("同步成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}

