# ADR 0009: Onde validar e quais exceções lançar

- Status: Aceito
- Data: 2026-11-06

## Contexto

Hoje a validação de formato depende do `@Valid` no controller, então só vale para a web. As regras de negócio ficam em `.filter(...)` repetidos nos services. Todas as falhas viram `NotFoundException` ou `ConflictException` genéricas.

## Decisão

A pergunta que decide é: **a validação precisa do estado atual do modelo?**

| Tipo | Onde fica | Exemplo |
|---|---|---|
| Validação de entrada (não precisa do modelo) | no construtor do comando | minutos maiores que zero, nota até 500 caracteres |
| Regra sobre uma entidade que o caso de uso carrega | na entidade de domínio | `session.belongsTo(userId)` |
| Regra que é uma checagem simples, sem carregar a entidade | no caso de uso, antes de mexer no domínio, por uma porta de saída | `checkCategoryOwnerPort.isOwner(...)` |

- O `@Valid` no `...Request` pode continuar, para devolver as mensagens de validação da web como hoje. O comando valida de novo, e é ele que garante a regra.
- "Não existe" e "não é seu" continuam a mesma `NotFoundException`, para não revelar se um id existe.
- Regras de negócio que recusam uma ação ganham exceção com nome próprio, que estende `ConflictException` ou outra exceção comum do `shared` (por exemplo, `CategoryInUseException`). O status HTTP continua sendo decidido só pelo `GlobalExceptionHandler`.

## Consequências

- A regra de formato vale para qualquer chamador, não só para a web.
- Cada regra de negócio é fácil de achar: pelo nome da exceção ou pelo método da entidade.
- Algumas validações ficam em dois lugares (anotação e comando) enquanto quisermos as mensagens atuais da API.

## Referências

- Hombergs, cap. 4, "Validating Input", "Validating Business Rules" e "The Power of Constructors".
