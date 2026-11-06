# ADR 0012: Tempo real, APIs externas e hora do sistema atrás de portas

- Status: Aceito
- Data: 2026-11-06

## Contexto

- O `ChatController` e o `CallService` usam o `SimpMessagingTemplate` direto: a regra da chamada conhece o WebSocket.
- O cliente da API do GitHub (`GitHubApiClient`) e a checagem de senha vazada (`PasswordBreachChecker`) moram em pacotes `service`.
- A hora é lida com `now()` em 47 lugares. Um teste não consegue fixar "hoje", o que importa para o ranking diário, semanal e mensal.

## Decisão

- **Tempo real:** o núcleo avisa por uma porta de saída (`NotifyChatPort`, `NotifyCallPort`). Quem implementa é um adaptador em `adapter/in/web` (por exemplo, `WebSocketChatNotifier`), que é ao mesmo tempo de entrada e de saída.
- **APIs externas:** cada uma fica num adaptador de saída atrás de uma porta: `adapter/out/api` no `github` (`LoadCommitsPort`, `ExchangeOAuthCodePort`), `adapter/out/hibp` no `user` (`CheckPasswordBreachPort`).
- **Hora:** um único `Clock` do Spring (`shared/config/ClockConfig`) é injetado em quem precisa de "agora". Código novo não chama `now()` sem `Clock`; o que já existe é trocado quando o módulo for migrado.

## Consequências

- O estado da chamada e do chat pode ser testado sem WebSocket, e o ranking com um dia fixo.
- Trocar um provedor externo mexe numa pasta só.
- Uma porta e um adaptador por integração.

## Referências

- Hombergs, cap. 5, "Dependency Inversion": aplicações com WebSocket precisam de uma porta de saída implementada pelo adaptador web.
- Hombergs, cap. 7, "Testing a Use Case with Unit Tests" (o `Clock` é uma decisão nossa, para tornar os casos de uso testáveis assim).
