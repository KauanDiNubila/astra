# Astra

App web para organizar estudos e trabalho de forma prática, com um gancho social
(ranking entre amigos) para dar consistência. A unidade fundamental é a **sessão**:
todo tempo focado vira uma sessão registrada e tudo o mais — ranking, heatmap,
streak, estatísticas e progresso — é **calculado por agregação sobre as sessões**,
nunca armazenado como tabela.

> Projeto de portfólio. Interface em português, código em inglês. O primeiro
> usuário é o próprio autor (dogfooding).

## Funcionalidades

- **Autenticação** — registro e login com JWT (senha em BCrypt); tudo isolado por dono.
- **Sessões** — registrar tempo focado (Pomodoro ou manual → minutos) e listar.
- **Categorias** — separar o tempo por tipo (estudo, trabalho, leitura…).
- **Dashboard** — horas de hoje/semana/total, streak e progresso das metas.
- **Heatmap** — minutos por dia (estilo GitHub), no fuso de Brasília.
- **Cursos** — cadastrar curso e módulos, acompanhar progresso (concluídos ÷ total); marcar um módulo inteiro como concluído de uma vez; ligar uma sessão a um curso.
- **Metas** — objetivo de horas diário/semanal (bateu ou não).
- **Roadmaps** — trilhas com etapas ordenadas (próprias + pré-definidas), diagrama visual interativo, e o "pin": pendurar um curso numa etapa.
- **Ranking** — placar diário/semanal/mensal de tempo focado (reseta à meia-noite de Brasília).

> Princípio central: nada de "total", "streak" ou "ranking" é armazenado — tudo é **agregação sobre `session`**.

## Stack

**Backend**

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

**Frontend**

| Camada | Tecnologia |
|---|---|
| Linguagem | TypeScript |
| Framework | React 19 + Vite |
| Roteamento | React Router 7 |
| Estilo | Tailwind CSS 4 + shadcn (Radix UI) |
| Animação | Motion (`motion/react`) |
| HTTP | Axios |
| Ícones | Lucide |
| Notificações | Sonner |
| Lint | oxlint |

## Como rodar (dev)

Pré-requisito: **Java 21**, **Node.js** e **Docker Desktop** ligado.

**Backend**

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

**Frontend**

```bash
cd frontend
npm install
npm run dev
```

- App: `http://localhost:5173`
- Aponta para a API em `http://localhost:8080` por padrão; ajustável via `VITE_API_URL`.

Backend e frontend rodam como dois processos separados — não há orquestração única entre eles.

## Configuração (variáveis de ambiente)

**Backend**

| Variável | Default (dev) | Descrição |
|---|---|---|
| `ASTRA_JWT_SECRET` | um segredo de dev (commitado) | Chave HMAC que assina o JWT. **Em produção, defina um valor aleatório forte** (≥ 32 bytes) — quem tem o segredo forja qualquer token. |
| `ASTRA_CORS_ORIGINS` | `http://localhost:5173,http://localhost:3000` | Origens liberadas no CORS (front-end). |

Ajuste fino em `application.properties`: `astra.jwt.expiration-minutes` (default `1440`).

**Frontend**

| Variável | Default (dev) | Descrição |
|---|---|---|
| `VITE_API_URL` | `http://localhost:8080` | Base URL da API. |

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

**Backend** — monólito modular, **pacote-por-feature**. Cada módulo é um pacote sob
`com.astra` e carrega suas próprias camadas (controller / service / repository /
entity / dto). Regra de fronteira: módulos conversam por *services públicos*, nunca
acessando o repository ou a entity do vizinho — mantendo o grafo de dependências
**sem ciclos**.

```
com.astra
├── user       → autenticação (JWT), perfil
├── tracking   → Session, Category            (o núcleo)
├── learning   → Course, CourseModule, Goal
├── roadmap    → Roadmap, RoadmapStep, CourseStepLink (o "pin")
├── stats      → dashboard, heatmap, streak, ranking (só leitura sobre os domínios)
└── shared     → config, security, exceptions, base
```

**Frontend** — SPA em `frontend/`, uma página por rota sob `src/pages`, componentes
de UI reutilizáveis (shadcn) em `src/components/ui`, e componentes de domínio
(diagrama de roadmap, timer Pomodoro, heatmap etc.) em `src/components`.

## Status

**Backend e frontend completos** — sessões, dashboard, heatmap, cursos/módulos,
metas, roadmaps (com diagrama interativo) e ranking, ponta a ponta.
Em polimento contínuo de UX e correções.
