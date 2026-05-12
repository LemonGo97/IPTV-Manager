-- 移除 m3u8_refresh_task 表中的 provider_name 字段
-- 原因：订阅源名称应该通过 JOIN 查询动态获取，而不是冗余存储
-- 这样当用户修改订阅源名称时，历史任务会自动反映新名称
-- 注意：Liquibase 自动处理事务，无需手动 BEGIN/COMMIT

-- SQLite 不支持 ALTER TABLE DROP COLUMN，需要重建表

-- 1. 创建新表（不包含 provider_name）
CREATE TABLE m3u8_refresh_task_new (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    provider_id INTEGER NOT NULL,
    trigger_type VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,
    start_time BIGINT,
    end_time BIGINT,
    duration INTEGER,
    channel_count INTEGER,
    error_message TEXT,
    raw_content TEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    deleted INTEGER DEFAULT 0
);

-- 2. 复制数据（排除 provider_name 列）
INSERT INTO m3u8_refresh_task_new (
    id, provider_id, trigger_type, status,
    start_time, end_time, duration, channel_count,
    error_message, raw_content, created_at, updated_at, deleted
)
SELECT
    id, provider_id, trigger_type, status,
    start_time, end_time, duration, channel_count,
    error_message, raw_content, created_at, updated_at, deleted
FROM m3u8_refresh_task;

-- 3. 删除旧表
DROP TABLE m3u8_refresh_task;

-- 4. 重命名新表
ALTER TABLE m3u8_refresh_task_new RENAME TO m3u8_refresh_task;

-- 5. 重建索引
CREATE INDEX IF NOT EXISTS idx_m3u8_refresh_task_provider ON m3u8_refresh_task(provider_id);
CREATE INDEX IF NOT EXISTS idx_m3u8_refresh_task_status ON m3u8_refresh_task(status);
CREATE INDEX IF NOT EXISTS idx_m3u8_refresh_task_created ON m3u8_refresh_task(created_at);
