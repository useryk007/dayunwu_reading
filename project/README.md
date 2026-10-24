# Reading — 阅读笔记社区平台

一个基于 Spring Boot 构建的阅读笔记分享社区，用户可以发布读书笔记、关注好友、点赞评论，管理员可在后台进行内容审核和用户管理。

## 目录

- [技术栈](#技术栈)
- [项目结构](#项目结构)
- [功能特性](#功能特性)
- [环境要求](#环境要求)
- [快速开始](#快速开始)
- [数据库设计](#数据库设计)
- [API 接口概览](#api-接口概览)
- [前端页面](#前端页面)
- [安全设计](#安全设计)

---

## 技术栈

| 类别 | 技术 |
|------|------|
| 语言 | Java 25 |
| 框架 | Spring Boot 4.0.8 |
| ORM | MyBatis 4.0.1 |
| 数据库 | MySQL 8.x |
| 密码加密 | Spring Security Crypto (BCrypt) |
| 缓存 | Caffeine (Spring Cache) |
| 校验 | Jakarta Validation |
| 监控 | Spring Boot Actuator |
| 简化 | Lombok |
| 构建 | Maven |
| 前端 | 原生 HTML / CSS / JavaScript |

---

## 项目结构

```
reading1.1/
├── pom.xml                          # Maven 配置
├── schema.sql                       # 数据库建表脚本
├── boot.log                         # 运行日志
├── uploads/                         # 上传文件存储目录
├── logs/
│   └── reading.log
└── src/main/
    ├── java/org/example/
    │   ├── ReadingApplication.java      # Spring Boot 启动类
    │   ├── Config/                      # Web 配置
    │   │   ├── WebConfig.java           # 静态资源 + 拦截器注册
    │   │   ├── LoginInterceptor.java    # 登录拦截器
    │   │   └── AdminInterceptor.java    # 管理员权限拦截器
    │   ├── Controller/                  # API 控制层
    │   │   ├── UsersController.java     # 用户相关接口
    │   │   ├── NotesController.java     # 笔记相关接口
    │   │   ├── FollowsController.java   # 关注相关接口
    │   │   └── AdminController.java     # 管理员接口
    │   ├── service/                     # 业务逻辑层
    │   │   ├── UsersService.java
    │   │   ├── NotesService.java
    │   │   ├── FollowsService.java
    │   │   ├── CommentsService.java
    │   │   └── impl/                    # 实现类
    │   ├── Mapper/                      # MyBatis Mapper 接口
    │   ├── Pojo/                        # 数据库实体 (Users, Notes, Comments, Likes, Follows, NoteImages)
    │   ├── dto/                         # 请求/响应数据传输对象
    │   │   ├── LoginDTO.java
    │   │   ├── RegisterDTO.java
    │   │   ├── UserVO.java
    │   │   ├── NoteVO.java
    │   │   └── NoteEnrichedVO.java
    │   └── common/                      # 通用组件
    │       ├── Result.java              # 统一响应封装
    │       ├── PageResult.java          # 分页结果封装
    │       ├── BusinessException.java   # 业务异常
    │       ├── GlobalExceptionHandler.java
    │       ├── SessionUtil.java         # Session 工具
    │       ├── UploadUtils.java         # 文件上传工具
    │       ├── BCryptConfig.java        # BCrypt 加密配置
    │       └── LoginFailCounter.java    # 登录失败计数器（防暴力破解）
    └── resources/
        ├── application.yml              # 主配置
        ├── application-dev.yml          # 开发环境配置
        ├── application-prod.yml         # 生产环境配置
        ├── logback-spring.xml           # 日志配置
        ├── mapper/                      # MyBatis XML 映射
        └── static/                      # 前端静态资源
            ├── *.html
            ├── css/
            └── js/
```

---

## 功能特性

### 用户模块
- 注册 / 登录（支持用户名或手机号登录）
- BCrypt 密码加密存储
- 登录失败锁定：连续 5 次失败后锁定 30 分钟
- Session 会话管理，HttpOnly + SameSite=Strict
- 个人信息修改、头像上传、账号注销

### 笔记模块
- 发布 / 编辑 / 删除笔记，支持标题、正文、关联书名、多图上传
- 公开 / 私有笔记切换
- 点赞 / 取消点赞（带点赞状态回显）
- 评论 / 回复（支持多级评论与配图）
- 笔记详情页，查看评论、点赞数、作者信息
- 我的笔记列表（支持按公开/私有/待审核筛选）
- 社区推荐（随机展示公开笔记）

### 社区互动
- 关注 / 取消关注用户
- 关注列表 / 粉丝列表查看
- 三大排行榜：点赞最多的笔记、粉丝最多的作者、获赞最多的用户
- 作者主页：公开笔记列表、粉丝数、关注数

### 管理员后台
- 用户管理：查看、禁用/启用、解锁、删除
- 笔记审核：待审核列表、通过 / 驳回 / 下架
- 评论管理：删除违规评论
- 数据统计：用户总数、待审核数、已发布笔记数、评论总数

### 文件上传
- 统一上传工具类，自动生成唯一文件名避免冲突
- 上传目录通过 WebConfig 映射为 `/uploads/**` 可直接访问

---

## 环境要求

- **JDK 25**（最低 Java 21 亦可，但需修改 pom.xml 中的 java.version）
- **Maven 3.9+**
- **MySQL 8.0+**

---

## 快速开始

### 1. 克隆项目

```bash
git clone <repo-url>
cd reading1.1
```

### 2. 初始化数据库

```bash
mysql -u root -p < schema.sql
```

脚本会自动创建 reading 数据库和所有表，字符集为 utf8mb4。

### 3. 配置数据库连接

编辑 src/main/resources/application.yml，或通过环境变量覆盖：

| 环境变量 | 默认值 | 说明 |
|---------|-------|------|
| DB_USERNAME | root | 数据库用户名 |
| DB_PASSWORD | 123456 | 数据库密码 |
| SERVER_PORT | 8080 | 服务端口 |

### 4. 开发模式（可选）

切换到开发 profile 可以看到 MyBatis SQL 日志：

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

### 5. 打包与运行

```bash
mvn clean package
java -jar target/reading-0.0.1-SNAPSHOT.jar
```

启动后访问：

| 页面 | URL |
|------|-----|
| 登录页 | http://localhost:8080/login.html |
| 注册页 | http://localhost:8080/register.html |
| 首页（需登录） | http://localhost:8080/home.html |
| 管理员后台 | http://localhost:8080/admin.html |

### 6. 创建管理员

先通过注册页注册一个普通账号，然后在数据库中将其角色改为 ADMIN：

```sql
UPDATE users SET role = 'ADMIN' WHERE username = 'your-admin-name';
```

---

## 数据库设计

共 6 张表，字符集统一为 utf8mb4_unicode_ci：

| 表名 | 说明 | 关键字段 |
|------|------|---------|
| users | 用户表 | id, username, phone, password(BCrypt), role, status |
| notes | 笔记表 | id, username, title, content, book_name, is_public, audit_status |
| note_images | 笔记配图 | id, note_id, image_url |
| likes | 点赞表 | id, note_id, username（联合唯一防重复点赞） |
| comments | 评论表 | id, note_id, parent_id, username, content |
| follows | 关注表 | id, follower, followee（联合唯一防重复关注） |

笔记审核状态 (audit_status)：1=待审核，2=已通过，3=已驳回/下架。

---

## API 接口概览

所有接口返回统一格式：

```json
{
  "code": 1,
  "message": "success",
  "data": ...
}
```

code=1 表示成功，code=0 或 401 表示失败。

### 用户相关 /api

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| POST | /api/register | 用户注册 | 否 |
| POST | /api/login | 用户登录 | 否 |
| GET  | /api/user/info | 获取当前用户信息 | 是 |
| POST | /api/user/update | 修改个人资料 | 是 |
| POST | /api/user/logout | 退出登录 | 是 |
| POST | /api/user/cancel | 注销账号 | 是 |
| GET  | /api/user/public/{username} | 查看用户公开信息 | 是 |

### 笔记相关 /api/note

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| POST | /api/note/publish | 发布笔记 | 是 |
| POST | /api/note/update/{id} | 编辑笔记 | 是 |
| POST | /api/note/delete/{id} | 删除笔记 | 是 |
| POST | /api/note/toggle/{id} | 公开/私有切换 | 是 |
| GET  | /api/note/list | 我的笔记列表 | 是 |
| GET  | /api/note/community | 社区推荐笔记 | 是 |
| GET  | /api/note/ranking | 排行榜数据 | 是 |
| GET  | /api/note/{id} | 笔记详情 | 是 |
| POST | /api/note/{id}/like | 点赞/取消点赞 | 是 |
| POST | /api/note/comment | 发表评论 | 是 |
| GET  | /api/note/comment/{noteId} | 评论列表 | 是 |
| POST | /api/note/comment/delete/{id} | 删除评论 | 是 |
| GET  | /api/note/liked | 我点赞过的笔记 | 是 |
| GET  | /api/note/user/{username} | 作者主页数据 | 是 |
| GET  | /api/note/author/{username} | 作者公开笔记列表 | 是 |

### 关注相关 /api/follow

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| POST | /api/follow/{followee} | 关注用户 | 是 |
| POST | /api/follow/{followee}/cancel | 取消关注 | 是 |
| GET  | /api/follow/check/{followee} | 检查是否已关注 | 是 |
| GET  | /api/followers/{username} | 粉丝列表 | 是 |
| GET  | /api/followees/{username} | 关注列表 | 是 |
| GET  | /api/follow/top | 粉丝最多的作者 | 是 |
| GET  | /api/follow/followees/detail | 我关注的用户详情 | 是 |

### 管理员接口 /admin

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| GET  | /admin/users | 用户列表 | ADMIN |
| POST | /admin/user/{id}/status | 禁用/启用用户 | ADMIN |
| POST | /admin/user/{id}/unlock | 解除登录锁定 | ADMIN |
| POST | /admin/user/{id}/delete | 删除用户 | ADMIN |
| GET  | /admin/notes/pending | 待审核笔记 | ADMIN |
| POST | /admin/note/{id}/audit | 审核通过/驳回 | ADMIN |
| GET  | /admin/notes/published | 已发布笔记 | ADMIN |
| GET  | /admin/note/{id}/detail | 笔记详情 | ADMIN |
| POST | /admin/note/{id}/offline | 下架笔记 | ADMIN |
| POST | /admin/comment/{id}/delete | 删除评论 | ADMIN |
| GET  | /admin/stats | 数据统计概览 | ADMIN |

### Actuator 端点

```
GET http://localhost:8080/actuator/health
GET http://localhost:8080/actuator/info
GET http://localhost:8080/actuator/metrics
```

---

## 前端页面

所有页面位于 src/main/resources/static/：

| 文件 | 说明 |
|------|------|
| login.html | 登录页 |
| register.html | 注册页 |
| home.html | 首页（社区笔记流） |
| note-upload.html | 发布笔记页 |
| edit.html | 编辑笔记页 |
| user-profile.html | 个人主页 / 他人主页 |
| admin.html | 管理员后台 |

---

## 安全设计

| 环节 | 措施 |
|------|------|
| 密码存储 | BCrypt 单向加盐哈希，BCryptPasswordEncoder |
| 暴力破解防护 | LoginFailCounter 基于内存计数，5 次失败锁定 30 分钟 |
| 会话管理 | HttpSession，Cookie 开启 HttpOnly + SameSite=Lax，30 分钟超时 |
| 权限控制 | LoginInterceptor 拦截 /home.html 和 /api/**；AdminInterceptor 拦截 /admin/** |
| 文件上传 | 统一入口 UploadUtils，生成 UUID 文件名避免路径穿越 |
| SQL 安全 | MyBatis #{} 参数绑定，自动预编译 |
| 输入校验 | Jakarta Validation (@NotBlank, @Size 等) |
| CORS | 同源策略，前后端同域部署 |