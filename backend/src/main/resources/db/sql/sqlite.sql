DROP TABLE IF EXISTS iptv_providers;

-- 创建 M3U8 源提供者表
CREATE TABLE IF NOT EXISTS iptv_providers
(
    id           INTEGER PRIMARY KEY AUTOINCREMENT,
    name         TEXT    NOT NULL UNIQUE,
    type         TEXT    NOT NULL DEFAULT 'online',
    content_type TEXT             DEFAULT 'M3U8',
    url          TEXT,
    filename     TEXT,
    headers      TEXT,
    refresh_rate INTEGER          DEFAULT 3600,
    enabled      BOOLEAN NOT NULL DEFAULT 1,
    description  TEXT,
    created_at   TEXT    NOT NULL DEFAULT (datetime('now')),
    updated_at   TEXT    NOT NULL DEFAULT (datetime('now')),
    deleted      BOOLEAN NOT NULL DEFAULT 0
);

-- 创建索引
CREATE INDEX IF NOT EXISTS idx_iptv_providers_enabled ON iptv_providers (enabled);
CREATE INDEX IF NOT EXISTS idx_iptv_providers_type ON iptv_providers (type);
CREATE INDEX IF NOT EXISTS idx_iptv_providers_deleted ON iptv_providers (deleted);

-- 创建频道组表
CREATE TABLE IF NOT EXISTS channel_groups
(
    id          INTEGER PRIMARY KEY AUTOINCREMENT,
    name        TEXT    NOT NULL UNIQUE,
    sort_order  INTEGER          DEFAULT 0,
    description TEXT,
    created_at  TEXT    NOT NULL DEFAULT (datetime('now')),
    updated_at  TEXT    NOT NULL DEFAULT (datetime('now')),
    deleted     BOOLEAN NOT NULL DEFAULT 0
);

-- 创建索引
CREATE INDEX IF NOT EXISTS idx_channel_groups_deleted ON channel_groups (deleted);
CREATE INDEX IF NOT EXISTS idx_channel_groups_sort_order ON channel_groups (sort_order);

DROP TABLE IF EXISTS epg_providers;
-- 创建 EPG 源表
CREATE TABLE IF NOT EXISTS epg_providers
(
    id          INTEGER PRIMARY KEY AUTOINCREMENT,
    name        TEXT    NOT NULL UNIQUE,
    url         TEXT    NOT NULL,
    type        TEXT    NOT NULL DEFAULT 'xml',
    enabled     BOOLEAN NOT NULL DEFAULT 1,
    description TEXT,
    created_at  TEXT    NOT NULL DEFAULT (datetime('now')),
    updated_at  TEXT    NOT NULL DEFAULT (datetime('now')),
    deleted     BOOLEAN NOT NULL DEFAULT 0
);

-- 创建索引
CREATE INDEX IF NOT EXISTS idx_epg_providers_enabled ON epg_providers (enabled);
CREATE INDEX IF NOT EXISTS idx_epg_providers_deleted ON epg_providers (deleted);

-- 删除旧表并创建新的频道表
DROP TABLE IF EXISTS channels;

CREATE TABLE IF NOT EXISTS channels
(
    id                               INTEGER PRIMARY KEY AUTOINCREMENT,
    name                             TEXT NOT NULL,
    logo                             TEXT,
    url                              TEXT NOT NULL,
    provider_id                      INTEGER,
    group_id                         INTEGER,
    epg_source_id                    TEXT,
    status                           TEXT,
    country                          TEXT,
    language                         TEXT,
    http_detect_delay_milliseconds   INTEGER,
    ffmpeg_detect_delay_milliseconds INTEGER,
    video_info                       TEXT,
    audio_info                       TEXT,
    score                            INTEGER,
    created_at                       TEXT NOT NULL DEFAULT (datetime('now')),
    updated_at                       TEXT NOT NULL DEFAULT (datetime('now')),
    deleted                          INTEGER       DEFAULT 0
);

