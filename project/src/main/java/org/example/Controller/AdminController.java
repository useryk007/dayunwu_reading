package org.example.Controller;

import jakarta.servlet.http.HttpSession;
import org.example.Pojo.Users;
import org.example.service.CommentsService;
import org.example.service.FollowsService;
import org.example.service.NotesService;
import org.example.service.UsersService;
import org.example.common.LoginFailCounter;
import org.example.common.Result;
import org.example.common.SessionUtil;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin")
public class AdminController {

    private final UsersService usersService;
    private final NotesService notesService;
    private final CommentsService commentsService;
    private final FollowsService followsService;
    private final LoginFailCounter failCounter;

    public AdminController(UsersService usersService, NotesService notesService,
                           CommentsService commentsService, FollowsService followsService,
                           LoginFailCounter failCounter) {
        this.usersService = usersService;
        this.notesService = notesService;
        this.commentsService = commentsService;
        this.followsService = followsService;
        this.failCounter = failCounter;
    }

    @GetMapping("/users")
    public Result listUsers() {
        List<Users> list = usersService.listAll();
        List<Map<String, Object>> result = list.stream().map(u -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", u.getId());
            m.put("username", u.getUsername());
            m.put("phone", u.getPhone());
            m.put("avatar", u.getAvatar());
            m.put("role", u.getRole());
            m.put("status", u.getStatus());
            m.put("registerTime", u.getRegisterTime());
            m.put("locked", failCounter.isLocked(u.getUsername()));
            if (failCounter.isLocked(u.getUsername())) {
                m.put("remainingLockMinutes", failCounter.remainingLockMillis(u.getUsername()) / 60000);
            }
            return m;
        }).toList();
        return Result.success(result);
    }

    @PostMapping("/user/{id}/status")
    public Result updateUserStatus(@PathVariable Long id, @RequestParam Integer status) {
        Users target = usersService.getUserById(id);
        if (target == null) return Result.error("用户不存在");
        usersService.updateStatus(id, status);
        if (status == 1) {
            failCounter.clear(target.getUsername());
        }
        return Result.success("操作成功", null);
    }

    @PostMapping("/user/{id}/unlock")
    public Result unlockUser(@PathVariable Long id) {
        Users target = usersService.getUserById(id);
        if (target == null) return Result.error("用户不存在");
        failCounter.clear(target.getUsername());
        return Result.success("账户已解锁", null);
    }

    @PostMapping("/user/{id}/delete")
    public Result deleteUser(@PathVariable Long id, HttpSession session) {
        Users current = SessionUtil.currentUser(session);
        Users target = usersService.getUserById(id);
        if (target == null) return Result.error("用户不存在");
        if ("ADMIN".equals(target.getRole())) return Result.error("管理员账号不允许删除");
        if (target.getUsername().equals(current.getUsername())) {
            session.invalidate();
        }
        usersService.delete(id, target.getUsername());
        return Result.success("用户已删除", null);
    }

    @GetMapping("/notes/pending")
    public Result pendingAudit() {
        return Result.success(notesService.listPendingAudit());
    }

    @PostMapping("/note/{id}/audit")
    public Result audit(@PathVariable Long id,
                        @RequestParam Integer auditStatus,
                        @RequestParam(required = false) String auditRemark) {
        if (auditStatus == 3) {
            notesService.reject(id);
            return Result.success("笔记已驳回并删除", null);
        }
        notesService.audit(id, auditStatus, auditRemark);
        return Result.success("审核完成", null);
    }

    @GetMapping("/notes/published")
    public Result publishedNotes() {
        return Result.success(notesService.listAuditPassedPublic());
    }

    @GetMapping("/note/{id}/detail")
    public Result noteDetail(@PathVariable Long id) {
        return Result.success(notesService.adminNoteDetail(id));
    }

    @PostMapping("/note/{id}/offline")
    public Result offline(@PathVariable Long id) {
        notesService.offline(id);
        return Result.success("已下架", null);
    }

    @PostMapping("/comment/{id}/delete")
    public Result deleteComment(@PathVariable Long id) {
        commentsService.adminDeleteById(id);
        return Result.success("删除成功", null);
    }

    @GetMapping("/stats")
    public Result stats() {
        Map<String, Object> s = new HashMap<>();
        s.put("topAuthors", followsService.topAuthors(10));
        s.put("pendingCount", notesService.countPendingAudit());
        s.put("totalUsers", usersService.countAll());
        s.put("publishedNotes", notesService.countAuditPassedPublic());
        s.put("totalComments", commentsService.countAll());
        return Result.success(s);
    }
}