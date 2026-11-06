# ADR 0003: Estrutura de pacotes de cada módulo

- Status: Aceito
- Data: 2026-11-06

## Contexto

Os pacotes de hoje (`controller`, `service`, `entity`, `repository`, `dto`) dizem a camada técnica, mas não o papel de cada classe na arquitetura. Não dá para olhar a árvore e saber o que o módulo faz, nem o que é entrada, núcleo ou saída.

## Decisão

Todo módulo migrado segue esta estrutura:

```
<modulo>/
├─ adapter/
│  ├─ in/<tipo>/        quem chama o núcleo: web, websocket, event, privacy, security
│  └─ out/<tipo>/       o que o núcleo usa de fora: persistence, outro módulo, API externa
├─ application/
│  ├─ port/in/          portas de entrada, comandos e modelos de saída
│  ├─ port/out/         portas de saída
│  └─ service/          casos de uso
└─ domain/              modelo de domínio, sem Spring e sem JPA
```

- `<tipo>` em `adapter/in` e `adapter/out` é o nome da tecnologia ou do módulo do outro lado (`persistence`, `category`, `api`, `hibp`).
- O modelo do JSON (`...Request`) fica em `adapter/in/web`. O comando do caso de uso fica em `application/port/in`.
- `shared` fica só com o que é comum de verdade: `CurrentUserProvider`, exceções, eventos, configuração, criptografia e o tratamento global de erros (`shared/adapter/in/web`).

## Consequências

- Uma caixa do diagrama de arquitetura corresponde a uma pasta.
- Trocar um adaptador é implementar as portas numa pasta nova e apagar a antiga.
- Mais pastas, e nomes mais técnicos. Exige disciplina para não criar pasta fora do padrão.

## Referências

- Hombergs, cap. 3, "An Architecturally Expressive Package Structure".
