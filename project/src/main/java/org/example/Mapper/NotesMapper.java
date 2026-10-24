package org.example.Mapper;

import org.example.Pojo.Notes;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface NotesMapper {

    int insert(Notes notes);

    Notes findById(Long id);

    List<Notes> findAllByUsername(@Param("username") String username);

    List<Notes> findRandomPublic(@Param("limit") int limit);

    List<Notes> findByUsernameAndPublic(@Param("username") String username, @Param("isPublic") Integer isPublic);

    List<Notes> findByUsernameAndAuditStatus(@Param("username") String username, @Param("auditStatus") Integer auditStatus);

    int updateById(Notes notes);

    int deleteById(Long id);

    int deleteByUsername(String username);

    List<Long> findIdsByUsername(String username);

    List<Notes> findPendingAudit();

    List<Notes> findAuditPassedPublic();

    List<Notes> findByAuthorAndPublic(@Param("username") String username);

    int updateAudit(@Param("id") Long id, @Param("auditStatus") Integer auditStatus, @Param("auditRemark") String auditRemark);

    List<Notes> findLikedNotesByUser(@Param("username") String username, @Param("offset") int offset, @Param("limit") int limit);

    int countLikedNotesByUser(@Param("username") String username);

    int countAuditPassedPublic();

    int countPendingAudit();

    int countPublicByUsername(@Param("username") String username);

    List<Map<String, Object>> batchCountLikes(@Param("noteIds") List<Long> noteIds);

    List<Map<String, Object>> batchCountComments(@Param("noteIds") List<Long> noteIds);

    List<Map<String, Object>> batchLikedByUser(@Param("username") String username, @Param("noteIds") List<Long> noteIds);

    List<Notes> findByIds(@Param("ids") List<Long> ids);
}