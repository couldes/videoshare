package com.videoshare.resource.service;

import com.videoshare.common.dto.TranscodeJobInfo;
import com.videoshare.common.dto.TranscodeResultReq;
import com.videoshare.common.vo.ResponseVO;
import com.videoshare.resource.client.WebInnerApiClient;
import com.videoshare.resource.component.VideoTranscoder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TranscodeWorkerTest {

    @Mock private WebInnerApiClient webInnerApiClient;
    @Mock private VideoTranscoder  videoTranscoder;
    @InjectMocks private TranscodeWorker worker;

    @Test
    void successPathReportsStatusTwo() {
        TranscodeJobInfo job = new TranscodeJobInfo();
        job.setVideoId("v1");
        job.setInputPath("/in/v1.mp4");
        job.setOutputPath("/out/v1/");
        when(webInnerApiClient.getTranscodeJob("v1")).thenReturn(ResponseVO.success(job));
        when(videoTranscoder.transcodeToHLS("/in/v1.mp4", "/out/v1/")).thenReturn(120);

        worker.execute("v1", false);

        ArgumentCaptor<TranscodeResultReq> captor = ArgumentCaptor.forClass(TranscodeResultReq.class);
        verify(webInnerApiClient).reportResult(captor.capture());
        TranscodeResultReq req = captor.getValue();
        assertEquals(Integer.valueOf(2), req.getStatus());
        assertEquals(Integer.valueOf(120), req.getDuration());
        assertNull(req.getCoverPath());
        assertEquals("", req.getErrorMsg());
    }

    @Test
    void successWithCoverReportsCoverPath() {
        TranscodeJobInfo job = new TranscodeJobInfo();
        job.setVideoId("v2");
        job.setInputPath("/in/v2.mp4");
        job.setOutputPath("/out/v2/");
        when(webInnerApiClient.getTranscodeJob("v2")).thenReturn(ResponseVO.success(job));
        when(videoTranscoder.transcodeToHLS("/in/v2.mp4", "/out/v2/")).thenReturn(100);

        worker.execute("v2", true);

        ArgumentCaptor<TranscodeResultReq> captor = ArgumentCaptor.forClass(TranscodeResultReq.class);
        verify(webInnerApiClient).reportResult(captor.capture());
        assertEquals(Integer.valueOf(2), captor.getValue().getStatus());
        assertEquals("/hls/v2/cover.jpg", captor.getValue().getCoverPath());
        verify(videoTranscoder).generateThumbnail("/in/v2.mp4", "/out/v2/cover.jpg", 50);
    }

    @Test
    void failurePathReportsStatusThree() {
        TranscodeJobInfo job = new TranscodeJobInfo();
        job.setVideoId("v3");
        job.setInputPath("/in/v3.mp4");
        job.setOutputPath("/out/v3/");
        when(webInnerApiClient.getTranscodeJob("v3")).thenReturn(ResponseVO.success(job));
        when(videoTranscoder.transcodeToHLS(anyString(), anyString())).thenThrow(new RuntimeException("boom"));

        worker.execute("v3", false);

        ArgumentCaptor<TranscodeResultReq> captor = ArgumentCaptor.forClass(TranscodeResultReq.class);
        verify(webInnerApiClient).reportResult(captor.capture());
        TranscodeResultReq req = captor.getValue();
        assertEquals(Integer.valueOf(3), req.getStatus());
        assertTrue(req.getErrorMsg().contains("boom"));
    }

    @Test
    void jobMissingReportsError() {
        when(webInnerApiClient.getTranscodeJob("v4")).thenReturn(ResponseVO.error("转码任务不存在: v4"));

        worker.execute("v4", false);

        ArgumentCaptor<TranscodeResultReq> captor = ArgumentCaptor.forClass(TranscodeResultReq.class);
        verify(webInnerApiClient).reportResult(captor.capture());
        assertEquals(Integer.valueOf(3), captor.getValue().getStatus());
    }

    @Test
    void feignExceptionIsTreatedAsJobMissing() {
        when(webInnerApiClient.getTranscodeJob("v5")).thenThrow(new RuntimeException("web down"));

        worker.execute("v5", false);

        ArgumentCaptor<TranscodeResultReq> captor = ArgumentCaptor.forClass(TranscodeResultReq.class);
        verify(webInnerApiClient).reportResult(captor.capture());
        assertEquals(Integer.valueOf(3), captor.getValue().getStatus());
    }
}
