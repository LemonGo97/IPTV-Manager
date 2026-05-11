-- M3U8 原始数据历史表
-- 用于存储每次获取的 M3U8 原始内容，保留最近3次数据用于备份和调试

CREATE TABLE IF NOT EXISTS m3u8_raw_data (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    provider_id INTEGER NOT NULL,
    content TEXT NOT NULL,
    fetched_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (provider_id) REFERENCES m3u8_providers(id) ON DELETE CASCADE
);

-- 索引：按 provider_id 和时间降序查询，便于快速获取最近的数据
CREATE INDEX IF NOT EXISTS idx_m3u8_raw_data_provider_fetched
    ON m3u8_raw_data(provider_id, fetched_at DESC);

-- 索引：用于清理旧数据
CREATE INDEX IF NOT EXISTS idx_m3u8_raw_data_fetched_at
    ON m3u8_raw_data(fetched_at DESC);
