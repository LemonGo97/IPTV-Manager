-- M3U8 原始频道表
CREATE TABLE IF NOT EXISTS original_channels (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    provider_id INTEGER NOT NULL,
    name TEXT NOT NULL,
    url TEXT NOT NULL,
    tvg_id TEXT,
    tvg_name TEXT,
    tvg_logo TEXT,
    group_title TEXT,
    tvg_country TEXT,
    tvg_language TEXT,
    task_id INTEGER,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    deleted INTEGER DEFAULT 0
);

-- 创建索引
CREATE INDEX IF NOT EXISTS idx_original_channels_provider ON original_channels(provider_id);
CREATE INDEX IF NOT EXISTS idx_original_channels_created ON original_channels(created_at);
