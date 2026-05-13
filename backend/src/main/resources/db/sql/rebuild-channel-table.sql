-- 移除 m3u8_refresh_task 表中的 provider_name 字段
-- 原因：订阅源名称应该通过 JOIN 查询动态获取，而不是冗余存储
-- 这样当用户修改订阅源名称时，历史任务会自动反映新名称
-- 注意：Liquibase 自动处理事务，无需手动 BEGIN/COMMIT

-- SQLite 不支持 ALTER TABLE DROP COLUMN，需要重建表

-- 1. 创建新表（不包含 provider_name）

DROP TABLE channels;

CREATE TABLE channels (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  name TEXT NOT NULL,
  logo TEXT,
  url TEXT NOT NULL,
  group_id INTEGER,
  epg_source_id TEXT,
  status TEXT,
  country TEXT,
  language TEXT,
  score INTEGER,
  created_at TEXT NOT NULL DEFAULT (datetime('now')),
  updated_at TEXT NOT NULL DEFAULT (datetime('now')),
  deleted INTEGER DEFAULT 0
);

-- 创建索引
CREATE INDEX IF NOT EXISTS idx_channels_group_id ON channels(group_id);
CREATE INDEX IF NOT EXISTS idx_channels_epg_source_id ON channels(epg_source_id);
