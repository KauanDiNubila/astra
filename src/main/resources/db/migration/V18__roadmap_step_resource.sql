-- Links curados (artigos, docs, vídeos) por etapa de roadmap. Conteúdo
-- compartilhado da etapa (não é por-usuário, ao contrário de completion).

CREATE TABLE roadmap_step_resource (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    step_id UUID NOT NULL REFERENCES roadmap_step(id) ON DELETE CASCADE,
    label VARCHAR(160) NOT NULL,
    url VARCHAR(500) NOT NULL,
    position INT NOT NULL DEFAULT 0
);

CREATE INDEX idx_roadmap_step_resource_step ON roadmap_step_resource(step_id);
