-- 为分发订阅表添加日期类型字段
ALTER TABLE distribution_subscriptions ADD COLUMN date_type TEXT NOT NULL DEFAULT 'YEAR';

-- 创建索引以优化查询
CREATE INDEX IF NOT EXISTS idx_distribution_subscriptions_date_type ON distribution_subscriptions(date_type);
