package org.example.Controller;

import jakarta.servlet.http.HttpSession;
import org.example.Pojo.Comments;
import org.example.Pojo.Notes;
import org.example.common.PageResult;
import org.example.common.Result;
import org.example.common.SessionUtil;
import org.example.Pojo.Users;
import org.example.common.UploadUtils;
import org.example.dto.NoteEnrichedVO;
import org.example.service.CommentsService;
import org.example.service.FollowsService;
import org.example.service.NotesService;
import org.example.Mapper.LikesMapper;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/note")
public class NotesController {

    private final NotesService notesService;
    private final CommentsService commentsService;
    private final FollowsService followsService;
    private final LikesMapper likesMapper;

    public NotesController(NotesService notesService,
                           CommentsService commentsService,
                           FollowsService followsService,
                           LikesMapper likesMapper) {
        this.notesService = notesService;
        this.commentsService = commentsService;
        this.followsService = followsService;
        this.likesMapper = likesMapper;
    }

    private void saveImagesToNote(Long noteId, MultipartFile[] images) throws IOException {
        if (images == null || images.length == 0) return;
        for (MultipartFile img : images) {
            if (img == null || img.isEmpty()) continue;
            String url = UploadUtils.save(img);
            if (url != null) {
                notesService.insertNoteImage(noteId, url);
            }
        }
    }

    @PostMapping("/publish")
    public Result publish(
            @RequestParam String title,
            @RequestParam(required = false) String content,
            @RequestParam(required = false) String bookName,
            @RequestParam(value = "images", required = false) MultipartFile[] images,
            @RequestParam(value = "isPublic", defaultValue = "1") Integer isPublic,
            HttpSession session) throws IOException {

        Users user = SessionUtil.currentUser(session);
        Notes notes = new Notes();
        notes.setUsername(user.getUsername());
        notes.setTitle(title.trim());
        notes.setContent(content);
        notes.setBookName(bookName != null ? bookName.trim() : null);
        notes.setIsPublic(isPublic);

        notesService.publish(notes);
        saveImagesToNote(notes.getId(), images);
        return Result.success("发布成功", null);
    }

    @PostMapping("/update/{id}")
    public Result update(
            @PathVariable Long id,
            @RequestParam String title,
            @RequestParam(required = false) String content,
            @RequestParam(required = false) String bookName,
            @RequestParam(value = "images", required = false) MultipartFile[] images,
            @RequestParam(value = "isPublic", defaultValue = "1") Integer isPublic,
            HttpSession session) throws IOException {

        Users user = SessionUtil.currentUser(session);
        Notes existing = notesService.getById(id);
        if (existing == null) return Result.error("笔记不存在");
        if (!existing.getUsername().equals(user.getUsername())) return Result.error("无权修改此笔记");

        existing.setTitle(title.trim());
        existing.setContent(content);
        existing.setBookName(bookName != null ? bookName.trim() : existing.getBookName());
        existing.setIsPublic(isPublic);

        notesService.deleteImagesByNoteId(id);
        saveImagesToNote(id, images);

        notesService.update(existing);
        return Result.success("修改成功", null);
    }

    @GetMapping("/list")
    public Result list(@RequestParam(required = false) String scope, HttpSession session) {
        String username = SessionUtil.currentUser(session).getUsername();
        List<Notes> raw = switch (scope == null ? "all" : scope) {
            case "public"  -> notesService.listMyNotesByScope(username, 1);
            case "private" -> notesService.listMyNotesByScope(username, 0);
            case "pending" -> notesService.listMyPending(username);
            default        -> notesService.listMyNotes(username);
        };
        return Result.success(notesService.enrichNotes(raw, username));
    }

    @GetMapping("/community")
    public Result community(HttpSession session) {
        String username = SessionUtil.currentUser(session).getUsername();
        List<Notes> notes = notesService.listRandomPublic(3);
        return Result.success(notesService.enrichNotes(notes, username));
    }

