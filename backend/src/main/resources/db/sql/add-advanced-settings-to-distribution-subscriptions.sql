-- 为分发订阅表添加高级设置字段
ALTER TABLE distribution_subscriptions ADD COLUMN filter_invalid_channels INTEGER NOT NULL DEFAULT 1;
ALTER TABLE distribution_subscriptions ADD COLUMN filter_http_high_delay INTEGER NOT NULL DEFAULT -1;
ALTER TABLE distribution_subscriptions ADD COLUMN filter_ffmpeg_high_delay INTEGER NOT NULL DEFAULT -1;
ALTER TABLE distribution_subscriptions ADD COLUMN filter_no_video_stream INTEGER NOT NULL DEFAULT 1;
ALTER TABLE distribution_subscriptions ADD COLUMN filter_no_audio_stream INTEGER NOT NULL DEFAULT 1;
ALTER TABLE distribution_subscriptions ADD COLUMN filter_low_resolution TEXT NOT NULL DEFAULT '1080p';
ALTER TABLE distribution_subscriptions ADD COLUMN merge_same_channels INTEGER NOT NULL DEFAULT 1;
