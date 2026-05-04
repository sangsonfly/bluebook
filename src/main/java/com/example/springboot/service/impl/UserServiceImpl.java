package com.example.springboot.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.springboot.common.Constants;
import com.example.springboot.entity.Account;
import com.example.springboot.entity.Club;
import com.example.springboot.entity.ClubMember;
import com.example.springboot.entity.Note;
import com.example.springboot.entity.User;
import com.example.springboot.entity.dto.UserVerifyApplyDTO;
import com.example.springboot.entity.vo.UserManageVO;
import com.example.springboot.mapper.ClubMapper;
import com.example.springboot.mapper.ClubMemberMapper;
import com.example.springboot.mapper.UserMapper;
import com.example.springboot.service.INoteService;
import com.example.springboot.service.IUserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.springboot.exception.ServiceException;
import com.example.springboot.utils.TokenUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import jakarta.annotation.Resource;
import java.util.List;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * <p>
 *  服务实现类
 * </p>
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {
    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);

    @Resource
    private UserMapper userMapper;
    
    @Resource
    @Lazy
    private INoteService noteService;

    @Resource
    private ClubMemberMapper clubMemberMapper;

    @Resource
    private ClubMapper clubMapper;

    @Override
    public Account login(Account account) {
        User one = getOne(Wrappers.<User>lambdaQuery().eq(User::getUsername, account.getUsername()).eq(User::getPassword, account.getPassword()));
        if (one != null) {
            String role = "ROLE_USER";
            BeanUtils.copyProperties(one,account);
            String token = TokenUtils.createToken( one.getId() + "-" + role, account.getPassword());
            account.setToken(token);
            account.setRole(role);
            account.setPassword(null);
            return account;
        } else {
            throw new ServiceException(Constants.CODE_605, "用户名或密码错误");
        }
    }

    @Override
    public void register(Account account) {
        User one = getOne(Wrappers.<User>lambdaQuery().eq(User::getUsername, account.getUsername()));
        if (one == null) {
            one = new User();
            BeanUtils.copyProperties(account, one);
            save(one);
        } else {
            throw new ServiceException(Constants.CODE_605, "用户已存在");
        }
    }

    @Override
    public void updatePassword(Account account) {
        LambdaUpdateWrapper<User> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(User::getUsername, account.getUsername());
        wrapper.eq(User::getPassword, account.getPassword());
        wrapper.set(User::getPassword, account.getNewPassword());
        // 执行更新操作
        int updateCount = userMapper.update(null, wrapper);
        // 检查更新结果
        if (updateCount == 0) {
            throw new ServiceException(Constants.CODE_605, "原密码输入错误，请检查后再试！");
        }
    }
    
    // ============ 关注功能相关方法 ============
    
    @Override
    public void incrementFollowingCount(Integer userId) {
        User user = getById(userId);
        if (user != null) {
            user.setFollowingCount(user.getFollowingCount() == null ? 1 : user.getFollowingCount() + 1);
            updateById(user);
        }
    }
    
    @Override
    public void decrementFollowingCount(Integer userId) {
        User user = getById(userId);
        if (user != null && user.getFollowingCount() != null && user.getFollowingCount() > 0) {
            user.setFollowingCount(user.getFollowingCount() - 1);
            updateById(user);
        }
    }
    
    @Override
    public void incrementFollowersCount(Integer userId) {
        User user = getById(userId);
        if (user != null) {
            user.setFollowersCount(user.getFollowersCount() == null ? 1 : user.getFollowersCount() + 1);
            updateById(user);
        }
    }
    
    @Override
    public void decrementFollowersCount(Integer userId) {
        User user = getById(userId);
        if (user != null && user.getFollowersCount() != null && user.getFollowersCount() > 0) {
            user.setFollowersCount(user.getFollowersCount() - 1);
            updateById(user);
        }
    }
    
    @Override
    public com.baomidou.mybatisplus.core.metadata.IPage<User> getFollowingUsers(Integer userId, com.baomidou.mybatisplus.extension.plugins.pagination.Page<User> page) {
        // 通过自定义SQL查询关注的用户列表
        return userMapper.getFollowingUsers(userId, page);
    }
    
    @Override
    public com.baomidou.mybatisplus.core.metadata.IPage<User> getFollowerUsers(Integer userId, com.baomidou.mybatisplus.extension.plugins.pagination.Page<User> page) {
        // 通过自定义SQL查询粉丝列表
        return userMapper.getFollowerUsers(userId, page);
    }
    
    // ============ 笔记统计相关方法 ============
    
    @Override
    public void incrementNotesCount(Integer userId) {
        User user = getById(userId);
        if (user != null) {
            user.setNotesCount(user.getNotesCount() == null ? 1 : user.getNotesCount() + 1);
            updateById(user);
        }
    }
    
    @Override
    public void decrementNotesCount(Integer userId) {
        User user = getById(userId);
        if (user != null && user.getNotesCount() != null && user.getNotesCount() > 0) {
            user.setNotesCount(user.getNotesCount() - 1);
            updateById(user);
        }
    }
    
    // ============ 懒更新缓存相关方法 ============
    
    @Override
    public User getUserWithUpdatedStats(Integer userId) {
        User user = getById(userId);
        if (user == null) {
            return null;
        }
        
        try {
            // 实时计算实际笔记数（从Note表查询，只统计已发布的笔记）
            Long actualNotesCount = noteService.count(
                Wrappers.<Note>lambdaQuery()
                    .eq(Note::getUserId, userId)
                    .eq(Note::getStatus, 1)  // status = 1 表示已发布
            );
            
            // 实时计算获赞总数（该用户所有已发布笔记的点赞数之和）
            List<Note> notes = noteService.list(
                Wrappers.<Note>lambdaQuery()
                    .eq(Note::getUserId, userId)
                    .eq(Note::getStatus, 1)
                    .select(Note::getLikes)  // 只查询点赞数字段，提升性能
            );
            Long totalLikes = notes.stream()
                .mapToLong(note -> note.getLikes() != null ? note.getLikes() : 0)
                .sum();
            
            int actualNotesCountInt = actualNotesCount.intValue();
            int totalLikesInt = totalLikes.intValue();
            
            // 检查缓存值是否与实际值一致
            Integer cachedNotesCount = user.getNotesCount();
            Integer cachedLikesReceived = user.getLikesReceived();
            
            boolean needUpdate = false;
            if (cachedNotesCount == null || !cachedNotesCount.equals(actualNotesCountInt)) {
                needUpdate = true;
            }
            if (cachedLikesReceived == null || !cachedLikesReceived.equals(totalLikesInt)) {
                needUpdate = true;
            }
            
            // 如果不一致，异步更新数据库缓存（不阻塞响应）
            if (needUpdate) {
                final int finalNotesCount = actualNotesCountInt;
                final int finalLikesReceived = totalLikesInt;
                CompletableFuture.runAsync(() -> {
                    try {
                        LambdaUpdateWrapper<User> updateWrapper = new LambdaUpdateWrapper<>();
                        updateWrapper.eq(User::getId, userId)
                                    .set(User::getNotesCount, finalNotesCount)
                                    .set(User::getLikesReceived, finalLikesReceived);
                        update(updateWrapper);
                    } catch (Exception e) {
                        // 异步更新失败不影响主请求，只记录日志
                        System.err.println("异步更新用户统计数据失败: " + e.getMessage());
                    }
                });
            }
            
            // 返回用户信息（使用实际值，确保前端看到的是准确数据）
            user.setNotesCount(actualNotesCountInt);
            user.setLikesReceived(totalLikesInt);
            
        } catch (Exception e) {
            // 如果计算失败，返回缓存的旧值，不影响主功能
            System.err.println("计算用户统计数据失败: " + e.getMessage());
        }
        
        return user;
    }

    @Override
    public IPage<UserManageVO> getManagePage(Integer pageNum, Integer pageSize, String keyword, Integer isVerified) {
        boolean hasKeyword = com.baomidou.mybatisplus.core.toolkit.StringUtils.isNotBlank(keyword);
        log.info("Load user manage page: pageNum={}, pageSize={}, hasKeyword={}, isVerified={}",
                pageNum, pageSize, hasKeyword, isVerified);

        LambdaQueryWrapper<User> queryWrapper = Wrappers.<User>lambdaQuery()
                .and(hasKeyword, c -> c
                        .like(User::getNickname, keyword)
                        .or()
                        .like(User::getUsername, keyword)
                        .or()
                        .like(User::getPhone, keyword)
                        .or()
                        .like(User::getEmail, keyword)
                )
                .eq(isVerified != null, User::getIsVerified, isVerified)
                .orderByDesc(User::getId);

        IPage<User> userPage = this.page(new Page<>(pageNum, pageSize), queryWrapper);

        List<User> users = userPage.getRecords();
        List<Integer> userIds = users.stream().map(User::getId).toList();

        Map<Integer, List<ClubMember>> memberMapByUserIdTemp = Collections.emptyMap();
        Map<Integer, String> clubNameMapTemp = Collections.emptyMap();
        if (!userIds.isEmpty()) {
            List<ClubMember> members = clubMemberMapper.selectList(
                    Wrappers.<ClubMember>lambdaQuery()
                            .in(ClubMember::getUserId, userIds)
                            .eq(ClubMember::getStatus, 1)
            );
            memberMapByUserIdTemp = members.stream().collect(Collectors.groupingBy(ClubMember::getUserId));

            Set<Integer> clubIds = members.stream()
                    .map(ClubMember::getClubId)
                    .collect(Collectors.toSet());
            if (!clubIds.isEmpty()) {
                clubNameMapTemp = clubMapper.selectList(
                        Wrappers.<Club>lambdaQuery()
                                .in(Club::getId, clubIds)
                                .select(Club::getId, Club::getName)
                ).stream().collect(Collectors.toMap(Club::getId, Club::getName, (a, b) -> a));
            }
        }
        final Map<Integer, List<ClubMember>> memberMapByUserId = memberMapByUserIdTemp;
        final Map<Integer, String> clubNameMap = clubNameMapTemp;

        List<UserManageVO> records = users.stream().map(user -> {
            UserManageVO vo = new UserManageVO();
            org.springframework.beans.BeanUtils.copyProperties(user, vo);

            List<ClubMember> userMembers = memberMapByUserId.getOrDefault(user.getId(), Collections.emptyList());
            vo.setClubCount(userMembers.size());

            String clubNames = userMembers.stream()
                    .map(ClubMember::getClubId)
                    .map(clubNameMap::get)
                    .filter(com.baomidou.mybatisplus.core.toolkit.StringUtils::isNotBlank)
                    .distinct()
                    .limit(3)
                    .collect(Collectors.joining("、"));
            vo.setClubNames(clubNames);
            return vo;
        }).toList();

        Page<UserManageVO> result = new Page<>(pageNum, pageSize);
        result.setTotal(userPage.getTotal());
        result.setRecords(records);
        return result;
    }

    @Override
    public void resetPassword(Integer userId, String newPassword) {
        if (userId == null || com.baomidou.mybatisplus.core.toolkit.StringUtils.isBlank(newPassword)) {
            throw new ServiceException(Constants.CODE_400, "用户ID和新密码不能为空");
        }

        boolean updated = this.lambdaUpdate()
                .eq(User::getId, userId)
                .set(User::getPassword, newPassword)
                .update();
        if (!updated) {
            throw new ServiceException(Constants.CODE_605, "用户不存在");
        }
    }

    @Override
    public void applyVerification(Integer userId, UserVerifyApplyDTO dto) {
        if (userId == null) {
            throw new ServiceException(Constants.CODE_400, "用户ID不能为空");
        }
        if (dto == null) {
            throw new ServiceException(Constants.CODE_400, "认证资料不能为空");
        }
        if (com.baomidou.mybatisplus.core.toolkit.StringUtils.isBlank(dto.getStudentId())
                || com.baomidou.mybatisplus.core.toolkit.StringUtils.isBlank(dto.getRealName())
                || com.baomidou.mybatisplus.core.toolkit.StringUtils.isBlank(dto.getSchool())) {
            throw new ServiceException(Constants.CODE_400, "学号、真实姓名、学校为必填项");
        }
        String studentId = dto.getStudentId().trim();
        if (studentId.length() < 6 || studentId.length() > 50) {
            throw new ServiceException(Constants.CODE_400, "学号长度应在 6-50 位之间");
        }

        User user = getById(userId);
        if (user == null) {
            throw new ServiceException(Constants.CODE_605, "用户不存在");
        }
        if (User.VERIFY_STATUS_VERIFIED == (user.getIsVerified() == null
                ? User.VERIFY_STATUS_UNVERIFIED
                : user.getIsVerified())) {
            throw new ServiceException(Constants.CODE_400, "您已通过校园认证，无需重复提交");
        }
        if (User.VERIFY_STATUS_PENDING == user.getIsVerified()) {
            throw new ServiceException(Constants.CODE_400, "认证申请正在审核中，请勿重复提交");
        }

        user.setStudentId(studentId);
        user.setRealName(dto.getRealName() == null ? null : dto.getRealName().trim());
        user.setSchool(dto.getSchool() == null ? null : dto.getSchool().trim());
        user.setCollege(dto.getCollege() == null ? null : dto.getCollege().trim());
        user.setMajor(dto.getMajor() == null ? null : dto.getMajor().trim());
        user.setGrade(dto.getGrade() == null ? null : dto.getGrade().trim());
        user.setIsVerified(User.VERIFY_STATUS_PENDING);
        user.setVerifyTime(null);

        updateById(user);
    }

    @Override
    public void reviewVerification(Integer userId, boolean approved) {
        if (userId == null) {
            throw new ServiceException(Constants.CODE_400, "用户ID不能为空");
        }
        User user = getById(userId);
        if (user == null) {
            throw new ServiceException(Constants.CODE_605, "用户不存在");
        }

        if (approved) {
            user.setIsVerified(User.VERIFY_STATUS_VERIFIED);
            user.setVerifyTime(LocalDateTime.now());
        } else {
            user.setIsVerified(User.VERIFY_STATUS_UNVERIFIED);
            user.setVerifyTime(null);
        }
        updateById(user);
    }

    @Override
    public IPage<User> getVerifyPage(Integer pageNum, Integer pageSize, String keyword, Integer status) {
        LambdaQueryWrapper<User> queryWrapper = Wrappers.<User>lambdaQuery()
                .and(com.baomidou.mybatisplus.core.toolkit.StringUtils.isNotBlank(keyword), c -> c
                        .like(User::getNickname, keyword)
                        .or()
                        .like(User::getUsername, keyword)
                        .or()
                        .like(User::getStudentId, keyword)
                        .or()
                        .like(User::getRealName, keyword)
                        .or()
                        .like(User::getSchool, keyword)
                )
                .eq(status != null, User::getIsVerified, status)
                .orderByDesc(User::getId);
        return this.page(new Page<>(pageNum, pageSize), queryWrapper);
    }

}
