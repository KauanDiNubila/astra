CREATE TABLE users (
    id            UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    name          VARCHAR(120) NOT NULL,
    email         VARCHAR(180) NOT NULL UNIQUE,
    password_hash VARCHAR(100) NOT NULL,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT now()
);
CREATE TABLE category (
    id      UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID        NOT NULL REFERENCES users (id),
    name    VARCHAR(80) NOT NULL,
    color   VARCHAR(20),
    CONSTRAINT uq_category_user_name UNIQUE (user_id, name)
);
CREATE TABLE course (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users (id),
    title VARCHAR(160) NOT NULL, platform VARCHAR(80), status VARCHAR(30)
);
CREATE TABLE module (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    course_id UUID NOT NULL REFERENCES course (id) ON DELETE CASCADE,
    title VARCHAR(160) NOT NULL, position INT NOT NULL,
    completed BOOLEAN NOT NULL DEFAULT FALSE
);
CREATE TABLE session (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users (id),
    category_id UUID NOT NULL REFERENCES category (id),
    course_id UUID REFERENCES course (id),
    focused_minutes INT NOT NULL CHECK (focused_minutes > 0),
    started_at TIMESTAMPTZ NOT NULL,
    note VARCHAR(500),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE TABLE goal (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users (id),
    type VARCHAR(20) NOT NULL, target_hours INT NOT NULL CHECK (target_hours > 0),
    CONSTRAINT uq_goal_user_type UNIQUE (user_id, type)
);
CREATE TABLE roadmap (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    owner_id UUID REFERENCES users (id),
    title VARCHAR(160) NOT NULL, source VARCHAR(200)
);
CREATE TABLE roadmap_step (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    roadmap_id UUID NOT NULL REFERENCES roadmap (id) ON DELETE CASCADE,
    title VARCHAR(160) NOT NULL, position INT NOT NULL
);
CREATE TABLE course_step_link (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    course_id UUID NOT NULL REFERENCES course (id) ON DELETE CASCADE,
    step_id UUID NOT NULL REFERENCES roadmap_step (id) ON DELETE CASCADE,
    status VARCHAR(30), rating INT CHECK (rating BETWEEN 1 AND 5),
    CONSTRAINT uq_csl_course_step UNIQUE (course_id, step_id)
);

CREATE INDEX idx_session_user_started ON session (user_id, started_at);
CREATE INDEX idx_category_user ON category (user_id);
CREATE INDEX idx_course_user ON course (user_id);
CREATE INDEX idx_module_course ON module (course_id);
CREATE INDEX idx_goal_user ON goal (user_id);
CREATE INDEX idx_roadmap_step_roadmap ON roadmap_step (roadmap_id);
CREATE INDEX idx_csl_course ON course_step_link (course_id);
CREATE INDEX idx_csl_step ON course_step_link (step_id);