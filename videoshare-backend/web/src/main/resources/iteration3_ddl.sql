-- Iteration 3: Playlists + Notifications DDL
-- Execute against the videoshare database

CREATE TABLE IF NOT EXISTS playlist (
    playlist_id  BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id      VARCHAR(20)  NOT NULL COMMENT '创建者用户ID',
    title        VARCHAR(100) NOT NULL COMMENT '播放列表标题',
    description  VARCHAR(500) DEFAULT '' COMMENT '描述',
    cover_url    VARCHAR(200) DEFAULT '' COMMENT '封面URL（自动取第一个视频封面）',
    is_private   TINYINT(1)   DEFAULT 0 COMMENT '0=公开 1=私密',
    create_time  DATETIME     DEFAULT CURRENT_TIMESTAMP,
    update_time  DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS playlist_video (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    playlist_id BIGINT       NOT NULL COMMENT '播放列表ID',
    video_id    VARCHAR(20)  NOT NULL COMMENT '视频ID',
    sort_order  INT          DEFAULT 0 COMMENT '排序序号（升序）',
    create_time DATETIME     DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_playlist_video (playlist_id, video_id),
    INDEX idx_playlist_id (playlist_id),
    INDEX idx_video_id (video_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS notification (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id      VARCHAR(20)  NOT NULL COMMENT '接收通知的用户ID',
    from_user_id VARCHAR(20)  NOT NULL COMMENT '触发通知的用户ID',
    type         VARCHAR(20)  NOT NULL COMMENT '通知类型：like/comment/reply/follow',
    video_id     VARCHAR(20)  DEFAULT NULL COMMENT '关联视频ID（like/comment/reply 时填写）',
    content      VARCHAR(200) DEFAULT '' COMMENT '通知摘要文本',
    is_read      TINYINT(1)   DEFAULT 0 COMMENT '0=未读 1=已读',
    create_time  DATETIME     DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_id_time (user_id, create_time DESC),
    INDEX idx_user_id_unread (user_id, is_read)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
