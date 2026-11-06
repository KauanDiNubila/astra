# ADR 0001: Registrar as decisões de arquitetura em ADRs

- Status: Aceito
- Data: 2026-11-06

## Contexto

O backend vai mudar de estrutura (ADR 0002). Parte das escolhas são atalhos tomados de propósito, como pular a porta de entrada num cadastro simples. Sem registro, um atalho escolhido parece um atalho esquecido, e a próxima pessoa (ou eu daqui a seis meses) não sabe se pode copiar o padrão, nem quando deve sair dele.

## Decisão

- Toda decisão que muda a estrutura do código, cria uma regra para o time ou aceita um atalho vira um ADR em `docs/adr`.
- Numeração sequencial de quatro dígitos e nome do arquivo em minúsculas com hífens.
- Um ADR aceito não é editado no mérito. Se a decisão mudar, ele passa a "Substituído por ADR NNNN" e um ADR novo explica o porquê.
- Todo atalho registrado diz qual é o critério para sair dele.

## Consequências

- O porquê de cada escolha fica junto do código e entra na revisão do pull request.
- Custa escrever um texto curto a cada decisão.

## Referências

- Hombergs, cap. 11, "Taking Shortcuts Consciously": documentar a arquitetura e o porquê de cada atalho para poder reavaliar no futuro.
- Hombergs, cap. 8, "When to use which Mapping Strategy?": diretrizes do time precisam dizer o motivo.
