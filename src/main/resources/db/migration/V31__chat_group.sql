CREATE TABLE chat_group (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(60) NOT NULL,
    -- sem NOT NULL/CASCADE: v1 não tem dono com poderes especiais, então o
    -- grupo (e os outros membros) não deveria sumir só porque quem criou
    -- saiu da plataforma — fica só como metadado de auditoria, viu NULL.
    created_by UUID REFERENCES users (id) ON DELETE SET NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Sem PK substituta: cada linha só é identificada/consultada pelo par
-- (group_id, user_id) — mesmo raciocínio de roadmap_step_completion.
CREATE TABLE chat_group_member (
    group_id UUID NOT NULL REFERENCES chat_group (id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    joined_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    -- baseline da contagem de não lidas: começa em joined_at pra quem entra
    -- num grupo não herdar o histórico inteiro como não lido (ver Message).
    last_read_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    PRIMARY KEY (group_id, user_id)
);

CREATE INDEX idx_chat_group_member_user ON chat_group_member (user_id);

-- Mensagem de grupo reaproveita a tabela message (mesmo pipeline de
-- criptografia/anexo/reply do chat 1:1) em vez de duplicar tudo numa
-- tabela paralela: exatamente um de recipient_id/group_id é preenchido.
ALTER TABLE message ALTER COLUMN recipient_id DROP NOT NULL;
ALTER TABLE message ADD COLUMN group_id UUID REFERENCES chat_group (id) ON DELETE CASCADE;
ALTER TABLE message ADD CONSTRAINT chk_message_target CHECK (
    (recipient_id IS NOT NULL AND group_id IS NULL) OR (recipient_id IS NULL AND group_id IS NOT NULL)
);

CREATE INDEX idx_message_group ON message (group_id, created_at);
