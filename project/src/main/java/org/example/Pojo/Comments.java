package org.example.Pojo;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 评论实体类，对应数据库的 comments 表。
 *
 * 树形结构设计：
 *   parentId = 0  → 顶层评论
 *   parentId = 某评论 id → 该评论的子评论（回复）
 *
 * children 字段不映射数据库（数据库只有一张扁平表），
 * 而是在 CommentsServiceImpl 中通过两遍遍历动态构建出来的嵌套结构。
 * 前端拿到的是已经组装好的评论树，可以直接递归渲染。
 */
@Data
public class Comments {
    private Long id;                    // 评论主键
    private Long noteId;                // 所属笔记 id（外键 → notes.id）
    private Long parentId;              // 父评论 id，0 表示顶层评论
    private String username;            // 评论者用户名（冗余存储）
    private String content;             // 评论正文
    private String image;               // 评论附带的图片（可为空）
    private LocalDateTime createTime;   // 评论时间
    private List<Comments> children = new ArrayList<>(); // 子评论列表，运行时组装
}