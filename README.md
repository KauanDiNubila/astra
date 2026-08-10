# Astra

App web para organizar estudos e trabalho de forma prática, com um gancho social
(ranking entre amigos) para dar consistência. A unidade fundamental é a **sessão**:
todo tempo focado vira uma sessão registrada e tudo o mais — ranking, heatmap,
streak, estatísticas e progresso — é **calculado por agregação sobre as sessões**,
nunca armazenado como tabela.

> Projeto de portfólio. Interface em português, código em inglês. O primeiro
> usuário é o próprio autor (dogfooding).

## Funcionalidades (MVP completo)

- **Autenticação** — registro e login com JWT (senha em BCrypt); tudo isolado por dono.
- **Sessões** — registrar tempo focado (Pomodoro ou manual → minutos) e listar.
- **Categorias** — separar o tempo por tipo (estudo, trabalho, leitura…).
- **Dashboard** — horas de hoje/semana/total, streak e progresso das metas.
- **Heatmap** — minutos por dia (estilo GitHub), no fuso de Brasília.
- **Cursos** — cadastrar curso e módulos, acompanhar progresso (concluídos ÷ total); ligar uma sessão a um curso.
- **Metas** — objetivo de horas diário/semanal (bateu ou não).
- **Roadmaps** — trilhas com etapas ordenadas (próprias + pré-definidas) e o "pin": pendurar um curso numa etapa, com status e nota (1-5).
- **Ranking** — placar diário/semanal/mensal de tempo focado (reseta à meia-noite de Brasília).

> Princípio central: nada de "total", "streak" ou "ranking" é armazenado — tudo é **agregação sobre `session`**.

## Stack

| Camada | Tecnologia |
|---|---|
| Linguagem | Java 21 (LTS) |
| Framework | Spring Boot 4.0.7 (sobre Spring Framework 7) |
| Build | Maven |
| Banco | PostgreSQL 17 |
| Migrations | Flyway |
| Persistência | Spring Data JPA / Hibernate |
| Segurança | Spring Security + JWT (jjwt) |
| Validação | Bean Validation |
| Docs de API | SpringDoc OpenAPI (Swagger UI) |
| Testes | JUnit 5, Testcontainers (Postgres real) |
| Dev local | Docker Compose (Postgres) |
| Utilitário | Lombok |
| CI | GitHub Actions (`mvnw verify` a cada PR) |

## Como rodar (dev)

Pré-requisito: **Java 21** e **Docker Desktop** ligado.

```bash
./mvnw spring-boot:run
```

O `spring-boot-docker-compose` sobe o Postgres do [`compose.yaml`](compose.yaml)
automaticamente e conecta a aplicação — não é preciso configurar datasource à mão.

- API: `http://localhost:8080`
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- Banco (dev): `localhost:5433`, db/user/senha `astra`

Testes (também usam Docker, via Testcontainers):

```bash
./mvnw test
```

## Configuração (variáveis de ambiente)

| Variável | Default (dev) | Descrição |
|---|---|---|
| `ASTRA_JWT_SECRET` | um segredo de dev (commitado) | Chave HMAC que assina o JWT. **Em produção, defina um valor aleatório forte** (≥ 32 bytes) — quem tem o segredo forja qualquer token. |
| `ASTRA_CORS_ORIGINS` | `http://localhost:5173,http://localhost:3000` | Origens liberadas no CORS (front-end). |

Ajuste fino em `application.properties`: `astra.jwt.expiration-minutes` (default `1440`).

> **Antes de produção:** servir por **HTTPS** e definir `ASTRA_JWT_SECRET`. Sem isso, o token trafega/valida de forma insegura.

## API

Documentação interativa completa no **Swagger UI** (`/swagger-ui.html`). Grupos principais:

- **Auth:** `POST /auth/register`, `POST /auth/login`, `GET /me`
- **Tracking:** `POST`/`GET /sessions`, `POST`/`GET /categories`
- **Stats:** `GET /dashboard`, `GET /heatmap`, `GET /ranking?period=DAILY|WEEKLY|MONTHLY`
- **Learning:** `POST`/`GET /courses`, `GET /courses/{id}`, `POST .../modules`, `PATCH .../modules/{id}`, `PUT`/`GET /goals`
- **Roadmap:** `POST`/`GET /roadmaps`, `GET /roadmaps/{id}`, `POST .../steps`, `POST`/`GET /steps/{id}/pins`

Tudo (exceto `/auth/**` e o Swagger) exige `Authorization: Bearer <token>`. Erros seguem um shape padrão (`timestamp, status, error, message, path, fieldErrors`).

## Arquitetura

Monólito modular, **pacote-por-feature**. Cada módulo é um pacote sob `com.astra` e
carrega suas próprias camadas (controller / service / repository / entity / dto).
Regra de fronteira: módulos conversam por *services públicos*, nunca acessando o
repository ou a entity do vizinho — mantendo o grafo de dependências **sem ciclos**.

```
com.astra
├── user       → autenticação (JWT), perfil
├── tracking   → Session, Category            (o núcleo)
├── learning   → Course, CourseModule, Goal
├── roadmap    → Roadmap, RoadmapStep, CourseStepLink (o "pin")
├── stats      → dashboard, heatmap, streak, ranking (só leitura sobre os domínios)
└── shared     → config, security, exceptions, base
```

## Contribuindo

Ver [CONTRIBUTING.md](CONTRIBUTING.md) — fluxo de branches, Conventional Commits, issues e labels.

## Status

**Backend: MVP completo** (das sessões ao ranking, tudo por agregação sobre `session`).
Próximo: **front-end** (React + Vite + TypeScript) e o deploy.
