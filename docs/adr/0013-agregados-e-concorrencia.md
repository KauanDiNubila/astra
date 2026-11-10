# ADR 0013: Agregados e concorrência

- Status: Proposto
- Data: 2026-11-06

## Contexto

Um adaptador de persistência por agregado define quais objetos são carregados e gravados juntos. A sessão é uma entidade sozinha, mas outros módulos têm grupos de objetos que precisam ficar consistentes entre si. O `call` guarda o estado das chamadas em memória, e duas pessoas entrando ou saindo ao mesmo tempo podem gerar conflito.

## Decisão (proposta, a confirmar ao migrar cada módulo)

| Módulo | Agregado | Muda só pela raiz? |
|---|---|---|
| `tracking.session` | `Session` | sim, entidade sozinha |
| `learning` | `Course` → `CourseModule` → `Lesson`; `Goal` separado | sim, decidido na migração (#121): módulos e aulas só mudam por métodos do `Course`, e um adaptador grava o agregado inteiro em cascata |
| `roadmap` | `Roadmap` → `RoadmapStep` → links; `StepProgress` (status por pessoa) e `CoursePin` separados | sim, decidido na migração (#121): passos e links só mudam pelo `Roadmap`; status e pin ficam fora porque também valem nos roadmaps prontos, compartilhados por todos |
| `chat` | `ChatGroup` → `GroupMember`; `Message` separado (anexo à parte); foto do grupo fora do agregado | sim, decidido na migração (#121): entrar no grupo e quem pode adicionar ficam no `ChatGroup`; resposta e fixação ficam na `Message`; o limite de 3 fixadas por conversa é conferido no service, contando pela porta, por ser uma regra entre várias mensagens |
| `call` | `Call` → `CallMember` | sim |
| `social` | `Friendship` | sim, entidade sozinha |
| `user` | `User`; `OAuthConnection` e `RefreshToken` separados; avatar fora do agregado | sim, decidido na migração (#121): moderação e hierarquia de papéis ficam no `User`; o login OAuth acha a conexão antes de saber o usuário, e os refresh tokens são muitos por pessoa e giram a cada renovação; o avatar (até 2 MB) só é lido e gravado pelo seu adaptador |

- Um adaptador de persistência por agregado.
- Concorrência no `call`: as transições de estado (`start`, `join`, `leave`, `decline`) ficam no domínio `Call`, e o acesso ao estado em memória passa por uma porta (`StoreCallPort`) cuja implementação garante exclusão mútua por chamada.
- Nos agregados guardados no banco, conflito de escrita simultânea será tratado quando aparecer um caso real, com `@Version` na entidade JPA.
- Leituras que não precisam do agregado inteiro (listar roadmaps, localizar um passo) usam modelos de leitura próprios (`RoadmapOutline`, `LocatedStep`), para não carregar centenas de passos e links à toa.
- Partes do agregado criadas dentro da raiz (como módulo e aula no `Course`) recebem o id no domínio, para a resposta sair com o id sem reler o banco.

Este ADR passa a "Aceito" (ou é substituído) quando o módulo correspondente for migrado.

## Consequências

- Fica explícito o que é gravado junto, e a regra que envolve vários objetos fica na raiz.
- Exige decidir cada agregado antes de migrar o módulo.

## Referências

- Hombergs, cap. 6, "Slicing Persistence Adapters": um adaptador por agregado.
- Hombergs, cap. 4, "A Use Case in a Nutshell": o `AccountLock` para evitar operações simultâneas na mesma conta.
