-- 创建订阅用户表
CREATE TABLE IF NOT EXISTS distribution_users (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    username TEXT NOT NULL UNIQUE,
    user_id TEXT NOT NULL UNIQUE,
    access_key TEXT NOT NULL,
    created_at TEXT NOT NULL DEFAULT (datetime('now')),
    updated_at TEXT NOT NULL DEFAULT (datetime('now')),
    deleted INTEGER NOT NULL DEFAULT 0
);

-- 创建索引
CREATE INDEX IF NOT EXISTS idx_distribution_users_username ON distribution_users(username);
CREATE INDEX IF NOT EXISTS idx_distribution_users_user_id ON distribution_users(user_id);
CREATE INDEX IF NOT EXISTS idx_distribution_users_deleted ON distribution_users(deleted);
