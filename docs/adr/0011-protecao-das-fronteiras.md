# ADR 0011: Proteção das fronteiras com visibilidade e ArchUnit

- Status: Aceito
- Data: 2026-11-06

## Contexto

Hoje todas as classes são `public` e nenhum teste confere as dependências. Foi assim que os três ciclos entre módulos entraram sem ninguém perceber. Uma regra que só existe na cabeça de quem programa não sobrevive a um prazo apertado.

## Decisão

- **Visibilidade:** classes de `adapter/...` e de `application/service` são package-private. Portas, comandos, modelos de saída e classes de domínio são `public`. As exceções são os services com atalho do ADR 0008.
- **ArchUnit:** um `ArchitectureTest` roda em todo build e confere:
  - `domain` não depende de Spring, JPA, `application` nem `adapter`;
  - `application` não depende de `adapter`;
  - um módulo só depende de `application.port.in` de outro módulo, além do `shared`;
  - nenhum ciclo entre módulos.
- Durante a migração, as regras valem só para os módulos já migrados. Cada pull request de migração acrescenta o módulo à lista. Os ciclos que ainda existem entram como exceções conhecidas e saem da lista quando forem resolvidos; a lista só pode diminuir.
- Módulos de build separados (Maven multi-módulo) ficam para quando a estrutura estiver estável, e exigem um ADR novo.

## Consequências

- O compilador e o build barram o atalho, não a memória de quem programa.
- Uma dependência de teste nova (ArchUnit) e regras para manter quando a estrutura mudar.

## Referências

- Hombergs, cap. 10, "Enforcing Architecture Boundaries": visibilidade, verificação pós-compilação e artefatos de build.
