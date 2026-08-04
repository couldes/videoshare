package com.videoshare.web.service.impl;

import com.videoshare.common.entity.TranscodeJob;
import com.videoshare.common.vo.ResponseVO;
import com.videoshare.web.client.ResourceTranscodeClient;
import com.videoshare.web.mapper.TranscodeJobMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TranscodeServiceImplTest {

    @Mock private TranscodeJobMapper     transcodeJobMapper;
    @Mock private ResourceTranscodeClient resourceTranscodeClient;
    @InjectMocks private TranscodeServiceImpl service;

    private TranscodeJob job(Long id, String videoId) {
        TranscodeJob job = new TranscodeJob();
        job.setJobId(id);
        job.setVideoId(videoId);
        return job;
    }

    @Test
    void notifySuccessReturnsTrueAndMarksJobInProgress() {
        when(transcodeJobMapper.selectByVideoId("v1")).thenReturn(job(1L, "v1"));
        when(resourceTranscodeClient.notifyTranscode(any())).thenReturn(ResponseVO.success());
        when(transcodeJobMapper.updateStatus(1L, 1, "")).thenReturn(1);

        boolean ok = service.notifyTranscode("v1", true);

        assertTrue(ok);
        verify(transcodeJobMapper).updateStatus(1L, 1, "");
    }

    @Test
    void clientExceptionReturnsFalse() {
        when(transcodeJobMapper.selectByVideoId("v2")).thenReturn(job(2L, "v2"));
        when(resourceTranscodeClient.notifyTranscode(any())).thenThrow(new RuntimeException("resource down"));

        boolean ok = service.notifyTranscode("v2", false);

        assertFalse(ok);
    }

    @Test
    void jobMissingReturnsFalse() {
        when(transcodeJobMapper.selectByVideoId("v3")).thenReturn(null);

        boolean ok = service.notifyTranscode("v3", true);

        assertFalse(ok);
    }
}
