-- 创建 M3U8 源提供者表
CREATE TABLE IF NOT EXISTS m3u8_providers (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL UNIQUE,
    type TEXT NOT NULL DEFAULT 'online',
    url TEXT,
    file_path TEXT,
    headers TEXT,
    refresh_rate INTEGER DEFAULT 3600,
    enabled BOOLEAN NOT NULL DEFAULT 1,
    description TEXT,
    created_at TEXT NOT NULL DEFAULT (datetime('now')),
    updated_at TEXT NOT NULL DEFAULT (datetime('now')),
    CHECK(type IN ('online', 'local')),
    CHECK(
        (type = 'online' AND url IS NOT NULL AND file_path IS NULL) OR
        (type = 'local' AND file_path IS NOT NULL AND url IS NULL)
    )
);

-- 创建索引
CREATE INDEX IF NOT EXISTS idx_m3u8_providers_enabled ON m3u8_providers(enabled);
CREATE INDEX IF NOT EXISTS idx_m3u8_providers_type ON m3u8_providers(type);
