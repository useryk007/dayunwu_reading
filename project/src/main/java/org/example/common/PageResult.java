package org.example.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 分页结果封装。
 *
 * 用于笔记列表、点赞列表等需要分页展示的场景。
 * total 是符合条件的总记录数（用于前端计算总页数），
 * list 是当前页的数据子集。接口层可以直接把它包进 Result.data 返回给前端。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageResult<T> {
    /** 符合条件的总记录数 */
    private long total;
    /** 当前页号，从 1 开始 */
    private int pageNum;
    /** 每页大小 */
    private int pageSize;
    /** 当前页的数据列表 */
    private List<T> list;
}