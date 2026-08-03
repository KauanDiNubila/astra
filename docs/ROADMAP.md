# Roadmap

O trabalho é fatiado bem fino: cada fatia entrega algo rodando de ponta a ponta,
em vez de modelar tudo antes de ver funcionar. Cada **fatia = um milestone** no
GitHub; cada **tarefa = uma issue**.

Legenda de status: ✅ feito · 🔜 em andamento · ⬜ pendente

---

## Fatia 0 — Fundação
> Milestone: `Fatia 0 — Fundação`

- ✅ Gerar projeto no Spring Initializr (Boot 4.0.7, Java 21).
- ✅ Achatar estrutura (pom.xml na raiz do repo).
- ✅ Esqueleto de pacotes do monólito modular (`com.astra.{user,tracking,learning,roadmap,stats,shared}`) com `package-info.java`.
- ✅ `SecurityConfig` com `permitAll()` (auth adiada para a Fatia 2).
- ✅ Postgres no Docker (`compose.yaml` pinado, porta fixa).
- ✅ Documentação inicial (README, ARCHITECTURE, ROADMAP, CONTRIBUTING).
- ⬜ Inicializar git, branches `main` + `development`, primeiro commit e push.
- ⬜ (opcional) CI no GitHub Actions: `./mvnw verify` a cada PR.

## Fatia 1 — Núcleo: registrar tempo
> Milestone: `Fatia 1 — Núcleo`  ·  Módulos: `tracking`, `user`, `shared`

O menor incremento que já entrega "registrar tempo e ver total".

- ⬜ `V1__init.sql` (Flyway): as 9 tabelas com restrições (`not null`, `unique`, defaults, FKs).
- ⬜ Entidades JPA `User`, `Category`, `Session` + repositories.
- ⬜ `user` semente fixo (enquanto não há auth).
- ⬜ DTOs de sessão (`CreateSessionRequest`, `SessionResponse`) + Bean Validation.
- ⬜ Endpoints: criar sessão, listar sessões do usuário.
- ⬜ CRUD mínimo de categoria.
- ⬜ Tratamento de erro global (`@RestControllerAdvice`) + shape de erro padrão.
- ⬜ OpenAPI/Swagger expondo os endpoints.
- ⬜ Teste de integração com Testcontainers (criar e listar sessão).

## Fatia 2 — Autenticação (JWT)
> Milestone: `Fatia 2 — Auth`  ·  Módulos: `user`, `shared`

- ⬜ Reescrever `SecurityConfig` (remover `permitAll`, filtro JWT).
- ⬜ Registro e login (BCrypt no `password_hash`).
- ⬜ Emissão/validação de JWT.
- ⬜ Isolamento por dono: trocar o user semente pelo user do token em todos os endpoints.

## Fatia 3 — Stats básicos
> Milestone: `Fatia 3 — Stats`  ·  Módulos: `stats`, `tracking`

- ⬜ Service público em `tracking` para `stats` ler sessões (respeitando a fronteira).
- ⬜ Total de horas e horas na semana (agregação sobre `session`).
- ⬜ Dashboard mínimo (horas de hoje, da semana).

## Fatia 4 — Heatmap + streak
> Milestone: `Fatia 4 — Heatmap`

- ⬜ Agregação de horas por dia no fuso `America/Sao_Paulo` (heatmap estilo GitHub).
- ⬜ Streak (dias seguidos com sessão), calculado.

## Fatia 5 — Learning (cursos, módulos, metas)
> Milestone: `Fatia 5 — Learning`  ·  Módulo: `learning`

- ⬜ `Course` + `CourseModule` (progresso = módulos concluídos ÷ total).
- ⬜ `Goal` (meta diária/semanal — bateu ou não).
- ⬜ Ligar sessão a curso (`session.course_id`).

## Fatia 6 — Roadmaps
> Milestone: `Fatia 6 — Roadmaps`  ·  Módulo: `roadmap`

- ⬜ `Roadmap` + `RoadmapStep` (ordenados).
- ⬜ `CourseStepLink` (o "pin": status + rating 1-5).
- ⬜ 2-3 roadmaps pré-definidos (crédito à fonte, ex. roadmap.sh).

## Fatia 7 — Ranking
> Milestone: `Fatia 7 — Ranking`  ·  Módulo: `stats`

- ⬜ Ranking diário (reseta à meia-noite de Brasília).
- ⬜ Ranking semanal e mensal (mesmo dado, período diferente).

---

## Backlog (pós-MVP)

Documentado, não priorizado: flashcards com repetição espaçada (SM-2), amigos/seguir,
conquistas/badges, notas em Markdown, biblioteca de livros, importar módulos colando
texto, IA para resumos/flashcards (sugestão editável), e o deploy real
(Neon + Oracle Cloud Always Free + Caddy; depois GraalVM native image).

## Mapeamento GitHub

- **Milestones** = fatias acima.
- **Issues** = cada item `⬜`/`🔜`. Título curto no imperativo; corpo com o "porquê".
- **Labels** — ver [CONTRIBUTING.md](../CONTRIBUTING.md#labels).
