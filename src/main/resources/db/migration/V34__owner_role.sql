ALTER TABLE users DROP CONSTRAINT chk_users_role;
ALTER TABLE users ADD CONSTRAINT chk_users_role CHECK (role IN ('USER', 'ADMIN', 'OWNER'));

UPDATE users SET role = 'OWNER' WHERE email = 'kauannito1@gmail.com';
