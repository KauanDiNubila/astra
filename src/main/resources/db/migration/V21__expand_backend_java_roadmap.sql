-- Expande o roadmap "Backend Java" com mais etapas, inspirado na cobertura
-- de tópicos do roadmap.sh (Backend Developer). Textos e curadoria de links
-- são autorais deste projeto, não uma cópia do conteúdo de lá — só a lista
-- de assuntos/organização serviu de referência.
--
-- Mantém as 4 etapas principais existentes e seus 12 filhos (não altera
-- nada de V4/V6/V20). Adiciona:
--   - 2 novos filhos em cada uma das 4 etapas principais existentes (8 no total)
--   - 6 novas etapas principais, cada uma com 3-4 subetapas (25 no total)

-- ===== Mais filhos nas etapas principais existentes =====

INSERT INTO roadmap_step (id, roadmap_id, parent_step_id, title, position, description) VALUES
    ('a1000000-0000-0000-0000-000000000001', '00000000-0000-0000-0000-000000000010', '00000000-0000-0000-0000-000000000011', 'Generics e tipos genéricos', 4, 'Classes e métodos parametrizados por tipo (List<T>, Map<K,V>), permitindo código reutilizável com segurança de tipos em tempo de compilação.'),
    ('a1000000-0000-0000-0000-000000000002', '00000000-0000-0000-0000-000000000010', '00000000-0000-0000-0000-000000000011', 'Concorrência (Threads e Executors)', 5, 'Threads, ExecutorService, sincronização e as armadilhas de condições de corrida — essencial pra qualquer backend que atenda requisições em paralelo.'),
    ('a1000000-0000-0000-0000-000000000003', '00000000-0000-0000-0000-000000000010', '00000000-0000-0000-0000-000000000012', 'Spring Security', 4, 'Autenticação e autorização declarativas: filtros de segurança, roles/permissions e integração com JWT ou sessões.'),
    ('a1000000-0000-0000-0000-000000000004', '00000000-0000-0000-0000-000000000010', '00000000-0000-0000-0000-000000000012', 'Configuração externa (profiles, application.yml)', 5, 'Separar configuração de código: profiles por ambiente (dev/prod), variáveis de ambiente e application.yml em vez de valores hardcoded.'),
    ('a1000000-0000-0000-0000-000000000005', '00000000-0000-0000-0000-000000000010', '00000000-0000-0000-0000-000000000013', 'Transações e ACID', 4, 'Atomicidade, consistência, isolamento e durabilidade — e como @Transactional do Spring controla commit/rollback automaticamente.'),
    ('a1000000-0000-0000-0000-000000000006', '00000000-0000-0000-0000-000000000010', '00000000-0000-0000-0000-000000000013', 'Connection pooling', 5, 'Por que abrir uma conexão nova a cada query é caro, e como um pool (HikariCP, padrão no Spring Boot) reaproveita conexões existentes.'),
    ('a1000000-0000-0000-0000-000000000007', '00000000-0000-0000-0000-000000000010', '00000000-0000-0000-0000-000000000014', 'Versionamento de API', 4, 'Estratégias pra evoluir uma API sem quebrar clientes existentes: versionar por URL, header ou content negotiation.'),
    ('a1000000-0000-0000-0000-000000000008', '00000000-0000-0000-0000-000000000010', '00000000-0000-0000-0000-000000000014', 'Documentação com OpenAPI/Swagger', 5, 'Gerar documentação interativa da API automaticamente a partir do código, pra quem consome saber o que existe sem precisar perguntar.');

INSERT INTO roadmap_step_resource (step_id, label, url, position) VALUES
    ('a1000000-0000-0000-0000-000000000001', 'Generics (Oracle Trail)', 'https://docs.oracle.com/javase/tutorial/java/generics/index.html', 0),
    ('a1000000-0000-0000-0000-000000000002', 'Concurrency (Oracle Trail)', 'https://docs.oracle.com/javase/tutorial/essential/concurrency/index.html', 0),
    ('a1000000-0000-0000-0000-000000000003', 'Spring Security Reference', 'https://docs.spring.io/spring-security/reference/', 0),
    ('a1000000-0000-0000-0000-000000000004', 'Externalized Configuration (Spring Boot Docs)', 'https://docs.spring.io/spring-boot/reference/features/external-config.html', 0),
    ('a1000000-0000-0000-0000-000000000005', 'Transaction Management (Spring Docs)', 'https://docs.spring.io/spring-framework/reference/data-access/transaction.html', 0),
    ('a1000000-0000-0000-0000-000000000006', 'HikariCP (GitHub)', 'https://github.com/brettwooldridge/HikariCP', 0),
    ('a1000000-0000-0000-0000-000000000007', 'API Versioning (restfulapi.net)', 'https://restfulapi.net/versioning/', 0),
    ('a1000000-0000-0000-0000-000000000008', 'springdoc-openapi', 'https://springdoc.org/', 0);

