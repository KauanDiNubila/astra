ALTER TABLE users ADD COLUMN tag VARCHAR(4);

DO $$
DECLARE
    r RECORD;
    candidate VARCHAR(4);
    attempts INT;
BEGIN
    FOR r IN SELECT id, name FROM users ORDER BY created_at LOOP
        attempts := 0;
        LOOP
            candidate := lpad(floor(random() * 10000)::text, 4, '0');
            attempts := attempts + 1;
            EXIT WHEN NOT EXISTS (
                SELECT 1 FROM users u2 WHERE u2.name = r.name AND u2.tag = candidate
            );
            IF attempts > 200 THEN
                RAISE EXCEPTION 'Não foi possível gerar tag única para %', r.name;
            END IF;
        END LOOP;
        UPDATE users SET tag = candidate WHERE id = r.id;
    END LOOP;
END $$;

ALTER TABLE users ALTER COLUMN tag SET NOT NULL;
ALTER TABLE users ADD CONSTRAINT uq_users_name_tag UNIQUE (name, tag);
