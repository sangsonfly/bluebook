package com.example.springboot.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.springboot.common.Constants;
import com.example.springboot.common.Result;
import com.example.springboot.config.interceptor.AuthAccess;
import com.example.springboot.entity.Account;
import com.example.springboot.entity.Note;
import com.example.springboot.entity.UserBehavior;
import com.example.springboot.entity.UserCollect;
import com.example.springboot.entity.dto.AiModerationResult;
import com.example.springboot.service.AiModerationService;
import com.example.springboot.service.INoteService;
import com.example.springboot.service.IUserService;
import com.example.springboot.service.IUserBehaviorService;
import com.example.springboot.service.IUserInterestService;
import com.example.springboot.service.ClubMemberService;
import com.example.springboot.service.UserCollectService;
import com.example.springboot.utils.TokenUtils;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 笔记Controller
 */
@RestController
@RequestMapping("/api/note")
public class NoteController {
    
    @Resource
    private INoteService noteService;

    @Resource
    private ClubMemberService clubMemberService;

    @Resource
    private AiModerationService aiModerationService;
    
    /**
     * 查询所有笔记
     */
    @AuthAccess
    @GetMapping("/list")
    public Result list() {
        QueryWrapper<Note> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("status", Note.STATUS_PUBLISHED);
        queryWrapper.orderByDesc("create_time");
        List<Note> list = noteService.list(queryWrapper);
        return Result.success(list);
    }
    
    /**
     * 分页查询笔记
     */
    @AuthAccess
    @GetMapping("/page")
    public Result page(@RequestParam(defaultValue = "1") Integer pageNum,
                       @RequestParam(defaultValue = "10") Integer pageSize,
                       @RequestParam(required = false) String category,
                       @RequestParam(required = false) String keyword,
                       @RequestParam(required = false) Integer userId,
                       @RequestParam(required = false) Integer clubId,
                       @RequestParam(required = false) Integer status) {
        Page<Note> page = new Page<>(pageNum, pageSize);
        QueryWrapper<Note> queryWrapper = new QueryWrapper<>();
        
        Account cur = TokenUtils.getCurrentUser();
        boolean isAdmin = isAdminAccount(cur);
        boolean isUser = isUserAccount(cur);

        if (clubId != null) {
            queryWrapper.eq("club_id", clubId);
        }
        
        if (userId != null) {
            queryWrapper.eq("user_id", userId);
            boolean viewingSelf = isUser && cur.getId() != null && cur.getId().equals(userId);
            if (viewingSelf) {
                queryWrapper.ne("status", Note.STATUS_DELETED);
                if (status != null) {
                    queryWrapper.eq("status", status);
                }
            } else {
                queryWrapper.eq("status", Note.STATUS_PUBLISHED);
            }
        } else if (clubId != null) {
            boolean clubModerator = isUser && cur.getId() != null
                    && clubMemberService.isAdmin(clubId, cur.getId());
            if (isAdmin) {
                if (status != null) {
                    queryWrapper.eq("status", status);
                }
            } else if (clubModerator) {
                if (status != null && !status.equals(Note.STATUS_DELETED)) {
                    queryWrapper.eq("status", status);
                } else {
                    queryWrapper.eq("status", Note.STATUS_PUBLISHED);
                }
            } else {
                queryWrapper.eq("status", Note.STATUS_PUBLISHED);
            }
        } else {
            if (isAdmin) {
                if (status != null) {
                    queryWrapper.eq("status", status);
                }
            } else {
                queryWrapper.eq("status", Note.STATUS_PUBLISHED);
            }
        }
        
        if (category != null && !category.isEmpty()) {
            queryWrapper.eq("category", category);
        }
        
        if (keyword != null && !keyword.isEmpty()) {
            queryWrapper.and(qw -> qw
                .like("title", keyword)
                .or()
                .like("content", keyword)
                .or()
                .like("tags", keyword)
            );
        }
        
        queryWrapper.orderByDesc("create_time");
        Page<Note> result = noteService.page(page, queryWrapper);
        return Result.success(result);
    }
    
    /**
     * 根据ID查询笔记详情
     */
    @AuthAccess
    @GetMapping("/{id}")
    public Result getById(@PathVariable Integer id, @RequestParam(required = false) Integer userId) {
        Note note = noteService.getNoteDetailById(id);
        if (note == null) {
            return Result.success(null);
        }
        
        Account cur = TokenUtils.getCurrentUser();
        Integer st = note.getStatus();
        boolean published = st != null && st == Note.STATUS_PUBLISHED;
        boolean canView = published || canViewNonPublishedNote(note, cur);
        if (!canView) {
            return Result.error(Constants.CODE_400, "笔记不存在或已下架");
        }
        
        if (published) {
            noteService.incrementViews(id);
            
            if (userId == null) {
                try {
                    if (cur != null && cur.getId() != null) {
                        userId = cur.getId();
                    }
                } catch (Exception e) {
                    // ignore
                }
            }
            
            if (userId != null) {
                userBehaviorService.recordBehavior(userId, id, UserBehavior.BEHAVIOR_VIEW, null);
                userInterestService.addInterestFromNote(userId, id, new java.math.BigDecimal("0.25"));
            }
            
            note = noteService.getNoteDetailById(id);
        }
        
        return Result.success(note);
    }
    
