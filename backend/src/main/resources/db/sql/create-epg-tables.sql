DROP TABLE IF EXISTS epg_sources;
DROP TABLE IF EXISTS epg_programs;


-- 创建 EPG 源表
CREATE TABLE IF NOT EXISTS epg_sources
(
    id          INTEGER PRIMARY KEY AUTOINCREMENT,
    name        TEXT    NOT NULL UNIQUE,
    url         TEXT    NOT NULL,
    type        TEXT    NOT NULL DEFAULT 'xml',
    enabled     BOOLEAN NOT NULL DEFAULT 1,
    description TEXT,
    created_at  TEXT    NOT NULL DEFAULT (datetime('now')),
    updated_at  TEXT    NOT NULL DEFAULT (datetime('now')),
    deleted       BOOLEAN NOT NULL DEFAULT 0
);

-- 创建索引
CREATE INDEX IF NOT EXISTS idx_epg_sources_enabled ON epg_sources (enabled);


-- 创建 EPG 节目表
CREATE TABLE IF NOT EXISTS epg_channels
(
    id            INTEGER PRIMARY KEY AUTOINCREMENT,
    epg_source_id INTEGER NOT NULL,
    channel_id  TEXT    NOT NULL,
    display_name  TEXT    NOT NULL,
    icon          TEXT    NOT NULL,
    created_at    TEXT    NOT NULL DEFAULT (datetime('now', 'localtime')),
    updated_at    TEXT    NOT NULL DEFAULT (datetime('now', 'localtime')),
    deleted       BOOLEAN NOT NULL DEFAULT 0,
    FOREIGN KEY (epg_source_id) REFERENCES epg_sources (id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_epg_channels_source_id ON epg_channels (epg_source_id);

-- 创建 EPG 节目表
CREATE TABLE IF NOT EXISTS epg_programs
(
    id            INTEGER PRIMARY KEY AUTOINCREMENT,
    epg_source_id INTEGER NOT NULL,
    channel_id    TEXT,             -- 关联频道ID (可选)
    start_time    TEXT    NOT NULL, -- 节目开始时间 (ISO 8601)
    stop_time     TEXT    NOT NULL, -- 节目结束时间 (ISO 8601)
    title         TEXT    NOT NULL, -- 节目标题
    sub_title     TEXT,             -- 节目副标题
    description   TEXT,             -- 节目描述
    created_at    TEXT    NOT NULL DEFAULT (datetime('now', 'localtime')),
    updated_at    TEXT    NOT NULL DEFAULT (datetime('now', 'localtime')),
    deleted       BOOLEAN NOT NULL DEFAULT 0,
    FOREIGN KEY (epg_source_id) REFERENCES epg_sources (id) ON DELETE CASCADE
);

-- 创建索引
CREATE INDEX IF NOT EXISTS idx_epg_programs_source_id ON epg_programs (epg_source_id);
CREATE INDEX IF NOT EXISTS idx_epg_programs_start_time ON epg_programs (start_time);
CREATE INDEX IF NOT EXISTS idx_epg_programs_deleted ON epg_programs (deleted);