-- ===== Novas etapas principais =====

INSERT INTO roadmap_step (id, roadmap_id, parent_step_id, title, position, description) VALUES
    ('a1000000-0000-0000-0000-000000000010', '00000000-0000-0000-0000-000000000010', NULL, 'Cache', 5, 'Guardar resultados caros de recalcular ou buscar em memória rápida, reduzindo carga no banco e latência de resposta.'),
    ('a1000000-0000-0000-0000-000000000020', '00000000-0000-0000-0000-000000000010', NULL, 'Mensageria', 6, 'Comunicação assíncrona entre serviços via filas/tópicos, desacoplando quem produz um evento de quem processa.'),
    ('a1000000-0000-0000-0000-000000000030', '00000000-0000-0000-0000-000000000010', NULL, 'Observabilidade', 7, 'Enxergar o que está acontecendo dentro de um sistema em produção: logs, métricas e rastreamento de requisições.'),
    ('a1000000-0000-0000-0000-000000000040', '00000000-0000-0000-0000-000000000010', NULL, 'Containers e deploy', 8, 'Empacotar a aplicação com suas dependências de forma reproduzível e automatizar o caminho até produção.'),
    ('a1000000-0000-0000-0000-000000000050', '00000000-0000-0000-0000-000000000010', NULL, 'Design de sistemas', 9, 'Decisões de arquitetura que importam quando o sistema cresce: como escalar, dividir responsabilidades e lidar com falhas.'),
    ('a1000000-0000-0000-0000-000000000060', '00000000-0000-0000-0000-000000000010', NULL, 'Segurança avançada', 10, 'Riscos comuns de aplicações web e como mitigá-los, além do básico de autenticação já visto em APIs REST e segurança.');

INSERT INTO roadmap_step_resource (step_id, label, url, position) VALUES
    ('a1000000-0000-0000-0000-000000000010', 'Caching Strategies (AWS)', 'https://aws.amazon.com/caching/best-practices/', 0),
    ('a1000000-0000-0000-0000-000000000020', 'Enterprise Integration Patterns — Messaging', 'https://www.enterpriseintegrationpatterns.com/patterns/messaging/', 0),
    ('a1000000-0000-0000-0000-000000000030', 'Observability (OpenTelemetry Docs)', 'https://opentelemetry.io/docs/concepts/observability-primer/', 0),
    ('a1000000-0000-0000-0000-000000000040', 'Docker Get Started', 'https://docs.docker.com/get-started/', 0),
    ('a1000000-0000-0000-0000-000000000050', 'System Design Primer (GitHub)', 'https://github.com/donnemartin/system-design-primer', 0),
    ('a1000000-0000-0000-0000-000000000060', 'OWASP Top 10', 'https://owasp.org/www-project-top-ten/', 0);

-- ---- Subetapas de Cache ----
INSERT INTO roadmap_step (id, roadmap_id, parent_step_id, title, position, description) VALUES
    ('a1000000-0000-0000-0000-000000000011', '00000000-0000-0000-0000-000000000010', 'a1000000-0000-0000-0000-000000000010', 'Redis', 1, 'Banco de dados em memória usado como cache, fila e muito mais — provavelmente a ferramenta de cache mais usada no mercado hoje.'),
    ('a1000000-0000-0000-0000-000000000012', '00000000-0000-0000-0000-000000000010', 'a1000000-0000-0000-0000-000000000010', 'Memcached', 2, 'Cache em memória mais simples que o Redis, focado só em guardar pares chave-valor de forma distribuída.'),
    ('a1000000-0000-0000-0000-000000000013', '00000000-0000-0000-0000-000000000010', 'a1000000-0000-0000-0000-000000000010', 'Estratégias de invalidação de cache', 3, 'Cache-aside, write-through, TTL — as diferentes formas de manter o cache coerente com a fonte de dados real.');

INSERT INTO roadmap_step_resource (step_id, label, url, position) VALUES
    ('a1000000-0000-0000-0000-000000000011', 'Redis Documentation', 'https://redis.io/docs/latest/', 0),
    ('a1000000-0000-0000-0000-000000000012', 'Memcached Wiki', 'https://github.com/memcached/memcached/wiki', 0),
    ('a1000000-0000-0000-0000-000000000013', 'Caching Patterns (AWS)', 'https://aws.amazon.com/caching/best-practices/', 0);