    @GetMapping("/ranking")
    public Result ranking(HttpSession session) {
        String username = SessionUtil.currentUser(session).getUsername();

        AtomicLong noteRank = new AtomicLong(1);
        List<Map<String, Object>> noteLikes = notesService.findTopNoteLikes(10).stream()
                .map(row -> {
                    Map<String, Object> m = new HashMap<>(row);
                    m.put("rank", noteRank.getAndIncrement());
                    return m;
                }).collect(Collectors.toList());

        List<Long> noteIds = noteLikes.stream()
                .map(m -> ((Number) m.get("id")).longValue())
                .collect(Collectors.toList());

        if (!noteIds.isEmpty()) {
            List<Notes> rankNotes = notesService.findByIds(noteIds);
            Map<Long, NoteEnrichedVO> enrichedMap = notesService.enrichNotes(rankNotes, username).stream()
                    .collect(Collectors.toMap(NoteEnrichedVO::getId, v -> v));
            for (Map<String, Object> m : noteLikes) {
                Long nid = ((Number) m.get("id")).longValue();
                NoteEnrichedVO vo = enrichedMap.get(nid);
                if (vo != null) {
                    m.put("liked", vo.getLiked());
                    m.put("commentCount", vo.getCommentCount());
                    m.put("following", vo.getFollowing());
                    m.put("isOwner", vo.getIsOwner());
                }
            }
        }

        List<Map<String, Object>> followerRankData = followsService.topAuthors(10);
        AtomicLong followerAtomic = new AtomicLong(1);
        List<Map<String, Object>> followerRankList = followerRankData.stream().map(row -> {
            Map<String, Object> m = new HashMap<>();
            String u = (String) row.get("username");
            m.put("rank", followerAtomic.getAndIncrement());
            m.put("username", u);
            m.put("followerCount", row.get("followerCount"));
            m.put("following", !username.equals(u) && followsService.isFollowing(username, u));
            m.put("isOwner", username.equals(u));
            return m;
        }).collect(Collectors.toList());

        List<Map<String, Object>> userLikesData = likesMapper.findTopUsersByTotalLikes(10);
        AtomicLong userLikesAtomic = new AtomicLong(1);
        List<Map<String, Object>> userLikesList = userLikesData.stream().map(row -> {
            Map<String, Object> m = new HashMap<>();
            String u = (String) row.get("username");
            m.put("rank", userLikesAtomic.getAndIncrement());
            m.put("username", u);
            m.put("totalLikes", row.get("totalLikes"));
            m.put("following", !username.equals(u) && followsService.isFollowing(username, u));
            m.put("isOwner", username.equals(u));
            return m;
        }).collect(Collectors.toList());

        Map<String, Object> data = new HashMap<>();
        data.put("noteLikes", noteLikes);
        data.put("followerRank", followerRankList);
        data.put("userLikes", userLikesList);
        return Result.success(data);
    }

    @GetMapping("/user/{username}")
    public Result userProfile(@PathVariable String username, HttpSession session) {
        String me = SessionUtil.currentUser(session).getUsername();
        Map<String, Object> data = new HashMap<>();
        data.put("username", username);
        data.put("avatar", "/avatar/" + username);
        data.put("noteCount", notesService.countPublicByUsername(username));
        data.put("totalLikes", likesMapper.countTotalByUsername(username));
        data.put("followerCount", followsService.countFollowers(username));
        data.put("followingCount", followsService.countFollowees(username));
        data.put("following", !me.equals(username) && followsService.isFollowing(me, username));
        data.put("isOwner", me.equals(username));

        List<Notes> notes = notesService.listByAuthorAndPublic(username);
        data.put("notes", notesService.enrichNotes(notes, me));
        return Result.success(data);
    }

    @PostMapping("/{id}/like")
    public Result toggleLike(@PathVariable Long id, HttpSession session) {
        Users user = SessionUtil.currentUser(session);
        boolean liked = notesService.toggleLike(id, user.getUsername());
        return Result.success(liked ? "点赞成功" : "已取消点赞", liked);
    }

