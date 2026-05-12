-- M3U8 刷新任务历史表
CREATE TABLE IF NOT EXISTS m3u8_refresh_task (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    provider_id INTEGER NOT NULL,
    provider_name VARCHAR(255) NOT NULL,
    trigger_type VARCHAR(20) NOT NULL,  -- manual: 手动刷新, scheduled: 定时任务
    status VARCHAR(20) NOT NULL,        -- success: 成功, failed: 失败, running: 进行中
    start_time BIGINT,
    end_time BIGINT,
    duration INTEGER,                   -- 毫秒
    channel_count INTEGER,
    error_message TEXT,
    raw_content TEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    deleted INTEGER DEFAULT 0
);

-- 创建索引
CREATE INDEX IF NOT EXISTS idx_m3u8_refresh_task_provider ON m3u8_refresh_task(provider_id);
CREATE INDEX IF NOT EXISTS idx_m3u8_refresh_task_status ON m3u8_refresh_task(status);
CREATE INDEX IF NOT EXISTS idx_m3u8_refresh_task_created ON m3u8_refresh_task(created_at);