-- ---- Subetapas de Mensageria ----
INSERT INTO roadmap_step (id, roadmap_id, parent_step_id, title, position, description) VALUES
    ('a1000000-0000-0000-0000-000000000021', '00000000-0000-0000-0000-000000000010', 'a1000000-0000-0000-0000-000000000020', 'Filas vs pub/sub', 1, 'Diferença entre uma fila (um consumidor processa cada mensagem) e publish/subscribe (vários assinantes recebem o mesmo evento).'),
    ('a1000000-0000-0000-0000-000000000022', '00000000-0000-0000-0000-000000000010', 'a1000000-0000-0000-0000-000000000020', 'RabbitMQ', 2, 'Message broker tradicional baseado em filas, com roteamento flexível via exchanges.'),
    ('a1000000-0000-0000-0000-000000000023', '00000000-0000-0000-0000-000000000010', 'a1000000-0000-0000-0000-000000000020', 'Kafka', 3, 'Plataforma de streaming de eventos de alta vazão, pensada pra reter e reprocessar grandes volumes de mensagens.');

INSERT INTO roadmap_step_resource (step_id, label, url, position) VALUES
    ('a1000000-0000-0000-0000-000000000021', 'Enterprise Integration Patterns', 'https://www.enterpriseintegrationpatterns.com/patterns/messaging/', 0),
    ('a1000000-0000-0000-0000-000000000022', 'RabbitMQ Documentation', 'https://www.rabbitmq.com/docs', 0),
    ('a1000000-0000-0000-0000-000000000023', 'Apache Kafka Documentation', 'https://kafka.apache.org/documentation/', 0);

-- ---- Subetapas de Observabilidade ----
INSERT INTO roadmap_step (id, roadmap_id, parent_step_id, title, position, description) VALUES
    ('a1000000-0000-0000-0000-000000000031', '00000000-0000-0000-0000-000000000010', 'a1000000-0000-0000-0000-000000000030', 'Logging estruturado', 1, 'Logs em formato consistente (JSON, chave-valor) em vez de texto livre, pra serem filtrados e agregados por ferramentas depois.'),
    ('a1000000-0000-0000-0000-000000000032', '00000000-0000-0000-0000-000000000010', 'a1000000-0000-0000-0000-000000000030', 'Métricas e monitoramento', 2, 'Números que descrevem a saúde do sistema ao longo do tempo (latência, taxa de erro, throughput) — a base de qualquer dashboard e alerta.'),
    ('a1000000-0000-0000-0000-000000000033', '00000000-0000-0000-0000-000000000010', 'a1000000-0000-0000-0000-000000000030', 'Tracing distribuído', 3, 'Seguir uma requisição através de vários serviços, essencial pra debugar latência em arquiteturas distribuídas.');

INSERT INTO roadmap_step_resource (step_id, label, url, position) VALUES
    ('a1000000-0000-0000-0000-000000000031', 'Structured Logging (12factor.net)', 'https://12factor.net/logs', 0),
    ('a1000000-0000-0000-0000-000000000032', 'Micrometer (Spring metrics)', 'https://docs.micrometer.io/micrometer/reference/', 0),
    ('a1000000-0000-0000-0000-000000000033', 'OpenTelemetry — Tracing', 'https://opentelemetry.io/docs/concepts/signals/traces/', 0);

-- ---- Subetapas de Containers e deploy ----
INSERT INTO roadmap_step (id, roadmap_id, parent_step_id, title, position, description) VALUES
    ('a1000000-0000-0000-0000-000000000041', '00000000-0000-0000-0000-000000000010', 'a1000000-0000-0000-0000-000000000040', 'Docker', 1, 'Empacotar a aplicação e suas dependências numa imagem que roda igual em qualquer máquina — dev, CI ou produção.'),
    ('a1000000-0000-0000-0000-000000000042', '00000000-0000-0000-0000-000000000010', 'a1000000-0000-0000-0000-000000000040', 'Kubernetes (conceitos básicos)', 2, 'Orquestrador que gerencia múltiplos containers em produção: reinicia o que cai, distribui carga, escala réplicas.'),
    ('a1000000-0000-0000-0000-000000000043', '00000000-0000-0000-0000-000000000010', 'a1000000-0000-0000-0000-000000000040', 'CI/CD', 3, 'Automatizar build, testes e deploy a cada mudança de código, em vez de fazer esses passos manualmente.');

