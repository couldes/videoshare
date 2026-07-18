-- Iteration 6: 修复数据库排序规则冲突
-- Execute against the videoshare database
--
-- 错误说明：JOIN 操作时发现 user_follow 和 user_info 两表使用了不同的排序规则
-- （utf8mb4_general_ci vs utf8mb4_0900_ai_ci），导致 "Illegal mix of collations"
--
-- 修复方法：统一两表的字符集和排序规则
-- 先用以下 SQL 查看当前各表的排序规则：

-- SELECT TABLE_NAME, TABLE_COLLATION
-- FROM information_schema.TABLES
-- WHERE TABLE_SCHEMA = 'videoshare' AND TABLE_NAME IN ('user_info', 'user_follow');

-- 然后将两个表统一为相同的排序规则（选择其中一种即可）：

ALTER TABLE user_follow CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
ALTER TABLE user_info CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;

-- Bug Fix: user_info 建表时 user_id 为 VARCHAR(10)，
-- 无法容纳 Snowflake ID（18-19位）
-- 扩到 VARCHAR(20) 留出安全余量，与 watch_history 修复保持一致
ALTER TABLE user_info MODIFY COLUMN user_id VARCHAR(20) NOT NULL;
