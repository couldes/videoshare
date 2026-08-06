package com.videoshare.admin.service.impl;

import com.videoshare.admin.client.CommentInnerApiClient;
import com.videoshare.common.entity.CommentInfo;
import com.videoshare.common.exception.BusinessException;
import com.videoshare.common.vo.PaginationResultVO;
import com.videoshare.common.vo.ResponseVO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminCommentServiceImplTest {

    @Mock private CommentInnerApiClient commentInnerApiClient;

    @InjectMocks private AdminCommentServiceImpl service;

    @Test
    void getCommentListReturnsDataOnSuccess() {
        CommentInfo c = new CommentInfo();
        c.setCommentId(1L);
        PaginationResultVO<CommentInfo> page = new PaginationResultVO<>(1, 20, 1, Arrays.asList(c));
        when(commentInnerApiClient.getCommentList("v1", 1, 20, null))
                .thenReturn(ResponseVO.success(page));

        PaginationResultVO<CommentInfo> result = service.getCommentList("v1", 1, 20, null);

        assertEquals(1, result.getTotal());
        assertEquals(1, result.getList().size());
    }

    @Test
    void getCommentListThrowsBusinessExceptionOnError() {
        when(commentInnerApiClient.getCommentList(null, 1, 20, 1))
                .thenReturn(ResponseVO.error("查询失败"));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.getCommentList(null, 1, 20, 1));
        assertEquals("查询失败", ex.getMessage());
    }

    @Test
    void updateCommentStatusDelegatesOnSuccess() {
        when(commentInnerApiClient.updateStatus(1L, 2)).thenReturn(ResponseVO.success());

        service.updateCommentStatus(1L, 2);

        verify(commentInnerApiClient).updateStatus(1L, 2);
    }

    @Test
    void updateCommentStatusThrowsOnError() {
        when(commentInnerApiClient.updateStatus(1L, 9))
                .thenReturn(ResponseVO.error("非法状态值：1=通过 2=删除"));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.updateCommentStatus(1L, 9));
        assertEquals("非法状态值：1=通过 2=删除", ex.getMessage());
    }

    @Test
    void deleteCommentDelegatesOnSuccess() {
        when(commentInnerApiClient.delete(2L)).thenReturn(ResponseVO.success());

        service.deleteComment(2L);

        verify(commentInnerApiClient).delete(2L);
    }

    @Test
    void deleteCommentThrowsOnError() {
        when(commentInnerApiClient.delete(3L)).thenReturn(ResponseVO.error("评论不存在"));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.deleteComment(3L));
        assertEquals("评论不存在", ex.getMessage());
    }
}
