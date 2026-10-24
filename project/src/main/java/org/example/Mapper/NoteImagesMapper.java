package org.example.Mapper;

import org.example.Pojo.NoteImages;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 笔记图片数据访问层，对应 note_images 表。
 *
 * 一篇笔记可以有多张图片（一对多），真实文件由 UploadUtils 存到磁盘，
 * 本表只记录 note_id 和图片的 URL 路径。
 */
@Mapper
public interface NoteImagesMapper {

    /** 新增一张笔记图片 */
    int insert(NoteImages image);

    /** 查某笔记的所有图片 */
    List<NoteImages> findByNoteId(@Param("noteId") Long noteId);

    /** 级联删除：删除某笔记的全部图片记录（编辑/删除笔记时用） */
    int deleteByNoteId(@Param("noteId") Long noteId);

    /** 级联删除：批量删除一组笔记的全部图片记录 */
    int deleteByNoteIds(@Param("noteIds") List<Long> noteIds);
}