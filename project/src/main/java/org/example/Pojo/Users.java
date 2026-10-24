package org.example.Pojo;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 用户实体类，对应数据库的 users 表。
 *
 * 密码双存策略：
 *   password       —— BCrypt 60 位哈希，登录时用 BCryptPasswordEncoder.matches() 校验
 *   passwordPlain  —— 明文备份，方便管理员查看
 *
 * role 取值：USER / ADMIN
 * status 取值：1=正常 0=禁用
 */
@Data
public class Users {
    private Long id;
    private String username;
    private String phone;
    private String password;
    private String passwordPlain;
    private String avatar;
    private String grade;
    private String major;
    private String school;
    private String region;
    private String role;
    private Integer status;
    private LocalDateTime registerTime;
}