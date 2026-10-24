package org.example.Controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.example.common.Result;
import org.example.common.SessionUtil;
import org.example.Pojo.Users;
import org.example.common.UploadUtils;
import org.example.dto.LoginDTO;
import org.example.dto.RegisterDTO;
import org.example.dto.UserVO;
import org.example.service.UsersService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api")
public class UsersController {

    private final UsersService usersService;

    public UsersController(UsersService usersService) {
        this.usersService = usersService;
    }

    @PostMapping("/register")
    public Result register(@Valid RegisterDTO dto,
                           @RequestParam(value = "avatar", required = false) MultipartFile avatar) throws IOException {
        if (!dto.getPassword().equals(dto.getConfirmPassword())) {
            return Result.error("两次输入的密码不一致");
        }
        Users users = new Users();
        users.setUsername(dto.getUsername().trim());
        users.setPhone(dto.getPhone().trim());
        users.setPassword(dto.getPassword());
        users.setGrade(dto.getGrade() != null ? dto.getGrade().trim() : null);
        users.setMajor(dto.getMajor() != null ? dto.getMajor().trim() : null);
        users.setSchool(dto.getSchool() != null ? dto.getSchool().trim() : null);
        users.setRegion(dto.getRegion() != null ? dto.getRegion().trim() : null);
        users.setAvatar(UploadUtils.save(avatar));

        usersService.register(users);
        return Result.success("注册成功", null);
    }

    @PostMapping("/login")
    public Result login(@Valid @RequestBody LoginDTO dto, HttpServletRequest request) {
        Users user = usersService.login(dto.getAccount().trim(), dto.getPassword());
        HttpSession session = request.getSession(false);
        if (session != null) session.invalidate();
        request.getSession(true).setAttribute("loginUser", user);
        return Result.success("登录成功", toUserVO(user));
    }

    @GetMapping("/user/info")
    public Result userInfo(HttpSession session) {
        return Result.success(toUserVO(SessionUtil.currentUser(session)));
    }

    @PostMapping("/logout")
    public Result logout(HttpSession session) {
        session.invalidate();
        return Result.success("已退出登录", null);
    }

    @PostMapping("/user/update")
    public Result update(
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) String password,
            @RequestParam(required = false) String grade,
            @RequestParam(required = false) String major,
            @RequestParam(required = false) String school,
            @RequestParam(required = false) String region,
            @RequestParam(value = "avatar", required = false) MultipartFile avatar,
            @RequestParam(value = "removeAvatar", required = false) String removeAvatar,
            HttpSession session) throws IOException {

        Users u = SessionUtil.currentUser(session);

        if (username != null && !username.isBlank()) u.setUsername(username.trim());
        if (phone != null && !phone.isBlank()) u.setPhone(phone.trim());
        if (password != null && !password.isBlank()) u.setPassword(password);
        else u.setPassword(null);
        if (grade != null) u.setGrade(grade.isBlank() ? null : grade.trim());
        if (major != null) u.setMajor(major.isBlank() ? null : major.trim());
        if (school != null) u.setSchool(school.isBlank() ? null : school.trim());
        if (region != null) u.setRegion(region.isBlank() ? null : region.trim());
        if ("true".equals(removeAvatar)) {
            u.setAvatar(null);
        } else {
            String avatarPath = UploadUtils.save(avatar);
            if (avatarPath != null) u.setAvatar(avatarPath);
        }

        usersService.update(u);
        return Result.success("修改成功", null);
    }

    @PostMapping("/user/cancel")
    public Result cancelAccount(HttpSession session) {
        Users loginUser = SessionUtil.currentUser(session);
        usersService.delete(loginUser.getId(), loginUser.getUsername());
        session.invalidate();
        return Result.success("账户已注销", null);
    }

    @GetMapping("/user/public/{username}")
    public Result userPublicInfo(@PathVariable String username) {
        Users u = usersService.getUserByUsername(username);
        if (u == null) return Result.error("用户不存在");
        UserVO vo = new UserVO();
        vo.setId(u.getId());
        vo.setUsername(u.getUsername());
        vo.setAvatar(u.getAvatar());
        vo.setGrade(u.getGrade());
        vo.setMajor(u.getMajor());
        vo.setSchool(u.getSchool());
        vo.setRegion(u.getRegion());
        return Result.success(vo);
    }

    private UserVO toUserVO(Users user) {
        UserVO vo = new UserVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setPhone(user.getPhone());
        vo.setAvatar(user.getAvatar());
        vo.setGrade(user.getGrade());
        vo.setMajor(user.getMajor());
        vo.setSchool(user.getSchool());
        vo.setRegion(user.getRegion());
        vo.setRole(user.getRole());
        vo.setStatus(user.getStatus());
        return vo;
    }
}