ALTER TABLE users ADD COLUMN role VARCHAR(20) NOT NULL DEFAULT 'USER';
ALTER TABLE users ADD CONSTRAINT chk_users_role CHECK (role IN ('USER', 'ADMIN'));
ALTER TABLE users ADD COLUMN banned_at TIMESTAMPTZ;

ALTER TABLE category DROP CONSTRAINT category_user_id_fkey,
    ADD CONSTRAINT category_user_id_fkey FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE;
ALTER TABLE course DROP CONSTRAINT course_user_id_fkey,
    ADD CONSTRAINT course_user_id_fkey FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE;
ALTER TABLE session DROP CONSTRAINT session_user_id_fkey,
    ADD CONSTRAINT session_user_id_fkey FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE;
ALTER TABLE goal DROP CONSTRAINT goal_user_id_fkey,
    ADD CONSTRAINT goal_user_id_fkey FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE;
ALTER TABLE roadmap DROP CONSTRAINT roadmap_owner_id_fkey,
    ADD CONSTRAINT roadmap_owner_id_fkey FOREIGN KEY (owner_id) REFERENCES users (id) ON DELETE CASCADE;
ALTER TABLE friendship DROP CONSTRAINT friendship_requester_id_fkey,
    ADD CONSTRAINT friendship_requester_id_fkey FOREIGN KEY (requester_id) REFERENCES users (id) ON DELETE CASCADE;
ALTER TABLE friendship DROP CONSTRAINT friendship_addressee_id_fkey,
    ADD CONSTRAINT friendship_addressee_id_fkey FOREIGN KEY (addressee_id) REFERENCES users (id) ON DELETE CASCADE;
ALTER TABLE message DROP CONSTRAINT message_sender_id_fkey,
    ADD CONSTRAINT message_sender_id_fkey FOREIGN KEY (sender_id) REFERENCES users (id) ON DELETE CASCADE;
ALTER TABLE message DROP CONSTRAINT message_recipient_id_fkey,
    ADD CONSTRAINT message_recipient_id_fkey FOREIGN KEY (recipient_id) REFERENCES users (id) ON DELETE CASCADE;
