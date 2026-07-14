// 路径: web/src/main/java/com/videoshare/web/service/CommentService.java
package com.videoshare.web.service;

import com.videoshare.common.vo.CommentVO;
import com.videoshare.common.vo.PaginationResultVO;

public interface CommentService {
    PaginationResultVO<CommentVO> getCommentList(String videoId, Integer pageNum, Integer pageSize);
    CommentVO postComment(String userId, String videoId, String content,
                          Long pCommentId, String replyUserId);
    boolean toggleLike(String userId, Long commentId);
    void deleteComment(Long commentId, String userId);
}