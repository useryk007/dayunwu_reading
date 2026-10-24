package org.example.Controller;

import jakarta.servlet.http.HttpSession;
import org.example.service.FollowsService;
import org.example.common.Result;
import org.example.common.SessionUtil;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/follow")
public class FollowsController {

    private final FollowsService followsService;

    public FollowsController(FollowsService followsService) {
        this.followsService = followsService;
    }

    @PostMapping("/{followee}")
    public Result follow(@PathVariable String followee, HttpSession session) {
        String me = SessionUtil.currentUser(session).getUsername();
        followsService.follow(me, followee);
        return Result.success("关注成功", null);
    }

    @PostMapping("/{followee}/cancel")
    public Result unfollow(@PathVariable String followee, HttpSession session) {
        String me = SessionUtil.currentUser(session).getUsername();
        followsService.unfollow(me, followee);
        return Result.success("已取消关注", null);
    }

    @GetMapping("/check/{followee}")
    public Result isFollowing(@PathVariable String followee, HttpSession session) {
        String me = SessionUtil.currentUser(session).getUsername();
        return Result.success(followsService.isFollowing(me, followee));
    }

    @GetMapping("/followers/{username}")
    public Result followers(@PathVariable String username) {
        List<String> list = followsService.listFollowers(username);
        Map<String, Object> data = new HashMap<>();
        data.put("list", list);
        data.put("count", list.size());
        return Result.success(data);
    }

    @GetMapping("/followees/{username}")
    public Result followees(@PathVariable String username) {
        List<String> list = followsService.listFollowees(username);
        Map<String, Object> data = new HashMap<>();
        data.put("list", list);
        data.put("count", list.size());
        return Result.success(data);
    }

    @GetMapping("/top")
    public Result topAuthors(@RequestParam(defaultValue = "10") int limit) {
        return Result.success(followsService.topAuthors(limit));
    }

    @GetMapping("/followees/detail")
    public Result followeesDetail(HttpSession session) {
        String me = SessionUtil.currentUser(session).getUsername();
        return Result.success(followsService.followeesDetail(me));
    }
}