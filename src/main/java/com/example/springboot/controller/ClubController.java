package com.example.springboot.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.springboot.common.Result;
import com.example.springboot.config.interceptor.AuthAccess;
import com.example.springboot.entity.Club;
import com.example.springboot.service.IClubService;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import java.util.List;

/**
 * 社团Controller
 */
@RestController
@RequestMapping("/api/club")
public class ClubController {
    
    @Resource
    private IClubService clubService;
    
    /**
     * 查询所有社团
     */
    @AuthAccess
    @GetMapping("/list")
    public Result list() {
        QueryWrapper<Club> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("status", 1); // 只查询正常状态的社团
        queryWrapper.orderByDesc("member_count"); // 按成员数量倒序
        List<Club> list = clubService.list(queryWrapper);
        return Result.success(list);
    }
    
    /**
     * 查询认证社团
     */
    @AuthAccess
    @GetMapping("/verified")
    public Result verified() {
        QueryWrapper<Club> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("is_verified", 1);
        queryWrapper.eq("status", 1);
        queryWrapper.orderByDesc("member_count");
        List<Club> list = clubService.list(queryWrapper);
        return Result.success(list);
    }
    
    /**
     * 分页查询社团
     */
    @GetMapping("/page")
    public Result page(@RequestParam(defaultValue = "1") Integer pageNum,
                       @RequestParam(defaultValue = "10") Integer pageSize,
                       @RequestParam(required = false) String category) {
        Page<Club> page = new Page<>(pageNum, pageSize);
        QueryWrapper<Club> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("status", 1);
        
        if (category != null && !category.isEmpty()) {
            queryWrapper.eq("category", category);
        }
        
        queryWrapper.orderByDesc("member_count");
        Page<Club> result = clubService.page(page, queryWrapper);
        return Result.success(result);
    }

    /**
     * 后台社团管理分页（聚合社长和管理员信息）
     */
    @GetMapping("/adminPage")
    public Result adminPage(@RequestParam(defaultValue = "1") Integer pageNum,
                            @RequestParam(defaultValue = "10") Integer pageSize,
                            @RequestParam(defaultValue = "") String keyword,
                            @RequestParam(defaultValue = "") String category,
                            @RequestParam(required = false) Integer isVerified) {
        return Result.success(clubService.getAdminPage(pageNum, pageSize, keyword, category, isVerified));
    }
    
    /**
     * 根据ID查询社团详情
     */
    @AuthAccess
    @GetMapping("/{id}")
    public Result getById(@PathVariable Integer id) {
        Club club = clubService.getById(id);
        return Result.success(club);
    }
    
    /**
     * 新增社团
     */
    @PostMapping
    public Result save(@RequestBody Club club) {
        clubService.save(club);
        return Result.success();
    }
    
    /**
     * 更新社团
     */
    @PutMapping
    public Result update(@RequestBody Club club) {
        clubService.updateById(club);
        return Result.success();
    }
    
    /**
     * 删除社团
     */
    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Integer id) {
        clubService.removeById(id);
        return Result.success();
    }
    
    /**
     * 申请社团认证
     */
    @PostMapping("/applyVerification/{clubId}")
    public Result applyVerification(@PathVariable Integer clubId) {
        try {
            boolean success = clubService.applyVerification(clubId);
            return success ? Result.success("认证申请已提交") : Result.error();
        } catch (Exception e) {
            return Result.error();
        }
    }
    
    /**
     * 审核社团认证（管理员）
     */
    @PostMapping("/reviewVerification")
    public Result reviewVerification(
            @RequestParam Integer clubId,
            @RequestParam Boolean approved) {
        try {
            boolean success = clubService.reviewVerification(clubId, approved);
            return success ? Result.success() : Result.error();
        } catch (Exception e) {
            return Result.error();
        }
    }
    
    /**
     * 搜索社团
     */
    @AuthAccess
    @GetMapping("/search")
    public Result search(@RequestParam String keyword) {
        QueryWrapper<Club> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("status", 1);
        queryWrapper.and(wrapper -> wrapper
            .like("name", keyword)
            .or()
            .like("description", keyword));
        queryWrapper.orderByDesc("member_count");
        List<Club> list = clubService.list(queryWrapper);
        return Result.success(list);
    }
    
    /**
     * 按分类查询社团
     */
    @AuthAccess
    @GetMapping("/category/{category}")
    public Result getByCategory(@PathVariable String category) {
        QueryWrapper<Club> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("category", category);
        queryWrapper.eq("status", 1);
        queryWrapper.orderByDesc("member_count");
        List<Club> list = clubService.list(queryWrapper);
        return Result.success(list);
    }
}

