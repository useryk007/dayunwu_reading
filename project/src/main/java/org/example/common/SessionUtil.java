package org.example.common;

import jakarta.servlet.http.HttpSession;
import org.example.Pojo.Users;

/**
 * Session 工具类。
 *
 * 把从 HttpSession 里取登录用户的逻辑统一封装，避免每个 Controller 都写一遍：
 *     (Users) session.getAttribute("loginUser")
 *
 * 所有 Controller 都可以这样用：
 *     Users user = SessionUtil.currentUser(session);
 */
public final class SessionUtil {

    /** 登录用户在 HttpSession 中的 key，和 LoginInterceptor/UsersController/login 保持一致 */
    public static final String USER_KEY = "loginUser";

    private SessionUtil() {}

    /** 从 session 中取出当前登录的 Users 对象；未登录返回 null（通常已被 LoginInterceptor 拦截） */
    public static Users currentUser(HttpSession session) {
        return (Users) session.getAttribute(USER_KEY);
    }
}