package org.example.Pojo;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 点赞记录实体类，对应数据库的 likes 表。
 * 一条记录代表某个用户对某篇笔记点了一次赞。
 * (noteId, username) 联合唯一，防止重复点赞。
 */
@Data
public class Likes {
    private Long id;              // 点赞记录主键
    private Long noteId;          // 被点赞的笔记 id
    private String username;      // 点赞者用户名
    private LocalDateTime createTime; // 点赞时间
}