# ADR 0004: Nomes de casos de uso, portas e adaptadores

- Status: Aceito
- Data: 2026-11-06

## Contexto

Com dezenas de classes pequenas por módulo, o nome é o que permite achar o código. Nomes genéricos como `SessionService` ou `SessionRepository` escondem o que a classe faz.

## Decisão

| Peça | Regra | Exemplo |
|---|---|---|
| Caso de uso que muda dados | verbo do negócio, como a pessoa diria; `Create`, `Update` e `Delete` só se descreverem bem | `RegisterSessionUseCase`, `LinkRepositoryToSessionUseCase`, `DeleteSessionUseCase` |
| Consulta | `Get...Query` ou `List...Query` | `GetFocusedMinutesQuery`, `ListSessionsQuery` |
| Comando | nome do caso de uso + `Command` | `RegisterSessionCommand` |
| Service | nome do caso de uso + `Service`; consultas agrupadas em `...QueryService` | `RegisterSessionService`, `SessionQueryService` |
| Porta de saída | o que ela faz com o quê + `Port`, sem `Save` genérico | `CreateSessionPort`, `LoadSessionPort`, `UpdateSessionRepositoryPort`, `CheckCategoryOwnerPort` |
| Adaptador de persistência | agregado + `PersistenceAdapter` | `SessionPersistenceAdapter` |
| Entidade JPA | nome do domínio + `JpaEntity` | `SessionJpaEntity` |
| Repositório do Spring Data | `SpringData` + nome + `Repository` | `SpringDataSessionRepository` |
| Adaptador para outro módulo | o que ele responde + `Adapter` | `CategoryOwnerAdapter` |

- Não existe `...Impl`. O nome diz o papel da classe na arquitetura.
- Classes, métodos e pacotes em inglês, como no resto do código.

## Consequências

- A pasta `application/port/in` vira o índice do que o módulo faz.
- Escolher bons verbos exige conversa; vale perguntar "o que o usuário está fazendo?" antes de usar `Create` ou `Update`.

## Referências

- Hombergs, cap. 5, "Slicing Controllers": `RegisterAccount` em vez de `CreateAccount`.
- Hombergs, cap. 6, "Slicing Port Interfaces": `LoadAccountPort`, `UpdateAccountStatePort`, `CreateAccountPort`.
- Hombergs, cap. 4, "Read-Only Use Cases": consultas com nome próprio.
