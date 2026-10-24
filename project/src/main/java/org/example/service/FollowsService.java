package org.example.service;

import org.example.Pojo.Follows;

import java.util.List;
import java.util.Map;

/**
 * 关注关系业务层接口。
 *
 * 提供关注/取关/判断状态/排行榜/粉丝列表等查询能力。
 * follow() 会校验不能关注自己、不能重复关注，失败抛 BusinessException。
 */
public interface FollowsService {

    /** 关注某人。不能关注自己、不能重复关注 */
    void follow(String follower, String followee);

    /** 取消关注 */
    void unfollow(String follower, String followee);

    /** 判断 follower 是否关注了 followee */
    boolean isFollowing(String follower, String followee);

    /** 查某人关注了哪些人（返回 followee 用户名列表） */
    List<String> listFollowees(String follower);

    /** 查某人的粉丝（返回 follower 用户名列表） */
    List<String> listFollowers(String followee);

    /** 统计某人的粉丝数 */
    int countFollowers(String followee);

    /** 统计某人关注的人数 */
    int countFollowees(String follower);

    /** 排行榜：按粉丝数降序取前 N 位作者 */
    List<Map<String, Object>> topAuthors(int limit);

    /** 我关注的人详情列表（每个被关注者附带笔记数、总点赞数、粉丝数） */
    List<Map<String, Object>> followeesDetail(String follower);
}