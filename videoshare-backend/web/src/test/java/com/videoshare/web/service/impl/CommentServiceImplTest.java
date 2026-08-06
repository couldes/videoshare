package com.videoshare.web.service.impl;

import com.videoshare.common.entity.CommentInfo;
import com.videoshare.common.exception.BusinessException;
import com.videoshare.common.query.CommentQuery;
import com.videoshare.common.vo.PaginationResultVO;
import com.videoshare.web.mapper.CommentMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CommentServiceImplTest {

    @Mock private CommentMapper commentMapper;

    @InjectMocks private CommentServiceImpl service;

    private CommentInfo comment(Long id) {
        CommentInfo c = new CommentInfo();
        c.setCommentId(id);
        return c;
    }

    @Test
    void getCommentListForAdminPassesFiltersAndReturnsPage() {
        when(commentMapper.selectTopComments(any()))
                .thenReturn(Arrays.asList(comment(1L), comment(2L)));
        when(commentMapper.countTopComments("v1", 1)).thenReturn(2);

        PaginationResultVO<CommentInfo> page = service.getCommentListForAdmin("v1", 1, 20, 1);

        assertEquals(2, page.getTotal());
        assertEquals(20, page.getPageSize());
        assertEquals(1, page.getPageNum());
        assertEquals(2, page.getList().size());

        ArgumentCaptor<CommentQuery> captor = ArgumentCaptor.forClass(CommentQuery.class);
        verify(commentMapper).selectTopComments(captor.capture());
        assertEquals("v1", captor.getValue().getVideoId());
        assertEquals(1, captor.getValue().getStatus());
        verify(commentMapper).countTopComments("v1", 1);
    }

    @Test
    void getCommentListForAdminAllowsGlobalQueryWhenFiltersNull() {
        when(commentMapper.selectTopComments(any())).thenReturn(Arrays.asList(comment(1L)));
        when(commentMapper.countTopComments(null, null)).thenReturn(1);

        service.getCommentListForAdmin(null, 1, 20, null);

        ArgumentCaptor<CommentQuery> captor = ArgumentCaptor.forClass(CommentQuery.class);
        verify(commentMapper).selectTopComments(captor.capture());
        assertEquals(null, captor.getValue().getVideoId());
        assertEquals(null, captor.getValue().getStatus());
    }

    @Test
    void updateCommentStatusForAdminAcceptsValidStatuses() {
        when(commentMapper.selectByCommentId(1L)).thenReturn(comment(1L));

        service.updateCommentStatusForAdmin(1L, 1);
        service.updateCommentStatusForAdmin(1L, 2);

        verify(commentMapper, times(2)).selectByCommentId(1L);
        verify(commentMapper).updateStatus(1L, 1);
        verify(commentMapper).updateStatus(1L, 2);
    }

    @Test
    void updateCommentStatusForAdminRejectsInvalidStatus() {
        assertThrows(BusinessException.class, () -> service.updateCommentStatusForAdmin(1L, 3));

        verifyNoInteractions(commentMapper);
    }

    @Test
    void updateCommentStatusForAdminRejectsMissingComment() {
        when(commentMapper.selectByCommentId(5L)).thenReturn(null);

        assertThrows(BusinessException.class, () -> service.updateCommentStatusForAdmin(5L, 1));

        verify(commentMapper, never()).updateStatus(anyLong(), anyInt());
    }

    @Test
    void deleteCommentForAdminDeletesLogically() {
        when(commentMapper.selectByCommentId(2L)).thenReturn(comment(2L));
        when(commentMapper.updateStatus(2L, 2)).thenReturn(1);

        service.deleteCommentForAdmin(2L);

        verify(commentMapper).updateStatus(2L, 2);
    }

    @Test
    void deleteCommentForAdminRejectsMissingComment() {
        when(commentMapper.selectByCommentId(3L)).thenReturn(null);

        assertThrows(BusinessException.class, () -> service.deleteCommentForAdmin(3L));

        verify(commentMapper, never()).updateStatus(anyLong(), anyInt());
    }
}