INSERT INTO roadmap_step_resource (step_id, label, url, position) VALUES
    ('a1000000-0000-0000-0000-000000000041', 'Docker Get Started', 'https://docs.docker.com/get-started/', 0),
    ('a1000000-0000-0000-0000-000000000042', 'Kubernetes Concepts', 'https://kubernetes.io/docs/concepts/', 0),
    ('a1000000-0000-0000-0000-000000000043', 'CI/CD (Red Hat Topics)', 'https://www.redhat.com/en/topics/devops/what-is-ci-cd', 0);

-- ---- Subetapas de Design de sistemas ----
INSERT INTO roadmap_step (id, roadmap_id, parent_step_id, title, position, description) VALUES
    ('a1000000-0000-0000-0000-000000000051', '00000000-0000-0000-0000-000000000010', 'a1000000-0000-0000-0000-000000000050', 'Escalabilidade horizontal vs vertical', 1, 'Escalar verticalmente (máquina maior) versus horizontalmente (mais máquinas) — e por que a segunda é o padrão pra sistemas grandes.'),
    ('a1000000-0000-0000-0000-000000000052', '00000000-0000-0000-0000-000000000010', 'a1000000-0000-0000-0000-000000000050', 'Microsserviços', 2, 'Dividir uma aplicação em serviços pequenos e independentes — os ganhos (deploy isolado, escala seletiva) e os custos (complexidade operacional).'),
    ('a1000000-0000-0000-0000-000000000053', '00000000-0000-0000-0000-000000000010', 'a1000000-0000-0000-0000-000000000050', 'CAP Theorem', 3, 'Num sistema distribuído, não dá pra ter consistência, disponibilidade e tolerância a partição ao mesmo tempo — só duas das três.'),
    ('a1000000-0000-0000-0000-000000000054', '00000000-0000-0000-0000-000000000010', 'a1000000-0000-0000-0000-000000000050', 'Circuit breaker e resiliência', 4, 'Padrão pra parar de chamar um serviço que está falhando, evitando que uma falha em cascata derrube o sistema inteiro.');

INSERT INTO roadmap_step_resource (step_id, label, url, position) VALUES
    ('a1000000-0000-0000-0000-000000000051', 'System Design Primer — Scalability', 'https://github.com/donnemartin/system-design-primer#scalability', 0),
    ('a1000000-0000-0000-0000-000000000052', 'Microservices (Martin Fowler)', 'https://martinfowler.com/articles/microservices.html', 0),
    ('a1000000-0000-0000-0000-000000000053', 'CAP Theorem (IBM)', 'https://www.ibm.com/topics/cap-theorem', 0),
    ('a1000000-0000-0000-0000-000000000054', 'CircuitBreaker (Martin Fowler)', 'https://martinfowler.com/bliki/CircuitBreaker.html', 0);

-- ---- Subetapas de Segurança avançada ----
INSERT INTO roadmap_step (id, roadmap_id, parent_step_id, title, position, description) VALUES
    ('a1000000-0000-0000-0000-000000000061', '00000000-0000-0000-0000-000000000010', 'a1000000-0000-0000-0000-000000000060', 'OWASP Top 10', 1, 'Lista das vulnerabilidades mais críticas e recorrentes em aplicações web (injeção, quebra de autenticação, etc.), mantida pela OWASP.'),
    ('a1000000-0000-0000-0000-000000000062', '00000000-0000-0000-0000-000000000010', 'a1000000-0000-0000-0000-000000000060', 'HTTPS e TLS', 2, 'Como o tráfego entre cliente e servidor é criptografado, e por que HTTPS deixou de ser opcional há anos.'),
    ('a1000000-0000-0000-0000-000000000063', '00000000-0000-0000-0000-000000000010', 'a1000000-0000-0000-0000-000000000060', 'Hashing de senhas (bcrypt)', 3, 'Nunca guardar senha em texto puro: bcrypt (ou Argon2) faz um hash lento e salgado, resistente a ataques de força bruta.');

INSERT INTO roadmap_step_resource (step_id, label, url, position) VALUES
    ('a1000000-0000-0000-0000-000000000061', 'OWASP Top 10', 'https://owasp.org/www-project-top-ten/', 0),
    ('a1000000-0000-0000-0000-000000000062', 'TLS Basics (Cloudflare Learning)', 'https://www.cloudflare.com/learning/ssl/transport-layer-security-tls/', 0),
    ('a1000000-0000-0000-0000-000000000063', 'bcrypt (Spring Security Docs)', 'https://docs.spring.io/spring-security/reference/features/authentication/password-storage.html', 0);
