-- 创建频道清洗中间表
-- 用于数据清洗过程中的临时存储，避免清洗中断导致数据丢失
-- 每次清洗任务开始前会物理清空此表

CREATE TABLE IF NOT EXISTS channel_cleaning_temp (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  name TEXT NOT NULL,
  logo TEXT,
  url TEXT NOT NULL,
  provider_id INTEGER,
  group_id INTEGER,
  epg_source_id TEXT,
  status TEXT,
  country TEXT,
  language TEXT,
  http_detect_delay_milliseconds INTEGER,
  ffmpeg_detect_delay_milliseconds INTEGER,
  video_info TEXT,
  audio_info TEXT,
  score INTEGER,
  created_at TEXT NOT NULL DEFAULT (datetime('now')),
  updated_at TEXT NOT NULL DEFAULT (datetime('now'))
);

-- 创建索引优化查询性能
CREATE INDEX IF NOT EXISTS idx_channel_cleaning_temp_provider_id
  ON channel_cleaning_temp(provider_id);

CREATE INDEX IF NOT EXISTS idx_channel_cleaning_temp_group_id
  ON channel_cleaning_temp(group_id);
