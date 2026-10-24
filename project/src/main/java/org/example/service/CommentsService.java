package org.example.service;

import org.example.Pojo.Comments;

import java.util.List;

/**
 * 评论业务层接口。
 *
 * 返回的 listByNoteId 结果已经是 Service 层组装好的树形结构
 * （顶层评论 + 子回复嵌套在 children 字段里），
 * Controller 可以直接返回给前端。
 */
public interface CommentsService {

    /** 新增评论。parentId=0 表示顶层评论，>0 表示回复某条已有评论 */
    void add(Comments comments);

    /** 查某笔记下的评论（已组装成树形结构） */
    List<Comments> listByNoteId(Long noteId);

    /** 统计某笔记的评论数（含回复） */
    int countByNoteId(Long noteId);

    /** 级联删除某用户的全部评论（注销账号时用） */
    void deleteByUser(String username);

    /** 作者删除自己的评论 */
    void deleteById(Long id, String username);

    /** 管理员强制删除任意评论 */
    void adminDeleteById(Long id);

    /** 统计评论总数（数据看板用） */
    int countAll();

    /** 按 id 查单条评论 */
    Comments findById(Long id);
}