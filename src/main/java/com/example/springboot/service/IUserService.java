package com.example.springboot.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.springboot.entity.Account;
import com.example.springboot.entity.User;
import com.example.springboot.entity.dto.UserVerifyApplyDTO;
import com.example.springboot.entity.vo.UserManageVO;

/**
 * <p>
 *  用户服务接口
 * </p>
 */
public interface IUserService extends IService<User> {

    Account login(Account account);

    void register(Account account);

    void updatePassword(Account account);
    
    // ============ 关注功能相关方法 ============
    
    /**
     * 增加关注数
     */
    void incrementFollowingCount(Integer userId);
    
    /**
     * 减少关注数
     */
    void decrementFollowingCount(Integer userId);
    
    /**
     * 增加粉丝数
     */
    void incrementFollowersCount(Integer userId);
    
    /**
     * 减少粉丝数
     */
    void decrementFollowersCount(Integer userId);
    
    /**
     * 获取关注列表（我关注的人）
     */
    IPage<User> getFollowingUsers(Integer userId, Page<User> page);
    
    /**
     * 获取粉丝列表（关注我的人）
     */
    IPage<User> getFollowerUsers(Integer userId, Page<User> page);
    
    // ============ 笔记统计相关方法 ============
    
    /**
     * 增加笔记数
     */
    void incrementNotesCount(Integer userId);
    
    /**
     * 减少笔记数
     */
    void decrementNotesCount(Integer userId);
    
    /**
     * 获取用户信息（带统计数据的懒更新）
     * 如果缓存的统计数据与实际值不一致，会自动更新缓存
     * 
     * @param userId 用户ID
     * @return 用户信息（包含更新后的统计数据）
     */
    User getUserWithUpdatedStats(Integer userId);

    /**
     * 后台用户管理分页（包含社团摘要信息）
     *
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @param keyword 关键词（昵称/用户名/手机号/邮箱）
     * @return 用户管理分页数据
     */
    IPage<UserManageVO> getManagePage(Integer pageNum, Integer pageSize, String keyword, Integer isVerified);

    /**
     * 管理员重置用户密码
     *
     * @param userId 用户ID
     * @param newPassword 新密码
     */
    void resetPassword(Integer userId, String newPassword);

    /**
     * 用户提交校园认证申请
     */
    void applyVerification(Integer userId, UserVerifyApplyDTO dto);

    /**
     * 管理员审核用户认证
     */
    void reviewVerification(Integer userId, boolean approved);

    /**
     * 管理员查看认证审核分页
     */
    IPage<User> getVerifyPage(Integer pageNum, Integer pageSize, String keyword, Integer status);

}
