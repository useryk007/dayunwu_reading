package org.example.Config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.example.Pojo.Users;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

/**
 * 管理员拦截器。
 *
 * 继承 LoginInterceptor 复用 session 检查逻辑，额外判断当前登录用户的 role 是否为 ADMIN。
 *   未登录 → 401（由父类 checkLogin 处理）
 *   已登录但非管理员 → 403 "无权限访问"
 *   管理员 → 放行
 *
 * 拦截范围在 WebConfig.addInterceptors() 中配置为 "/admin/**"。
 */
@Component
public class AdminInterceptor extends LoginInterceptor {

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request,
                             @NonNull HttpServletResponse response,
                             @NonNull Object handler) throws Exception {
        if (!super.checkLogin(request, response)) {
            return false;
        }

        HttpSession session = request.getSession(false);
        Users user = (Users) session.getAttribute("loginUser");
        if (!"ADMIN".equals(user.getRole())) {
            writeJson(response, 403, "无权限访问");
            return false;
        }
        return true;
    }
}