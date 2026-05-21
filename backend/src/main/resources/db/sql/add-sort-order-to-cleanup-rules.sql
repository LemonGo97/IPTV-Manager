-- Add sort_order column to cleanup_rules table
ALTER TABLE cleanup_rules ADD COLUMN sort_order INTEGER DEFAULT 0;

-- Create index for sort order queries
CREATE INDEX IF NOT EXISTS idx_cleanup_rules_sort_order ON cleanup_rules(sort_order);

-- Update existing records to have sort_order equal to id (preserves creation order)
UPDATE cleanup_rules SET sort_order = id WHERE sort_order = 0;
