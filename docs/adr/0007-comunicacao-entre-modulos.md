# ADR 0007: Módulos conversam só por portas

- Status: Aceito
- Data: 2026-11-06

## Contexto

Hoje um módulo usa service, entidade e repositório de outro. Exemplos: `SessionService` usa o `CategoryRepository`, o `CourseService` e o `GitHubRepositoryRepository`; a entidade `Session` tem `@ManyToOne` para `Category`; o `CategoryService` usa o `SessionRepository`; a segurança no `shared` usa o `UserService`. Isso cria três ciclos, e o `DataExportService` depende de 11 services de 9 módulos.

## Decisão

- Um módulo só usa de outro módulo as **portas de entrada** dele (`application.port.in`), incluindo as consultas.
- Quem precisa de outro módulo declara uma **porta de saída** própria e a implementa com um adaptador em `adapter/out/<modulo>` que chama a porta de entrada do outro lado. Exemplo: `CheckCategoryOwnerPort` (sessão) → `CategoryOwnerAdapter` → `GetCategoryOwnerQuery` (categoria).
- Entre módulos só passam ids e modelos de saída. Nenhuma entidade, de domínio ou JPA, cruza a fronteira. Relações JPA entre módulos (`@ManyToOne`) viram colunas de id.
- A segurança (filtro JWT, OAuth, STOMP) sai do `shared` e vai para `user/adapter/in/security`.
- Exportação de dados: o `privacy` define a porta de saída `ExportUserDataPort` (em `application/port/out`), e cada módulo implementa a sua parte em `adapter/in/privacy`; a ordem das seções vem de `@Order`. O `privacy` passa a não conhecer nenhum módulo. É a única porta de saída que outro módulo pode usar, e o `ArchitectureTest` só libera isso para `adapter/in/privacy`.
- Eventos entre módulos (como `UserRegisteredEvent`) continuam permitidos: quem publica usa uma porta de saída, e quem escuta é um adaptador de entrada em `adapter/in/event`.
- Quando dois módulos precisariam perguntar coisas um ao outro, a dependência é invertida: o módulo que pergunta declara a porta de saída, e o outro a implementa em `adapter/in/<módulo que pergunta>`, chamando a própria porta de entrada. É o mesmo padrão da exportação. Assim, `tracking.session` implementa `CheckCategoryInUsePort` (categoria) e `LoadLastStudiedPort` (learning), e a dependência fica só no sentido `session` → `category` e `session` → `learning`.
- Nenhum ciclo entre módulos é permitido (ADR 0011). O `ArchitectureTest` confere isso com `modulesAreFreeOfCycles`.

## Consequências

- Cada módulo pode mudar por dentro sem quebrar os outros.
- Um adaptador pequeno a cada encontro entre módulos.
- Consultas que hoje usam `JOIN` entre tabelas de módulos diferentes precisam ser revistas caso a caso; projeções de leitura podem continuar no adaptador de persistência quando o custo de juntar em memória for alto, desde que o resultado não exponha entidade de outro módulo.

## Referências

- Hombergs, cap. 3: cada módulo como um contexto delimitado com pontos de entrada e saída dedicados.
- Hombergs, cap. 10, "Boundaries and Dependencies".
- Hombergs, cap. 2, "Dependency Inversion Principle" (exportação de dados).
