-- Iteration 4: Video Transcoding + Creator Analytics DDL
-- Execute against the videoshare database

CREATE TABLE IF NOT EXISTS transcode_job (
    job_id       BIGINT AUTO_INCREMENT PRIMARY KEY,
    video_id     VARCHAR(20)  NOT NULL COMMENT '关联 video_info.video_id',
    status       TINYINT      DEFAULT 0 COMMENT '0=待处理 1=处理中 2=完成 3=失败',
    input_path   VARCHAR(500) NOT NULL COMMENT '原始文件路径',
    output_path  VARCHAR(500) DEFAULT '' COMMENT 'HLS输出目录路径',
    error_msg    VARCHAR(500) DEFAULT '' COMMENT '失败时的错误信息',
    create_time  DATETIME     DEFAULT CURRENT_TIMESTAMP,
    update_time  DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_video_id (video_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 数据迁移：旧 status 1（已发布）→ 新 status 2（已发布）
UPDATE video_info SET status = 2 WHERE status = 1;
