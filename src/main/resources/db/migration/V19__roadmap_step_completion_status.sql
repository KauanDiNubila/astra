-- Progresso por etapa deixa de ser binário (concluído/não) e passa a ter
-- 3 estados: aprendendo, concluído, pulado. Ausência de linha continua
-- significando "pendente". Toda linha existente hoje já significa
-- "concluído", então o backfill via DEFAULT está correto.

ALTER TABLE roadmap_step_completion ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'DONE';
