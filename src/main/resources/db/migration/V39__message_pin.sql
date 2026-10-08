ALTER TABLE message ADD COLUMN pinned_at TIMESTAMPTZ;
ALTER TABLE message ADD COLUMN pinned_by UUID;

CREATE INDEX idx_message_pinned_group ON message (group_id) WHERE pinned_at IS NOT NULL;
CREATE INDEX idx_message_pinned_conversation
    ON message (LEAST(sender_id, recipient_id), GREATEST(sender_id, recipient_id))
    WHERE pinned_at IS NOT NULL AND group_id IS NULL;
