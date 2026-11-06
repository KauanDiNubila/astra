# ADR 0013: Agregados e concorrência

- Status: Proposto
- Data: 2026-11-06

## Contexto

Um adaptador de persistência por agregado define quais objetos são carregados e gravados juntos. A sessão é uma entidade sozinha, mas outros módulos têm grupos de objetos que precisam ficar consistentes entre si. O `call` guarda o estado das chamadas em memória, e duas pessoas entrando ou saindo ao mesmo tempo podem gerar conflito.

## Decisão (proposta, a confirmar ao migrar cada módulo)

| Módulo | Agregado | Muda só pela raiz? |
|---|---|---|
| `tracking.session` | `Session` | sim, entidade sozinha |
| `learning` | `Course` → `CourseModule` → `Lesson` | a confirmar: aulas e módulos mudam pelo `Course` ou têm portas próprias |
| `roadmap` | `Roadmap` → `RoadmapStep`; `StepCompletion` por usuário | a confirmar |
| `chat` | `ChatGroup` → `GroupMember`; `Message` separado | a confirmar |
| `call` | `Call` → `CallMember` | sim |
| `social` | `Friendship` | sim, entidade sozinha |

- Um adaptador de persistência por agregado.
- Concorrência no `call`: as transições de estado (`start`, `join`, `leave`, `decline`) ficam no domínio `Call`, e o acesso ao estado em memória passa por uma porta (`StoreCallPort`) cuja implementação garante exclusão mútua por chamada.
- Nos agregados guardados no banco, conflito de escrita simultânea será tratado quando aparecer um caso real, com `@Version` na entidade JPA.

Este ADR passa a "Aceito" (ou é substituído) quando o módulo correspondente for migrado.

## Consequências

- Fica explícito o que é gravado junto, e a regra que envolve vários objetos fica na raiz.
- Exige decidir cada agregado antes de migrar o módulo.

## Referências

- Hombergs, cap. 6, "Slicing Persistence Adapters": um adaptador por agregado.
- Hombergs, cap. 4, "A Use Case in a Nutshell": o `AccountLock` para evitar operações simultâneas na mesma conta.
