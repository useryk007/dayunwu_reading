-- ==================== reading 数据库建表脚本 ====================
-- 字符集统一 utf8mb4，支持 emoji

CREATE DATABASE IF NOT EXISTS reading DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE reading;

-- ==================== 用户表 ====================
DROP TABLE IF EXISTS users;
CREATE TABLE users (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    username        VARCHAR(50)  NOT NULL UNIQUE COMMENT '登录用户名',
    phone           VARCHAR(20)  NOT NULL UNIQUE COMMENT '手机号',
    password        VARCHAR(200) NOT NULL COMMENT 'BCrypt 加密后的密码',
    avatar          VARCHAR(500) DEFAULT NULL COMMENT '头像 URL',
    grade           VARCHAR(50)  DEFAULT NULL COMMENT '年级',
    major           VARCHAR(100) DEFAULT NULL COMMENT '专业',
    school          VARCHAR(100) DEFAULT NULL COMMENT '学校',
    region          VARCHAR(100) DEFAULT NULL COMMENT '地区',
    role            VARCHAR(20)  NOT NULL DEFAULT 'USER' COMMENT '角色：USER / ADMIN',
    status          TINYINT      NOT NULL DEFAULT 1 COMMENT '1=启用 0=禁用',
    register_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ==================== 笔记表 ====================
DROP TABLE IF EXISTS notes;
CREATE TABLE notes (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    username        VARCHAR(50)  NOT NULL COMMENT '作者用户名',
    title           VARCHAR(200) NOT NULL COMMENT '笔记标题',
    content         TEXT         COMMENT '笔记正文',
    book_name       VARCHAR(200) NOT NULL COMMENT '关联图书名称',
    is_public       TINYINT      NOT NULL DEFAULT 1 COMMENT '1=公开 0=私有',
    audit_status    TINYINT      NOT NULL DEFAULT 1 COMMENT '1=待审核 2=已通过 3=已驳回/下架',
    audit_remark    VARCHAR(500) DEFAULT NULL COMMENT '审核备注',
    create_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_username (username),
    INDEX idx_public_audit (is_public, audit_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ==================== 笔记配图表 ====================
DROP TABLE IF EXISTS note_images;
CREATE TABLE note_images (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    note_id         BIGINT       NOT NULL COMMENT '所属笔记 id',
    image_url       VARCHAR(500) NOT NULL COMMENT '图片 URL',
    create_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_note_id (note_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ==================== 点赞表 ====================
DROP TABLE IF EXISTS likes;
CREATE TABLE likes (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    note_id         BIGINT       NOT NULL COMMENT '被点赞的笔记 id',
    username        VARCHAR(50)  NOT NULL COMMENT '点赞者',
    create_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_note_user (note_id, username),
    INDEX idx_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ==================== 评论表 ====================
DROP TABLE IF EXISTS comments;
CREATE TABLE comments (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    note_id         BIGINT       NOT NULL COMMENT '所属笔记 id',
    parent_id       BIGINT       NOT NULL DEFAULT 0 COMMENT '父评论 id，0 表示顶层评论',
    username        VARCHAR(50)  NOT NULL COMMENT '评论者',
    content         VARCHAR(1000) NOT NULL COMMENT '评论内容',
    image           VARCHAR(500) DEFAULT NULL COMMENT '配图',
    create_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_combo_note_parent (note_id, parent_id),
    INDEX idx_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ==================== 关注表 ====================
DROP TABLE IF EXISTS follows;
CREATE TABLE follows (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    follower        VARCHAR(50) NOT NULL COMMENT '关注者',
    followee        VARCHAR(50) NOT NULL COMMENT '被关注者',
    create_time     DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_follow (follower, followee),
    INDEX idx_follower (follower),
    INDEX idx_followee (followee)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ==================== 初始化管理员账户 ====================
-- 先不插密码，等 Spring Boot 启动后注册 admin，再手动改 role