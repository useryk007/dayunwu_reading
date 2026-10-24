package org.example.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class NoteVO {
    private Long id;
    private String title;
    private String content;
    private String bookName;
    private String username;
    private LocalDateTime createTime;
    private Integer auditStatus;
    private Integer isPublic;
}