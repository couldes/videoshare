package com.videoshare.resource.controller;

import com.videoshare.resource.dto.UploadResult;
import com.videoshare.resource.service.ResourceFileService;
import com.videoshare.resource.service.TranscodeWorker;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ResourceController.class)
class ResourceControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockBean private ResourceFileService resourceFileService;
    @MockBean private TranscodeWorker transcodeWorker;

    @Test
    void uploadVideoReturnsVideoUrl() throws Exception {
        UploadResult result = new UploadResult();
        result.setVideoId("v123");
        result.setVideoUrl("/video/resource/v123.mp4");
        result.setDuration(0);
        when(resourceFileService.uploadVideo(any())).thenReturn(result);

        MockMultipartFile file = new MockMultipartFile("file", "a.mp4", "video/mp4", new byte[]{1, 2, 3});
        mockMvc.perform(multipart("/resource/upload").file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.videoUrl").value("/video/resource/v123.mp4"));

        verify(resourceFileService).uploadVideo(any());
    }

    @Test
    void uploadImageWithoutUserIdReturnsError() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "a.jpg", "image/jpeg", new byte[]{1, 2, 3});
        mockMvc.perform(multipart("/resource/uploadImage").file(file).param("type", "avatar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("error"));
    }

    @Test
    void uploadImageWithInvalidTypeReturnsError() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "a.jpg", "image/jpeg", new byte[]{1, 2, 3});
        mockMvc.perform(multipart("/resource/uploadImage").file(file).param("type", "banner").header("X-User-Id", "u1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("error"));
    }

    @Test
    void uploadImageWithValidUserReturnsUrl() throws Exception {
        Map<String, String> urlMap = new HashMap<>();
        urlMap.put("url", "/images/u1/avatar.jpg?t=123");
        when(resourceFileService.uploadImage(any(), eq("avatar"), eq("u1"))).thenReturn(urlMap);

        MockMultipartFile file = new MockMultipartFile("file", "a.jpg", "image/jpeg", new byte[]{1, 2, 3});
        mockMvc.perform(multipart("/resource/uploadImage").file(file).param("type", "avatar").header("X-User-Id", "u1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.url").value("/images/u1/avatar.jpg?t=123"));

        verify(resourceFileService).uploadImage(any(), eq("avatar"), eq("u1"));
    }
}
