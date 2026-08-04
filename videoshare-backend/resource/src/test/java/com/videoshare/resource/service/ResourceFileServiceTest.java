package com.videoshare.resource.service;

import com.videoshare.common.utils.SnowflakeIdGenerator;
import com.videoshare.resource.dto.UploadResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.File;
import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 用临时目录作为 project.folder，验证上传文件真实落盘
 */
class ResourceFileServiceTest {

    @TempDir
    Path tempDir;

    private ResourceFileService service;

    @BeforeEach
    void setUp() {
        service = new ResourceFileService();
        ReflectionTestUtils.setField(service, "projectFolder", tempDir.toString() + File.separator);

        SnowflakeIdGenerator gen = new SnowflakeIdGenerator();
        ReflectionTestUtils.setField(gen, "workerId", 3L);
        ReflectionTestUtils.setField(gen, "datacenterId", 1L);
        gen.init();
        ReflectionTestUtils.setField(service, "snowflakeIdGenerator", gen);
    }

    @Test
    void uploadVideoWritesFileAndReturnsVideoUrl() {
        MockMultipartFile file = new MockMultipartFile("file", "clip.mp4", "video/mp4", new byte[]{1, 2, 3, 4});
        UploadResult result = service.uploadVideo(file);

        assertNotNull(result.getVideoId());
        assertTrue(result.getVideoUrl().startsWith("/video/resource/"));
        assertTrue(result.getVideoUrl().endsWith(".mp4"));
        assertEquals(0, result.getDuration());

        String fileName = result.getVideoUrl().replace("/video/resource/", "");
        File dest = new File(tempDir.toFile(), "videos" + File.separator + fileName);
        assertTrue(dest.exists());
        assertEquals(4, dest.length());
    }

    @Test
    void uploadImageWritesToUserDir() {
        MockMultipartFile file = new MockMultipartFile("file", "a.png", "image/png", new byte[]{1, 2});
        Map<String, String> result = service.uploadImage(file, "avatar", "u99");

        assertTrue(result.get("url").startsWith("/images/u99/avatar."));
        File dest = new File(tempDir.toFile(), "images" + File.separator + "u99" + File.separator + "avatar.png");
        assertTrue(dest.exists());
        assertEquals(2, dest.length());
    }
}
