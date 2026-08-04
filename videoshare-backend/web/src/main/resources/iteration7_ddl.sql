-- Iteration 7: 转码任务对账
-- Execute against the recordvideo database

-- 对账重试计数：transcode_job 增加 retry_count 列
-- 注意：MySQL 8.0 不支持 ADD COLUMN IF NOT EXISTS，执行前确认列不存在
ALTER TABLE transcode_job
    ADD COLUMN retry_count INT NOT NULL DEFAULT 0 COMMENT '对账重试次数' AFTER error_msg;
