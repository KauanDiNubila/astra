-- Conteúdo autoral (descrição + links curados) do roadmap "Backend Java",
-- como piloto da funcionalidade de descrição/links por etapa. Os outros 3
-- roadmaps predefinidos (Frontend, DevOps, Full-Stack Web) continuam sem
-- conteúdo por enquanto — degradam normalmente (description/resources vazios).
--
-- As 4 etapas principais têm id fixo (seedadas em V4__seed_roadmaps.sql).
-- As 12 subetapas (seedadas em V6__seed_roadmap_topics.sql) NÃO têm id fixo
-- (roadmap_step.id usa DEFAULT gen_random_uuid()), então são localizadas por
-- roadmap_id + title em vez de id.

-- ===== Fundamentos de Java =====

UPDATE roadmap_step
SET description = 'Base da linguagem: sintaxe, tipos, orientação a objetos, coleções e tratamento de exceções. É o alicerce para qualquer coisa que venha depois, incluindo Spring.'
WHERE id = '00000000-0000-0000-0000-000000000011';

INSERT INTO roadmap_step_resource (step_id, label, url, position) VALUES
    ('00000000-0000-0000-0000-000000000011', 'Java Tutorials (Oracle)', 'https://docs.oracle.com/javase/tutorial/', 0),
    ('00000000-0000-0000-0000-000000000011', 'dev.java — guias oficiais', 'https://dev.java/learn/', 1);

UPDATE roadmap_step
SET description = 'Classes, objetos, herança, interfaces e os quatro pilares da orientação a objetos (encapsulamento, herança, polimorfismo, abstração) aplicados em Java.'
WHERE roadmap_id = '00000000-0000-0000-0000-000000000010' AND title = 'Sintaxe e orientação a objetos';

INSERT INTO roadmap_step_resource (step_id, label, url, position)
SELECT id, 'Classes and Objects (Oracle Trail)', 'https://docs.oracle.com/javase/tutorial/java/javaOO/index.html', 0
FROM roadmap_step WHERE roadmap_id = '00000000-0000-0000-0000-000000000010' AND title = 'Sintaxe e orientação a objetos';

UPDATE roadmap_step
SET description = 'List, Set, Map e a Collections Framework, além da Streams API para processar coleções de forma declarativa (filter, map, reduce).'
WHERE roadmap_id = '00000000-0000-0000-0000-000000000010' AND title = 'Coleções e streams';

INSERT INTO roadmap_step_resource (step_id, label, url, position)
SELECT id, 'Collections Framework (Oracle Trail)', 'https://docs.oracle.com/javase/tutorial/collections/index.html', 0
FROM roadmap_step WHERE roadmap_id = '00000000-0000-0000-0000-000000000010' AND title = 'Coleções e streams'
UNION ALL
SELECT id, 'Java Stream API (Baeldung)', 'https://www.baeldung.com/java-streams', 1
FROM roadmap_step WHERE roadmap_id = '00000000-0000-0000-0000-000000000010' AND title = 'Coleções e streams';

UPDATE roadmap_step
SET description = 'checked vs unchecked exceptions, try-with-resources e boas práticas para não engolir erros nem estourar stack traces genéricas em produção.'
WHERE roadmap_id = '00000000-0000-0000-0000-000000000010' AND title = 'Tratamento de exceções';

INSERT INTO roadmap_step_resource (step_id, label, url, position)
SELECT id, 'Exceptions (Oracle Trail)', 'https://docs.oracle.com/javase/tutorial/essential/exceptions/index.html', 0
FROM roadmap_step WHERE roadmap_id = '00000000-0000-0000-0000-000000000010' AND title = 'Tratamento de exceções';

-- ===== Spring Boot =====

UPDATE roadmap_step
SET description = 'Framework que reduz a configuração manual do Spring com auto-configuração e starters. É o padrão de mercado para APIs Java hoje em dia.'
WHERE id = '00000000-0000-0000-0000-000000000012';

INSERT INTO roadmap_step_resource (step_id, label, url, position) VALUES
    ('00000000-0000-0000-0000-000000000012', 'Spring Boot Reference Docs', 'https://docs.spring.io/spring-boot/index.html', 0);

UPDATE roadmap_step
SET description = 'Inversão de controle: em vez de suas classes criarem suas próprias dependências, o Spring injeta o que for preciso via construtor. Facilita testes e desacopla o código.'
WHERE roadmap_id = '00000000-0000-0000-0000-000000000010' AND title = 'Injeção de dependência';

INSERT INTO roadmap_step_resource (step_id, label, url, position)
SELECT id, 'Spring Framework — IoC Container', 'https://docs.spring.io/spring-framework/reference/core/beans.html', 0
FROM roadmap_step WHERE roadmap_id = '00000000-0000-0000-0000-000000000010' AND title = 'Injeção de dependência';

UPDATE roadmap_step
SET description = 'Camada que mapeia requisições HTTP para métodos Java (@RestController, @GetMapping etc.) e serializa as respostas, normalmente em JSON.'
WHERE roadmap_id = '00000000-0000-0000-0000-000000000010' AND title = 'Spring MVC';

INSERT INTO roadmap_step_resource (step_id, label, url, position)
SELECT id, 'Spring Web MVC — Reference Docs', 'https://docs.spring.io/spring-framework/reference/web/webmvc.html', 0
FROM roadmap_step WHERE roadmap_id = '00000000-0000-0000-0000-000000000010' AND title = 'Spring MVC';

UPDATE roadmap_step
SET description = 'Abstração sobre JPA/Hibernate que gera repositórios (CRUD, queries derivadas do nome do método) sem precisar escrever SQL manualmente na maioria dos casos.'
WHERE roadmap_id = '00000000-0000-0000-0000-000000000010' AND title = 'Spring Data JPA';