    @Resource
    private IUserService userService;
    
    /**
     * 新增笔记
     */
    @PostMapping
    public Result save(@RequestBody Note note) {
        if (note == null) {
            return Result.error(Constants.CODE_400, "参数错误");
        }
        Account cur = TokenUtils.getCurrentUser();
        if (cur == null || cur.getId() == null) {
            return Result.error(Constants.CODE_401, "请先登录");
        }
        boolean admin = "ROLE_ADMIN".equals(cur.getRole());
        boolean user = "ROLE_USER".equals(cur.getRole());
        if (!admin && !user) {
            return Result.error(Constants.CODE_401, "无权限");
        }
        if (note.getUserId() == null) {
            note.setUserId(cur.getId());
        }
        if (!admin && !cur.getId().equals(note.getUserId())) {
            return Result.error(Constants.CODE_401, "无权限为其他用户发布");
        }
        if (note.getClubId() != null && !clubMemberService.isMember(note.getClubId(), note.getUserId())) {
            return Result.error(Constants.CODE_401, "仅可关联自己已加入的社团");
        }
        note.setPublishType(note.getClubId() == null ? Note.PUBLISH_TYPE_PERSONAL : Note.PUBLISH_TYPE_CLUB);
        if (note.getStatus() != null && note.getStatus() == Note.STATUS_DELETED) {
            return Result.error(Constants.CODE_400, "非法状态");
        }
        try {
            AiModerationResult moderationResult = aiModerationService.check(note.getTitle(), note.getContent());
            if (moderationResult == null || !moderationResult.isPass()) {
                return Result.error(Constants.CODE_400, "内容可能涉及违规，请修改后重试");
            }
        } catch (IllegalArgumentException e) {
            return Result.error(Constants.CODE_400, e.getMessage());
        } catch (Exception e) {
            return Result.error(Constants.CODE_400, "内容可能涉及违规，请修改后重试");
        }
        noteService.save(note);
        if (note.getUserId() != null) {
            userService.incrementNotesCount(note.getUserId());
        }
        return Result.success();
    }
    
    /**
     * 更新笔记
     */
    @PutMapping
    public Result update(@RequestBody Note note) {
        if (note == null || note.getId() == null) {
            return Result.error(Constants.CODE_400, "参数错误");
        }
        Account cur = TokenUtils.getCurrentUser();
        if (cur == null || cur.getId() == null) {
            return Result.error(Constants.CODE_401, "请先登录");
        }
        Note existing = noteService.getById(note.getId());
        if (existing == null) {
            return Result.error(Constants.CODE_400, "笔记不存在");
        }
        boolean admin = "ROLE_ADMIN".equals(cur.getRole());
        boolean owner = "ROLE_USER".equals(cur.getRole())
                && existing.getUserId() != null && existing.getUserId().equals(cur.getId());
        if (!admin && !owner) {
            return Result.error(Constants.CODE_401, "无权限");
        }
        if (note.getStatus() != null && note.getStatus() == Note.STATUS_DELETED) {
            return Result.error(Constants.CODE_400, "请使用删除接口");
        }
        noteService.updateById(note);
        return Result.success();
    }
    
