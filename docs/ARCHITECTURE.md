# Arquitetura

## Princípio central

A unidade fundamental é a **`session`** (tempo focado: quanto, quando, de que
categoria e, opcionalmente, ligada a qual curso). Todo dado derivado —
ranking, heatmap, streak, estatísticas, progresso — é **agregação sobre
`session`** e **nunca vira tabela**. Guardar isso em tabela seria duplicar dado.

Consequências de design:
- `session.started_at` é `timestamptz` (UTC no banco). "Hoje" é sempre derivado
  no fuso fixo `America/Sao_Paulo`, para que o dia seja consistente para todos
  (ranking diário reseta à meia-noite de Brasília).
- Pomodoro (com/sem pausa) e registro manual só mudam a tela; o que é salvo é
  sempre um número de **minutos focados**. A sessão não sabe de onde veio.

## Monólito modular (pacote-por-feature)

O topo é organizado por domínio; cada módulo carrega suas próprias camadas
(controller / service / repository / entity / dto) dentro.

```
com.astra
├── user          → User, autenticação (JWT), perfil
├── tracking      → Session, Category            (o núcleo)
├── learning      → Course, CourseModule, Goal
├── roadmap       → Roadmap, RoadmapStep, CourseStepLink
├── stats         → ranking, heatmap, streak (só leitura sobre Session)
└── shared        → config, security, exceptions, base
```

**Regra de fronteira:** módulos conversam por *services públicos*, nunca acessando
o repository ou a entity do módulo vizinho. Ex.: `stats` lê dados de sessão por um
service que `tracking` expõe — não injeta `SessionRepository` direto. O módulo
`stats` não tem entidade própria (só faz agregações).

Cada módulo é um pacote top-level sob `com.astra`, o que deixa o projeto pronto
para formalizar as fronteiras com **Spring Modulith** depois, sem refatorar.

### Notas de nomenclatura
- A entidade do curso se chama **`CourseModule`** (mapeada à tabela `module`) para
  não colidir com `java.lang.Module`. Por isso **não** existe pacote `module`
  (identificador restrito em Java).
- `users` no plural porque `user` é palavra reservada no Postgres.
- `position` em vez de `order` (também reservado).

## Schema (9 tabelas)

Chaves `uuid`. Restrições (`not null`, `unique`, `default`) vivem nas migrations
do Flyway, não no diagrama.

| Tabela | Campos principais | Observações |
|---|---|---|
| `users` | id, name, email (unique), password_hash, created_at | |
| `category` | id, user_id→users, name, color | |
| `course` | id, user_id→users, title, platform, status | |
| `module` | id, course_id→course, title, position, completed | entity `CourseModule` |
| `session` | id, user_id→users, category_id→category, course_id→course (nullable), focused_minutes, started_at, note, created_at | núcleo |
| `goal` | id, user_id→users, type (DAILY/WEEKLY), target_hours | |
| `roadmap` | id, owner_id→users (nullable), title, source | owner nulo = pré-definido |
| `roadmap_step` | id, roadmap_id→roadmap, title, position | |
| `course_step_link` | id, course_id→course, step_id→roadmap_step, status, rating | o "pin" |

Campos opcionais por design:
- `session.course_id` nulo → sessão de **trabalho** (não se liga a curso).
- `roadmap.owner_id` nulo → roadmap **pré-definido**; preenchido → criado pelo usuário.

## Decisões que não mudam

- **Sem scraping** de plataformas (Alura, Udemy…). O usuário é a fonte da verdade
  do progresso; o app calcula e agrega.
- **Dado derivado não vira tabela** (cache em memória depois, se pesar — Caffeine,
  nunca Redis pago).
- **MVC bloqueante com JPA** — nada de WebFlux/R2DBC neste projeto.

## Hospedagem (fase futura)

- Banco: **Neon** (Postgres persistente, escala a zero e volta em <500ms).
- App: **VM Oracle Cloud Always Free** + Docker Compose + Caddy (HTTPS).
- Fase 2: **GraalVM native image** (RAM em dezenas de MB, startup em ms).
