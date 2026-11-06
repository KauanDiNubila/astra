# ADR 0006: Estratégia de mapeamento por tipo de caso de uso

- Status: Aceito
- Data: 2026-11-06

## Contexto

Hoje existe um modelo só (a entidade JPA) usado do banco até o service, e um DTO montado à mão (`toDto`) para a resposta. Com o domínio rico (ADR 0005), a entidade JPA não pode mais ser o modelo de negócio.

## Decisão

| Fronteira | Caso de uso que muda dados | Consulta |
|---|---|---|
| Web ↔ aplicação | **completa**: o `...Request` da web vira um `...Command` do caso de uso | **sem mapeamento**: o modelo de saída vai direto para o JSON |
| Aplicação ↔ persistência | **duas vias**: `Session` ↔ `SessionJpaEntity`, com um `...Mapper` no adaptador | a consulta pode devolver o modelo de saída direto do adaptador (projeção) |

- Modelos de saída (`...Response`) podem ser compartilhados entre casos de uso enquanto forem iguais. Quando um caso de uso precisar de algo diferente, ganha o seu próprio.
- Nos módulos de cadastro (ADR 0008), aplicação ↔ persistência pode ser **sem mapeamento**: a entidade do domínio carrega as anotações JPA.
- Entidade de domínio nunca sai no JSON, e entidade JPA nunca sai do adaptador de persistência.

## Consequências

- Mudar uma coluna não mexe no domínio, e mudar o JSON não mexe no caso de uso.
- Mais código de mapeamento nos módulos com tratamento completo.

## Referências

- Hombergs, cap. 8, "Mapping Between Boundaries": as quatro estratégias e a recomendação de escolher por situação, com diretrizes escritas.
- Hombergs, cap. 11, "Sharing Models between Use Cases".
