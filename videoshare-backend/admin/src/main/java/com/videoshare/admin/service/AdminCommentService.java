package com.videoshare.admin.service;

import com.videoshare.common.vo.PaginationResultVO;
import com.videoshare.common.entity.CommentInfo;

public interface AdminCommentService {
    PaginationResultVO<CommentInfo> getCommentList(String videoId, Integer pageNum, Integer pageSize, Integer status);
    void updateCommentStatus(Long commentId, Integer status);
    void deleteComment(Long commentId);
}
