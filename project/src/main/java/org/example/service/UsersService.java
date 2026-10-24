package org.example.service;

import org.example.Pojo.Users;

import java.util.List;

/**
 * 用户业务层接口。
 *
 * 处理注册时的账号查重、BCrypt 密码加密，
 * 登录时的 BCrypt 密码校验，
 * 注销时的级联清理笔记/点赞/评论/关注关系。
 */
public interface UsersService {

    /** 注册新用户。密码会被 BCrypt 加密后入库；用户名/手机号查重失败时抛 BusinessException */
    void register(Users users);

    /** 登录校验。account 可以是用户名也可以是手机号；密码用 BCrypt matches 校验；失败抛 BusinessException */
    Users login(String account, String password);

    /** 按 id 查用户（管理员后台用） */
    Users getUserById(Long id);

    /** 按用户名查用户（用户主页展示公开信息用） */
    Users getUserByUsername(String username);

    /** 更新用户信息。只更新非 null 字段；密码会被重新 BCrypt 加密 */
    void update(Users users);

    /** 注销账号。会级联删除该用户名下的全部笔记、评论、点赞、关注关系 */
    void delete(Long id, String username);

    /** 管理员：列出全部用户 */
    List<Users> listAll();

    /** 管理员：启用(1) 或 禁用(0) 某用户 */
    void updateStatus(Long id, Integer status);

    /** 统计用户总数（数据看板用） */
    int countAll();
}