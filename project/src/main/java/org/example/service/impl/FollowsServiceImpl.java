package org.example.service.impl;

import org.example.Mapper.FollowsMapper;
import org.example.Mapper.LikesMapper;
import org.example.Mapper.NotesMapper;
import org.example.Pojo.Follows;
import org.example.common.BusinessException;
import org.example.service.FollowsService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class FollowsServiceImpl implements FollowsService {

    private final FollowsMapper followsMapper;
    private final NotesMapper notesMapper;
    private final LikesMapper likesMapper;

    public FollowsServiceImpl(FollowsMapper followsMapper,
                              NotesMapper notesMapper,
                              LikesMapper likesMapper) {
        this.followsMapper = followsMapper;
        this.notesMapper = notesMapper;
        this.likesMapper = likesMapper;
    }

    @Override
    public void follow(String follower, String followee) {
        if (follower.equals(followee)) {
            throw new BusinessException("不能关注自己");
        }
        if (followsMapper.count(follower, followee) > 0) {
            throw new BusinessException("已经关注过了");
        }
        Follows f = new Follows();
        f.setFollower(follower);
        f.setFollowee(followee);
        followsMapper.insert(f);
    }

    @Override
    public void unfollow(String follower, String followee) {
        followsMapper.delete(follower, followee);
    }

    @Override
    public boolean isFollowing(String follower, String followee) {
        return followsMapper.count(follower, followee) > 0;
    }

    @Override
    public List<String> listFollowees(String follower) {
        return followsMapper.findFollowees(follower);
    }

    @Override
    public List<String> listFollowers(String followee) {
        return followsMapper.findFollowers(followee);
    }

    @Override
    public int countFollowers(String followee) {
        return followsMapper.countFollowers(followee);
    }

    @Override
    public int countFollowees(String follower) {
        return followsMapper.countFollowees(follower);
    }

    @Override
    public List<Map<String, Object>> topAuthors(int limit) {
        return followsMapper.findTopAuthors(limit);
    }

    @Override
    public List<Map<String, Object>> followeesDetail(String follower) {
        List<String> names = followsMapper.findFollowees(follower);
        List<Map<String, Object>> result = new ArrayList<>();
        for (String name : names) {
            Map<String, Object> m = new HashMap<>();
            m.put("username", name);
            m.put("avatar", "/avatar/" + name);
            m.put("noteCount", notesMapper.countPublicByUsername(name));
            m.put("totalLikes", likesMapper.countTotalByUsername(name));
            m.put("followerCount", followsMapper.countFollowers(name));
            result.add(m);
        }
        return result;
    }
}