INSERT INTO roadmap_step_resource (step_id, label, url, position)
SELECT id, 'Spring Data JPA — Reference Docs', 'https://docs.spring.io/spring-data/jpa/reference/', 0
FROM roadmap_step WHERE roadmap_id = '00000000-0000-0000-0000-000000000010' AND title = 'Spring Data JPA';

-- ===== Banco de dados e SQL =====

UPDATE roadmap_step
SET description = 'Modelagem, normalização e SQL — a base que sustenta qualquer backend que precise persistir dados de forma confiável.'
WHERE id = '00000000-0000-0000-0000-000000000013';

INSERT INTO roadmap_step_resource (step_id, label, url, position) VALUES
    ('00000000-0000-0000-0000-000000000013', 'PostgreSQL Documentation', 'https://www.postgresql.org/docs/current/', 0);

UPDATE roadmap_step
SET description = 'Como desenhar tabelas, chaves primárias/estrangeiras e relacionamentos (1:1, 1:N, N:N) evitando dados duplicados ou inconsistentes.'
WHERE roadmap_id = '00000000-0000-0000-0000-000000000010' AND title = 'Modelagem relacional';

INSERT INTO roadmap_step_resource (step_id, label, url, position)
SELECT id, 'Database Design Basics (PostgreSQL Tutorial)', 'https://www.postgresql.org/docs/current/tutorial-fk.html', 0
FROM roadmap_step WHERE roadmap_id = '00000000-0000-0000-0000-000000000010' AND title = 'Modelagem relacional';

UPDATE roadmap_step
SET description = 'Joins, subqueries, window functions e índices — o que separa uma query que funciona de uma que funciona rápido em produção.'
WHERE roadmap_id = '00000000-0000-0000-0000-000000000010' AND title = 'SQL avançado';

INSERT INTO roadmap_step_resource (step_id, label, url, position)
SELECT id, 'Window Functions (PostgreSQL Docs)', 'https://www.postgresql.org/docs/current/tutorial-window.html', 0
FROM roadmap_step WHERE roadmap_id = '00000000-0000-0000-0000-000000000010' AND title = 'SQL avançado'
UNION ALL
SELECT id, 'Indexes (PostgreSQL Docs)', 'https://www.postgresql.org/docs/current/indexes.html', 1
FROM roadmap_step WHERE roadmap_id = '00000000-0000-0000-0000-000000000010' AND title = 'SQL avançado';

UPDATE roadmap_step
SET description = 'Versionamento de schema de banco como código: cada mudança vira um arquivo SQL numerado e reproduzível, em vez de alterações manuais direto na base. É como este projeto gerencia seu próprio schema.'
WHERE roadmap_id = '00000000-0000-0000-0000-000000000010' AND title = 'Migrations com Flyway';

INSERT INTO roadmap_step_resource (step_id, label, url, position)
SELECT id, 'Flyway Documentation', 'https://documentation.red-gate.com/flyway', 0
FROM roadmap_step WHERE roadmap_id = '00000000-0000-0000-0000-000000000010' AND title = 'Migrations com Flyway';

-- ===== APIs REST e segurança =====

UPDATE roadmap_step
SET description = 'Como expor os dados da aplicação de forma consistente e segura pela web: convenções REST, autenticação e boas práticas de design de API.'
WHERE id = '00000000-0000-0000-0000-000000000014';

INSERT INTO roadmap_step_resource (step_id, label, url, position) VALUES
    ('00000000-0000-0000-0000-000000000014', 'REST API Tutorial', 'https://restfulapi.net/', 0);

UPDATE roadmap_step
SET description = 'Verbos HTTP corretos, status codes, nomeação de recursos no plural e versionamento — convenções que tornam uma API previsível para quem consome.'
WHERE roadmap_id = '00000000-0000-0000-0000-000000000010' AND title = 'Design de APIs REST';

INSERT INTO roadmap_step_resource (step_id, label, url, position)
SELECT id, 'HTTP Methods (restfulapi.net)', 'https://restfulapi.net/http-methods/', 0
FROM roadmap_step WHERE roadmap_id = '00000000-0000-0000-0000-000000000010' AND title = 'Design de APIs REST';

UPDATE roadmap_step
SET description = 'Autenticação sem estado no servidor: o token carrega as informações do usuário assinadas digitalmente, validado a cada requisição. É o que este próprio projeto usa para login.'
WHERE roadmap_id = '00000000-0000-0000-0000-000000000010' AND title = 'Autenticação com JWT';

INSERT INTO roadmap_step_resource (step_id, label, url, position)
SELECT id, 'Introduction to JSON Web Tokens', 'https://jwt.io/introduction', 0
FROM roadmap_step WHERE roadmap_id = '00000000-0000-0000-0000-000000000010' AND title = 'Autenticação com JWT';

UPDATE roadmap_step
SET description = 'Testes unitários (JUnit, Mockito) e testes de integração que sobem o contexto Spring de verdade — a rede de segurança que permite refatorar sem medo.'
WHERE roadmap_id = '00000000-0000-0000-0000-000000000010' AND title = 'Testes automatizados';

INSERT INTO roadmap_step_resource (step_id, label, url, position)
SELECT id, 'Testing (Spring Boot Reference Docs)', 'https://docs.spring.io/spring-boot/reference/testing/index.html', 0
FROM roadmap_step WHERE roadmap_id = '00000000-0000-0000-0000-000000000010' AND title = 'Testes automatizados'
UNION ALL
SELECT id, 'JUnit 5 User Guide', 'https://docs.junit.org/current/user-guide/', 1
FROM roadmap_step WHERE roadmap_id = '00000000-0000-0000-0000-000000000010' AND title = 'Testes automatizados';
