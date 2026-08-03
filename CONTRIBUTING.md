# Como contribuir

Este projeto usa um fluxo enxuto inspirado no Git Flow, pensado para ser
compartilhado — quem quiser colaborar **faz um fork** e abre PR.

## Fluxo de branches

| Branch | Papel |
|---|---|
| `main` | Estável. Só recebe merge de `development` em pontos de release (com tag). |
| `development` | Integração. É onde o trabalho do dia a dia é reunido. |
| `feature/<nº-issue>-<slug>` | Uma branch por issue, saindo de `development`. |

Também: `fix/…`, `docs/…`, `chore/…`, `refactor/…` conforme o tipo do trabalho.

Fluxo típico:

```bash
git switch development
git switch -c feature/12-registrar-sessao
# ... trabalha, commita ...
git push -u origin feature/12-registrar-sessao
# abre PR: feature/... -> development
```

Quem forkou: PR a partir do fork para a branch `development` deste repositório.

## Commits — Conventional Commits

Formato: `<tipo>(escopo opcional): descrição no imperativo`.

Tipos: `feat`, `fix`, `docs`, `chore`, `refactor`, `test`, `build`, `ci`.

```
feat(tracking): registrar sessão de estudo
fix(session): descontar pausa dos minutos focados
docs: adicionar guia de arquitetura
test(tracking): cobrir criação de sessão com Testcontainers
```

Referencie a issue no corpo ou no rodapé: `Closes #12`.

## Pull Requests

- PR sempre para `development` (nunca direto para `main`).
- Título no padrão de commit; descrição com o "porquê" e `Closes #<issue>`.
- Build passando: `./mvnw verify`.
- Um PR por issue, escopo pequeno.

## Issues

- **Milestones** = as fatias do projeto (ver os milestones do repositório).
- **Uma issue por tarefa.** Título curto no imperativo; corpo explica o *porquê*
  e critérios de "pronto".

### Labels

| Label | Uso |
|---|---|
| `type:feature` | Nova funcionalidade |
| `type:fix` | Correção de bug |
| `type:docs` | Documentação |
| `type:test` | Testes |
| `type:chore` | Infra, build, config |
| `type:refactor` | Refatoração sem mudar comportamento |
| `module:user` · `module:tracking` · `module:learning` · `module:roadmap` · `module:stats` · `module:shared` | Módulo afetado |
| `good first issue` | Bom ponto de entrada para quem forkou |

## Ambiente local

Pré-requisitos: **Java 21** e **Docker Desktop** ligado.

```bash
./mvnw spring-boot:run   # sobe o Postgres via Docker Compose e a app
./mvnw test              # testes de integração (Testcontainers)
```

## Convenções de código

- Código e comentários em **inglês**; interface em **português**.
- Pacote-por-feature: cada módulo carrega suas camadas dentro; módulos conversam
  por services públicos, nunca acessando repository/entity do vizinho. Cada módulo
  é um pacote top-level sob `com.astra` (`user`, `tracking`, `learning`, `roadmap`,
  `stats`, `shared`).
