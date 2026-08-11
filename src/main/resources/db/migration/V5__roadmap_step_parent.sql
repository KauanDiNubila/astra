ALTER TABLE roadmap_step ADD COLUMN parent_step_id UUID REFERENCES roadmap_step (id) ON DELETE CASCADE;
CREATE INDEX idx_roadmap_step_parent ON roadmap_step (parent_step_id);