-- 创建索引
CREATE INDEX IF NOT EXISTS idx_channels_provider_id ON channels (provider_id);
CREATE INDEX IF NOT EXISTS idx_channels_group_id ON channels (group_id);
CREATE INDEX IF NOT EXISTS idx_channels_epg_provider_id ON channels (epg_source_id);

-- M3U8 原始数据历史表
-- 用于存储每次获取的 M3U8 原始内容，保留最近3次数据用于备份和调试

CREATE TABLE IF NOT EXISTS iptv_provider_raw_data
(
    id          INTEGER PRIMARY KEY AUTOINCREMENT,
    provider_id INTEGER   NOT NULL,
    content     TEXT      NOT NULL,
    fetched_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted     INTEGER            DEFAULT 0,
    FOREIGN KEY (provider_id) REFERENCES iptv_providers (id) ON DELETE CASCADE
);

-- 索引：按 provider_id 和时间降序查询，便于快速获取最近的数据
CREATE INDEX IF NOT EXISTS idx_iptv_provider_raw_data_provider_fetched
    ON iptv_provider_raw_data (provider_id, fetched_at DESC);

-- 索引：用于清理旧数据
CREATE INDEX IF NOT EXISTS idx_iptv_provider_raw_data_fetched_at
    ON iptv_provider_raw_data (fetched_at DESC);

CREATE INDEX IF NOT EXISTS idx_iptv_provider_raw_data_deleted ON iptv_provider_raw_data (deleted);

-- M3U8 刷新任务历史表
CREATE TABLE iptv_refresh_task
(
    id            INTEGER PRIMARY KEY AUTOINCREMENT,
    provider_id   INTEGER     NOT NULL,
    trigger_type  VARCHAR(20) NOT NULL,
    status        VARCHAR(20) NOT NULL,
    start_time    BIGINT,
    end_time      BIGINT,
    duration      INTEGER,
    channel_count INTEGER,
    error_message TEXT,
    raw_content   TEXT,
    created_at    DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at    DATETIME DEFAULT CURRENT_TIMESTAMP,
    deleted       INTEGER  DEFAULT 0
);

-- 创建索引
CREATE INDEX IF NOT EXISTS idx_iptv_refresh_task_provider ON iptv_refresh_task (provider_id);
CREATE INDEX IF NOT EXISTS idx_iptv_refresh_task_status ON iptv_refresh_task (status);
CREATE INDEX IF NOT EXISTS idx_iptv_refresh_task_created ON iptv_refresh_task (created_at);

-- M3U8 原始频道表
CREATE TABLE IF NOT EXISTS original_channels
(
    id           INTEGER PRIMARY KEY AUTOINCREMENT,
    provider_id  INTEGER NOT NULL,
    name         TEXT    NOT NULL,
    url          TEXT    NOT NULL,
    tvg_id       TEXT,
    tvg_name     TEXT,
    tvg_logo     TEXT,
    group_title  TEXT,
    tvg_country  TEXT,
    tvg_language TEXT,
    task_id      INTEGER,
    created_at   DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at   DATETIME DEFAULT CURRENT_TIMESTAMP,
    deleted      INTEGER  DEFAULT 0
);

-- 创建索引
CREATE INDEX IF NOT EXISTS idx_original_channels_provider ON original_channels (provider_id);
CREATE INDEX IF NOT EXISTS idx_original_channels_created ON original_channels (created_at);

-- 删除旧表并创建新的表
DROP TABLE IF EXISTS cleanup_engine;

create table cleanup_engine
(
    id              INTEGER
        constraint cleanup_engine_pk
            primary key AUTOINCREMENT,
    name            TEXT,
    engine          TEXT,
    rule_type       TEXT,
    params          TEXT,
    description     TEXT,
    full_class_name TEXT
);

