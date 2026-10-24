package org.example.service;

import org.example.Pojo.Notes;
import org.example.common.PageResult;
import org.example.dto.NoteEnrichedVO;

import java.util.List;
import java.util.Map;

public interface NotesService {

    void publish(Notes notes);

    List<Notes> listMyNotes(String username);

    List<Notes> listMyNotesByScope(String username, Integer isPublic);

    List<Notes> listMyPending(String username);

    List<Notes> listRandomPublic(int limit);

    Notes getById(Long id);

    void update(Notes notes);

    void delete(Long id, String username);

    void deleteByUser(String username);

    List<Notes> listPendingAudit();

    List<Notes> listAuditPassedPublic();

    List<Notes> listByAuthorAndPublic(String username);

    void audit(Long id, Integer auditStatus, String auditRemark);

    void offline(Long id);

    void reject(Long id);

    PageResult<Notes> listLikedByUser(String username, int pageNum, int pageSize);

    List<NoteEnrichedVO> enrichNotes(List<Notes> notes, String currentUsername);

    boolean toggleLike(Long noteId, String username);

    Map<String, Object> getNoteDetail(Long id, String username);

    int countPublicByUsername(String username);

    int countPendingAudit();

    int countAuditPassedPublic();

    List<Map<String, Object>> findTopNoteLikes(int limit);

    List<Notes> findByIds(List<Long> ids);

    void insertNoteImage(Long noteId, String url);

    void deleteImagesByNoteId(Long noteId);

    Map<String, Object> adminNoteDetail(Long id);
}