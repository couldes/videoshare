// 路径: web/src/main/java/com/videoshare/web/service/CommentService.java
package com.videoshare.web.service;

import com.videoshare.common.entity.CommentInfo;
import com.videoshare.common.vo.CommentVO;
import com.videoshare.common.vo.PaginationResultVO;

public interface CommentService {
    PaginationResultVO<CommentVO> getCommentList(String videoId, Integer pageNum, Integer pageSize);
    CommentVO postComment(String userId, String videoId, String content,
                          Long pCommentId, String replyUserId);
    boolean toggleLike(String userId, Long commentId);
    void deleteComment(Long commentId, String userId);

    // ===== 以下为 admin 端内部接口使用（经 /innerApi/comment/** 暴露）=====
    PaginationResultVO<CommentInfo> getCommentListForAdmin(String videoId, Integer pageNum,
                                                           Integer pageSize, Integer status);
    void updateCommentStatusForAdmin(Long commentId, Integer status);
    void deleteCommentForAdmin(Long commentId);
}