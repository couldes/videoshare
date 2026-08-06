package com.videoshare.admin.service.impl;

import com.videoshare.admin.client.CommentInnerApiClient;
import com.videoshare.admin.service.AdminCommentService;
import com.videoshare.common.entity.CommentInfo;
import com.videoshare.common.exception.BusinessException;
import com.videoshare.common.vo.PaginationResultVO;
import com.videoshare.common.vo.ResponseVO;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * 评论管理已收拢到 web 侧（web CommentServiceImpl + CommentMapper）。
 * 本实现仅为薄委托层：经 CommentInnerApiClient 调用 web 内部接口，不再直连 comment_info。
 */
@Service
public class AdminCommentServiceImpl implements AdminCommentService {

    @Resource
    private CommentInnerApiClient commentInnerApiClient;

    @Override
    public PaginationResultVO<CommentInfo> getCommentList(String videoId, Integer pageNum,
                                                          Integer pageSize, Integer status) {
        return ensureSuccess(
                commentInnerApiClient.getCommentList(videoId, pageNum, pageSize, status)).getData();
    }

    @Override
    public void updateCommentStatus(Long commentId, Integer status) {
        ensureSuccess(commentInnerApiClient.updateStatus(commentId, status));
    }

    @Override
    public void deleteComment(Long commentId) {
        ensureSuccess(commentInnerApiClient.delete(commentId));
    }

    /** web 内部接口返回 error 时转回业务异常，透传错误信息 */
    private <T> ResponseVO<T> ensureSuccess(ResponseVO<T> vo) {
        if (!"success".equals(vo.getStatus())) {
            throw new BusinessException(vo.getInfo());
        }
        return vo;
    }
}
