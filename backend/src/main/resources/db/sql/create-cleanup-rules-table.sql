-- 创建数据清洗规则表
CREATE TABLE IF NOT EXISTS cleanup_rules (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    engine TEXT NOT NULL,
    rule_type TEXT NOT NULL,
    enabled INTEGER DEFAULT 1,
    params TEXT,
    created_at TEXT NOT NULL DEFAULT (datetime('now')),
    updated_at TEXT NOT NULL DEFAULT (datetime('now')),
    deleted INTEGER DEFAULT 0
);

-- 创建索引
CREATE INDEX IF NOT EXISTS idx_cleanup_rules_rule_type ON cleanup_rules(rule_type);
CREATE INDEX IF NOT EXISTS idx_cleanup_rules_enabled ON cleanup_rules(enabled);
CREATE INDEX IF NOT EXISTS idx_cleanup_rules_deleted ON cleanup_rules(deleted);