INSERT INTO cleanup_engine ("id", "name", "engine", "rule_type", "params", "description", "full_class_name")
VALUES (1, '黑名单', 'BlackListEngine', 'FILTER',
        '[{"field":"keyword","label":"关键字","placeholder":null,"required":false,"type":"DYNAMIC_INPUT"}]', NULL,
        'com.lemongo97.iptv.iptvmanager.cleanup.engine.filter.BlackListEngine');
INSERT INTO cleanup_engine ("id", "name", "engine", "rule_type", "params", "description", "full_class_name")
VALUES (2, 'FFProbe 检测', 'FFProbeCheckEngine', 'DELAY',
        '[{"field":"delayMillisecond","label":"最高延迟时间(ms)","placeholder":null,"required":false,"type":"NUMBER"},{"field":"discardNoVideo","label":"丢弃无视频","placeholder":null,"required":false,"type":"SWITCH"},{"field":"discardNoAudio","label":"丢弃无音频","placeholder":null,"required":false,"type":"SWITCH"},{"field":"minVideoFrameWidth","label":"最小视频帧宽度","placeholder":null,"required":false,"type":"NUMBER"},{"field":"minVideoFrameHeight","label":"最小视频帧高度","placeholder":null,"required":false,"type":"NUMBER"}]',
        NULL, 'com.lemongo97.iptv.iptvmanager.cleanup.engine.delay.FFProbeCheckEngine');
INSERT INTO cleanup_engine ("id", "name", "engine", "rule_type", "params", "description", "full_class_name")
VALUES (3, '简繁转换', 'OpenCCEngine', 'NAME',
        '[{"field":"input","label":"输入语言","options":[{"value":"simple","label":"简体"},{"value":"traditional","label":"繁体"}],"placeholder":null,"required":false,"type":"SELECT"},{"field":"output","label":"输出语言","options":[{"value":"simple","label":"简体"},{"value":"traditional","label":"繁体"}],"placeholder":null,"required":false,"type":"SELECT"}]',
        NULL, 'com.lemongo97.iptv.iptvmanager.cleanup.engine.name.OpenCCEngine');
INSERT INTO cleanup_engine ("id", "name", "engine", "rule_type", "params", "description", "full_class_name")
VALUES (4, '大小写转换', 'CaseConversionEngine', 'NAME',
        '[{"field":"input","label":"输入","options":[{"value":"uppercase","label":"大写"},{"value":"lowercase","label":"小写"}],"placeholder":null,"required":false,"type":"SELECT"},{"field":"output","label":"输出","options":[{"value":"uppercase","label":"大写"},{"value":"lowercase","label":"小写"}],"placeholder":null,"required":false,"type":"SELECT"}]',
        NULL, 'com.lemongo97.iptv.iptvmanager.cleanup.engine.name.CaseConversionEngine');
INSERT INTO cleanup_engine ("id", "name", "engine", "rule_type", "params", "description", "full_class_name")
VALUES (5, '正则替换', 'RegexReplaceEngine', 'NAME',
        '[{"field":"regex","label":"正则表达式","placeholder":null,"required":false,"type":"INPUT"},{"field":"groups","label":"分组替换设置","keyField":"groupId","keyPlaceholder":"分组ID","placeholder":null,"required":false,"type":"DYNAMIC_PAIR_INPUT","valueField":"text","valuePlaceholder":"替换值"}]',
        NULL, 'com.lemongo97.iptv.iptvmanager.cleanup.engine.name.RegexReplaceEngine');
INSERT INTO cleanup_engine ("id", "name", "engine", "rule_type", "params", "description", "full_class_name")
VALUES (6, '字符串替换', 'StringReplaceEngine', 'NAME',
        '[{"field":"target","label":"匹配值","placeholder":null,"required":false,"type":"INPUT"},{"field":"text","label":"替换文字","placeholder":null,"required":false,"type":"INPUT"}]',
        NULL, 'com.lemongo97.iptv.iptvmanager.cleanup.engine.name.StringReplaceEngine');
