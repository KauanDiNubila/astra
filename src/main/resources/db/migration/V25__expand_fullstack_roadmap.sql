-- Expande o roadmap "Full-Stack Web" (de 16 pra ~36 etapas), mesma linha
-- das expansões anteriores.

-- ===== Mais filhos nas etapas principais existentes =====

INSERT INTO roadmap_step (id, roadmap_id, parent_step_id, title, position, description) VALUES
    ('d1000000-0000-0000-0000-000000000001', '00000000-0000-0000-0000-000000000040', '00000000-0000-0000-0000-000000000041', 'Segurança básica (CORS, CSRF, XSS)', 4, 'Os três problemas de segurança que qualquer aplicação web full-stack encontra logo cedo, e como cada camada (frontend/backend) ajuda a mitigá-los.'),
    ('d1000000-0000-0000-0000-000000000002', '00000000-0000-0000-0000-000000000040', '00000000-0000-0000-0000-000000000042', 'Filas e jobs em background', 4, 'Tarefas que não precisam (ou não podem) rodar dentro do ciclo de uma requisição HTTP: envio de e-mail, geração de relatório, processamento de imagem.'),
    ('d1000000-0000-0000-0000-000000000003', '00000000-0000-0000-0000-000000000040', '00000000-0000-0000-0000-000000000043', 'Acessibilidade', 4, 'Interfaces utilizáveis por teclado e leitor de tela — parte do trabalho de frontend, não um extra opcional.'),
    ('d1000000-0000-0000-0000-000000000004', '00000000-0000-0000-0000-000000000040', '00000000-0000-0000-0000-000000000044', 'Variáveis de ambiente e secrets', 4, 'Configuração e credenciais fora do código-fonte, injetadas em tempo de execução — diferente pra cada ambiente (dev, staging, produção).');

INSERT INTO roadmap_step_resource (step_id, label, url, position) VALUES
    ('d1000000-0000-0000-0000-000000000001', 'OWASP — Cross-Site Request Forgery', 'https://owasp.org/www-community/attacks/csrf', 0),
    ('d1000000-0000-0000-0000-000000000002', 'BullMQ (job queue) Docs', 'https://docs.bullmq.io/', 0),
    ('d1000000-0000-0000-0000-000000000003', 'MDN — Accessibility', 'https://developer.mozilla.org/en-US/docs/Web/Accessibility', 0),
    ('d1000000-0000-0000-0000-000000000004', '12factor.net — Config', 'https://12factor.net/config', 0);

-- ===== Novas etapas principais =====

INSERT INTO roadmap_step (id, roadmap_id, parent_step_id, title, position, description) VALUES
    ('d1000000-0000-0000-0000-000000000010', '00000000-0000-0000-0000-000000000040', NULL, 'Autenticação e autorização full-stack', 5, 'Como identidade e permissão atravessam frontend e backend juntos: quem é o usuário, e o que ele pode fazer.'),
    ('d1000000-0000-0000-0000-000000000020', '00000000-0000-0000-0000-000000000040', NULL, 'Qualidade e testes end-to-end', 6, 'Testar o sistema como um todo, simulando um usuário real navegando pela aplicação de ponta a ponta.'),
    ('d1000000-0000-0000-0000-000000000030', '00000000-0000-0000-0000-000000000040', NULL, 'Performance full-stack', 7, 'Onde otimizar quando frontend e backend interagem: às vezes o gargalo não é onde parece.'),
    ('d1000000-0000-0000-0000-000000000040', '00000000-0000-0000-0000-000000000040', NULL, 'Colaboração em equipe', 8, 'Full-stack raramente é trabalho solo — práticas que fazem várias pessoas mexerem no mesmo código sem pisar uma na outra.');

INSERT INTO roadmap_step_resource (step_id, label, url, position) VALUES
    ('d1000000-0000-0000-0000-000000000010', 'Auth0 — Sessions vs Tokens', 'https://auth0.com/blog/cookies-vs-tokens-definitive-guide/', 0),
    ('d1000000-0000-0000-0000-000000000020', 'Playwright Docs', 'https://playwright.dev/docs/intro', 0),
    ('d1000000-0000-0000-0000-000000000030', 'web.dev — Core Web Vitals', 'https://web.dev/articles/vitals', 0),
    ('d1000000-0000-0000-0000-000000000040', 'Google Engineering Practices — Code Review', 'https://google.github.io/eng-practices/review/', 0);

-- ---- Subetapas de Autenticação e autorização full-stack ----
INSERT INTO roadmap_step (id, roadmap_id, parent_step_id, title, position, description) VALUES
    ('d1000000-0000-0000-0000-000000000011', '00000000-0000-0000-0000-000000000040', 'd1000000-0000-0000-0000-000000000010', 'Sessions vs tokens', 1, 'Sessão guardada no servidor versus token (JWT) que carrega a própria informação — trade-offs de escala e revogação.'),
    ('d1000000-0000-0000-0000-000000000012', '00000000-0000-0000-0000-000000000040', 'd1000000-0000-0000-0000-000000000010', 'OAuth e login social', 2, '"Entrar com Google/GitHub" delega a autenticação a um provedor terceiro, em vez de a aplicação guardar senha nenhuma.'),
    ('d1000000-0000-0000-0000-000000000013', '00000000-0000-0000-0000-000000000040', 'd1000000-0000-0000-0000-000000000010', 'Autorização por papéis', 3, 'Depois de saber quem é o usuário, decidir o que ele pode ver ou fazer (admin, membro, convidado) — RBAC na prática.');

