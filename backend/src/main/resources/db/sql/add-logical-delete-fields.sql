-- 为所有表添加逻辑删除字段
-- 使用 0 表示未删除，1 表示已删除

-- m3u8_providers 表添加 deleted 字段
ALTER TABLE m3u8_providers ADD COLUMN deleted INTEGER NOT NULL DEFAULT 0;

-- channel_groups 表添加 deleted 字段
ALTER TABLE channel_groups ADD COLUMN deleted INTEGER NOT NULL DEFAULT 0;

-- channels 表添加 deleted 字段
ALTER TABLE channels ADD COLUMN deleted INTEGER NOT NULL DEFAULT 0;

-- epg_sources 表添加 deleted 字段
ALTER TABLE epg_sources ADD COLUMN deleted INTEGER NOT NULL DEFAULT 0;

-- m3u8_raw_data 表添加 deleted 字段
ALTER TABLE m3u8_raw_data ADD COLUMN deleted INTEGER NOT NULL DEFAULT 0;

-- 为常用查询添加索引，提升性能
CREATE INDEX IF NOT EXISTS idx_m3u8_providers_deleted ON m3u8_providers(deleted);
CREATE INDEX IF NOT EXISTS idx_channel_groups_deleted ON channel_groups(deleted);
CREATE INDEX IF NOT EXISTS idx_channels_deleted ON channels(deleted);
CREATE INDEX IF NOT EXISTS idx_epg_sources_deleted ON epg_sources(deleted);
CREATE INDEX IF NOT EXISTS idx_m3u8_raw_data_deleted ON m3u8_raw_data(deleted);
