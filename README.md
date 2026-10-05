# Astra

[![CI](https://github.com/KauanDiNubila/astra/actions/workflows/ci.yml/badge.svg?branch=development)](https://github.com/KauanDiNubila/astra/actions/workflows/ci.yml)

App web para organizar estudos e trabalho: sessões de foco (Pomodoro ou manual),
categorias, cursos com módulos, metas e roadmaps de aprendizado. Tem uma camada
social (amigos, chat em tempo real, chamadas de voz e vídeo com compartilhamento
de tela, ranking), um app para Windows e integração com GitHub, que
sincroniza atividade real (commits, PRs, issues, repositórios) e cruza com o
tempo estudado. A unidade fundamental é a **sessão**: todo tempo focado vira uma
sessão registrada, e tudo o mais — ranking, heatmap, streak, estatísticas e
progresso — é **calculado por agregação sobre as sessões**, nunca armazenado
como tabela.

**🔗 No ar:** [astra-app.dev](https://astra-app.dev)

**🖥️ App para Windows:** [baixar o instalador](https://github.com/KauanDiNubila/astra/releases/latest)
(`Astra-Setup-<versão>.exe`). O instalador não é assinado, então na primeira
vez o Windows mostra "O Windows protegeu seu PC": clique em **Mais informações**
e depois em **Executar assim mesmo**. O app se atualiza sozinho. Detalhes em
[`desktop/README.md`](desktop/README.md).

> Projeto de portfólio. Interface em português, código em inglês.

## Funcionalidades

- **Autenticação** — registro e login com JWT de acesso curto (15min) + refresh
  token opaco em cookie `httpOnly` (rotação a cada uso, reuso detectado revoga
  a sessão inteira); senha em BCrypt, recusa senha já vazada (consulta ao Have
  I Been Pwned por k-anonimato). Também dá pra entrar via **OAuth2 (Google e
  GitHub)**, sem senha.
- **Perfil** — nome, bio curta e avatar. Hierarquia de papel `USER < ADMIN <
  OWNER`.
- **Privacidade (LGPD)** — Termos de Uso e Política de Privacidade com aceite
  registrado (no cadastro, no login social e, para contas antigas, num aviso
  único), baixar uma cópia dos próprios dados em JSON e excluir a própria conta.
- **Sessões** — registrar tempo focado (Pomodoro ou manual → minutos) e listar.
- **Categorias** — separar o tempo por tipo (estudo, trabalho, leitura…).
- **Dashboard** — horas de hoje/semana/total, streak e progresso das metas.
- **Heatmap** — minutos por dia (estilo GitHub) no fuso de Brasília, com a
  atividade do GitHub combinada na mesma célula (ver seção **Integração com
  GitHub**).
- **Cursos** — cadastrar curso e módulos, acompanhar progresso (concluídos ÷ total); marcar um módulo inteiro como concluído de uma vez; ligar uma sessão a um curso; ligar repositórios do GitHub a um curso.
- **Metas** — objetivo de horas diário/semanal (bateu ou não).
- **Roadmaps** — trilhas com etapas ordenadas (próprias + pré-definidas), diagrama visual interativo, e o "pin": pendurar um curso numa etapa. Conclusão de etapa é por-usuário mesmo em roadmaps compartilhados.
- **Amigos** — pedido de amizade por e-mail, aceitar/recusar/remover.
- **Chat** — mensagens em tempo real entre amigos via WebSocket (STOMP), com
  anexo de imagem, grupos, responder mensagem (duplo-clique no desktop, swipe
  no mobile) e mensagens cifradas em repouso (AES-256-GCM).
- **Chamadas** — voz e vídeo entre amigos, 1:1 ou em grupo (até 5 pessoas),
  direto entre os navegadores (WebRTC ponto a ponto, sem servidor de mídia):
  câmera, compartilhamento de tela com o som do sistema, toque e notificação de
  chamada recebida, mudo, fone desligado, indicador de quem está falando,
  escolha de microfone/câmera/saída de som, cancelamento de eco e supressão de
  ruído. Dá pra entrar sem microfone (só ouvindo) e conectar um depois; quem
  está sem microfone aparece marcado pros outros.
- **App para Windows** — janela própria (Electron) que carrega o site: a call
  não é pausada em segundo plano, o Windows não suspende durante a chamada, a
  tela compartilhada com som não devolve a voz da call (eco), barra de título
  integrada ao app, login com Google/GitHub pelo navegador do sistema e
  atualização automática.
- **Ranking** — placar diário/semanal/mensal, global ou só entre amigos (reseta à meia-noite de Brasília).
- **Integração com GitHub** — conectar a conta via OAuth2 e sincronizar sob
  demanda: atividade (commits, PRs, issues) por período, heatmap combinado,
  repositórios/linguagens mais usados, evidência de GitHub num passo de
  roadmap concluído, sugestão automática de repositório pra uma sessão, e o
  login do GitHub visível no perfil pros amigos — só se o dono autorizar (o
  perfil do amigo mostra também repositórios públicos, seguidores e desde quando
  ele está no GitHub).
- **Administração** — painel pra listar, banir (reversível) ou excluir (cascata) contas; acesso restrito a `ADMIN`/`OWNER`.

> Princípio central: nada de "total", "streak" ou "ranking" é armazenado — tudo é **agregação sobre `session`**.

## Segurança

- JWT de acesso (15min) + refresh token opaco (30 dias, hash SHA-256 no banco,
  rotação a cada uso, família inteira revogada se um token já usado reaparecer)
  — refresh token nunca trafega em JSON, só via cookie `httpOnly` + `Secure` +
  `SameSite=None`.
- RBAC (`USER`/`ADMIN`/`OWNER`), reavaliado a cada request (ban tem efeito imediato).
- Tokens de acesso do GitHub cifrados em repouso (mesmo esquema AES-256-GCM
  do chat); falha ao descriptografar força reconexão em vez de seguir com
  valor corrompido. Estado do fluxo OAuth (`state`) é um JWT de propósito
  único, rejeitado pelo filtro de autenticação normal se vazar.
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
- Chamadas: a mídia vai direto entre os participantes (DTLS-SRTP do WebRTC). O
  relay TURN usa credenciais efêmeras (HMAC, válidas por 1h) geradas pelo
  backend, nunca uma senha fixa no front; o relay recusa repassar tráfego para
  IPs internos. A sinalização só aceita quem está na chamada e tem limite de
  taxa.
- App para Windows: a página não acessa o Node.js (`contextIsolation`,
  `sandbox`), só o domínio do Astra carrega na janela e a ponte com o app
  confere a origem de cada pedido. O login social no app usa um código de uso
  único amarrado a um desafio (no estilo PKCE), trocado de dentro do app.

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
| Tempo real | WebSocket + STOMP (chat e sinalização das chamadas) |
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
| Chamadas | WebRTC (malha ponto a ponto) |
| Lint | oxlint |

**App desktop** (`desktop/`)

| Camada | Tecnologia |
|---|---|
| Casca | Electron 44 (TypeScript) |
| Instalador | electron-builder (NSIS, por usuário, sem admin) |
| Atualização | electron-updater (GitHub Releases) |

**Produção**

| Peça | Onde |
|---|---|
| Frontend | Vercel (deploy automático a cada push) |
| Backend | VM Oracle Cloud (Docker Compose + Caddy) |
| Banco | Neon (Postgres serverless) |
| Relay das chamadas | coturn (TURN) na mesma VM Oracle |
| App Windows | GitHub Releases (workflow a cada tag `v*`) |
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

**App desktop** (opcional, Windows)

```bash
cd desktop
npm install
ASTRA_URL=http://localhost:5173 ASTRA_API_URL=http://localhost:8080 npm start
```

Sem as variáveis, o app abre o site de produção. Gerar instalador e publicar
versão: ver [`desktop/README.md`](desktop/README.md).

## Configuração (variáveis de ambiente)

**Backend** (dev usa `application.properties`, produção ativa o profile `prod`
via `SPRING_PROFILES_ACTIVE=prod` — nesse profile as variáveis abaixo **não
têm valor default**, a aplicação falha na subida se faltar alguma)

| Variável | Default (dev) | Descrição |
|---|---|---|
| `ASTRA_JWT_SECRET` | um segredo de dev (commitado) | Chave HMAC que assina o JWT. **Em produção, defina um valor aleatório forte** (≥ 32 bytes) — quem tem o segredo forja qualquer token. |
| `ASTRA_CORS_ORIGINS` | `http://localhost:5173,http://localhost:3000` | Origens liberadas no CORS (front-end). |
| `SPRING_DATASOURCE_URL` / `_USERNAME` / `_PASSWORD` | — (só produção) | Conexão com o Postgres de produção. |
| `ASTRA_TURN_SECRET` | um segredo de dev (commitado) | Segredo compartilhado com o coturn para gerar as credenciais temporárias do relay das chamadas. **Em produção, use um valor aleatório forte**, igual ao `static-auth-secret` do coturn. |
| `ASTRA_TURN_URLS` | vazio (só STUN) | Endereços do relay TURN, separados por vírgula (ex.: `turn:<ip>:3478?transport=udp`). |

Ajuste fino em `application.properties`: `astra.jwt.expiration-minutes`
(default `15`), `astra.jwt.refresh-expiration-days` (default `30`).

**Frontend**

| Variável | Default (dev) | Descrição |
|---|---|---|
| `VITE_API_URL` | `http://localhost:8080` | Base URL da API. |

**App desktop** (só para desenvolvimento)

| Variável | Default | Descrição |
|---|---|---|
| `ASTRA_URL` | `https://astra-app.dev` | Site que o app carrega. |
| `ASTRA_API_URL` | `https://api.astra-app.dev` | API usada na troca do login social. |
| `ASTRA_UPDATE_URL` | — | Feed de atualização de teste (nunca instala sozinho). |

## API

Documentação interativa completa no **Swagger UI** (`/swagger-ui.html`,
disponível só em dev — desativado em produção). Grupos principais:

- **Auth:** `POST /auth/register`, `POST /auth/login`, `POST /auth/refresh`, `POST /auth/logout`, `GET /oauth2/authorization/{google|github}` (login sem senha), `GET /auth/desktop/login` + `POST /auth/desktop/exchange` (login social do app desktop)
- **User:** `GET`/`PUT /me`, `POST /me/avatar`, `GET /users/{id}/avatar`, `POST /me/terms` (aceite dos termos), `DELETE /me` (excluir a própria conta)
- **Privacidade:** `GET /me/export` (cópia dos dados do usuário em JSON)
- **Tracking:** `POST`/`GET /sessions`, `POST`/`GET /categories`
- **Stats:** `GET /dashboard`, `GET /heatmap`, `GET /ranking?period=DAILY|WEEKLY|MONTHLY&scope=GLOBAL|FRIENDS`
- **Learning:** `POST`/`GET /courses`, `GET /courses/{id}`, `POST .../modules`, `PATCH .../modules/{id}`, `PUT`/`GET /goals`
- **Roadmap:** `POST`/`GET /roadmaps`, `GET /roadmaps/{id}`, `POST .../steps`, `PATCH .../steps/{id}`, `POST`/`GET`/`DELETE /steps/{id}/pins`
- **Social:** `POST`/`GET /friends`, `GET /friends/requests`, `POST /friends/{id}/accept`, `DELETE /friends/{id}`
- **Chat:** `GET /chat/conversations`, `GET /chat/{friendId}/messages`, `POST /chat/{friendId}/read`, WebSocket `/ws` (STOMP)
- **Chamadas:** `GET /call/ice-servers` (STUN/TURN com credenciais temporárias); sinalização pelo mesmo `/ws` — envio em `/app/call.start|join|decline|leave|signal`, eventos em `/user/queue/call`
- **GitHub:** `GET /github/connect/authorize-url`, `GET /github/status`, `POST /github/sync`, `DELETE /github/connection`, `PATCH /github/visibility`, `GET /github/activity`, `GET /github/insights`
- **Admin:** `GET /admin/users`, `POST /admin/users/{id}/ban|unban`, `DELETE /admin/users/{id}` — exige `ADMIN`/`OWNER`

Tudo (exceto `/auth/**`, `/oauth2/**`, `/github/connect/callback`, `/ws/**` e o
avatar) exige `Authorization: Bearer <token>`.
Erros seguem um shape padrão (`timestamp, status, error, message, path, fieldErrors`).

## Arquitetura

**Backend** — monólito modular, **pacote-por-feature**. Cada módulo é um pacote sob
`com.astra` e carrega suas próprias camadas (controller / service / repository /
entity / dto). Regra de fronteira: módulos conversam por *services públicos*, nunca
acessando o repository ou a entity do vizinho — mantendo o grafo de dependências
**sem ciclos**.

```
com.astra
├── user       → autenticação (JWT + refresh token, OAuth2), perfil, roles/admin
├── tracking   → Session, Category            (o núcleo)
├── learning   → Course, CourseModule, Goal
├── roadmap    → Roadmap, RoadmapStep, CourseStepLink (o "pin")
├── social     → Friendship (pedidos de amizade)
├── chat       → Message, WebSocket/STOMP
├── call       → chamadas: estado em memória, sinalização WebRTC, credenciais TURN
├── github     → conexão OAuth2, sincronização e insights do GitHub
├── stats      → dashboard, heatmap, streak, ranking (só leitura sobre os domínios)
├── privacy    → exportação dos dados do usuário (LGPD), juntando o que cada módulo expõe
└── shared     → config, security, exceptions, base
```

**Frontend** — SPA em `frontend/`, uma página por rota sob `src/pages`, componentes
de UI reutilizáveis (shadcn) em `src/components/ui`, e componentes de domínio
(diagrama de roadmap, timer Pomodoro, heatmap, tela de chamada etc.) em
`src/components`. A lógica das chamadas (conexões WebRTC, áudio, dispositivos)
fica em `src/lib/call*.ts` e `src/context/CallContext.tsx`.

**App desktop** — casca Electron em `desktop/`: não empacota o front, só abre
o site (que segue atualizando pela Vercel). O processo principal cuida da
janela, do seletor de tela, do bloqueio de suspensão durante a call, do login
social e da atualização; a página fala com ele por uma ponte pequena
(`preload.ts`).

## Status

**Em produção.** Backend e frontend completos e implantados de ponta a ponta —
sessões, dashboard, heatmap, cursos/módulos, metas, roadmaps (com diagrama
interativo), amigos, chat em tempo real, chamadas de voz e vídeo com
compartilhamento de tela, ranking, integração com GitHub, administração e app
para Windows. Em polimento contínuo de UX e correções.
