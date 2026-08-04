package com.videoshare.resource.service;

import com.videoshare.common.exception.BusinessException;
import com.videoshare.common.utils.SnowflakeIdGenerator;
import com.videoshare.resource.dto.UploadResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Service
public class ResourceFileService {

    @Value("${project.folder:d:/webser/videoshare/}")
    private String projectFolder;

    @Resource
    private SnowflakeIdGenerator snowflakeIdGenerator;

    /** 视频上传：雪花 ID 命名，落盘到 videos/，返回 videoUrl 占位 */
    public UploadResult uploadVideo(MultipartFile file) {
        String originalName = file.getOriginalFilename();
        String ext = originalName != null && originalName.contains(".")
                ? originalName.substring(originalName.lastIndexOf("."))
                : ".mp4";

        String videoId = snowflakeIdGenerator.nextIdString();
        String fileName = videoId + ext;
        File dest = new File(getVideoDir() + fileName);
        dest.getParentFile().mkdirs();

        try {
            file.transferTo(dest);
        } catch (IOException e) {
            throw new BusinessException("视频上传失败，请重试");
        }

        UploadResult result = new UploadResult();
        result.setVideoId(videoId);
        result.setVideoUrl("/video/resource/" + fileName);
        result.setDuration(0);
        return result;
    }

    /** 头像/背景上传：落盘到 images/{userId}/{type}.{ext} */
    public Map<String, String> uploadImage(MultipartFile file, String type, String userId) {
        String ext = "jpg";
        String originalName = file.getOriginalFilename();
        if (originalName != null && originalName.contains(".")) {
            ext = originalName.substring(originalName.lastIndexOf(".") + 1);
        }

        String dir = projectFolder.endsWith("/") || projectFolder.endsWith("\\")
                ? projectFolder + "images/" + userId + "/"
                : projectFolder + "/images/" + userId + "/";
        String fileName = type + "." + ext;
        File dest = new File(dir + fileName);
        dest.getParentFile().mkdirs();

        try {
            file.transferTo(dest);
        } catch (IOException e) {
            throw new BusinessException("上传失败");
        }

        String url = "/images/" + userId + "/" + fileName + "?t=" + System.currentTimeMillis();
        Map<String, String> result = new HashMap<>();
        result.put("url", url);
        return result;
    }

    private String getVideoDir() {
        return projectFolder.endsWith("/") || projectFolder.endsWith("\\")
                ? projectFolder + "videos/"
                : projectFolder + "/videos/";
    }
}
