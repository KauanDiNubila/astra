-- Anexo de imagem por mensagem. Tabela separada (não bytea direto em
-- message) porque message é hot-path e tem muito mais linhas que users —
-- guardar bytes de imagem na própria linha infla índice/backup da tabela
-- mais quente do sistema. Um anexo por mensagem (unique em message_id).
CREATE TABLE message_attachment (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    message_id UUID NOT NULL UNIQUE REFERENCES message (id) ON DELETE CASCADE,
    content_type VARCHAR(50) NOT NULL,
    data BYTEA NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Mensagem só-imagem (sem legenda) precisa ser permitida.
ALTER TABLE message ALTER COLUMN content DROP NOT NULL;
