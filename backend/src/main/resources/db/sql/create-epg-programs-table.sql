-- 创建 EPG 节目表
CREATE TABLE IF NOT EXISTS epg_programs (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    epg_source_id INTEGER NOT NULL,
    channel_id TEXT,                    -- 关联频道ID (可选)
    channel_name TEXT NOT NULL,         -- 频道名称 (从EPG获取)
    start_time TEXT NOT NULL,           -- 节目开始时间 (ISO 8601)
    stop_time TEXT NOT NULL,            -- 节目结束时间 (ISO 8601)
    title TEXT NOT NULL,                -- 节目标题
    sub_title TEXT,                     -- 节目副标题
    description TEXT,                   -- 节目描述
    categories TEXT,                    -- 节目分类 (JSON数组字符串)
    icons TEXT,                         -- 节目图标 (JSON数组字符串)
    credits TEXT,                       -- 演职人员 (JSON对象字符串)
    ratings TEXT,                       -- 评分信息 (JSON数组字符串)
    created_at TEXT NOT NULL DEFAULT (datetime('now', 'localtime')),
    updated_at TEXT NOT NULL DEFAULT (datetime('now', 'localtime')),
    deleted BOOLEAN NOT NULL DEFAULT 0,
    FOREIGN KEY (epg_source_id) REFERENCES epg_sources(id) ON DELETE CASCADE
);

-- 创建索引
CREATE INDEX IF NOT EXISTS idx_epg_programs_source_id ON epg_programs(epg_source_id);
CREATE INDEX IF NOT EXISTS idx_epg_programs_channel_name ON epg_programs(channel_name);
CREATE INDEX IF NOT EXISTS idx_epg_programs_start_time ON epg_programs(start_time);
CREATE INDEX IF NOT EXISTS idx_epg_programs_deleted ON epg_programs(deleted);

-- 创建复合索引用于查询指定频道的节目
CREATE INDEX IF NOT EXISTS idx_epg_programs_source_channel ON epg_programs(epg_source_id, channel_name);
