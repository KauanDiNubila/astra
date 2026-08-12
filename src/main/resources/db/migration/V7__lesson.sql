CREATE TABLE lesson (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    module_id UUID NOT NULL REFERENCES module (id) ON DELETE CASCADE,
    title VARCHAR(160) NOT NULL,
    position INT NOT NULL,
    completed BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_lesson_module ON lesson (module_id);

ALTER TABLE module DROP COLUMN completed;
