-- 通用任务进度跟踪表
-- 用于跟踪长时间运行的任务（如数据清洗、M3U8解析等）
CREATE TABLE IF NOT EXISTS task_progress (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    task_id TEXT NOT NULL,              -- 任务唯一标识（UUID或自定义ID）
    task_type TEXT NOT NULL,            -- 任务类型：DATA_CLEANUP, M3U8_PARSE, HTTP_DETECT等
    progress REAL DEFAULT 0,         -- 进度百分比（0-100）
    status TEXT NOT NULL,               -- 状态：RUNNING, SUCCESS, ERROR, NOT_RUNNING
    message TEXT,                       -- 状态消息或错误信息
    total_items INTEGER,                -- 总项目数（如频道总数）
    processed_items INTEGER,            -- 已处理项目数
    started_at TEXT,                    -- 任务开始时间
    completed_at TEXT,                  -- 任务完成时间
    created_at TEXT NOT NULL DEFAULT (datetime('now')),
    updated_at TEXT NOT NULL DEFAULT (datetime('now'))
);

-- 创建索引
CREATE UNIQUE INDEX IF NOT EXISTS idx_task_progress_task_id
    ON task_progress(task_id);

CREATE INDEX IF NOT EXISTS idx_task_progress_task_type
    ON task_progress(task_type);

CREATE INDEX IF NOT EXISTS idx_task_progress_status
    ON task_progress(status);
