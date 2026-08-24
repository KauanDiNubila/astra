# Astra

[![CI](https://github.com/KauanDiNubila/astra/actions/workflows/ci.yml/badge.svg?branch=development)](https://github.com/KauanDiNubila/astra/actions/workflows/ci.yml)

App web para organizar estudos e trabalho de forma prática, com um gancho social
(amigos, chat, ranking) para dar consistência. A unidade fundamental é a **sessão**:
todo tempo focado vira uma sessão registrada e tudo o mais — ranking, heatmap,
streak, estatísticas e progresso — é **calculado por agregação sobre as sessões**,
nunca armazenado como tabela.

**🔗 No ar:** [astra-eight-sigma.vercel.app](https://astra-eight-sigma.vercel.app)

> Projeto de portfólio. Interface em português, código em inglês.

## Funcionalidades

- **Autenticação** — registro e login com JWT de acesso curto (15min) + refresh
  token opaco em cookie `httpOnly` (rotação a cada uso, reuso detectado revoga
  a sessão inteira); senha em BCrypt, recusa senha já vazada (consulta ao Have
  I Been Pwned por k-anonimato).
- **Perfil** — nome, bio curta e avatar.
- **Sessões** — registrar tempo focado (Pomodoro ou manual → minutos) e listar.
- **Categorias** — separar o tempo por tipo (estudo, trabalho, leitura…).
- **Dashboard** — horas de hoje/semana/total, streak e progresso das metas.
- **Heatmap** — minutos por dia (estilo GitHub), no fuso de Brasília.
- **Cursos** — cadastrar curso e módulos, acompanhar progresso (concluídos ÷ total); marcar um módulo inteiro como concluído de uma vez; ligar uma sessão a um curso.
- **Metas** — objetivo de horas diário/semanal (bateu ou não).
- **Roadmaps** — trilhas com etapas ordenadas (próprias + pré-definidas), diagrama visual interativo, e o "pin": pendurar um curso numa etapa. Conclusão de etapa é por-usuário mesmo em roadmaps compartilhados.
- **Amigos** — pedido de amizade por e-mail, aceitar/recusar/remover.
- **Chat** — mensagens em tempo real entre amigos, via WebSocket (STOMP).
- **Ranking** — placar diário/semanal/mensal, global ou só entre amigos (reseta à meia-noite de Brasília).
- **Administração** — painel pra listar, banir (reversível) ou excluir (cascata) contas; acesso restrito a `ROLE_ADMIN`.

> Princípio central: nada de "total", "streak" ou "ranking" é armazenado — tudo é **agregação sobre `session`**.

## Segurança

- JWT de acesso (15min) + refresh token opaco (30 dias, hash SHA-256 no banco,
  rotação a cada uso, família inteira revogada se um token já usado reaparecer)
  — refresh token nunca trafega em JSON, só via cookie `httpOnly` + `Secure` +
  `SameSite=None`.
- RBAC (`USER`/`ADMIN`), reavaliado a cada request (ban tem efeito imediato).
- `Content-Security-Policy` restritiva no frontend (`script-src 'self'`, sem
  inline/eval) — bloqueia script injetado de rodar, não só protege onde o
  token mora.
- Rate limit no login (por IP) + rate limit de borda no Cloudflare (por IP,
  todos os endpoints).
- Autorização por dono verificada em todo endpoint com id (auditoria manual +
  scan automatizado com OWASP ZAP, sem falha encontrada).
- Sem SQL Injection (100% Spring Data JPA parametrizado, incluindo a única
  query nativa do projeto).
- `HTTPS` de ponta a ponta (Let's Encrypt na origem, Cloudflare na borda).

Detalhes completos (modelo de ameaças, decisões e trade-offs documentados) na
pasta de notas do projeto — não faz parte deste repositório público.

## Stack

**Backend**

| Camada | Tecnologia |
|---|---|
| Linguagem | Java 21 (LTS) |
| Framework | Spring Boot 4.0.7 (sobre Spring Framework 7) |
| Build | Maven |
| Banco | PostgreSQL 18 |
| Migrations | Flyway |
| Persistência | Spring Data JPA / Hibernate |
| Segurança | Spring Security + JWT (jjwt) + refresh token |
| Tempo real | WebSocket + STOMP (chat) |
| Validação | Bean Validation |
| Docs de API | SpringDoc OpenAPI (Swagger UI, só em dev) |
| Testes | JUnit 5, Testcontainers (Postgres real) |
| Dev local | Docker Compose (Postgres) |
| Utilitário | Lombok |
| CI | GitHub Actions (`mvnw verify` a cada push) |

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

**Produção**

| Peça | Onde |
|---|---|
| Frontend | Vercel (deploy automático a cada push) |
| Backend | VM Oracle Cloud (Docker Compose + Caddy) |
| Banco | Neon (Postgres serverless) |
| Borda | Cloudflare (proxy, HTTPS, rate limit) |
| Domínio | `astra-app.dev` |

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

**Backend** (dev usa `application.properties`, produção ativa o profile `prod`
via `SPRING_PROFILES_ACTIVE=prod` — nesse profile as variáveis abaixo **não
têm valor default**, a aplicação falha na subida se faltar alguma)

| Variável | Default (dev) | Descrição |
|---|---|---|
| `ASTRA_JWT_SECRET` | um segredo de dev (commitado) | Chave HMAC que assina o JWT. **Em produção, defina um valor aleatório forte** (≥ 32 bytes) — quem tem o segredo forja qualquer token. |
| `ASTRA_CORS_ORIGINS` | `http://localhost:5173,http://localhost:3000` | Origens liberadas no CORS (front-end). |
| `SPRING_DATASOURCE_URL` / `_USERNAME` / `_PASSWORD` | — (só produção) | Conexão com o Postgres de produção. |

Ajuste fino em `application.properties`: `astra.jwt.expiration-minutes`
(default `15`), `astra.jwt.refresh-expiration-days` (default `30`).

**Frontend**

| Variável | Default (dev) | Descrição |
|---|---|---|
| `VITE_API_URL` | `http://localhost:8080` | Base URL da API. |

## API

Documentação interativa completa no **Swagger UI** (`/swagger-ui.html`,
disponível só em dev — desativado em produção). Grupos principais:

- **Auth:** `POST /auth/register`, `POST /auth/login`, `POST /auth/refresh`, `POST /auth/logout`
- **User:** `GET`/`PUT /me`, `POST /me/avatar`, `GET /users/{id}/avatar`
- **Tracking:** `POST`/`GET /sessions`, `POST`/`GET /categories`
- **Stats:** `GET /dashboard`, `GET /heatmap`, `GET /ranking?period=DAILY|WEEKLY|MONTHLY&scope=GLOBAL|FRIENDS`
- **Learning:** `POST`/`GET /courses`, `GET /courses/{id}`, `POST .../modules`, `PATCH .../modules/{id}`, `PUT`/`GET /goals`
- **Roadmap:** `POST`/`GET /roadmaps`, `GET /roadmaps/{id}`, `POST .../steps`, `PATCH .../steps/{id}`, `POST`/`GET`/`DELETE /steps/{id}/pins`
- **Social:** `POST`/`GET /friends`, `GET /friends/requests`, `POST /friends/{id}/accept`, `DELETE /friends/{id}`
- **Chat:** `GET /chat/conversations`, `GET /chat/{friendId}/messages`, `POST /chat/{friendId}/read`, WebSocket `/ws` (STOMP)
- **Admin:** `GET /admin/users`, `POST /admin/users/{id}/ban|unban`, `DELETE /admin/users/{id}` — exige `ROLE_ADMIN`

Tudo (exceto `/auth/**`, `/ws/**` e o avatar) exige `Authorization: Bearer <token>`.
Erros seguem um shape padrão (`timestamp, status, error, message, path, fieldErrors`).

## Arquitetura

**Backend** — monólito modular, **pacote-por-feature**. Cada módulo é um pacote sob
`com.astra` e carrega suas próprias camadas (controller / service / repository /
entity / dto). Regra de fronteira: módulos conversam por *services públicos*, nunca
acessando o repository ou a entity do vizinho — mantendo o grafo de dependências
**sem ciclos**.

```
com.astra
├── user       → autenticação (JWT + refresh token), perfil, roles/admin
├── tracking   → Session, Category            (o núcleo)
├── learning   → Course, CourseModule, Goal
├── roadmap    → Roadmap, RoadmapStep, CourseStepLink (o "pin")
├── social     → Friendship (pedidos de amizade)
├── chat       → Message, WebSocket/STOMP
├── stats      → dashboard, heatmap, streak, ranking (só leitura sobre os domínios)
└── shared     → config, security, exceptions, base
```

**Frontend** — SPA em `frontend/`, uma página por rota sob `src/pages`, componentes
de UI reutilizáveis (shadcn) em `src/components/ui`, e componentes de domínio
(diagrama de roadmap, timer Pomodoro, heatmap etc.) em `src/components`.

## Status

**Em produção.** Backend e frontend completos e implantados de ponta a ponta —
sessões, dashboard, heatmap, cursos/módulos, metas, roadmaps (com diagrama
interativo), amigos, chat em tempo real, ranking e administração.
Em polimento contínuo de UX e correções.
