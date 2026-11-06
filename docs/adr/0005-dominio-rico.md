# ADR 0005: Domínio rico nos módulos com regra de negócio

- Status: Aceito
- Data: 2026-11-06

## Contexto

A arquitetura não impõe como implementar o domínio. Hoje o modelo é anêmico e preso ao JPA: as entidades têm `@Setter` e as regras estão espalhadas nos services e até no controller. A checagem "a sessão é sua?" aparece três vezes no `SessionService`. A conta "início mais minutos" é feita no `SessionController`. O início do dia, da semana e do mês do ranking é calculado no `RankingService`.

As regras do Astra são, em boa parte, mudanças de estado: uma chamada que começa, recebe participantes e termina; um pedido de amizade que é aceito; uma etapa de roadmap que é concluída.

## Decisão

- Nos módulos com tratamento completo, o domínio é **rico**: a entidade só permite mudanças válidas, e o caso de uso só orquestra.
- Entidades de domínio não têm setters públicos. Mudança de estado é um método com nome do negócio (`linkRepository`, `accept`, `join`).
- Uma entidade sempre nasce válida: pelo construtor ou por um método de fábrica (`Session.start(...)`).
- Ids podem ser tipados (`SessionId`) quando houver risco de trocar um id por outro.
- Nos módulos de cadastro listados no ADR 0008, o domínio pode ser anêmico.

## Consequências

- Cada regra fica num lugar só e é testada com teste unitário, sem mock (ADR 0010).
- Exige o mapeamento de duas vias com a persistência (ADR 0006), porque a entidade JPA precisa de construtor vazio e setters.
- Mais código: construtores, métodos de fábrica e mappers.

## Referências

- Hombergs, cap. 4, "Rich versus Anemic Domain Model": o livro não escolhe; escolhemos o rico pelos motivos acima.
- Hombergs, cap. 4, "Implementing the Domain Model" e "The Power of Constructors".
- Hombergs, cap. 6, fechamento: persistência como plugin permite um domínio rico.
