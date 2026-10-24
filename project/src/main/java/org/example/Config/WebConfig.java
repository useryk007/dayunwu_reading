package org.example.Config;

import org.example.common.UploadUtils;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 全局配置：静态资源映射 + 拦截器注册。
 *
 * addResourceHandlers —— 把 URL /uploads/** 映射到磁盘上传目录，
 *   前端就能直接访问服务器上存的图片文件了。
 *
 * addInterceptors —— 注册两层拦截器：
 *   1. LoginInterceptor 拦截 /home.html 和 /api/**，
 *      但放行 /api/login /api/register 以及登录页、静态资源
 *   2. AdminInterceptor 只拦截 /admin/**，要求已登录 + role=ADMIN
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final LoginInterceptor loginInterceptor;
    private final AdminInterceptor adminInterceptor;

    public WebConfig(LoginInterceptor loginInterceptor, AdminInterceptor adminInterceptor) {
        this.loginInterceptor = loginInterceptor;
        this.adminInterceptor = adminInterceptor;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + UploadUtils.UPLOAD_DIR + "/");
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loginInterceptor)
                .addPathPatterns("/home.html", "/api/**")
                .excludePathPatterns("/login.html", "/register.html", "/css/**", "/js/**", "/uploads/**",
                        "/api/login", "/api/register");

        registry.addInterceptor(adminInterceptor)
                .addPathPatterns("/admin/**");
    }
}