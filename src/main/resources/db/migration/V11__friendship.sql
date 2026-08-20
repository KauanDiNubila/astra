CREATE TABLE friendship (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    requester_id UUID NOT NULL REFERENCES users (id),
    addressee_id UUID NOT NULL REFERENCES users (id),
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT chk_friendship_not_self CHECK (requester_id <> addressee_id)
);

CREATE UNIQUE INDEX uq_friendship_pair ON friendship (LEAST(requester_id, addressee_id), GREATEST(requester_id, addressee_id));
CREATE INDEX idx_friendship_requester ON friendship (requester_id);
CREATE INDEX idx_friendship_addressee ON friendship (addressee_id);