INSERT INTO cleanup_engine ("id", "name", "engine", "rule_type", "params", "description", "full_class_name")
VALUES (7, 'HTTP 检测', 'HttpCheckEngine', 'DELAY',
        '[{"field":"type","label":"检测方式","options":[{"value":"GET","label":"GET"},{"value":"HEAD","label":"HEAD"}],"placeholder":null,"required":false,"type":"SELECT"},{"field":"delayMillisecond","label":"最大延迟时间(ms)","placeholder":null,"required":false,"type":"NUMBER"}]',
        NULL, 'com.lemongo97.iptv.iptvmanager.cleanup.engine.delay.HttpCheckEngine');
INSERT INTO cleanup_engine ("id", "name", "engine", "rule_type", "params", "description", "full_class_name")
VALUES (8, '分组（关键字）', 'GroupingEngine', 'GROUP',
        '[{"field":"keyword","label":"匹配值","placeholder":null,"required":false,"type":"INPUT"},{"field":"groupId","label":"分组","options":[],"placeholder":null,"required":false,"type":"SELECT"}]',
        NULL, 'com.lemongo97.iptv.iptvmanager.cleanup.engine.group.GroupingEngine');
INSERT INTO cleanup_engine ("id", "name", "engine", "rule_type", "params", "description", "full_class_name")
VALUES (9, '字符串移除', 'StringRemoveEngine', 'NAME',
        '[{"field":"ignoreCase","label":"忽略大小写","placeholder":null,"required":false,"type":"SWITCH"},{"field":"removeSpaces","label":"移除空格","placeholder":null,"required":false,"type":"SWITCH"},{"field":"target","label":"将被移除的字符串","placeholder":null,"required":false,"type":"DYNAMIC_INPUT"}]',
        NULL, 'com.lemongo97.iptv.iptvmanager.cleanup.engine.name.StringRemoveEngine');

-- 创建数据清洗规则表
CREATE TABLE IF NOT EXISTS cleanup_rules
(
    id         INTEGER PRIMARY KEY AUTOINCREMENT,
    name       TEXT NOT NULL,
    engine     TEXT NOT NULL,
    rule_type  TEXT NOT NULL,
    enabled    INTEGER       DEFAULT 1,
    params     TEXT,
    created_at TEXT NOT NULL DEFAULT (datetime('now')),
    updated_at TEXT NOT NULL DEFAULT (datetime('now')),
    sort_order INTEGER       DEFAULT 0,
    deleted    INTEGER       DEFAULT 0
);

-- 创建索引
CREATE INDEX IF NOT EXISTS idx_cleanup_rules_rule_type ON cleanup_rules (rule_type);
CREATE INDEX IF NOT EXISTS idx_cleanup_rules_enabled ON cleanup_rules (enabled);
CREATE INDEX IF NOT EXISTS idx_cleanup_rules_deleted ON cleanup_rules (deleted);
CREATE INDEX IF NOT EXISTS idx_cleanup_rules_sort_order ON cleanup_rules (sort_order);

-- 创建频道清洗中间表
-- 用于数据清洗过程中的临时存储，避免清洗中断导致数据丢失
-- 每次清洗任务开始前会物理清空此表

CREATE TABLE IF NOT EXISTS channel_cleaning_temp
(
    id                               INTEGER PRIMARY KEY AUTOINCREMENT,
    name                             TEXT NOT NULL,
    logo                             TEXT,
    url                              TEXT NOT NULL,
    provider_id                      INTEGER,
    group_id                         INTEGER,
    epg_source_id                    TEXT,
    status                           TEXT,
    country                          TEXT,
    language                         TEXT,
    http_detect_delay_milliseconds   INTEGER,
    ffmpeg_detect_delay_milliseconds INTEGER,
    video_info                       TEXT,
    audio_info                       TEXT,
    score                            INTEGER,
    created_at                       TEXT NOT NULL DEFAULT (datetime('now')),
    updated_at                       TEXT NOT NULL DEFAULT (datetime('now'))
);

