-- 删除旧表并创建新的频道表
DROP TABLE IF EXISTS channels;

CREATE TABLE channels (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    logo TEXT,
    url TEXT NOT NULL,
    number TEXT,
    channel_id TEXT,
    enabled BOOLEAN NOT NULL DEFAULT 1,
    provider_id INTEGER,
    group_id INTEGER,
    epg_source_id INTEGER,
    sort_order INTEGER DEFAULT 0,
    description TEXT,
    created_at TEXT NOT NULL DEFAULT (datetime('now')),
    updated_at TEXT NOT NULL DEFAULT (datetime('now')),
    FOREIGN KEY (provider_id) REFERENCES m3u8_providers(id) ON DELETE SET NULL,
    FOREIGN KEY (group_id) REFERENCES channel_groups(id) ON DELETE SET NULL,
    FOREIGN KEY (epg_source_id) REFERENCES epg_sources(id) ON DELETE SET NULL
);

-- 创建索引
CREATE INDEX IF NOT EXISTS idx_channels_provider_id ON channels(provider_id);
CREATE INDEX IF NOT EXISTS idx_channels_group_id ON channels(group_id);
CREATE INDEX IF NOT EXISTS idx_channels_epg_source_id ON channels(epg_source_id);
CREATE INDEX IF NOT EXISTS idx_channels_enabled ON channels(enabled);
CREATE INDEX IF NOT EXISTS idx_channels_number ON channels(number);
CREATE INDEX IF NOT EXISTS idx_channels_sort_order ON channels(sort_order);
