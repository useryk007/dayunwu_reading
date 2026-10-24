package org.example.Mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import java.util.Map;

/**
 * 点赞数据访问层，对应 likes 表。
 *
 * 一条记录代表 "某用户对某笔记点过赞"。
 * 表上有 (note_id, username) 联合唯一索引，防止重复点赞。
 */
@Mapper
public interface LikesMapper {

    /** 判断某用户是否点赞过某笔记（count > 0 即已赞） */
    int countByNoteAndUser(@Param("noteId") Long noteId, @Param("username") String username);

    /** 统计某笔记的总点赞数 */
    int countByNoteId(@Param("noteId") Long noteId);

    /** 点赞（insert 一条记录） */
    int insert(@Param("noteId") Long noteId, @Param("username") String username);

    /** 取消点赞 */
    int delete(@Param("noteId") Long noteId, @Param("username") String username);

    /** 级联删除：某笔记下的全部点赞 */
    int deleteByNoteId(@Param("noteId") Long noteId);

    /** 级联删除：某用户的全部点赞（注销账号时用） */
    int deleteByUsername(@Param("username") String username);

    /** 级联删除：批量删除一组笔记的全部点赞 */
    int deleteByNoteIds(@Param("noteIds") List<Long> noteIds);

    /** 排行榜：按点赞数降序取前 N 篇笔记 */
    List<Map<String, Object>> findTopByLikeCount(@Param("limit") int limit);

    /** 某作者的笔记被点赞总数（用户主页展示） */
    int countTotalByUsername(@Param("username") String username);

    /** 排行榜：按作者的全部公开笔记被点赞总数降序取前 N 位 */
    List<Map<String, Object>> findTopUsersByTotalLikes(@Param("limit") int limit);
}