package org.example.Mapper;

import org.example.Pojo.Comments;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * 评论数据访问层，对应 comments 表。
 *
 * 支持顶层评论（parent_id=0）和回复（parent_id=父评论 id）两种形态，
 * Service 层负责把扁平查询结果组装成树形。
 */
@Mapper
public interface CommentsMapper {

    /** 新增评论（包括顶层和回复） */
    int insert(Comments comments);

    /** 按主键查询单条评论（回复时校验父评论归属用） */
    Comments findById(Long id);

    /** 查某笔记下的所有评论（按时间升序，Service 再组装树） */
    List<Comments> findByNoteId(Long noteId);

    /** 统计某笔记的评论数 */
    int countByNoteId(Long noteId);

    /** 级联删除：删除某笔记下的全部评论 */
    int deleteByNoteId(Long noteId);

    /** 级联删除：删除某用户的全部评论（注销账号时用） */
    int deleteByUsername(String username);

    /** 级联删除：批量删除一组笔记下的全部评论 */
    int deleteByNoteIds(@Param("noteIds") List<Long> noteIds);

    /** 按主键删除单条评论（作者删除自己的评论 或 管理员强制删除） */
    int deleteById(Long id);

    /** 删除某条评论下的全部子回复 */
    int deleteByParentId(Long parentId);

    /** 查某条评论的所有子回复（递归清理图片用） */
    List<Comments> findByParentId(Long parentId);

    /** 查某用户写的全部评论（删除账户前清理图片用） */
    List<Comments> findByUsername(String username);

    /** 全表统计（数据看板用） */
    int countAll();
}