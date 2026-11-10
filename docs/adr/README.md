# Decisões de arquitetura (ADRs)

Cada arquivo registra uma decisão: o contexto, o que foi decidido e o que isso custa. Uma decisão não é apagada quando muda; ela ganha o status "Substituído por ADR NNNN" e um ADR novo explica a mudança.

| ADR | Decisão | Status |
|---|---|---|
| [0001](0001-registrar-decisoes-em-adrs.md) | Registrar as decisões de arquitetura em ADRs | Aceito |
| [0002](0002-arquitetura-hexagonal-modulo-a-modulo.md) | Migrar o backend para arquitetura hexagonal, módulo a módulo | Aceito |
| [0003](0003-estrutura-de-pacotes.md) | Estrutura de pacotes de cada módulo | Aceito |
| [0004](0004-nomes-de-casos-de-uso-portas-e-adaptadores.md) | Nomes de casos de uso, portas e adaptadores | Aceito |
| [0005](0005-dominio-rico.md) | Domínio rico nos módulos com regra de negócio | Aceito |
| [0006](0006-estrategia-de-mapeamento.md) | Estratégia de mapeamento por tipo de caso de uso | Aceito |
| [0007](0007-comunicacao-entre-modulos.md) | Módulos conversam só por portas | Aceito |
| [0008](0008-atalhos-conscientes.md) | Atalhos conscientes nos módulos de cadastro | Substituído por [0015](0015-sem-atalho-de-porta-de-entrada.md) |
| [0009](0009-validacao-e-excecoes.md) | Onde validar e quais exceções lançar | Aceito |
| [0010](0010-estrategia-de-testes.md) | Estratégia de testes | Aceito |
| [0011](0011-protecao-das-fronteiras.md) | Proteção das fronteiras com visibilidade e ArchUnit | Aceito |
| [0012](0012-tempo-real-e-integracoes-externas.md) | Tempo real, APIs externas e hora do sistema atrás de portas | Aceito |
| [0013](0013-agregados-e-concorrencia.md) | Agregados e concorrência | Aceito |
| [0014](0014-fluxo-de-trabalho-da-migracao.md) | Fluxo de trabalho no Git durante a migração | Aceito |
| [0015](0015-sem-atalho-de-porta-de-entrada.md) | Todo módulo tem porta de entrada; cadastro só dispensa o mapeamento | Aceito |

## Modelo

```markdown
# ADR NNNN: Título no infinitivo

- Status: Proposto | Aceito | Substituído por ADR NNNN
- Data: AAAA-MM-DD

## Contexto
O problema e o que existe hoje.

## Decisão
O que foi decidido, de forma que dê para conferir no código.

## Consequências
O que melhora, o que custa e o que precisa ser vigiado.

## Referências
```

A principal referência das decisões de 0002 a 0013 é *Get Your Hands Dirty on Clean Architecture*, de Tom Hombergs.
