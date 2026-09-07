ALTER TABLE users ADD COLUMN banner BYTEA;
ALTER TABLE users ADD COLUMN banner_content_type VARCHAR(50);
ALTER TABLE users ADD COLUMN accent_color VARCHAR(7);
ALTER TABLE users ADD COLUMN profile_effect VARCHAR(20);

ALTER TABLE users ADD CONSTRAINT chk_users_accent_color CHECK (accent_color ~ '^#[0-9a-fA-F]{6}$');
ALTER TABLE users ADD CONSTRAINT chk_users_profile_effect CHECK (profile_effect IN ('SPARKLES', 'CONFETTI', 'SNOW'));
