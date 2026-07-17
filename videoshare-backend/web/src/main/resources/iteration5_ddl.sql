-- Iteration 5: 缺陷修复 & 功能增强 DDL
-- Execute against the videoshare database

-- 新增驳回原因字段（管理员审核功能）
ALTER TABLE video_info
    ADD COLUMN IF NOT EXISTS remark VARCHAR(500) DEFAULT '' COMMENT '驳回原因';

-- 新增头像和背景图字段（用户已存在这些列，此处仅做幂等确认）
ALTER TABLE user_info
    ADD COLUMN IF NOT EXISTS avatar_url VARCHAR(200) DEFAULT '' COMMENT '头像URL',
    ADD COLUMN IF NOT EXISTS background VARCHAR(200) DEFAULT '' COMMENT '频道背景图URL';

-- 修复 background 列类型（旧表可能为 INT，IF NOT EXISTS 无法覆盖）
ALTER TABLE user_info
    MODIFY COLUMN background VARCHAR(200) DEFAULT '' COMMENT '频道背景图URL';

-- 数据迁移：修复被转码 bug 错误设置为 status=2（已下架）的视频
-- 该 bug 导致发布后转码完成时将状态设为 2，而非保持 0（待审核）
-- 影响范围：status=2 且未经过管理员操作（如已有 remark 的不重置）
-- 如需执行，取消注释并按实际情况调整 create_time 范围：
-- UPDATE video_info SET status = 0
-- WHERE status = 2
--   AND create_time >= '2025-01-01 00:00:00'
--   AND (remark IS NULL OR remark = '');
