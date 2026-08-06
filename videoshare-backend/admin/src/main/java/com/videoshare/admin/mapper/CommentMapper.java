package com.videoshare.admin.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 评论表级联清理专用 Mapper（仅删视频时使用）。
 * 评论管理（列表/审核/删除）已收拢到 web 侧（CommentInnerApiController + CommentServiceImpl），
 * admin 不再直接读写 comment_info，仅保留物理级联删除。
 */
@Mapper
public interface CommentMapper {

    /** 删除某视频的全部评论（物理删除，含子评论与已逻辑删除行；删视频时级联使用） */
    Integer deleteByVideoId(@Param("videoId") String videoId);
}