-- 创建索引优化查询性能
CREATE INDEX IF NOT EXISTS idx_channel_cleaning_temp_provider_id
    ON channel_cleaning_temp (provider_id);

CREATE INDEX IF NOT EXISTS idx_channel_cleaning_temp_group_id
    ON channel_cleaning_temp (group_id);

-- 通用任务进度跟踪表
-- 用于跟踪长时间运行的任务（如数据清洗、M3U8解析等）
CREATE TABLE IF NOT EXISTS task_progress
(
    id              INTEGER PRIMARY KEY AUTOINCREMENT,
    task_id         TEXT NOT NULL,           -- 任务唯一标识（UUID或自定义ID）
    task_type       TEXT NOT NULL,           -- 任务类型：DATA_CLEANUP, M3U8_PARSE, HTTP_DETECT等
    progress        REAL          DEFAULT 0, -- 进度百分比（0-100）
    status          TEXT NOT NULL,           -- 状态：RUNNING, SUCCESS, ERROR, NOT_RUNNING
    message         TEXT,                    -- 状态消息或错误信息
    total_items     INTEGER,                 -- 总项目数（如频道总数）
    processed_items INTEGER,                 -- 已处理项目数
    started_at      TEXT,                    -- 任务开始时间
    completed_at    TEXT,                    -- 任务完成时间
    created_at      TEXT NOT NULL DEFAULT (datetime('now')),
    updated_at      TEXT NOT NULL DEFAULT (datetime('now'))
);

-- 创建索引
CREATE UNIQUE INDEX IF NOT EXISTS idx_task_progress_task_id
    ON task_progress (task_id);

CREATE INDEX IF NOT EXISTS idx_task_progress_task_type
    ON task_progress (task_type);

CREATE INDEX IF NOT EXISTS idx_task_progress_status
    ON task_progress (status);

DROP TABLE IF EXISTS epg_programs;

-- 创建 EPG 节目表
CREATE TABLE IF NOT EXISTS epg_programs
(
    id            INTEGER PRIMARY KEY AUTOINCREMENT,
    epg_source_id INTEGER NOT NULL,
    channel_id    TEXT, -- 关联频道ID (可选)
    start_time    TEXT, -- 节目开始时间 (ISO 8601)
    stop_time     TEXT, -- 节目结束时间 (ISO 8601)
    title         TEXT, -- 节目标题
    sub_title     TEXT, -- 节目副标题
    description   TEXT, -- 节目描述
    created_at    TEXT    DEFAULT (datetime('now', 'localtime')),
    updated_at    TEXT    DEFAULT (datetime('now', 'localtime')),
    deleted       BOOLEAN DEFAULT 0,
    FOREIGN KEY (epg_source_id) REFERENCES epg_providers (id) ON DELETE CASCADE
);

-- 创建索引
CREATE INDEX IF NOT EXISTS idx_epg_programs_source_id ON epg_programs (epg_source_id);
CREATE INDEX IF NOT EXISTS idx_epg_programs_start_time ON epg_programs (start_time);
CREATE INDEX IF NOT EXISTS idx_epg_programs_deleted ON epg_programs (deleted);

-- 创建 EPG 节目表
CREATE TABLE IF NOT EXISTS epg_channels
(
    id            INTEGER PRIMARY KEY AUTOINCREMENT,
    epg_source_id INTEGER NOT NULL,
    channel_id    TEXT    NOT NULL,
    display_name  TEXT,
    icon          TEXT,
    created_at    TEXT    DEFAULT (datetime('now', 'localtime')),
    updated_at    TEXT    DEFAULT (datetime('now', 'localtime')),
    deleted       BOOLEAN DEFAULT 0,
    FOREIGN KEY (epg_source_id) REFERENCES epg_providers (id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_epg_channels_source_id ON epg_channels (epg_source_id);
