package org.example.service.impl;

import org.example.Mapper.CommentsMapper;
import org.example.Mapper.FollowsMapper;
import org.example.Mapper.LikesMapper;
import org.example.Mapper.NoteImagesMapper;
import org.example.Mapper.NotesMapper;
import org.example.Pojo.Comments;
import org.example.Pojo.NoteImages;
import org.example.Pojo.Notes;
import org.example.common.BusinessException;
import org.example.common.PageResult;
import org.example.common.UploadUtils;
import org.example.dto.NoteEnrichedVO;
import org.example.service.NotesService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class NotesServiceImpl implements NotesService {

    private static final Logger log = LoggerFactory.getLogger(NotesServiceImpl.class);

    private final NotesMapper notesMapper;
    private final CommentsMapper commentsMapper;
    private final LikesMapper likesMapper;
    private final NoteImagesMapper noteImagesMapper;
    private final FollowsMapper followsMapper;

    public NotesServiceImpl(NotesMapper notesMapper,
                            CommentsMapper commentsMapper,
                            LikesMapper likesMapper,
                            NoteImagesMapper noteImagesMapper,
                            FollowsMapper followsMapper) {
        this.notesMapper = notesMapper;
        this.commentsMapper = commentsMapper;
        this.likesMapper = likesMapper;
        this.noteImagesMapper = noteImagesMapper;
        this.followsMapper = followsMapper;
    }

    @Override
    public void publish(Notes notes) {
        if (notes.getTitle() == null || notes.getTitle().isBlank()) {
            throw new BusinessException("笔记标题不能为空");
        }
        if (notes.getBookName() != null && notes.getBookName().isBlank()) {
            notes.setBookName(null);
        }
        if (notes.getIsPublic() == null) notes.setIsPublic(1);
        if (notes.getIsPublic() == 1) {
            notes.setAuditStatus(1);
        } else {
            notes.setAuditStatus(2);
        }
        notesMapper.insert(notes);
        log.info("笔记发布: id={}, username={}, isPublic={}, auditStatus={}",
                notes.getId(), notes.getUsername(), notes.getIsPublic(), notes.getAuditStatus());
    }

    @Override
    public List<Notes> listMyNotes(String username) {
        return notesMapper.findAllByUsername(username);
    }

    @Override
    public List<Notes> listMyNotesByScope(String username, Integer isPublic) {
        return notesMapper.findByUsernameAndPublic(username, isPublic);
    }

    @Override
    public List<Notes> listMyPending(String username) {
        return notesMapper.findByUsernameAndAuditStatus(username, 1);
    }

    @Override
    public List<Notes> listRandomPublic(int limit) {
        return notesMapper.findRandomPublic(limit);
    }

    @Override
    public Notes getById(Long id) {
        return notesMapper.findById(id);
    }

    @Override
    public void update(Notes notes) {
        Notes existing = notesMapper.findById(notes.getId());
        if (existing == null) throw new BusinessException("笔记不存在");
        if (notes.getBookName() != null && !notes.getBookName().isBlank()) {
            notes.setAuditStatus(notes.getIsPublic() != null && notes.getIsPublic() == 1 ? 1 : 2);
        } else if (notes.getIsPublic() != null && notes.getIsPublic() == 1 && existing.getIsPublic() != 1) {
            notes.setAuditStatus(1);
        }
        notesMapper.updateById(notes);
    }

    @Override
    @Transactional
    public void delete(Long id, String username) {
        Notes n = notesMapper.findById(id);
        if (n == null) throw new BusinessException("笔记不存在");
        if (!n.getUsername().equals(username)) throw new BusinessException("无权删除此笔记");
        cleanupNote(id);
        notesMapper.deleteById(id);
    }

    @Override
    @Transactional
    public void deleteByUser(String username) {
        List<Long> noteIds = notesMapper.findIdsByUsername(username);
        for (Long nid : noteIds) {
            cleanupNote(nid);
        }
        likesMapper.deleteByUsername(username);
        commentsMapper.deleteByUsername(username);
        notesMapper.deleteByUsername(username);
    }

    @Override
    public List<Notes> listPendingAudit() {
        return notesMapper.findPendingAudit();
    }

    @Override
    public List<Notes> listAuditPassedPublic() {
        return notesMapper.findAuditPassedPublic();
    }

    @Override
    public List<Notes> listByAuthorAndPublic(String username) {
        return notesMapper.findByAuthorAndPublic(username);
    }

    @Override
    public void audit(Long id, Integer auditStatus, String auditRemark) {
        Notes n = notesMapper.findById(id);
        if (n == null) throw new BusinessException("笔记不存在");
        notesMapper.updateAudit(id, auditStatus, auditRemark);
        log.info("笔记审核: id={}, auditStatus={}, remark={}", id, auditStatus, auditRemark);
    }

    @Override
    @Transactional
    public void offline(Long id) {
        Notes n = notesMapper.findById(id);
        if (n == null) throw new BusinessException("笔记不存在");
        cleanupNote(id);
        notesMapper.deleteById(id);
        log.info("笔记被删除: id={}, username={}, title={}", id, n.getUsername(), n.getTitle());
    }

    @Override
    public void reject(Long id) {
        offline(id);
    }

    @Override
    public PageResult<Notes> listLikedByUser(String username, int pageNum, int pageSize) {
        if (pageNum < 1) pageNum = 1;
        if (pageSize < 1) pageSize = 10;
        int offset = (pageNum - 1) * pageSize;
        int total = notesMapper.countLikedNotesByUser(username);
        List<Notes> list = notesMapper.findLikedNotesByUser(username, offset, pageSize);
        return new PageResult<>(total, pageNum, pageSize, list);
    }

    @Override
    public List<NoteEnrichedVO> enrichNotes(List<Notes> notes, String currentUsername) {
        if (notes == null || notes.isEmpty()) return new ArrayList<>();

        List<Long> noteIds = notes.stream().map(Notes::getId).toList();
        Set<String> authors = notes.stream().map(Notes::getUsername).collect(Collectors.toCollection(HashSet::new));

        Map<Long, Integer> likeCountMap = new HashMap<>();
        for (Map<String, Object> row : notesMapper.batchCountLikes(noteIds)) {
            likeCountMap.put(((Number) row.get("noteId")).longValue(), ((Number) row.get("likeCount")).intValue());
        }

        Map<Long, Integer> commentCountMap = new HashMap<>();
        for (Map<String, Object> row : notesMapper.batchCountComments(noteIds)) {
            commentCountMap.put(((Number) row.get("noteId")).longValue(), ((Number) row.get("commentCount")).intValue());
        }

        Set<Long> likedSet = new HashSet<>();
        if (currentUsername != null) {
            for (Map<String, Object> row : notesMapper.batchLikedByUser(currentUsername, noteIds)) {
                likedSet.add(((Number) row.get("noteId")).longValue());
            }
        }

        Set<String> followingSet = new HashSet<>();
        if (currentUsername != null && !authors.isEmpty()) {
            followingSet.addAll(followsMapper.batchFindFollowed(currentUsername, new ArrayList<>(authors)));
        }

        List<NoteEnrichedVO> result = new ArrayList<>(notes.size());
        for (Notes n : notes) {
            NoteEnrichedVO vo = new NoteEnrichedVO();
            vo.setId(n.getId());
            vo.setTitle(n.getTitle());
            vo.setContent(n.getContent());
            vo.setBookName(n.getBookName());
            vo.setUsername(n.getUsername());
            vo.setCreateTime(n.getCreateTime());
            vo.setAuditStatus(n.getAuditStatus());
            vo.setIsPublic(n.getIsPublic());
            vo.setLikeCount(likeCountMap.getOrDefault(n.getId(), 0));
            vo.setLiked(likedSet.contains(n.getId()));
            vo.setCommentCount(commentCountMap.getOrDefault(n.getId(), 0));
            vo.setFollowing(followingSet.contains(n.getUsername()));
            vo.setIsOwner(currentUsername != null && currentUsername.equals(n.getUsername()));
            result.add(vo);
        }
        return result;
    }

    private void cleanupNote(Long noteId) {
        List<NoteImages> images = noteImagesMapper.findByNoteId(noteId);
        for (NoteImages img : images) {
            if (img.getImageUrl() != null) {
                UploadUtils.deleteByUrl(img.getImageUrl());
            }
        }

        List<Comments> noteComments = commentsMapper.findByNoteId(noteId);
        for (Comments c : noteComments) {
            if (c.getImage() != null) UploadUtils.deleteByUrl(c.getImage());
        }

        commentsMapper.deleteByNoteId(noteId);
        likesMapper.deleteByNoteId(noteId);
        noteImagesMapper.deleteByNoteId(noteId);
    }

    @Override
    public boolean toggleLike(Long noteId, String username) {
        Notes n = notesMapper.findById(noteId);
        if (n == null || n.getIsPublic() != 1
                || n.getAuditStatus() == null || n.getAuditStatus() != 2) {
            throw new BusinessException("笔记不存在或不可见");
        }
        if (likesMapper.countByNoteAndUser(noteId, username) > 0) {
            likesMapper.delete(noteId, username);
            return false;
        }
        try {
            likesMapper.insert(noteId, username);
            return true;
        } catch (DuplicateKeyException e) {
            return true;
        }
    }

    @Override
    public Map<String, Object> getNoteDetail(Long id, String username) {
        Notes n = notesMapper.findById(id);
        if (n == null) throw new BusinessException("笔记不存在");

        if (n.getIsPublic() != 1 && !n.getUsername().equals(username)) {
            throw new BusinessException("无权查看此笔记");
        }
        if (n.getIsPublic() == 1 && (n.getAuditStatus() == null || n.getAuditStatus() != 2)
                && !n.getUsername().equals(username)) {
            throw new BusinessException("笔记暂不可见");
        }

        Map<String, Object> data = new HashMap<>();
        data.put("note", n);
        data.put("images", noteImagesMapper.findByNoteId(id).stream()
                .map(NoteImages::getImageUrl).toList());
        data.put("likeCount", likesMapper.countByNoteId(id));
        data.put("liked", likesMapper.countByNoteAndUser(id, username) > 0);
        return data;
    }

    @Override
    public int countPublicByUsername(String username) {
        return notesMapper.countPublicByUsername(username);
    }

    @Override
    public int countPendingAudit() {
        return notesMapper.countPendingAudit();
    }

    @Override
    public int countAuditPassedPublic() {
        return notesMapper.countAuditPassedPublic();
    }

    @Override
    public List<Map<String, Object>> findTopNoteLikes(int limit) {
        return likesMapper.findTopByLikeCount(limit);
    }

    @Override
    public List<Notes> findByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) return new ArrayList<>();
        return notesMapper.findByIds(ids);
    }

    @Override
    public void insertNoteImage(Long noteId, String url) {
        NoteImages ni = new NoteImages();
        ni.setNoteId(noteId);
        ni.setImageUrl(url);
        noteImagesMapper.insert(ni);
    }

    @Override
    public void deleteImagesByNoteId(Long noteId) {
        List<NoteImages> images = noteImagesMapper.findByNoteId(noteId);
        for (NoteImages img : images) {
            if (img.getImageUrl() != null) {
                UploadUtils.deleteByUrl(img.getImageUrl());
            }
        }
        noteImagesMapper.deleteByNoteId(noteId);
    }

    @Override
    public Map<String, Object> adminNoteDetail(Long id) {
        Notes n = notesMapper.findById(id);
        if (n == null) throw new BusinessException("笔记不存在");

        Map<String, Object> data = new HashMap<>();
        data.put("note", n);
        data.put("images", noteImagesMapper.findByNoteId(id).stream()
                .map(NoteImages::getImageUrl).toList());
        data.put("likeCount", likesMapper.countByNoteId(id));
        data.put("commentCount", commentsMapper.countByNoteId(id));
        return data;
    }
}