    /**
     * 删除笔记（软删）
     */
    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Integer id) {
        Account cur = TokenUtils.getCurrentUser();
        if (cur == null || cur.getId() == null) {
            return Result.error(Constants.CODE_401, "请先登录");
        }
        Note note = noteService.getById(id);
        if (note == null) {
            return Result.success();
        }
        boolean admin = "ROLE_ADMIN".equals(cur.getRole());
        boolean owner = "ROLE_USER".equals(cur.getRole())
                && note.getUserId() != null && note.getUserId().equals(cur.getId());
        boolean clubModerator = "ROLE_USER".equals(cur.getRole())
                && note.getClubId() != null
                && clubMemberService.isAdmin(note.getClubId(), cur.getId());
        if (!admin && !owner && !clubModerator) {
            return Result.error(Constants.CODE_401, "无权限");
        }
        if (note.getStatus() != null && note.getStatus() == Note.STATUS_DELETED) {
            return Result.success();
        }
        Note upd = new Note();
        upd.setId(id);
        upd.setStatus(Note.STATUS_DELETED);
        noteService.updateById(upd);
        if (note.getUserId() != null) {
            userService.decrementNotesCount(note.getUserId());
        }
        return Result.success();
    }
    
    @Resource
    private IUserBehaviorService userBehaviorService;
    
    @Resource
    private UserCollectService userCollectService;
    
    @Resource
    private IUserInterestService userInterestService;
    
    /**
     * 点赞笔记（切换状态）
     */
    @PostMapping("/{id}/like")
    public Result like(@PathVariable Integer id, @RequestParam Integer userId) {
        Note n = noteService.getById(id);
        if (n == null || n.getStatus() == null || n.getStatus() != Note.STATUS_PUBLISHED) {
            return Result.error("仅已发布的笔记可操作");
        }
        boolean added = userBehaviorService.toggleBehavior(userId, id, UserBehavior.BEHAVIOR_LIKE);
        if (added) {
            noteService.incrementLikes(id);
            userInterestService.addInterestFromNote(userId, id, new java.math.BigDecimal("0.5"));
        } else {
            noteService.decrementLikes(id);
        }
        return Result.success(added);
    }
    
    /**
     * 收藏笔记（切换状态）
     */
    @PostMapping("/{id}/collect")
    public Result collect(@PathVariable Integer id, @RequestParam Integer userId) {
        Note n = noteService.getById(id);
        if (n == null || n.getStatus() == null || n.getStatus() != Note.STATUS_PUBLISHED) {
            return Result.error("仅已发布的笔记可操作");
        }
        try {
            boolean isCollected = userCollectService.isCollected(userId, UserCollect.TYPE_NOTE, id);
            
            if (isCollected) {
                userCollectService.uncollect(userId, UserCollect.TYPE_NOTE, id);
                return Result.success(false);
            } else {
                userCollectService.collect(userId, UserCollect.TYPE_NOTE, id, null);
                userInterestService.addInterestFromNote(userId, id, new java.math.BigDecimal("0.75"));
                return Result.success(true);
            }
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 检查用户是否点赞/收藏了笔记
     */
    @AuthAccess
    @GetMapping("/{id}/behavior")
    public Result getBehavior(@PathVariable Integer id, @RequestParam Integer userId) {
        boolean liked = userBehaviorService.hasBehavior(userId, id, UserBehavior.BEHAVIOR_LIKE);
        boolean collected = userBehaviorService.hasBehavior(userId, id, UserBehavior.BEHAVIOR_COLLECT);
        Map<String, Boolean> result = new HashMap<>();
        result.put("liked", liked);
        result.put("collected", collected);
        return Result.success(result);
    }
    
    /**
     * 根据作者查询笔记
     */
    @AuthAccess
    @GetMapping("/author/{authorName}")
    public Result getByAuthor(@PathVariable String authorName) {
        QueryWrapper<Note> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("author_name", authorName);
        queryWrapper.eq("status", Note.STATUS_PUBLISHED);
        queryWrapper.orderByDesc("create_time");
        List<Note> list = noteService.list(queryWrapper);
        return Result.success(list);
    }
    
    /**
     * 获取关注用户的笔记（关注流）
     */
    @GetMapping("/following")
    public Result getFollowingNotes(@RequestParam Integer userId,
                                    @RequestParam(defaultValue = "1") Integer pageNum,
                                    @RequestParam(defaultValue = "20") Integer pageSize) {
        Page<Note> page = noteService.getFollowingUserNotes(userId, pageNum, pageSize);
        return Result.success(page);
    }
    
    /**
     * 获取热门推荐笔记
     */
    @AuthAccess
    @GetMapping("/hot")
    public Result getHotNotes(@RequestParam(defaultValue = "1") Integer pageNum,
                             @RequestParam(defaultValue = "20") Integer pageSize) {
        Page<Note> page = new Page<>(pageNum, pageSize);
        QueryWrapper<Note> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("status", Note.STATUS_PUBLISHED);
        queryWrapper.orderByDesc("likes", "views", "create_time");
        Page<Note> result = noteService.page(page, queryWrapper);
        return Result.success(result);
    }
    
    /**
     * 获取所有分类列表
     */
    @AuthAccess
    @GetMapping("/categories")
    public Result getCategories() {
        QueryWrapper<Note> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("DISTINCT category");
        queryWrapper.eq("status", Note.STATUS_PUBLISHED);
        queryWrapper.isNotNull("category");
        queryWrapper.ne("category", "");
        queryWrapper.orderByAsc("category");
        List<Note> list = noteService.list(queryWrapper);
        
        List<String> categories = list.stream()
                .map(Note::getCategory)
                .filter(category -> category != null && !category.trim().isEmpty())
                .distinct()
                .sorted()
                .collect(Collectors.toList());
        
        return Result.success(categories);
    }
    
    private static boolean isAdminAccount(Account cur) {
        return cur != null && cur.getId() != null && "ROLE_ADMIN".equals(cur.getRole());
    }
    
    private static boolean isUserAccount(Account cur) {
        return cur != null && cur.getId() != null && "ROLE_USER".equals(cur.getRole());
    }
    
    private static boolean canViewNonPublishedNote(Note note, Account cur) {
        if (cur == null || cur.getId() == null) {
            return false;
        }
        if ("ROLE_ADMIN".equals(cur.getRole())) {
            return true;
        }
        if ("ROLE_USER".equals(cur.getRole()) && note.getUserId() != null) {
            return note.getUserId().equals(cur.getId());
        }
        return false;
    }
}
