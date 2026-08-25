-- Etapas de roadmap ganham uma descrição opcional, exibida no painel de
-- detalhe ao clicar na etapa. Nula por padrão — só o roadmap "Backend Java"
-- recebe conteúdo autoral por enquanto (ver V20).

ALTER TABLE roadmap_step ADD COLUMN description TEXT NULL;
