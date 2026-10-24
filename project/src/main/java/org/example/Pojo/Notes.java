package org.example.Pojo;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 笔记实体类，对应数据库的 notes 表。
 *
 * 关键字段语义：
 *   isPublic     —— 0=私有 1=公开
 *   auditStatus  —— 1=待审核 2=审核通过 3=已下架
 *   auditRemark  —— 审核备注，比如"被管理员下架"
 *
 * 公开笔记只有 auditStatus=2 才对外展示。
 */
@Data
public class Notes {
    private Long id;
    private String username;
    private String title;
    private String content;
    private String bookName;
    private Integer isPublic;
    private Integer auditStatus;
    private String auditRemark;
    private LocalDateTime createTime;
}