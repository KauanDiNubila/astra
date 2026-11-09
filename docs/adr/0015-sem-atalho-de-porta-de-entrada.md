# ADR 0015: Todo módulo tem porta de entrada; cadastro só dispensa o mapeamento

- Status: Aceito
- Data: 2026-11-09

## Contexto

O ADR 0008 permitia dois atalhos nos casos de uso de cadastro: pular a porta de entrada (o controller chama o service direto, e o service é `public`) e não ter mapeamento com a persistência.

Ao migrar a categoria, conferimos os lugares listados:

- `tracking.category`: criar, listar e apagar. Só a web chama.
- `learning`, metas: o `goalsForUser` já é usado pelo `stats`, então pelo próprio critério de saída do ADR 0008 as metas precisariam de porta de entrada.
- `roadmap`, fixados e recursos de etapa: pequenos, a confirmar na migração.

O atalho de porta de entrada valeria em dois ou três lugares e economizaria poucas interfaces de um método. Em troca, o projeto teria dois estilos, exceções a mais no `ArchitectureTest` e a pergunta "esse é atalho?" a cada módulo novo.

## Decisão

- **Todo caso de uso tem porta de entrada**, inclusive os de cadastro. O service é package-private, como em qualquer módulo (ADR 0011). O atalho "pular a porta de entrada" deixa de existir.
- **Cadastro pode continuar sem mapeamento com a persistência**: a entidade do domínio carrega as anotações JPA, e não existe `...JpaEntity` nem mapper. Vale para a categoria, as metas e os fixados e recursos do roadmap.
  - As portas de saída continuam existindo; o service não usa o Spring Data direto.
  - No `ArchitectureTest`, o domínio desses módulos pode depender de `jakarta.persistence`, e essa exceção fica nomeada por módulo.
- **Critério de saída do "sem mapeamento"**, mantido do ADR 0008: quando o caso de uso ganhar uma regra que precise do estado do modelo (além de "existe" e "é do usuário"), a entidade ganha `...JpaEntity` e mapper.

## Consequências

- Todos os módulos têm a mesma estrutura de pacotes e o mesmo caminho do controller ao service.
- Algumas interfaces de um método a mais nos módulos de cadastro.
- Fica uma diferença só, e restrita à persistência: nos cadastros, o domínio conhece JPA.

## Referências

- Substitui o ADR 0008.
- Hombergs, cap. 8, "No Mapping" Strategy: a entidade compartilhada entre camadas, aceitável enquanto o caso de uso for simples.
- Hombergs, cap. 11, "Why Shortcuts Are Like Broken Windows".
