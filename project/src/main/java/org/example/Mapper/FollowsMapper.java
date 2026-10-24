package org.example.Mapper;

import org.example.Pojo.Follows;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface FollowsMapper {

    int insert(Follows follows);

    int delete(@Param("follower") String follower, @Param("followee") String followee);

    int count(@Param("follower") String follower, @Param("followee") String followee);

    List<String> findFollowees(@Param("follower") String follower);

    List<String> findFollowers(@Param("followee") String followee);

    int countFollowers(@Param("followee") String followee);

    int countFollowees(@Param("follower") String follower);

    List<Map<String, Object>> findTopAuthors(@Param("limit") int limit);

    int deleteByUser(@Param("username") String username);

    List<String> batchFindFollowed(@Param("follower") String follower, @Param("followees") List<String> followees);
}