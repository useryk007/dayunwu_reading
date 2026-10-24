package org.example.Pojo;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 关注关系实体类，对应数据库的 follows 表。
 *
 * follower（关注者）关注了 followee（被关注者）。
 * (follower, followee) 联合唯一，防止重复关注。
 */
@Data
public class Follows {
    private Long id;
    private String follower;
    private String followee;
    private LocalDateTime createTime;
}