-- 创建 EPG 源表
CREATE TABLE IF NOT EXISTS epg_sources (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL UNIQUE,
    url TEXT NOT NULL,
    type TEXT NOT NULL DEFAULT 'xml',
    enabled BOOLEAN NOT NULL DEFAULT 1,
    description TEXT,
    created_at TEXT NOT NULL DEFAULT (datetime('now')),
    updated_at TEXT NOT NULL DEFAULT (datetime('now'))
);

-- 创建索引
CREATE INDEX IF NOT EXISTS idx_epg_sources_enabled ON epg_sources(enabled);
