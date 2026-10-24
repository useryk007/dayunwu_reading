package org.example.service.impl;

import org.example.Mapper.CommentsMapper;
import org.example.Pojo.Comments;
import org.example.common.BusinessException;
import org.example.common.UploadUtils;
import org.example.service.CommentsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 评论业务层实现。
 *
 * 核心难点是 listByNoteId —— 数据库存的是扁平表（每条记录只有 parentId 指向父评论），
 * 但前端需要嵌套的树形结构（每条评论带 children 子列表）。
 *
 * 组装策略（两遍遍历，O(n) 复杂度）：
 *   第一遍：按 parentId 分组，得到 Map<parentId, List<子评论>>，其中 parentId=null 归入 key=0
 *   第二遍：遍历每条评论，从分组 Map 里找出它的子评论列表，塞进 children 字段
 *   最后：key=0 的那组就是根评论（顶层评论），直接返回
 */
@Service
public class CommentsServiceImpl implements CommentsService {

    private final CommentsMapper commentsMapper;

    public CommentsServiceImpl(CommentsMapper commentsMapper) {
        this.commentsMapper = commentsMapper;
    }

    @Override
    public void add(Comments comments) {
        if (comments.getContent() == null || comments.getContent().isBlank()) {
            throw new BusinessException("评论内容不能为空");
        }
        commentsMapper.insert(comments);
    }

    /**
     * 查询笔记下的所有评论并组装成树形结构。
     * 数据库只存扁平表，运行时两遍遍历拼装 children 嵌套，前端拿到可直接递归渲染。
     */
    @Override
    public List<Comments> listByNoteId(Long noteId) {
        List<Comments> flatList = commentsMapper.findByNoteId(noteId);

        // 第一遍：按 parentId 分组。0 表示顶层评论
        Map<Long, List<Comments>> groupByParent = new HashMap<>();
        for (Comments c : flatList) {
            groupByParent.computeIfAbsent(c.getParentId(), k -> new ArrayList<>()).add(c);
        }

        // 第二遍：把每条评论的子列表挂到它的 children 字段上
        for (Comments c : flatList) {
            List<Comments> kids = groupByParent.get(c.getId());
            if (kids != null) {
                c.setChildren(kids);
            }
        }

        // parentId=0 对应的就是所有顶层评论，已是完整的树形了
        List<Comments> roots = groupByParent.get(0L);
        return roots != null ? roots : new ArrayList<>();
    }

    @Override
    public int countByNoteId(Long noteId) {
        return commentsMapper.countByNoteId(noteId);
    }

    @Override
    public void deleteByUser(String username) {
        List<Comments> userComments = commentsMapper.findByUsername(username);
        for (Comments c : userComments) {
            if (c.getImage() != null) UploadUtils.deleteByUrl(c.getImage());
        }
        commentsMapper.deleteByUsername(username);
    }

    @Override
    @Transactional
    public void deleteById(Long id, String username) {
        Comments c = commentsMapper.findById(id);
        if (c == null) throw new BusinessException("评论不存在");
        if (!c.getUsername().equals(username)) {
            throw new BusinessException("无权删除此评论");
        }
        cleanupCommentImages(id);
        commentsMapper.deleteByParentId(id);
        commentsMapper.deleteById(id);
    }

    @Override
    @Transactional
    public void adminDeleteById(Long id) {
        Comments c = commentsMapper.findById(id);
        if (c == null) throw new BusinessException("评论不存在");
        cleanupCommentImages(id);
        commentsMapper.deleteByParentId(id);
        commentsMapper.deleteById(id);
    }

    @Override
    public int countAll() {
        return commentsMapper.countAll();
    }

    @Override
    public Comments findById(Long id) {
        return commentsMapper.findById(id);
    }

    private void cleanupCommentImages(Long commentId) {
        List<Comments> children = commentsMapper.findByParentId(commentId);
        for (Comments child : children) {
            if (child.getImage() != null) UploadUtils.deleteByUrl(child.getImage());
            cleanupCommentImages(child.getId());
        }
        Comments self = commentsMapper.findById(commentId);
        if (self != null && self.getImage() != null) UploadUtils.deleteByUrl(self.getImage());
    }
}