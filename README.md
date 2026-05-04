# 校园蓝珊笔记（BlueBook）

校园场景内容社区与社交互动平台，支持笔记发布、社团运营、活动报名、互动关系、私聊消息、推荐流与后台管理。  
本项目当前为 **Spring Boot + Vue3** 全栈实现，不再是早期静态页原型。

---

## 1. 项目概述

校园蓝珊笔记面向校园场景，围绕“内容发布 + 社交互动 + 社团运营”构建一体化系统，核心目标：

- 为学生提供高质量的校园信息流（学习经验、活动资讯、生活分享等）
- 为社团提供可持续运营能力（成员管理、活动发布与报名）
- 为平台提供基础治理与数据支撑（后台管理、推荐数据、统计）

---

## 2. 技术栈

### 后端

- Java 17
- Spring Boot 3.5.x
- MyBatis-Plus 3.5.x
- MySQL
- JWT（登录鉴权）
- Hutool / Lombok

### 前端

- Vue 3
- Vite
- Vue Router 4
- Element Plus
- Axios
- ECharts（统计可视化）
- Sass

---

## 3. 目录结构

```text
校园蓝珊笔记/
├─ src/main/java/com/example/springboot/   # 后端源码（controller/service/mapper/entity）
├─ src/main/resources/                     # 后端配置（application*.yaml、mapper xml）
├─ vue/                                    # Vue3 前端工程
│  ├─ src/api/                             # 前端接口封装
│  ├─ src/views/                           # 前后台页面
│  ├─ src/router/                          # 路由与守卫
│  └─ config/                              # 前端配置
├─ sql/bluebook_complete.sql               # 数据库初始化脚本
├─ 毕设文档/                                # 论文/资料相关文档
└─ README.md
```

---

## 4. 功能现状（按当前代码）

> 说明：以下为代码层面的实现状态，不等同于产品终态。

### 4.1 已实现（核心链路）

- 用户登录注册、基础鉴权、角色分流（前台/后台）
- 笔记流：发布、详情、分类筛选、推荐/最新/关注流
- 互动能力：点赞、收藏、评论、关注
- 社团模块：社团信息、成员管理、社团主页与管理页
- 活动模块：活动发布、报名、审核、签到等接口链路
- 私聊模块：会话列表、发送消息、未读统计
- 后台模块：用户/管理员/笔记/社团/评论/统计页面骨架
- 文件上传下载接口

### 4.2 部分实现（可用但仍在完善）

- 推荐系统：具备推荐接口、热门兜底、行为与标签相关数据结构
- AI 辅助：已接入文案优化接口；笔记发布前增加 AI 二分类违规检测（PASS/BLOCK，fail-close）
- 统计模块：概览可用，趋势与分类统计仍有示例数据与待细化逻辑

### 4.3 规划/待扩展

- 更完整的推荐策略与离线计算任务
- 内容审核与治理流程深化
- 二手、问答等扩展业务的前后端完整闭环

---

## 5. 关键接口分组（后端）

主要控制器位于 `src/main/java/com/example/springboot/controller`：

- `/api/note`：笔记相关（发布前含 AI 违规检测拦截）
- `/api/comment`：评论相关
- `/api/recommendation`：推荐相关
- `/api/club`、`/api/clubMember`：社团与成员
- `/api/activity`、`/api/activityRegistration`：活动与报名
- `/api/privateMessage`：私聊消息
- `/api/ai`：AI文案优化
- `/api/file`：文件上传下载
- `/api/userLike`、`/api/userCollect`、`/api/userFollow`、`/api/userShare`：互动行为
- `/statistics`：统计数据

---

## 6. 数据库说明

- 初始化脚本：`sql/bluebook_complete.sql`
- 核心表（示例）：
  - 用户与权限：`sys_user`、`sys_admin`
  - 内容与互动：`note`、`comment`、`user_like`、`user_collect`、`user_follow`
  - 社团与活动：`club`、`club_member`、`activity`、`activity_registration`
  - 推荐相关：`user_behavior`、`user_interest`、`tag`、`note_tag`、`recommendation_result`
  - 消息与通知：`private_message`、`notification`

---

## 7. 本地运行

### 7.1 环境要求

- JDK 17
- Maven 3.8+
- Node.js 18+（建议）
- MySQL 8.x

### 7.2 初始化数据库

1. 新建数据库（如 `bluebook`）
2. 执行脚本：

```sql
source sql/bluebook_complete.sql;
```

### 7.3 启动后端

在项目根目录执行：

```bash
mvn spring-boot:run
```

默认端口：`9090`

### 7.4 启动前端

```bash
cd vue
npm install
npm run dev
```

默认访问：`http://localhost:5173`

---

## 8. 默认访问入口

- 前台首页：`/front/home`
- 后台首页：`/back/home`
- 登录页：`/login`

> 根路径会根据登录状态与角色进行路由分流。

---

## 9. 毕设写作建议对齐

如果该仓库用于毕业设计，建议论文与项目说明保持一致：

- 论文“系统分析与设计”对应：模块划分、角色权限、ER 关系、接口边界
- 论文“系统实现”对应：核心功能链路（内容、社团、活动、推荐、私聊）
- 论文“系统测试”对应：功能测试、接口测试、关键页面验证
- 对“未完成项”明确写成后续优化，避免与代码现状冲突

---

## 10. 安全与配置说明（重要）

请勿在公开仓库提交真实生产凭据（数据库密码、API Key、服务器地址等）。  
建议：

- 使用环境变量或本地配置文件注入敏感信息
- 将敏感配置与示例配置分离（例如 `application.example.yaml`）
- 生产环境最小权限原则

### AI 审核策略（当前实现）

- 触发时机：仅在“发布笔记”时触发一次同步审核
- 审核结果：二分类 `PASS` / `BLOCK`
- 失败策略：fail-close（AI 超时、异常、返回格式异常时默认拦截发布）
- 用户提示：统一返回“内容可能涉及违规，请修改后重试”
- 当前限制：未落审核记录库，不支持后台人工复核

---

## 11. 图片持久化部署（Nginx）

- Nginx 示例配置：`ops/nginx/bluebook.conf.example`
- 运维检查与备份建议：`ops/maintenance/image-storage-ops.md`
- 后端访问地址配置：
  - `file.upload.path`：上传目录（必须绝对路径）
  - `file.access.domain`：图片访问域名（建议 Nginx 域名）
  - `file.access.prefix`：静态前缀（默认 `/uploads/`）

---

## 12. 版本说明

- 当前版本定位：毕业设计开发版（持续迭代）
- 文档更新时间：2026-05-04
