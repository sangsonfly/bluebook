-- ================================================
-- 校园蓝珊笔记完整数据库脚本 V2.0
-- 支持协同过滤推荐、活动报名、二手交易等完整功能
-- ================================================

-- 创建数据库（如果需要）
-- CREATE DATABASE IF NOT EXISTS bluebook DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
-- USE bluebook;

-- ================================================
-- 1. 用户相关表
-- ================================================

-- 管理员表
DROP TABLE IF EXISTS `sys_admin`;
CREATE TABLE `sys_admin` (
  `id` INT NOT NULL AUTO_INCREMENT COMMENT 'id',
  `username` VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '用户名',
  `password` VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '密码',
  `nickname` VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '昵称',
  `avatar_url` VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '头像',
  `email` VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '邮箱',
  `phone` VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '电话',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='管理员表';

-- 用户表（增强版）
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user` (
  `id` INT NOT NULL AUTO_INCREMENT COMMENT 'id',
  `username` VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '用户名',
  `password` VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '密码',
  `nickname` VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '昵称',
  `avatar_url` VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '头像',
  `email` VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '邮箱',
  `phone` VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '电话',
  
  -- 校园认证相关
  `student_id` VARCHAR(50) DEFAULT NULL COMMENT '学号',
  `real_name` VARCHAR(100) DEFAULT NULL COMMENT '真实姓名',
  `is_verified` TINYINT DEFAULT 0 COMMENT '是否认证 1-是 0-否',
  `verify_time` DATETIME DEFAULT NULL COMMENT '认证时间',
  `school` VARCHAR(255) DEFAULT NULL COMMENT '学校',
  `college` VARCHAR(255) DEFAULT NULL COMMENT '学院',
  `major` VARCHAR(255) DEFAULT NULL COMMENT '专业',
  `grade` VARCHAR(50) DEFAULT NULL COMMENT '年级',
  
  -- 社交数据统计
  `following_count` INT DEFAULT 0 COMMENT '关注数',
  `followers_count` INT DEFAULT 0 COMMENT '粉丝数',
  `notes_count` INT DEFAULT 0 COMMENT '笔记数',
  `likes_received` INT DEFAULT 0 COMMENT '获赞总数',
  
  -- 个人信息
  `bio` VARCHAR(500) DEFAULT NULL COMMENT '个人简介',
  `gender` TINYINT DEFAULT 0 COMMENT '性别 0-未知 1-男 2-女',
  
  -- 账号状态
  `status` TINYINT DEFAULT 1 COMMENT '状态 1-正常 0-禁用',
  `account_type` TINYINT DEFAULT 1 COMMENT '账号类型 1-普通用户 2-社团账号 3-机构账号 4-企业账号',
  `register_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
  `last_login_time` DATETIME DEFAULT NULL COMMENT '最后登录时间',
  
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_username` (`username`),
  KEY `idx_student_id` (`student_id`),
  KEY `idx_is_verified` (`is_verified`),
  KEY `idx_account_type` (`account_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- ================================================
-- 2. 内容相关表
-- ================================================

-- 笔记表（增强版）
DROP TABLE IF EXISTS `note`;
CREATE TABLE `note` (
  `id` INT NOT NULL AUTO_INCREMENT COMMENT 'id',
  `title` VARCHAR(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '标题',
  `content` TEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '内容',
  `image_url` VARCHAR(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '图片URL（多图用逗号分隔）',
  
  -- 发布者信息
  `user_id` INT DEFAULT NULL COMMENT '发布用户ID',
  `club_id` INT DEFAULT NULL COMMENT '社团ID（社团发布时填写）',
  `publish_type` TINYINT DEFAULT 1 COMMENT '发布类型 1-个人 2-社团',
  `author_name` VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '作者名称',
  `author_avatar` VARCHAR(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '作者头像',
  
  -- 互动数据
  `likes` INT DEFAULT 0 COMMENT '点赞数',
  `views` INT DEFAULT 0 COMMENT '浏览数',
  `collects` INT DEFAULT 0 COMMENT '收藏数',
  `comments` INT DEFAULT 0 COMMENT '评论数',
  
  -- 分类与标签
  `tags` VARCHAR(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '标签（逗号分隔）',
  `category` VARCHAR(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '分类',
  
  -- 状态
  `is_official` TINYINT DEFAULT 0 COMMENT '是否官方内容 1-是 0-否',
  `status` INT DEFAULT 1 COMMENT '状态 0-草稿 1-已发布 2-下架 -1-已删除(软删)',
  
  `create_time` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_user_id` (`user_id`),
  KEY `idx_club_id` (`club_id`),
  KEY `idx_category` (`category`),
  KEY `idx_status` (`status`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='笔记表';

-- 评论表
DROP TABLE IF EXISTS `comment`;
CREATE TABLE `comment` (
  `id` INT NOT NULL AUTO_INCREMENT COMMENT 'id',
  `note_id` INT DEFAULT NULL COMMENT '笔记ID',
  `user_id` INT DEFAULT NULL COMMENT '评论用户ID',
  `content` TEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '评论内容',
  `parent_id` INT DEFAULT NULL COMMENT '父评论ID（回复功能）',
  `reply_to_user_id` INT DEFAULT NULL COMMENT '回复给谁（@功能）',
  `likes` INT DEFAULT 0 COMMENT '点赞数',
  `create_time` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_note_id` (`note_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_parent_id` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='评论表';

-- 评论点赞表
DROP TABLE IF EXISTS `comment_like`;
CREATE TABLE `comment_like` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `user_id` INT NOT NULL COMMENT '用户ID',
  `comment_id` INT NOT NULL COMMENT '评论ID',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  UNIQUE KEY `uk_user_comment` (`user_id`, `comment_id`),
  KEY `idx_comment_id` (`comment_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评论点赞表';

-- ================================================
-- 3. 社团相关表
-- ================================================

-- 社团表（增强版）
DROP TABLE IF EXISTS `club`;
CREATE TABLE `club` (
  `id` INT NOT NULL AUTO_INCREMENT COMMENT 'id',
  `name` VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '社团名称',
  `description` TEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '社团简介',
  `avatar_url` VARCHAR(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '社团头像',
  `cover_url` VARCHAR(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '封面图',
  `category` VARCHAR(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '社团类别',
  
  -- 管理信息
  `admin_user_id` INT DEFAULT NULL COMMENT '社团管理员用户ID（对应sys_user表的社团账号）',
  `contact_info` VARCHAR(500) DEFAULT NULL COMMENT '联系方式（JSON格式）',
  
  -- 统计数据
  `member_count` INT DEFAULT 0 COMMENT '成员数量',
  `activity_count` INT DEFAULT 0 COMMENT '活动数量',
  
  -- 认证状态
  `is_verified` INT DEFAULT 0 COMMENT '是否认证 1-是 0-否',
  `verify_time` DATETIME DEFAULT NULL COMMENT '认证时间',
  
  `status` INT DEFAULT 1 COMMENT '状态 1-正常 0-禁用',
  `create_time` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_admin_user` (`admin_user_id`),
  KEY `idx_is_verified` (`is_verified`),
  KEY `idx_category` (`category`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='社团表';

-- 社团成员表
DROP TABLE IF EXISTS `club_member`;
CREATE TABLE `club_member` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'id',
  `club_id` INT NOT NULL COMMENT '社团ID',
  `user_id` INT NOT NULL COMMENT '用户ID',
  `role` TINYINT DEFAULT 1 COMMENT '角色 1-普通成员 2-管理员 3-社长',
  `status` TINYINT DEFAULT 1 COMMENT '状态 1-正常 0-已退出',
  `join_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '加入时间',
  
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_club_user` (`club_id`, `user_id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='社团成员表';

-- 活动表
DROP TABLE IF EXISTS `activity`;
CREATE TABLE `activity` (
  `id` INT NOT NULL AUTO_INCREMENT COMMENT 'id',
  `club_id` INT NOT NULL COMMENT '发布社团ID',
  `title` VARCHAR(255) NOT NULL COMMENT '活动标题',
  `description` TEXT COMMENT '活动描述',
  `cover_url` VARCHAR(500) COMMENT '封面图',
  `location` VARCHAR(255) COMMENT '活动地点',
  `start_time` DATETIME NOT NULL COMMENT '开始时间',
  `end_time` DATETIME NOT NULL COMMENT '结束时间',
  `max_participants` INT DEFAULT NULL COMMENT '最大参与人数（NULL为不限）',
  `current_participants` INT DEFAULT 0 COMMENT '当前报名人数',
  `need_approval` TINYINT DEFAULT 0 COMMENT '是否需要审核 1-是 0-否',
  `tags` VARCHAR(500) COMMENT '活动标签',
  `status` TINYINT DEFAULT 1 COMMENT '状态 1-报名中 2-进行中 3-已结束 0-已取消',
  `create_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  
  PRIMARY KEY (`id`),
  KEY `idx_club_id` (`club_id`),
  KEY `idx_start_time` (`start_time`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='活动表';

-- 活动报名表
DROP TABLE IF EXISTS `activity_registration`;
CREATE TABLE `activity_registration` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'id',
  `activity_id` INT NOT NULL COMMENT '活动ID',
  `user_id` INT NOT NULL COMMENT '用户ID',
  `status` TINYINT DEFAULT 0 COMMENT '状态 0-待审核 1-已通过 2-已拒绝 3-已签到 4-已取消',
  `remark` VARCHAR(500) COMMENT '报名备注',
  `review_remark` VARCHAR(500) COMMENT '审核备注',
  `create_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '报名时间',
  `review_time` DATETIME DEFAULT NULL COMMENT '审核时间',
  
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_activity_user` (`activity_id`, `user_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='活动报名表';

-- ================================================
-- 4. 推荐系统相关表（核心）
-- ================================================

-- 用户行为表（推荐算法基础数据）
DROP TABLE IF EXISTS `user_behavior`;
CREATE TABLE `user_behavior` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'id',
  `user_id` INT NOT NULL COMMENT '用户ID',
  `note_id` INT NOT NULL COMMENT '笔记ID',
  `behavior_type` TINYINT NOT NULL COMMENT '行为类型 1-浏览 2-点赞 3-收藏 4-评论 5-分享',
  `weight` DECIMAL(3,2) DEFAULT 1.0 COMMENT '行为权重（浏览0.5，点赞1.0，收藏1.5，评论2.0，分享2.5）',
  `duration` INT DEFAULT 0 COMMENT '停留时长（秒，仅浏览时有效）',
  `create_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_note_id` (`note_id`),
  KEY `idx_behavior_type` (`behavior_type`),
  KEY `idx_create_time` (`create_time`),
  KEY `idx_user_note` (`user_id`, `note_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户行为记录表-推荐算法核心';

-- 用户兴趣标签表（推荐算法辅助）
DROP TABLE IF EXISTS `user_interest`;
CREATE TABLE `user_interest` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'id',
  `user_id` INT NOT NULL COMMENT '用户ID',
  `tag` VARCHAR(100) NOT NULL COMMENT '兴趣标签',
  `weight` DECIMAL(5,2) DEFAULT 1.0 COMMENT '权重（根据行为累计）',
  `update_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_tag` (`user_id`, `tag`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户兴趣标签表';

-- ================================================
-- 5. 社交互动详情表（推荐算法核心数据源）
-- ================================================

-- 用户点赞表（推荐算法关键数据）
DROP TABLE IF EXISTS `user_like`;
CREATE TABLE `user_like` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'id',
  `user_id` INT NOT NULL COMMENT '用户ID',
  `target_type` TINYINT NOT NULL COMMENT '点赞对象类型 1-笔记 2-评论 3-答案',
  `target_id` INT NOT NULL COMMENT '点赞对象ID',
  `create_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '点赞时间',
  
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_target` (`user_id`, `target_type`, `target_id`),
  KEY `idx_target` (`target_type`, `target_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户点赞表-推荐算法数据源';

-- 用户收藏表（推荐算法高权重数据）
DROP TABLE IF EXISTS `user_collect`;
CREATE TABLE `user_collect` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'id',
  `user_id` INT NOT NULL COMMENT '用户ID',
  `target_type` TINYINT NOT NULL COMMENT '收藏对象类型 1-笔记 2-二手商品 3-问题',
  `target_id` INT NOT NULL COMMENT '收藏对象ID',
  `folder_name` VARCHAR(100) DEFAULT '默认收藏夹' COMMENT '收藏夹名称',
  `create_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '收藏时间',
  
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_target` (`user_id`, `target_type`, `target_id`),
  KEY `idx_user_folder` (`user_id`, `folder_name`),
  KEY `idx_target` (`target_type`, `target_id`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户收藏表-推荐算法数据源';

-- 用户分享表
DROP TABLE IF EXISTS `user_share`;
CREATE TABLE `user_share` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'id',
  `user_id` INT NOT NULL COMMENT '分享用户ID',
  `target_type` TINYINT NOT NULL COMMENT '分享对象类型 1-笔记 2-活动 3-二手商品',
  `target_id` INT NOT NULL COMMENT '分享对象ID',
  `platform` VARCHAR(50) DEFAULT 'internal' COMMENT '分享平台（internal-站内/wechat-微信/qq-QQ等）',
  `create_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '分享时间',
  
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_target` (`target_type`, `target_id`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='分享记录表';

-- ================================================
-- 6. 标签系统（独立管理，优化推荐算法）
-- ================================================

-- 标签表
DROP TABLE IF EXISTS `tag`;
CREATE TABLE `tag` (
  `id` INT NOT NULL AUTO_INCREMENT COMMENT 'id',
  `name` VARCHAR(50) NOT NULL COMMENT '标签名称',
  `category` VARCHAR(50) DEFAULT 'general' COMMENT '标签分类（学习/生活/社团/运动等）',
  `use_count` INT DEFAULT 0 COMMENT '使用次数',
  `status` TINYINT DEFAULT 1 COMMENT '状态 1-正常 0-禁用',
  `create_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_name` (`name`),
  KEY `idx_use_count` (`use_count` DESC),
  KEY `idx_category` (`category`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='标签表';

-- 笔记标签关联表
DROP TABLE IF EXISTS `note_tag`;
CREATE TABLE `note_tag` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'id',
  `note_id` INT NOT NULL COMMENT '笔记ID',
  `tag_id` INT NOT NULL COMMENT '标签ID',
  `create_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_note_tag` (`note_id`, `tag_id`),
  KEY `idx_tag_id` (`tag_id`),
  KEY `idx_note_id` (`note_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='笔记标签关联表';

-- ================================================
-- 7. 社交功能相关表
-- ================================================

-- 用户关注表
DROP TABLE IF EXISTS `user_follow`;
CREATE TABLE `user_follow` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'id',
  `follower_id` INT NOT NULL COMMENT '关注者ID（粉丝）',
  `followee_id` INT NOT NULL COMMENT '被关注者ID',
  `status` TINYINT DEFAULT 1 COMMENT '状态 1-正常 0-取消',
  `create_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '关注时间',
  
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_follow` (`follower_id`, `followee_id`),
  KEY `idx_follower` (`follower_id`),
  KEY `idx_followee` (`followee_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户关注关系表';

-- 消息通知表
DROP TABLE IF EXISTS `notification`;
CREATE TABLE `notification` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'id',
  `receiver_id` INT NOT NULL COMMENT '接收者ID',
  `sender_id` INT DEFAULT NULL COMMENT '发送者ID（系统通知为NULL）',
  `type` TINYINT NOT NULL COMMENT '通知类型 1-点赞 2-评论 3-关注 4-系统通知 5-活动通知 6-回复',
  `content` VARCHAR(500) COMMENT '通知内容',
  `related_type` TINYINT DEFAULT NULL COMMENT '关联类型 1-笔记 2-评论 3-活动',
  `related_id` INT DEFAULT NULL COMMENT '关联对象ID',
  `is_read` TINYINT DEFAULT 0 COMMENT '是否已读 1-是 0-否',
  `create_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  
  PRIMARY KEY (`id`),
  KEY `idx_receiver` (`receiver_id`),
  KEY `idx_is_read` (`is_read`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='消息通知表';

-- 私信表
DROP TABLE IF EXISTS `private_message`;
CREATE TABLE `private_message` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'id',
  `sender_id` INT NOT NULL COMMENT '发送者ID',
  `receiver_id` INT NOT NULL COMMENT '接收者ID',
  `content` TEXT NOT NULL COMMENT '消息内容',
  `is_read` TINYINT DEFAULT 0 COMMENT '是否已读 1-是 0-否',
  `create_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  
  PRIMARY KEY (`id`),
  KEY `idx_receiver` (`receiver_id`),
  KEY `idx_sender` (`sender_id`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='私信表';

-- ================================================
-- 8. 二手交易相关表
-- ================================================

-- 二手商品表
DROP TABLE IF EXISTS `secondhand_item`;
CREATE TABLE `secondhand_item` (
  `id` INT NOT NULL AUTO_INCREMENT COMMENT 'id',
  `user_id` INT NOT NULL COMMENT '发布用户ID',
  `title` VARCHAR(255) NOT NULL COMMENT '商品标题',
  `description` TEXT COMMENT '商品描述',
  `images` VARCHAR(2000) COMMENT '商品图片（JSON数组或逗号分隔）',
  `price` DECIMAL(10,2) NOT NULL COMMENT '价格',
  `original_price` DECIMAL(10,2) DEFAULT NULL COMMENT '原价',
  `category` VARCHAR(100) COMMENT '分类（教材/电子产品/日用品/运动器材/其他）',
  `condition` TINYINT DEFAULT 1 COMMENT '新旧程度 1-全新 2-几乎全新 3-轻微使用 4-明显使用',
  `status` TINYINT DEFAULT 1 COMMENT '状态 1-在售 2-已预订 3-已售出 0-已下架',
  `views` INT DEFAULT 0 COMMENT '浏览量',
  `location` VARCHAR(255) COMMENT '交易地点',
  `contact_info` VARCHAR(500) COMMENT '联系方式',
  `create_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '发布时间',
  `update_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_status` (`status`),
  KEY `idx_category` (`category`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='二手商品表';

-- ================================================
-- 9. 问答模块相关表
-- ================================================

-- 问题表
DROP TABLE IF EXISTS `question`;
CREATE TABLE `question` (
  `id` INT NOT NULL AUTO_INCREMENT COMMENT 'id',
  `user_id` INT NOT NULL COMMENT '提问用户ID',
  `title` VARCHAR(500) NOT NULL COMMENT '问题标题',
  `content` TEXT NOT NULL COMMENT '问题描述',
  `images` VARCHAR(1000) COMMENT '图片URL',
  `tags` VARCHAR(500) COMMENT '标签（逗号分隔）',
  `category` VARCHAR(100) COMMENT '分类',
  `views` INT DEFAULT 0 COMMENT '浏览数',
  `answer_count` INT DEFAULT 0 COMMENT '回答数',
  `best_answer_id` INT DEFAULT NULL COMMENT '最佳答案ID',
  `reward_points` INT DEFAULT 0 COMMENT '悬赏积分',
  `status` TINYINT DEFAULT 1 COMMENT '状态 1-待解决 2-已解决',
  `create_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_status` (`status`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='问题表';

-- 答案表
DROP TABLE IF EXISTS `answer`;
CREATE TABLE `answer` (
  `id` INT NOT NULL AUTO_INCREMENT COMMENT 'id',
  `question_id` INT NOT NULL COMMENT '问题ID',
  `user_id` INT NOT NULL COMMENT '回答用户ID',
  `content` TEXT NOT NULL COMMENT '回答内容',
  `images` VARCHAR(1000) COMMENT '图片URL',
  `likes` INT DEFAULT 0 COMMENT '点赞数',
  `is_best` TINYINT DEFAULT 0 COMMENT '是否最佳答案 1-是 0-否',
  `create_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  
  PRIMARY KEY (`id`),
  KEY `idx_question_id` (`question_id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='答案表';

-- ================================================
-- 10. 内容审核与举报表
-- ================================================

-- 举报记录表
DROP TABLE IF EXISTS `report`;
CREATE TABLE `report` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'id',
  `user_id` INT NOT NULL COMMENT '举报用户ID',
  `target_type` TINYINT NOT NULL COMMENT '举报对象类型 1-笔记 2-评论 3-用户 4-二手商品 5-问题/答案',
  `target_id` INT NOT NULL COMMENT '举报对象ID',
  `reason_type` TINYINT NOT NULL COMMENT '举报原因类型 1-违法违规 2-色情低俗 3-垃圾广告 4-侵权 5-其他',
  `reason` VARCHAR(500) COMMENT '举报原因详细描述',
  `status` TINYINT DEFAULT 0 COMMENT '状态 0-待处理 1-已处理 2-已驳回',
  `result` VARCHAR(500) COMMENT '处理结果',
  `handler_id` INT DEFAULT NULL COMMENT '处理人ID',
  `create_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '举报时间',
  `handle_time` DATETIME DEFAULT NULL COMMENT '处理时间',
  
  PRIMARY KEY (`id`),
  KEY `idx_target` (`target_type`, `target_id`),
  KEY `idx_status` (`status`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='举报记录表';

-- 内容审核记录表（AI审核）
DROP TABLE IF EXISTS `content_audit`;
CREATE TABLE `content_audit` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'id',
  `content_type` TINYINT NOT NULL COMMENT '内容类型 1-笔记 2-评论 3-二手商品 4-问题/答案',
  `content_id` INT NOT NULL COMMENT '内容ID',
  `audit_type` TINYINT NOT NULL COMMENT '审核类型 1-AI自动 2-人工审核',
  `audit_result` TINYINT COMMENT '审核结果 1-通过 2-疑似违规 3-违规',
  `risk_score` DECIMAL(5,2) COMMENT '风险评分（0-100）',
  `keywords` VARCHAR(500) COMMENT '命中的敏感关键词',
  `auditor_id` INT DEFAULT NULL COMMENT '审核人ID（人工审核时）',
  `remark` VARCHAR(500) COMMENT '审核备注',
  `create_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '审核时间',
  
  PRIMARY KEY (`id`),
  KEY `idx_content` (`content_type`, `content_id`),
  KEY `idx_audit_result` (`audit_result`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='内容审核记录表';

-- ================================================
-- 11. 系统配置与日志表
-- ================================================

-- 系统配置表
DROP TABLE IF EXISTS `system_config`;
CREATE TABLE `system_config` (
  `id` INT NOT NULL AUTO_INCREMENT COMMENT 'id',
  `config_key` VARCHAR(100) NOT NULL COMMENT '配置键',
  `config_value` TEXT COMMENT '配置值',
  `description` VARCHAR(500) COMMENT '配置说明',
  `update_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_config_key` (`config_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统配置表';


-- ================================================
-- 推荐结果缓存表
-- 用于存储预计算的推荐结果，支持协同过滤推荐算法
-- ================================================

-- 推荐结果缓存表
DROP TABLE IF EXISTS `recommendation_result`;
CREATE TABLE `recommendation_result` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'id',
  `user_id` INT NOT NULL COMMENT '用户ID',
  `note_ids` TEXT NOT NULL COMMENT '推荐的笔记ID列表（逗号分隔）',
  `algorithm_type` TINYINT DEFAULT 1 COMMENT '算法类型 1-协同过滤 2-基于内容',
  `score` DECIMAL(10,2) DEFAULT 0 COMMENT '推荐质量评分',
  `create_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '生成时间',
  `update_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_algorithm` (`user_id`, `algorithm_type`),
  KEY `idx_update_time` (`update_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='推荐结果缓存表';


-- ================================================
-- 12. 插入示例数据
-- ================================================

-- 管理员数据
INSERT INTO `sys_admin` VALUES 
(1,'admin','admin','管理员','https://api.dicebear.com/7.x/initials/svg?seed=Admin','admin@bluebook.com','18888888888');

-- 用户数据
INSERT INTO `sys_user` (`id`, `username`, `password`, `nickname`, `avatar_url`, `email`, `phone`, `student_id`, `school`, `college`, `major`, `grade`, `is_verified`, `bio`, `gender`, `status`, `account_type`) VALUES
(1, 'user001', '123456', '学长学姐说', 'https://api.dicebear.com/7.x/initials/svg?seed=学长&backgroundColor=667eea', 'user001@stu.edu.cn', '13800001111', '2021001001', '某某大学', '计算机学院', '软件工程', '2021级', 1, '热爱分享，帮助学弟学妹少走弯路！', 1, 1, 1),
(2, 'user002', '123456', '吃货小王', 'https://api.dicebear.com/7.x/initials/svg?seed=小王&backgroundColor=f093fb', 'user002@stu.edu.cn', '13800002222', '2022002002', '某某大学', '经济学院', '金融学', '2022级', 1, '校园美食探索者🍜', 2, 1, 1),
(3, 'user003', '123456', '上岸学长', 'https://api.dicebear.com/7.x/initials/svg?seed=上岸&backgroundColor=fa709a', 'user003@stu.edu.cn', '13800003333', '2020003003', '某某大学', '文学院', '汉语言文学', '2020级', 1, '考研成功上岸，愿与君共勉💪', 1, 1, 1),
(4, 'club_badminton', '123456', '羽毛球社官方', 'https://api.dicebear.com/7.x/initials/svg?seed=羽球社&backgroundColor=FF6B35', 'badminton@club.edu.cn', '13800004444', NULL, '某某大学', NULL, NULL, NULL, 1, '某某大学羽毛球社官方账号🏸 欢迎关注！', 0, 1, 2);

-- 社团数据
INSERT INTO `club` (`id`, `name`, `description`, `avatar_url`, `category`, `member_count`, `is_verified`, `admin_user_id`, `status`) VALUES
(1, '羽毛球社', '校园羽毛球爱好者的聚集地，定期举办训练和比赛活动', 'https://api.dicebear.com/7.x/initials/svg?seed=羽球社&backgroundColor=FF6B35', '体育运动', 350, 1, 4, 1),
(2, '摄影社', '记录校园美好瞬间，分享摄影技巧与作品', 'https://api.dicebear.com/7.x/initials/svg?seed=摄影社&backgroundColor=43e97b', '文化艺术', 280, 1, NULL, 1),
(3, '舞蹈社', '用舞蹈表达自我，展现青春活力', 'https://api.dicebear.com/7.x/initials/svg?seed=舞蹈社&backgroundColor=f093fb', '文化艺术', 220, 1, NULL, 1),
(4, '志愿者协会', '传递爱心，服务社会，奉献青春', 'https://api.dicebear.com/7.x/initials/svg?seed=志愿者&backgroundColor=4facfe', '公益服务', 450, 1, NULL, 1),
(5, '计算机协会', '技术交流，代码改变世界', 'https://api.dicebear.com/7.x/initials/svg?seed=计协&backgroundColor=00d2ff', '学术科技', 520, 1, NULL, 1);

-- 笔记数据
INSERT INTO `note` (`id`, `title`, `content`, `image_url`, `user_id`, `club_id`, `publish_type`, `author_name`, `author_avatar`, `likes`, `views`, `collects`, `comments`, `tags`, `category`, `is_official`, `status`) VALUES
(1, '新生必看！校园生活完全指南📚', '大家好！作为在这所大学生活了三年的老学长，今天给大家整理了一份超全的新生指南！从入学到毕业，你想知道的都在这里~\n\n【学习篇】\n1. 图书馆自习室预约攻略\n2. 选课技巧与课程评价\n3. 考试周复习方法\n\n【生活篇】\n1. 宿舍生活小贴士\n2. 食堂美食推荐\n3. 校园周边探店\n\n希望对大家有帮助！', 'https://picsum.photos/seed/campus-books/400/500', 1, NULL, 1, '学长学姐说', 'https://api.dicebear.com/7.x/initials/svg?seed=学长&backgroundColor=667eea', 1234, 12500, 856, 89, '新生指南,校园生活,学习经验', '学习经验', 0, 1),

(2, '🏸羽毛球社招新啦！零基础也能加入我们', '【社团简介】\n羽毛球社成立于2015年，是学校最具活力的体育类社团之一！\n\n【招新要求】\n✅ 热爱羽毛球运动\n✅ 零基础也欢迎\n✅ 能参加每周训练\n\n【社团福利】\n🎾 专业教练指导\n🏆 参加校际比赛机会\n🎉 丰富的团建活动\n🎁 社团定制队服\n\n【报名方式】\n扫描海报二维码或私信社团账号\n招新时间：9月1日-9月15日\n\n期待你的加入！', 'https://picsum.photos/seed/badminton-club/400/500', 4, 1, 2, '羽毛球社官方', 'https://api.dicebear.com/7.x/initials/svg?seed=羽球社&backgroundColor=FF6B35', 2341, 8900, 567, 134, '社团招新,羽毛球,体育运动', '社团活动', 1, 1),

(3, '📢【官方通知】本周六羽毛球社友谊赛，欢迎观战！', '各位同学大家好！\n\n本周六下午2点，体育馆二楼羽毛球馆，我们将举办社团内部友谊赛。\n\n【赛事信息】\n🕐 时间：本周六 14:00-17:00\n📍 地点：体育馆二楼羽毛球馆\n🎯 赛制：男单、女单、混双\n\n【精彩看点】\n⭐ 社团高手对决\n⭐ 现场抽奖活动\n⭐ 免费饮料供应\n\n欢迎所有同学前来观战加油！现场还有精彩抽奖活动哦~', 'https://picsum.photos/seed/badminton-match/400/500', 4, 1, 2, '羽毛球社官方', 'https://api.dicebear.com/7.x/initials/svg?seed=羽球社&backgroundColor=FF6B35', 1567, 5600, 234, 67, '友谊赛,活动通知,羽毛球', '校园活动', 1, 1),

(4, '🎉羽毛球社团建活动精彩回顾｜汗水与欢笑并存', '上周末的团建活动圆满结束！🎊\n\n这次我们去了郊外进行户外拓展，30多名社员参加。活动包括：\n\n【上午】\n🏸 羽毛球友谊赛\n🎯 团队拓展游戏\n\n【中午】\n🍱 农家乐聚餐\n\n【下午】\n🎮 桌游竞技\n📸 集体合影\n\n感谢所有参与的社员们！我们一起打球、聚餐、玩游戏，留下了太多美好回忆。期待下次活动不见不散！\n\n更多照片请看图片↓', 'https://picsum.photos/seed/team-building/400/500,https://picsum.photos/seed/team-photo/400/500', 4, 1, 2, '羽毛球社官方', 'https://api.dicebear.com/7.x/initials/svg?seed=羽球社&backgroundColor=FF6B35', 3210, 10200, 890, 156, '团建活动,社团生活,羽毛球', '社团活动', 1, 1),

(5, '🏆校园羽毛球赛冠军专访：训练心得大公开', '本期我们采访了校赛冠军李明同学，听他分享训练心得和比赛经验。\n\n【Q：你是什么时候开始打羽毛球的？】\nA：大一刚入学就加入了羽毛球社，到现在已经三年了。\n\n【Q：平时训练强度如何？】\nA：每周训练3-4次，每次2小时左右。重点练习步伐和杀球技术。\n\n【Q：给新手的建议？】\nA：\n1. 基本功最重要，不要急于求成\n2. 找个好教练或学长指导\n3. 多看专业比赛视频学习\n4. 坚持训练，享受过程\n\n想提高球技的同学千万不要错过！', 'https://picsum.photos/seed/badminton-champion/400/500', 4, 1, 2, '羽毛球社官方', 'https://api.dicebear.com/7.x/initials/svg?seed=羽球社&backgroundColor=FF6B35', 2890, 9500, 1200, 98, '比赛,经验分享,羽毛球,技巧', '学习经验', 1, 1),

(6, '食堂隐藏美食大盘点！第三食堂绝了🍜', '在学校吃了三年，终于把所有食堂都吃遍了！今天给大家分享各个食堂的隐藏美食~\n\n【第一食堂】\n⭐⭐⭐⭐\n推荐：红烧肉盖饭（8元）\n窗口：2楼3号窗口\n\n【第二食堂】\n⭐⭐⭐\n推荐：手抓饼+鸡蛋（5元）\n窗口：1楼早餐区\n\n【第三食堂】🔥\n⭐⭐⭐⭐⭐\n推荐：麻辣香锅（15元起）\n窗口：2楼角落\n评价：这个真的绝了！料足味美，性价比超高！\n\n【第四食堂】\n⭐⭐⭐⭐\n推荐：黄焖鸡（12元）\n\n大家还有什么好吃的推荐吗？评论区见！', 'https://picsum.photos/seed/chinese-food/400/500', 2, NULL, 1, '吃货小王', 'https://api.dicebear.com/7.x/initials/svg?seed=小王&backgroundColor=f093fb', 3456, 15600, 2300, 267, '美食,食堂,校园生活', '美食探店', 0, 1),

(7, '考研经验分享：从二本到985的逆袭之路💪', '作为一个成功上岸的过来人，想把我的经验分享给大家。\n\n【我的背景】\n本科：普通二本\n目标：985高校计算机专业\n结果：初试390+，成功上岸\n\n【时间规划】\n3-6月：基础阶段（数学、英语）\n7-9月：强化阶段（专业课）\n10-12月：冲刺阶段（真题+模拟）\n\n【各科经验】\n📐 数学：\n- 基础最重要，教材至少过2遍\n- 真题刷3遍以上\n- 错题本必须有\n\n📖 英语：\n- 单词每天背，从头背到尾\n- 阅读理解是重点\n- 作文模板要自己整理\n\n💻 专业课：\n- 目标院校真题很重要\n- 找学长学姐要资料\n- 理解大于背诵\n\n【心态调整】\n考研路上最难的是坚持。累了就休息，不要硬撑。相信自己，你一定可以！\n\n有问题欢迎评论区提问！', 'https://picsum.photos/seed/study-success/400/500', 3, NULL, 1, '上岸学长', 'https://api.dicebear.com/7.x/initials/svg?seed=上岸&backgroundColor=fa709a', 5678, 25000, 3400, 423, '考研,经验分享,学习方法', '学习经验', 0, 1);

-- 评论数据示例
INSERT INTO `comment` (`id`, `note_id`, `user_id`, `content`, `parent_id`, `likes`) VALUES
(1, 1, 2, '学长太棒了！正好需要这份指南！', NULL, 56),
(2, 1, 3, '已收藏，慢慢看', NULL, 34),
(3, 6, 1, '第三食堂的麻辣香锅确实好吃！', NULL, 89),
(4, 7, 2, '学长太强了！我也要加油！', NULL, 123);

-- 活动数据示例
INSERT INTO `activity` (`id`, `club_id`, `title`, `description`, `cover_url`, `location`, `start_time`, `end_time`, `max_participants`, `current_participants`, `need_approval`, `status`) VALUES
(1, 1, '羽毛球社新生见面会', '欢迎所有新社员参加！现场有学长学姐指导，还有精美礼品赠送。', 'https://picsum.photos/seed/meetup/600/400', '体育馆二楼', '2025-09-10 14:00:00', '2025-09-10 17:00:00', 50, 23, 0, 1),
(2, 4, '校园环保志愿活动', '让我们一起美化校园环境！', 'https://picsum.photos/seed/volunteer/600/400', '校园各处', '2025-09-15 09:00:00', '2025-09-15 12:00:00', 100, 67, 1, 1);

-- 点赞数据示例（推荐算法训练数据）
INSERT INTO `user_like` (`user_id`, `target_type`, `target_id`) VALUES
(1, 1, 6),  -- 用户1点赞笔记6（美食）
(1, 1, 7),  -- 用户1点赞笔记7（考研）
(2, 1, 1),  -- 用户2点赞笔记1（新生指南）
(2, 1, 6),  -- 用户2点赞笔记6（美食）
(2, 1, 2),  -- 用户2点赞笔记2（羽毛球社招新）
(3, 1, 7),  -- 用户3点赞笔记7（考研）
(3, 1, 1),  -- 用户3点赞笔记1（新生指南）
(1, 2, 1),  -- 用户1点赞评论1
(2, 2, 3);  -- 用户2点赞评论3

-- 收藏数据示例
INSERT INTO `user_collect` (`user_id`, `target_type`, `target_id`, `folder_name`) VALUES
(1, 1, 7, '学习资料'),   -- 用户1收藏考研笔记
(1, 1, 1, '实用指南'),   -- 用户1收藏新生指南
(2, 1, 6, '美食收藏'),   -- 用户2收藏美食笔记
(2, 1, 2, '社团相关'),   -- 用户2收藏羽毛球社招新
(3, 1, 1, '实用指南'),   -- 用户3收藏新生指南
(3, 1, 7, '考研专区');   -- 用户3收藏考研笔记

-- 分享数据示例
INSERT INTO `user_share` (`user_id`, `target_type`, `target_id`, `platform`) VALUES
(1, 1, 7, 'wechat'),     -- 用户1分享考研笔记到微信
(2, 1, 6, 'internal'),   -- 用户2站内分享美食笔记
(3, 2, 1, 'qq');         -- 用户3分享活动到QQ

-- 标签数据示例
INSERT INTO `tag` (`id`, `name`, `category`, `use_count`) VALUES
(1, '新生指南', '学习', 15),
(2, '校园生活', '生活', 28),
(3, '学习经验', '学习', 32),
(4, '社团招新', '社团', 18),
(5, '羽毛球', '运动', 25),
(6, '体育运动', '运动', 20),
(7, '社团活动', '社团', 22),
(8, '美食', '生活', 45),
(9, '食堂', '生活', 35),
(10, '考研', '学习', 50),
(11, '经验分享', '学习', 40),
(12, '学习方法', '学习', 38),
(13, '活动通知', '社团', 16),
(14, '比赛', '运动', 12),
(15, '技巧', '学习', 20),
(16, '团建活动', '社团', 10);

-- 笔记标签关联数据示例
INSERT INTO `note_tag` (`note_id`, `tag_id`) VALUES
(1, 1),  -- 笔记1: 新生指南
(1, 2),  -- 笔记1: 校园生活
(1, 3),  -- 笔记1: 学习经验
(2, 4),  -- 笔记2: 社团招新
(2, 5),  -- 笔记2: 羽毛球
(2, 6),  -- 笔记2: 体育运动
(3, 13), -- 笔记3: 活动通知
(3, 5),  -- 笔记3: 羽毛球
(3, 14), -- 笔记3: 比赛
(4, 7),  -- 笔记4: 社团活动
(4, 16), -- 笔记4: 团建活动
(4, 5),  -- 笔记4: 羽毛球
(5, 5),  -- 笔记5: 羽毛球
(5, 14), -- 笔记5: 比赛
(5, 11), -- 笔记5: 经验分享
(5, 15), -- 笔记5: 技巧
(6, 8),  -- 笔记6: 美食
(6, 9),  -- 笔记6: 食堂
(6, 2),  -- 笔记6: 校园生活
(7, 10), -- 笔记7: 考研
(7, 11), -- 笔记7: 经验分享
(7, 12); -- 笔记7: 学习方法

-- ================================================
-- 数据库创建完成
-- ================================================

-- ================================================
-- 方案1：统一账号体系 - 已实施修改说明
-- ================================================
-- 1. sys_user 表新增 account_type 字段
--    - 1: 普通用户
--    - 2: 社团账号
--    - 3: 机构账号
--    - 4: 企业账号
--
-- 2. 用户数据修改
--    - 用户ID 4（club_badminton）标记为社团账号（account_type = 2）
--
-- 3. 笔记数据修改
--    - 社团发布的笔记（笔记ID 2,3,4,5）的 user_id 设置为 4
--    - 确保所有笔记都有 user_id，可以使用统一的关注功能
--
-- 4. 社团数据修改
--    - 羽毛球社（club_id = 1）的 admin_user_id 设置为 4
--    - 建立社团与社团官方账号的关联
--
-- 优势：
--    ✅ 关注逻辑统一，所有笔记都可以关注
--    ✅ 代码简单，无需判断类型
--    ✅ 符合"关注账号"的语义
--    ✅ 扩展性好，可支持更多账号类型
-- ================================================

-- ================================================
-- V2.1 更新说明 - 完善推荐算法数据支持
-- ================================================
-- 更新时间：2025-11-27
-- 更新内容：新增5张表，完善协同过滤推荐算法所需的数据结构
--
-- 新增表：
-- 1. user_like - 用户点赞明细表
--    用途：记录用户点赞行为，支持协同过滤推荐算法
--    关键：可查询"用户点赞了哪些内容"，计算用户相似度
--    推荐算法应用：作为用户行为数据的重要组成部分（权重1.0）
--
-- 2. user_collect - 用户收藏表
--    用途：记录收藏行为（高权重），支持收藏夹管理
--    关键：收藏行为权重高于点赞，是推荐算法重要数据源
--    推荐算法应用：高权重行为（权重1.5），表示用户强烈兴趣
--
-- 3. user_share - 分享记录表
--    用途：记录分享行为，完善用户行为数据
--    关键：分享是最高权重行为，表明内容高度认可
--    推荐算法应用：最高权重行为（权重2.5），用于识别优质内容
--
-- 4. tag - 标签表
--    用途：标签独立管理，支持热门标签统计和标签推荐
--    关键：为基于内容的推荐提供数据支持
--    推荐算法应用：用于计算内容相似度，缓解冷启动问题
--
-- 5. note_tag - 笔记标签关联表
--    用途：多对多关系，一篇笔记可以有多个标签
--    关键：支持基于标签的内容推荐和相似内容查找
--    推荐算法应用：通过标签匹配实现基于内容的推荐
--
-- 推荐算法数据流：
-- ┌─────────────────────────────────────────────────────────────┐
-- │ 协同过滤推荐（基于用户行为）                                 │
-- ├─────────────────────────────────────────────────────────────┤
-- │ user_behavior（浏览记录）                                    │
-- │ + user_like（点赞明细）                                      │
-- │ + user_collect（收藏明细）                                   │
-- │ + user_share（分享记录）                                     │
-- │   → 计算用户相似度矩阵                                       │
-- │   → 生成协同过滤推荐结果                                     │
-- └─────────────────────────────────────────────────────────────┘
--
-- ┌─────────────────────────────────────────────────────────────┐
-- │ 基于内容的推荐（缓解冷启动）                                 │
-- ├─────────────────────────────────────────────────────────────┤
-- │ tag（标签库）                                               │
-- │ + note_tag（内容标签关联）                                   │
-- │ + user_interest（用户兴趣标签）                              │
-- │   → 计算内容相似度                                           │
-- │   → 为新用户推荐热门标签内容                                 │
-- └─────────────────────────────────────────────────────────────┘
--
-- 数据完整性说明：
-- - user_like 表通过唯一索引防止重复点赞
-- - user_collect 表支持多收藏夹管理
-- - tag 表的 use_count 字段需要通过触发器或应用层维护
-- - 所有新增表都添加了适当的索引以优化查询性能
--
-- 下一步建议：
-- 1. 在应用层实现点赞/取消点赞时同步更新 note.likes 统计
-- 2. 在应用层实现收藏/取消收藏时同步更新 note.collects 统计
-- 3. 定期执行推荐算法，将结果缓存到 Redis
-- 4. 考虑为 user_behavior 表添加数据清理策略（保留近3-6个月）
-- ================================================

