CREATE TABLE github_connection (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    github_user_id BIGINT NOT NULL,
    github_login VARCHAR(120) NOT NULL,
    github_avatar_url VARCHAR(500),
    access_token TEXT NOT NULL,
    refresh_token TEXT,
    access_token_expires_at TIMESTAMPTZ NOT NULL,
    refresh_token_expires_at TIMESTAMPTZ,
    connected_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    last_synced_at TIMESTAMPTZ,
    last_sync_error VARCHAR(300)
);

CREATE TABLE github_repository (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    github_repo_id BIGINT NOT NULL,
    owner VARCHAR(120) NOT NULL,
    name VARCHAR(200) NOT NULL,
    full_name VARCHAR(320) NOT NULL,
    description VARCHAR(500),
    primary_language VARCHAR(60),
    html_url VARCHAR(500) NOT NULL,
    private BOOLEAN NOT NULL,
    star_count INT NOT NULL DEFAULT 0,
    fork_count INT NOT NULL DEFAULT 0,
    default_branch VARCHAR(100),
    pushed_at TIMESTAMPTZ,
    created_at_on_github TIMESTAMPTZ,
    recent_commit_count INT NOT NULL DEFAULT 0,
    synced_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uk_github_repository_user_repo UNIQUE (user_id, github_repo_id)
);

CREATE INDEX idx_github_repository_user ON github_repository (user_id);

CREATE TABLE github_daily_activity (
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    activity_date DATE NOT NULL,
    contribution_count INT NOT NULL DEFAULT 0,
    PRIMARY KEY (user_id, activity_date)
);

CREATE TABLE github_activity_summary (
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    period VARCHAR(10) NOT NULL,
    commit_count INT NOT NULL DEFAULT 0,
    pull_request_opened_count INT NOT NULL DEFAULT 0,
    pull_request_merged_count INT NOT NULL DEFAULT 0,
    issue_closed_count INT NOT NULL DEFAULT 0,
    active_repo_count INT NOT NULL DEFAULT 0,
    PRIMARY KEY (user_id, period)
);

CREATE TABLE course_github_repo (
    course_id UUID NOT NULL REFERENCES course(id) ON DELETE CASCADE,
    repository_id UUID NOT NULL REFERENCES github_repository(id) ON DELETE CASCADE,
    linked_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    PRIMARY KEY (course_id, repository_id)
);

ALTER TABLE session ADD COLUMN github_repository_id UUID REFERENCES github_repository(id) ON DELETE SET NULL;
