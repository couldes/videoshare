package com.videoshare.web.service.impl;

import com.videoshare.common.entity.TranscodeJob;
import com.videoshare.common.entity.VideoInfo;
import com.videoshare.web.mapper.TranscodeJobMapper;
import com.videoshare.web.mapper.VideoInfoMapper;
import com.videoshare.web.service.TranscodeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TranscodeReconcileServiceImplTest {

    @Mock private TranscodeJobMapper transcodeJobMapper;
    @Mock private VideoInfoMapper    videoInfoMapper;
    @Mock private TranscodeService   transcodeService;
    @InjectMocks private TranscodeReconcileServiceImpl service;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(service, "pendingStaleMinutes", 1);
        ReflectionTestUtils.setField(service, "processingTimeoutMinutes", 30);
        ReflectionTestUtils.setField(service, "maxRetries", 3);
    }

    private TranscodeJob job(Long id, String videoId, int status, int retryCount) {
        TranscodeJob job = new TranscodeJob();
        job.setJobId(id);
        job.setVideoId(videoId);
        job.setStatus(status);
        job.setRetryCount(retryCount);
        return job;
    }

    private VideoInfo video(String videoId, String coverUrl) {
        VideoInfo video = new VideoInfo();
        video.setVideoId(videoId);
        video.setCoverUrl(coverUrl);
        return video;
    }

    @Test
    void stalePendingRetriggeredWithCoverWhenVideoHasNoCover() {
        TranscodeJob job = job(1L, "v1", 0, 0);
        when(transcodeJobMapper.selectStalePending(1, 3)).thenReturn(Collections.singletonList(job));
        when(transcodeJobMapper.claimPending(1L, 3)).thenReturn(1);
        when(videoInfoMapper.selectByVideoId("v1")).thenReturn(video("v1", null));
        when(transcodeService.notifyTranscode("v1", true)).thenReturn(true);

        service.reconcile();

        verify(transcodeService).notifyTranscode("v1", true);
    }

    @Test
    void stalePendingRetriggeredWithoutCoverWhenVideoHasCover() {
        TranscodeJob job = job(2L, "v2", 0, 0);
        when(transcodeJobMapper.selectStalePending(1, 3)).thenReturn(Collections.singletonList(job));
        when(transcodeJobMapper.claimPending(2L, 3)).thenReturn(1);
        when(videoInfoMapper.selectByVideoId("v2")).thenReturn(video("v2", "/cover/v2.jpg"));

        service.reconcile();

        verify(transcodeService).notifyTranscode("v2", false);
    }

    @Test
    void claimRejectedSkipsRetrigger() {
        TranscodeJob job = job(3L, "v3", 0, 0);
        when(transcodeJobMapper.selectStalePending(1, 3)).thenReturn(Collections.singletonList(job));
        when(transcodeJobMapper.claimPending(3L, 3)).thenReturn(0);

        service.reconcile();

        verify(transcodeService, never()).notifyTranscode(anyString(), anyBoolean());
    }

    @Test
    void maxedPendingMarkedFailed() {
        TranscodeJob job = job(4L, "v4", 0, 3);
        when(transcodeJobMapper.selectMaxedPending(3)).thenReturn(Collections.singletonList(job));
        when(transcodeJobMapper.markFailed(eq(4L), eq(0), contains("超限"))).thenReturn(1);

        service.reconcile();

        verify(transcodeJobMapper).markFailed(eq(4L), eq(0), contains("超限"));
    }

    @Test
    void staleProcessingMarkedFailed() {
        TranscodeJob job = job(5L, "v5", 1, 0);
        when(transcodeJobMapper.selectStaleProcessing(30)).thenReturn(Collections.singletonList(job));
        when(transcodeJobMapper.markFailed(eq(5L), eq(1), contains("超时"))).thenReturn(1);

        service.reconcile();

        verify(transcodeJobMapper).markFailed(eq(5L), eq(1), contains("超时"));
    }

    @Test
    void notifyFailureKeepsJobPendingWithoutMarkingFailed() {
        TranscodeJob job = job(6L, "v6", 0, 0);
        when(transcodeJobMapper.selectStalePending(1, 3)).thenReturn(Collections.singletonList(job));
        when(transcodeJobMapper.claimPending(6L, 3)).thenReturn(1);
        when(videoInfoMapper.selectByVideoId("v6")).thenReturn(video("v6", null));
        when(transcodeService.notifyTranscode("v6", true)).thenReturn(false);

        service.reconcile();

        verify(transcodeService).notifyTranscode("v6", true);
        // 通知失败：job 保持 status=0，不置失败，交由下轮对账继续重试
        verify(transcodeJobMapper, never()).markFailed(anyLong(), anyInt(), anyString());
    }
}
