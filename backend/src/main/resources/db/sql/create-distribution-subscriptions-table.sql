-- 创建分发订阅表
CREATE TABLE IF NOT EXISTS distribution_subscriptions (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    user_id INTEGER NOT NULL,
    start_time TEXT NOT NULL DEFAULT (datetime('now')),
    end_time TEXT,
    created_at TEXT NOT NULL DEFAULT (datetime('now')),
    updated_at TEXT NOT NULL DEFAULT (datetime('now')),
    deleted INTEGER NOT NULL DEFAULT 0,
    FOREIGN KEY (user_id) REFERENCES distribution_users(id)
);

-- 创建索引
CREATE INDEX IF NOT EXISTS idx_distribution_subscriptions_user_id ON distribution_subscriptions(user_id);
CREATE INDEX IF NOT EXISTS idx_distribution_subscriptions_deleted ON distribution_subscriptions(deleted);
CREATE INDEX IF NOT EXISTS idx_distribution_subscriptions_start_time ON distribution_subscriptions(start_time);
CREATE INDEX IF NOT EXISTS idx_distribution_subscriptions_end_time ON distribution_subscriptions(end_time);