INSERT INTO roadmap_step_resource (step_id, label, url, position) VALUES
    ('d1000000-0000-0000-0000-000000000011', 'Auth0 — Sessions vs Tokens', 'https://auth0.com/blog/cookies-vs-tokens-definitive-guide/', 0),
    ('d1000000-0000-0000-0000-000000000012', 'OAuth 2.0 — Introduction', 'https://oauth.net/2/', 0),
    ('d1000000-0000-0000-0000-000000000013', 'Role-Based Access Control (NIST)', 'https://csrc.nist.gov/projects/role-based-access-control', 0);

-- ---- Subetapas de Qualidade e testes end-to-end ----
INSERT INTO roadmap_step (id, roadmap_id, parent_step_id, title, position, description) VALUES
    ('d1000000-0000-0000-0000-000000000021', '00000000-0000-0000-0000-000000000040', 'd1000000-0000-0000-0000-000000000020', 'Testes E2E (Playwright/Cypress)', 1, 'Simular um navegador de verdade clicando pela aplicação, pegando bugs que testes unitários isolados não conseguem ver.'),
    ('d1000000-0000-0000-0000-000000000022', '00000000-0000-0000-0000-000000000040', 'd1000000-0000-0000-0000-000000000020', 'Testes de API', 2, 'Testar os endpoints do backend diretamente (sem UI), verificando contrato, status codes e formato de resposta.'),
    ('d1000000-0000-0000-0000-000000000023', '00000000-0000-0000-0000-000000000040', 'd1000000-0000-0000-0000-000000000020', 'Cobertura de testes', 3, 'Métrica de quanto do código é exercitado pelos testes — útil como sinal de alerta, perigosa como meta cega a perseguir.');

INSERT INTO roadmap_step_resource (step_id, label, url, position) VALUES
    ('d1000000-0000-0000-0000-000000000021', 'Playwright Docs', 'https://playwright.dev/docs/intro', 0),
    ('d1000000-0000-0000-0000-000000000022', 'Postman Learning Center', 'https://learning.postman.com/', 0),
    ('d1000000-0000-0000-0000-000000000023', 'Martin Fowler — Test Coverage', 'https://martinfowler.com/bliki/TestCoverage.html', 0);

-- ---- Subetapas de Performance full-stack ----
INSERT INTO roadmap_step (id, roadmap_id, parent_step_id, title, position, description) VALUES
    ('d1000000-0000-0000-0000-000000000031', '00000000-0000-0000-0000-000000000040', 'd1000000-0000-0000-0000-000000000030', 'Cache em camadas', 1, 'Cache no navegador, CDN, aplicação e banco — cada camada resolve um tipo diferente de latência.'),
    ('d1000000-0000-0000-0000-000000000032', '00000000-0000-0000-0000-000000000040', 'd1000000-0000-0000-0000-000000000030', 'SSR vs CSR vs SSG', 2, 'Renderizar no servidor, no cliente ou em build time — cada abordagem troca velocidade inicial por complexidade de infraestrutura.'),
    ('d1000000-0000-0000-0000-000000000033', '00000000-0000-0000-0000-000000000040', 'd1000000-0000-0000-0000-000000000030', 'CDN', 3, 'Servir arquivos estáticos a partir do servidor geograficamente mais próximo do usuário, em vez de sempre da origem.');

INSERT INTO roadmap_step_resource (step_id, label, url, position) VALUES
    ('d1000000-0000-0000-0000-000000000031', 'web.dev — Caching', 'https://web.dev/articles/http-cache', 0),
    ('d1000000-0000-0000-0000-000000000032', 'Next.js — Rendering', 'https://nextjs.org/docs/app/building-your-application/rendering', 0),
    ('d1000000-0000-0000-0000-000000000033', 'MDN — CDN', 'https://developer.mozilla.org/en-US/docs/Glossary/CDN', 0);

-- ---- Subetapas de Colaboração em equipe ----
INSERT INTO roadmap_step (id, roadmap_id, parent_step_id, title, position, description) VALUES
    ('d1000000-0000-0000-0000-000000000041', '00000000-0000-0000-0000-000000000040', 'd1000000-0000-0000-0000-000000000040', 'Code review', 1, 'Revisar o código de outra pessoa antes de mesclar — a rede de segurança mais barata contra bug e a forma mais comum de espalhar conhecimento no time.'),
    ('d1000000-0000-0000-0000-000000000042', '00000000-0000-0000-0000-000000000040', 'd1000000-0000-0000-0000-000000000040', 'Convenções de commit', 2, 'Mensagens de commit padronizadas (Conventional Commits) que permitem gerar changelog e versionamento automaticamente.'),
    ('d1000000-0000-0000-0000-000000000043', '00000000-0000-0000-0000-000000000040', 'd1000000-0000-0000-0000-000000000040', 'Documentação de projeto', 3, 'README, ADRs e comentários que explicam o "porquê", não o "o quê" — o que salva a próxima pessoa (ou você mesmo em 6 meses).');

INSERT INTO roadmap_step_resource (step_id, label, url, position) VALUES
    ('d1000000-0000-0000-0000-000000000041', 'Google Engineering Practices — Code Review', 'https://google.github.io/eng-practices/review/', 0),
    ('d1000000-0000-0000-0000-000000000042', 'Conventional Commits', 'https://www.conventionalcommits.org/', 0),
    ('d1000000-0000-0000-0000-000000000043', 'Architecture Decision Records', 'https://adr.github.io/', 0);
