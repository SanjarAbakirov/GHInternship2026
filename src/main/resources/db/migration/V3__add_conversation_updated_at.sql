-- Adds updated_at to conversations so "order by update time" can be a plain
-- column query instead of a JOIN + GROUP BY over messages.

ALTER TABLE conversations ADD COLUMN updated_at TIMESTAMP;

UPDATE conversations SET updated_at = created_at WHERE updated_at IS NULL;

ALTER TABLE conversations ALTER COLUMN updated_at SET NOT NULL;

CREATE INDEX idx_conversations_user_updated_at ON conversations (user_id, updated_at DESC);
