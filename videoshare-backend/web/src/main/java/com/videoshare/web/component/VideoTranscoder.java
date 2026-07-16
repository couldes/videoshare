package com.videoshare.web.component;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.InputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * ffmpeg 命令行封装：HLS 转码、封面截图、时长检测
 */
@Component
public class VideoTranscoder {

    private static final Logger log = LoggerFactory.getLogger(VideoTranscoder.class);
    private static final long TIMEOUT_SECONDS = 600;

    private final ExecutorService streamDrainer = Executors.newCachedThreadPool();

    /**
     * 将原始视频转为 HLS，生成 index.m3u8 + .ts 分片
     * @return 视频时长（秒）
     */
    public int transcodeToHLS(String inputPath, String outputDir) {
        new File(outputDir).mkdirs();
        String outputPath = outputDir + "/index.m3u8";
        String segmentPath = outputDir + "/segment_%03d.ts";
        try {
            ProcessBuilder pb = new ProcessBuilder(
                    "ffmpeg",
                    "-i", inputPath,
                    "-c:v", "libx264",
                    "-c:a", "aac",
                    "-hls_time", "10",
                    "-hls_list_size", "0",
                    "-hls_segment_filename", segmentPath,
                    "-y",
                    outputPath
            );
            Process process = startAndDrain(pb);
            boolean finished = process.waitFor(TIMEOUT_SECONDS, TimeUnit.SECONDS);
            if (!finished) {
                process.destroyForcibly();
                throw new RuntimeException("ffmpeg HLS transcode timed out after " + TIMEOUT_SECONDS + "s");
            }
            if (process.exitValue() != 0) {
                throw new RuntimeException("ffmpeg HLS transcode failed with exit code " + process.exitValue());
            }
            log.info("HLS transcode complete: {}", outputPath);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("ffmpeg HLS transcode error: " + e.getMessage(), e);
        }
        return detectDuration(inputPath);
    }

    /**
     * 从视频指定时间点截取一帧作为封面
     */
    public void generateThumbnail(String inputPath, String outputPath, int timeOffset) {
        new File(outputPath).getParentFile().mkdirs();
        try {
            ProcessBuilder pb = new ProcessBuilder(
                    "ffmpeg",
                    "-ss", String.valueOf(timeOffset),
                    "-i", inputPath,
                    "-vframes", "1",
                    "-q:v", "2",
                    "-y",
                    outputPath
            );
            Process process = startAndDrain(pb);
            boolean finished = process.waitFor(TIMEOUT_SECONDS, TimeUnit.SECONDS);
            if (!finished) {
                process.destroyForcibly();
                throw new RuntimeException("ffmpeg thumbnail timed out");
            }
            if (process.exitValue() != 0) {
                throw new RuntimeException("ffmpeg thumbnail failed with exit code " + process.exitValue());
            }
            log.info("Thumbnail generated: {}", outputPath);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("ffmpeg thumbnail error: " + e.getMessage(), e);
        }
    }

    /**
     * 用 ffprobe 检测视频实际时长（秒）
     */
    public int detectDuration(String inputPath) {
        try {
            ProcessBuilder pb = new ProcessBuilder(
                    "ffprobe",
                    "-v", "error",
                    "-show_entries", "format=duration",
                    "-of", "csv=p=0",
                    inputPath
            );
            Process process = pb.start();
            // ffprobe output is tiny, no need for async draining
            boolean finished = process.waitFor(TIMEOUT_SECONDS, TimeUnit.SECONDS);
            if (!finished) {
                process.destroyForcibly();
                throw new RuntimeException("ffprobe timed out");
            }
            String output = readAll(process.getInputStream()).trim();
            if (process.exitValue() != 0 || output.isEmpty()) {
                throw new RuntimeException("ffprobe failed: " + output);
            }
            double seconds = Double.parseDouble(output);
            return (int) Math.round(seconds);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("ffprobe duration error: " + e.getMessage(), e);
        }
    }

    /** Start process and drain its stdout/stderr in background to prevent buffer deadlock. */
    private Process startAndDrain(ProcessBuilder pb) throws Exception {
        pb.redirectErrorStream(true);
        Process process = pb.start();
        InputStream stream = process.getInputStream();
        streamDrainer.submit(() -> {
            byte[] buf = new byte[8192];
            try {
                while (stream.read(buf) != -1) { /* drain */ }
            } catch (Exception ignored) { }
        });
        return process;
    }

    private String readAll(InputStream stream) throws Exception {
        StringBuilder sb = new StringBuilder();
        byte[] buf = new byte[8192];
        int n;
        while ((n = stream.read(buf)) != -1) {
            sb.append(new String(buf, 0, n));
        }
        return sb.toString();
    }
}
