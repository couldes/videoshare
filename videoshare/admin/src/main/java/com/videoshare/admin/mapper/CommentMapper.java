// 路径: web/src/main/java/com/videoshare/web/mapper/CommentMapper.java
package com.videoshare.admin.mapper;

import com.videoshare.common.entity.CommentInfo;
import com.videoshare.common.query.CommentQuery;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CommentMapper {
    Integer insert(CommentInfo comment);

    /** 分页查顶级评论（pCommentId=0）*/
    List<CommentInfo> selectTopComments(CommentQuery query);

    /** 统计顶级评论总数 */
    Integer countTopComments(@Param("videoId") String videoId, @Param("status") Integer status);

    /** 查询某顶级评论的所有回复（不分页，因为通常回复数较少） */
    List<CommentInfo> selectReplies(@Param("pCommentId") Long pCommentId,
                                    @Param("status") Integer status);

    /** 按 ID 查单条评论 */
    CommentInfo selectByCommentId(@Param("commentId") Long commentId);

    /** 修改状态（逻辑删除：status=2）*/
    Integer updateStatus(@Param("commentId") Long commentId, @Param("status") Integer status);

    /** 点赞数 ±1 */
    Integer updateLikeCount(@Param("commentId") Long commentId, @Param("count") int count);
}