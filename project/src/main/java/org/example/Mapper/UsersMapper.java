package org.example.Mapper;

import org.example.Pojo.Users;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用户数据访问层，对应 users 表。
 *
 * 所有 SQL 在 UsersMapper.xml 中定义，这里只声明接口签名。
 * MyBatis 通过 @MapperScan 自动扫描注册，XML 中的 namespace 必须与本接口全限定名一致。
 */
@Mapper
public interface UsersMapper {

    /** 新增用户（注册时用） */
    int insert(Users users);

    /** 按主键查询 */
    Users findById(Long id);

    /** 按用户名精确查询，登录/注册查重用 */
    Users findByUsername(String username);

    /** 按手机号精确查询 */
    Users findByPhone(String phone);

    /** 按登录账号查询（account 可以是用户名也可以是手机号） */
    Users findByAccount(String account);

    /** 按主键动态更新（只更新非 null 字段） */
    int updateById(Users users);

    /** 按主键删除（注销账号用） */
    int deleteById(Long id);

    /** 查询全部用户（管理员后台列表用） */
    List<Users> findAll();

    /** 启用/禁用用户账号 */
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);

    /** 统计用户总数（数据看板用） */
    int countAll();
}