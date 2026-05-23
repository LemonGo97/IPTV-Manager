-- 为 epg_sources 表添加 deleted 字段以支持逻辑删除
-- 注意：SQLite 不支持 IF NOT EXISTS 语法用于 ALTER TABLE ADD COLUMN
-- 如果列已存在，此脚本会报错，可以忽略

-- 添加 deleted 字段（默认值为 0 表示未删除）
ALTER TABLE epg_sources ADD COLUMN deleted BOOLEAN NOT NULL DEFAULT 0;

-- 创建索引以提高查询性能
CREATE INDEX IF NOT EXISTS idx_epg_sources_deleted ON epg_sources(deleted);
