package com.videoshare.admin.service.impl;

import com.videoshare.admin.mapper.AdminUserMapper;
import com.videoshare.admin.mapper.AdminVideoMapper;
import com.videoshare.admin.mapper.CommentMapper;
import com.videoshare.admin.mapper.NotificationMapper;
import com.videoshare.admin.mapper.PlaylistVideoMapper;
import com.videoshare.admin.mapper.TranscodeJobMapper;
import com.videoshare.admin.mapper.UserActionMapper;
import com.videoshare.admin.mapper.WatchHistoryMapper;
import com.videoshare.common.entity.VideoInfo;
import com.videoshare.common.exception.BusinessException;
import com.videoshare.common.search.VideoSearchService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminVideoServiceImplTest {

    @Mock private AdminVideoMapper    adminVideoMapper;
    @Mock private AdminUserMapper     adminUserMapper;
    @Mock private CommentMapper       commentMapper;
    @Mock private UserActionMapper    userActionMapper;
    @Mock private WatchHistoryMapper  watchHistoryMapper;
    @Mock private PlaylistVideoMapper playlistVideoMapper;
    @Mock private NotificationMapper  notificationMapper;
    @Mock private TranscodeJobMapper  transcodeJobMapper;
    @Mock private VideoSearchService  videoSearchService;

    @InjectMocks private AdminVideoServiceImpl service;

    private VideoInfo video(String videoId) {
        VideoInfo v = new VideoInfo();
        v.setVideoId(videoId);
        return v;
    }

    @Test
    void deleteVideoWithRelationsCleansAllInOrder() {
        when(adminVideoMapper.selectByVideoId("v1")).thenReturn(video("v1"));
        when(adminVideoMapper.deleteByVideoId("v1")).thenReturn(1);

        service.deleteVideo("v1");

        InOrder order = inOrder(userActionMapper, commentMapper, watchHistoryMapper,
                playlistVideoMapper, notificationMapper, transcodeJobMapper, adminVideoMapper);
        order.verify(userActionMapper).deleteCommentLikes("v1");
        order.verify(userActionMapper).deleteVideoLikes("v1");
        order.verify(commentMapper).deleteByVideoId("v1");
        order.verify(watchHistoryMapper).deleteByVideoId("v1");
        order.verify(playlistVideoMapper).deleteByVideoId("v1");
        order.verify(notificationMapper).deleteByVideoId("v1");
        order.verify(transcodeJobMapper).deleteByVideoId("v1");
        order.verify(adminVideoMapper).deleteByVideoId("v1");
        verify(videoSearchService).deleteById("v1");
    }

    @Test
    void deleteMissingVideoThrowsAndSkipsCascades() {
        when(adminVideoMapper.selectByVideoId("v2")).thenReturn(null);

        assertThrows(BusinessException.class, () -> service.deleteVideo("v2"));

        verify(adminVideoMapper, never()).deleteByVideoId(anyString());
        verifyNoInteractions(commentMapper, userActionMapper, watchHistoryMapper,
                playlistVideoMapper, notificationMapper, transcodeJobMapper, videoSearchService);
    }

    @Test
    void cascadeFailurePropagatesBeforeMainDelete() {
        when(adminVideoMapper.selectByVideoId("v3")).thenReturn(video("v3"));
        when(commentMapper.deleteByVideoId("v3")).thenThrow(new RuntimeException("db down"));

        assertThrows(RuntimeException.class, () -> service.deleteVideo("v3"));

        verify(adminVideoMapper, never()).deleteByVideoId(anyString());
        verify(videoSearchService, never()).deleteById(anyString());
    }

    @Test
    void deleteVideoIsTransactional() throws NoSuchMethodException {
        assertTrue(service.getClass()
                .getMethod("deleteVideo", String.class)
                .isAnnotationPresent(Transactional.class));
    }
}
