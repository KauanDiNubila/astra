# ADR 0002: Migrar o backend para arquitetura hexagonal, módulo a módulo

- Status: Aceito
- Data: 2026-11-06

## Contexto

O backend é um monólito Spring Boot com 12 módulos por assunto (`call`, `chat`, `github`, `learning`, `privacy`, `roadmap`, `shared`, `social`, `stats`, `tracking.category`, `tracking.session`, `user`). Dentro de cada módulo, o código é organizado em camadas: `controller`, `dto`, `service`, `entity` e `repository`.

Problemas medidos no commit `fe14776`:

- A entidade JPA é também o modelo de negócio (`Session` com `@Setter` e nenhuma regra dentro).
- Services largos, que falam direto com o Spring Data: `ChatService` (449 linhas), `CallService` (360), `UserService` (340), `CourseService` (339).
- Módulos usam service, entidade e repositório uns dos outros, com três ciclos: `tracking.session ↔ tracking.category`, `tracking.session ↔ learning` e `shared ↔ user`.
- Nenhuma classe é package-private e nada verifica as dependências.
- Os 48 testes são de integração; não há teste unitário de regra.

## Decisão

- Adotar a arquitetura hexagonal (portas e adaptadores) como estilo do backend.
- Migrar um módulo por vez, começando por `tracking.session`, que é o núcleo do produto, é pequeno e está em dois dos três ciclos.
- Durante a migração, módulos antigos e novos convivem. Um módulo só é considerado migrado quando segue os ADRs 0003 a 0012 e passa no teste de arquitetura (ADR 0011).
- As rotas da API, o JSON, os códigos de erro e o esquema do banco não mudam por causa da migração.
- A ordem prevista: `tracking.session`, `tracking.category`, `social`, `stats`, `learning`, `roadmap`, `user` (com a segurança que hoje está no `shared`), `chat`, `call`, `github`, `privacy`. A ordem pode mudar, e cada troca é anotada no item de acompanhamento (ADR 0014).

## Consequências

- O domínio fica livre de JPA e de Spring, e cada regra passa a ter um lugar.
- Os ciclos entre módulos somem, e o build passa a impedir que voltem.
- Muito mais arquivos: a proposta tem cerca de 520 arquivos Java em `main`, contra 216 hoje.
- Durante meses, o código terá dois estilos. Os ADRs e o teste de arquitetura dizem qual é qual.

## Referências

- Hombergs, cap. 1, "What's Wrong with Layers?": banco como centro, atalhos, testes difíceis, casos de uso escondidos.
- Hombergs, cap. 2, "Inverting Dependencies": Clean Architecture e arquitetura hexagonal.
- Hombergs, cap. 12, "Experience is Queen": experimentar primeiro num módulo pequeno.
