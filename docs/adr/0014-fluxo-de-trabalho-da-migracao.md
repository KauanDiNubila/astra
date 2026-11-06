# ADR 0014: Fluxo de trabalho no Git durante a migração

- Status: Aceito
- Data: 2026-11-06

## Contexto

A migração vai levar semanas e mexer em centenas de arquivos. O branch `main` é o que vai para produção (Vercel no push e backend por deploy manual), então ele não pode ficar parado esperando a migração, nem receber código pela metade. Um branch longo, aberto por semanas, acumularia conflitos com toda correção feita no meio do caminho.

## Decisão

- **Um único branch fixo, `main`** (trunk-based), que é o de produção. Ele se chamava `development` e foi renomeado em 2026-11-06 para seguir a convenção; o histórico não mudou. Sem GitFlow: com deploy contínuo e uma pessoa só, um segundo branch fixo só duplicaria os merges.
- **Uma tag de ponto de partida:** `pre-hexagonal`, no último commit antes da migração, para comparar e voltar se preciso.
- **Um branch e um pull request por módulo**, saindo de `main` e voltando para ele: `refactor/hexagonal-session`, `refactor/hexagonal-category` e assim por diante. Cada PR deixa o módulo inteiro migrado e o build verde.
- **Dentro do PR, commits pequenos por etapa**, cada um compilando e com os testes passando:
  1. testes que faltam para proteger o comportamento atual;
  2. domínio e portas;
  3. casos de uso;
  4. adaptadores e troca das chamadas antigas;
  5. remoção do código antigo e inclusão do módulo no `ArchitectureTest`.
- **Merge sem squash**, para os commits de cada etapa ficarem no histórico.
- **Correções urgentes** continuam saindo direto de `main`, como hoje. O branch de migração é atualizado a partir de `main` antes do merge.
- **Comportamento:** nenhum PR de migração muda rota, JSON, código de erro ou tabela. Se precisar mudar, vai num PR separado.
- **Acompanhamento:** uma issue no GitHub com a lista dos módulos (ordem do ADR 0002), marcada a cada merge.
- **Deploy:** cada merge de módulo é um deploy normal do backend, feito depois de conferir o CI.
- Decisões novas que surgirem durante a migração viram ADR no mesmo PR.

## Consequências

- `main` sempre pode ir para produção, mesmo no meio da migração.
- Cada PR é revisável sozinho e pode ser revertido sem afetar os outros módulos.
- Durante a migração, convivem módulos nos dois estilos (ADR 0002).

## Referências

- Hombergs, cap. 12, "Experience is Queen": começar por um módulo pequeno e ajustar o estilo com a experiência.
