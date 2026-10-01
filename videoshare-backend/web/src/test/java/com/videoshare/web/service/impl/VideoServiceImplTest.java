package com.videoshare.web.service.impl;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class VideoServiceImplTest {

    @Test
    void buildPathHandlesTrailingSlashCorrectly() {
        // Simulate the buildPath logic from VideoServiceImpl
        String projectFolder = "d:/webser/videoshare/";
        
        // Build videos path
        StringBuilder videosPath = new StringBuilder();
        boolean first = true;
        for (String segment : new String[]{projectFolder, "videos"}) {
            if (segment == null || segment.isEmpty()) continue;
            if (!first && !videosPath.toString().endsWith("/") && !videosPath.toString().endsWith("\\")) {
                videosPath.append("/");
            }
            videosPath.append(segment);
            first = false;
        }
        
        assertEquals("d:/webser/videoshare/videos", videosPath.toString());
    }

    @Test
    void buildPathHandlesNoTrailingSlashCorrectly() {
        String projectFolder = "d:/webser/videoshare";
        
        StringBuilder videosPath = new StringBuilder();
        boolean first = true;
        for (String segment : new String[]{projectFolder, "videos"}) {
            if (segment == null || segment.isEmpty()) continue;
            if (!first && !videosPath.toString().endsWith("/") && !videosPath.toString().endsWith("\\")) {
                videosPath.append("/");
            }
            videosPath.append(segment);
            first = false;
        }
        
        assertEquals("d:/webser/videoshare/videos", videosPath.toString());
    }

    @Test
    void buildPathHandlesMultipleSegments() {
        String projectFolder = "d:/webser/videoshare/";
        
        StringBuilder hlsPath = new StringBuilder();
        boolean first = true;
        for (String segment : new String[]{projectFolder, "hls"}) {
            if (segment == null || segment.isEmpty()) continue;
            if (!first && !hlsPath.toString().endsWith("/") && !hlsPath.toString().endsWith("\\")) {
                hlsPath.append("/");
            }
            hlsPath.append(segment);
            first = false;
        }
        
        assertEquals("d:/webser/videoshare/hls", hlsPath.toString());
    }
}
