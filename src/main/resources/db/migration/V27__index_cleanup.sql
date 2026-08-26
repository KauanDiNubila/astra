-- session.category_id é FK sem índice — Postgres não indexa o lado
-- referenciante de uma FK automaticamente. Usado por
-- CategoryService.delete (existsByCategoryId) e pela verificação de
-- integridade referencial ao excluir uma categoria.
CREATE INDEX idx_session_category ON session (category_id);

-- Índices redundantes: cada um é prefixo de um índice UNIQUE que já existe
-- na mesma tabela, então o planner nunca escolhe eles — só custam escrita
-- extra em todo INSERT/UPDATE sem nenhum ganho de leitura.
DROP INDEX idx_category_user;       -- prefixo de uq_category_user_name (user_id, name)
DROP INDEX idx_goal_user;           -- prefixo de uq_goal_user_type (user_id, type)
DROP INDEX idx_csl_course;          -- prefixo de uq_csl_course_step (course_id, step_id)
DROP INDEX idx_refresh_token_hash;  -- duplicata do índice único implícito de token_hash
