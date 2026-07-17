-- Bug Fix: watch_history 建表时 video_id/user_id 均为 VARCHAR(10)，
-- 无法容纳 Snowflake ID（18-19位，用户ID/视频ID均由雪花算法生成）
-- video_id 扩到 VARCHAR(25)、user_id 扩到 VARCHAR(20) 留出安全余量
ALTER TABLE watch_history MODIFY COLUMN video_id VARCHAR(25) NOT NULL;
ALTER TABLE watch_history MODIFY COLUMN user_id  VARCHAR(20) NOT NULL;

-- 补充缺失的基础 DDL（原表可能是手动创建的，不在版本控制中）
CREATE TABLE IF NOT EXISTS watch_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id VARCHAR(20) NOT NULL,
    video_id VARCHAR(25) NOT NULL,
    watch_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_video (user_id, video_id),
    KEY idx_user_id (user_id),
    KEY idx_watch_time (watch_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
