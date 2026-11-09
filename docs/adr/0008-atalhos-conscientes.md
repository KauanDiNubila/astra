# ADR 0008: Atalhos conscientes nos módulos de cadastro

- Status: Substituído por ADR 0015
- Data: 2026-11-06

## Contexto

Alguns casos de uso são só cadastro: criar, listar e apagar, sem regra que dependa do estado do modelo. Aplicar a arquitetura completa neles gera services e portas que só repassam a chamada. Por outro lado, todo cadastro pode crescer, e um atalho esquecido vira o lugar onde a regra nova entra.

## Decisão

Os casos de uso abaixo podem usar dois atalhos:

- **Pular a porta de entrada**: o controller chama o service direto, e o service é `public`.
- **Sem mapeamento com a persistência**: a entidade do domínio carrega as anotações JPA.

| Módulo | Casos de uso com atalho |
|---|---|
| `tracking.category` | criar, listar e apagar categoria |
| `learning` | definir e listar metas |
| `roadmap` | fixar curso em etapa; adicionar, listar e remover recurso de etapa |

Regras que continuam valendo mesmo com atalho:

- A porta de saída continua existindo; o service não usa o Spring Data direto.
- Outros módulos continuam usando só portas de entrada (por exemplo, `GetCategoryOwnerQuery`).
- Cada classe com atalho é marcada no `ArchitectureTest` como exceção conhecida, e não com um comentário no código.

**Critério de saída:** o caso de uso deixa de ser cadastro quando ganhar uma regra que precisa do estado do modelo (além de "existe" e "é do usuário") ou um segundo chamador além da web. Nesse momento ele ganha porta de entrada, domínio sem JPA e mapper, e este ADR é atualizado.

## Consequências

- Não pagamos o preço completo onde não há regra.
- O projeto tem dois padrões. Esta lista diz exatamente onde está cada um.

## Referências

- Hombergs, cap. 11, "Skipping Incoming Ports" e "Skipping Application Services".
- Hombergs, cap. 11, fechamento: combinar quando um caso de uso deixa de ser CRUD.
- Hombergs, cap. 11, "Why Shortcuts Are Like Broken Windows".
