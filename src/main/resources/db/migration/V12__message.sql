CREATE TABLE message (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    sender_id UUID NOT NULL REFERENCES users (id),
    recipient_id UUID NOT NULL REFERENCES users (id),
    content VARCHAR(2000) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    read_at TIMESTAMPTZ,
    CONSTRAINT chk_message_not_self CHECK (sender_id <> recipient_id)
);

CREATE INDEX idx_message_conversation ON message (LEAST(sender_id, recipient_id), GREATEST(sender_id, recipient_id), created_at);
CREATE INDEX idx_message_recipient_unread ON message (recipient_id, read_at);
