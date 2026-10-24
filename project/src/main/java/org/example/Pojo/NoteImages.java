package org.example.Pojo;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 笔记关联图片实体类，对应数据库的 note_images 表。
 *
 * 一篇笔记可配多张图（一对多），noteId 外键关联 notes.id。
 * 图片真实存磁盘，这里只存 imageUrl（"/uploads/xxx.png" 格式）。
 */
@Data
public class NoteImages {
    private Long id;
    private Long noteId;
    private String imageUrl;
    private LocalDateTime createTime;
}