    @GetMapping("/{id}")
    public Result detail(@PathVariable Long id, HttpSession session) {
        Users user = SessionUtil.currentUser(session);
        Map<String, Object> data = notesService.getNoteDetail(id, user.getUsername());
        data.put("comments", commentsService.listByNoteId(id));
        return Result.success(data);
    }

    @PostMapping("/delete/{id}")
    public Result delete(@PathVariable Long id, HttpSession session) {
        String username = SessionUtil.currentUser(session).getUsername();
        notesService.delete(id, username);
        return Result.success("删除成功", null);
    }

    @PostMapping("/toggle/{id}")
    public Result toggle(@PathVariable Long id, HttpSession session) {
        Users user = SessionUtil.currentUser(session);
        Notes n = notesService.getById(id);
        if (n == null) return Result.error("笔记不存在");
        if (!n.getUsername().equals(user.getUsername())) return Result.error("无权操作此笔记");

        Notes update = new Notes();
        update.setId(id);
        int newPublic = n.getIsPublic() == 1 ? 0 : 1;
        update.setIsPublic(newPublic);
        update.setAuditStatus(newPublic == 1 ? 1 : 2);
        notesService.update(update);
        return Result.success("操作成功", null);
    }

    @PostMapping("/comment")
    public Result comment(
            @RequestParam Long noteId,
            @RequestParam(value = "parentId", required = false) Long parentId,
            @RequestParam(required = false) String content,
            @RequestParam(value = "image", required = false) MultipartFile image,
            HttpSession session) throws IOException {

        Users user = SessionUtil.currentUser(session);
        notesService.getById(noteId);

        if (parentId != null) {
            Comments parent = commentsService.findById(parentId);
            if (parent == null || !parent.getNoteId().equals(noteId)) {
                return Result.error("回复的评论不存在");
            }
        }

        Comments c = new Comments();
        c.setNoteId(noteId);
        c.setParentId(parentId != null ? parentId : 0L);
        c.setUsername(user.getUsername());
        c.setContent(content);
        c.setImage(image != null && !image.isEmpty() ? UploadUtils.save(image) : null);
        commentsService.add(c);
        return Result.success("评论成功", null);
    }

    @GetMapping("/comment/{noteId}")
    public Result listComments(@PathVariable Long noteId) {
        return Result.success(commentsService.listByNoteId(noteId));
    }

    @PostMapping("/comment/delete/{id}")
    public Result deleteComment(@PathVariable Long id, HttpSession session) {
        Users user = SessionUtil.currentUser(session);
        if ("ADMIN".equals(user.getRole())) {
            commentsService.adminDeleteById(id);
        } else {
            commentsService.deleteById(id, user.getUsername());
        }
        return Result.success("删除成功", null);
    }

    @GetMapping("/liked")
    public Result likedNotes(@RequestParam(defaultValue = "1") int pageNum,
                             @RequestParam(defaultValue = "10") int pageSize,
                             HttpSession session) {
        String username = SessionUtil.currentUser(session).getUsername();
        PageResult<Notes> pr = notesService.listLikedByUser(username, pageNum, pageSize);
        List<NoteEnrichedVO> enriched = notesService.enrichNotes(pr.getList(), username);
        PageResult<NoteEnrichedVO> result = new PageResult<>(pr.getTotal(), pr.getPageNum(), pr.getPageSize(), enriched);
        return Result.success(result);
    }

    @GetMapping("/author/{username}")
    public Result authorNotes(@PathVariable String username, HttpSession session) {
        String viewer = SessionUtil.currentUser(session).getUsername();
        List<Notes> notes = notesService.listByAuthorAndPublic(username);
        boolean following = followsService.isFollowing(viewer, username);
        Map<String, Object> data = new HashMap<>();
        data.put("notes", notesService.enrichNotes(notes, viewer));
        data.put("following", following);
        data.put("followerCount", followsService.countFollowers(username));
        return Result.success(data);
    }
}