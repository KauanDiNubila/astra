# ADR 0010: Estratégia de testes

- Status: Aceito
- Data: 2026-11-06

## Contexto

Os 48 testes de hoje sobem a aplicação inteira com Postgres (Testcontainers) e testam pela API. São bons para garantir que a API não muda, mas não existe teste unitário de regra, e todo teste é lento.

## Decisão

| Peça | Tipo de teste | Ferramenta |
|---|---|---|
| Entidade de domínio | unitário | JUnit, sem Spring |
| Caso de uso | unitário, com mock das portas | JUnit + Mockito |
| Adaptador web | integração | `@WebMvcTest`, com mock da porta de entrada |
| Adaptador de persistência | integração com banco real | `@DataJpaTest` + Testcontainers |
| Caminhos principais | sistema | `@SpringBootTest`, os testes de integração atuais |

- Os testes atuais não são apagados. Eles são a rede de segurança da migração: se continuam passando, a API não mudou.
- Ao migrar um módulo, os testes novos são escritos na mesma estrutura de pacotes do código (`domain`, `application/service`, `adapter/...`).
- Se um caso de uso precisar de muitos mocks, isso é tratado como sinal de que a porta ou o service está largo demais.

## Consequências

- Regra testada em milissegundos, sem banco.
- Mais testes para manter; testes com mock podem quebrar numa refatoração interna.

## Referências

- Hombergs, cap. 7, "Testing Architecture Elements": pirâmide de testes e o tipo de teste de cada elemento.
