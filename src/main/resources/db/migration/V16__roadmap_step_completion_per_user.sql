-- Conclusão de etapa de roadmap passa a ser por-usuário.
-- Antes, roadmap_step.completed era uma coluna única na linha compartilhada da
-- etapa: para roadmaps predefinidos (owner_id null, comuns a todos) isso tornava
-- a conclusão global e alterável por qualquer usuário. Agora cada usuário tem
-- suas próprias conclusões.

CREATE TABLE roadmap_step_completion (
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    step_id UUID NOT NULL REFERENCES roadmap_step(id) ON DELETE CASCADE,
    completed_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    PRIMARY KEY (user_id, step_id)
);

CREATE INDEX idx_roadmap_step_completion_step ON roadmap_step_completion(step_id);

-- Migra as conclusões existentes de roadmaps PRÓPRIOS para o respectivo dono.
INSERT INTO roadmap_step_completion (user_id, step_id)
SELECT r.owner_id, s.id
FROM roadmap_step s
JOIN roadmap r ON r.id = s.roadmap_id
WHERE s.completed = TRUE AND r.owner_id IS NOT NULL;

-- Conclusões em roadmaps predefinidos (owner_id null) eram estado compartilhado
-- corrompido, sem dono real — são descartadas ao remover a coluna.
ALTER TABLE roadmap_step DROP COLUMN completed;
