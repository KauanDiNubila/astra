-- Resposta a uma mensagem específica (estilo WhatsApp). Auto-FK simples,
-- mesmo estilo de roadmap_step.parent_step_id: coluna UUID pura, não
-- @ManyToOne, consistente com sender_id/recipient_id já sendo UUID puro.
--
-- ON DELETE SET NULL (não CASCADE): apagar a mensagem original não deveria
-- apagar em cascata as respostas a ela — são mensagens independentes que só
-- perdem a citação. Ainda não existe delete de mensagem no sistema, mas essa
-- escolha já deixa o design correto pra quando existir.
ALTER TABLE message ADD COLUMN reply_to_message_id UUID REFERENCES message (id) ON DELETE SET NULL;
CREATE INDEX idx_message_reply_to ON message (reply_to_message_id);
